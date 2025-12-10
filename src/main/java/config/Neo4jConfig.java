package config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jConfig {
    private static final String URI = "bolt://localhost:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "123456789";

    private static Driver driver;

    // Get or create the driver instance (singleton pattern)
    public static Driver getDriver() {
        if (driver == null) {
            driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));
        }
        return driver;
    }

    // Close the driver connection
    public static void closeDriver() {
        if (driver != null) {
            driver.close();
            driver = null;
        }
    }

    // Verify connectivity
    public static boolean testConnection() {
        try {
            Driver testDriver = getDriver();
            testDriver.verifyConnectivity();
            return true;
        } catch (Exception e) {
            System.err.println("Failed to connect to Neo4j: " + e.getMessage());
            return false;
        }
    }
}