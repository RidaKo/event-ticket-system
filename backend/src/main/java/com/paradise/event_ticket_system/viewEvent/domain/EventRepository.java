package com.paradise.event_ticket_system.viewEvent.domain;

import com.paradise.event_ticket_system.model.Event;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Integer> {

    @Query("""
        SELECT e FROM Event e
        JOIN FETCH e.venue
        JOIN FETCH e.organizer
        JOIN FETCH e.category
        WHERE e.id = :id
    """)
    Optional<Event> findByIdWithDetails(Integer id);

    @Query("""
        SELECT e FROM Event e
        JOIN FETCH e.venue
        JOIN FETCH e.organizer
        JOIN FETCH e.category
    """)
    List<Event> findAllWithDetails();

    @Query(
        value = """
            SELECT e FROM Event e
            JOIN FETCH e.venue
            JOIN FETCH e.organizer
            JOIN FETCH e.category
            """,
        countQuery = "SELECT count(e) FROM Event e"
    )
    Page<Event> findAllWithDetails(Pageable pageable);

    List<Event> findByOrganizerId(Integer organizerId);

    @Query("""
           SELECT DISTINCT e
           FROM Event e
           LEFT JOIN FETCH e.tags
           JOIN FETCH e.venue v
           JOIN FETCH e.category c
           WHERE UPPER(CAST(e.status AS string)) NOT IN :closedStatuses
             AND e.startDatetime > :now
           """)
    List<Event> findUpcomingDiscoverable(@Param("now") Instant now,
                                         @Param("closedStatuses") List<String> closedStatuses);

    @Query(
        value = """
           SELECT e.id
           FROM Event e
           JOIN e.venue v
           JOIN e.category c
           WHERE UPPER(CAST(e.status AS string)) NOT IN :closedStatuses
             AND e.startDatetime > :now
             AND (:startInstant IS NULL OR e.startDatetime >= :startInstant)
             AND (:endInstantExclusive IS NULL OR e.startDatetime < :endInstantExclusive)
             AND (:locationLike IS NULL
                  OR LOWER(v.city) LIKE :locationLike
                  OR LOWER(v.name) LIKE :locationLike)
             AND (:categoryCount = 0 OR LOWER(c.slug) IN :categorySlugs)
             AND (:tagCount = 0 OR e.id IN (
                  SELECT taggedEvent.id
                  FROM Event taggedEvent
                  JOIN taggedEvent.tags tag
                  WHERE LOWER(tag.slug) IN :tagSlugs
                  GROUP BY taggedEvent.id
                  HAVING COUNT(DISTINCT LOWER(tag.slug)) = :tagCount
             ))
           ORDER BY e.startDatetime ASC, e.id ASC
           """,
        countQuery = """
           SELECT COUNT(e.id)
           FROM Event e
           JOIN e.venue v
           JOIN e.category c
           WHERE UPPER(CAST(e.status AS string)) NOT IN :closedStatuses
             AND e.startDatetime > :now
             AND (:startInstant IS NULL OR e.startDatetime >= :startInstant)
             AND (:endInstantExclusive IS NULL OR e.startDatetime < :endInstantExclusive)
             AND (:locationLike IS NULL
                  OR LOWER(v.city) LIKE :locationLike
                  OR LOWER(v.name) LIKE :locationLike)
             AND (:categoryCount = 0 OR LOWER(c.slug) IN :categorySlugs)
             AND (:tagCount = 0 OR e.id IN (
                  SELECT taggedEvent.id
                  FROM Event taggedEvent
                  JOIN taggedEvent.tags tag
                  WHERE LOWER(tag.slug) IN :tagSlugs
                  GROUP BY taggedEvent.id
                  HAVING COUNT(DISTINCT LOWER(tag.slug)) = :tagCount
             ))
           """
    )
    Page<Integer> findUpcomingDiscoverableFilteredIds(
            @Param("now") Instant now,
            @Param("closedStatuses") List<String> closedStatuses,
            @Param("categorySlugs") Collection<String> categorySlugs,
            @Param("categoryCount") long categoryCount,
            @Param("tagSlugs") Collection<String> tagSlugs,
            @Param("tagCount") long tagCount,
            @Param("startInstant") Instant startInstant,
            @Param("endInstantExclusive") Instant endInstantExclusive,
            @Param("locationLike") String locationLike,
            Pageable pageable);

    @Query("""
           SELECT DISTINCT e
           FROM Event e
           LEFT JOIN FETCH e.tags
           JOIN FETCH e.venue
           JOIN FETCH e.category
           WHERE e.id IN :ids
           """)
    List<Event> findDiscoverableDetailsByIdIn(@Param("ids") Collection<Integer> ids);
}
