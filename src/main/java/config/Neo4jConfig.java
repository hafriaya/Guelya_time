package config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import java.io.InputStream;
import java.util.Properties;

public class Neo4jConfig {

    private static Driver driver;

    public static void initialize() {
        try {
            Properties properties = new Properties();
            InputStream input = Neo4jConfig.class.getClassLoader()
                    .getResourceAsStream("application.properties");

            properties.load(input);

            String uri = properties.getProperty("neo4j.uri");
            String user = properties.getProperty("neo4j.username");
            String pass = properties.getProperty("neo4j.password");

            driver = GraphDatabase.driver(uri, AuthTokens.basic(user, pass));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Driver getDriver() {
        return driver;
    }

    public static void close() {
        driver.close();
    }
}