package com.planotech.plano.model;

import com.planotech.plano.enums.CheckpointType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "checkpoints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Checkpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long checkpointId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckpointType type;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "json")
    private String metadataJson;

    private Boolean active = true;

    private Boolean systemDefined = false;

    private LocalDateTime createdAt;
}

