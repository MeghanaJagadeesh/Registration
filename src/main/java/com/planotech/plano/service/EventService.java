package com.planotech.plano.service;

import com.planotech.plano.enums.AccountStatus;
import com.planotech.plano.enums.EventRole;
import com.planotech.plano.enums.PlatformRole;
import com.planotech.plano.exception.AccessDeniedException;
import com.planotech.plano.helper.FileUploader;
import com.planotech.plano.model.Checkpoint;
import com.planotech.plano.model.Event;
import com.planotech.plano.model.EventUser;
import com.planotech.plano.model.User;
import com.planotech.plano.repository.CheckpointRepository;
import com.planotech.plano.repository.EventRepository;
import com.planotech.plano.repository.EventUserRepository;
import com.planotech.plano.repository.UserRepository;
import com.planotech.plano.request.CreateEventRequest;
import com.planotech.plano.response.EventResponse;
import com.planotech.plano.response.EventUserDTO;
import com.planotech.plano.response.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.planotech.plano.enums.CheckpointType.*;

@Service
public class EventService {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventUserRepository eventUserRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CheckpointRepository checkpointRepository;


    public ResponseEntity<?> createEvent(CreateEventRequest eventRequest, User loggedInUser) {

        if (loggedInUser.getPlatformRole() != PlatformRole.ROLE_SUPER_ADMIN &&
                loggedInUser.getPlatformRole() != PlatformRole.ROLE_ORG_ADMIN) {
            throw new AccessDeniedException("You are not allowed to create events");
        }

        Event event = new Event();
        event.setName(eventRequest.getName());
        event.setDescription(eventRequest.getDescription());
        event.setStartDate(eventRequest.getStartDate());
        event.setEndDate(eventRequest.getEndDate());
        event.setLocation(eventRequest.getLocation());
        event.setCreatedBy(loggedInUser);
        event.setCreatedAt(LocalDateTime.now());
        event.setLogoUrl(eventRequest.getLogo());

        if (loggedInUser.getPlatformRole() == PlatformRole.ROLE_ORG_ADMIN) {
            event.setCompany(loggedInUser.getCompany());
        } else {
            event.setCompany(null); // SUPER ADMIN
        }
        Event savedEvent = eventRepository.save(event);

        User eventAdmin = userRepository
                .findByEmail(eventRequest.getEmail())
                .orElseGet(() -> createNewUser(eventRequest, loggedInUser));

        eventUserRepository.findByUser_UserIdAndEvent_EventId(
                eventAdmin.getUserId(), savedEvent.getEventId()
        ).orElseGet(() -> {

            EventUser eu = new EventUser();
            eu.setUser(eventAdmin);
            eu.setEvent(savedEvent);
            eu.setRole(EventRole.ROLE_EVENT_ADMIN);
            eu.setAssignedBy(loggedInUser);
            eu.setAssignedAt(LocalDateTime.now());
            eu.setActive(true);

            return eventUserRepository.save(eu);
        });

        createDefaultCheckpoints(event);

        return ResponseEntity.ok(Map.of(
                "message", "Event created successfully",
                "status", "success",
                "code", HttpStatus.OK.value()
        ));
    }

    private void createDefaultCheckpoints(Event event) {
        List<Checkpoint> defaults = List.of(
                new Checkpoint(null, event, REGISTRATION, "Registration Desk", null, true, true, LocalDateTime.now()),
                new Checkpoint(null, event, FOOD, "Lunch", null, true, true, LocalDateTime.now()),
                new Checkpoint(null, event, FOOD, "Dinner", null, true, true, LocalDateTime.now()),
                new Checkpoint(null, event, KIT, "Kit Distribution", null, true, true, LocalDateTime.now())
        );
        checkpointRepository.saveAll(defaults);
    }

    private User createNewUser(CreateEventRequest req, User creator) {
        User user = new User();
        user.setName(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPassword(encoder.encode(req.getPassword()));

        // DEFAULT PLATFORM ROLE
        user.setPlatformRole(PlatformRole.ROLE_USER);

        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedBy(creator);
        user.setCreatedAt(LocalDateTime.now());

        // ORG ADMIN creates → same org
        if (creator.getPlatformRole() == PlatformRole.ROLE_ORG_ADMIN) {
            user.setCompany(creator.getCompany());
        }

        return userRepository.save(user);
    }

    public ResponseEntity<?> getEventsByUserRole(User user) {

        LocalDate today = LocalDate.now();
        List<Event> events;

        switch (user.getPlatformRole()) {

            case ROLE_SUPER_ADMIN -> {
                events = eventRepository.findAllActiveEvents(today);
            }

            case ROLE_ORG_ADMIN -> {
                if (user.getCompany() == null) {
                    throw new IllegalStateException("Organization not assigned");
                }
                events = eventRepository.findActiveEventsByCompany(
                        user.getCompany().getCompanyId(), today
                );
            }

            default -> {
                // NORMAL USER → EVENTS VIA EVENT_USER
                events = eventRepository.findActiveEventsAssignedToUser(
                        user.getUserId(), today
                );
            }
        }

        List<Long> eventIds = events.stream()
                .map(Event::getEventId)
                .toList();

        List<EventUser> eventUsers =
                eventUserRepository.findActiveUsersByEventIds(eventIds);

        Map<Long, List<EventUserDTO>> usersByEvent = eventUsers.stream()
                .collect(Collectors.groupingBy(
                        eu -> eu.getEvent().getEventId(),
                        Collectors.mapping(
                                eu -> new EventUserDTO(
                                        eu.getUser().getUserId(),
                                        eu.getUser().getName(),
                                        eu.getUser().getEmail(),
                                        eu.getRole()
                                ),
                                Collectors.toList()
                        )
                ));

        List<EventResponse> response = events.stream()
                .map(event -> {
                    EventResponse r = toDto(event);
                    r.setAssignedUsers(
                            usersByEvent.getOrDefault(
                                    event.getEventId(),
                                    Collections.emptyList()
                            )
                    );
                    return r;
                })
                .toList();

        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "count", response.size(),
                        "data", response
                )
        );
    }

    private EventResponse toDto(Event e) {
        User user = e.getCreatedBy();
        UserDTO userDTO = new UserDTO(user.getUserId(), user.getName(), user.getEmail(), null);
        EventResponse r = new EventResponse();
        r.setEventId(e.getEventId());
        r.setName(e.getName());
        r.setEventKey(e.getEventKey());
        r.setStartDate(e.getStartDate());
        r.setEndDate(e.getEndDate());
        r.setLocation(e.getLocation());
        r.setLogoUrl(e.getLogoUrl());
        r.setDescription(e.getDescription());
        r.setCreatedBy(userDTO);
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
