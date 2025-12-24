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
}