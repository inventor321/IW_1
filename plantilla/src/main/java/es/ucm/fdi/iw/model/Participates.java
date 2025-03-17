package es.ucm.fdi.iw.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "participates")
public class Participates {
    
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_event", nullable=false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_group", nullable=false)
    private UserGroup userGroup;

    public Participates() {
    }

    public Participates(User user, UserGroup userGroup) {
        this.user = user;
        this.userGroup = userGroup;
       
    }

    public Long getId() {
        return id;
    }   

    public User getUser() {
        return user;
    }   

    public UserGroup getUserGroup() {
        return userGroup;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

}
