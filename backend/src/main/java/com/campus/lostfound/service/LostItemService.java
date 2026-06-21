package com.campus.lostfound.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.dto.request.LostItemCreateRequest;
import com.campus.lostfound.dto.request.LostItemUpdateRequest;
import com.campus.lostfound.dto.response.ImageUploadResponse;
import com.campus.lostfound.dto.response.LostItemResponse;
import org.springframework.web.multipart.MultipartFile;

public interface LostItemService {

    /**
     * 上传图片并触发YOLO检测（异步）
     */
    ImageUploadResponse uploadImage(MultipartFile file);

    /**
     * 仅YOLO检测，不保存文件
     */
    ImageUploadResponse detectOnly(MultipartFile file);

    /**
     * 创建拾物记录
     */
    LostItemResponse create(LostItemCreateRequest request, Long userId);

    /**
     * 分页查询拾物列表
     */
    Page<LostItemResponse> list(int page, int size, String status, String category, String keyword);

    /**
     * 获取拾物详情
     */
    LostItemResponse getById(Long id);

    /**
     * 更新拾物（仅UNCLAIMED状态可编辑）
     */
    void update(Long id, LostItemUpdateRequest request, Long userId);

    /**
     * 删除拾物（管理员）
     */
    void delete(Long id);
}
