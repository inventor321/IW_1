package es.ucm.fdi.iw.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import es.ucm.fdi.iw.model.EventService;
import es.ucm.fdi.iw.model.Event;

import es.ucm.fdi.iw.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 *  Site administration.
 *
 *  Access to this end-point is authenticated - see SecurityConfig
 */
@Controller
@RequestMapping("admin")
public class AdminController {

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {        
        for (String name : new String[] {"u", "url", "ws"}) {
            model.addAttribute(name, session.getAttribute(name));
        }
    }

    private static final Logger log = LogManager.getLogger(AdminController.class);

	@GetMapping("/")
    public String index(Model model) {
        log.info("Admin acaba de entrar");
        return "admin";
    }

    @Autowired
    private EventService eventService;
    
    @GetMapping("/events")
    public String listEvents(Model model) {
        // model.addAttribute("events", eventService.findAll());
        return "events";
    }
    
    @GetMapping("/events/create")
    public String showCreateEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "createEvent";
    }
    
    @PostMapping("/events")
    public String createEvent(@ModelAttribute Event event) {
        eventService.save(event);
        return "redirect:/admin/events";
    }

    // @GetMapping("/event")
    // public String event(Model model) {
    //     return "event";
    // }

    @GetMapping("/chat")
    public String chat(Model model) {
        return "chat";
    }
}
