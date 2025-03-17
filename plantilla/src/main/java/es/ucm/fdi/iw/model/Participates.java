package es.ucm.fdi.iw.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "participates")
public class Participates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long idGroup;
    private Long idUser;

    public Participates() {
    }

    public Participates(Long id, Long idUser) {
        this.id = id;
        this.idUser = idUser;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }    

    public Long getIdUser() { return idUser; }

    public void setIdUser(Long idUser) { this.idUser = idUser; }


}
