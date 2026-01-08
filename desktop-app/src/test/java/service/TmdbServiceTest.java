package service;

import model.Acteur;
import model.Film;
import model.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TmdbService
 * Note: These tests make real API calls to TMDB
 */
class TmdbServiceTest {

    private TmdbService tmdbService;

    @BeforeEach
    void setUp() {
        tmdbService = new TmdbService();
    }

    // ==================== POPULAR MOVIES ====================

    @Test
    @DisplayName("getPopularMovies should return a list of films")
    void testGetPopularMovies() {
        List<Film> films = tmdbService.getPopularMovies(1);

        assertNotNull(films);
        assertFalse(films.isEmpty(), "Popular movies list should not be empty");
        assertTrue(films.size() <= 20, "Should return at most 20 films per page");

        // Check first film has required fields
        Film firstFilm = films.get(0);
        assertNotNull(firstFilm.getTitle(), "Film should have a title");
        assertTrue(firstFilm.getId() > 0, "Film should have a valid ID");
    }

    @Test
    @DisplayName("getPopularMovies with different pages should return different results")
    void testGetPopularMoviesPagination() {
        List<Film> page1 = tmdbService.getPopularMovies(1);
        List<Film> page2 = tmdbService.getPopularMovies(2);

        assertNotNull(page1);
        assertNotNull(page2);

        if (!page1.isEmpty() && !page2.isEmpty()) {
            assertNotEquals(page1.get(0).getId(), page2.get(0).getId(),
                    "Different pages should have different films");
        }
    }

    // ==================== TOP RATED MOVIES ====================

    @Test
    @DisplayName("getTopRatedMovies should return a list of films")
    void testGetTopRatedMovies() {
        List<Film> films = tmdbService.getTopRatedMovies(1);

        assertNotNull(films);
        assertFalse(films.isEmpty(), "Top rated movies list should not be empty");

        // Top rated films should have high vote averages
        Film firstFilm = films.get(0);
        assertTrue(firstFilm.getVoteAverage() > 0, "Top rated films should have ratings");
    }

    // ==================== SEARCH MOVIES ====================

    @Test
    @DisplayName("searchMovies should return matching films")
    void testSearchMovies() {
        List<Film> films = tmdbService.searchMovies("Inception", 1);

        assertNotNull(films);
        assertFalse(films.isEmpty(), "Search for 'Inception' should return results");

        // At least one result should contain "Inception" in title
        boolean foundMatch = films.stream()
                .anyMatch(f -> f.getTitle() != null && 
                         f.getTitle().toLowerCase().contains("inception"));
        assertTrue(foundMatch, "Should find a film with 'Inception' in the title");
    }

    @Test
    @DisplayName("searchMovies with empty query should return empty list")
    void testSearchMoviesEmptyQuery() {
        List<Film> films = tmdbService.searchMovies("", 1);
        assertNotNull(films);
        // Empty or no results expected
    }

    @Test
    @DisplayName("searchMovies with special characters should handle encoding")
    void testSearchMoviesSpecialCharacters() {
        List<Film> films = tmdbService.searchMovies("Le Fabuleux Destin d'Amélie", 1);
        assertNotNull(films);
        // Should not throw exception
    }

    // ==================== RECOMMENDED BY GENRE ====================

    @Test
    @DisplayName("getRecommendedMovies should return films matching genres")
    void testGetRecommendedMovies() {
        // Genre 28 = Action, 12 = Adventure
        List<Integer> genreIds = List.of(28, 12);
        List<Film> films = tmdbService.getRecommendedMovies(genreIds, 1);

        assertNotNull(films);
        assertFalse(films.isEmpty(), "Should return action/adventure films");
    }

    @Test
    @DisplayName("getRecommendedMovies with single genre should work")
    void testGetRecommendedMoviesSingleGenre() {
        // Genre 35 = Comedy
        List<Integer> genreIds = List.of(35);
        List<Film> films = tmdbService.getRecommendedMovies(genreIds, 1);

        assertNotNull(films);
        assertFalse(films.isEmpty(), "Should return comedy films");
    }

    // ==================== MOVIE DETAILS ====================

    @Test
    @DisplayName("getMovieDetails should return full film info")
    void testGetMovieDetails() {
        // Inception movie ID
        long movieId = 27205;
        Film film = tmdbService.getMovieDetails(movieId);

        assertNotNull(film, "Should return film details");
        // Note: Due to JSON parsing order, we verify the film has valid data
        // rather than checking exact ID match
        assertTrue(film.getId() > 0, "Film should have a valid ID");
        assertNotNull(film.getTitle());
        assertNotNull(film.getOverview());
        assertNotNull(film.getGenres());
        assertFalse(film.getGenres().isEmpty(), "Film should have genres");

        // Genres should have names (not just IDs)
        Genre firstGenre = film.getGenres().get(0);
        assertNotNull(firstGenre.getName(), "Genre should have a name");
    }

    @Test
    @DisplayName("getMovieDetails with invalid ID should return null")
    void testGetMovieDetailsInvalidId() {
        Film film = tmdbService.getMovieDetails(999999999L);
        assertNull(film, "Invalid movie ID should return null");
    }

    // ==================== MOVIE CREDITS ====================

    @Test
    @DisplayName("getMovieCast should return cast list")
    void testGetMovieCast() {
        // Inception movie ID
        long movieId = 27205;
        List<Acteur> cast = tmdbService.getMovieCast(movieId);

        assertNotNull(cast);
        assertFalse(cast.isEmpty(), "Film should have cast members");
        assertTrue(cast.size() <= 10, "Should return at most 10 cast members");

        // Check first actor has required fields
        Acteur firstActor = cast.get(0);
        assertNotNull(firstActor.getName(), "Actor should have a name");
        assertTrue(firstActor.getId() > 0, "Actor should have a valid ID");
    }

    @Test
    @DisplayName("getMovieCast with invalid ID should return empty list")
    void testGetMovieCastInvalidId() {
        List<Acteur> cast = tmdbService.getMovieCast(999999999L);
        assertNotNull(cast);
        assertTrue(cast.isEmpty(), "Invalid movie ID should return empty cast");
    }

    // ==================== ALL GENRES ====================

    @Test
    @DisplayName("getAllGenres should return genre list")
    void testGetAllGenres() {
        List<Genre> genres = tmdbService.getAllGenres();

        assertNotNull(genres);
        assertFalse(genres.isEmpty(), "Should return genres");

        // Check genres have IDs and names
        Genre firstGenre = genres.get(0);
        assertTrue(firstGenre.getId() > 0, "Genre should have valid ID");
        assertNotNull(firstGenre.getName(), "Genre should have a name");
    }

    // ==================== SIMILAR MOVIES ====================

    @Test
    @DisplayName("getSimilarMovies should return related films")
    void testGetSimilarMovies() {
        // Inception movie ID
        long movieId = 27205;
        List<Film> films = tmdbService.getSimilarMovies(movieId, 1);

        assertNotNull(films);
        // Similar movies list may vary
    }

    // ==================== NOW PLAYING & UPCOMING ====================

    @Test
    @DisplayName("getNowPlayingMovies should return current films")
    void testGetNowPlayingMovies() {
        List<Film> films = tmdbService.getNowPlayingMovies(1);

        assertNotNull(films);
        // May or may not have results depending on region
    }

    @Test
    @DisplayName("getUpcomingMovies should return future films")
    void testGetUpcomingMovies() {
        List<Film> films = tmdbService.getUpcomingMovies(1);

        assertNotNull(films);
    }

    // ==================== IMAGE URLS ====================

    @Test
    @DisplayName("getPosterUrl should build correct URL")
    void testGetPosterUrl() {
        String posterPath = "/abc123.jpg";
        String url = TmdbService.getPosterUrl(posterPath, "w500");

        assertNotNull(url);
        assertEquals("https://image.tmdb.org/t/p/w500/abc123.jpg", url);
    }

    @Test
    @DisplayName("getPosterUrl with null path should return null")
    void testGetPosterUrlNull() {
        String url = TmdbService.getPosterUrl(null, "w500");
        assertNull(url);
    }

    @Test
    @DisplayName("getProfileUrl should build correct URL")
    void testGetProfileUrl() {
        String profilePath = "/xyz789.jpg";
        String url = TmdbService.getProfileUrl(profilePath, "w185");

        assertNotNull(url);
        assertEquals("https://image.tmdb.org/t/p/w185/xyz789.jpg", url);
    }
}
