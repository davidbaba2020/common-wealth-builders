//package com.common_wealth_builders.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.AuditorAware;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import java.util.Optional;
//
//@Component
//@Slf4j
//public class AuditorAwareImpl implements AuditorAware<String> {
//
//    @Override
//    public Optional<String> getCurrentAuditor() {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//            if (authentication == null || !authentication.isAuthenticated()) {
//                log.debug("No authenticated user found, using SYSTEM as auditor");
//                return Optional.of("SYSTEM");
//            }
//
//            Object principal = authentication.getPrincipal();
//
//            if (principal instanceof UserDetails) {
//                String username = ((UserDetails) principal).getUsername();
//                log.trace("Current auditor from UserDetails: {}", username);
//                return Optional.of(username);
//            }
//
//            if (principal instanceof String) {
//                log.trace("Current auditor from String principal: {}", principal);
//                return Optional.of((String) principal);
//            }
//
//            log.debug("Unknown principal type: {}, using SYSTEM as auditor",
//                     principal.getClass().getName());
//            return Optional.of("SYSTEM");
//
//        } catch (Exception e) {
//            log.error("Error getting current auditor, using SYSTEM as fallback", e);
//            return Optional.of("SYSTEM");
//        }
//    }
//}