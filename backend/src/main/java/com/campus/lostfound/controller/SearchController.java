package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.dto.request.SearchRequest;
import com.campus.lostfound.dto.response.SearchResultResponse;
import com.campus.lostfound.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "智能检索", description = "自然语言语义检索——失主核心API")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "自然语言语义检索（调用DeepSeek Embedding + 余弦相似度）")
    @PostMapping
    public Result<SearchResultResponse> search(@Valid @RequestBody SearchRequest request) {
        return Result.ok(searchService.search(request));
    }
}
