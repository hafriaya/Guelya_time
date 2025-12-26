package service;

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

    //search movies 
    public List<Film> searchMovies(String query, int page) {
    String encodedQuery = URLEncoder.encode(query, "UTF-8");
    String url = BASE_URL + "/search/movie?api_key=" + API_KEY + "&query=" + encodedQuery;
    return fetchMovieList(url);
    }

    //get recommended movies 
    public List<Film> getRecommendedMovies(List<Integer> genreIds, int page) {
    StringBuilder genres = new StringBuilder();
    for (int i = 0; i < genreIds.size(); i++) {
        if (i > 0) genres.append(",");
        genres.append(genreIds.get(i));
    }
    String url = BASE_URL + "/discover/movie?with_genres=" + genres.toString();
    return fetchMovieList(url);
    }

    //fetch movie list
    private List <Film> fetchMovieList(String url) {
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