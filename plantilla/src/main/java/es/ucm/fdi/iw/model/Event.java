package es.ucm.fdi.iw.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "event") // Mejor en singular y en minúsculas
@Data
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

    public Event(String name, String description, LocalDateTime date, String location, String imageUrl, Long org) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.location = location;
        this.imageUrl = imageUrl;
        this.org = org;
    }

}
