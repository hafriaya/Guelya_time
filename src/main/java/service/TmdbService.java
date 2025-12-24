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

//fetch movie list from TMDB API
private List<Film> fetchMovieList(String url) {
    try {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.8,en-US;q=0.6,en;q=0.4");
        con.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
}