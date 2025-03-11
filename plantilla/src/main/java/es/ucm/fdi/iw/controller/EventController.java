package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.model.Event;
import es.ucm.fdi.iw.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/events")
public class EventController {

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
            @RequestParam(required = false) String imageUrl,
            Authentication authentication) {

        // Crear un evento con los valores proporcionados, incluyendo el nombre del
        // organizador (org)
        Event event = new Event(name, description, date, location, imageUrl);
        eventRepository.save(event); // Guardar el evento en la base de datos

        return "redirect:/events"; // Redirigir a la lista de eventos
    }
}
