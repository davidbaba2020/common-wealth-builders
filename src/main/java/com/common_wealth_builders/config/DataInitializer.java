package com.common_wealth_builders.config;

import com.common_wealth_builders.entity.Role;
import com.common_wealth_builders.entity.User;
import com.common_wealth_builders.enums.RoleType;
import com.common_wealth_builders.enums.UserType;
import com.common_wealth_builders.repository.RoleRepository;
import com.common_wealth_builders.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
                // 1️⃣ Initialize roles
                initializeRoles();

                // 2️⃣ Initialize users
                initializeUsers();

                log.info("System data initialization completed successfully");
            } catch (Exception e) {
                log.error("System data initialization failed: {}", e.getMessage(), e);
            }
        };
    }

    // =========================
    // 1️⃣ Role Initialization
    // =========================
    private void initializeRoles() {
        Map<RoleType, String[]> roles = Map.of(
                RoleType.SUPER_ADMIN, new String[]{"Super Administrator", "Full system access with all permissions including user management, role assignment, and system configuration"},
                RoleType.TECH_ADMIN, new String[]{"Technical Administrator", "Manage users, view audit trails, manage roles, and access all technical features"},
                RoleType.FIN_ADMIN, new String[]{"Financial Administrator", "Manage payments, verify contributions, manage expenses, and generate financial reports"},
                RoleType.USER, new String[]{"Regular User", "View personal information, contributions, and public notices"}
        );

        roles.forEach((roleType, details) -> createRoleIfNotExists(roleType, details[0], details[1]));
    }

    private void createRoleIfNotExists(RoleType roleType, String displayName, String description) {
        if (roleRepository.existsByName(roleType)) {
            log.debug("Role already exists: {}, skipping", roleType);
            return;
        }

        Role role = Role.builder()
                .name(roleType)
                .displayName(displayName)
                .description(description)
                .isActive(true)
                .isSystemRole(true)
                .code("ROLE_" + roleType.name())
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .createdBy("SYSTEM")
                .updatedBy("SYSTEM")
                .version(0L)
                .isDeleted(false)
                .build();

        roleRepository.save(role);
        log.info("Initialized role: {}", roleType);
    }

    // =========================
    // 2️⃣ User Initialization
    // =========================
    private void initializeUsers() {

        // Super Admin
        createUserIfNotExists(
                "superadmin@commonwealth.com",
                "Super",
                "Admin",
                "superadmin",
                "+2348000000000",
                "ChangeMe@123",
                UserType.SUPER_ADMIN,
                Set.of(RoleType.SUPER_ADMIN)
        );

        // Tech Admin
        createUserIfNotExists(
                "techadmin@commonwealth.com",
                "Tech",
                "Admin",
                "techadmin",
                "+2348000000001",
                "ChangeMe@123",
                UserType.TECH_ADMIN,
                Set.of(RoleType.TECH_ADMIN)
        );

        // Financial Admin
        createUserIfNotExists(
                "finadmin@commonwealth.com",
                "Financial",
                "Admin",
                "finadmin",
                "+2348000000002",
                "ChangeMe@123",
                UserType.FIN_ADMIN,
                Set.of(RoleType.FIN_ADMIN)
        );

        // Regular User
        createUserIfNotExists(
                "user@commonwealth.com",
                "Regular",
                "User",
                "regularuser",
                "+2348000000003",
                "ChangeMe@123",
                UserType.USER,
                Set.of(RoleType.USER)
        );
    }

    private void createUserIfNotExists(
            String email,
            String firstName,
            String lastName,
            String username,
            String phone,
            String password,
            UserType userType,
            Set<RoleType> roles
    ) {
        if (userRepository.existsByEmail(email)) {
            log.info("User {} already exists, skipping creation", email);
            return;
        }

        User user = User.builder()
                .firstname(firstName)
                .lastname(lastName)
                .email(email)
                .userName(username)
                .phoneNumber(phone)
                .password(passwordEncoder.encode(password))
                .userType(userType) // ✅ set user type
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .createdBy("SYSTEM")
                .updatedBy("SYSTEM")
                .version(0L)
                .isDeleted(false)
                .build();

        // Assign roles using the new assignRoles method
        Set<Role> roleEntities = new HashSet<>();
        for (RoleType roleType : roles) {
            Role role = roleRepository.findByName(roleType)
                    .orElseThrow(() -> new IllegalStateException("Role not found: " + roleType));
            roleEntities.add(role);
        }

        user.assignRoles(roleEntities, "SYSTEM");

        userRepository.save(user);

        log.warn("""
                USER CREATED
                Email: {}
                Username: {}
                Temporary Password: {}
                ⚠️ CHANGE THIS PASSWORD IMMEDIATELY
                """, email, username, password);
    }
}
