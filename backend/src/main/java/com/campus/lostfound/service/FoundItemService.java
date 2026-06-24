package com.campus.lostfound.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.dto.request.FoundItemCreateRequest;
import com.campus.lostfound.dto.response.FoundItemResponse;

public interface FoundItemService {

    FoundItemResponse create(FoundItemCreateRequest request, Long userId);

    FoundItemResponse getById(Long id);

    Page<FoundItemResponse> list(int page, int size, Long userId, String status);

    Page<FoundItemResponse> myList(int page, int size, Long userId);

    void updateStatus(Long id, String status);

    void delete(Long id, Long userId);
}
