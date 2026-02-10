package com.planotech.plano.service;

import com.planotech.plano.enums.CheckpointType;
import com.planotech.plano.exception.ResourceNotFoundException;
import com.planotech.plano.model.Checkpoint;
import com.planotech.plano.model.RegistrationEntry;
import com.planotech.plano.model.User;
import com.planotech.plano.repository.CheckpointRepository;
import com.planotech.plano.repository.RegistrationEntryCustomRepository;
import com.planotech.plano.repository.RegistrationEntryRepository;
import com.planotech.plano.request.BadgeFilterRequest;
import com.planotech.plano.response.BadgeListResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
        res.setQrUrl(entry.getQrUrl());

        res.setSubmittedAt(entry.getSubmittedAt());

        try {
            res.setResponses(
                    objectMapper.readValue(
                            entry.getResponsesJson(),
                            new TypeReference<Map<String, Object>>() {
                            }
                    )
            );
        } catch (Exception e) {
            res.setResponses(Map.of());
        }

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

        Checkpoint registrationCheckpoint =
                checkpointRepository
                        .findByEvent_EventIdAndTypeAndActiveTrue(
                                eventId,
                                CheckpointType.REGISTRATION
                        )
                        .orElseThrow(() ->
                                new IllegalStateException("Registration checkpoint not configured")
                        );
        checkPointService.saveLog(entry, registrationCheckpoint, user);

        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "message", "Check-in successful",
                        "data", toBadgeListResponse(entry)
                )
        );
    }

}
