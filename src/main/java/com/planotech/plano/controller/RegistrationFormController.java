package com.planotech.plano.controller;

import com.planotech.plano.auth.UserPrincipal;
import com.planotech.plano.model.User;
import com.planotech.plano.request.FormFieldRequest;
import com.planotech.plano.service.RegistrationFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/form")
public class RegistrationFormController {

    @Autowired
    RegistrationFormService formService;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','USER')")
    @PostMapping("/event/{eventId}/draft")
    public ResponseEntity<?> createDraft(@PathVariable Long eventId, @AuthenticationPrincipal UserPrincipal user) {
        return formService.createDraft(eventId, user.getUser());
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getActiveForm(@PathVariable Long eventId, @AuthenticationPrincipal UserPrincipal user) {
        return formService.getFormByEvent(eventId, user.getUser());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','USER')")
    @PutMapping("/{formId}/draft")
    public ResponseEntity<?> saveDraft(@PathVariable Long formId, @RequestBody List<FormFieldRequest> fields, @AuthenticationPrincipal UserPrincipal user) {
        formService.saveDraft(formId, fields, user.getUser());
        return ResponseEntity.ok(Map.of(
                "message", "Draft saved",
                "code", 200,
                "status", "success"
        ));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','USER')")
    @PostMapping("/{formId}/publish")
    public ResponseEntity<?> publish(@PathVariable Long formId, @AuthenticationPrincipal UserPrincipal user) {
        return formService.publish(formId, user.getUser());

    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','USER')")
    @GetMapping("/event/{eventId}/versions")
    public ResponseEntity<?> getAllFormVersions(@PathVariable Long eventId, @AuthenticationPrincipal UserPrincipal userDetails) {
        return formService.getAllVersions(eventId, userDetails.getUser());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','USER')")
    @GetMapping("/{formId}")
    public ResponseEntity<?> getFormById(@PathVariable Long formId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return formService.getFormById(formId, userPrincipal.getUser());
    }
}