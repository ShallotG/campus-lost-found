package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("system_log")
public class SystemLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String logType;    // API / AI / SYSTEM

    private String logLevel;   // INFO / WARN / ERROR

    private String requestUri;

    private String requestMethod;

    private String requestParams;

    private String responseBody;

    private String ipAddress;

    private Long userId;

    private Long durationMs;

    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
