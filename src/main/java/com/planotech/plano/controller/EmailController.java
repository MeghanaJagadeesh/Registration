package com.planotech.plano.controller;

import com.planotech.plano.auth.UserPrincipal;
import com.planotech.plano.request.EmailContentRequest;
import com.planotech.plano.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EmailController {

    @Autowired
    EmailService emailService;


    @PutMapping("/{eventId}/email-content")
    public ResponseEntity<?> updateEmailContent(
            @PathVariable Long eventId,
            @RequestBody EmailContentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return emailService.updateEmailContent(
                eventId,
                request,
                principal.getUser()
        );
    }

    @GetMapping("/{eventId}/email-template")
    public ResponseEntity<?> getEmailTemplate(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return emailService.getEmailTemplate(eventId, principal.getUser());
    }
}
