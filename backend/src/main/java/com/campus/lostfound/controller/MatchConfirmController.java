package com.campus.lostfound.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.common.Result;
import com.campus.lostfound.dto.request.MatchConfirmRequest;
import com.campus.lostfound.dto.response.MatchConfirmResponse;
import com.campus.lostfound.security.JwtTokenProvider;
import com.campus.lostfound.service.MatchConfirmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "确认匹配", description = "失主确认物品匹配并获取招领处联系方式")
@RestController
@RequestMapping("/api/match-confirm")
@RequiredArgsConstructor
public class MatchConfirmController {

    private final MatchConfirmService matchConfirmService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "确认匹配")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public Result<MatchConfirmResponse> confirm(@Valid @RequestBody MatchConfirmRequest request,
                                                 @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        return Result.ok("确认成功", matchConfirmService.confirm(request, userId));
    }

    @Operation(summary = "个人确认记录")
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public Result<PageResult<MatchConfirmResponse>> getMyConfirms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        Page<MatchConfirmResponse> confirmPage = matchConfirmService.getMyConfirms(page, size, userId);
        PageResult<MatchConfirmResponse> pageResult = new PageResult<>(
                confirmPage.getRecords(), confirmPage.getTotal(),
                confirmPage.getCurrent(), confirmPage.getSize(), confirmPage.getPages());
        return Result.ok(pageResult);
    }
}
