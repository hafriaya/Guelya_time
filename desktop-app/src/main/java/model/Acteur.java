package model;

import java.util.ArrayList;
import java.util.List;

public class Acteur {
    private long id;
    private String name;
    private String profilePath;
    private String character;
    private String biography;
    private String birthday;
    private String placeOfBirth;
    private double popularity;
    private List<Film> knownFor;

    public Acteur() {
        this.knownFor = new ArrayList<>();
    }

    public Acteur(long id, String name, String profilePath, String character) {
        this.id = id;
        this.name = name;
        this.profilePath = profilePath;
        this.character = character;
        this.knownFor = new ArrayList<>();
    }

    // Legacy constructor for String id
    public Acteur(String id, String name, String profilePath, String character) {
        this.id = Long.parseLong(id);
        this.name = name;
        this.profilePath = profilePath;
        this.character = character;
        this.knownFor = new ArrayList<>();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProfilePath() { return profilePath; }
    public void setProfilePath(String profilePath) { this.profilePath = profilePath; }

    public String getCharacter() { return character; }
    public void setCharacter(String character) { this.character = character; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getPlaceOfBirth() { return placeOfBirth; }
    public void setPlaceOfBirth(String placeOfBirth) { this.placeOfBirth = placeOfBirth; }

    public double getPopularity() { return popularity; }
    public void setPopularity(double popularity) { this.popularity = popularity; }

    public List<Film> getKnownFor() { return knownFor; }
    public void setKnownFor(List<Film> knownFor) { this.knownFor = knownFor; }

    public String getFullProfileUrl() {
        if (profilePath == null || profilePath.isEmpty()) return null;
        return "https://image.tmdb.org/t/p/w185" + profilePath;
    }

    public String getLargeProfileUrl() {
        if (profilePath == null || profilePath.isEmpty()) return null;
        return "https://image.tmdb.org/t/p/w500" + profilePath;
    }
}
