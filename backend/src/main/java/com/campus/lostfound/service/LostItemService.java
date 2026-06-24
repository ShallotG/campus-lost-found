package com.campus.lostfound.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.dto.request.LostItemCreateRequest;
import com.campus.lostfound.dto.request.LostItemUpdateRequest;
import com.campus.lostfound.dto.response.ImageUploadResponse;
import com.campus.lostfound.dto.response.LostItemResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LostItemService {

    ImageUploadResponse uploadImage(MultipartFile file);

    ImageUploadResponse detectOnly(MultipartFile file);

    LostItemResponse create(LostItemCreateRequest request, Long userId);

    Page<LostItemResponse> list(int page, int size, String status, String category, String keyword);

    LostItemResponse getById(Long id);

    void update(Long id, LostItemUpdateRequest request, Long userId);

    void delete(Long id);

    List<String> getDistinctCategories();
}