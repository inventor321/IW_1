package es.ucm.fdi.iw.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.iw.model.Event;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.repository.EventRepository;
import es.ucm.fdi.iw.repository.MessageRepository;
import es.ucm.fdi.iw.repository.ParticipationRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Site administration.
 *
 * Access to this end-point is authenticated - see SecurityConfig
 */
@Controller
@RequestMapping("/org")
public class OrgController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws" }) {
            model.addAttribute(name, session.getAttribute(name));
        }

        User currentUser = (User) session.getAttribute("u");

        if (currentUser != null) {
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

        }
    }

    private static final Logger log = LogManager.getLogger(OrgController.class);

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        log.info("Org acaba de entrar");
        User currentUser = (User) session.getAttribute("u");
        model.addAttribute("u", currentUser);

        // Obtener todos los eventos activos
        List<Event> events = eventRepository.findAll();

        if (currentUser != null) {
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

        }

        // Añadir la cantidad de participantes a cada evento
        Map<Long, Long> participantCounts = new HashMap<>();
        for (Event event : events) {
            long count = participationRepository.countByEventId(event.getId());
            participantCounts.put(event.getId(), count);
            log.info("Event ID: {}, Participant Count: {}", event.getId(), count);
        }

        model.addAttribute("events", events);
        model.addAttribute("participantCounts", participantCounts);
        return "org";
    }

}
