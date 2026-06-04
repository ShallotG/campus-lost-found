package com.campus.lostfound.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.dto.request.MatchConfirmRequest;
import com.campus.lostfound.dto.response.MatchConfirmResponse;

public interface MatchConfirmService {

    /**
     * 失主确认匹配
     */
    MatchConfirmResponse confirm(MatchConfirmRequest request, Long userId);

    /**
     * 失主查看个人确认记录
     */
    Page<MatchConfirmResponse> getMyConfirms(int page, int size, Long userId);
}
