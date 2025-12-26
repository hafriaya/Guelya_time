package service;

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
    private static final String API_KEY = "22c0aa4a342097dd598f010fd52eb22c";
    private static final String BASE_URL = "https://api.themoviedb.org/3";

    // Get popular movies
    public List<Film> getPopularMovies(int page) {
        String url = BASE_URL + "/movie/popular?api_key=" + API_KEY + "&language=fr-FR&page=" + page;
        return fetchMovieList(url);
    }

    // Get top rated movies
    public List<Film> getTopRatedMovies(int page) {
        String url = BASE_URL + "/movie/top_rated?api_key=" + API_KEY + "&language=fr-FR&page=" + page;
        return fetchMovieList(url);
    }

    // Search movies
    public List<Film> searchMovies(String query, int page) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = BASE_URL + "/search/movie?api_key=" + API_KEY + "&language=fr-FR&query=" + encodedQuery + "&page=" + page;
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
        String url = BASE_URL + "/discover/movie?api_key=" + API_KEY + "&language=fr-FR&sort_by=popularity.desc&with_genres=" + genres.toString() + "&page=" + page;
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
}
