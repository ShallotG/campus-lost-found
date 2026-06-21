package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("claim_record")
public class ClaimRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long lostItemId;

    private String claimerName;

    private String claimerIdentity;

    private String claimerPhone;

    private Long handlerId;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
