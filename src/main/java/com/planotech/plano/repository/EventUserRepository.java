package com.planotech.plano.repository;

import com.planotech.plano.enums.EventRole;
import com.planotech.plano.model.EventUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface EventUserRepository extends JpaRepository<EventUser, Long> {
    boolean existsByUser_UserIdAndEvent_EventId(Long userId, Long eventId);

//    @Query("""
//                SELECT eu.event
//                FROM EventUser eu
//                WHERE eu.user.id = :userId
//                  AND eu.role = :role
//                  AND eu.active = true
//            """)
    Optional<EventUser> findByEvent_EventIdAndUser_UserIdAndActiveTrue(Long eventId, Long userId);

    Optional<EventUser> findByUser_UserIdAndEvent_EventId(Long userId, Long eventId);

    List<EventUser> findByActiveTrueAndUser_CompanyIsNull();

    @Query("""
                select eu.role
                from EventUser eu
                where eu.user.userId = :userId
                  and eu.active = true
                order by
                  case eu.role
                    when 'ROLE_EVENT_ADMIN' then 1
                    when 'ROLE_ADMIN' then 2
                    when 'ROLE_COORDINATOR' then 3
                    else 4
                  end
            """)
    Optional<EventRole> findHighestRoleByUserId(Long userId);

    @Query("""
        SELECT eu FROM EventUser eu
        JOIN FETCH eu.user u
        WHERE eu.active = true
        AND eu.event.eventId IN :eventIds
    """)
    List<EventUser> findActiveUsersByEventIds(List<Long> eventIds);
}
