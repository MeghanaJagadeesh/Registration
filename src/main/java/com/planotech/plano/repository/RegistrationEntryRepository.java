package com.planotech.plano.repository;

import com.planotech.plano.model.RegistrationEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationEntryRepository extends JpaRepository<RegistrationEntry, Long> {
    boolean existsByEvent_EventIdAndEmail(Long eventId, String email);

    Page<RegistrationEntry> findByEvent_EventId(Long eventId, Pageable pageable);

    @Query("""
        SELECT r FROM RegistrationEntry r
        WHERE r.event.eventId = :eventId
        AND (
            LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(r.email) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(r.phone) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    Page<RegistrationEntry> search(
            @Param("eventId") Long eventId,
            @Param("search") String search,
            Pageable pageable
    );
}
