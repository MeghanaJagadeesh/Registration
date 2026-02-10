package com.planotech.plano.repository;

import com.planotech.plano.enums.CheckpointType;
import com.planotech.plano.model.Checkpoint;
import com.planotech.plano.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckpointRepository extends JpaRepository<Checkpoint, Long> {
    List<Checkpoint> findByEventAndActiveTrue(Event event);
    Optional<Checkpoint> findByEvent_EventIdAndTypeAndActiveTrue(
            Long eventId,
            CheckpointType type
    );
}
