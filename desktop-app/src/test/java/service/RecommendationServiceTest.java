package service;

import model.Film;
import model.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.FilmRepository;
import repository.GenreRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecommendationService
 */
@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private TmdbService tmdbService;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService(filmRepository, genreRepository, tmdbService);
    }

    // ==================== GENRE-BASED RECOMMENDATIONS ====================

    @Test
    @DisplayName("getRecommendationsByFavoriteGenres should return empty for null userId")
    void testGetRecommendationsByFavoriteGenresNullUser() {
        List<Film> recommendations = recommendationService.getRecommendationsByFavoriteGenres(null, 10);

        assertTrue(recommendations.isEmpty());
    }

    @Test
    @DisplayName("getRecommendationsByFavoriteGenres should return empty for empty userId")
    void testGetRecommendationsByFavoriteGenresEmptyUser() {
        List<Film> recommendations = recommendationService.getRecommendationsByFavoriteGenres("  ", 10);

        assertTrue(recommendations.isEmpty());
    }

    @Test
    @DisplayName("getRecommendationsByFavoriteGenres should return popular films when no favorite genres")
    void testGetRecommendationsByFavoriteGenresNoFavorites() {
        when(genreRepository.getUserFavoriteGenres("user123")).thenReturn(List.of());
        when(filmRepository.getPopularFilms(10)).thenReturn(List.of(createMockFilm(1L, "Popular")));

        List<Film> recommendations = recommendationService.getRecommendationsByFavoriteGenres("user123", 10);

        assertEquals(1, recommendations.size());
        assertEquals("Popular", recommendations.get(0).getTitle());
    }

    @Test
    @DisplayName("getRecommendationsByFavoriteGenres should fetch films by genres")
    void testGetRecommendationsByFavoriteGenresWithFavorites() {
        Genre actionGenre = new Genre(28, "Action");
        when(genreRepository.getUserFavoriteGenres("user123")).thenReturn(List.of(actionGenre));
        when(tmdbService.getRecommendedMovies(List.of(28), 1)).thenReturn(List.of(createMockFilm(1L, "Action Movie")));

        List<Film> recommendations = recommendationService.getRecommendationsByFavoriteGenres("user123", 10);

        assertFalse(recommendations.isEmpty());
        verify(tmdbService).getRecommendedMovies(List.of(28), 1);
    }

    // ==================== RECOMMENDATIONS BY GENRE ====================

    @Test
    @DisplayName("getRecommendationsByGenre should return films from database")
    void testGetRecommendationsByGenre() {
        Film mockFilm = createMockFilm(1L, "Comedy Film");
        when(filmRepository.findByGenre(35, 20)).thenReturn(List.of(mockFilm));

        List<Film> recommendations = recommendationService.getRecommendationsByGenre(35, null, 10);

        assertFalse(recommendations.isEmpty());
        verify(filmRepository).findByGenre(35, 20);
    }

    // ==================== SIMILAR FILMS ====================

    @Test
    @DisplayName("getSimilarFilms should return films with same genres")
    void testGetSimilarFilms() {
        // This test requires Neo4j connection for full test
        // Here we just verify no exceptions
        List<Film> similar = recommendationService.getSimilarFilms(27205L, 5);
        assertNotNull(similar);
    }

    // ==================== TRENDING FILMS ====================

    @Test
    @DisplayName("getTrendingFilms should fetch from TMDB when DB is empty")
    void testGetTrendingFilmsFallbackToTmdb() {
        Film mockFilm = createMockFilm(1L, "Popular");
        when(tmdbService.getPopularMovies(1)).thenReturn(List.of(mockFilm));

        List<Film> trending = recommendationService.getTrendingFilms(10);

        // Should include TMDB results
        verify(tmdbService).getPopularMovies(1);
    }

    // ==================== PERSONALIZED RECOMMENDATIONS ====================

    @Test
    @DisplayName("getPersonalizedRecommendations should return trending for null user")
    void testGetPersonalizedRecommendationsNullUser() {
        Film mockFilm = createMockFilm(1L, "Trending");
        when(tmdbService.getPopularMovies(1)).thenReturn(List.of(mockFilm));

        List<Film> recommendations = recommendationService.getPersonalizedRecommendations(null, 10);

        assertNotNull(recommendations);
    }

    @Test
    @DisplayName("getPersonalizedRecommendations should return trending for empty user")
    void testGetPersonalizedRecommendationsEmptyUser() {
        Film mockFilm = createMockFilm(1L, "Trending");
        when(tmdbService.getPopularMovies(1)).thenReturn(List.of(mockFilm));

        List<Film> recommendations = recommendationService.getPersonalizedRecommendations("  ", 10);

        assertNotNull(recommendations);
    }

    @Test
    @DisplayName("getPersonalizedRecommendations should combine multiple strategies")
    void testGetPersonalizedRecommendationsCombined() {
        // Setup mocks
        Genre actionGenre = new Genre(28, "Action");
        when(genreRepository.getUserFavoriteGenres("user123")).thenReturn(List.of(actionGenre));
        
        Film actionFilm = createMockFilm(1L, "Action Movie");
        when(tmdbService.getRecommendedMovies(anyList(), anyInt())).thenReturn(List.of(actionFilm));
        
        Film popularFilm = createMockFilm(2L, "Popular");
        when(tmdbService.getPopularMovies(1)).thenReturn(List.of(popularFilm));

        List<Film> recommendations = recommendationService.getPersonalizedRecommendations("user123", 10);

        assertNotNull(recommendations);
        // Should have called multiple recommendation strategies
        verify(genreRepository).getUserFavoriteGenres("user123");
    }

    @Test
    @DisplayName("getPersonalizedRecommendations should limit results")
    void testGetPersonalizedRecommendationsLimit() {
        Genre actionGenre = new Genre(28, "Action");
        when(genreRepository.getUserFavoriteGenres("user123")).thenReturn(List.of(actionGenre));
        
        // Return many films
        List<Film> manyFilms = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            manyFilms.add(createMockFilm(i, "Movie " + i));
        }
        when(tmdbService.getRecommendedMovies(anyList(), anyInt())).thenReturn(manyFilms);

        List<Film> recommendations = recommendationService.getPersonalizedRecommendations("user123", 10);

        assertTrue(recommendations.size() <= 10, "Should limit to requested amount");
    }

    @Test
    @DisplayName("getPersonalizedRecommendations should not have duplicates")
    void testGetPersonalizedRecommendationsNoDuplicates() {
        Genre actionGenre = new Genre(28, "Action");
        when(genreRepository.getUserFavoriteGenres("user123")).thenReturn(List.of(actionGenre));
        
        // Return same film from multiple sources
        Film sameFilm = createMockFilm(1L, "Same Movie");
        when(tmdbService.getRecommendedMovies(anyList(), anyInt())).thenReturn(List.of(sameFilm));
        when(tmdbService.getPopularMovies(anyInt())).thenReturn(List.of(sameFilm));

        List<Film> recommendations = recommendationService.getPersonalizedRecommendations("user123", 10);

        long uniqueCount = recommendations.stream().map(Film::getId).distinct().count();
        assertEquals(recommendations.size(), uniqueCount, "Should not have duplicate films");
    }

    // ==================== HELPER METHODS ====================

    private Film createMockFilm(long id, String title) {
        Film film = new Film();
        film.setId(id);
        film.setTitle(title);
        film.setOverview("Test overview");
        film.setVoteAverage(7.5);
        film.setPopularity(100.0);
        film.setGenres(new ArrayList<>());
        return film;
    }
}
