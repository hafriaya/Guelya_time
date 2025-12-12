package repository;

import config.Neo4jConfig;
import model.User;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.time.LocalDate;

public class UserRepository {

    private final Driver driver = Neo4jConfig.getDriver();

    private long getNextId() {
        try (Session session = driver.session()) {
            return session.writeTransaction(tx -> {
                Result result = tx.run(
                        "MERGE (c:Counter {name:'UserId'}) " +
                                "ON CREATE SET c.value = 1 " +
                                "ON MATCH SET c.value = c.value + 1 " +
                                "RETURN c.value AS id"
                );
                return result.single().get("id").asLong();
            });
        }
    }

    public boolean saveUser(User user) {
        long id = getNextId();
        user.setId(id);

        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("CREATE (u:User {id:$id, name:$name, email:$email, password:$password, dateInscription:$date})",
                        Values.parameters(
                                "id", user.getId(),
                                "name", user.getName(),
                                "email", user.getEmail(),
                                "password", user.getPassword(),
                                "date", user.getDateInscription() // LocalDate is supported
                        ));
                return null;
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public User findByEmail(String email) {
        try (Session session = driver.session()) {
            Result result = session.run(
                    "MATCH (u:User {email: $email}) RETURN u",
                    Values.parameters("email", email)
            );

            if (!result.hasNext()) return null;

            Record record = result.next();
            Value u = record.get("u");

            long id = u.get("id").asLong();
            String name = u.get("name").asString();
            String password = u.get("password").asString();
            LocalDate date = u.get("dateInscription").asLocalDate();

            return new User(id, name, email, password, date);
        }
    }

    public boolean authenticateUser(String email, String password) {
        User user = findByEmail(email);
        if (user == null) return false;
        return user.getPassword().equals(password); // Hash comparison if needed
    }
}
