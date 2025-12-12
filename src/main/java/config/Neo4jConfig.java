package config;

import org.neo4j.driver.*;

public class Neo4jConfig {
    private static final Driver driver = GraphDatabase.driver(
            "bolt://localhost:7687",
            AuthTokens.basic("neo4j", "123456789")
    );

    public static Driver getDriver() {
        return driver;
    }
}
