package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Acteur model
 */
class ActeurTest {

    @Test
    @DisplayName("Acteur constructor should set all properties")
    void testConstructor() {
        Acteur acteur = new Acteur(12345L, "Leonardo DiCaprio", "/profile.jpg", "Dom Cobb");

        assertEquals(12345L, acteur.getId());
        assertEquals("Leonardo DiCaprio", acteur.getName());
        assertEquals("/profile.jpg", acteur.getProfilePath());
        assertEquals("Dom Cobb", acteur.getCharacter());
    }

    @Test
    @DisplayName("Acteur getters and setters should work")
    void testGettersAndSetters() {
        Acteur acteur = new Acteur(1L, "Name", null, null);

        acteur.setId(99999L);
        acteur.setName("Tom Hanks");
        acteur.setProfilePath("/tom.jpg");
        acteur.setCharacter("Forrest Gump");

        assertEquals(99999L, acteur.getId());
        assertEquals("Tom Hanks", acteur.getName());
        assertEquals("/tom.jpg", acteur.getProfilePath());
        assertEquals("Forrest Gump", acteur.getCharacter());
    }

    @Test
    @DisplayName("Acteur should handle null values")
    void testNullValues() {
        Acteur acteur = new Acteur();

        assertEquals(0L, acteur.getId());
        assertNull(acteur.getName());
        assertNull(acteur.getProfilePath());
        assertNull(acteur.getCharacter());
    }

    @Test
    @DisplayName("Acteur profilePath can be null for actors without photos")
    void testNullProfilePath() {
        Acteur acteur = new Acteur(123L, "Unknown Actor", null, "Extra");

        assertEquals(123L, acteur.getId());
        assertEquals("Unknown Actor", acteur.getName());
        assertNull(acteur.getProfilePath());
        assertEquals("Extra", acteur.getCharacter());
    }
    
    @Test
    @DisplayName("getFullProfileUrl should return valid URL when profilePath exists")
    void testGetFullProfileUrl() {
        Acteur acteur = new Acteur(1L, "Test", "/abc123.jpg", null);
        assertEquals("https://image.tmdb.org/t/p/w185/abc123.jpg", acteur.getFullProfileUrl());
    }
    
    @Test
    @DisplayName("getFullProfileUrl should return null when profilePath is null")
    void testGetFullProfileUrlNull() {
        Acteur acteur = new Acteur(1L, "Test", null, null);
        assertNull(acteur.getFullProfileUrl());
    }
}
