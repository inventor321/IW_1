package es.ucm.fdi.iw.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "membership")
public class Membership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_group", nullable = false)
    private UserGroup usergroup;

    public Membership() {
    }

    public Membership(User user, UserGroup usergroup) {
        this.user = user;
        this.usergroup = usergroup;
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public UserGroup getUsergroup() {
        return usergroup;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUsergroup(UserGroup usergroup) {
        this.usergroup = usergroup;
    }

}
