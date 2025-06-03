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
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpStatus;

import es.ucm.fdi.iw.model.*;
import es.ucm.fdi.iw.repository.MessageRepository;
import es.ucm.fdi.iw.repository.UserRepository;
import es.ucm.fdi.iw.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private EventRepository eventRepository; // Asegúrate de tener este repositorio

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws" }) {
            model.addAttribute(name, session.getAttribute(name));
        }
    }

    @GetMapping("/{partnerId}")
    @Transactional
    public String chat(@PathVariable Long partnerId, Model model, HttpSession session, RedirectAttributes ra) {

        User currentUser = (User) session.getAttribute("u");
        User chatPartner = userRepository.findById(partnerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Actualizar dateRead de los mensajes recibidos por el usuario actual
        List<Message> unreadMessages = messageRepository.findUnreadMessages(currentUser.getId(), partnerId);
        unreadMessages.forEach(message -> message.setDateRead(LocalDateTime.now()));
        messageRepository.saveAll(unreadMessages);

        // Obtener todos los mensajes entre los dos usuarios
        List<Message> messages = messageRepository.findChatMessages(currentUser.getId(), partnerId);

        // Pasar datos al modelo
        model.addAttribute("chatPartnerName", chatPartner.getUsername());
        model.addAttribute("chatPartnerId", chatPartner.getId());
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("messages", messages);

        return "chat";
    }

    @GetMapping("/conversations")
    public String conversations(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("u");
        if (currentUser == null) {
            // Redirige al login si no hay usuario en sesión
            return "redirect:/login";
        }

        // Obtener todas las conversaciones con el conteo de mensajes no leídos
        List<Object[]> conversations = messageRepository.findAllConversationsWithUnreadCount(currentUser.getId());

        // Ordenar: primero las que tienen mensajes sin leer, luego las demás
        conversations.sort((a, b) -> {
            Long unreadA = (Long) a[1];
            Long unreadB = (Long) b[1];
            if ((unreadA > 0 && unreadB > 0) || (unreadA == 0 && unreadB == 0)) {
                return 0;
            }
            return unreadA > 0 ? -1 : 1;
        });

        int unreadCount = conversations.stream()
                .mapToInt(conversation -> ((Long) conversation[1]).intValue())
                .sum();

        model.addAttribute("conversations", conversations);
        model.addAttribute("unreadCount", unreadCount);
        return "conversations";
    }

    @PostMapping("/send")
    @Transactional
    public String sendMessage(@RequestParam Long receiverId, @RequestParam String message, HttpSession session) {
        User sender = (User) session.getAttribute("u");
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Crear y guardar el mensaje
        Message newMessage = new Message();
        newMessage.setSender(sender);
        newMessage.setRecipient(receiver);
        newMessage.setText(message);
        newMessage.setDateSent(LocalDateTime.now());
        messageRepository.save(newMessage);

        return "redirect:/chat/" + receiverId;
    }

    @PostMapping("/group/{groupId}/send")
    public String sendGroupMessage(@PathVariable long groupId,
            @RequestParam String text,
            HttpSession session) {
        User sender = (User) session.getAttribute("u");
        if (sender == null) {
            return "redirect:/login";
        }

        Event event = eventRepository.findById(groupId).orElse(null);
        if (event == null) {
            // Maneja el error como prefieras
            return "redirect:/events";
        }

        Message message = new Message();
        message.setSender(sender);
        message.setUsergroup(groupId);
        message.setText(text);
        message.setDateSent(LocalDateTime.now());
        message.setEvent(event); // <-- ¡IMPORTANTE!

        messageRepository.save(message);

        return "redirect:/events/" + groupId;
    }

}