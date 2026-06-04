package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.entity.SystemLog;
import com.campus.lostfound.mapper.SystemLogMapper;
import com.campus.lostfound.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogMapper systemLogMapper;

    @Override
    public Page<SystemLog> list(int page, int size, String logType, String startDate, String endDate) {
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();

        if (logType != null && !logType.isEmpty()) {
            wrapper.eq(SystemLog::getLogType, logType);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(SystemLog::getCreateTime, startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(SystemLog::getCreateTime, endDate + " 23:59:59");
        }
        wrapper.orderByDesc(SystemLog::getCreateTime);

        return systemLogMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public void save(SystemLog log) {
        systemLogMapper.insert(log);
    }
}
