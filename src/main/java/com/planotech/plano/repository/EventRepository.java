package com.planotech.plano.repository;

import com.planotech.plano.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByEventKey(String eventKey);

    // SUPER ADMIN – all active events
//    @Query("""
//                SELECT e FROM Event e
//                WHERE e.active = true
//                AND e.endDate >= :today
//                ORDER BY e.startDate ASC
//            """)
    @Query("""
                SELECT e FROM Event e
                WHERE e.active = true
                ORDER BY e.startDate ASC
            """)
    List<Event> findAllActiveEvents(@Param("today") LocalDate today);

    // ORG ADMIN – active events under company
    @Query("""
                SELECT e FROM Event e
                WHERE e.company.companyId = :companyId
                AND e.active = true
                ORDER BY e.startDate ASC
            """)
    List<Event> findActiveEventsByCompany(
            @Param("companyId") Long companyId,
            @Param("today") LocalDate today
    );

    // EVENT ADMIN – active events assigned to user
    @Query("""
                SELECT e FROM Event e
                JOIN EventUser eu ON eu.event.id = e.id
                WHERE eu.user.userId = :userId
                AND e.active = true
                ORDER BY e.startDate ASC
            """)
    List<Event> findActiveEventsAssignedToUser(
            @Param("userId") Long userId,
            @Param("today") LocalDate today
    );

}
