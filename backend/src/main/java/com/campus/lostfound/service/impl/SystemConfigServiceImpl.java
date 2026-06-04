package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostfound.common.BusinessException;
import com.campus.lostfound.entity.SystemConfig;
import com.campus.lostfound.mapper.SystemConfigMapper;
import com.campus.lostfound.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<SystemConfig> listAll() {
        return systemConfigMapper.selectList(null);
    }

    @Override
    public SystemConfig getByKey(String key) {
        return systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
    }

    @Override
    public void updateValue(String key, String value) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));

        if (config == null) {
            throw new BusinessException(404, "配置项不存在: " + key);
        }

        config.setConfigValue(value);
        systemConfigMapper.updateById(config);

        // 更新Redis缓存
        stringRedisTemplate.delete("config:" + key);

        log.info("系统配置更新成功: key={}", key);
    }
}
