package com.planotech.plano.service;

import com.planotech.plano.auth.SecurityUtil;
import com.planotech.plano.enums.AccountStatus;
import com.planotech.plano.enums.EventRole;
import com.planotech.plano.enums.PlatformRole;
import com.planotech.plano.exception.EmailAlreadyExistsException;
import com.planotech.plano.model.EventUser;
import com.planotech.plano.model.User;
import com.planotech.plano.repository.EventRepository;
import com.planotech.plano.repository.EventUserRepository;
import com.planotech.plano.repository.UserRepository;
import com.planotech.plano.response.AssignedEventDTO;
import com.planotech.plano.response.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SuperAdminService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventUserRepository eventUserRepository;

    public ResponseEntity<?> createSuperAdmin(User user) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser.isPresent()) {
            User ex = existingUser.get();
            throw new EmailAlreadyExistsException(String.format(
                    "%s email already exists with %s",
                    ex.getEmail(),
                    ex.getPlatformRole().name().toLowerCase().replace("_", " ")
            ));
        }
        user.setPlatformRole(PlatformRole.ROLE_SUPER_ADMIN);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedBy(SecurityUtil.getCurrentUser());

        userRepository.save(user);
        response.put("message", "super admin created successfully");
        response.put("code", HttpStatus.CREATED.value());
        response.put("status", "success");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    public ResponseEntity<?> getEventUsers() {

        List<EventUser> mappings =
                eventUserRepository.findByActiveTrueAndUser_CompanyIsNull();

        Map<Long, UserDTO> userMap = new LinkedHashMap<>();

        for (EventUser eu : mappings) {

            User user = eu.getUser();

            userMap.computeIfAbsent(user.getUserId(), id ->
                    new UserDTO(
                            user.getUserId(),
                            user.getName(),
                            user.getEmail(),
                            new ArrayList<>()
                    )
            ).getAssignedEvents().add(
                    new AssignedEventDTO(
                            eu.getEvent().getEventId(),
                            eu.getEvent().getName(),
                            eu.getEvent().getEventKey(),
                            eu.getRole()
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "code", HttpStatus.OK.value(),
                        "data", userMap.values()
                )
        );
    }

}
