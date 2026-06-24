package com.campus.lostfound.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.common.Result;
import com.campus.lostfound.dto.request.FoundItemCreateRequest;
import com.campus.lostfound.dto.response.FoundItemResponse;
import com.campus.lostfound.security.JwtTokenProvider;
import com.campus.lostfound.service.FoundItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "寻物启事", description = "用户发布寻物启事、查看列表")
@RestController
@RequestMapping("/api/found-items")
@RequiredArgsConstructor
public class FoundItemController {

    private final FoundItemService foundItemService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "发布寻物启事")
    @PostMapping
    public Result<FoundItemResponse> create(@Valid @RequestBody FoundItemCreateRequest request,
                                             @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        return Result.ok("发布成功", foundItemService.create(request, userId));
    }

    @Operation(summary = "寻物启事详情")
    @GetMapping("/{id}")
    public Result<FoundItemResponse> getById(@PathVariable Long id) {
        return Result.ok(foundItemService.getById(id));
    }

    @Operation(summary = "我的寻物启事列表")
    @GetMapping("/my")
    public Result<PageResult<FoundItemResponse>> myList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        Page<FoundItemResponse> itemPage = foundItemService.myList(page, size, userId);
        return Result.ok(PageResult.of(itemPage));
    }

    @Operation(summary = "所有寻物启事列表（管理端）")
    @GetMapping
    public Result<PageResult<FoundItemResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        Page<FoundItemResponse> itemPage = foundItemService.list(page, size, null, status);
        return Result.ok(PageResult.of(itemPage));
    }

    @Operation(summary = "关闭寻物启事")
    @PutMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id,
                               @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        foundItemService.updateStatus(id, "CLOSED");
        return Result.ok("已关闭");
    }

    @Operation(summary = "删除寻物启事")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                                @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        foundItemService.delete(id, userId);
        return Result.ok("删除成功");
    }
}
