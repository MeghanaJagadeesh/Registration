package com.planotech.plano.repository;

import com.planotech.plano.enums.CheckpointType;
import com.planotech.plano.model.Checkpoint;
import com.planotech.plano.model.CheckpointLog;
import com.planotech.plano.model.Event;
import com.planotech.plano.model.RegistrationEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    List<CheckpointLog> findByEvent(Event event);

    List<CheckpointLog> findByCheckpointAndEvent(
            Checkpoint checkpoint,
            Event event
    );

    List<CheckpointLog> findByCheckpoint_TypeAndEvent(
            CheckpointType type,
            Event event
    );
}
