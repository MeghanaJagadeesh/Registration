package com.planotech.plano.controller;

import com.planotech.plano.auth.UserPrincipal;
import com.planotech.plano.model.User;
import com.planotech.plano.request.BadgeFilterRequest;
import com.planotech.plano.service.BadgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events/{eventId}")
public class BadgeController {

    @Autowired
    BadgeService badgeService;

    @GetMapping("/badges")
    public ResponseEntity<?> listAllBadges(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return badgeService.getAllBadges(eventId, page, size, search, userPrincipal.getUser());
    }

    @PostMapping("/badges/filter")
    public ResponseEntity<?> filterBadges(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestBody BadgeFilterRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return badgeService.filterBadges(eventId, page, size, request, userPrincipal.getUser());
    }

    @GetMapping("/badges/{badgeCode}")
    public ResponseEntity<?> getBadgeByCode(
            @PathVariable Long eventId,
            @PathVariable String badgeCode,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return badgeService.getBadgeByCode(
                eventId,
                badgeCode,
                userPrincipal.getUser()
        );
    }

    @PostMapping("/badges/{entryId}/manual-checkin")
    public ResponseEntity<?> manualCheckIn(
            @PathVariable Long eventId,
            @PathVariable Long entryId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return badgeService.manualCheckIn(
                eventId,
                entryId,
                userPrincipal.getUser()
        );
    }
}
