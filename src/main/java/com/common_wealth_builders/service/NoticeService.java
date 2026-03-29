package com.common_wealth_builders.service;

import com.common_wealth_builders.dto.request.NoticeRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import org.springframework.data.domain.Pageable;

public interface NoticeService {

        GenericResponse createNotice(NoticeRequest request, String authorEmail);

        GenericResponse getAllNotices(Pageable pageable);

        GenericResponse getPublicNotices();

        GenericResponse getNoticeById(Long id);

        GenericResponse updateNotice(Long id, NoticeRequest request);

        GenericResponse deleteNotice(Long id);

        GenericResponse publishNotice(Long id);

        GenericResponse unpublishNotice(Long id);

        GenericResponse pinNotice(Long id);

        GenericResponse unpinNotice(Long id);

        GenericResponse searchNotices(String query);

    GenericResponse getUnreadCount(String name); // for logged-in user

    GenericResponse markAsRead(Long id, String name);

}