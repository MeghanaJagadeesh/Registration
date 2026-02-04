package com.planotech.plano.repository;

import com.planotech.plano.enums.FormStatus;
import com.planotech.plano.model.RegistrationForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationFormRepository extends JpaRepository<RegistrationForm, Long> {
    List<RegistrationForm> findByEvent_EventIdOrderByVersionDesc(Long eventId);

    Optional<RegistrationForm> findTopByEvent_EventIdAndStatusOrderByVersionDesc(
            Long eventId, FormStatus status
    );

    Optional<RegistrationForm> findTopByEvent_EventIdOrderByVersionDesc(Long eventId);

    @Query("SELECT COALESCE(MAX(r.version),0) FROM RegistrationForm r WHERE r.event.eventId = :eventId")
    int findMaxVersionByEventId(Long eventId);

    @Modifying
    @Query("""
        UPDATE RegistrationForm r 
        SET r.status = 'ARCHIVED', r.active = false
        WHERE r.event.eventId = :eventId AND r.formId <> :currentId
    """)
    void archiveOtherVersions(Long eventId, Long currentId);
}
