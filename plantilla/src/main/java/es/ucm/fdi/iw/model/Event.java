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
    private LocalDateTime ending;
    private Integer aforo;
    private String location;
    private String imageUrl;
    private boolean active;

    private Long org;

    @Enumerated(EnumType.STRING) // Almacena el nombre de la categoría como texto
    private Category category;

    public Event() {
    }

    public Event(String name, String description, LocalDateTime date, LocalDateTime ending, String location,
            String imageUrl, Long org, Integer aforo, Category category) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.ending = ending;
        this.location = location;
        this.imageUrl = imageUrl;
        this.org = org;
        this.aforo = aforo;
        this.category = category;
        this.active = true; // Por defecto, el evento está habilitado
    }

    public void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public enum Category {
        CULTURA_Y_ARTE,
        MUSICA_Y_ESPECTACULOS,
        OCIO_Y_VIDA_NOCTURNA,
        NATURALEZA_Y_AIRE_LIBRE,
        GASTRONOMIA,
        BIENESTAR_Y_DESARROLLO_PERSONAL,
        JUEGOS_Y_ENTRETENIMIENTO,
        DEPORTES_Y_ACTIVIDADES_FISICAS,
        NETWORKING_Y_PROFESIONALES
    }
}
