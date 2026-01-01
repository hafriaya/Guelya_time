package repository;

import config.Neo4jConfig;
import model.Film;
import model.Genre;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FilmRepository {
    private final Driver driver;
    public FilmRepository() {
        this.driver = Neo4jConfig.getDriver();
    }
    //save and update film in neo4j
    public  Film Save(Film film) {
        try (Session session = driver.session()) {
            String query = """
                MERGE (f:Film {id: $id})
                SET f.title = $title,
                    f.overview = $overview,
                    f.posterPath = $posterPath,
                    f.releaseDate = $releaseDate,
                    f.voteAverage = $voteAverage,
                    f.voteCount = $voteCount,
                    f.popularity = $popularity
                RETURN f
                """;
            session.run(query, Values.parameters(
                    "id", film.getId(),
                    "title", film.getTitle(),
                    "overview", film.getOverview(),
                    "posterPath", film.getPosterPath(),
                    "releaseDate", film.getReleaseDate(),
                    "voteAverage", film.getVoteAverage(),
                    "voteCount", film.getVoteCount(),
                    "popularity", film.getPopularity()
            ));
            // Save genres and create relationships
            for (Genre genre : film.getGenres()) {
                String genreQuery = """
                    MERGE (g:Genre {id: $genreId})
                    ON CREATE SET g.name = $genreName
                    WITH g
                    MATCH (f:Film {id: $filmId})
                    MERGE (f)-[:HAS_GENRE]->(g)
                    """;
                session.run(genreQuery, Values.parameters(
                        "genreId", genre.getId(),
                        "genreName", genre.getName() != null ? genre.getName() : "",
                        "filmId", film.getId()
                ));
            }

            return film;
        }
    }

    //find film by id
    public Optional<Film> findById(long id) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (f:Film {id: $id})
                OPTIONAL MATCH (f)-[:HAS_GENRE]->(g:Genre)
                RETURN f, collect(g) as genres
                """;
            Result result = session.run(query, Values.parameters("id", id));
            if (result.hasNext()) {
                return Optional.of(mapRecordToFilm(result.next()));
            }
            return Optional.empty();
        }
    }

    // Search films by title
    public List<Film> findByTitle(String title) {
        List<Film> films = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = """
                MATCH (f:Film)
                WHERE toLower(f.title) CONTAINS toLower($title)
                OPTIONAL MATCH (f)-[:HAS_GENRE]->(g:Genre)
                RETURN f, collect(g) as genres
                ORDER BY f.popularity DESC
                LIMIT 20
                """;
            Result result = session.run(query, Values.parameters("title", title));
            while (result.hasNext()) {
                films.add(mapRecordToFilm(result.next()));
            }
        }
        return films;
    }
    //add film to watchlist
    public void addToUserWatchlist(String userId, long filmId) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId}), (f:Film {id: $filmId})
                MERGE (u)-[r:WATCHLIST]->(f)
                ON CREATE SET r.addedAt = $addedAt
                """;
            session.run(query, Values.parameters(
                    "userId", userId,
                    "filmId", filmId,
                    "addedAt", LocalDateTime.now().toString()
            ));
        }
    }

    //remove film from watchlist
    public void removeFromUserWatchlist(String userId, long filmId) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId})-[r:WATCHLIST]->(f:Film {id: $filmId})
                DELETE r
                """;
            session.run(query, Values.parameters("userId", userId, "filmId", filmId));
        }
    }

    // Get user's watchlist
    public List<Film> getUserWatchlist(String userId) {
        List<Film> films = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId})-[:WATCHLIST]->(f:Film)
                OPTIONAL MATCH (f)-[:HAS_GENRE]->(g:Genre)
                RETURN f, collect(g) as genres
                ORDER BY f.title
                """;
            Result result = session.run(query, Values.parameters("userId", userId));
            while (result.hasNext()) {
                films.add(mapRecordToFilm(result.next()));
            }
        }
        return films;
    }

    // Add film to user's favorites
    public void addToUserFavorites(String userId, long filmId) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId}), (f:Film {id: $filmId})
                MERGE (u)-[r:FAVORITED]->(f)
                ON CREATE SET r.addedAt = $addedAt
                """;
            session.run(query, Values.parameters(
                    "userId", userId,
                    "filmId", filmId,
                    "addedAt", LocalDateTime.now().toString()
            ));
        }
    }

    // Remove film from user's favorites
    public void removeFromUserFavorites(String userId, long filmId) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId})-[r:FAVORITED]->(f:Film {id: $filmId})
                DELETE r
                """;
            session.run(query, Values.parameters("userId", userId, "filmId", filmId));
        }
    }

    // Rate a film
    public void rateFilm(String userId, long filmId, int score) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId}), (f:Film {id: $filmId})
                MERGE (u)-[r:RATED]->(f)
                SET r.score = $score, r.ratedAt = $ratedAt
                """;
            session.run(query, Values.parameters(
                    "userId", userId,
                    "filmId", filmId,
                    "score", score,
                    "ratedAt", LocalDateTime.now().toString()
            ));
        }
    }

    // Remove user's rating
    public void removeRating(String userId, long filmId) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId})-[r:RATED]->(f:Film {id: $filmId})
                DELETE r
                """;
            session.run(query, Values.parameters("userId", userId, "filmId", filmId));
        }
    }

    // Get all films rated by user
    public List<Film> getUserRatedFilms(String userId) {
        List<Film> films = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId})-[r:RATED]->(f:Film)
                OPTIONAL MATCH (f)-[:HAS_GENRE]->(g:Genre)
                RETURN f, collect(g) as genres, r.score as userScore
                ORDER BY r.ratedAt DESC
                """;
            Result result = session.run(query, Values.parameters("userId", userId));
            while (result.hasNext()) {
                films.add(mapRecordToFilm(result.next()));
            }
        }
        return films;
    }

    // Get user's rating for a film
    public Optional<Integer> getUserRating(String userId, long filmId) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId})-[r:RATED]->(f:Film {id: $filmId})
                RETURN r.score as score
                """;
            Result result = session.run(query, Values.parameters("userId", userId, "filmId", filmId));
            if (result.hasNext()) {
                return Optional.of(result.next().get("score").asInt());
            }
            return Optional.empty();
        }
    }

    // Mark film as watched
    public void markAsWatched(String userId, long filmId) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId}), (f:Film {id: $filmId})
                MERGE (u)-[r:WATCHED]->(f)
                ON CREATE SET r.watchedAt = $watchedAt
                """;
            session.run(query, Values.parameters(
                    "userId", userId,
                    "filmId", filmId,
                    "watchedAt", LocalDateTime.now().toString()
            ));
        }
    }

    // Get user's watched films
    public List<Film> getUserWatchedFilms(String userId) {
        List<Film> films = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId})-[:WATCHED]->(f:Film)
                OPTIONAL MATCH (f)-[:HAS_GENRE]->(g:Genre)
                RETURN f, collect(g) as genres
                ORDER BY f.title
                """;
            Result result = session.run(query, Values.parameters("userId", userId));
            while (result.hasNext()) {
                films.add(mapRecordToFilm(result.next()));
            }
        }
        return films;
    }

    // Check if film is in user's watchlist
    public boolean isInWatchlist(String userId, long filmId) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId})-[:WATCHLIST]->(f:Film {id: $filmId})
                RETURN count(*) > 0 as exists
                """;
            Result result = session.run(query, Values.parameters("userId", userId, "filmId", filmId));
            return result.single().get("exists").asBoolean();
        }
    }

    // Check if film is in user's favorites
    public boolean isInFavorites(String userId, long filmId) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {id: $userId})-[:FAVORITED]->(f:Film {id: $filmId})
                RETURN count(*) > 0 as exists
                """;
            Result result = session.run(query, Values.parameters("userId", userId, "filmId", filmId));
            return result.single().get("exists").asBoolean();
        }
    }

    // Get films by genre
    public List<Film> findByGenre(int genreId, int limit) {
        List<Film> films = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = """
                MATCH (f:Film)-[:HAS_GENRE]->(g:Genre {id: $genreId})
                OPTIONAL MATCH (f)-[:HAS_GENRE]->(allGenres:Genre)
                RETURN f, collect(DISTINCT allGenres) as genres
                ORDER BY f.popularity DESC
                LIMIT $limit
                """;
            Result result = session.run(query, Values.parameters("genreId", genreId, "limit", limit));
            while (result.hasNext()) {
                films.add(mapRecordToFilm(result.next()));
            }
        }
        return films;
    }

    // Get popular films from database
    public List<Film> getPopularFilms(int limit) {
        List<Film> films = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = """
                MATCH (f:Film)
                OPTIONAL MATCH (f)-[:HAS_GENRE]->(g:Genre)
                RETURN f, collect(g) as genres
                ORDER BY f.popularity DESC
                LIMIT $limit
                """;
            Result result = session.run(query, Values.parameters("limit", limit));
            while (result.hasNext()) {
                films.add(mapRecordToFilm(result.next()));
            }
        }
        return films;
    }

    // Delete a film
    public void delete(long filmId) {
        try (Session session = driver.session()) {
            String query = "MATCH (f:Film {id: $id}) DETACH DELETE f";
            session.run(query, Values.parameters("id", filmId));
        }
    }





    private Film mapRecordToFilm(Record record) {
        var node = record.get("f").asNode();

        Film film = new Film();
        film.setId(node.get("id").asLong());
        film.setTitle(node.get("title").asString(null));
        film.setOverview(node.get("overview").asString(null));
        film.setPosterPath(node.get("posterPath").asString(null));
        film.setReleaseDate(node.get("releaseDate").asString(null));
        film.setVoteAverage(node.get("voteAverage").asDouble(0.0));
        film.setVoteCount(node.get("voteCount").asInt(0));
        film.setPopularity(node.get("popularity").asDouble(0.0));

        // Map genres
        List<Genre> genres = new ArrayList<>();
        var genreNodes = record.get("genres").asList();
        for (Object genreObj : genreNodes) {
            if (genreObj instanceof org.neo4j.driver.types.Node genreNode) {
                Genre genre = new Genre();
                genre.setId(genreNode.get("id").asInt());
                genre.setName(genreNode.get("name").asString(null));
                genres.add(genre);
            }
        }
        film.setGenres(genres);

        return film;
    }
}
