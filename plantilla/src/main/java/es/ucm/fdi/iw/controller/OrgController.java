package es.ucm.fdi.iw.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import es.ucm.fdi.iw.repository.EventRepository;
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
    private EventRepository eventRepository;

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

    @GetMapping("/create-event")
    public String showCreateEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "createEvent";
    }
    

}
