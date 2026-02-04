package com.planotech.plano.repository;

import com.planotech.plano.enums.PlatformRole;
import com.planotech.plano.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
     Optional<User> findByEmail(String email);
     boolean existsByPlatformRole(PlatformRole role);
}
