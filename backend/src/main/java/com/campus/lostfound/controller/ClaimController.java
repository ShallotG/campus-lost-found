package com.campus.lostfound.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.common.Result;
import com.campus.lostfound.dto.request.ClaimRequest;
import com.campus.lostfound.dto.response.ClaimRecordResponse;
import com.campus.lostfound.security.JwtTokenProvider;
import com.campus.lostfound.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "领取登记", description = "工作人员办理物品领取登记")
@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "登记领取")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_STAFF', 'ROLE_ADMIN')")
    public Result<ClaimRecordResponse> claim(@Valid @RequestBody ClaimRequest request,
                                              @RequestHeader("Authorization") String authHeader) {
        Long handlerId = jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        return Result.ok("领取登记成功", claimService.claim(request, handlerId));
    }

    @Operation(summary = "领取记录列表")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_STAFF', 'ROLE_ADMIN')")
    public Result<PageResult<ClaimRecordResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long lostItemId) {
        Page<ClaimRecordResponse> recordPage = claimService.list(page, size, lostItemId);
        PageResult<ClaimRecordResponse> pageResult = new PageResult<>(
                recordPage.getRecords(), recordPage.getTotal(),
                recordPage.getCurrent(), recordPage.getSize(), recordPage.getPages());
        return Result.ok(pageResult);
    }
}
