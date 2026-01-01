package service;

import model.Film;
import model.Genre;
import repository.FilmRepository;
import repository.GenreRepository;

import java.util.List;
import java.util.Optional;

public class FilmService {
    private final FilmRepository filmRepository;
    private final GenreRepository genreRepository;
    private final TmdbService tmdbService;

    public FilmService() {
        this.filmRepository = new FilmRepository();
        this.genreRepository = new GenreRepository();
        this.tmdbService = new TmdbService();
    }

    public FilmService(FilmRepository filmRepository, GenreRepository genreRepository, TmdbService tmdbService) {
        this.filmRepository = filmRepository;
        this.genreRepository = genreRepository;
        this.tmdbService = tmdbService;
    }

    // ==================== FILM DISCOVERY ====================

    // Get popular films from TMDB and save to Neo4j
    public List<Film> getPopularFilms(int page) {
        List<Film> films = tmdbService.getPopularMovies(page);
        saveFilmsToDatabase(films);
        return films;
    }

    // Get top rated films from TMDB
    public List<Film> getTopRatedFilms(int page) {
        List<Film> films = tmdbService.getTopRatedMovies(page);
        saveFilmsToDatabase(films);
        return films;
    }

    // Search films from TMDB
    public List<Film> searchFilms(String query, int page) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        List<Film> films = tmdbService.searchMovies(query.trim(), page);
        saveFilmsToDatabase(films);
        return films;
    }

    // Get films by genre from TMDB
    public List<Film> getFilmsByGenre(List<Integer> genreIds, int page) {
        if (genreIds == null || genreIds.isEmpty()) {
            return List.of();
        }
        List<Film> films = tmdbService.getRecommendedMovies(genreIds, page);
        saveFilmsToDatabase(films);
        return films;
    }

    // Save films to database
    private void saveFilmsToDatabase(List<Film> films) {
        for (Film film : films) {
            try {
                filmRepository.save(film);
            } catch (Exception e) {
                System.err.println("Error saving film: " + film.getTitle() + " - " + e.getMessage());
            }
        }
    }

    // ==================== FILM DETAILS ====================

    // Get film by ID (from DB first, then TMDB if not found)
    public Optional<Film> getFilmById(long filmId) {
        Optional<Film> film = filmRepository.findById(filmId);
        if (film.isPresent()) {
            return film;
        }
        // Could fetch from TMDB if not in DB (requires extending TmdbService)
        return Optional.empty();
    }

    // Get film from database
    public Optional<Film> getFilmFromDatabase(long filmId) {
        return filmRepository.findById(filmId);
    }

    // Search films in database
    public List<Film> searchFilmsInDatabase(String title) {
        if (title == null || title.trim().isEmpty()) {
            return List.of();
        }
        return filmRepository.findByTitle(title.trim());
    }

    // Get popular films from database
    public List<Film> getPopularFilmsFromDatabase(int limit) {
        return filmRepository.getPopularFilms(limit);
    }

    // Get films by genre from database
    public List<Film> getFilmsByGenreFromDatabase(int genreId, int limit) {
        return filmRepository.findByGenre(genreId, limit);
    }

    // ==================== WATCHLIST ====================

    // Add film to user's watchlist
    public void addToWatchlist(String userId, long filmId) {
        validateUserAndFilm(userId, filmId);
        
        // Ensure film exists in database
        ensureFilmExists(filmId);
        
        filmRepository.addToUserWatchlist(userId, filmId);
    }

    // Remove film from watchlist
    public void removeFromWatchlist(String userId, long filmId) {
        validateUserAndFilm(userId, filmId);
        filmRepository.removeFromUserWatchlist(userId, filmId);
    }

    // Get user's watchlist
    public List<Film> getUserWatchlist(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return List.of();
        }
        return filmRepository.getUserWatchlist(userId);
    }

    // Check if film is in watchlist
    public boolean isInWatchlist(String userId, long filmId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        return filmRepository.isInWatchlist(userId, filmId);
    }

    // ==================== FAVORITES ====================

    // Add film to favorites
    public void addToFavorites(String userId, long filmId) {
        validateUserAndFilm(userId, filmId);
        
        // Ensure film exists in database
        ensureFilmExists(filmId);
        
        filmRepository.addToUserFavorites(userId, filmId);
    }

    // Remove film from favorites
    public void removeFromFavorites(String userId, long filmId) {
        validateUserAndFilm(userId, filmId);
        filmRepository.removeFromUserFavorites(userId, filmId);
    }

    // Get user's favorite films
    public List<Film> getUserFavorites(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return List.of();
        }
        return filmRepository.getUserFavorites(userId);
    }

    // Check if film is in favorites
    public boolean isInFavorites(String userId, long filmId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        return filmRepository.isInFavorites(userId, filmId);
    }

    // ==================== RATINGS ====================

    // Rate a film (score 1-10)
    public void rateFilm(String userId, long filmId, int score) {
        validateUserAndFilm(userId, filmId);
        
        if (score < 1 || score > 10) {
            throw new IllegalArgumentException("Score must be between 1 and 10");
        }
        
        // Ensure film exists in database
        ensureFilmExists(filmId);
        
        filmRepository.rateFilm(userId, filmId, score);
    }

    // Get user's rating for a film
    public Optional<Integer> getUserRating(String userId, long filmId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }
        return filmRepository.getUserRating(userId, filmId);
    }

    // Remove user's rating
    public void removeRating(String userId, long filmId) {
        validateUserAndFilm(userId, filmId);
        filmRepository.removeRating(userId, filmId);
    }

    // Get all films rated by user
    public List<Film> getUserRatedFilms(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return List.of();
        }
        return filmRepository.getUserRatedFilms(userId);
    }

    // ==================== WATCHED ====================

    // Mark film as watched
    public void markAsWatched(String userId, long filmId) {
        validateUserAndFilm(userId, filmId);
        
        // Ensure film exists in database
        ensureFilmExists(filmId);
        
        filmRepository.markAsWatched(userId, filmId);
    }

    // Get user's watched films
    public List<Film> getUserWatchedFilms(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return List.of();
        }
        return filmRepository.getUserWatchedFilms(userId);
    }

    // ==================== USER GENRE PREFERENCES ====================

    // Set user's favorite genres
    public void setUserFavoriteGenres(String userId, List<Integer> genreIds) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (genreIds == null) {
            genreIds = List.of();
        }
        genreRepository.setUserFavoriteGenres(userId, genreIds);
    }

    // Add a genre to user's favorites
    public void addUserFavoriteGenre(String userId, int genreId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        genreRepository.addUserFavoriteGenre(userId, genreId);
    }

    // Remove a genre from user's favorites
    public void removeUserFavoriteGenre(String userId, int genreId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        genreRepository.removeUserFavoriteGenre(userId, genreId);
    }

    // Get user's favorite genres
    public List<Genre> getUserFavoriteGenres(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return List.of();
        }
        return genreRepository.getUserFavoriteGenres(userId);
    }

    // Get all available genres
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    // ==================== UTILITY METHODS ====================

    // Validate user ID and film ID
    private void validateUserAndFilm(String userId, long filmId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (filmId <= 0) {
            throw new IllegalArgumentException("Film ID must be positive");
        }
    }

    // Ensure film exists in database (fetch from TMDB if needed)
    private void ensureFilmExists(long filmId) {
        Optional<Film> film = filmRepository.findById(filmId);
        if (film.isEmpty()) {
            // Film not in database, try to fetch from TMDB
            // This requires extending TmdbService with getMovieDetails(long id)
            // For now, we just skip
            System.out.println("Film " + filmId + " not found in database. Consider fetching from TMDB.");
        }
    }

    // Delete a film from database
    public void deleteFilm(long filmId) {
        if (filmId <= 0) {
            throw new IllegalArgumentException("Film ID must be positive");
        }
        filmRepository.delete(filmId);
    }

    // Get user's film statistics
    public UserFilmStats getUserStats(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return new UserFilmStats(0, 0, 0, 0);
        }
        
        int watchlistCount = getUserWatchlist(userId).size();
        int favoritesCount = getUserFavorites(userId).size();
        int watchedCount = getUserWatchedFilms(userId).size();
        int ratedCount = getUserRatedFilms(userId).size();
        
        return new UserFilmStats(watchlistCount, favoritesCount, watchedCount, ratedCount);
    }

    // Inner class for user statistics
    public static class UserFilmStats {
        private final int watchlistCount;
        private final int favoritesCount;
        private final int watchedCount;
        private final int ratedCount;

        public UserFilmStats(int watchlistCount, int favoritesCount, int watchedCount, int ratedCount) {
            this.watchlistCount = watchlistCount;
            this.favoritesCount = favoritesCount;
            this.watchedCount = watchedCount;
            this.ratedCount = ratedCount;
        }

        public int getWatchlistCount() { return watchlistCount; }
        public int getFavoritesCount() { return favoritesCount; }
        public int getWatchedCount() { return watchedCount; }
        public int getRatedCount() { return ratedCount; }
    }
}
