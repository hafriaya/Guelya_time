package repository;


import config.Neo4jConfig;
import model.User;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public class UserRepository {
    public final Driver driver;
    public UserRepository(){
        this.driver = Neo4jConfig.getDriver();
    }

    //create user database

    public User create(User u){
        String id = UUID.randomUUID().toString();
        u.setId(id);

        try(Session session =driver.session()){
            String query ="""
                    CREATE (u:User{
                        id: $id,
                        username:$username,
                        email:$email,
                        password:$password,
                        firstName:$firstName,
                        lastName:$lastName,
                        createdAt: $createdAt,
                        favoriteGenres: $favoriteGenres

                    })
                    RETURN u
                    """;
            session.run(query, Values.parameters(
                        "id",id,
                        "username", u.getUsername(),
                        "email", u.getEmail(),
                        "password", u.getPassword(),
                        "firstName", u.getFirstName(),
                        "lastName", u.getLastName(),
                        "createdAt", u.getCreatedAt().toString(),
                        "favoriteGenres", u.getFavoriteGenres()
                    ));
            return u;

        }
    }

    //map neo4j record to user object
    private User mapRecordToUser(Record record) {
        var node = record.get("u").asNode();

        User user = new User();
        
        // Handle id - could be string or integer
        user.setId(getAsString(node, "id"));
        user.setUsername(getAsString(node, "username"));
        user.setEmail(getAsString(node, "email"));
        user.setPassword(getAsString(node, "password"));

        // Handle optional fields
        if (!node.get("firstName").isNull()) {
            user.setFirstName(getAsString(node, "firstName"));
        }
        if (!node.get("lastName").isNull()) {
            user.setLastName(getAsString(node, "lastName"));
        }
        if (!node.get("createdAt").isNull()) {
            try {
                user.setCreatedAt(LocalDateTime.parse(getAsString(node, "createdAt")));
            } catch (Exception e) {
                // ignore parse errors
            }
        }
        if (!node.get("lastLogin").isNull()) {
            try {
                user.setLastLogin(LocalDateTime.parse(getAsString(node, "lastLogin")));
            } catch (Exception e) {
                // ignore parse errors
            }
        }
        if (!node.get("favoriteGenres").isNull()) {
            List<String> genres = new ArrayList<>();
            try {
                node.get("favoriteGenres").asList(v -> v.asObject().toString()).forEach(genres::add);
            } catch (Exception e) {
                // ignore if format is different
            }
            user.setFavoriteGenres(genres);
        }
        if (!node.get("onboardingCompleted").isNull()) {
            user.setOnboardingCompleted(node.get("onboardingCompleted").asBoolean());
        }

        return user;
    }
    
    // helper to get value as string regardless of type
    private String getAsString(org.neo4j.driver.types.Node node, String key) {
        Value value = node.get(key);
        if (value.isNull()) {
            return null;
        }
        // convert any type to string
        Object obj = value.asObject();
        return obj != null ? obj.toString() : null;
    }

    //find user by username

    public Optional<User> findByUsername(String u){
        try(Session session =driver.session()){
            String query="MATCH (u:User {username:$username}) RETURN u";
            Result result= session.run(query,Values.parameters("username",u));
            if (result.hasNext()){
                return Optional.of(mapRecordToUser(result.next()));

            }
            return Optional.empty();
        }
    }

    //find user by email
    public Optional<User> findByEmail(String e){
        try(Session session =driver.session()){
            String query="MATCH (u:User {email:$email}) RETURN u";
            Result result= session.run(query,Values.parameters("email",e));
            if (result.hasNext()){
                return Optional.of(mapRecordToUser(result.next()));

            }
            return Optional.empty();
        }
    }

    //find user by id
    public Optional<User> findById(String id){
        try(Session session =driver.session()){
            String query="MATCH (u:User {id:$id}) RETURN u";
            Result result= session.run(query,Values.parameters("id",id));
            if (result.hasNext()){
                return Optional.of(mapRecordToUser(result.next()));

            }
            return Optional.empty();
        }
    }

    //Find user by username or email
    public Optional<User> findByUsernameOrEmail(String ue){
        try(Session session =driver.session()){
            String query="""
                            MATCH (u:User)
                            WHERE u.username = $value OR u.email = $value
                            RETURN u
                        """;;
            Result result= session.run(query,Values.parameters("value",ue));
            if (result.hasNext()){
                return Optional.of(mapRecordToUser(result.next()));

            }
            return Optional.empty();
        }
    }

    // check username exist
    public boolean usernameExists(String u){
        try(Session session =driver.session()){
            String query= "MATCH (u:User {username: $username}) RETURN count(u)>0 AS exists";
            Result result = session.run(query, Values.parameters("username", u));
            return result.single().get("exists").asBoolean();
        }

    }

    // check email exist
    public boolean emailExists(String e){
        try(Session session =driver.session()){
            String query= "MATCH (u:User {email: $email}) RETURN count(u)>0 AS exists";
            Result result = session.run(query, Values.parameters("email", e));
            return result.single().get("exists").asBoolean();
        }

    }

    // update last login time
    public void updateLastLogin(String id){
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $id})
                SET u.lastLogin = $lastLogin
                """;
            session.run(query,Values.parameters(
                "id",id,
                "lastLogin", LocalDateTime.now().toString()

            ));
        }
    }

    // update user
    public void updateProfile(User u) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $id})
                SET u.firstName = $firstName,
                    u.lastName = $lastName,
                    u.favoriteGenres = $favoriteGenres
                """;
            session.run(query, Values.parameters(
                    "id", u.getId(),
                    "firstName", u.getFirstName(),
                    "lastName", u.getLastName(),
                    "favoriteGenres", u.getFavoriteGenres()
            ));
        }
    }

    //update password
    public void updatePassword(String id, String newpass) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $id})
                SET u.password = $password
                """;
            session.run(query, Values.parameters(
                    "id", id,
                    "password", newpass
            ));
        }
    }

    //delete user
    public void delete(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (u:User {id: $id}) DETACH DELETE u";
            session.run(query, Values.parameters("id", id));
        }
    }
    
    // all users
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = "MATCH (u:User) RETURN u";
            Result result = session.run(query);

            while (result.hasNext()) {
                users.add(mapRecordToUser(result.next()));
            }
        }
        return users;
    }

    // Set onboarding completed flag for user
    public void setOnboardingCompleted(String userId, boolean completed) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId})
                SET u.onboardingCompleted = $completed
                """;
            session.run(query, Values.parameters(
                    "userId", userId,
                    "completed", completed
            ));
        }
    }

    // Check if user has completed onboarding
    public boolean hasCompletedOnboarding(String userId) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId})
                RETURN u.onboardingCompleted AS completed
                """;
            Result result = session.run(query, Values.parameters("userId", userId));
            if (result.hasNext()) {
                Value value = result.next().get("completed");
                if (!value.isNull()) {
                    return value.asBoolean();
                }
            }
            return false;
        }
    }

}
