package com.planotech.plano.repository;

import com.planotech.plano.model.BadgeConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BadgeConfigurationRepository extends JpaRepository<BadgeConfiguration, Long> {
    Optional<BadgeConfiguration> findByEventEventId(Long eventId);
}
