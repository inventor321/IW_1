package es.ucm.fdi.iw.model;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import es.ucm.fdi.iw.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @PostConstruct
    public void init() {
        // Add sample data if repository is empty
        if (eventRepository.count() == 0) {
            Event event1 = new Event("Concurso de tortillas de patatas",
                    "Descripción del evento 1",
                    LocalDateTime.of(2025, 2, 21, 12, 0),
                    "Lugar 1",
                    "/img/event1.jpg", 1);
            eventRepository.save(event1);

            Event event2 = new Event("Concurso de tortillas de patatas 2",
                    "Descripción del evento 2",
                    LocalDateTime.of(2025, 2, 28, 12, 0),
                    "Lugar 2",
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f2/Tortilla_de_patatas.jpg/640px-Tortilla_de_patatas.jpg",
                    1);
            eventRepository.save(event2);
        }
    }

    public List<Event> findAll() {
        return eventRepository.findAllByOrderByDateAsc();
    }

    public Event save(Event event) {
        return eventRepository.save(event);
    }
}
