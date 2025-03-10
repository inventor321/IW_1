package es.ucm.fdi.iw.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.iw.model.EventService;
import es.ucm.fdi.iw.model.Event;

import es.ucm.fdi.iw.model.User;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

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
    @Transactional
    public String createEvent(@ModelAttribute Event event,
                            @RequestParam(required = false) MultipartFile imageFile,
                            HttpSession session,
                            RedirectAttributes ra) {
        try {
            User u = (User)session.getAttribute("u");
            event.setOrg(u.getId());
            
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                Path staticPath = Paths.get("src/main/resources/static/img/events");
                Files.createDirectories(staticPath);
                Path filepath = staticPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);
                event.setImageUrl("/img/events/" + fileName);
            }
            // print every part of the event
            ra.addFlashAttribute("message", "Event: " + event.getName() + " " + event.getDescription());
            log.info("Event: " + event.getName() + " " + event.getDescription() + " " + event.getDate() + " " + event.getOrg() + " " + event.getImageUrl());

            eventService.save(event);
            ra.addFlashAttribute("message", "Event created successfully!");
            return "redirect:/events";
        } catch (Exception e) {
            log.error("Error creating event", e);
            ra.addFlashAttribute("error", "Failed to create event: " + e.getMessage());
            return "redirect:/org/create-event";
        }
    }

    @GetMapping("/event/{id}")
    public String event(@PathVariable long id, Model model) {
        Event event = entityManager.find(Event.class, id);
        model.addAttribute("event", event);
        return "event";
    }

}
