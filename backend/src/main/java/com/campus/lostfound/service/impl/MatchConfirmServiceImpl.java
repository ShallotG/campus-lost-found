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

    @Override
    public MatchConfirmResponse confirm(MatchConfirmRequest request, Long userId) {
        // 校验物品存在
        LostItem item = lostItemMapper.selectById(request.getLostItemId());
        if (item == null) {
            throw new BusinessException(404, "拾物记录不存在");
        }

        // 记录确认行为
        MatchConfirm confirm = new MatchConfirm();
        confirm.setLostItemId(request.getLostItemId());
        confirm.setUserId(userId);
        confirm.setMatchScore(request.getMatchScore());
        confirm.setConfirmTime(LocalDateTime.now());
        matchConfirmMapper.insert(confirm);

        log.info("失主确认匹配: confirmId={}, lostItemId={}, userId={}",
                confirm.getId(), request.getLostItemId(), userId);

        // 查询招领处联系方式
        MatchConfirmResponse.ContactInfo contactInfo = MatchConfirmResponse.ContactInfo.builder()
                .address(getConfigValue("contact_address"))
                .phone(getConfigValue("contact_phone"))
                .workingHours(getConfigValue("working_hours"))
                .claimNotice(getConfigValue("claim_notice"))
                .build();

        return MatchConfirmResponse.builder()
                .confirmId(confirm.getId())
                .contactInfo(contactInfo)
                .build();
    }

    @Override
    public Page<MatchConfirmResponse> getMyConfirms(int page, int size, Long userId) {
        LambdaQueryWrapper<MatchConfirm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MatchConfirm::getUserId, userId)
                .orderByDesc(MatchConfirm::getCreateTime);

        Page<MatchConfirm> confirmPage = matchConfirmMapper.selectPage(new Page<>(page, size), wrapper);

        return (Page<MatchConfirmResponse>) confirmPage.convert(confirm -> {
            MatchConfirmResponse resp = MatchConfirmResponse.builder()
                    .confirmId(confirm.getId())
                    .build();

            // 查询关联的物品信息（可选扩展）
            return resp;
        });
    }

    private String getConfigValue(String key) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key)
        );
        return config != null ? config.getConfigValue() : "暂无信息";
    }
}
