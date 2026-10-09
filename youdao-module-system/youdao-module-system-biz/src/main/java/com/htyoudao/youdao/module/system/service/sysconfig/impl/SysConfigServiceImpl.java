package com.htyoudao.youdao.module.system.service.sysconfig.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.module.system.controller.admin.sysconfig.vo.SysConfigReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.sysconfig.SysConfigDO;
import com.htyoudao.youdao.module.system.dal.mysql.sysconfig.SysConfigMapper;
import com.htyoudao.youdao.module.system.service.sysconfig.SysConfigService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfigDO> implements SysConfigService {
    @Override
    public Map<String, String> getBykeys(List<String> keyList) {
        return this.list(
                        new LambdaQueryWrapper<SysConfigDO>()
                                .in(SysConfigDO::getConfigKey, keyList)
                ).stream()
                .collect(Collectors.toMap(
                        SysConfigDO::getConfigKey,
                        SysConfigDO::getConfigValue,
                        (existingValue, newValue) -> newValue  // 保留最后一个出现的值
                ));
    }

    @Override
    public boolean checkSysConfigByConfigKey(SysConfigReqVO sysConfig) {
        // 检查sysConfig是否为空或者配置键是否为空
        if (sysConfig == null || StringUtils.isEmpty(sysConfig.getConfigKey())) {
            throw new ServiceException(new ErrorCode(500, "参数键不能为空"));
        }
        return this.count(
                new LambdaQueryWrapper<SysConfigDO>()
                        .eq(SysConfigDO::getConfigKey, sysConfig.getConfigKey())
        ) == 0;
    }

    @Override
    public Boolean updateSysConfig(SysConfigReqVO sysConfig) {
        if (sysConfig == null || StringUtils.isEmpty(sysConfig.getConfigKey())) {
            throw new ServiceException(new ErrorCode(500, "参数键不能为空"));
        }
        if (sysConfig == null || StringUtils.isEmpty(sysConfig.getConfigValue())) {
            throw new ServiceException(new ErrorCode(500, "参数键不能为空"));
        }
        LambdaUpdateWrapper<SysConfigDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysConfigDO::getConfigKey, sysConfig.getConfigKey());
        boolean update = this.update(BeanUtils.toBean(sysConfig, SysConfigDO.class), updateWrapper);
        return update;
    }
}
