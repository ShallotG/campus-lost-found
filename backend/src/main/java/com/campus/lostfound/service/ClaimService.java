package com.campus.lostfound.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.dto.request.ClaimRequest;
import com.campus.lostfound.dto.response.ClaimRecordResponse;

public interface ClaimService {

    /**
     * 登记领取
     */
    ClaimRecordResponse claim(ClaimRequest request, Long handlerId);

    /**
     * 领取记录列表（分页）
     */
    Page<ClaimRecordResponse> list(int page, int size, Long lostItemId);
}
