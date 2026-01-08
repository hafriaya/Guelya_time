package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Genre model
 */
class GenreTest {

    @Test
    @DisplayName("Genre default constructor should work")
    void testDefaultConstructor() {
        Genre genre = new Genre();

        assertEquals(0, genre.getId());
        assertNull(genre.getName());
    }

    @Test
    @DisplayName("Genre parameterized constructor should set values")
    void testParameterizedConstructor() {
        Genre genre = new Genre(28, "Action");

        assertEquals(28, genre.getId());
        assertEquals("Action", genre.getName());
    }

    @Test
    @DisplayName("Genre getters and setters should work")
    void testGettersAndSetters() {
        Genre genre = new Genre();

        genre.setId(35);
        genre.setName("Comedy");

        assertEquals(35, genre.getId());
        assertEquals("Comedy", genre.getName());
    }

    @Test
    @DisplayName("Genre toString should return name")
    void testToString() {
        Genre genre = new Genre(28, "Action");

        assertEquals("Action", genre.toString());
    }

    @Test
    @DisplayName("Genre toString with null name should return null")
    void testToStringNullName() {
        Genre genre = new Genre(28, null);

        assertNull(genre.toString());
    }

    @Test
    @DisplayName("Genre should handle common TMDB genre IDs")
    void testCommonGenreIds() {
        // Test common TMDB genre mappings
        Genre action = new Genre(28, "Action");
        Genre adventure = new Genre(12, "Adventure");
        Genre comedy = new Genre(35, "Comedy");
        Genre drama = new Genre(18, "Drama");
        Genre horror = new Genre(27, "Horror");
        Genre romance = new Genre(10749, "Romance");
        Genre sciFi = new Genre(878, "Science Fiction");
        Genre thriller = new Genre(53, "Thriller");

        assertEquals(28, action.getId());
        assertEquals(12, adventure.getId());
        assertEquals(35, comedy.getId());
        assertEquals(18, drama.getId());
        assertEquals(27, horror.getId());
        assertEquals(10749, romance.getId());
        assertEquals(878, sciFi.getId());
        assertEquals(53, thriller.getId());
    }
}
