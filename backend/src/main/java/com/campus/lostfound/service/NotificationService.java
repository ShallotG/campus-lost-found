package com.campus.lostfound.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.dto.response.NotificationResponse;

public interface NotificationService {

    void create(Long userId, String type, String title, String content, Long relatedItemId);

    Page<NotificationResponse> list(int page, int size, Long userId);

    long countUnread(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);
}
