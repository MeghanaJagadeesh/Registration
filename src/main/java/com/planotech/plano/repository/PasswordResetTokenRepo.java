package com.planotech.plano.repository;

import com.planotech.plano.model.PasswordResetToken;
import com.planotech.plano.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepo
        extends JpaRepository<PasswordResetToken, Long> {

    @Query("""
        SELECT o FROM PasswordResetToken o
        WHERE o.user = :user AND o.used = false
    """)
    Optional<PasswordResetToken> findActiveByUser(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("""
        UPDATE PasswordResetToken o
        SET o.used = true
        WHERE o.user = :user AND o.used = false
    """)
    void invalidateByUser(@Param("user") User user);
}
