package com.planotech.plano.repository;

import com.planotech.plano.model.RegistrationEntry;
import com.planotech.plano.request.BadgeFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RegistrationEntryCustomRepository {

    Page<RegistrationEntry> findBadgesWithFilters(
            Long eventId,
            BadgeFilterRequest request,
            Pageable pageable
    );

    public Page<RegistrationEntry> searchBadges(
            Long eventId,
            String search,
            Pageable pageable
    );
}
