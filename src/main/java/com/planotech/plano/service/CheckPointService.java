package com.planotech.plano.service;

import com.nimbusds.oauth2.sdk.util.JSONUtils;
import com.planotech.plano.enums.CheckpointType;
import com.planotech.plano.exception.CustomBadRequestException;
import com.planotech.plano.exception.ResourceNotFoundException;
import com.planotech.plano.helper.JsonUtil;
import com.planotech.plano.model.*;
import com.planotech.plano.repository.CheckpointLogRepository;
import com.planotech.plano.repository.CheckpointRepository;
import com.planotech.plano.repository.EventRepository;
import com.planotech.plano.repository.RegistrationEntryRepository;
import com.planotech.plano.request.CreateCheckpointRequest;
import com.planotech.plano.response.CheckpointLogResponse;
import com.planotech.plano.response.CheckpointResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CheckPointService {

    @Autowired
    EventAuthorizationService eventAuthorizationService;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    CheckpointRepository checkpointRepository;

    @Autowired
    RegistrationEntryRepository registrationEntryRepository;

    @Autowired
    CheckpointLogRepository checkpointLogRepository;

    @Autowired
    JsonUtil jsonUtils;

    @Autowired
    ObjectMapper objectMapper;

    public ResponseEntity<?> getCheckPoints(Long eventId, User user) {
        eventAuthorizationService.authorize(eventId, user);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        List<CheckpointResponse> checkpoints =
                checkpointRepository.findByEventAndActiveTrue(event)
                        .stream()
                        .map(cp -> CheckpointResponse.builder()
                                .checkpointId(cp.getCheckpointId())
                                .name(cp.getName())
                                .type(cp.getType())
                                .systemDefined(cp.getSystemDefined())
                                .active(cp.getActive())
                                .metadata(jsonUtils.toMap(cp.getMetadataJson()))
                                .build()
                        )
                        .toList();

        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "data", checkpoints,
                        "code", 200
                )
        );
    }

    public ResponseEntity<?> scanQr(Long eventId, String badgeCode, Long checkpointId, User user) {
        eventAuthorizationService.authorize(eventId, user);
        RegistrationEntry entry = registrationEntryRepository
                .findByBadgeCode(badgeCode).orElseThrow(() -> new ResourceNotFoundException("Badge code not found"));
        Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
                .orElseThrow(() -> new RuntimeException("Checkpoint not found"));
        validateCheckpointAccess(checkpoint, entry);

        return switch (checkpoint.getType()) {

            case REGISTRATION -> handleRegistration(entry, user, checkpoint);

            case KIT -> handleKit(entry, user, checkpoint);

            case FOOD -> handleFood(entry, user, checkpoint);

            case HALL, CUSTOM -> {
                saveLog(entry, checkpoint, user);
                yield ResponseEntity.ok(success(entry, checkpoint));
            }
        };
    }

    private void validateCheckpointAccess(Checkpoint checkpoint, RegistrationEntry entry) {
        if (checkpoint.getType() == CheckpointType.REGISTRATION) {
            return;
        }
        if (!entry.getCheckedIn()) {
            throw new CustomBadRequestException("Attendee must complete registration before accessing " + checkpoint.getType());
        }
    }

    private ResponseEntity<?> handleFood(RegistrationEntry entry, User user, Checkpoint checkpoint) {
        boolean scannedToday =
                checkpointLogRepository.alreadyScannedToday(entry, checkpoint);
        if (scannedToday) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", "Already consumed " + checkpoint.getName() + " today",
                            "status", "fail",
                            "code", HttpStatus.BAD_REQUEST.value()
                    ));
        }
        saveLog(entry, checkpoint, user);
        return ResponseEntity.ok(success(entry, checkpoint));
    }

    private ResponseEntity<?> handleKit(RegistrationEntry entry, User user, Checkpoint checkpoint) {
        if (checkpointLogRepository
                .existsByCheckpointAndRegistrationEntry(checkpoint, entry)) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", "Kit already collected",
                            "status", "fail",
                            "code", HttpStatus.BAD_REQUEST.value()
                    ));
        }
        saveLog(entry, checkpoint, user);
        return ResponseEntity.ok(success(entry, checkpoint));
    }

    private ResponseEntity<?> handleRegistration(RegistrationEntry entry, User user, Checkpoint checkpoint) {
        if (Boolean.TRUE.equals(entry.getCheckedIn())) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", "Already checked in",
                            "status", "fail",
                            "code", HttpStatus.BAD_REQUEST.value()
                    ));
        }
        entry.setCheckedIn(true);
        entry.setCheckedInAt(LocalDateTime.now());
        registrationEntryRepository.save(entry);
        saveLog(entry, checkpoint, user);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "action", "PRINT_BADGE",
                "name", entry.getName()
        ));
    }

    public void saveLog(
            RegistrationEntry entry,
            Checkpoint checkpoint,
            User scanner
    ) {
        checkpointLogRepository.save(
                CheckpointLog.builder()
                        .event(checkpoint.getEvent())
                        .registrationEntry(entry)
                        .checkpoint(checkpoint)
                        .scannedBy(scanner)
                        .scannedAt(LocalDateTime.now())
                        .build()
        );
    }

    private Map<String, Object> success(
            RegistrationEntry entry,
            Checkpoint checkpoint
    ) {
        return Map.of(
                "status", "SUCCESS",
                "attendee", entry.getName(),
                "checkpoint", checkpoint.getName(),
                "time", LocalDateTime.now()
        );
    }


    @Transactional
    public ResponseEntity<?> createCheckpoint(Long eventId, CreateCheckpointRequest request, User user) {

        eventAuthorizationService.authorize(eventId, user);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Checkpoint checkpoint = new Checkpoint();
        checkpoint.setEvent(event);
        checkpoint.setType(request.getType());
        checkpoint.setName(request.getName());
        checkpoint.setSystemDefined(false);
        checkpoint.setActive(true);
        checkpoint.setCreatedAt(LocalDateTime.now());

        if (request.getMetadata() != null) {
            checkpoint.setMetadataJson(
                    objectMapper.writeValueAsString(request.getMetadata())
            );
        }
        checkpointRepository.save(checkpoint);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Checkpoint added"
        ));
    }

    public ResponseEntity<?> getFoodScans(Long eventId, User user) {

        eventAuthorizationService.authorize(eventId, user);

        Event event = eventRepository.findById(eventId)
                .orElseThrow();

        List<CheckpointLogResponse> data =
                checkpointLogRepository
                        .findByCheckpoint_TypeAndEvent(CheckpointType.FOOD, event)
                        .stream()
                        .map(this::toDto)
                        .toList();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "count", data.size(),
                "data", data
        ));
    }

    public ResponseEntity<?> getKitScans(Long eventId, User user) {

        eventAuthorizationService.authorize(eventId, user);

        Event event = eventRepository.findById(eventId)
                .orElseThrow();

        List<CheckpointLogResponse> data =
                checkpointLogRepository
                        .findByCheckpoint_TypeAndEvent(CheckpointType.KIT, event)
                        .stream()
                        .map(this::toDto)
                        .toList();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "kitCollectedCount", data.size(),
                "data", data
        ));
    }

    public ResponseEntity<?> getHallEntries(Long eventId, User user) {

        eventAuthorizationService.authorize(eventId, user);

        Event event = eventRepository.findById(eventId)
                .orElseThrow();

        List<CheckpointLogResponse> data =
                checkpointLogRepository
                        .findByCheckpoint_TypeAndEvent(CheckpointType.HALL, event)
                        .stream()
                        .map(this::toDto)
                        .toList();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "entries", data.size(),
                "data", data
        ));
    }

    public ResponseEntity<?> getAllLogs(Long eventId, User user) {

        eventAuthorizationService.authorize(eventId, user);

        Event event = eventRepository.findById(eventId)
                .orElseThrow();

        List<CheckpointLogResponse> data =
                checkpointLogRepository.findByEvent(event)
                        .stream()
                        .map(this::toDto)
                        .toList();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "totalLogs", data.size(),
                "data", data
        ));
    }

    private CheckpointLogResponse toDto(CheckpointLog log) {

        return CheckpointLogResponse.builder()
                .attendeeName(log.getRegistrationEntry().getName())
                .attendeeEmail(log.getRegistrationEntry().getEmail())
                .checkpointName(log.getCheckpoint().getName())
                .checkpointType(log.getCheckpoint().getType())
                .scannedBy(log.getScannedBy().getName())
                .scannedAt(log.getScannedAt())
                .build();
    }


    @Transactional
    public ResponseEntity<?> getLogs(
            Long eventId,
            CheckpointType type,
            Long checkpointId,
            LocalDate fromDate,
            LocalDate toDate,
            User user
    ) {

        eventAuthorizationService.authorize(eventId, user);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        List<CheckpointLog> logs;

        if (checkpointId != null) {
            Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
                    .orElseThrow(() -> new RuntimeException("Checkpoint not found"));
            logs = checkpointLogRepository.findByCheckpointAndEvent(checkpoint, event);

        } else if (type != null) {
            logs = checkpointLogRepository.findByCheckpoint_TypeAndEvent(type, event);
        } else {
            logs = checkpointLogRepository.findByEvent(event);
        }

        if (fromDate != null || toDate != null) {
            logs = logs.stream()
                    .filter(log -> {
                        LocalDate date = log.getScannedAt().toLocalDate();
                        boolean after = fromDate == null || !date.isBefore(fromDate);
                        boolean before = toDate == null || !date.isAfter(toDate);
                        return after && before;
                    })
                    .toList();
        }

        List<CheckpointLogResponse> response =
                logs.stream().map(this::toDto).toList();

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "count", response.size(),
                "data", response
        ));
    }
}
