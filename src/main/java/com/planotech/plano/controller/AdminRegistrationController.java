package com.planotech.plano.controller;

import com.planotech.plano.auth.UserPrincipal;
import com.planotech.plano.service.EventService;
import com.planotech.plano.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminRegistrationController {

    @Autowired
    RegistrationService registrationService;

    @Autowired
    EventService eventService;

    @GetMapping("/events/{eventId}/registrations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','USER')")
    public ResponseEntity<?> getRegistrations(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return registrationService.getRegistrations(eventId, page, size, search, userPrincipal.getUser());
    }


}
