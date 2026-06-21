package com.campus.lostfound.service;

import com.campus.lostfound.dto.response.DashboardResponse;

public interface DashboardService {

    /**
     * 获取数据看板统计
     */
    DashboardResponse getStats();
}
