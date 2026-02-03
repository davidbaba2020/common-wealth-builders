package com.common_wealth_builders.config;

import com.common_wealth_builders.entity.Role;
import com.common_wealth_builders.entity.User;
import com.common_wealth_builders.enums.RoleType;
import com.common_wealth_builders.repository.RoleRepository;
import com.common_wealth_builders.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            log.info("Starting system data initialization...");

            try {
                initializeRoles();
                initializeSuperAdmin();
                log.info("System data initialization completed successfully");
            } catch (Exception e) {
                log.error("System data initialization failed: {}", e.getMessage(), e);
            }
        };
    }

    /* =========================
       ROLE INITIALIZATION
       ========================= */
    private void initializeRoles() {

        initializeRole(
                RoleType.SUPER_ADMIN,
                "Super Administrator",
                "Full system access with all permissions including user management, role assignment, and system configuration",
                true
        );

        initializeRole(
                RoleType.TECH_ADMIN,
                "Technical Administrator",
                "Manage users, view audit trails, manage roles, and access all technical features",
                true
        );

        initializeRole(
                RoleType.FIN_ADMIN,
                "Financial Administrator",
                "Manage payments, verify contributions, manage expenses, and generate financial reports",
                true
        );

        initializeRole(
                RoleType.USER,
                "Regular User",
                "View personal information, contributions, and public notices",
                true
        );
    }

    private void initializeRole(
            RoleType roleType,
            String displayName,
            String description,
            boolean isSystemRole
    ) {
        if (roleRepository.existsByName(roleType)) {
            log.debug("Role already exists: {}, skipping", roleType);
            return;
        }

        Role role = Role.builder()
                .name(roleType)
                .displayName(displayName)
                .description(description)
                .isActive(true)
                .isSystemRole(isSystemRole)
                .code("ROLE_" + roleType.name())
                .build();

        role.setCreatedDate(LocalDateTime.now());
        role.setUpdatedDate(LocalDateTime.now());
        role.setCreatedBy("SYSTEM");
        role.setUpdatedBy("SYSTEM");
        role.setVersion(0L);
        role.setDeleted(false);

        roleRepository.save(role);

        log.info("Initialized role: {}", roleType);
    }

    /* =========================
       SUPER ADMIN INITIALIZATION
       ========================= */
    private void initializeSuperAdmin() {

        String adminEmail = "superadmin@commonwealth.com";

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Super admin already exists, skipping creation");
            return;
        }

        Role superAdminRole = roleRepository.findByName(RoleType.SUPER_ADMIN)
                .orElseThrow(() ->
                        new IllegalStateException("SUPER_ADMIN role not found"));

        User superAdmin = User.builder()
                .firstname("Super")
                .lastname("Admin")
                .email(adminEmail)
                .userName("superadmin")
                .phoneNumber("+2348000000000")
                .password(passwordEncoder.encode("ChangeMe@123"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        superAdmin.setCreatedBy("SYSTEM");
        superAdmin.setUpdatedBy("SYSTEM");
        superAdmin.setCreatedDate(LocalDateTime.now());
        superAdmin.setUpdatedDate(LocalDateTime.now());
        superAdmin.setDeleted(false);
        superAdmin.setVersion(0L);

        // Assign SUPER_ADMIN role
        superAdmin.assignRole(superAdminRole, "SYSTEM");

        userRepository.save(superAdmin);

        log.warn("""
                SUPER ADMIN CREATED
                Email: {}
                Username: superadmin
                Temporary Password: ChangeMe@123
                ⚠️ CHANGE THIS PASSWORD IMMEDIATELY
                """, adminEmail);
    }
}
