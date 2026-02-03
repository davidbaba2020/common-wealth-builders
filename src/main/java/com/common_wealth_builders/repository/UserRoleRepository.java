package com.common_wealth_builders.repository;

import com.common_wealth_builders.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    List<UserRole> findByUserIdAndIsActiveTrue(Long userId);
    
    List<UserRole> findByRoleIdAndIsActiveTrue(Long roleId);
    
    Page<UserRole> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.id = :roleId AND ur.isActive = true")
    Optional<UserRole> findActiveByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.role.id = :roleId AND ur.isActive = true")
    Long countActiveUsersByRoleId(@Param("roleId") Long roleId);
}
