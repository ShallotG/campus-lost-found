package com.campus.lostfound.dto.request;

import lombok.Data;

@Data
public class LostItemUpdateRequest {

    private String category;

    private String storageLocation;

    private String remark;
}
