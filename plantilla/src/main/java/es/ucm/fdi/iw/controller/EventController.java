package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.model.Event;
import es.ucm.fdi.iw.repository.EventRepository;
import es.ucm.fdi.iw.model.User;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}
