package com.common_wealth_builders.service.impl;

import com.common_wealth_builders.dto.request.NoticeRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.dto.response.NoticeResponse;
import com.common_wealth_builders.entity.Notice;
import com.common_wealth_builders.entity.User;
import com.common_wealth_builders.exception.ResourceNotFoundException;
import com.common_wealth_builders.repository.NoticeRepository;
import com.common_wealth_builders.repository.UserRepository;
import com.common_wealth_builders.service.NoticeService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    public NoticeServiceImpl(NoticeRepository noticeRepository, UserRepository userRepository) {
        this.noticeRepository = noticeRepository;
        this.userRepository = userRepository;
    }

    private NoticeResponse mapToResponse(Notice notice) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .type(notice.getType())
                .isPublished(notice.isPublished())
                .publishDate(notice.getPublishDate())
                .expiryDate(notice.getExpiryDate())
                .isPinned(notice.isPinned())
                .authorEmail(notice.getAuthor().getEmail())
                .attachmentUrl(notice.getAttachmentUrl())
                .viewCount(notice.getViewCount())
                .createdDate(notice.getCreatedDate())
                .build();
    }

    @Override
    @Transactional
    public GenericResponse createNotice(NoticeRequest request, String userEmail) {
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType())
                .expiryDate(request.getExpiryDate())
                .isPinned(request.getIsPinned() != null ? request.getIsPinned() : false)
                .attachmentUrl(request.getAttachmentUrl())
                .viewCount(0)
                .author(author) // ✅ this was missing
                .build();

        Notice saved = noticeRepository.save(notice);

        return GenericResponse.<NoticeResponse>builder()
                .isSuccess(true)
                .message("Notice created successfully")
                .data(mapToResponse(saved))
                .httpStatus(HttpStatus.CREATED)
                .build();
    }


    @Override
    public GenericResponse getAllNotices(Pageable pageable) {

        Page<NoticeResponse> page = noticeRepository
                .findAllByIsDeletedFalse(pageable)
                .map(this::mapToResponse);

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Notices retrieved")
                .data(page)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    public GenericResponse getPublicNotices() {

        List<NoticeResponse> notices = noticeRepository
                .findByIsPublishedTrueAndIsDeletedFalse()
                .stream()
                .filter(Notice::isActive)
                .map(this::mapToResponse)
                .toList();

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Public notices retrieved")
                .data(notices)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    public GenericResponse getNoticeById(Long id) {

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found"));

        notice.incrementViewCount();
        noticeRepository.save(notice);

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Notice retrieved successfully")
                .data(mapToResponse(notice))
                .httpStatus(HttpStatus.OK)
                .build();
    }


    @Override
    public GenericResponse getUnreadCount(String name) {
        Optional<User> user1 = userRepository.findByEmail(name);
        if (user1.isEmpty()){
            throw new ResourceNotFoundException("User not found");
        }
        User user = user1.get();
        int count = noticeRepository.countByIsPublishedTrueAndReadByUsersNotContaining(user);
        return GenericResponse.builder()
                .isSuccess(true)
                .data(count)
                .message("Unread notice count retrieved")
                .build();
    }

    @Override
    @Transactional
    public GenericResponse markAsRead(Long id, String name) {
        Optional<User> user1 = userRepository.findByEmail(name);
        if (user1.isEmpty()){
            throw new ResourceNotFoundException("User not found");
        }
        User user = user1.get();
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notice not found"));
        notice.markAsRead(user);
        noticeRepository.save(notice);
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Notice marked as read")
                .build();
    }

    @Override
    public GenericResponse updateNotice(Long id, NoticeRequest request) {

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found"));

        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setType(request.getType());
        notice.setPinned(request.getIsPinned());
        notice.setAttachmentUrl(request.getAttachmentUrl());
        notice.setExpiryDate(request.getExpiryDate());

        noticeRepository.save(notice);

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Notice updated successfully")
                .data(mapToResponse(notice))
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    public GenericResponse deleteNotice(Long id) {

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found"));

        notice.setDeleted(true);
        noticeRepository.save(notice);

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Notice deleted successfully")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    public GenericResponse publishNotice(Long id) {

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found"));

        notice.publish();
        noticeRepository.save(notice);

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Notice published")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    public GenericResponse unpublishNotice(Long id) {

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found"));

        notice.unpublish();
        noticeRepository.save(notice);

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Notice unpublished")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    public GenericResponse pinNotice(Long id) {

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found"));

        notice.pin();
        noticeRepository.save(notice);

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Notice pinned")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    public GenericResponse unpinNotice(Long id) {

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found"));
        notice.unpin();
        noticeRepository.save(notice);

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Notice unpinned")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    @Transactional
    public GenericResponse searchNotices(String query) {

        List<NoticeResponse> notices = noticeRepository.search(query)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Search results")
                .data(notices)
                .httpStatus(HttpStatus.OK)
                .build();
    }
}
