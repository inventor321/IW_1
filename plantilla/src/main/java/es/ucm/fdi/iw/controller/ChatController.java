package es.ucm.fdi.iw.controller;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import es.ucm.fdi.iw.model.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private EntityManager entityManager;

    @PostMapping("/event/{eventId}")
    @Transactional
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendEventMessage(
            @PathVariable Long eventId,
            @RequestBody Map<String, String> messageData,
            @AuthenticationPrincipal User user) {

        System.out.println("Recibido mensaje para evento: " + eventId);
        System.out.println("Contenido: " + messageData.get("content"));
        System.out.println("Usuario: " + user.getUsername());

        Event event = entityManager.find(Event.class, eventId);
        if (event == null) {
            System.out.println("Evento no encontrado: " + eventId);
            return ResponseEntity.notFound().build();
        }

        Message m = new Message();
        m.setSender(user);
        m.setEvent(event);
        m.setText(messageData.get("content"));
        m.setDateSent(LocalDateTime.now());
        m.setType(Message.MessageType.EVENT);

        try {
            entityManager.persist(m);
            entityManager.flush();
            System.out.println("Mensaje guardado con ID: " + m.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("from", user.getUsername());
            response.put("text", m.getText());
            response.put("sent", m.getDateSent().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error al guardar el mensaje: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/event/{eventId}/messages")
    @ResponseBody
    public List<Message.Transfer> getEventMessages(@PathVariable Long eventId) {
        return entityManager
                .createQuery(
                    "SELECT m FROM Message m WHERE m.event.id = :eventId ORDER BY m.dateSent",
                    Message.class)
                .setParameter("eventId", eventId)
                .getResultList()
                .stream()
                .map(Message::toTransfer)
                .toList();
    }

    @GetMapping("/event/{eventId}/messages/unread")
    @ResponseBody
    public Long getUnreadMessagesCount(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User user) {
        return entityManager
                .createQuery(
                    "SELECT COUNT(m) FROM Message m WHERE m.event.id = :eventId AND m.dateRead IS NULL AND m.sender.id != :userId",
                    Long.class)
                .setParameter("eventId", eventId)
                .setParameter("userId", user.getId())
                .getSingleResult();
    }

    @PostMapping("/event/{eventId}/messages/read")
    @Transactional
    @ResponseBody
    public void markMessagesAsRead(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User user) {
        entityManager
                .createQuery(
                    "UPDATE Message m SET m.dateRead = :now WHERE m.event.id = :eventId AND m.sender.id != :userId AND m.dateRead IS NULL")
                .setParameter("now", LocalDateTime.now())
                .setParameter("eventId", eventId)
                .setParameter("userId", user.getId())
                .executeUpdate();
    }

    @PostMapping("/event/{eventId}/message/{messageId}/delete")
    @Transactional
    @ResponseBody
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long eventId,
            @PathVariable Long messageId,
            @AuthenticationPrincipal User user) {

        Message message = entityManager.find(Message.class, messageId);
        
        if (message == null) {
            return ResponseEntity.notFound().build();
        }

        entityManager.remove(message);
        return ResponseEntity.ok().build();
    }
}