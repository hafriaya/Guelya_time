package model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class User {
    private long id;
    private String name;
    private String email;
    private String password;
    private LocalDate dateInscription;  // LocalDate instead of Date

    public User() {}

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public User(long id, String name, String email, String password, LocalDate dateInscription) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.dateInscription = dateInscription;
    }

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

    // Convert epoch millis to LocalDate
    public void setDateFromEpoch(long epochMillis) {
        this.dateInscription = Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    // Convert LocalDate to epoch millis for Neo4j storage
    public long getDateAsEpoch() {
        return dateInscription.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
