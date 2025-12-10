package service;



import config.Neo4jConfig;
import model.User;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

public class UserService {

    public boolean register(User user) {
        try (Session session = Neo4jConfig.getDriver().session()) {

            return session.writeTransaction(tx -> {

                var result = tx.run(
                        "CREATE (u:User {email: $email, password: $password, name: $name})",
                        org.neo4j.driver.Values.parameters(
                                "email", user.getEmail(),
                                "password", user.getPassword(),
                                "name", user.getName()
                        )
                );

                return true;
            });

        } catch (Exception e) {
            return false;
        }
    }

    public boolean login(String email, String password) {
        try (Session session = Neo4jConfig.getDriver().session()) {

            var result = session.run(
                    "MATCH (u:User {email: $email, password: $password}) RETURN u",
                    org.neo4j.driver.Values.parameters("email", email, "password", password)
            );

            return result.hasNext();
        }
    }
}
