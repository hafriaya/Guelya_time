package model;

public class User {
    private String email;
    private String password;
    private String name;

    // Nouveaux champs pour le dashboard
    private String dateInscription;
    private String genresFavoris;

    public User() {}

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.dateInscription = java.time.LocalDate.now().toString();
    }

    // Getters
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getDateInscription() { return dateInscription; }
    public String getGenresFavoris() { return genresFavoris; }

    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setDateInscription(String date) { this.dateInscription = date; }
    public void setGenresFavoris(String genres) { this.genresFavoris = genres; }
}