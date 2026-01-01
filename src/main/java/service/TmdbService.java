package service;

import config.Neo4jConfig;
import model.Film;
import model.Genre;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class TmdbService {
    private final String apiKey;
    private final String baseUrl;

    public TmdbService() {
        this.apiKey = Neo4jConfig.getProperty("tmdb.api.key", "22c0aa4a342097dd598f010fd52eb22c");
        this.baseUrl = Neo4jConfig.getProperty("tmdb.base.url", "https://api.themoviedb.org/3");
    }

    // Get popular movies
    public List<Film> getPopularMovies(int page) {
        String url = baseUrl + "/movie/popular?api_key=" + apiKey + "&language=fr-FR&page=" + page;
        return fetchMovieList(url);
    }

    // Get top rated movies
    public List<Film> getTopRatedMovies(int page) {
        String url = baseUrl + "/movie/top_rated?api_key=" + apiKey + "&language=fr-FR&page=" + page;
        return fetchMovieList(url);
    }

    // Search movies
    public List<Film> searchMovies(String query, int page) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = baseUrl + "/search/movie?api_key=" + apiKey + "&language=fr-FR&query=" + encodedQuery + "&page=" + page;
            return fetchMovieList(url);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Get recommended movies by genre IDs
    public List<Film> getRecommendedMovies(List<Integer> genreIds, int page) {
        StringBuilder genres = new StringBuilder();
        for (int i = 0; i < genreIds.size(); i++) {
            if (i > 0) genres.append(",");
            genres.append(genreIds.get(i));
        }
        String url = baseUrl + "/discover/movie?api_key=" + apiKey + "&language=fr-FR&sort_by=popularity.desc&with_genres=" + genres.toString() + "&page=" + page;
        return fetchMovieList(url);
    }

    // Fetch movie list from URL
    private List<Film> fetchMovieList(String urlString) {
        List<Film> films = new ArrayList<>();
        try {
            String json = fetchJson(urlString);
            if (json == null) return films;

            int resultsStart = json.indexOf("\"results\":[");
            if (resultsStart == -1) return films;

            String resultsJson = json.substring(resultsStart + 11);

            List<String> movieObjects = splitJsonArray(resultsJson);

            for (String movieJson : movieObjects) {
                Film film = parseMovie(movieJson);
                if (film != null) {
                    films.add(film);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return films;
    }

    // Fetch JSON from URL
    private String fetchJson(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("HTTP Error: " + responseCode);
                return null;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            conn.disconnect();

            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Parse a single movie from JSON
    private Film parseMovie(String json) {
        try {
            Film film = new Film();
            film.setId(extractLong(json, "id"));
            film.setTitle(extractString(json, "title"));
            film.setOverview(extractString(json, "overview"));
            film.setPosterPath(extractString(json, "poster_path"));
            film.setReleaseDate(extractString(json, "release_date"));
            film.setVoteAverage(extractDouble(json, "vote_average"));
            film.setVoteCount(extractInt(json, "vote_count"));
            film.setPopularity(extractDouble(json, "popularity"));

            List<Integer> genreIds = extractIntArray(json, "genre_ids");
            for (int genreId : genreIds) {
                film.getGenres().add(new Genre(genreId, null));
            }

            return film;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== JSON PARSING HELPERS ====================

    private String extractString(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        int valueStart = keyIndex + searchKey.length();
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= json.length()) return null;
        char firstChar = json.charAt(valueStart);
        if (firstChar == 'n') return null;
        if (firstChar != '"') return null;

        valueStart++;
        StringBuilder value = new StringBuilder();
        for (int i = valueStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') break;
            if (c == '\\' && i + 1 < json.length()) {
                i++;
                c = json.charAt(i);
            }
            value.append(c);
        }
        return value.toString();
    }

    private long extractLong(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return 0;

        int valueStart = keyIndex + searchKey.length();
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        StringBuilder number = new StringBuilder();
        for (int i = valueStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isDigit(c) || c == '-') {
                number.append(c);
            } else if (number.length() > 0) {
                break;
            }
        }

        try {
            return Long.parseLong(number.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int extractInt(String json, String key) {
        return (int) extractLong(json, key);
    }

    private double extractDouble(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return 0.0;

        int valueStart = keyIndex + searchKey.length();
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        StringBuilder number = new StringBuilder();
        for (int i = valueStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isDigit(c) || c == '.' || c == '-') {
                number.append(c);
            } else if (number.length() > 0) {
                break;
            }
        }

        try {
            return Double.parseDouble(number.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private List<Integer> extractIntArray(String json, String key) {
        List<Integer> result = new ArrayList<>();
        String searchKey = "\"" + key + "\":[";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return result;

        int arrayStart = keyIndex + searchKey.length();
        int arrayEnd = json.indexOf(']', arrayStart);
        if (arrayEnd == -1) return result;

        String arrayContent = json.substring(arrayStart, arrayEnd);
        String[] parts = arrayContent.split(",");

        for (String part : parts) {
            part = part.trim();
            if (!part.isEmpty()) {
                try {
                    result.add(Integer.parseInt(part));
                } catch (NumberFormatException e) {
                    // Skip invalid entries
                }
            }
        }
        return result;
    }

    private List<String> splitJsonArray(String arrayContent) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);

            if (c == '{') {
                depth++;
                current.append(c);
            } else if (c == '}') {
                depth--;
                current.append(c);
                if (depth == 0) {
                    objects.add(current.toString());
                    current = new StringBuilder();
                }
            } else if (depth > 0) {
                current.append(c);
            }
        }
        return objects;
    }

    // ==================== MOVIE DETAILS ====================

    /**
     * Get detailed information about a specific movie
     */
    public Film getMovieDetails(long movieId) {
        String url = baseUrl + "/movie/" + movieId + "?api_key=" + apiKey + "&language=fr-FR";
        String json = fetchJson(url);
        if (json == null) return null;

        return parseMovieDetails(json);
    }

    /**
     * Parse movie details (includes full genre objects, not just IDs)
     */
    private Film parseMovieDetails(String json) {
        try {
            Film film = new Film();
            film.setId(extractLong(json, "id"));
            film.setTitle(extractString(json, "title"));
            film.setOverview(extractString(json, "overview"));
            film.setPosterPath(extractString(json, "poster_path"));
            film.setReleaseDate(extractString(json, "release_date"));
            film.setVoteAverage(extractDouble(json, "vote_average"));
            film.setVoteCount(extractInt(json, "vote_count"));
            film.setPopularity(extractDouble(json, "popularity"));

            // Parse full genre objects
            List<Genre> genres = extractGenres(json);
            film.setGenres(genres);

            return film;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract genres array with full objects (id and name)
     */
    private List<Genre> extractGenres(String json) {
        List<Genre> genres = new ArrayList<>();
        String searchKey = "\"genres\":[";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return genres;

        int arrayStart = keyIndex + searchKey.length();
        int depth = 1;
        int arrayEnd = arrayStart;

        for (int i = arrayStart; i < json.length() && depth > 0; i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') depth--;
            arrayEnd = i;
        }

        String genresJson = json.substring(arrayStart, arrayEnd);
        List<String> genreObjects = splitJsonArray("{" + genresJson + "}");

        for (String genreJson : genreObjects) {
            int id = extractInt(genreJson, "id");
            String name = extractString(genreJson, "name");
            if (id > 0) {
                genres.add(new Genre(id, name));
            }
        }

        return genres;
    }

    // ==================== MOVIE CREDITS (CAST) ====================

    /**
     * Get movie credits (cast and crew)
     */
    public List<model.Acteur> getMovieCredits(long movieId) {
        String url = baseUrl + "/movie/" + movieId + "/credits?api_key=" + apiKey + "&language=fr-FR";
        String json = fetchJson(url);
        if (json == null) return new ArrayList<>();

        return parseCast(json);
    }

    /**
     * Parse cast from credits JSON
     */
    private List<model.Acteur> parseCast(String json) {
        List<model.Acteur> cast = new ArrayList<>();

        String searchKey = "\"cast\":[";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return cast;

        int arrayStart = keyIndex + searchKey.length();
        String castJson = json.substring(arrayStart);

        List<String> castObjects = splitJsonArray(castJson);

        // Limit to top 10 cast members
        int limit = Math.min(castObjects.size(), 10);
        for (int i = 0; i < limit; i++) {
            String actorJson = castObjects.get(i);
            model.Acteur actor = parseActor(actorJson);
            if (actor != null) {
                cast.add(actor);
            }
        }

        return cast;
    }

    /**
     * Parse a single actor from JSON
     */
    private model.Acteur parseActor(String json) {
        try {
            String id = String.valueOf(extractLong(json, "id"));
            String name = extractString(json, "name");
            String profilePath = extractString(json, "profile_path");
            String character = extractString(json, "character");

            return new model.Acteur(id, name, profilePath, character);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== ALL GENRES ====================

    /**
     * Get all movie genres from TMDB
     */
    public List<Genre> getAllGenres() {
        String url = baseUrl + "/genre/movie/list?api_key=" + apiKey + "&language=fr-FR";
        String json = fetchJson(url);
        if (json == null) return new ArrayList<>();

        return extractGenres(json);
    }

    // ==================== SIMILAR MOVIES ====================

    /**
     * Get similar movies from TMDB
     */
    public List<Film> getSimilarMovies(long movieId, int page) {
        String url = baseUrl + "/movie/" + movieId + "/similar?api_key=" + apiKey + "&language=fr-FR&page=" + page;
        return fetchMovieList(url);
    }

    /**
     * Get movie recommendations from TMDB (based on the movie)
     */
    public List<Film> getMovieRecommendations(long movieId, int page) {
        String url = baseUrl + "/movie/" + movieId + "/recommendations?api_key=" + apiKey + "&language=fr-FR&page=" + page;
        return fetchMovieList(url);
    }

    // ==================== NOW PLAYING & UPCOMING ====================

    /**
     * Get now playing movies
     */
    public List<Film> getNowPlayingMovies(int page) {
        String url = baseUrl + "/movie/now_playing?api_key=" + apiKey + "&language=fr-FR&page=" + page;
        return fetchMovieList(url);
    }

    /**
     * Get upcoming movies
     */
    public List<Film> getUpcomingMovies(int page) {
        String url = baseUrl + "/movie/upcoming?api_key=" + apiKey + "&language=fr-FR&page=" + page;
        return fetchMovieList(url);
    }

    // ==================== IMAGE URLS ====================

    /**
     * Get full poster URL
     */
    public static String getPosterUrl(String posterPath, String size) {
        if (posterPath == null || posterPath.isEmpty()) {
            return null;
        }
        // Sizes: w92, w154, w185, w342, w500, w780, original
        return "https://image.tmdb.org/t/p/" + size + posterPath;
    }

    /**
     * Get full profile URL for actors
     */
    public static String getProfileUrl(String profilePath, String size) {
        if (profilePath == null || profilePath.isEmpty()) {
            return null;
        }
        // Sizes: w45, w185, h632, original
        return "https://image.tmdb.org/t/p/" + size + profilePath;
    }
}
