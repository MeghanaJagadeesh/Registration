package com.planotech.plano.controller;

import com.planotech.plano.auth.UserPrincipal;
import com.planotech.plano.enums.CheckpointType;
import com.planotech.plano.request.CreateCheckpointRequest;
import com.planotech.plano.service.CheckPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/events")
public class CheckPointController {

    @Autowired
    CheckPointService checkPointService;

    @GetMapping("/{eventId}/checkpoints")
    public ResponseEntity<?> getCheckpoints(@PathVariable Long eventId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
       return checkPointService.getCheckPoints(eventId, userPrincipal.getUser());
    }

    @PostMapping("/{eventId}/scan")
    public ResponseEntity<?> scanQr(@PathVariable Long eventId, @RequestParam String badgeCode,
            @RequestParam Long checkpointId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return checkPointService.scanQr(eventId, badgeCode, checkpointId, principal.getUser());


    }

    @PostMapping("/{eventId}/checkpoints")
    public ResponseEntity<?> addCheckpoint(
            @PathVariable Long eventId,
            @RequestBody CreateCheckpointRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) throws Exception {
        return checkPointService.createCheckpoint(eventId, request, principal.getUser()
        );
    }

    @GetMapping("/{eventId}/logs")
    public ResponseEntity<?> getLogs(
            @PathVariable Long eventId,
            @RequestParam(required = false) CheckpointType type,
            @RequestParam(required = false) Long checkpointId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return checkPointService.getLogs(
                eventId, type, checkpointId, fromDate, toDate,
                principal.getUser()
        );
    }

}
