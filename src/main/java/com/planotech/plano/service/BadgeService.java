package com.planotech.plano.service;

import com.planotech.plano.enums.CheckpointType;
import com.planotech.plano.enums.FormStatus;
import com.planotech.plano.exception.ResourceNotFoundException;
import com.planotech.plano.model.*;
import com.planotech.plano.repository.*;
import com.planotech.plano.request.BadgeConfigRequestDTO;
import com.planotech.plano.request.BadgeFilterRequest;
import com.planotech.plano.response.BadgeFormField;
import com.planotech.plano.response.BadgeListResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BadgeService {

    @Autowired
    RegistrationEntryRepository entryRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EventAuthorizationService eventAuthorizationService;

    @Autowired
    RegistrationEntryCustomRepository badgeRepository;

    @Autowired
    CheckPointService checkPointService;

    @Autowired
    CheckpointRepository checkpointRepository;

    @Autowired
    RegistrationFormRepository formRepository;

    @Autowired
    BadgeConfigurationRepository badgeRepo;

    @Autowired
    EventRepository eventRepository;

    @Transactional
    public ResponseEntity<?> getAllBadges(
            Long eventId,
            int page,
            int size,
            String search,
            User user
    ) {
        eventAuthorizationService.authorize(eventId, user);
        page = Math.max(page, 0);
        size = Math.min(size, 100);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("submittedAt").descending()
        );

        Page<RegistrationEntry> pageResult;

        if (search != null && !search.isBlank()) {
            pageResult = badgeRepository.searchBadges(
                    eventId,
                    search.trim(),
                    pageable
            );
        } else {
            pageResult =
                    entryRepository.findByEvent_EventId(eventId, pageable);
        }

        List<BadgeListResponse> data = pageResult.getContent()
                .stream()
                .map(this::toBadgeListResponse)
                .toList();

        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "totalElements", pageResult.getTotalElements(),
                        "totalPages", pageResult.getTotalPages(),
                        "data", data
                )
        );
    }

    private BadgeListResponse toBadgeListResponse(RegistrationEntry entry) {
        BadgeListResponse res = new BadgeListResponse();

        res.setEntryId(entry.getEntryId());
        res.setName(entry.getName());
        res.setEmail(entry.getEmail());
        res.setPhone(entry.getPhone());

        res.setBadgeCode(entry.getBadgeCode());


        res.setSubmittedAt(entry.getSubmittedAt());

        res.setResponses(
                objectMapper.readValue(
                        entry.getResponsesJson(),
                        new TypeReference<Map<String, Object>>() {
                        }
                )
        );
        return res;
    }

    @Transactional
    public ResponseEntity<?> filterBadges(
            Long eventId,
            int page,
            int size,
            BadgeFilterRequest request,
            User user
    ) {
        eventAuthorizationService.authorize(eventId, user);

        page = Math.max(page, 0);
        size = Math.min(size, 100);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("submittedAt").descending()
        );

        Page<RegistrationEntry> pageResult =
                badgeRepository.findBadgesWithFilters(
                        eventId,
                        request,
                        pageable
                );

        List<BadgeListResponse> data = pageResult.getContent()
                .stream()
                .map(this::toBadgeListResponse)
                .toList();

        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "totalElements", pageResult.getTotalElements(),
                        "totalPages", pageResult.getTotalPages(),
                        "data", data
                )
        );
    }

    @Transactional
    public ResponseEntity<?> getBadgeByCode(
            Long eventId,
            String badgeCode,
            User user
    ) {
        eventAuthorizationService.authorize(eventId, user);

        RegistrationEntry entry = entryRepository
                .findByEvent_EventIdAndBadgeCode(eventId, badgeCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Badge not found")
                );

        BadgeListResponse response = toBadgeListResponse(entry);

        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "data", response
                )
        );
    }

    @Transactional
    public ResponseEntity<?> manualCheckIn(
            Long eventId,
            Long entryId,
            User user
    ) {
        eventAuthorizationService.authorize(eventId, user);
        RegistrationEntry entry = entryRepository
                .findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found"));

        if (entry.getCheckedIn()) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", "error",
                            "message", "Attendee already checked in"
                    )
            );
        }
        entry.setCheckedIn(true);
        entry.setCheckedInAt(LocalDateTime.now());
        entryRepository.save(entry);

        List<Checkpoint> registrationCheckpoint =
                checkpointRepository
                        .findByEvent_EventIdAndTypeAndActiveTrue(
                                eventId,
                                CheckpointType.REGISTRATION
                        );
        checkPointService.saveLog(entry, registrationCheckpoint.get(0), user);

        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "message", "Check-in successful",
                        "data", toBadgeListResponse(entry)
                )
        );
    }

    @Transactional
    public ResponseEntity<?> exportAll(Long eventId, User user) {

        eventAuthorizationService.authorize(eventId, user);

        List<RegistrationEntry> entries =
                entryRepository.findByEvent_EventIdOrderBySubmittedAtDesc(eventId);

        List<BadgeListResponse> data = entries.stream()
                .map(this::toBadgeListResponse)
                .toList();

        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "total", data.size(),
                        "data", data
                )
        );
    }

    public ResponseEntity<?> getFormFieldsForBadge(Long eventId, User user) {
        eventAuthorizationService.authorize(eventId, user);
        RegistrationForm form = formRepository
                .findByEventEventIdAndActiveTrueAndStatus(eventId, FormStatus.PUBLISHED)
                .orElseThrow(() -> new RuntimeException("Active form not found"));
        System.out.println(form.getFormId());
        return ResponseEntity.ok(form.getFields()
                .stream()
                .sorted(Comparator.comparing(FormField::getDisplayOrder))
                .map(field -> new BadgeFormField(
                        field.getFieldKey(),
                        field.getLabel(),
                        field.getRequired(),
                        field.getFieldType().name()
                ))
                .collect(Collectors.toList()));
    }

    public ResponseEntity<?> saveConfig(Long eventId,
                                        BadgeConfigRequestDTO dto,
                                        User user) {

        eventAuthorizationService.authorize(eventId, user);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        RegistrationForm form = formRepository
                .findByEventEventIdAndActiveTrueAndStatus(eventId, FormStatus.PUBLISHED)
                .orElseThrow(() -> new RuntimeException("Active form not found"));

        if (dto.getSelectedFieldKeys() == null || dto.getSelectedFieldKeys().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "code", 400,
                            "status", "fail",
                            "message", "At least one field must be selected"
                    )
            );
        }

        List<String> validKeys = form.getFields()
                .stream()
                .map(FormField::getFieldKey)
                .toList();

        List<String> cleanedUniqueKeys = dto.getSelectedFieldKeys()
                .stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()  // removes duplicates while keeping order
                .toList();

        for (String key : cleanedUniqueKeys) {
            if (!validKeys.contains(key)) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "code", 400,
                                "status", "fail",
                                "message", "Invalid field selected: " + key
                        )
                );
            }
        }

        String json = objectMapper.writeValueAsString(cleanedUniqueKeys);

        BadgeConfiguration config = badgeRepo
                .findByEventEventId(eventId)
                .orElse(new BadgeConfiguration());

        config.setEvent(event);
        config.setSelectedFieldKeysJson(json);

        badgeRepo.save(config);

        return ResponseEntity.ok(
                Map.of(
                        "code", 200,
                        "status", "success",
                        "message", "Badge configuration saved successfully"
                )
        );
    }

    public ResponseEntity<?> getConfig(Long eventId, User user) {

        eventAuthorizationService.authorize(eventId, user);

        BadgeConfiguration config = badgeRepo
                .findByEventEventId(eventId)
                .orElseThrow(() -> new RuntimeException("Badge config not found"));

        try {
            List<String> selectedKeys = objectMapper.readValue(
                    config.getSelectedFieldKeysJson(),
                    new TypeReference<List<String>>() {
                    }
            );

            return ResponseEntity.ok(
                    Map.of(
                            "code", 200,
                            "status", "success",
                            "data", new BadgeConfigRequestDTO(selectedKeys)
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "code", 500,
                            "status", "fail",
                            "message", "Error parsing badge configuration"
                    ));
        }
    }

}
