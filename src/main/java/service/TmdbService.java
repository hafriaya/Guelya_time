package service;

public class TmdbService {
    private static final String API_KEY = "22c0aa4a342097dd598f010fd52eb22c";
    private static final String BASE_URL = "https://api.themoviedb.org/3";

    
    // Get popular movies
    public List<Film> getPopularMovies() {
        String url = BASE_URL + "/movie/popular?api_key=" + API_KEY;
        return fetchFilms(url);
    }
}