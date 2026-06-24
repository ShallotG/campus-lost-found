package com.campus.lostfound.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.common.Result;
import com.campus.lostfound.dto.response.LostItemResponse;
import com.campus.lostfound.service.LostItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "公开浏览", description = "用户端分类浏览失物，无需登录")
@RestController
@RequestMapping("/api/browse")
@RequiredArgsConstructor
public class BrowseController {

    private final LostItemService lostItemService;

    @Operation(summary = "浏览失物列表（仅未领取）")
    @GetMapping("/items")
    public Result<PageResult<LostItemResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        Page<LostItemResponse> itemPage = lostItemService.list(page, size, "UNCLAIMED", category, keyword);
        return Result.ok(PageResult.of(itemPage));
    }

    @Operation(summary = "获取所有物品分类")
    @GetMapping("/categories")
    public Result<List<String>> categories() {
        return Result.ok(lostItemService.getDistinctCategories());
    }
}