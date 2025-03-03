package es.ucm.fdi.iw.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.iw.model.EventService;
import es.ucm.fdi.iw.model.Event;

import es.ucm.fdi.iw.model.User;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Site administration.
 *
 * Access to this end-point is authenticated - see SecurityConfig
 */
@Controller
@RequestMapping("org")
public class OrgController {

    @Autowired
    private EntityManager entityManager;

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
        return "org";
    }

    @Autowired
    private EventService eventService;

    @GetMapping("/create-event")
    public String showCreateEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "createEvent";
    }

    @PostMapping("/events")
    public String createEvent(@ModelAttribute Event event, RedirectAttributes ra) {
        try {
            eventService.save(event);
            ra.addFlashAttribute("message", "Event created successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to create event: " + e.getMessage());
        }
        return "redirect:/events";
    }

    @GetMapping("/event/{id}")
    public String event(@PathVariable long id, Model model) {
        Event event = entityManager.find(Event.class, id);
        model.addAttribute("event", event);
        return "event";
    }

}
