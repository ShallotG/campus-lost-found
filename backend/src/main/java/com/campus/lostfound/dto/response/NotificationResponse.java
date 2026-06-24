package com.campus.lostfound.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;
    private String type;
    private String title;
    private String content;
    private Long relatedItemId;
    private Boolean isRead;
    private LocalDateTime createTime;
}
