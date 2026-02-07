package com.common_wealth_builders.utils;

import com.common_wealth_builders.entity.Role;
import com.common_wealth_builders.entity.User;
import com.common_wealth_builders.entity.UserRole;
import com.common_wealth_builders.enums.RoleType;
import com.common_wealth_builders.repository.RoleRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AuthUtils {

    // private constructor to prevent instantiation
    private AuthUtils() {}

    // ============================
    // 1️⃣ Get currently logged-in principal (UserDetails)
    // ============================
    public static Optional<Object> getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.ofNullable(auth.getPrincipal());
    }

    // ============================
// 2️⃣ Get currently logged-in username/email
// ============================
    public static String getLoggedInUsername() {
        return getPrincipal()
                .map(p -> {
                    if (p instanceof User) {
                        return ((User) p).getEmail();
                    } else {
                        return p.toString();
                    }
                })
                // ✅ Fallback to SYSTEM if not authenticated
                .orElse("SYSTEM");
    }

    // ============================
// 2️⃣ Get currently logged-in full name or fallback to "SYSTEM"
// ============================
    public static String getLoggedInNames() {
        try {
            return getPrincipal()
                    .map(p -> {
                        if (p instanceof User user) {
                            String fullName = Stream.of(user.getFirstname(), user.getLastname())
                                    .filter(Objects::nonNull)
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .collect(Collectors.joining(" "));
                            return fullName.isEmpty() ? "SYSTEM" : fullName;
                        } else {
                            return p.toString();
                        }
                    })
                    .orElse("SYSTEM");
        } catch (Exception e) {
            return "SYSTEM";
        }
    }

    // ============================
    // 3️⃣ Check if the currently logged-in user has a role
    // ============================
    public static boolean hasRole(User user, RoleType roleType) {
        if (user == null || user.getUserRoles() == null) return false;

        return user.getUserRoles().stream()
                .filter(UserRole::isActive)
                .map(ur -> ur.getRole().getName())
                .anyMatch(rt -> rt == roleType);
    }

    // ============================
    // 4️⃣ Convert Set<RoleType> → Set<UserRole>
    // ============================
    public static Set<UserRole> createUserRoles(User user, Set<RoleType> roles, String assignedBy, RoleRepository roleRepository) {
        if (roles == null || roles.isEmpty()) return new HashSet<>();

        return roles.stream()
                .map(roleType -> {
                    Role role = roleRepository.findByName(roleType)
                            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleType));

                    UserRole ur = new UserRole();
                    ur.setUser(user);
                    ur.setRole(role);
                    ur.setAssignedBy(assignedBy);
                    ur.setAssignedDate(LocalDateTime.now());
                    ur.setActive(true);
                    return ur;
                })
                .collect(Collectors.toSet());
    }
}
