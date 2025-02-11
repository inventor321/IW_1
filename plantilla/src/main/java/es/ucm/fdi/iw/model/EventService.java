package es.ucm.fdi.iw.model;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class EventService {
    
    @Autowired
    private EventRepository eventRepository;

    public List<Event> findAll() {
        return eventRepository.findAll();
    }
    
    public Event save(Event event) {
        return eventRepository.save(event);
    }
}
