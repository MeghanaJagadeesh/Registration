package com.planotech.plano.service;

import com.planotech.plano.model.Event;
import com.planotech.plano.model.RegistrationEntry;
import com.planotech.plano.model.RegistrationForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class RegistrationEmailVariableService {

    @Autowired
    FormSectionService formSectionService;

    private static final String DEFAULT_EVENT_LOGO =
            "https://aws.quantumparadigm.in/documents/f2f77d_planotech_logo_black.png";

    public Map<String, Object> buildRegistrationEmailVariables(
            Event event,
            RegistrationForm form,
            RegistrationEntry entry
    ) {

        Map<String, Object> vars = new HashMap<>();

        vars.put("userName", entry.getName());
        vars.put("userEmail", entry.getEmail());

        vars.put("eventName", event.getName());
        vars.put("eventDescription", event.getDescription());
        vars.put("eventLocation", event.getLocation());
        vars.put("badgeCode", entry.getBadgeCode());

        vars.put("eventStartDate",
                event.getStartDate() != null
                        ? event.getStartDate().toString()
                        : ""
        );

        vars.put("qrUrl", entry.getQrUrl());

        Map<String, Object> mailVars =
                formSectionService.getMailVariables(form.getFormId());

        mailVars.putIfAbsent("eventWebsite", "#");
        mailVars.putIfAbsent(
                "supportEmail",
                "support@" + event.getEventKey().toLowerCase() + ".com"
        );
        mailVars.putIfAbsent("supportPhone", "");

        if (!mailVars.containsKey("footerLogo")
                || mailVars.get("footerLogo") == null) {

            mailVars.put("footerLogo",
                    event.getLogoUrl() != null
                            ? event.getLogoUrl()
                            : DEFAULT_EVENT_LOGO
            );
        }

        vars.putAll(mailVars);

        String eventLogo =
                (event.getLogoUrl() != null && !event.getLogoUrl().trim().isEmpty())
                        ? event.getLogoUrl().trim()
                        : DEFAULT_EVENT_LOGO;

        vars.put("eventLogo", eventLogo);

        log.info("Event Logo URL set to: {}", eventLogo);
        log.info("All variables in map: {}", vars.keySet());

        return vars;
    }
}
