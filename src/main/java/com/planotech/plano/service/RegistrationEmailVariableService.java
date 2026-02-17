package com.planotech.plano.service;

import com.planotech.plano.model.EmailContentSettings;
import com.planotech.plano.model.Event;
import com.planotech.plano.model.RegistrationEntry;
import com.planotech.plano.model.RegistrationForm;
import com.planotech.plano.repository.EmailContentSettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class RegistrationEmailVariableService {

    @Autowired
    private EmailContentSettingsRepository emailContentSettingsRepository;

    public Map<String, Object> buildRegistrationEmailVariables(
            Event event,
            RegistrationForm form,
            RegistrationEntry entry
    ) {
        EmailContentSettings settings =
                emailContentSettingsRepository
                        .findByEvent_EventId(event.getEventId())
                        .orElse(null);

        Map<String, Object> vars = new HashMap<>();

        vars.put("userName", entry.getName());
        vars.put("userEmail", entry.getEmail());
        vars.put("badgeCode", entry.getBadgeCode());
        vars.put("eventName", event.getName());
        vars.put("eventLocation", event.getLocation());
        vars.put("eventDate", event.getStartDate());
        vars.put("eventLogo", event.getLogoUrl());
        vars.put("eventDescription", event.getDescription());

        vars.put("supportEmail",
                settings != null && settings.getSupportEmail() != null
                        ? settings.getSupportEmail()
                        : "support@" + event.getEventKey().toLowerCase() + ".com"
        );

        vars.put("supportPhone",
                settings != null && settings.getSupportPhone() != null
                        ? settings.getSupportPhone()
                        : ""
        );

        vars.put("eventWebsite",
                settings != null && settings.getEventWebsite() != null
                        ? settings.getEventWebsite()
                        : "#"
        );

        return vars;
    }
}
