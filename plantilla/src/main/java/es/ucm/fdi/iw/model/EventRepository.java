package es.ucm.fdi.iw.model;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}
