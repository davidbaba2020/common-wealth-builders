//package com.common_wealth_builders.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.domain.AuditorAware;
//import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
//
//@Configuration
//@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
//@Slf4j
//public class JpaAuditingConfig {
//
//    @Bean
//    public AuditorAware<String> auditorAwareImpl() {
//        log.info("Initializing JPA Auditing with AuditorAwareImpl");
//        return new AuditorAwareImpl();
//    }
//}