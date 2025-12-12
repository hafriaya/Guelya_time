package model;

import java.time.LocalDate;

public class User {
    private long id;
    private String name;
    private String email;
    private String password;
    private LocalDate dateInscription;

    public User() {}

    public User(long id, String name, String email, String password, LocalDate dateInscription) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.dateInscription = dateInscription;
    }

    public User(String name, String email, String password, LocalDate dateInscription) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.dateInscription = dateInscription;
    }

    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }
}
