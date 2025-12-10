package repository;

import config.Neo4jConfig;
import model.User;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

public class UserRepository {

    public boolean saveUser(User user) {
        try (Session session = Neo4jConfig.getDriver().session()) {
            session.writeTransaction(tx -> {
                tx.run("CREATE (u:User {email: $email, password: $password, name: $name, dateInscription: $date})",
                        Values.parameters(
                                "email", user.getEmail(),
                                "password", user.getPassword(),
                                "name", user.getName(),
                                "date", user.getDateInscription()
                        ));
                return null;
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean authenticateUser(String email, String password) {
        try (Session session = Neo4jConfig.getDriver().session()) {
            Result result = session.run(
                    "MATCH (u:User {email: $email, password: $password}) RETURN u",
                    Values.parameters("email", email, "password", password)
            );
            return result.hasNext();
        }
    }

    // Nouvelle méthode
    public User findByEmail(String email) {
        try (Session session = Neo4jConfig.getDriver().session()) {
            Result result = session.run(
                    "MATCH (u:User {email: $email}) RETURN u.name AS name, u.email AS email, u.password AS password, u.dateInscription AS date",
                    Values.parameters("email", email)
            );

            if (result.hasNext()) {
                Record record = result.next();
                User user = new User(
                        record.get("email").asString(),
                        record.get("password").asString(),
                        record.get("name").asString()
                );
                user.setDateInscription(record.get("date").asString());
                return user;
            }
            return null;
        }
    }
}