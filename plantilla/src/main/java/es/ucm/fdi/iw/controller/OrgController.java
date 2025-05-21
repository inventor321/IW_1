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

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws" }) {
            model.addAttribute(name, session.getAttribute(name));
        }
    }

    private static final Logger log = LogManager.getLogger(OrgController.class);

    @GetMapping("/")
    public String index(Model model) {
        log.info("Org acaba de entrar");

        // Obtener todos los eventos activos
        List<Event> events = eventRepository.findAll();

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
