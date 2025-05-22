package es.ucm.fdi.iw.repository;

import es.ucm.fdi.iw.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale.Category;
import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAll();
    List<Event> findByActiveTrue(Sort sort);
    List<Event> findAllByActiveTrueOrderByDateAsc();
    List<Event> findByActiveTrueAndCategory(Event.Category category);
    List<Event> findByNameContaining(String name);
    List<Event> findByNameContainingIgnoreCase(String name);
    List<Event> findByLocationContainingIgnoreCase(String query);
    List<Event> findByActiveTrueAndNameContainingIgnoreCase(String name);
    
    @Query("SELECT e FROM Event e WHERE e.org = :orgId")
    List<Event> findAllByOrg(@Param("orgId") long orgId);
    
    @Query("SELECT e FROM Event e WHERE e.active = true AND e.org = :orgId")
    List<Event> findActiveByOrg(@Param("orgId") long orgId, Sort sort);
    
    @Modifying
    @Query("UPDATE Event e SET e.active = false WHERE e.id = :id")
    void disableEventById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Event e SET e.active = true WHERE e.id = :id")
    void enableEventById(@Param("id") Long id);

    @Query("SELECT e FROM Event e WHERE " +
           "e.active = true AND " + 
           "(LOWER(e.name) LIKE %:query% OR " +
           "LOWER(e.description) LIKE %:query% OR " +
           "LOWER(e.location) LIKE %:query% OR :query IS NULL) AND " + // Filtro por palabra clave (opcional)
           "(:category IS NULL OR e.category = :category) AND " +    // Filtro por categoría (opcional)
           "(:startDate IS NULL OR e.date >= :startDate) AND " +      // Filtro por fecha inicio (opcional)
           "(:endDate IS NULL OR e.date <= :endDate) " +             // Filtro por fecha fin (opcional)
           "ORDER BY " +
           "CASE WHEN :sortOption = 'date-asc' THEN e.date END ASC, " +
           "CASE WHEN :sortOption = 'date-desc' THEN e.date END DESC, " +
           "CASE WHEN :sortOption = 'name-asc' THEN e.name END ASC, " +
           "CASE WHEN :sortOption = 'name-desc' THEN e.name END DESC, " +
           "e.date ASC")
    List<Event> findFilteredEvents(
            @Param("query") String query,
            @Param("category") Event.Category category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("sortOption") String sortOption);
}