package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Film model
 */
class FilmTest {

    @Test
    @DisplayName("Film default constructor should initialize genres list")
    void testDefaultConstructor() {
        Film film = new Film();

        assertNotNull(film.getGenres(), "Genres should not be null");
        assertTrue(film.getGenres().isEmpty(), "Genres should be empty");
    }

    @Test
    @DisplayName("Film should store and retrieve all properties")
    void testGettersAndSetters() {
        Film film = new Film();

        film.setId(12345L);
        film.setTitle("Test Movie");
        film.setOverview("This is a test movie overview.");
        film.setPosterPath("/poster.jpg");
        film.setReleaseDate("2025-01-15");
        film.setVoteAverage(8.5);
        film.setVoteCount(1000);
        film.setPopularity(150.5);

        List<Genre> genres = new ArrayList<>();
        genres.add(new Genre(28, "Action"));
        genres.add(new Genre(12, "Adventure"));
        film.setGenres(genres);

        assertEquals(12345L, film.getId());
        assertEquals("Test Movie", film.getTitle());
        assertEquals("This is a test movie overview.", film.getOverview());
        assertEquals("/poster.jpg", film.getPosterPath());
        assertEquals("2025-01-15", film.getReleaseDate());
        assertEquals(8.5, film.getVoteAverage());
        assertEquals(1000, film.getVoteCount());
        assertEquals(150.5, film.getPopularity());
        assertEquals(2, film.getGenres().size());
    }

    @Test
    @DisplayName("Film voteAverage should handle edge cases")
    void testVoteAverageEdgeCases() {
        Film film = new Film();

        film.setVoteAverage(0.0);
        assertEquals(0.0, film.getVoteAverage());

        film.setVoteAverage(10.0);
        assertEquals(10.0, film.getVoteAverage());
    }

    @Test
    @DisplayName("Film should handle null values gracefully")
    void testNullValues() {
        Film film = new Film();

        film.setTitle(null);
        film.setOverview(null);
        film.setPosterPath(null);
        film.setReleaseDate(null);

        assertNull(film.getTitle());
        assertNull(film.getOverview());
        assertNull(film.getPosterPath());
        assertNull(film.getReleaseDate());
    }

    @Test
    @DisplayName("Film genres list should be modifiable")
    void testGenresModification() {
        Film film = new Film();
        
        Genre action = new Genre(28, "Action");
        film.getGenres().add(action);

        assertEquals(1, film.getGenres().size());
        assertEquals("Action", film.getGenres().get(0).getName());
    }
}
