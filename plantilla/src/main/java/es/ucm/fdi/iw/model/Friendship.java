package es.ucm.fdi.iw.model;

import java.sql.Timestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "friendship", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "id_user1", "id_user2" })
})
@Data
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_user1", nullable = false)
    private User user1;

    @ManyToOne
    @JoinColumn(name = "id_user2", nullable = false)
    private User user2;

    @Column(nullable = false)
    private Timestamp createdAt;

    private Timestamp acceptedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;

    @Column(nullable = false)
    private Boolean isAccepted = false;

    public enum FriendshipStatus {
        PENDING,
        ACCEPTED,
        BLOCKED,
        DECLINED
    }
}
