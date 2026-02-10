package com.planotech.plano.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "checkpoint_logs",
        indexes = {
                @Index(name = "idx_badge_checkpoint", columnList = "checkpoint_id, registration_entry_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckpointLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_entry_id", nullable = false)
    private RegistrationEntry registrationEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_id", nullable = false)
    private Checkpoint checkpoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scanned_by", nullable = false)
    private User scannedBy;

    private LocalDateTime scannedAt;
}

