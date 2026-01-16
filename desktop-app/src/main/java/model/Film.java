package model;

import java.util.List;
import java.util.ArrayList;


public class Film {
    private long id;
    private String title;
    private String overview;
    private String posterPath;
    private String releaseDate;
    private double voteAverage;
    private int voteCount;
    private double popularity;
    private List<Genre> genres;
    private Acteur director;

    public Film() {
        this.genres = new ArrayList<>();
    }


    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(double voteAverage) { this.voteAverage = voteAverage; }

    public int getVoteCount() { return voteCount; }
    public void setVoteCount(int voteCount) { this.voteCount = voteCount; }

    public double getPopularity() { return popularity; }
    public void setPopularity(double popularity) { this.popularity = popularity; }

    public List<Genre> getGenres() { return genres; }
    public void setGenres(List<Genre> genres) { this.genres = genres; }

    public Acteur getDirector() { return director; }
    public void setDirector(Acteur director) { this.director = director; }


     // Get full poster URL (high quality for details view)
    public String getFullPosterUrl() {
        if (posterPath == null || posterPath.isEmpty()) return null;
        return "https://image.tmdb.org/t/p/w500" + posterPath;
    }
    
    // Get small poster URL (faster loading for grid/list views)
    public String getSmallPosterUrl() {
        if (posterPath == null || posterPath.isEmpty()) return null;
        return "https://image.tmdb.org/t/p/w185" + posterPath;
    }
}
