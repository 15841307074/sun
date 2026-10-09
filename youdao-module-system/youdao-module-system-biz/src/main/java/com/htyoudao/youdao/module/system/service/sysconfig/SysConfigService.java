package com.htyoudao.youdao.module.system.service.sysconfig;

import com.htyoudao.youdao.module.system.controller.admin.sysconfig.vo.SysConfigReqVO;

import java.util.List;
import java.util.Map;

public interface SysConfigService {
    Map<String, String> getBykeys(List<String> keyList);

    boolean checkSysConfigByConfigKey(SysConfigReqVO sysConfig);

    Boolean updateSysConfig(SysConfigReqVO sysConfig);
}
