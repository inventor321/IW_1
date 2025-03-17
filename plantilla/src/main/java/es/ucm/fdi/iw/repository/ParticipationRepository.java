package es.ucm.fdi.iw.repository;

import es.ucm.fdi.iw.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import es.ucm.fdi.iw.model.Participation;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findAllByOrderByIdAsc();
}
