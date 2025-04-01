package es.ucm.fdi.iw.controller;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import es.ucm.fdi.iw.model.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping("/event/{eventId}")
    @Transactional
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendEventMessage(
            @PathVariable Long eventId,
            @RequestBody Map<String, String> messageData,
            HttpSession session) {

        User user = (User) session.getAttribute("u");

        System.out.println("Debug: Received message for event " + eventId);
        System.out.println("Debug: Message content: " + messageData.get("content"));
        System.out.println("Debug: User: " + user.getUsername());

        try {
            Event event = entityManager.find(Event.class, eventId);
            if (event == null) {
                System.out.println("Debug: Event not found: " + eventId);
                return ResponseEntity.notFound().build();
            }

            Message message = new Message();
            message.setSender(user);
            message.setEvent(event);
            message.setText(messageData.get("content"));
            message.setDateSent(LocalDateTime.now());
            message.setType(Message.MessageType.EVENT);

            entityManager.persist(message);
            entityManager.flush();

            System.out.println("Debug: Message saved successfully with ID: " + message.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("from", user.getUsername());
            response.put("text", message.getText());
            response.put("dateSent", message.getDateSent().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error saving message: ");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

   
    @PostMapping("/events/{eventId}")
    @Transactional
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendEventMessage(
            @PathVariable Long eventId,
            @RequestBody Map<String, String> messageData,
            @AuthenticationPrincipal User user) {

        try {
            Event event = entityManager.find(Event.class, eventId);
            if (event == null) {
                return ResponseEntity.notFound().build();
            }

            Message message = new Message();
            message.setSender(user);
            message.setEvent(event);
            message.setText(messageData.get("content"));
            message.setDateSent(LocalDateTime.now());
            message.setType(Message.MessageType.EVENT);

            entityManager.persist(message);
            entityManager.flush();

            Map<String, Object> response = new HashMap<>();
            response.put("from", user.getUsername());
            response.put("text", message.getText());
            response.put("dateSent", message.getDateSent().toString());

            // Broadcast the message to all subscribers
            simpMessagingTemplate.convertAndSend(
                "/topic/events/" + eventId,
                response
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
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

    @MessageMapping("/test")
    @SendTo("/topic/test")
    public Map<String, Object> handleTestMessage(String message) {
        System.out.println("Received test message: " + message);
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("message", message);
            response.put("status", "received");
            
            System.out.println("Sending response: " + response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    @MessageMapping("/chat/{eventId}")
    @SendTo("/topic/chat/{eventId}")
    public Map<String, Object> handleChatMessage(@DestinationVariable Long eventId, 
                                               @Payload Map<String, String> messageData,
                                               @AuthenticationPrincipal User user) {
        System.out.println("Received chat message for event " + eventId);
        
        try {
            Event event = entityManager.find(Event.class, eventId);
            if (event == null) {
                throw new RuntimeException("Event not found");
            }

            Message message = new Message();
            message.setSender(user);
            message.setEvent(event);
            message.setText(messageData.get("content"));
            message.setDateSent(LocalDateTime.now());
            message.setType(Message.MessageType.EVENT);

            entityManager.persist(message);
            entityManager.flush();

            Map<String, Object> response = new HashMap<>();
            response.put("from", user.getUsername());
            response.put("text", message.getText());
            response.put("dateSent", message.getDateSent().toString());
            
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process message: " + e.getMessage());
            return errorResponse;
        }
    }
}