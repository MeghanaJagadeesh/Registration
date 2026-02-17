package com.planotech.plano.repository;

import com.planotech.plano.model.EmailContentSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailContentSettingsRepository extends JpaRepository<EmailContentSettings, Long> {
    Optional<EmailContentSettings> findByEvent_EventId(Long eventId);
}
