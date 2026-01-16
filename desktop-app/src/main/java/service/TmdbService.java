package service;

import config.Neo4jConfig;
import model.Acteur;
import model.Film;
import model.Genre;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;

public class TmdbService {
    private final String apiKey;
    private final String baseUrl;
    private static Map<Integer, String> genreCache; // Static cache shared across instances
    
    // Movie cache with expiration for faster repeated lookups
    private static final Map<Long, CachedFilm> movieCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 30 * 60 * 1000; // 30 minutes
    
    // Movie list cache for popular/top-rated pages
    private static final Map<String, CachedList> listCache = new ConcurrentHashMap<>();
    private static final long LIST_CACHE_DURATION_MS = 10 * 60 * 1000; // 10 minutes
    
    // ExecutorService for parallel requests
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);
    
    // Cache wrapper with timestamp for individual movies
    private static class CachedFilm {
        final Film film;
        final long timestamp;
        
        CachedFilm(Film film) {
            this.film = film;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }
    
    // Cache wrapper for movie lists
    private static class CachedList {
        final List<Film> films;
        final long timestamp;
        
        CachedList(List<Film> films) {
            this.films = new ArrayList<>(films);
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > LIST_CACHE_DURATION_MS;
        }
    }

    public TmdbService() {
        this.apiKey = Neo4jConfig.getProperty("tmdb.api.key", "22c0aa4a342097dd598f010fd52eb22c");
        this.baseUrl = Neo4jConfig.getProperty("tmdb.base.url", "https://api.themoviedb.org/3");
    }

    /**
     * Get genre map (ID -> Name) from TMDB, cached for performance
     */
    public Map<Integer, String> getGenreMap() {
        if (genreCache != null && !genreCache.isEmpty()) {
            return genreCache;
        }
        
        genreCache = new HashMap<>();
        String url = baseUrl + "/genre/movie/list?api_key=" + apiKey + "&language=fr-FR";
        
        try {
            String json = fetchJson(url);
            if (json == null) return genreCache;
            
            // Parse the genres array directly
            String searchKey = "\"genres\":[";
            int keyIndex = json.indexOf(searchKey);
            if (keyIndex == -1) return genreCache;

            int arrayStart = keyIndex + searchKey.length();
            int arrayEnd = json.indexOf(']', arrayStart);
            if (arrayEnd == -1) return genreCache;

            String genresArrayContent = json.substring(arrayStart, arrayEnd);
            
            // Split by },{ to get individual genre objects
            String[] genreStrings = genresArrayContent.split("\\},\\s*\\{");
            
            for (String genreStr : genreStrings) {
                // Clean up the string
                genreStr = genreStr.replace("{", "").replace("}", "");
                
                int id = 0;
                String name = null;
                
                // Extract id
                int idIndex = genreStr.indexOf("\"id\":");
                if (idIndex != -1) {
                    int idStart = idIndex + 5;
                    StringBuilder idBuilder = new StringBuilder();
                    for (int i = idStart; i < genreStr.length(); i++) {
                        char c = genreStr.charAt(i);
                        if (Character.isDigit(c)) {
                            idBuilder.append(c);
                        } else if (idBuilder.length() > 0) {
                            break;
                        }
                    }
                    if (idBuilder.length() > 0) {
                        id = Integer.parseInt(idBuilder.toString());
                    }
                }
                
                // Extract name
                int nameIndex = genreStr.indexOf("\"name\":\"");
                if (nameIndex != -1) {
                    int nameStart = nameIndex + 8;
                    int nameEnd = genreStr.indexOf("\"", nameStart);
                    if (nameEnd != -1) {
                        name = genreStr.substring(nameStart, nameEnd);
                    }
                }
                
                if (id > 0 && name != null && !name.isEmpty()) {
                    genreCache.put(id, name);
                }
            }
            
            System.out.println("Loaded " + genreCache.size() + " genres into cache");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return genreCache;
    }

    /**
     * Clear the genre cache (useful for testing or refreshing)
     */
    public static void clearGenreCache() {
        genreCache = null;
    }

    // Get popular movies (with caching)
    public List<Film> getPopularMovies(int page) {
        String cacheKey = "popular_" + page;
        CachedList cached = listCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            System.out.println("[Cache hit] Popular movies page " + page);
            return new ArrayList<>(cached.films);
        }
        
        String url = baseUrl + "/movie/popular?api_key=" + apiKey + "&language=fr-FR&page=" + page;
        List<Film> films = fetchMovieList(url);
        
        if (!films.isEmpty()) {
            listCache.put(cacheKey, new CachedList(films));
        }
        return films;
    }

    // Get top rated movies (with caching)
    public List<Film> getTopRatedMovies(int page) {
        String cacheKey = "toprated_" + page;
        CachedList cached = listCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            System.out.println("[Cache hit] Top rated movies page " + page);
            return new ArrayList<>(cached.films);
        }
        
        String url = baseUrl + "/movie/top_rated?api_key=" + apiKey + "&language=fr-FR&page=" + page;
        List<Film> films = fetchMovieList(url);
        
        if (!films.isEmpty()) {
            listCache.put(cacheKey, new CachedList(films));
        }
        return films;
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

    // Fetch JSON from URL with gzip support and optimized settings
    private String fetchJson(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-Encoding", "gzip"); // Request compressed responses
            conn.setConnectTimeout(5000);  // Reduced timeout for faster failure
            conn.setReadTimeout(5000);
            conn.setUseCaches(true);       // Enable HTTP caching

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("HTTP Error: " + responseCode);
                return null;
            }

            // Handle gzip compressed responses
            InputStream inputStream = conn.getInputStream();
            String encoding = conn.getContentEncoding();
            if ("gzip".equalsIgnoreCase(encoding)) {
                inputStream = new GZIPInputStream(inputStream);
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, "UTF-8")
            );

            StringBuilder response = new StringBuilder(8192); // Pre-allocate buffer
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                response.append(buffer, 0, read);
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

            // Load genre names from cache
            Map<Integer, String> genreMap = getGenreMap();
            
            List<Integer> genreIds = extractIntArray(json, "genre_ids");
            for (int genreId : genreIds) {
                String genreName = genreMap.getOrDefault(genreId, "Unknown");
                film.getGenres().add(new Genre(genreId, genreName));
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
     * Get detailed information about a specific movie (with caching)
     */
    public Film getMovieDetails(long movieId) {
        // Check cache first
        CachedFilm cached = movieCache.get(movieId);
        if (cached != null && !cached.isExpired()) {
            System.out.println("[Cache hit] Movie " + movieId);
            return cached.film;
        }
        
        String url = baseUrl + "/movie/" + movieId + "?api_key=" + apiKey + "&language=fr-FR";
        String json = fetchJson(url);
        if (json == null) return null;

        Film film = parseMovieDetails(json);
        
        // Cache the result
        if (film != null) {
            movieCache.put(movieId, new CachedFilm(film));
        }
        
        return film;
    }
    
    /**
     * Fetch multiple movies in parallel (useful for batch operations)
     */
    public List<Film> getMoviesInParallel(List<Long> movieIds) {
        List<Future<Film>> futures = new ArrayList<>();
        
        for (Long movieId : movieIds) {
            futures.add(executor.submit(() -> getMovieDetails(movieId)));
        }
        
        List<Film> films = new ArrayList<>();
        for (Future<Film> future : futures) {
            try {
                Film film = future.get(10, TimeUnit.SECONDS);
                if (film != null) {
                    films.add(film);
                }
            } catch (Exception e) {
                System.err.println("Error fetching movie in parallel: " + e.getMessage());
            }
        }
        
        return films;
    }
    
    /**
     * Clear all caches (useful for testing or forced refresh)
     */
    public static void clearAllCaches() {
        genreCache = null;
        movieCache.clear();
        listCache.clear();
        System.out.println("All TMDB caches cleared");
    }
    
    /**
     * Remove expired entries from caches
     */
    public static void cleanupExpiredCaches() {
        movieCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        listCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Parse movie details (includes full genre objects, not just IDs)
     */
    private Film parseMovieDetails(String json) {
        try {
            Film film = new Film();
            
            // Extract movie ID before genres section to avoid confusion with genre IDs
            // Find "id": at the start of the JSON (movie's ID comes first in TMDB response)
            int firstIdIndex = json.indexOf("\"id\":");
            int genresIndex = json.indexOf("\"genres\":");
            
            // If id comes before genres, extract it directly
            if (firstIdIndex != -1 && (genresIndex == -1 || firstIdIndex < genresIndex)) {
                film.setId(extractLong(json.substring(0, genresIndex != -1 ? genresIndex : json.length()), "id"));
            } else {
                // Fallback: try to find id after adult field which is typically near the start
                int adultIndex = json.indexOf("\"adult\":");
                if (adultIndex != -1) {
                    String afterAdult = json.substring(adultIndex);
                    int idInAfterAdult = afterAdult.indexOf("\"id\":");
                    if (idInAfterAdult != -1) {
                        film.setId(extractLong(afterAdult.substring(idInAfterAdult), "id"));
                    }
                }
            }
            
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

    // ==================== ALL GENRES ====================

    /**
     * Get all movie genres from TMDB (uses cache)
     */
    public List<Genre> getAllGenres() {
        List<Genre> genres = new ArrayList<>();
        Map<Integer, String> genreMap = getGenreMap();
        
        for (Map.Entry<Integer, String> entry : genreMap.entrySet()) {
            genres.add(new Genre(entry.getKey(), entry.getValue()));
        }
        
        return genres;
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

    // ==================== CAST/CREDITS ====================

    /**
     * Get movie cast (actors)
     */
    public List<Acteur> getMovieCast(long movieId) {
        return getMovieCast(movieId, 10); // Default to 10 actors
    }

    /**
     * Get movie cast with limit
     */
    public List<Acteur> getMovieCast(long movieId, int limit) {
        List<Acteur> cast = new ArrayList<>();
        String url = baseUrl + "/movie/" + movieId + "/credits?api_key=" + apiKey + "&language=fr-FR";
        
        try {
            String json = fetchJson(url);
            if (json == null) return cast;

            // Find the cast array
            int castStart = json.indexOf("\"cast\":[");
            if (castStart == -1) return cast;

            String castJson = json.substring(castStart + 8);
            List<String> actorObjects = splitJsonArray(castJson);

            int count = 0;
            for (String actorJson : actorObjects) {
                if (count >= limit) break;
                
                Acteur acteur = parseActor(actorJson);
                if (acteur != null) {
                    cast.add(acteur);
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cast;
    }

    /**
     * Parse actor from JSON
     */
    private Acteur parseActor(String json) {
        try {
            Acteur acteur = new Acteur();
            acteur.setId(extractLong(json, "id"));
            acteur.setName(extractString(json, "name"));
            acteur.setProfilePath(extractString(json, "profile_path"));
            acteur.setCharacter(extractString(json, "character"));
            acteur.setPopularity(extractDouble(json, "popularity"));
            return acteur;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get actor details
     */
    public Acteur getActorDetails(long actorId) {
        String url = baseUrl + "/person/" + actorId + "?api_key=" + apiKey + "&language=fr-FR";
        
        try {
            String json = fetchJson(url);
            if (json == null) return null;

            Acteur acteur = new Acteur();
            acteur.setId(extractLong(json, "id"));
            acteur.setName(extractString(json, "name"));
            acteur.setProfilePath(extractString(json, "profile_path"));
            acteur.setBiography(extractString(json, "biography"));
            acteur.setBirthday(extractString(json, "birthday"));
            acteur.setPlaceOfBirth(extractString(json, "place_of_birth"));
            acteur.setPopularity(extractDouble(json, "popularity"));
            
            return acteur;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get movies an actor is known for
     */
    public List<Film> getActorMovies(long actorId, int limit) {
        List<Film> films = new ArrayList<>();
        String url = baseUrl + "/person/" + actorId + "/movie_credits?api_key=" + apiKey + "&language=fr-FR";
        
        try {
            String json = fetchJson(url);
            if (json == null) return films;

            // Find the cast array (movies the actor appeared in)
            int castStart = json.indexOf("\"cast\":[");
            if (castStart == -1) return films;

            String castJson = json.substring(castStart + 8);
            List<String> movieObjects = splitJsonArray(castJson);

            // Use a set to track seen movie IDs and avoid duplicates
            java.util.Set<Long> seenIds = new java.util.HashSet<>();
            List<Film> allFilms = new ArrayList<>();
            for (String movieJson : movieObjects) {
                Film film = parseMovie(movieJson);
                if (film != null && film.getPosterPath() != null && !seenIds.contains(film.getId())) {
                    seenIds.add(film.getId());
                    allFilms.add(film);
                }
            }
            
            // Sort by popularity
            allFilms.sort((a, b) -> Double.compare(b.getPopularity(), a.getPopularity()));
            
            // Take top N films
            for (int i = 0; i < Math.min(limit, allFilms.size()); i++) {
                films.add(allFilms.get(i));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return films;
    }

    // ==================== DIRECTOR ====================

    /**
     * Get movie director from credits
     */
    public Acteur getMovieDirector(long movieId) {
        String url = baseUrl + "/movie/" + movieId + "/credits?api_key=" + apiKey + "&language=fr-FR";
        
        try {
            String json = fetchJson(url);
            if (json == null) return null;

            // Find the crew array
            int crewStart = json.indexOf("\"crew\":[");
            if (crewStart == -1) return null;

            String crewJson = json.substring(crewStart + 8);
            List<String> crewObjects = splitJsonArray(crewJson);

            // Find director in crew
            for (String crewMember : crewObjects) {
                String job = extractString(crewMember, "job");
                if ("Director".equals(job)) {
                    Acteur director = new Acteur();
                    director.setId(extractLong(crewMember, "id"));
                    director.setName(extractString(crewMember, "name"));
                    director.setProfilePath(extractString(crewMember, "profile_path"));
                    director.setPopularity(extractDouble(crewMember, "popularity"));
                    return director;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get movies directed by a person
     */
    public List<Film> getDirectorMovies(long personId, int limit) {
        List<Film> films = new ArrayList<>();
        String url = baseUrl + "/person/" + personId + "/movie_credits?api_key=" + apiKey + "&language=fr-FR";
        
        try {
            String json = fetchJson(url);
            if (json == null) return films;

            // Find the crew array (movies the person worked on)
            int crewStart = json.indexOf("\"crew\":[");
            if (crewStart == -1) return films;

            String crewJson = json.substring(crewStart + 8);
            List<String> movieObjects = splitJsonArray(crewJson);

            // Use a set to track seen movie IDs and avoid duplicates
            java.util.Set<Long> seenIds = new java.util.HashSet<>();
            List<Film> allFilms = new ArrayList<>();
            for (String movieJson : movieObjects) {
                String job = extractString(movieJson, "job");
                if ("Director".equals(job)) {
                    Film film = parseMovie(movieJson);
                    if (film != null && film.getPosterPath() != null && !seenIds.contains(film.getId())) {
                        seenIds.add(film.getId());
                        allFilms.add(film);
                    }
                }
            }
            
            // Sort by popularity
            allFilms.sort((a, b) -> Double.compare(b.getPopularity(), a.getPopularity()));
            
            // Take top N films
            for (int i = 0; i < Math.min(limit, allFilms.size()); i++) {
                films.add(allFilms.get(i));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return films;
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
