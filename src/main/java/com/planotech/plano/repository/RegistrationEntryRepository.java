package com.planotech.plano.repository;

import com.planotech.plano.model.RegistrationEntry;
import com.planotech.plano.request.BadgeFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationEntryRepository extends JpaRepository<RegistrationEntry, Long> {
    boolean existsByEvent_EventIdAndEmail(Long eventId, String email);

    Page<RegistrationEntry> findByEvent_EventId(Long eventId, Pageable pageable);

    @Query("""
                SELECT r FROM RegistrationEntry r
                WHERE r.event.eventId = :eventId
                AND (
                    :search IS NULL
                    OR :search = ''
                    OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(r.email) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(r.phone) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(r.badgeCode) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(r.responsesJson) LIKE LOWER(CONCAT('%', :search, '%'))
                )
            """)
    Page<RegistrationEntry> search(
            @Param("eventId") Long eventId,
            @Param("search") String search,
            Pageable pageable
    );

    Optional<RegistrationEntry> findByBadgeCode(String badgeCode);

    Optional<RegistrationEntry> findByEvent_EventIdAndBadgeCode(
            Long eventId,
            String badgeCode
    );

    List<RegistrationEntry> findByEvent_EventIdOrderBySubmittedAtDesc(Long eventId);

    //analytics

        long countByEvent_EventId(Long eventId);

    long countByEvent_EventIdAndCheckedInTrue(Long eventId);


    @Query("""
    SELECT COUNT(r.entryId)
    FROM RegistrationEntry r
    WHERE r.event.eventId = :eventId
      AND r.submittedAt BETWEEN :start AND :end
""")
    long countBetween(
            Long eventId,
            LocalDateTime start,
            LocalDateTime end
    );

      long countByEvent_EventIdIn(List<Long> eventIds);

    long countByEvent_EventIdInAndCheckedInTrue(List<Long> eventIds);

    @Query("""
    SELECT COUNT(r.entryId)
    FROM RegistrationEntry r
    WHERE r.event.eventId = :eventId
      AND r.submittedAt BETWEEN :start AND :end
""")
    long countTodayRegistrations(
            Long eventId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
    SELECT COUNT(r.entryId)
    FROM RegistrationEntry r
    WHERE r.event.eventId = :eventId
      AND r.checkedIn = true
      AND r.checkedInAt BETWEEN :start AND :end
""")
    long countTodayCheckIns(
            Long eventId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
    SELECT DATE(r.submittedAt), COUNT(r.entryId)
    FROM RegistrationEntry r
    WHERE r.event.eventId = :eventId
    GROUP BY DATE(r.submittedAt)
    ORDER BY DATE(r.submittedAt)
""")
    List<Object[]> getRegistrationTrend(Long eventId);

}
