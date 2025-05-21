package es.ucm.fdi.iw.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import es.ucm.fdi.iw.model.Event;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.repository.EventRepository;
import es.ucm.fdi.iw.repository.ParticipationRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

/**
 * Site administration.
 *
 * Access to this end-point is authenticated - see SecurityConfig
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private ParticipationRepository participationRepository;

    private static final Logger log = LogManager.getLogger(AdminController.class);

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws" }) {
            model.addAttribute(name, session.getAttribute(name));
        }
    }

    @GetMapping("/")  // Changed from "" to "/"
    public String index(Model model) {
        log.info("Admin acaba de entrar");
        try {
            // Obtener todos los eventos activos
            List<Event> events = eventRepository.findAll();

            // Añadir la cantidad de participantes a cada evento
            Map<Long, Long> participantCounts = new HashMap<>();
            for (Event event : events) {
                long count = participationRepository.countByEventId(event.getId());
                participantCounts.put(event.getId(), count);
                log.info("Event ID: {}, Participant Count: {}", event.getId(), count);
            }

            List<User> users = entityManager
                .createQuery("SELECT u FROM User u", User.class)
                .getResultList();
            model.addAttribute("users", users);
            model.addAttribute("events", events);
            model.addAttribute("participantCounts", participantCounts);

            return "admin"; // This should match exactly with the template name (without .html)
        } catch (Exception e) {
            log.error("Error loading users: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/toggle/{id}")
    @Transactional
    @ResponseBody
    public ResponseEntity<String> toggleUser(@PathVariable long id, Model model) {
        try {
            log.info("Admin cambia estado de " + id);
            User target = entityManager.find(User.class, id);
            if (target == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"Usuario no encontrado\"}");
            }
            target.setEnabled(!target.isEnabled());
            return ResponseEntity.ok("{\"enabled\":" + target.isEnabled() + "}");
        } catch (Exception e) {
            log.error("Error toggling user: " + e.getMessage());
            return ResponseEntity.internalServerError().body("{\"error\": \"Error al cambiar estado del usuario\"}");
        }
    }

}
