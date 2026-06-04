package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lost_item")
public class LostItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String imageUrl;

    private String category;

    private Double categoryConfidence;

    private String storageLocation;

    private String remark;

    private String status; // UNCLAIMED / CLAIMED

    private String textEmbedding;

    private String imagePhash;

    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
