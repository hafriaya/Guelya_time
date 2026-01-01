package config;

import org.neo4j.driver.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Neo4jConfig {
    private static final Driver driver;
    private static final Properties properties = new Properties();

    static {
        loadProperties();
        String url = properties.getProperty("neo4j.url", "bolt://localhost:7687");
        String username = properties.getProperty("neo4j.username", "neo4j");
        String password = properties.getProperty("neo4j.password", "");
        driver = GraphDatabase.driver(url, AuthTokens.basic(username, password));
    }

    private static void loadProperties() {
        try (InputStream input = Neo4jConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("application.properties not found, using defaults");
            }
        } catch (IOException e) {
            System.err.println("Error loading application.properties: " + e.getMessage());
        }
    }

    public static Driver getDriver() {
        return driver;
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static void close() {
        if (driver != null) {
            driver.close();
        }
    }
}
