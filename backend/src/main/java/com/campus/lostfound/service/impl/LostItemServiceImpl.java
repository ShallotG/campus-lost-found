package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.ai.DeepSeekEmbeddingClient;
import com.campus.lostfound.ai.YoloClient;
import com.campus.lostfound.common.BusinessException;
import com.campus.lostfound.dto.request.LostItemCreateRequest;
import com.campus.lostfound.dto.request.LostItemUpdateRequest;
import com.campus.lostfound.dto.response.ImageUploadResponse;
import com.campus.lostfound.dto.response.LostItemResponse;
import com.campus.lostfound.dto.response.YoloDetectResponse;
import com.campus.lostfound.entity.LostItem;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.mapper.LostItemMapper;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.service.LostItemService;
import com.campus.lostfound.utils.CosineSimilarityUtil;
import com.campus.lostfound.utils.FileUploadUtil;
import com.campus.lostfound.utils.ImageHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class LostItemServiceImpl implements LostItemService {

    private final LostItemMapper lostItemMapper;
    private final UserMapper userMapper;
    private final FileUploadUtil fileUploadUtil;
    private final YoloClient yoloClient;
    private final DeepSeekEmbeddingClient embeddingClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.storage-mode:local}")
    private String storageMode;

    @Override
    public ImageUploadResponse uploadImage(MultipartFile file) {
        // 1. 上传文件（本地或OSS）
        String imageUrl = fileUploadUtil.uploadImage(file);

        // 2. YOLO检测
        YoloDetectResponse detectResult;
        if ("oss".equalsIgnoreCase(storageMode)) {
            // OSS模式：直接用字节数组发送给YOLO，无需本地文件
            byte[] imageBytes = fileUploadUtil.getFileBytes(file);
            detectResult = yoloClient.detect(imageBytes, file.getOriginalFilename());
            // 异步计算pHash
            computeImageHashAsync(imageBytes);
        } else {
            // 本地模式：用本地文件路径
            String absolutePath = uploadDir + imageUrl.replace("/uploads", "").replace("/", File.separator);
            detectResult = yoloClient.detect(absolutePath);
            // 异步计算pHash
            computeImageHashAsync(absolutePath);
        }

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
        // 仅做YOLO检测 + 校验，不保存文件到本地/OSS
        fileUploadUtil.validateOnly(file);
        byte[] imageBytes = fileUploadUtil.getFileBytes(file);
        YoloDetectResponse detectResult = yoloClient.detect(imageBytes, file.getOriginalFilename());

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

        // 计算文本embedding并缓存
        String textToEmbed = buildEmbeddingText(
                request.getCategory(), request.getStorageLocation(), request.getRemark());
        asyncComputeAndCacheEmbedding(null, textToEmbed);

        lostItemMapper.insert(item);

        // 异步更新embedding到数据库
        asyncUpdateEmbedding(item.getId(), textToEmbed);

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

        // 重新计算embedding
        String textToEmbed = buildEmbeddingText(
                item.getCategory(), item.getStorageLocation(), item.getRemark());
        asyncUpdateEmbedding(item.getId(), textToEmbed);

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
        // 清理Redis缓存
        stringRedisTemplate.delete("embedding:item:" + id);

        log.info("拾物记录删除成功: id={}", id);
    }

    // ==================== 私有方法 ====================

    /**
     * 构建用于Embedding的文本
     */
    /**
     * 转换 YOLO Top3 → ImageUploadResponse Top3
     */
    private java.util.List<com.campus.lostfound.dto.response.ImageUploadResponse.CategoryOption> convertTop3(
            java.util.List<com.campus.lostfound.dto.response.YoloDetectResponse.CategoryOption> yoloTop3) {
        if (yoloTop3 == null) return java.util.Collections.emptyList();
        return yoloTop3.stream().map(opt ->
                com.campus.lostfound.dto.response.ImageUploadResponse.CategoryOption.builder()
                        .category(opt.getCategory())
                        .categoryCn(opt.getCategoryCn())
                        .confidence(opt.getConfidence())
                        .build()
        ).toList();
    }

    private String buildEmbeddingText(String category, String location, String remark) {
        StringBuilder sb = new StringBuilder();
        sb.append("物品类别:").append(category != null ? category : "未知");
        sb.append(" 存放位置:").append(location != null ? location : "未知");
        if (remark != null && !remark.isEmpty()) {
            sb.append(" 备注:").append(remark);
        }
        return sb.toString();
    }

    /**
     * 异步计算并缓存Embedding
     */
    @Async("aiTaskExecutor")
    public CompletableFuture<Void> asyncComputeAndCacheEmbedding(Long itemId, String text) {
        try {
            double[] embedding = embeddingClient.embed(text);
            if (embedding != null && itemId != null) {
                String json = CosineSimilarityUtil.toJson(embedding);
                stringRedisTemplate.opsForValue().set("embedding:item:" + itemId, json);
                log.debug("Embedding缓存成功: itemId={}", itemId);
            }
        } catch (Exception e) {
            log.error("异步计算Embedding失败: itemId={}", itemId, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 异步更新DB中的embedding
     */
    @Async("aiTaskExecutor")
    public void asyncUpdateEmbedding(Long itemId, String text) {
        try {
            double[] embedding = embeddingClient.embed(text);
            if (embedding != null) {
                String json = CosineSimilarityUtil.toJson(embedding);
                LostItem update = new LostItem();
                update.setId(itemId);
                update.setTextEmbedding(json);
                lostItemMapper.updateById(update);
                // 同时写入Redis
                stringRedisTemplate.opsForValue().set("embedding:item:" + itemId, json);
                log.debug("Embedding DB更新成功: itemId={}", itemId);
            }
        } catch (Exception e) {
            log.error("异步更新Embedding到DB失败: itemId={}", itemId, e);
        }
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
