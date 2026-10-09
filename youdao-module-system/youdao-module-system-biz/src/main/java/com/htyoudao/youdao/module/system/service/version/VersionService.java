package com.htyoudao.youdao.module.system.service.version;

import com.htyoudao.youdao.module.system.controller.app.version.vo.VersionDataRequestVo;

import java.util.Map;

public interface VersionService {
    String getVersion();

    /**
     * @param clientType
     */
    Integer getSwitch0(String clientType);

    VersionDataRequestVo selectActive();
}
