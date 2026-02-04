package com.planotech.plano.service;

import com.planotech.plano.enums.EventRole;
import com.planotech.plano.enums.PlatformRole;
import com.planotech.plano.exception.AccessDeniedException;
import com.planotech.plano.exception.ResourceNotFoundException;
import com.planotech.plano.model.Event;
import com.planotech.plano.model.EventUser;
import com.planotech.plano.model.User;
import com.planotech.plano.repository.EventRepository;
import com.planotech.plano.repository.EventUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventAuthorizationService {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventUserRepository eventUserRepository;

    public EventUser authorize(Long eventId, User user) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // SUPER ADMIN → everything
        if (user.getPlatformRole() == PlatformRole.ROLE_SUPER_ADMIN) {
            return null;
        }

        // ORG ADMIN → only company events
        if (user.getPlatformRole() == PlatformRole.ROLE_ORG_ADMIN) {
            if (event.getCompany() == null ||
                    !event.getCompany().getCompanyId().equals(user.getCompany().getCompanyId())) {
                throw new AccessDeniedException("Not your organization event");
            }
            return null;
        }

        // EVENT ROLE REQUIRED
        return eventUserRepository
                .findByEvent_EventIdAndUser_UserIdAndActiveTrue(eventId, user.getUserId())
                .orElseThrow(() ->
                        new AccessDeniedException("You are not assigned to this event")
                );
    }

    public void validateDraftPermission(User user, EventUser eventUser) {

        if (user.getPlatformRole() == PlatformRole.ROLE_SUPER_ADMIN ||
                user.getPlatformRole() == PlatformRole.ROLE_ORG_ADMIN) {
            return;
        }

        if (eventUser != null && eventUser.getRole() == EventRole.ROLE_EVENT_ADMIN) {
            return;
        }

        throw new AccessDeniedException("You are not allowed to create or edit draft forms");
    }

}
