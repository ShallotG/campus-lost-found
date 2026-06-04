package com.campus.lostfound.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.common.Result;
import com.campus.lostfound.entity.SystemLog;
import com.campus.lostfound.service.SystemLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统日志", description = "管理员查看API/AI调用日志")
@RestController
@RequestMapping("/api/system-logs")
@RequiredArgsConstructor
public class SystemLogController {

    private final SystemLogService systemLogService;

    @Operation(summary = "日志列表（分页）")
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<PageResult<SystemLog>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String logType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Page<SystemLog> logPage = systemLogService.list(page, size, logType, startDate, endDate);
        PageResult<SystemLog> pageResult = new PageResult<>(
                logPage.getRecords(), logPage.getTotal(),
                logPage.getCurrent(), logPage.getSize(), logPage.getPages());
        return Result.ok(pageResult);
    }
}
