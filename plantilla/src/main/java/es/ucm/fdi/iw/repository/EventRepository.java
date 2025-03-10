package es.ucm.fdi.iw.repository;

import es.ucm.fdi.iw.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // Ordena todos los eventos por fecha en orden ascendente
    List<Event> findAllByOrderByDateAsc();
}
