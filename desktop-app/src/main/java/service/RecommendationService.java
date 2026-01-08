package service;

import config.Neo4jConfig;
import model.Film;
import model.Genre;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import repository.FilmRepository;
import repository.GenreRepository;

import java.util.*;
import java.util.stream.Collectors;

public class RecommendationService {
    private final Driver driver;
    private final FilmRepository filmRepository;
    private final GenreRepository genreRepository;
    private final TmdbService tmdbService;

    public RecommendationService() {
        this.driver = Neo4jConfig.getDriver();
        this.filmRepository = new FilmRepository();
        this.genreRepository = new GenreRepository();
        this.tmdbService = new TmdbService();
    }

    public RecommendationService(FilmRepository filmRepository, GenreRepository genreRepository, TmdbService tmdbService) {
        this.driver = Neo4jConfig.getDriver();
        this.filmRepository = filmRepository;
        this.genreRepository = genreRepository;
        this.tmdbService = tmdbService;
    }

    // ==================== GENRE-BASED RECOMMENDATIONS ====================

    /**
     * Get recommendations based on user's favorite genres
     */
    public List<Film> getRecommendationsByFavoriteGenres(String userId, int limit) {
        if (userId == null || userId.trim().isEmpty()) {
            return List.of();
        }

        // Get user's favorite genres
        List<Genre> favoriteGenres = genreRepository.getUserFavoriteGenres(userId);
        if (favoriteGenres.isEmpty()) {
            // If no favorite genres, return popular films
            return filmRepository.getPopularFilms(limit);
        }

        // Get genre IDs
        List<Integer> genreIds = favoriteGenres.stream()
                .map(Genre::getId)
                .collect(Collectors.toList());

        // Get films from TMDB based on these genres
        List<Film> recommendations = tmdbService.getRecommendedMovies(genreIds, 1);

        // Filter out films the user has already watched
        Set<Long> watchedFilmIds = getWatchedFilmIds(userId);
        recommendations = recommendations.stream()
                .filter(film -> !watchedFilmIds.contains(film.getId()))
                .limit(limit)
                .collect(Collectors.toList());

        return recommendations;
    }

    /**
     * Get recommendations based on a specific genre
     */
    public List<Film> getRecommendationsByGenre(int genreId, String userId, int limit) {
        // Get films from database first
        List<Film> films = filmRepository.findByGenre(genreId, limit * 2);

        // Filter out watched films if userId is provided
        if (userId != null && !userId.trim().isEmpty()) {
            Set<Long> watchedFilmIds = getWatchedFilmIds(userId);
            films = films.stream()
                    .filter(film -> !watchedFilmIds.contains(film.getId()))
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        // If not enough films in DB, fetch from TMDB
        if (films.size() < limit) {
            List<Film> tmdbFilms = tmdbService.getRecommendedMovies(List.of(genreId), 1);
            Set<Long> existingIds = films.stream().map(Film::getId).collect(Collectors.toSet());
            
            for (Film film : tmdbFilms) {
                if (!existingIds.contains(film.getId()) && films.size() < limit) {
                    films.add(film);
                    filmRepository.save(film);
                }
            }
        }

        return films;
    }

    // ==================== RATING-BASED RECOMMENDATIONS ====================

    /**
     * Get recommendations based on films the user rated highly (7+)
     */
    public List<Film> getRecommendationsByHighRatings(String userId, int limit) {
        if (userId == null || userId.trim().isEmpty()) {
            return List.of();
        }

        List<Film> recommendations = new ArrayList<>();

        try (Session session = driver.session()) {
            // Find films with similar genres to highly rated films
            String query = """
                MATCH (u:User {id: $userId})-[r:RATED]->(f:Film)-[:HAS_GENRE]->(g:Genre)
                WHERE r.score >= 7
                WITH g, count(*) as genreCount
                ORDER BY genreCount DESC
                LIMIT 5
                MATCH (recommended:Film)-[:HAS_GENRE]->(g)
                WHERE NOT EXISTS {
                    MATCH (u:User {id: $userId})-[:WATCHED]->(recommended)
                }
                AND NOT EXISTS {
                    MATCH (u:User {id: $userId})-[:RATED]->(recommended)
                }
                WITH DISTINCT recommended, count(g) as matchingGenres
                ORDER BY matchingGenres DESC, recommended.popularity DESC
                LIMIT $limit
                OPTIONAL MATCH (recommended)-[:HAS_GENRE]->(allGenres:Genre)
                RETURN recommended as f, collect(allGenres) as genres
                """;

            Result result = session.run(query, Values.parameters(
                    "userId", userId,
                    "limit", limit
            ));

            while (result.hasNext()) {
                recommendations.add(mapRecordToFilm(result.next()));
            }
        }

        return recommendations;
    }

    // ==================== COLLABORATIVE FILTERING ====================

    /**
     * Get recommendations based on what similar users liked
     * "Users who liked what you liked, also liked these films"
     */
    public List<Film> getCollaborativeRecommendations(String userId, int limit) {
        if (userId == null || userId.trim().isEmpty()) {
            return List.of();
        }

        List<Film> recommendations = new ArrayList<>();

        try (Session session = driver.session()) {
            String query = """
                // Find users who rated the same films similarly
                MATCH (u:User {id: $userId})-[r1:RATED]->(f:Film)<-[r2:RATED]-(other:User)
                WHERE abs(r1.score - r2.score) <= 2 AND u <> other
                WITH other, count(f) as commonFilms, avg(abs(r1.score - r2.score)) as avgDiff
                WHERE commonFilms >= 2
                ORDER BY commonFilms DESC, avgDiff ASC
                LIMIT 10
                
                // Get films that similar users liked but current user hasn't seen
                MATCH (other)-[r:RATED]->(recommended:Film)
                WHERE r.score >= 7
                AND NOT EXISTS {
                    MATCH (u:User {id: $userId})-[:WATCHED]->(recommended)
                }
                AND NOT EXISTS {
                    MATCH (u:User {id: $userId})-[:RATED]->(recommended)
                }
                WITH DISTINCT recommended, avg(r.score) as avgScore, count(*) as ratingCount
                ORDER BY avgScore DESC, ratingCount DESC
                LIMIT $limit
                OPTIONAL MATCH (recommended)-[:HAS_GENRE]->(g:Genre)
                RETURN recommended as f, collect(g) as genres
                """;

            Result result = session.run(query, Values.parameters(
                    "userId", userId,
                    "limit", limit
            ));

            while (result.hasNext()) {
                recommendations.add(mapRecordToFilm(result.next()));
            }
        }

        return recommendations;
    }

    // ==================== SIMILAR FILMS ====================

    /**
     * Get films similar to a specific film (same genres)
     */
    public List<Film> getSimilarFilms(long filmId, int limit) {
        List<Film> similarFilms = new ArrayList<>();

        try (Session session = driver.session()) {
            String query = """
                MATCH (f:Film {id: $filmId})-[:HAS_GENRE]->(g:Genre)<-[:HAS_GENRE]-(similar:Film)
                WHERE similar.id <> $filmId
                WITH similar, count(g) as commonGenres
                ORDER BY commonGenres DESC, similar.popularity DESC
                LIMIT $limit
                OPTIONAL MATCH (similar)-[:HAS_GENRE]->(allGenres:Genre)
                RETURN similar as f, collect(allGenres) as genres
                """;

            Result result = session.run(query, Values.parameters(
                    "filmId", filmId,
                    "limit", limit
            ));

            while (result.hasNext()) {
                similarFilms.add(mapRecordToFilm(result.next()));
            }
        }

        return similarFilms;
    }

    /**
     * Get films similar to a specific film, excluding user's watched films
     */
    public List<Film> getSimilarFilms(long filmId, String userId, int limit) {
        if (userId == null || userId.trim().isEmpty()) {
            return getSimilarFilms(filmId, limit);
        }

        List<Film> similarFilms = new ArrayList<>();

        try (Session session = driver.session()) {
            String query = """
                MATCH (f:Film {id: $filmId})-[:HAS_GENRE]->(g:Genre)<-[:HAS_GENRE]-(similar:Film)
                WHERE similar.id <> $filmId
                AND NOT EXISTS {
                    MATCH (u:User {id: $userId})-[:WATCHED]->(similar)
                }
                WITH similar, count(g) as commonGenres
                ORDER BY commonGenres DESC, similar.popularity DESC
                LIMIT $limit
                OPTIONAL MATCH (similar)-[:HAS_GENRE]->(allGenres:Genre)
                RETURN similar as f, collect(allGenres) as genres
                """;

            Result result = session.run(query, Values.parameters(
                    "filmId", filmId,
                    "userId", userId,
                    "limit", limit
            ));

            while (result.hasNext()) {
                similarFilms.add(mapRecordToFilm(result.next()));
            }
        }

        return similarFilms;
    }

    // ==================== TRENDING & DISCOVERY ====================

    /**
     * Get trending films (highly rated by many users recently)
     */
    public List<Film> getTrendingFilms(int limit) {
        List<Film> trending = new ArrayList<>();

        try (Session session = driver.session()) {
            String query = """
                MATCH (f:Film)<-[r:RATED]-(:User)
                WITH f, avg(r.score) as avgRating, count(r) as ratingCount
                WHERE ratingCount >= 3
                ORDER BY avgRating DESC, ratingCount DESC
                LIMIT $limit
                OPTIONAL MATCH (f)-[:HAS_GENRE]->(g:Genre)
                RETURN f, collect(g) as genres
                """;

            Result result = session.run(query, Values.parameters("limit", limit));

            while (result.hasNext()) {
                trending.add(mapRecordToFilm(result.next()));
            }
        }

        // If not enough trending films in DB, get popular from TMDB
        if (trending.size() < limit) {
            List<Film> popular = tmdbService.getPopularMovies(1);
            Set<Long> existingIds = trending.stream().map(Film::getId).collect(Collectors.toSet());
            
            for (Film film : popular) {
                if (!existingIds.contains(film.getId()) && trending.size() < limit) {
                    trending.add(film);
                }
            }
        }

        return trending;
    }

    /**
     * Get personalized recommendations combining multiple strategies
     */
    public List<Film> getPersonalizedRecommendations(String userId, int limit) {
        if (userId == null || userId.trim().isEmpty()) {
            return getTrendingFilms(limit);
        }

        Set<Long> addedFilmIds = new HashSet<>();
        List<Film> recommendations = new ArrayList<>();

        // 1. Get recommendations by favorite genres (40% of results)
        int genreLimit = (int) Math.ceil(limit * 0.4);
        List<Film> genreRecs = getRecommendationsByFavoriteGenres(userId, genreLimit);
        for (Film film : genreRecs) {
            if (addedFilmIds.add(film.getId())) {
                recommendations.add(film);
            }
        }

        // 2. Get collaborative recommendations (30% of results)
        int collabLimit = (int) Math.ceil(limit * 0.3);
        List<Film> collabRecs = getCollaborativeRecommendations(userId, collabLimit);
        for (Film film : collabRecs) {
            if (addedFilmIds.add(film.getId())) {
                recommendations.add(film);
            }
        }

        // 3. Get rating-based recommendations (30% of results)
        int ratingLimit = (int) Math.ceil(limit * 0.3);
        List<Film> ratingRecs = getRecommendationsByHighRatings(userId, ratingLimit);
        for (Film film : ratingRecs) {
            if (addedFilmIds.add(film.getId())) {
                recommendations.add(film);
            }
        }

        // Fill remaining slots with trending films
        if (recommendations.size() < limit) {
            List<Film> trending = getTrendingFilms(limit - recommendations.size());
            for (Film film : trending) {
                if (addedFilmIds.add(film.getId())) {
                    recommendations.add(film);
                }
                if (recommendations.size() >= limit) break;
            }
        }

        return recommendations.stream().limit(limit).collect(Collectors.toList());
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Get set of film IDs that user has watched
     */
    private Set<Long> getWatchedFilmIds(String userId) {
        Set<Long> watchedIds = new HashSet<>();
        try (Session session = driver.session()) {
            String query = "MATCH (u:User {id: $userId})-[:WATCHED]->(f:Film) RETURN f.id as id";
            Result result = session.run(query, Values.parameters("userId", userId));
            while (result.hasNext()) {
                watchedIds.add(result.next().get("id").asLong());
            }
        }
        return watchedIds;
    }

    /**
     * Map Neo4j record to Film object
     */
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
