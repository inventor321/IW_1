package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.model.Event;
import es.ucm.fdi.iw.repository.EventRepository;
import es.ucm.fdi.iw.model.User;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/events")
public class EventController {

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws" }) {
            model.addAttribute(name, session.getAttribute(name));
        }
    }

    @Autowired
    private EventRepository eventRepository;

    @GetMapping
    public String listEvents(Model model) {
        List<Event> events = eventRepository.findAll();
        model.addAttribute("events", events);
        return "events";
    }

    @PostMapping
    public String createEvent(@RequestParam String name,
                         @RequestParam String description,
                         @RequestParam LocalDateTime date,
                         @RequestParam String location,
                         @RequestParam String imageSource,
                         @RequestParam(required = false) String imageUrl,
                         @RequestParam(required = false) MultipartFile imageFile,
                         HttpSession session) {
        try {
            String finalImagePath = null;
            User u = (User)session.getAttribute("u");
            long id = u.getId();
            
            if ("file".equals(imageSource) && imageFile != null && !imageFile.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                Path staticPath = Paths.get("src/main/resources/static/img/events");
                Files.createDirectories(staticPath);
                Path filepath = staticPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);
                finalImagePath = "/img/events/" + fileName;
            } else if ("url".equals(imageSource) && imageUrl != null && !imageUrl.isEmpty()) {
                finalImagePath = imageUrl;
            }

            Event event = new Event(name, description, date, location, finalImagePath, id);
            eventRepository.save(event);
            return "redirect:/events";
        } catch (Exception e) {
            return "redirect:/events/create?error=" + e.getMessage();
        }
    }

    @GetMapping("/{id}")
    public String getEvent(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        
        model.addAttribute("event", event);
        return "event";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ORG', 'ADMIN')")
    public String deleteEvent(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }

            Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

            User user = (User) authentication.getPrincipal();
            if (user.hasRole(User.Role.ADMIN) || event.getOrg().equals(user.getId())) {
                eventRepository.delete(event);
                ra.addFlashAttribute("message", "Event deleted successfully");
            } else {
                ra.addFlashAttribute("error", "You don't have permission to delete this event");
            }

            return "redirect:/events";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting event: " + e.getMessage());
            return "redirect:/events";
        }
    }
}
