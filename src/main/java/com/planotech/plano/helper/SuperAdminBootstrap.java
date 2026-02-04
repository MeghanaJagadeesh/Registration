package com.planotech.plano.helper;

import com.planotech.plano.enums.AccountStatus;
import com.planotech.plano.enums.PlatformRole;
import com.planotech.plano.model.User;
import com.planotech.plano.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SuperAdminBootstrap implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        boolean exists = userRepository.existsByPlatformRole(PlatformRole.ROLE_SUPER_ADMIN);

        if (!exists) {
            User superAdmin = new User();
            superAdmin.setName("System Admin");
            superAdmin.setEmail("admin@quantumparadigm.in");
            superAdmin.setPassword(passwordEncoder.encode("Admin@123"));
            superAdmin.setPlatformRole(PlatformRole.ROLE_SUPER_ADMIN);
            superAdmin.setStatus(AccountStatus.ACTIVE);
            superAdmin.setCreatedAt(LocalDateTime.now());

            userRepository.save(superAdmin);

            System.out.println("✅ Super Admin created: admin@system.com / Admin@123");
        }
    }
}