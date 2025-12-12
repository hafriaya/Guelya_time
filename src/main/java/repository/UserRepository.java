package repository;

import config.Neo4jConfig;
import model.User;
import org.neo4j.driver.*;

import org.mindrot.jbcrypt.BCrypt;
import org.neo4j.driver.Record;

public class UserRepository {
    private final Driver driver = Neo4jConfig.getDriver();

    // Auto-increment ID
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

    // Check if email already exists
    public boolean emailExists(String email) {
        try (Session session = driver.session()) {
            Result result = session.run(
                    "MATCH (u:User {email: $email}) RETURN u",
                    Values.parameters("email", email)
            );
            return result.hasNext();
        }
    }

    // Save user
    public boolean saveUser(User user) {
        if (emailExists(user.getEmail())) {
            return false; // email already exists
        }

        long id = getNextId();
        user.setId(id);

        // Hash password
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);

        // Set registration date
        if (user.getDateInscription() == null) {
            user.setDateInscription(java.time.LocalDate.now());
        }

        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "CREATE (u:User {id:$id, email:$email, password:$password, name:$name, dateInscription:$date})",
                        Values.parameters(
                                "id", user.getId(),
                                "email", user.getEmail(),
                                "password", user.getPassword(),
                                "name", user.getName(),
                                "date", user.getDateAsEpoch()
                        )
                );
                return null;
            });
            return true;
        }
    }

    // Authenticate user
    public boolean authenticateUser(String email, String password) {
        User user = findByEmail(email);
        if (user == null) return false;
        return BCrypt.checkpw(password, user.getPassword());
    }

    // Find user by email
    public User findByEmail(String email) {
        try (Session session = driver.session()) {
            Result result = session.run(
                    "MATCH (u:User {email: $email}) " +
                            "RETURN u.id AS id, u.name AS name, u.email AS email, u.password AS password, u.dateInscription AS date",
                    Values.parameters("email", email)
            );

            if (!result.hasNext()) return null;

            Record record = result.next();
            User user = new User(
                    record.get("email").asString(),
                    record.get("password").asString(),
                    record.get("name").asString()
            );
            user.setId(record.get("id").asLong());
            user.setDateFromEpoch(record.get("date").asLong());
            return user;
        }
    }
}
