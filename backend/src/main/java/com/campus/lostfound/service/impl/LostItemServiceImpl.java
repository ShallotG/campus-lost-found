package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.ai.QwenVLClient;
import com.campus.lostfound.common.BusinessException;
import com.campus.lostfound.dto.request.LostItemCreateRequest;
import com.campus.lostfound.dto.request.LostItemUpdateRequest;
import com.campus.lostfound.dto.response.DetectResponse;
import com.campus.lostfound.dto.response.ImageUploadResponse;
import com.campus.lostfound.dto.response.LostItemResponse;
import com.campus.lostfound.entity.LostItem;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.mapper.LostItemMapper;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.service.LostItemService;
import com.campus.lostfound.utils.FileUploadUtil;
import com.campus.lostfound.utils.ImageHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class LostItemServiceImpl implements LostItemService {

    private final LostItemMapper lostItemMapper;
    private final UserMapper userMapper;
    private final FileUploadUtil fileUploadUtil;
    private final QwenVLClient qwenVLClient;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public ImageUploadResponse uploadImage(MultipartFile file) {
        // 1. 上传文件到本地
        String imageUrl = fileUploadUtil.uploadImage(file);

        // 2. 千问VL识别
        String absolutePath = uploadDir + imageUrl.replace("/uploads", "").replace("/", java.io.File.separator);
        DetectResponse detectResult = qwenVLClient.detect(absolutePath);
        // 异步计算pHash
        computeImageHashAsync(absolutePath);

        return ImageUploadResponse.builder()
                .imageUrl(imageUrl)
                .category(detectResult.getCategory())
                .categoryCn(detectResult.getCategoryCn())
                .confidence(detectResult.getConfidence())
                .description(detectResult.getDescription())
                .top3(convertTop3(detectResult.getTop3()))
                .build();
    }

    @Override
    public ImageUploadResponse detectOnly(MultipartFile file) {
        // 仅做检测 + 校验，不保存文件到本地
        fileUploadUtil.validateOnly(file);
        byte[] imageBytes = fileUploadUtil.getFileBytes(file);
        DetectResponse detectResult = qwenVLClient.detect(imageBytes, file.getOriginalFilename());

        return ImageUploadResponse.builder()
                .imageUrl(null)  // 仅检测，不返回URL
                .category(detectResult.getCategory())
                .categoryCn(detectResult.getCategoryCn())
                .confidence(detectResult.getConfidence())
                .description(detectResult.getDescription())
                .top3(convertTop3(detectResult.getTop3()))
                .build();
    }

    @Override
    @Transactional
    public LostItemResponse create(LostItemCreateRequest request, Long userId) {
        LostItem item = new LostItem();
        item.setImageUrl(request.getImageUrl());
        item.setCategory(request.getCategory());
        item.setCategoryConfidence(request.getCategoryConfidence());
        item.setStorageLocation(request.getStorageLocation());
        item.setRemark(request.getRemark());
        item.setStatus("UNCLAIMED");
        item.setCreateUserId(userId);

        lostItemMapper.insert(item);


        log.info("拾物记录创建成功: id={}, category={}", item.getId(), item.getCategory());
        return toResponse(item);
    }

    @Override
    public Page<LostItemResponse> list(int page, int size, String status, String category, String keyword) {
        LambdaQueryWrapper<LostItem> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.isEmpty()) {
            wrapper.eq(LostItem::getStatus, status);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(LostItem::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(LostItem::getCategory, keyword)
                    .or().like(LostItem::getStorageLocation, keyword)
                    .or().like(LostItem::getRemark, keyword));
        }
        wrapper.orderByDesc(LostItem::getCreateTime);

        Page<LostItem> itemPage = lostItemMapper.selectPage(new Page<>(page, size), wrapper);
        return (Page<LostItemResponse>) itemPage.convert(this::toResponse);
    }

    @Override
    public LostItemResponse getById(Long id) {
        LostItem item = lostItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(404, "拾物记录不存在");
        }
        return toResponse(item);
    }

    @Override
    @Transactional
    public void update(Long id, LostItemUpdateRequest request, Long userId) {
        LostItem item = lostItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(404, "拾物记录不存在");
        }
        if ("CLAIMED".equals(item.getStatus())) {
            throw new BusinessException("已领取的物品不可编辑");
        }

        if (request.getCategory() != null) {
            item.setCategory(request.getCategory());
        }
        if (request.getStorageLocation() != null) {
            item.setStorageLocation(request.getStorageLocation());
        }
        if (request.getRemark() != null) {
            item.setRemark(request.getRemark());
        }

        lostItemMapper.updateById(item);


        log.info("拾物记录更新成功: id={}", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        LostItem item = lostItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(404, "拾物记录不存在");
        }

        lostItemMapper.deleteById(id);
        log.info("拾物记录删除成功: id={}", id);
    }

    // ==================== 私有方法 ====================

    /**
     * 转换 YOLO Top3 → ImageUploadResponse Top3
     */
    private java.util.List<com.campus.lostfound.dto.response.ImageUploadResponse.CategoryOption> convertTop3(
            java.util.List<com.campus.lostfound.dto.response.DetectResponse.CategoryOption> top3List) {
        if (top3List == null) return java.util.Collections.emptyList();
        return top3List.stream().map(opt ->
                com.campus.lostfound.dto.response.ImageUploadResponse.CategoryOption.builder()
                        .category(opt.getCategory())
                        .categoryCn(opt.getCategoryCn())
                        .confidence(opt.getConfidence())
                        .build()
        ).toList();
    }



    /**
     * 异步计算图片pHash（本地文件路径）
     */
    @Async("aiTaskExecutor")
    public void computeImageHashAsync(String imagePath) {
        try {
            String phash = ImageHashUtil.phash(imagePath);
            if (phash != null) {
                log.debug("pHash计算成功: {}", phash);
            }
        } catch (Exception e) {
            log.error("pHash计算失败", e);
        }
    }

    /**
     * 异步计算图片pHash（字节数组，OSS模式）
     */
    @Async("aiTaskExecutor")
    public void computeImageHashAsync(byte[] imageBytes) {
        try {
            String phash = ImageHashUtil.phash(imageBytes);
            if (phash != null) {
                log.debug("pHash计算成功: {}", phash);
            }
        } catch (Exception e) {
            log.error("pHash计算失败", e);
        }
    }

    /**
     * 实体转响应
     */
    private LostItemResponse toResponse(LostItem item) {
        LostItemResponse resp = new LostItemResponse();
        resp.setId(item.getId());
        resp.setImageUrl(item.getImageUrl());
        resp.setCategory(item.getCategory());
        resp.setCategoryConfidence(item.getCategoryConfidence());
        resp.setStorageLocation(item.getStorageLocation());
        resp.setRemark(item.getRemark());
        resp.setStatus(item.getStatus());
        resp.setCreateTime(item.getCreateTime());
        resp.setUpdateTime(item.getUpdateTime());

        // 获取登记人姓名
        if (item.getCreateUserId() != null) {
            User user = userMapper.selectById(item.getCreateUserId());
            if (user != null) {
                resp.setCreateUserName(user.getRealName());
            }
        }

        return resp;
    }
}