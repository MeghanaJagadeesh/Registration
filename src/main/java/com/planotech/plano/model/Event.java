package com.planotech.plano.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, unique = true, updatable = false)
    private String eventKey;

    private LocalDate startDate;
    private LocalDate endDate;

    private String logoUrl;

    // If event belongs to a company
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    private LocalDateTime createdAt;

    //    // Users assigned to this event (Event Admins for single-event or Admins)
//    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<User> users;
    private Boolean active = true;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<EventUser> eventUsers;


    @PrePersist
    public void generateEventKey() {
        if (this.eventKey == null) {
            this.eventKey = "EVT-" + UUID.randomUUID();
        }
    }

}