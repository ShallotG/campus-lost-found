package com.campus.lostfound.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.common.Result;
import com.campus.lostfound.dto.response.NotificationResponse;
import com.campus.lostfound.security.JwtTokenProvider;
import com.campus.lostfound.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "通知中心", description = "用户通知列表、已读标记")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "通知列表")
    @GetMapping
    public Result<PageResult<NotificationResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        Page<NotificationResponse> notifPage = notificationService.list(page, size, userId);
        return Result.ok(PageResult.of(notifPage));
    }

    @Operation(summary = "未读通知数量")
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        long count = notificationService.countUnread(userId);
        return Result.ok(Map.of("count", count));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id,
                                    @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        notificationService.markAsRead(id, userId);
        return Result.ok("已标记为已读");
    }

    @Operation(summary = "全部标记已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        notificationService.markAllAsRead(userId);
        return Result.ok("全部已标记为已读");
    }
}
