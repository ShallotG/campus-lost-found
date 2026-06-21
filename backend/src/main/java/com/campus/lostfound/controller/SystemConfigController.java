package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.entity.SystemConfig;
import com.campus.lostfound.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "系统配置", description = "管理员管理系统配置项")
@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @Operation(summary = "配置列表")
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<List<SystemConfig>> list() {
        return Result.ok(systemConfigService.listAll());
    }

    @Operation(summary = "更新配置")
    @PutMapping("/{key}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> update(@PathVariable String key, @RequestBody Map<String, String> body) {
        systemConfigService.updateValue(key, body.get("configValue"));
        return Result.ok("更新成功");
    }
}
