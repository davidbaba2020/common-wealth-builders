package com.common_wealth_builders.service;

import com.common_wealth_builders.dto.request.NoticeRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import org.springframework.data.domain.Pageable;

public interface NoticeService {
    GenericResponse createNotice(NoticeRequest request, String authorEmail);
    GenericResponse getAllNotices(Pageable pageable);
    GenericResponse getPublicNotices(Pageable pageable);
    GenericResponse getNoticeById(Long id);
    GenericResponse updateNotice(Long id, NoticeRequest request, String updatedBy);
    GenericResponse deleteNotice(Long id, String deletedBy);
    GenericResponse publishNotice(Long id);
    GenericResponse unpublishNotice(Long id);
    GenericResponse pinNotice(Long id);
    GenericResponse unpinNotice(Long id);
    GenericResponse searchNotices(String type, Boolean isPublished, String search, Pageable pageable);
}