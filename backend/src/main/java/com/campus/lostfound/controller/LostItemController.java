package com.campus.lostfound.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.common.Result;
import com.campus.lostfound.dto.request.LostItemCreateRequest;
import com.campus.lostfound.dto.request.LostItemUpdateRequest;
import com.campus.lostfound.dto.response.ImageUploadResponse;
import com.campus.lostfound.dto.response.LostItemResponse;
import com.campus.lostfound.service.LostItemService;
import com.campus.lostfound.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "拾物管理", description = "拾物CRUD、图片上传、YOLO检测")
@RestController
@RequestMapping("/api/lost-items")
@RequiredArgsConstructor
public class LostItemController {

    private final LostItemService lostItemService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "上传图片并触发YOLO检测")
    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority('ROLE_STAFF', 'ROLE_ADMIN')")
    public Result<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.ok("上传成功", lostItemService.uploadImage(file));
    }

    @Operation(summary = "仅YOLO检测（不保存文件，不触发OSS上传）")
    @PostMapping("/detect")
    @PreAuthorize("hasAnyAuthority('ROLE_STAFF', 'ROLE_ADMIN')")
    public Result<ImageUploadResponse> detectOnly(@RequestParam("file") MultipartFile file) {
        return Result.ok(lostItemService.detectOnly(file));
    }

    @Operation(summary = "创建拾物记录")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_STAFF', 'ROLE_ADMIN')")
    public Result<LostItemResponse> create(@Valid @RequestBody LostItemCreateRequest request,
                                           @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        return Result.ok("创建成功", lostItemService.create(request, userId));
    }

    @Operation(summary = "拾物列表（分页+筛选）")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_STAFF', 'ROLE_ADMIN')")
    public Result<PageResult<LostItemResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        Page<LostItemResponse> itemPage = lostItemService.list(page, size, status, category, keyword);
        PageResult<LostItemResponse> pageResult = new PageResult<>(
                itemPage.getRecords(), itemPage.getTotal(),
                itemPage.getCurrent(), itemPage.getSize(), itemPage.getPages());
        return Result.ok(pageResult);
    }

    @Operation(summary = "拾物详情")
    @GetMapping("/{id}")
    public Result<LostItemResponse> getById(@PathVariable Long id) {
        return Result.ok(lostItemService.getById(id));
    }

    @Operation(summary = "更新拾物（仅未领取状态可编辑）")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_STAFF', 'ROLE_ADMIN')")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody LostItemUpdateRequest request,
                               @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        lostItemService.update(id, request, userId);
        return Result.ok("更新成功");
    }

    @Operation(summary = "删除拾物（管理员）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        lostItemService.delete(id);
        return Result.ok("删除成功");
    }
}
