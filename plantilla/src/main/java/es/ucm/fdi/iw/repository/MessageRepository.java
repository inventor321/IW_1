package es.ucm.fdi.iw.repository;

import es.ucm.fdi.iw.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Obtener los mensajes no leídos entre dos usuarios (emisor y receptor)
    @Query("SELECT m FROM Message m WHERE m.recipient.id = :currentUserId AND m.sender.id = :partnerId AND m.dateRead IS NULL")
    List<Message> findUnreadMessages(@Param("currentUserId") Long currentUserId, @Param("partnerId") Long partnerId);

    // Obtener los mensajes entre dos usuarios (emisor y receptor)
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :userId1 AND m.recipient.id = :userId2) OR (m.sender.id = :userId2 AND m.recipient.id = :userId1) ORDER BY m.dateSent ASC")
    List<Message> findChatMessages(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // Obtener los mensajes de un grupo específico
    @Query("SELECT m FROM Message m WHERE m.usergroup = :groupId ORDER BY m.dateSent ASC")
    List<Message> findGroupMessages(@Param("groupId") long groupId);

    // Contar mensajes no leídos de un usuario
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipient.id = :userId AND m.dateRead IS NULL")
    long countUnreadMessages(@Param("userId") long userId);

    @Query("SELECT m.sender, COUNT(m) " +
           "FROM Message m " +
           "WHERE m.recipient.id = :currentUserId AND m.dateRead IS NULL " +
           "GROUP BY m.sender " +
           "ORDER BY COUNT(m) DESC")
    List<Object[]> findConversationsWithUnreadCount(@Param("currentUserId") Long currentUserId);

    @Query("""
        SELECT u, 
               SUM(CASE WHEN m.recipient.id = :userId AND m.dateRead IS NULL THEN 1 ELSE 0 END) as unreadCount
        FROM Message m
        JOIN User u ON (u.id = m.sender.id AND m.sender.id <> :userId) OR (u.id = m.recipient.id AND m.recipient.id <> :userId)
        WHERE m.sender.id = :userId OR m.recipient.id = :userId
        GROUP BY u
    """)
    List<Object[]> findAllConversationsWithUnreadCount(@Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE m.event.id = :eventId ORDER BY m.dateSent ASC")
    List<Message> findEventMessages(@Param("eventId") Long eventId);
}
