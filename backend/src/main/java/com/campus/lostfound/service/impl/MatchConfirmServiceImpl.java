package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.BusinessException;
import com.campus.lostfound.dto.request.MatchConfirmRequest;
import com.campus.lostfound.dto.response.MatchConfirmResponse;
import com.campus.lostfound.entity.LostItem;
import com.campus.lostfound.entity.MatchConfirm;
import com.campus.lostfound.entity.SystemConfig;
import com.campus.lostfound.mapper.LostItemMapper;
import com.campus.lostfound.mapper.MatchConfirmMapper;
import com.campus.lostfound.mapper.SystemConfigMapper;
import com.campus.lostfound.service.MatchConfirmService;
import com.campus.lostfound.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchConfirmServiceImpl implements MatchConfirmService {

    private final MatchConfirmMapper matchConfirmMapper;
    private final LostItemMapper lostItemMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final NotificationService notificationService;

    @Override
    public MatchConfirmResponse confirm(MatchConfirmRequest request, Long userId) {
        LostItem item = lostItemMapper.selectById(request.getLostItemId());
        if (item == null) {
            throw new BusinessException(404, "拾物记录不存在");
        }

        MatchConfirm confirm = new MatchConfirm();
        confirm.setLostItemId(request.getLostItemId());
        confirm.setUserId(userId);
        confirm.setMatchScore(request.getMatchScore());
        confirm.setConfirmTime(LocalDateTime.now());
        matchConfirmMapper.insert(confirm);

        log.info("失主确认匹配: confirmId={}, lostItemId={}, userId={}",
                confirm.getId(), request.getLostItemId(), userId);

        // 发送通知给登记该物品的工作人员
        try {
            if (item.getCreateUserId() != null) {
                notificationService.create(
                        item.getCreateUserId(),
                        "MATCH_CONFIRM",
                        "有失主确认了您登记的物品",
                        "物品「" + item.getCategory() + "」被失主确认为自己的物品，请及时处理。物品编号：" + item.getId(),
                        item.getId()
                );
            }
        } catch (Exception e) {
            log.warn("发送匹配确认通知失败: {}", e.getMessage());
        }

        MatchConfirmResponse.ContactInfo contactInfo = MatchConfirmResponse.ContactInfo.builder()
                .address(getConfigValue("contact_address"))
                .phone(getConfigValue("contact_phone"))
                .workingHours(getConfigValue("working_hours"))
                .claimNotice(getConfigValue("claim_notice"))
                .build();

        return MatchConfirmResponse.builder()
                .confirmId(confirm.getId())
                .lostItemId(item.getId())
                .matchScore(request.getMatchScore())
                .confirmTime(confirm.getConfirmTime())
                .contactInfo(contactInfo)
                .itemImageUrl(item.getImageUrl())
                .itemCategory(item.getCategory())
                .itemStorageLocation(item.getStorageLocation())
                .itemStatus(item.getStatus())
                .build();
    }

    @Override
    public Page<MatchConfirmResponse> getMyConfirms(int page, int size, Long userId) {
        LambdaQueryWrapper<MatchConfirm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MatchConfirm::getUserId, userId)
                .orderByDesc(MatchConfirm::getCreateTime);

        Page<MatchConfirm> confirmPage = matchConfirmMapper.selectPage(new Page<>(page, size), wrapper);

        return (Page<MatchConfirmResponse>) confirmPage.convert(confirm -> {
            MatchConfirmResponse.MatchConfirmResponseBuilder builder = MatchConfirmResponse.builder()
                    .confirmId(confirm.getId())
                    .lostItemId(confirm.getLostItemId())
                    .matchScore(confirm.getMatchScore())
                    .confirmTime(confirm.getConfirmTime());

            LostItem item = lostItemMapper.selectById(confirm.getLostItemId());
            if (item != null) {
                builder.itemImageUrl(item.getImageUrl())
                       .itemCategory(item.getCategory())
                       .itemStorageLocation(item.getStorageLocation())
                       .itemStatus(item.getStatus());
            }

            return builder.build();
        });
    }

    private String getConfigValue(String key) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key)
        );
        return config != null ? config.getConfigValue() : "暂无信息";
    }
}
