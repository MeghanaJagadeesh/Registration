package com.planotech.plano.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class EmailContentSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, unique = true)
    private Event event;

//    private String eventName;
//    private String eventDescription;
//    private String eventLocation;
//    private String eventDate;
    private String supportEmail;
    private String supportPhone;
    private String eventWebsite;
//    private String eventLogo;

    @Column(columnDefinition = "TEXT")
    private String customDescription;
    private LocalDateTime updatedAt;
}

