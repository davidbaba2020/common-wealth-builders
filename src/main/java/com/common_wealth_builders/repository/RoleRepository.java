package com.common_wealth_builders.repository;

import com.common_wealth_builders.entity.Role;
import com.common_wealth_builders.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(RoleType name);
    
    boolean existsByName(RoleType name);
    
    List<Role> findByIsActiveTrue();
    
    Page<Role> findByIsDeletedFalse(Pageable pageable);
    
    @Query("SELECT r FROM Role r WHERE r.isDeleted = false AND r.isActive = true")
    List<Role> findAllActiveRoles();
    
    @Query("SELECT r FROM Role r WHERE r.isSystemRole = true")
    List<Role> findSystemRoles();
    
    @Query("SELECT r FROM Role r WHERE " +
           "LOWER(r.displayName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Role> searchRoles(@Param("search") String search, Pageable pageable);
}