package service;

import model.Film;
import model.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.GenreRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Genre sync functionality between TMDB and Neo4j database.
 * Note: These tests require a running Neo4j database and TMDB API access.
 */
class GenreSyncTest {

    private TmdbService tmdbService;
    private GenreRepository genreRepository;
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        // Clear the static cache before each test
        TmdbService.clearGenreCache();
        tmdbService = new TmdbService();
        genreRepository = new GenreRepository();
        filmService = new FilmService();
    }

    // ==================== TMDB GENRE CACHE ====================

    @Test
    @DisplayName("getGenreMap should return a map of genre IDs to names")
    void testGetGenreMap() {
        Map<Integer, String> genreMap = tmdbService.getGenreMap();

        assertNotNull(genreMap, "Genre map should not be null");
        assertFalse(genreMap.isEmpty(), "Genre map should not be empty");

        // TMDB has standard genres - check they exist (names may be in French)
        assertTrue(genreMap.containsKey(28), "Should contain Action genre (ID: 28)");
        assertNotNull(genreMap.get(28), "Genre 28 should have a name");

        assertTrue(genreMap.containsKey(35), "Should contain Comedy genre (ID: 35)");
        assertNotNull(genreMap.get(35), "Genre 35 should have a name");

        System.out.println("Loaded " + genreMap.size() + " genres from TMDB:");
        genreMap.forEach((id, name) -> System.out.println("  " + id + " -> " + name));
    }

    @Test
    @DisplayName("getGenreMap should cache results")
    void testGenreMapCaching() {
        // First call - loads from API
        Map<Integer, String> firstCall = tmdbService.getGenreMap();
        
        // Second call - should return cached version (same map reference)
        Map<Integer, String> secondCall = tmdbService.getGenreMap();

        assertEquals(firstCall.size(), secondCall.size(), "Cache should return same data");
        // Both should have same content
        for (Integer key : firstCall.keySet()) {
            assertEquals(firstCall.get(key), secondCall.get(key), "Cached values should match");
        }
    }

    @Test
    @DisplayName("getAllGenres should return list of Genre objects")
    void testGetAllGenresFromTmdb() {
        List<Genre> genres = tmdbService.getAllGenres();

        assertNotNull(genres, "Genres list should not be null");
        assertFalse(genres.isEmpty(), "Genres list should not be empty");

        // Check that genres have both ID and name
        for (Genre genre : genres) {
            assertTrue(genre.getId() > 0, "Genre should have a valid ID");
            assertNotNull(genre.getName(), "Genre should have a name");
            assertFalse(genre.getName().isEmpty(), "Genre name should not be empty");
        }

        System.out.println("Loaded " + genres.size() + " genres from TMDB");
    }

    // ==================== DATABASE SYNC ====================

    @Test
    @DisplayName("syncGenresFromTmdb should save all genres to database")
    void testSyncGenresToDatabase() {
        // Sync genres from TMDB to database
        filmService.syncGenresFromTmdb();

        // Verify genres are in the database
        List<Genre> dbGenres = genreRepository.findAll();

        assertNotNull(dbGenres, "Database genres should not be null");
        assertFalse(dbGenres.isEmpty(), "Database should have genres after sync");

        System.out.println("Synced " + dbGenres.size() + " genres to database:");
        dbGenres.forEach(g -> System.out.println("  " + g.getId() + " -> " + g.getName()));

        // Verify some standard genres are present with names
        Optional<Genre> actionGenre = genreRepository.findById(28);
        assertTrue(actionGenre.isPresent(), "Action genre should be in database");
        assertNotNull(actionGenre.get().getName(), "Action genre should have a name");
        assertFalse(actionGenre.get().getName().isEmpty(), "Action genre name should not be empty");
    }

    @Test
    @DisplayName("Genre names should not be empty after sync")
    void testGenreNamesNotEmpty() {
        // First sync
        filmService.syncGenresFromTmdb();

        // Get all genres from database
        List<Genre> dbGenres = genreRepository.findAll();

        for (Genre genre : dbGenres) {
            assertNotNull(genre.getName(), "Genre " + genre.getId() + " should have a name");
            assertFalse(genre.getName().isEmpty(), "Genre " + genre.getId() + " name should not be empty");
        }
    }

    // ==================== FILM PARSING WITH GENRE NAMES ====================

    @Test
    @DisplayName("Popular movies should have genre names, not null")
    void testPopularMoviesHaveGenreNames() {
        List<Film> films = tmdbService.getPopularMovies(1);

        assertFalse(films.isEmpty(), "Should have popular movies");

        for (Film film : films) {
            if (!film.getGenres().isEmpty()) {
                for (Genre genre : film.getGenres()) {
                    assertNotNull(genre.getName(), 
                        "Genre " + genre.getId() + " for film '" + film.getTitle() + "' should have a name");
                    assertNotEquals("", genre.getName(),
                        "Genre " + genre.getId() + " for film '" + film.getTitle() + "' should not be empty");
                }
            }
        }

        // Print first few films with their genres
        System.out.println("\nFirst 5 popular movies with genres:");
        films.stream().limit(5).forEach(film -> {
            System.out.println("  " + film.getTitle() + ": " + 
                film.getGenres().stream()
                    .map(g -> g.getName() + "(" + g.getId() + ")")
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("No genres"));
        });
    }

    @Test
    @DisplayName("Search results should have genre names")
    void testSearchMoviesHaveGenreNames() {
        List<Film> films = tmdbService.searchMovies("Matrix", 1);

        assertFalse(films.isEmpty(), "Should find Matrix movies");

        Film firstFilm = films.get(0);
        System.out.println("\nSearching 'Matrix' - First result: " + firstFilm.getTitle());

        for (Genre genre : firstFilm.getGenres()) {
            System.out.println("  Genre: " + genre.getName() + " (ID: " + genre.getId() + ")");
            assertNotNull(genre.getName(), "Genre should have a name");
        }
    }

    // ==================== GENRE REPOSITORY ====================

    @Test
    @DisplayName("GenreRepository should save and retrieve genre correctly")
    void testSaveAndRetrieveGenre() {
        Genre testGenre = new Genre(9999, "Test Genre");
        
        // Save genre
        genreRepository.save(testGenre);

        // Retrieve and verify
        Optional<Genre> retrieved = genreRepository.findById(9999);
        
        assertTrue(retrieved.isPresent(), "Saved genre should be retrievable");
        assertEquals("Test Genre", retrieved.get().getName(), "Genre name should match");

        // Clean up - you may want to add a delete method
        System.out.println("Successfully saved and retrieved test genre");
    }

    @Test
    @DisplayName("GenreRepository saveAll should save multiple genres")
    void testSaveAllGenres() {
        List<Genre> testGenres = List.of(
            new Genre(9901, "Test Genre 1"),
            new Genre(9902, "Test Genre 2"),
            new Genre(9903, "Test Genre 3")
        );

        // Save all
        genreRepository.saveAll(testGenres);

        // Verify each was saved
        for (Genre genre : testGenres) {
            Optional<Genre> retrieved = genreRepository.findById(genre.getId());
            assertTrue(retrieved.isPresent(), "Genre " + genre.getId() + " should be saved");
            assertEquals(genre.getName(), retrieved.get().getName(), "Genre name should match");
        }

        System.out.println("Successfully saved " + testGenres.size() + " test genres");
    }
}
