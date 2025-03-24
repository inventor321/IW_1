package es.ucm.fdi.iw.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "participation", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "id_user", "id_event" })
})
@Data
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_event", nullable = false)
    private Event event;

    Timestamp timestamp;
    Boolean enabled;

    public Participation() {
    }

    public Participation(User user, Event event, Timestamp timestamp, Boolean enabled) {
        this.user = user;
        this.event = event;
        this.timestamp = timestamp;
        this.enabled = enabled;
    }

}
