package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.BusinessException;
import com.campus.lostfound.dto.request.ClaimRequest;
import com.campus.lostfound.dto.response.ClaimRecordResponse;
import com.campus.lostfound.entity.ClaimRecord;
import com.campus.lostfound.entity.LostItem;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.mapper.ClaimRecordMapper;
import com.campus.lostfound.mapper.LostItemMapper;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.service.ClaimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRecordMapper claimRecordMapper;
    private final LostItemMapper lostItemMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public ClaimRecordResponse claim(ClaimRequest request, Long handlerId) {
        // 1. 校验拾物状态
        LostItem item = lostItemMapper.selectById(request.getLostItemId());
        if (item == null) {
            throw new BusinessException(404, "拾物记录不存在");
        }
        if ("CLAIMED".equals(item.getStatus())) {
            throw new BusinessException("该物品已被领取");
        }

        // 2. 插入领取记录
        ClaimRecord record = new ClaimRecord();
        record.setLostItemId(request.getLostItemId());
        record.setClaimerName(request.getClaimerName());
        record.setClaimerIdentity(request.getClaimerIdentity());
        record.setClaimerPhone(request.getClaimerPhone());
        record.setHandlerId(handlerId);
        record.setRemark(request.getRemark());
        claimRecordMapper.insert(record);

        // 3. 更新拾物状态为已领取
        LostItem update = new LostItem();
        update.setId(request.getLostItemId());
        update.setStatus("CLAIMED");
        lostItemMapper.updateById(update);

        // 4. 删除Redis embedding缓存（已领取物品不参与检索）
        stringRedisTemplate.delete("embedding:item:" + request.getLostItemId());

        log.info("领取登记成功: claimId={}, lostItemId={}, claimer={}",
                record.getId(), request.getLostItemId(), request.getClaimerName());

        // 5. 构建响应
        ClaimRecordResponse resp = new ClaimRecordResponse();
        resp.setClaimId(record.getId());
        resp.setLostItemId(record.getLostItemId());
        resp.setClaimerName(record.getClaimerName());
        resp.setClaimerIdentity(record.getClaimerIdentity());
        resp.setClaimerPhone(record.getClaimerPhone());
        resp.setRemark(record.getRemark());
        resp.setClaimTime(record.getCreateTime());
        resp.setItemCategory(item.getCategory());
        resp.setItemImageUrl(item.getImageUrl());

        // 办理人姓名
        User handler = userMapper.selectById(handlerId);
        if (handler != null) {
            resp.setHandlerName(handler.getRealName());
        }

        return resp;
    }

    @Override
    public Page<ClaimRecordResponse> list(int page, int size, Long lostItemId) {
        LambdaQueryWrapper<ClaimRecord> wrapper = new LambdaQueryWrapper<>();
        if (lostItemId != null) {
            wrapper.eq(ClaimRecord::getLostItemId, lostItemId);
        }
        wrapper.orderByDesc(ClaimRecord::getCreateTime);

        Page<ClaimRecord> recordPage = claimRecordMapper.selectPage(new Page<>(page, size), wrapper);

        return (Page<ClaimRecordResponse>) recordPage.convert(record -> {
            ClaimRecordResponse resp = new ClaimRecordResponse();
            resp.setClaimId(record.getId());
            resp.setLostItemId(record.getLostItemId());
            resp.setClaimerName(record.getClaimerName());
            resp.setClaimerIdentity(record.getClaimerIdentity());
            resp.setClaimerPhone(record.getClaimerPhone());
            resp.setRemark(record.getRemark());
            resp.setClaimTime(record.getCreateTime());

            // 物品信息
            LostItem item = lostItemMapper.selectById(record.getLostItemId());
            if (item != null) {
                resp.setItemCategory(item.getCategory());
                resp.setItemImageUrl(item.getImageUrl());
            }

            // 办理人信息
            User handler = userMapper.selectById(record.getHandlerId());
            if (handler != null) {
                resp.setHandlerName(handler.getRealName());
            }

            return resp;
        });
    }
}
