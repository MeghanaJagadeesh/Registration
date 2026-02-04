package com.planotech.plano.controller;

import com.planotech.plano.auth.UserPrincipal;
import com.planotech.plano.request.CreateEventRequest;
import com.planotech.plano.request.CreateUserRequest;
import com.planotech.plano.service.AdminService;
import com.planotech.plano.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    EventService eventService;

    @Autowired
    AdminService adminService;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','USER')")
    @GetMapping("/events")
    public ResponseEntity<?> viewEvent(@AuthenticationPrincipal UserPrincipal userDetails) {
        return eventService.getEventsByUserRole(userDetails.getUser());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','USER')")
    @PostMapping("/events/{eventId}/assign-user")
    public ResponseEntity<?> assignEventUser(@PathVariable Long eventId,
                                             @RequestBody CreateUserRequest userRequest,
                                             @AuthenticationPrincipal UserPrincipal userDetails) {
       return adminService.assignEventUser(eventId, userRequest, userDetails.getUser());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','USER')")
    @PutMapping("/events/{eventId}")
    public ResponseEntity<?> modifyEvent(@PathVariable Long eventId,
                                             @RequestBody CreateEventRequest eventRequest,
                                             @AuthenticationPrincipal UserPrincipal userDetails) {
       return adminService.modifyEvent(eventId, eventRequest, userDetails.getUser());
    }

//    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','USER')")
//    @GetMapping("/events/{eventId}/entry")
//    public ResponseEntity<?> getEntry(@PathVariable Long eventId,
//                                         @AuthenticationPrincipal UserPrincipal userDetails) {
//        return adminService.getRegisteredUsers(eventId, userDetails.getUser());

}
