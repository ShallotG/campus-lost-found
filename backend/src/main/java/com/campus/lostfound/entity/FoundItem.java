package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("found_item")
public class FoundItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String itemName;

    private String category;

    private String color;

    private String brand;

    private String description;

    private LocalDateTime lostTime;

    private String lostLocation;

    private String contactPhone;

    private String status; // OPEN / MATCHED / CLOSED

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
