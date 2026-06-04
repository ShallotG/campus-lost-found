package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("match_confirm")
public class MatchConfirm {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long lostItemId;

    private Long userId;

    private Double matchScore;

    private LocalDateTime confirmTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
