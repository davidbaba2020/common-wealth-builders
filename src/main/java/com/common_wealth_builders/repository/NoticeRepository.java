package com.common_wealth_builders.repository;

import com.common_wealth_builders.entity.Notice;
import com.common_wealth_builders.enums.NoticeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    
    Page<Notice> findByType(NoticeType type, Pageable pageable);
    
    Page<Notice> findByIsPublished(boolean isPublished, Pageable pageable);
    
    Page<Notice> findByAuthorId(Long authorId, Pageable pageable);
    
    @Query("SELECT n FROM Notice n WHERE " +
           "n.isPublished = true AND n.isDeleted = false AND " +
           "(n.expiryDate IS NULL OR n.expiryDate > :now) " +
           "ORDER BY n.isPinned DESC, n.publishDate DESC")
    Page<Notice> findPublicNotices(@Param("now") LocalDateTime now, Pageable pageable);
    
    @Query("SELECT n FROM Notice n WHERE " +
           "n.isPinned = true AND n.isPublished = true AND n.isDeleted = false " +
           "ORDER BY n.publishDate DESC")
    List<Notice> findPinnedNotices();
    
    @Query("SELECT n FROM Notice n WHERE " +
           "(:type IS NULL OR n.type = :type) AND " +
           "(:isPublished IS NULL OR n.isPublished = :isPublished) AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Notice> searchNotices(
        @Param("type") NoticeType type,
        @Param("isPublished") Boolean isPublished,
        @Param("search") String search,
        Pageable pageable
    );
}