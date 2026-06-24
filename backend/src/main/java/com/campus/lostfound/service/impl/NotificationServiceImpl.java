package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.BusinessException;
import com.campus.lostfound.dto.response.NotificationResponse;
import com.campus.lostfound.entity.Notification;
import com.campus.lostfound.mapper.NotificationMapper;
import com.campus.lostfound.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void create(Long userId, String type, String title, String content, Long relatedItemId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedItemId(relatedItemId);
        notification.setIsRead(false);
        notificationMapper.insert(notification);
        log.info("通知创建成功: userId={}, type={}, title={}", userId, type, title);
    }

    @Override
    public Page<NotificationResponse> list(int page, int size, Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);

        Page<Notification> notifPage = notificationMapper.selectPage(new Page<>(page, size), wrapper);
        return (Page<NotificationResponse>) notifPage.convert(n -> NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .content(n.getContent())
                .relatedItemId(n.getRelatedItemId())
                .isRead(n.getIsRead())
                .createTime(n.getCreateTime())
                .build());
    }

    @Override
    public long countUnread(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false);
        return notificationMapper.selectCount(wrapper);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(404, "通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此通知");
        }
        Notification update = new Notification();
        update.setId(notificationId);
        update.setIsRead(true);
        notificationMapper.updateById(update);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false);
        Notification update = new Notification();
        update.setIsRead(true);
        // update all unread for this user
        notificationMapper.update(update, wrapper);
    }
}
