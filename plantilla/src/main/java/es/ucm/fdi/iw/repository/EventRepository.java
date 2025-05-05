package es.ucm.fdi.iw.repository;

import es.ucm.fdi.iw.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // Ordena todos los eventos por fecha en orden ascendente
    List<Event> findAllByOrderByDateAsc();
    List<Event> findByCategory(Event.Category category);
    List<Event> findByDate(LocalDateTime date);
    List<Event> findByNameContaining(String name);
    List<Event> findAllByActiveTrueOrderByDateAsc(); // Busca solo eventos habilitados y los ordena por fecha
    /*List<Event> findByNameContainingIgnoreCase(String query);
    List<Event> findByLocationContainingIgnoreCase(String query);
    List<Event> findByCategoryContainingIgnoreCase(String query);
    */

    @Modifying
    @Query("UPDATE Event e SET e.active = false WHERE e.id = :id")
    void disableEventById(@Param("id") Long id);

    @Query("SELECT e FROM Event e WHERE e.org = :orgId")
    List<Event> findAllByOrg(@Param("orgId") long orgId);
}