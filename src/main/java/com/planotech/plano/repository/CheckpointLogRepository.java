package com.planotech.plano.repository;

import com.planotech.plano.enums.CheckpointType;
import com.planotech.plano.model.Checkpoint;
import com.planotech.plano.model.CheckpointLog;
import com.planotech.plano.model.Event;
import com.planotech.plano.model.RegistrationEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CheckpointLogRepository extends JpaRepository<CheckpointLog, Long> {

    boolean existsByCheckpointAndRegistrationEntry(
            Checkpoint checkpoint,
            RegistrationEntry registrationEntry
    );

    @Query("""
                SELECT COUNT(c) > 0 FROM CheckpointLog c
                WHERE c.registrationEntry = :entry
                  AND c.checkpoint = :checkpoint
                  AND DATE(c.scannedAt) = CURRENT_DATE
            """)
    boolean alreadyScannedToday(
            @Param("entry") RegistrationEntry entry,
            @Param("checkpoint") Checkpoint checkpoint
    );

    @Query("""
            SELECT c FROM CheckpointLog c
            WHERE c.event = :event
            AND (:type IS NULL OR c.checkpoint.type = :type)
            AND (:checkpointId IS NULL OR c.checkpoint.id = :checkpointId)
            AND (:fromDateTime IS NULL OR c.scannedAt >= :fromDateTime)
            AND (:toDateTime IS NULL OR c.scannedAt <= :toDateTime)
            """)
    Page<CheckpointLog> findLogsWithFilters(
            @Param("event") Event event,
            @Param("type") CheckpointType type,
            @Param("checkpointId") Long checkpointId,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime,
            Pageable pageable
    );

    @Query("""
                SELECT COUNT(c.logId)
                FROM CheckpointLog c
                WHERE c.scannedBy.userId = :userId
                  AND c.scannedAt BETWEEN :start AND :end
            """)
    long countTodayScansByUser(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );


    @Query("""
                SELECT COUNT(c.logId)
                FROM CheckpointLog c
                WHERE c.event.eventId IN :eventIds
            """)
    long countTotalScansForEvents(List<Long> eventIds);

    @Query("""
                SELECT COUNT(c.logId)
                FROM CheckpointLog c
                WHERE c.event.eventId = :eventId
            """)
    long countTotalScans(Long eventId);

    @Query("""
    SELECT DATE(c.scannedAt), COUNT(c.logId)
    FROM CheckpointLog c
    WHERE c.event.eventId = :eventId
      AND c.scannedAt >= :startDate
    GROUP BY DATE(c.scannedAt)
    ORDER BY DATE(c.scannedAt)
""")
    List<Object[]> getCheckpointDailyUsage(
            Long eventId,
            LocalDateTime startDate
    );

    @Query("""
    SELECT c.checkpoint.name,
           COUNT(c.logId),
           COUNT(DISTINCT c.registrationEntry.entryId),
           SUM(
               CASE
                   WHEN FUNCTION('DATE', c.scannedAt) = CURRENT_DATE
                   THEN 1 ELSE 0
               END
           )
    FROM CheckpointLog c
    WHERE c.event.eventId = :eventId
    GROUP BY c.checkpoint.name
    ORDER BY COUNT(c.logId) DESC
""")
    List<Object[]> getCheckpointTotalUsage(Long eventId);

    @Query("""
    SELECT DATE(c.scannedAt),
           c.checkpoint.name,
           COUNT(c.logId)
    FROM CheckpointLog c
    WHERE c.event.eventId = :eventId
    GROUP BY DATE(c.scannedAt), c.checkpoint.name
    ORDER BY DATE(c.scannedAt)
""")
    List<Object[]> getCheckpointDailyUsageDetailed(Long eventId);

    @Query("""
    SELECT COUNT(c.logId)
    FROM CheckpointLog c
    WHERE c.event.eventId = :eventId
      AND c.scannedAt BETWEEN :start AND :end
""")
    long countTodayScans(
            Long eventId,
            LocalDateTime start,
            LocalDateTime end
    );


}
