package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String type;   // MATCH_CONFIRM / ITEM_CLAIMED / SYSTEM

    private String title;

    private String content;

    private Long relatedItemId;

    private Boolean isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
