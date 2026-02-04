package com.planotech.plano.controller;


import com.planotech.plano.auth.UserPrincipal;
import com.planotech.plano.model.User;
import com.planotech.plano.request.CreateEventRequest;
import com.planotech.plano.service.EventService;
import com.planotech.plano.service.SuperAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sa")
public class SuperAdminController {

    @Autowired
    SuperAdminService superAdminService;

    @Autowired
    EventService eventService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/create/superadmin")
    public ResponseEntity<?> createSuperAdmin(@RequestBody User user) {
        return superAdminService.createSuperAdmin(user);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/create/event")
    public ResponseEntity<?> createEvent(@ModelAttribute CreateEventRequest eventRequest, @AuthenticationPrincipal UserPrincipal userDetails) {
        return eventService.createEvent(eventRequest, userDetails.getUser());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/get/eventUsers")
    public ResponseEntity<?> getEventUsers() {
        return superAdminService.getEventUsers();
    }

}
