package com.common_wealth_builders.repository;

import com.common_wealth_builders.entity.AuditTrail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {
    Page<AuditTrail> findByUserId(Long userId, Pageable pageable);
    Page<AuditTrail> findByModule(String module, Pageable pageable);
    Page<AuditTrail> findByAction(String action, Pageable pageable);
    
    @Query("SELECT a FROM AuditTrail a WHERE " +
           "(:userId IS NULL OR a.user.id = :userId) AND " +
           "(:module IS NULL OR a.module = :module) AND " +
           "(:action IS NULL OR a.action = :action)")
    Page<AuditTrail> searchAuditTrails(
        @Param("userId") Long userId,
        @Param("module") String module,
        @Param("action") String action,
        Pageable pageable
    );
}