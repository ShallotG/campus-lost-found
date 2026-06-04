package com.campus.lostfound.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.entity.SystemLog;

public interface SystemLogService {

    Page<SystemLog> list(int page, int size, String logType, String startDate, String endDate);

    void save(SystemLog log);
}
