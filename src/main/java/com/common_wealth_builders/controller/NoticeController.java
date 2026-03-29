package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.request.NoticeRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.NoticeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/notices")
@Slf4j
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping
    public ResponseEntity<GenericResponse> createNotice(
            @Valid @RequestBody NoticeRequest request,
            Authentication authentication) {

        GenericResponse response = noticeService.createNotice(request, authentication.getName());

        return new ResponseEntity<>(response, response.getHttpStatus());
    }


    @GetMapping
    public ResponseEntity<GenericResponse> getAll(Pageable pageable) {
        return ResponseEntity.ok(noticeService.getAllNotices(pageable));
    }

    @GetMapping("/public")
    public ResponseEntity<GenericResponse> getPublic() {
        return ResponseEntity.ok(noticeService.getPublicNotices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getNoticeById(id));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<GenericResponse> getUnreadCount(@Valid @RequestBody Authentication authentication) {
        return ResponseEntity.ok(noticeService.getUnreadCount(authentication.getName()));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<GenericResponse> markAsRead(@PathVariable Long id, @Valid @RequestBody Authentication authentication) {
        return ResponseEntity.ok(noticeService.markAsRead(id, authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody NoticeRequest request) {

        return ResponseEntity.ok(noticeService.updateNotice(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.deleteNotice(id));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> publish(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.publishNotice(id));
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> unpublish(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.unpublishNotice(id));
    }

    @PostMapping("/{id}/pin")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> pin(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.pinNotice(id));
    }

    @PostMapping("/{id}/unpin")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> unpin(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.unpinNotice(id));
    }

    @GetMapping("/search")
    public ResponseEntity<GenericResponse> search(@RequestParam String q) {
        return ResponseEntity.ok(noticeService.searchNotices(q));
    }
}
