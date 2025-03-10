package es.ucm.fdi.iw.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event") // Mejor en singular y en minúsculas
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Lob // Si la descripción puede ser larga
    private String description;

    private LocalDateTime date;
    private String location;
    private String imageUrl;

    @Transient // Evita conflictos con palabras reservadas
    private Long org;

    public Event() {
    }

    public Event(String name, String description, LocalDateTime date, String location, String imageUrl) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Long getOrg() { return org; }
    public void setOrg(Long org) { this.org = org; }
}
