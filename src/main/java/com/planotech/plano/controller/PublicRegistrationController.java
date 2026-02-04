package com.planotech.plano.controller;

import com.planotech.plano.request.RegistrationSubmitRequest;
import com.planotech.plano.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/events")
public class PublicRegistrationController {

    @Autowired
    RegistrationService registrationService;

    @GetMapping("/{eventKey}/form")
    public ResponseEntity<?> getLiveForm(@PathVariable String eventKey) {
        return registrationService.getLiveForm(eventKey);
    }

    @PostMapping("/{eventKey}/register")
    public ResponseEntity<?> submit(@PathVariable String eventKey,@RequestBody RegistrationSubmitRequest request) {
        return registrationService.register(eventKey, request);
    }
}

