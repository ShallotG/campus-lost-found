package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.BusinessException;
import com.campus.lostfound.dto.request.FoundItemCreateRequest;
import com.campus.lostfound.dto.response.FoundItemResponse;
import com.campus.lostfound.entity.FoundItem;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.mapper.FoundItemMapper;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.service.FoundItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoundItemServiceImpl implements FoundItemService {

    private final FoundItemMapper foundItemMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public FoundItemResponse create(FoundItemCreateRequest request, Long userId) {
        FoundItem item = new FoundItem();
        item.setUserId(userId);
        item.setItemName(request.getItemName());
        item.setCategory(request.getCategory());
        item.setColor(request.getColor());
        item.setBrand(request.getBrand());
        item.setDescription(request.getDescription());
        item.setLostTime(request.getLostTime());
        item.setLostLocation(request.getLostLocation());
        item.setContactPhone(request.getContactPhone());
        item.setStatus("OPEN");

        foundItemMapper.insert(item);
        log.info("寻物启事创建成功: id={}, itemName={}", item.getId(), item.getItemName());
        return toResponse(item);
    }

    @Override
    public FoundItemResponse getById(Long id) {
        FoundItem item = foundItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(404, "寻物启事不存在");
        }
        return toResponse(item);
    }

    @Override
    public Page<FoundItemResponse> list(int page, int size, Long userId, String status) {
        LambdaQueryWrapper<FoundItem> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(FoundItem::getStatus, status);
        }
        if (userId != null) {
            wrapper.eq(FoundItem::getUserId, userId);
        }
        wrapper.orderByDesc(FoundItem::getCreateTime);

        Page<FoundItem> itemPage = foundItemMapper.selectPage(new Page<>(page, size), wrapper);
        return (Page<FoundItemResponse>) itemPage.convert(this::toResponse);
    }

    @Override
    public Page<FoundItemResponse> myList(int page, int size, Long userId) {
        return list(page, size, userId, null);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        FoundItem item = foundItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(404, "寻物启事不存在");
        }
        FoundItem update = new FoundItem();
        update.setId(id);
        update.setStatus(status);
        foundItemMapper.updateById(update);
        log.info("寻物启事状态更新: id={}, status={}", id, status);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        FoundItem item = foundItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(404, "寻物启事不存在");
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己的寻物启事");
        }
        foundItemMapper.deleteById(id);
        log.info("寻物启事删除成功: id={}", id);
    }

    private FoundItemResponse toResponse(FoundItem item) {
        FoundItemResponse.FoundItemResponseBuilder builder = FoundItemResponse.builder()
                .id(item.getId())
                .userId(item.getUserId())
                .itemName(item.getItemName())
                .category(item.getCategory())
                .color(item.getColor())
                .brand(item.getBrand())
                .description(item.getDescription())
                .lostTime(item.getLostTime())
                .lostLocation(item.getLostLocation())
                .contactPhone(item.getContactPhone())
                .status(item.getStatus())
                .createTime(item.getCreateTime())
                .updateTime(item.getUpdateTime());

        if (item.getUserId() != null) {
            User user = userMapper.selectById(item.getUserId());
            if (user != null) {
                builder.userName(user.getRealName());
            }
        }

        return builder.build();
    }
}
