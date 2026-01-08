package repository;

import config.Neo4jConfig;
import model.Genre;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenreRepository {
    private final Driver driver;

    public GenreRepository() {
        this.driver = Neo4jConfig.getDriver();
    }

    // Save or update a genre in Neo4j
    public Genre save(Genre genre) {
        try (Session session = driver.session()) {
            String query = """
                MERGE (g:Genre {id: $id})
                SET g.name = $name
                RETURN g
                """;
            session.run(query, Values.parameters(
                    "id", genre.getId(),
                    "name", genre.getName()
            ));
            return genre;
        }
    }

    // Save multiple genres at once
    public void saveAll(List<Genre> genres) {
        try (Session session = driver.session()) {
            for (Genre genre : genres) {
                String query = """
                    MERGE (g:Genre {id: $id})
                    SET g.name = $name
                    """;
                session.run(query, Values.parameters(
                        "id", genre.getId(),
                        "name", genre.getName()
                ));
            }
        }
    }

    // Find genre by ID
    public Optional<Genre> findById(int id) {
        try (Session session = driver.session()) {
            String query = "MATCH (g:Genre {id: $id}) RETURN g";
            Result result = session.run(query, Values.parameters("id", id));
            if (result.hasNext()) {
                return Optional.of(mapRecordToGenre(result.next()));
            }
            return Optional.empty();
        }
    }

    // Find genre by name
    public Optional<Genre> findByName(String name) {
        try (Session session = driver.session()) {
            String query = "MATCH (g:Genre) WHERE toLower(g.name) = toLower($name) RETURN g";
            Result result = session.run(query, Values.parameters("name", name));
            if (result.hasNext()) {
                return Optional.of(mapRecordToGenre(result.next()));
            }
            return Optional.empty();
        }
    }

    // Get all genres
    public List<Genre> findAll() {
        List<Genre> genres = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = "MATCH (g:Genre) RETURN g ORDER BY g.name";
            Result result = session.run(query);
            while (result.hasNext()) {
                genres.add(mapRecordToGenre(result.next()));
            }
        }
        return genres;
    }

    // Set user's favorite genres (replaces existing preferences)
    public void setUserFavoriteGenres(String userId, List<Integer> genreIds) {
        try (Session session = driver.session()) {
            // First, remove existing preferences
            String removeQuery = """
                MATCH (u:User {id: $userId})-[r:PREFERS]->(g:Genre)
                DELETE r
                """;
            session.run(removeQuery, Values.parameters("userId", userId));

            // Then, add new preferences
            for (Integer genreId : genreIds) {
                String addQuery = """
                    MATCH (u:User {id: $userId}), (g:Genre {id: $genreId})
                    MERGE (u)-[:PREFERS]->(g)
                    """;
                session.run(addQuery, Values.parameters(
                        "userId", userId,
                        "genreId", genreId
                ));
            }
        }
    }

    // Add a single genre to user's favorites
    public void addUserFavoriteGenre(String userId, int genreId) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId}), (g:Genre {id: $genreId})
                MERGE (u)-[:PREFERS]->(g)
                """;
            session.run(query, Values.parameters(
                    "userId", userId,
                    "genreId", genreId
            ));
        }
    }

    // Remove a genre from user's favorites
    public void removeUserFavoriteGenre(String userId, int genreId) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId})-[r:PREFERS]->(g:Genre {id: $genreId})
                DELETE r
                """;
            session.run(query, Values.parameters(
                    "userId", userId,
                    "genreId", genreId
            ));
        }
    }

    // Get user's favorite genres
    public List<Genre> getUserFavoriteGenres(String userId) {
        List<Genre> genres = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId})-[:PREFERS]->(g:Genre)
                RETURN g
                ORDER BY g.name
                """;
            Result result = session.run(query, Values.parameters("userId", userId));
            while (result.hasNext()) {
                genres.add(mapRecordToGenre(result.next()));
            }
        }
        return genres;
    }

    // Check if genre is in user's favorites
    public boolean isUserFavoriteGenre(String userId, int genreId) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId})-[:PREFERS]->(g:Genre {id: $genreId})
                RETURN count(*) > 0 as exists
                """;
            Result result = session.run(query, Values.parameters(
                    "userId", userId,
                    "genreId", genreId
            ));
            return result.single().get("exists").asBoolean();
        }
    }

    // Get popular genres (based on how many users prefer them)
    public List<Genre> getPopularGenres(int limit) {
        List<Genre> genres = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = """
                MATCH (g:Genre)
                OPTIONAL MATCH (u:User)-[:PREFERS]->(g)
                RETURN g, count(u) as preferCount
                ORDER BY preferCount DESC
                LIMIT $limit
                """;
            Result result = session.run(query, Values.parameters("limit", limit));
            while (result.hasNext()) {
                genres.add(mapRecordToGenre(result.next()));
            }
        }
        return genres;
    }

    // Get genres for a specific film
    public List<Genre> getGenresForFilm(long filmId) {
        List<Genre> genres = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = """
                MATCH (f:Film {id: $filmId})-[:HAS_GENRE]->(g:Genre)
                RETURN g
                ORDER BY g.name
                """;
            Result result = session.run(query, Values.parameters("filmId", filmId));
            while (result.hasNext()) {
                genres.add(mapRecordToGenre(result.next()));
            }
        }
        return genres;
    }

    // Delete a genre
    public void delete(int genreId) {
        try (Session session = driver.session()) {
            String query = "MATCH (g:Genre {id: $id}) DETACH DELETE g";
            session.run(query, Values.parameters("id", genreId));
        }
    }

    // Count total genres
    public long count() {
        try (Session session = driver.session()) {
            String query = "MATCH (g:Genre) RETURN count(g) as count";
            Result result = session.run(query);
            return result.single().get("count").asLong();
        }
    }

    // Map Neo4j record to Genre object
    private Genre mapRecordToGenre(Record record) {
        var node = record.get("g").asNode();
        Genre genre = new Genre();
        genre.setId(node.get("id").asInt());
        genre.setName(node.get("name").asString(null));
        return genre;
    }
}
