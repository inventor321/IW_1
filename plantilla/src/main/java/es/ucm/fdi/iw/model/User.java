package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDateTime;

/**
 * An authorized user of the system.
 */
@Entity
@Data
@NoArgsConstructor
@NamedQueries({
        @NamedQuery(name = "User.byUsername", query = "SELECT u FROM User u "
                + "WHERE u.username = :username AND u.enabled = TRUE"),
        @NamedQuery(name = "User.hasUsername", query = "SELECT COUNT(u) "
                + "FROM User u "
                + "WHERE u.username = :username")
})
@Table(name = "IWUser")
public class User implements Transferable<User.Transfer> {

    public enum Role {
        ADMIN, // admin users
        ORG, // org users
        USER, // normal users
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private long id;

    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String firstName;

    @Column(nullable = true)
    private String email;

    @Column(nullable = true)
    private String phonenumber;
    private String imageUrl;

    private LocalDateTime lastlogin;
    private boolean enabled;
    @Column(nullable = false)
    private String roles; // split by ',' to separate roles

    @OneToMany
    @JoinColumn(name = "user_id")
    private List<User> friends = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "sender_id")
    private List<Message> sent = new ArrayList<>();
    @OneToMany
    @JoinColumn(name = "recipient_id")
    private List<Message> received = new ArrayList<>();

    /**
     * Checks whether this user has a given role.
     * 
     * @param role to check
     * @return true iff this user has that role.
     */
    public boolean hasRole(Role role) {
        String roleName = role.name();
        return Arrays.asList(roles.split(",")).contains(roleName);
    }

    @Getter
    @AllArgsConstructor
    public static class Transfer {
        private long id;
        private String username;
        private String firstName;
        private String email;
        private int totalReceived;
        private int totalSent;
        private LocalDateTime lastlogin;
    }

    @Override
    public Transfer toTransfer() {
        return new Transfer(id, username, firstName, email, received.size(), sent.size(), lastlogin);
    }

    @Override
    public String toString() {
        return toTransfer().toString();
    }
}
