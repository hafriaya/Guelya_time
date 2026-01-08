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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FilmService using mocks
 */
@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private TmdbService tmdbService;

    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmService = new FilmService(filmRepository, genreRepository, tmdbService);
    }

    // ==================== FILM DISCOVERY ====================

    @Test
    @DisplayName("getPopularFilms should fetch from TMDB and save to database")
    void testGetPopularFilms() {
        // Arrange
        Film mockFilm = createMockFilm(1L, "Test Movie");
        when(tmdbService.getPopularMovies(1)).thenReturn(List.of(mockFilm));

        // Act
        List<Film> films = filmService.getPopularFilms(1);

        // Assert
        assertNotNull(films);
        assertEquals(1, films.size());
        assertEquals("Test Movie", films.get(0).getTitle());
        verify(filmRepository, times(1)).save(any(Film.class));
    }

    @Test
    @DisplayName("searchFilms should return empty list for null query")
    void testSearchFilmsNullQuery() {
        List<Film> films = filmService.searchFilms(null, 1);
        
        assertTrue(films.isEmpty());
        verify(tmdbService, never()).searchMovies(anyString(), anyInt());
    }

    @Test
    @DisplayName("searchFilms should return empty list for empty query")
    void testSearchFilmsEmptyQuery() {
        List<Film> films = filmService.searchFilms("   ", 1);
        
        assertTrue(films.isEmpty());
        verify(tmdbService, never()).searchMovies(anyString(), anyInt());
    }

    @Test
    @DisplayName("searchFilms should trim query and search")
    void testSearchFilmsTrimQuery() {
        Film mockFilm = createMockFilm(1L, "Inception");
        when(tmdbService.searchMovies("Inception", 1)).thenReturn(List.of(mockFilm));

        List<Film> films = filmService.searchFilms("  Inception  ", 1);

        assertEquals(1, films.size());
        verify(tmdbService).searchMovies("Inception", 1);
    }

    // ==================== FILM DETAILS ====================

    @Test
    @DisplayName("getFilmById should return film from database")
    void testGetFilmById() {
        Film mockFilm = createMockFilm(123L, "Test Movie");
        when(filmRepository.findById(123L)).thenReturn(Optional.of(mockFilm));

        Optional<Film> result = filmService.getFilmById(123L);

        assertTrue(result.isPresent());
        assertEquals("Test Movie", result.get().getTitle());
    }

    @Test
    @DisplayName("getFilmById should return empty when film not found")
    void testGetFilmByIdNotFound() {
        when(filmRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Film> result = filmService.getFilmById(999L);

        assertFalse(result.isPresent());
    }

    // ==================== WATCHLIST ====================

    @Test
    @DisplayName("addToWatchlist should add film to user's watchlist")
    void testAddToWatchlist() {
        when(filmRepository.findById(1L)).thenReturn(Optional.of(createMockFilm(1L, "Movie")));

        filmService.addToWatchlist("user123", 1L);

        verify(filmRepository).addToUserWatchlist("user123", 1L);
    }

    @Test
    @DisplayName("addToWatchlist should throw exception for null userId")
    void testAddToWatchlistNullUser() {
        assertThrows(IllegalArgumentException.class, () -> 
            filmService.addToWatchlist(null, 1L));
    }

    @Test
    @DisplayName("addToWatchlist should throw exception for empty userId")
    void testAddToWatchlistEmptyUser() {
        assertThrows(IllegalArgumentException.class, () -> 
            filmService.addToWatchlist("", 1L));
    }

    @Test
    @DisplayName("addToWatchlist should throw exception for invalid filmId")
    void testAddToWatchlistInvalidFilmId() {
        assertThrows(IllegalArgumentException.class, () -> 
            filmService.addToWatchlist("user123", 0L));
    }

    @Test
    @DisplayName("removeFromWatchlist should remove film from watchlist")
    void testRemoveFromWatchlist() {
        filmService.removeFromWatchlist("user123", 1L);

        verify(filmRepository).removeFromUserWatchlist("user123", 1L);
    }

    @Test
    @DisplayName("getUserWatchlist should return films for valid user")
    void testGetUserWatchlist() {
        Film mockFilm = createMockFilm(1L, "Movie");
        when(filmRepository.getUserWatchlist("user123")).thenReturn(List.of(mockFilm));

        List<Film> watchlist = filmService.getUserWatchlist("user123");

        assertEquals(1, watchlist.size());
    }

    @Test
    @DisplayName("getUserWatchlist should return empty list for null user")
    void testGetUserWatchlistNullUser() {
        List<Film> watchlist = filmService.getUserWatchlist(null);

        assertTrue(watchlist.isEmpty());
        verify(filmRepository, never()).getUserWatchlist(anyString());
    }

    @Test
    @DisplayName("isInWatchlist should check correctly")
    void testIsInWatchlist() {
        when(filmRepository.isInWatchlist("user123", 1L)).thenReturn(true);

        assertTrue(filmService.isInWatchlist("user123", 1L));
    }

    // ==================== FAVORITES ====================

    @Test
    @DisplayName("addToFavorites should add film to favorites")
    void testAddToFavorites() {
        when(filmRepository.findById(1L)).thenReturn(Optional.of(createMockFilm(1L, "Movie")));

        filmService.addToFavorites("user123", 1L);

        verify(filmRepository).addToUserFavorites("user123", 1L);
    }

    @Test
    @DisplayName("getUserFavorites should return favorites")
    void testGetUserFavorites() {
        Film mockFilm = createMockFilm(1L, "Favorite Movie");
        when(filmRepository.getUserFavorites("user123")).thenReturn(List.of(mockFilm));

        List<Film> favorites = filmService.getUserFavorites("user123");

        assertEquals(1, favorites.size());
    }

    // ==================== RATINGS ====================

    @Test
    @DisplayName("rateFilm should save rating")
    void testRateFilm() {
        when(filmRepository.findById(1L)).thenReturn(Optional.of(createMockFilm(1L, "Movie")));

        filmService.rateFilm("user123", 1L, 8);

        verify(filmRepository).rateFilm("user123", 1L, 8);
    }

    @Test
    @DisplayName("rateFilm should throw exception for invalid score (too low)")
    void testRateFilmInvalidScoreLow() {
        assertThrows(IllegalArgumentException.class, () -> 
            filmService.rateFilm("user123", 1L, 0));
    }

    @Test
    @DisplayName("rateFilm should throw exception for invalid score (too high)")
    void testRateFilmInvalidScoreHigh() {
        assertThrows(IllegalArgumentException.class, () -> 
            filmService.rateFilm("user123", 1L, 11));
    }

    @Test
    @DisplayName("rateFilm should accept minimum valid score")
    void testRateFilmMinScore() {
        when(filmRepository.findById(1L)).thenReturn(Optional.of(createMockFilm(1L, "Movie")));

        assertDoesNotThrow(() -> filmService.rateFilm("user123", 1L, 1));
    }

    @Test
    @DisplayName("rateFilm should accept maximum valid score")
    void testRateFilmMaxScore() {
        when(filmRepository.findById(1L)).thenReturn(Optional.of(createMockFilm(1L, "Movie")));

        assertDoesNotThrow(() -> filmService.rateFilm("user123", 1L, 10));
    }

    @Test
    @DisplayName("getUserRating should return rating")
    void testGetUserRating() {
        when(filmRepository.getUserRating("user123", 1L)).thenReturn(Optional.of(8));

        Optional<Integer> rating = filmService.getUserRating("user123", 1L);

        assertTrue(rating.isPresent());
        assertEquals(8, rating.get());
    }

    // ==================== WATCHED ====================

    @Test
    @DisplayName("markAsWatched should mark film as watched")
    void testMarkAsWatched() {
        when(filmRepository.findById(1L)).thenReturn(Optional.of(createMockFilm(1L, "Movie")));

        filmService.markAsWatched("user123", 1L);

        verify(filmRepository).markAsWatched("user123", 1L);
    }

    // ==================== USER GENRE PREFERENCES ====================

    @Test
    @DisplayName("setUserFavoriteGenres should save genres")
    void testSetUserFavoriteGenres() {
        List<Integer> genreIds = List.of(28, 12, 35);

        filmService.setUserFavoriteGenres("user123", genreIds);

        verify(genreRepository).setUserFavoriteGenres("user123", genreIds);
    }

    @Test
    @DisplayName("setUserFavoriteGenres with null list should use empty list")
    void testSetUserFavoriteGenresNullList() {
        filmService.setUserFavoriteGenres("user123", null);

        verify(genreRepository).setUserFavoriteGenres(eq("user123"), eq(List.of()));
    }

    @Test
    @DisplayName("getUserFavoriteGenres should return genres")
    void testGetUserFavoriteGenres() {
        Genre mockGenre = new Genre(28, "Action");
        when(genreRepository.getUserFavoriteGenres("user123")).thenReturn(List.of(mockGenre));

        List<Genre> genres = filmService.getUserFavoriteGenres("user123");

        assertEquals(1, genres.size());
        assertEquals("Action", genres.get(0).getName());
    }

    // ==================== USER STATS ====================

    @Test
    @DisplayName("getUserStats should return correct counts")
    void testGetUserStats() {
        when(filmRepository.getUserWatchlist("user123")).thenReturn(List.of(createMockFilm(1L, "A")));
        when(filmRepository.getUserFavorites("user123")).thenReturn(List.of(createMockFilm(2L, "B"), createMockFilm(3L, "C")));
        when(filmRepository.getUserWatchedFilms("user123")).thenReturn(List.of(createMockFilm(4L, "D")));
        when(filmRepository.getUserRatedFilms("user123")).thenReturn(List.of());

        FilmService.UserFilmStats stats = filmService.getUserStats("user123");

        assertEquals(1, stats.getWatchlistCount());
        assertEquals(2, stats.getFavoritesCount());
        assertEquals(1, stats.getWatchedCount());
        assertEquals(0, stats.getRatedCount());
    }

    @Test
    @DisplayName("getUserStats should return zeros for null user")
    void testGetUserStatsNullUser() {
        FilmService.UserFilmStats stats = filmService.getUserStats(null);

        assertEquals(0, stats.getWatchlistCount());
        assertEquals(0, stats.getFavoritesCount());
        assertEquals(0, stats.getWatchedCount());
        assertEquals(0, stats.getRatedCount());
    }

    // ==================== HELPER METHODS ====================

    private Film createMockFilm(long id, String title) {
        Film film = new Film();
        film.setId(id);
        film.setTitle(title);
        film.setOverview("Test overview");
        film.setVoteAverage(7.5);
        film.setPopularity(100.0);
        return film;
    }
}
