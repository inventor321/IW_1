package es.ucm.fdi.iw.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "usergroup")
@Data
public class UserGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long eventId;

    public UserGroup() {

    }

    public UserGroup(String name, Long eventId) {
        this.name = name;
        this.eventId = eventId;
    }

}