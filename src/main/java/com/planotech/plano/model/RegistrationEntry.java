package com.planotech.plano.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"event_id", "email"}
                )
        }
)
public class RegistrationEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long entryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private RegistrationForm form;

    private String name;
    private String email;
    private String phone;

    @Column(columnDefinition = "JSON")
    private String responsesJson;

    @Column(nullable = false, unique = true, updatable = false)
    private String badgeCode;

    private LocalDateTime submittedAt;

    @PrePersist
    public void generateBadgeCode() {
        if (this.badgeCode == null) {
            this.badgeCode = "BDG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        this.submittedAt = LocalDateTime.now();
    }
}

