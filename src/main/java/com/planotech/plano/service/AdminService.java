package com.planotech.plano.service;

import com.planotech.plano.enums.AccountStatus;
import com.planotech.plano.enums.EventRole;
import com.planotech.plano.enums.PlatformRole;
import com.planotech.plano.exception.AccessDeniedException;
import com.planotech.plano.exception.ResourceNotFoundException;
import com.planotech.plano.model.Event;
import com.planotech.plano.model.EventUser;
import com.planotech.plano.model.User;
import com.planotech.plano.repository.EventRepository;
import com.planotech.plano.repository.EventUserRepository;
import com.planotech.plano.repository.UserRepository;
import com.planotech.plano.request.CreateEventRequest;
import com.planotech.plano.request.CreateUserRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class AdminService {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventAuthorizationService eventAuthorizationService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    EventUserRepository eventUserRepository;

    @Transactional
    public ResponseEntity<?> assignEventUser(Long eventId, CreateUserRequest request, User loggedInUser) {
        log.info("Assigning user to event: {}", eventId);
        EventUser actor =
                eventAuthorizationService.authorize(eventId, loggedInUser);
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event Not Found"));

        EventRole actorRole = (actor == null)
                ? EventRole.ROLE_EVENT_ADMIN
                : actor.getRole();
        if (!canAssign(actorRole, request.getRole())) {
            throw new AccessDeniedException("You cannot assign this role");
        }
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseGet(() -> createNewUser(request, loggedInUser));
        log.info("User ID: {}", user.getUserId());
        if (eventUserRepository.existsByUser_UserIdAndEvent_EventId(user.getUserId(), eventId)) {
            throw new IllegalStateException("User already assigned");
        }

        EventUser mapping = new EventUser();
        mapping.setUser(user);
        mapping.setEvent(event);
        mapping.setRole(request.getRole());
        mapping.setAssignedBy(loggedInUser);
        mapping.setAssignedAt(LocalDateTime.now());
        mapping.setActive(true);

        eventUserRepository.save(mapping);
        return ResponseEntity.ok(Map.of(
                "message", "Assigned successfully",
                "status", "success",
                "code", HttpStatus.OK.value()
        ));
    }

    private User createNewUser(CreateUserRequest req, User creator) {
        User user = new User();
        user.setName(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setPlatformRole(PlatformRole.ROLE_USER);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedBy(creator);
        user.setCreatedAt(LocalDateTime.now());
        user.setCompany(null);
        return userRepository.saveAndFlush(user);
    }

    private boolean canAssign(EventRole actor, EventRole target) {
        System.out.println(actor + " " + target);
        return switch (actor) {
            case ROLE_EVENT_ADMIN ->
                    target == EventRole.ROLE_ADMIN || target == EventRole.ROLE_COORDINATOR || target == EventRole.ROLE_EVENT_ADMIN;
            case ROLE_ADMIN -> target == EventRole.ROLE_COORDINATOR;
            default -> false;
        };
    }

    public ResponseEntity<?> modifyEvent(Long eventId, CreateEventRequest request, User user) {
        EventUser eu = eventAuthorizationService.authorize(eventId, user);
        eventAuthorizationService.validateDraftPermission(user, eu);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        event.setName(request.getName());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        event.setLogoUrl(request.getLogo());
        event.setDescription(request.getDescription());
        eventRepository.save(event);
        return ResponseEntity.ok(Map.of(
                "message", "Event updated successfully",
                "code", "200",
                "status", "success"
        ));
    }

    public ResponseEntity<?> getUser(Long eventId, String email, User loggedInUser) {

        eventAuthorizationService.authorize(eventId, loggedInUser);
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "exists", false,
                    "assignedToEvent", false
            ));
        }

        User user = userOpt.get();
        Optional<EventUser> eventUserOpt =
                eventUserRepository.findByUser_UserIdAndEvent_EventId(
                        user.getUserId(), eventId
                );

        if (eventUserOpt.isPresent()) {
            EventUser eventUser = eventUserOpt.get();

            return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "assignedToEvent", true,
                    "user", Map.of(
                            "userId", user.getUserId(),
                            "email", user.getEmail(),
                            "name", user.getName()
                    ),
                    "eventRole", eventUser.getRole()
            ));
        }
        return ResponseEntity.ok(Map.of(
                "exists", true,
                "assignedToEvent", false,
                "user", Map.of(
                        "userId", user.getUserId(),
                        "email", user.getEmail(),
                        "name", user.getName()
                )
        ));
    }
}
