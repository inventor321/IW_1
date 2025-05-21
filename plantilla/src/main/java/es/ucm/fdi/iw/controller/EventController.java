package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.Event;
import es.ucm.fdi.iw.model.Event.Category;
import es.ucm.fdi.iw.repository.EventRepository;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Participation;
import es.ucm.fdi.iw.model.Message;
import es.ucm.fdi.iw.repository.MessageRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.time.LocalDate;
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
    private LocalData localData;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private EntityManager entityManager;

    @GetMapping
    public String listEvents(Model model,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Event.Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "date-asc") String sort) {

        List<Event> events;
        LocalDateTime start = null, end = null;
        if (startDate != null){
            start = startDate.atStartOfDay();
        }
        if (endDate != null){
            end = endDate.atTime(23, 59, 59);
        }

        events = eventRepository.findFilteredEvents(
            query,
            category,
            start,
            end,
            sort
        );

        model.addAttribute("events", events);
        return "events"; // Renderiza la pagina de eventos
    }

    @GetMapping("/create")
    public String createEventForm(Model model) {
        if (!model.containsAttribute("event")) {
            model.addAttribute("event", new Event());
        }
        return "create-event";
    }

    @PostMapping("/created")
    @PreAuthorize("hasAnyRole('ORG', 'ADMIN')")
    public String created(@RequestParam String name,
            @RequestParam String description,
            @RequestParam LocalDateTime date,
            @RequestParam String location,
            @RequestParam String imageSource,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam String category,
            HttpSession session,
            RedirectAttributes ra) {
        try {
            User u = (User) session.getAttribute("u");

            Category cat = Category.valueOf(category);

            Event event = new Event(name, description, date, location, null, u.getId(), cat);
            eventRepository.save(event);

            if ("file".equals(imageSource) && imageFile != null && !imageFile.isEmpty()) {
                File f = localData.getFile("event", event.getId() + ".jpg");
                try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
                    stream.write(imageFile.getBytes());
                    event.setImage("/event/" + event.getId() + ".jpg");
                }
            } else if ("url".equals(imageSource) && imageUrl != null && !imageUrl.isEmpty()) {
                event.setImage(imageUrl);
            }

            // Update event with image URL
            eventRepository.save(event);
            ra.addFlashAttribute("message", "Event created successfully!");
            return "redirect:/events/" + event.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error creating event: " + e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/{id}")
    public String getEvent(@PathVariable Long id, Model model, Authentication authentication, HttpSession session) {
        if (authentication == null || !authentication.isAuthenticated() || session.getAttribute("u") == null) {
            return "redirect:/login";
        }
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        User u = (User) session.getAttribute("u");
        boolean isParticipating = entityManager
                .createQuery(
                        "SELECT COUNT(p) FROM Participation p WHERE p.user = :user AND p.event = :event AND p.enabled = true",
                        Long.class)
                .setParameter("user", u)
                .setParameter("event", event)
                .getSingleResult() > 0;

        List<User> participants = entityManager
                .createQuery("SELECT p.user FROM Participation p WHERE p.event = :event AND p.enabled = true",
                        User.class)
                .setParameter("event", event).getResultList();

        // Cargar mensajes del chat de grupo
        List<Message> eventMessages = messageRepository.findEventMessages(id);

        model.addAttribute("event", event);
        model.addAttribute("isParticipating", isParticipating);
        model.addAttribute("participants", participants);
        model.addAttribute("eventMessages", eventMessages); // <-- Añade esto

        return "event";
    }

    @PostMapping("/participate/{id}")
    @Transactional
    public String participateInEvent(@PathVariable Long id, Model model, HttpSession session) {

        User u = (User) session.getAttribute("u");
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        try {
            Participation p = entityManager.createQuery(
                    "SELECT p FROM Participation p WHERE p.user = :user AND p.event = :event",
                    Participation.class)
                    .setParameter("user", u)
                    .setParameter("event", event)
                    .getSingleResult();

            p.setEnabled(true);
            p.setTimestamp(new Timestamp(System.currentTimeMillis()));

            entityManager.merge(p);
            entityManager.flush();

        } catch (jakarta.persistence.NoResultException e) {
            Participation p = new Participation(u, event, new Timestamp(System.currentTimeMillis()), true);

            entityManager.persist(p);
            entityManager.flush();
        }

        return "redirect:/events/" + id;
    }

    @PostMapping("/withdraw/{id}")
    @Transactional
    public String withdrawFromEvent(@PathVariable Long id, Model model, HttpSession session) {
        User u = (User) session.getAttribute("u");
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        try {
            Participation p = entityManager.createQuery(
                    "SELECT p FROM Participation p WHERE p.user = :user AND p.event = :event AND p.enabled = true",
                    Participation.class)
                    .setParameter("user", u)
                    .setParameter("event", event)
                    .getSingleResult();

            p.setEnabled(false);
            p.setTimestamp(new Timestamp(System.currentTimeMillis()));

            entityManager.merge(p);
            entityManager.flush();
        } catch (jakarta.persistence.NoResultException e) {
            return "redirect:/events/" + id;
        }

        return "redirect:/events/" + id;
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('ORG', 'ADMIN')")
    @Transactional
    public String disableEvent(@PathVariable Long id, RedirectAttributes ra, HttpSession session,
                               @RequestHeader(value = "referer", required = false) String referer) {
        try {
            User user = (User) session.getAttribute("u");
            Event event = eventRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

            if (user.hasRole(User.Role.ADMIN) || event.getOrg() == null || event.getOrg().equals(user.getId())) {
                eventRepository.disableEventById(id);
                ra.addFlashAttribute("message", "Event disabled successfully");
            } else {
                ra.addFlashAttribute("error", "You don't have permission to disable this event");
            }
            // Redirige a la página anterior si existe, si no a "/"
            return "redirect:" + (
                referer != null && referer.contains("/events/") ? "/events" :
                referer != null ? referer : "/"
            );
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error disabling event: " + e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasAnyRole('ORG', 'ADMIN')")
    @Transactional
    public String enableEvent(@PathVariable Long id, RedirectAttributes ra, HttpSession session,
                              @RequestHeader(value = "referer", required = false) String referer) {
        try {
            User user = (User) session.getAttribute("u");
            Event event = eventRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

            if (user.hasRole(User.Role.ADMIN) || event.getOrg() == null || event.getOrg().equals(user.getId())) {
                event.setActive(true);
                eventRepository.save(event);
                ra.addFlashAttribute("message", "Event enabled successfully");
            } else {
                ra.addFlashAttribute("error", "You don't have permission to enable this event");
            }
            // Redirige a la página anterior si existe, si no a "/events"
            return "redirect:" + (referer != null ? referer : "/events");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error enabling event: " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ORG', 'ADMIN')")
    public String editEvent(Model model, @PathVariable Long id, HttpSession session, RedirectAttributes ra) {

        User u = (User) session.getAttribute("u");
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        
        if (u.hasRole(User.Role.ORG) && !event.getOrg().equals(u.getId())) {
            ra.addFlashAttribute("error", "You don't have permission to edit this event");
            return "redirect:/events/" + id;
        }
        
        model.addAttribute("event", event);
        return "create-event";
    }

    @PostMapping("/{id}/update")
    @PreAuthorize("hasAnyRole('ORG', 'ADMIN')")
    @Transactional
    public String updateEvent(@PathVariable Long id,
                            @RequestParam String name,
                            @RequestParam String description,
                            @RequestParam LocalDateTime date,
                            @RequestParam String location,
                            @RequestParam String imageSource,
                            @RequestParam String category,
                            @RequestParam(required = false) String imageUrl,
                            @RequestParam(required = false) MultipartFile imageFile,
                            HttpSession session,
                            RedirectAttributes ra) {
        try {
            User u = (User) session.getAttribute("u");
            Event event = eventRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

            // Verify permissions
            if (u.hasRole(User.Role.ORG) && !event.getOrg().equals(u.getId())) {
                ra.addFlashAttribute("error", "You don't have permission to edit this event");
                return "redirect:/events/" + id;
            }

            // Update event details
            event.setName(name);
            event.setDescription(description);
            event.setDate(date);
            event.setLocation(location);
            event.setCategory(Category.valueOf(category));

            // Handle image update
            if ("file".equals(imageSource) && imageFile != null && !imageFile.isEmpty()) {
                File f = localData.getFile("event", event.getId() + ".jpg");
                try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
                    stream.write(imageFile.getBytes());
                    event.setImage("/event/" + event.getId() + ".jpg");
                }
            } else if ("url".equals(imageSource) && imageUrl != null && !imageUrl.isEmpty()) {
                event.setImage(imageUrl);
            }

            eventRepository.save(event);
            ra.addFlashAttribute("message", "Event updated successfully!");
            return "redirect:/events/" + event.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error updating event: " + e.getMessage());
            return "redirect:/events/" + id;
        }
    }


}
