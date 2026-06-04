package com.campus.lostfound.service;

import com.campus.lostfound.entity.SystemConfig;

import java.util.List;

public interface SystemConfigService {

    List<SystemConfig> listAll();

    SystemConfig getByKey(String key);

    void updateValue(String key, String value);
}
