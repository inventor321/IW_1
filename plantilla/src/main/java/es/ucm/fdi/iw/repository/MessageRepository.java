package es.ucm.fdi.iw.repository;

import es.ucm.fdi.iw.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Obtener los mensajes entre dos usuarios (emisor y receptor)
    @Query("SELECT m FROM Message m WHERE (m.sender.username = :user1 AND m.recipient.username = :user2) OR (m.sender.username = :user2 AND m.recipient.username = :user1) ORDER BY m.dateSent ASC")
    List<Message> findChatMessages(@Param("user1") String user1, @Param("user2") String user2);

    // Obtener los mensajes de un grupo específico
    @Query("SELECT m FROM Message m WHERE m.usergroup = :groupId ORDER BY m.dateSent ASC")
    List<Message> findGroupMessages(@Param("groupId") long groupId);

    // Contar mensajes no leídos de un usuario
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipient.id = :userId AND m.dateRead IS NULL")
    long countUnreadMessages(@Param("userId") long userId);
}
