package es.ucm.fdi.iw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import es.ucm.fdi.iw.model.Participation;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findAllByOrderByIdAsc();

    @Query("SELECT COUNT(p) FROM Participation p WHERE p.event.id = :eventId AND p.enabled = true")
    long countByEventId(@Param("eventId") Long eventId);
}
