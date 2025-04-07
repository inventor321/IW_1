package es.ucm.fdi.iw.model;

import java.sql.Timestamp;
import java.time.Instant;

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

    @PrePersist
    protected void onCreate() {
        createdAt = Timestamp.from(Instant.now());
        status = FriendshipStatus.PENDING;
    }

    public void accept() {
        this.status = FriendshipStatus.ACCEPTED;
        this.isAccepted = true;
        this.acceptedAt = Timestamp.from(Instant.now());
    }

    public void decline() {
        this.status = FriendshipStatus.DECLINED;
        this.isAccepted = false;
    }

    public void block() {
        this.status = FriendshipStatus.BLOCKED;
        this.isAccepted = false;
    }

    public boolean isPending() {
        return this.status == FriendshipStatus.PENDING;
    }

    public boolean isAccepted() {
        return this.status == FriendshipStatus.ACCEPTED;
    }

    public boolean isBlocked() {
        return this.status == FriendshipStatus.BLOCKED;
    }

    public boolean isDeclined() {
        return this.status == FriendshipStatus.DECLINED;
    }

    // Helper method to create a friendship request
    public static Friendship createRequest(User from, User to) {
        Friendship friendship = new Friendship();
        friendship.setUser1(from);
        friendship.setUser2(to);
        return friendship;
    }
}
