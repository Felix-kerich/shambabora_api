package com.app.shambabora.config;

import com.app.shambabora.entity.Role;
import com.app.shambabora.entity.User;
import com.app.shambabora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedAdminUser();
        seedExtensionOfficer();
    }

    private void seedAdminUser() {
        // Check if admin user already exists
        if (userRepository.findByUsername("admin").isPresent()) {
            log.info("Admin user already exists, skipping creation");
            return;
        }

        try {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@shambabora.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator")
                    .phoneNumber("+254700000000")
                    .roles(Set.of(Role.ADMIN))
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
            log.info("✅ Admin user created successfully!");
            log.info("   Username: admin");
            log.info("   Password: admin123");
            log.info("   Email: admin@shambabora.com");
        } catch (Exception e) {
            log.error("❌ Error creating admin user: ", e);
        }
    }

    private void seedExtensionOfficer() {
        // Check if extension officer already exists
        if (userRepository.findByUsername("extension_officer").isPresent()) {
            log.info("Extension officer already exists, skipping creation");
            return;
        }

        try {
            User extensionOfficer = User.builder()
                    .username("extension_officer")
                    .email("officer@shambabora.com")
                    .password(passwordEncoder.encode("officer123"))
                    .fullName("Agricultural Extension Officer")
                    .phoneNumber("+254700000001")
                    .roles(Set.of(Role.EXTENSION_OFFICER))
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            userRepository.save(extensionOfficer);
            log.info("✅ Extension officer created successfully!");
            log.info("   Username: extension_officer");
            log.info("   Password: officer123");
            log.info("   Email: officer@shambabora.com");
        } catch (Exception e) {
            log.error("❌ Error creating extension officer: ", e);
        }
    }
}
