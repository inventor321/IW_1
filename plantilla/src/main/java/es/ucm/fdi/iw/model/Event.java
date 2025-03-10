package es.ucm.fdi.iw.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private LocalDateTime date;
    private String location;
    private String imageUrl;
    private Long org;
    @ManyToOne
    private User user;

    public Event() {
    }

    public Event(String name, String description, LocalDateTime date, String location, String imageUrl, Long org) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.location = location;
        this.imageUrl = imageUrl;
        this.org = org;
    }

    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    public void setDescription(String description) { this.description = description; }
    public String getDescription() { return description; }

    public void setDate(LocalDateTime date) { this.date = date; }
    public LocalDateTime getDate() { return date; }

    public void setLocation(String location) { this.location = location; }
    public String getLocation() { return location; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getImageUrl() { return imageUrl; }

    public void setOrg(Long org) { this.org = org; }
    public Long getOrg() { return org; }

}