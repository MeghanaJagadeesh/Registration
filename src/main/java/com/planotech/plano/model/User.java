package com.planotech.plano.model;

import com.planotech.plano.enums.AccountStatus;
import com.planotech.plano.enums.PlatformRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Data
@ToString(exclude = {"eventRoles", "subordinates", "manager", "company", "createdBy"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String name;

    @Column(unique = true)
    private String email;
    private String password;
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlatformRole platformRole;

    private String refreshToken;
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // If the user belongs to a company (Organization Admin or Event Admin under company)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

//    // If the user is assigned to a single event (Event Admin for a single event or Admin)
//    @OneToMany(fetch = FetchType.LAZY)
//    @JoinColumn(name = "event_id")
//    private List<Event> event;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<EventUser> eventRoles;

    // Optional: Track which users are assigned under this user (for Event Admin managing Admins)
    @OneToMany(mappedBy = "manager")
    private List<User> subordinates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

//    Added manager field in your entity to map the subordinates properly. Without it, subordinates won’t work correctly.
}


//| User                       | company   | event   | Meaning                                |
//        | -------------------------- | --------- | ------- | -------------------------------------- |
//        | Super Admin                | null      | null    | Platform-wide                          |
//        | Organization Admin         | companyId | null    | Full access to company events          |
//        | Event Admin (Company)      | companyId | null    | Can manage all company events assigned |
//        | Event Admin (Single Event) | null      | eventId | Can manage only one event              |
//        | Admin (low-level)          | null      | eventId | Limited access to one event            |
