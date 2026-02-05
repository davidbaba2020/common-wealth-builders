package com.common_wealth_builders.repository;

import com.common_wealth_builders.entity.Expense;
import com.common_wealth_builders.enums.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    Page<Expense> findByCategory(ExpenseCategory category, Pageable pageable);
    
    Page<Expense> findByIsApproved(boolean isApproved, Pageable pageable);
    
    @Query("SELECT e FROM Expense e WHERE " +
           "e.expenseDate BETWEEN :startDate AND :endDate")
    List<Expense> findByExpenseDateBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE " +
           "e.isApproved = true AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumApprovedExpensesBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE " +
           "e.isApproved = true AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "GROUP BY e.category")
    List<Object[]> sumExpensesByCategory(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT e FROM Expense e WHERE " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:isApproved IS NULL OR e.isApproved = :isApproved) AND " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Expense> searchExpenses(
        @Param("category") ExpenseCategory category,
        @Param("isApproved") Boolean isApproved,
        @Param("search") String search,
        Pageable pageable
    );
}