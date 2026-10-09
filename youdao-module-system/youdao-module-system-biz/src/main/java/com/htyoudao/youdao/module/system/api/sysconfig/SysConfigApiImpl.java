package com.htyoudao.youdao.module.system.api.sysconfig;

import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.api.sysconfig.dto.ClientSysConfigDTO;
import com.htyoudao.youdao.module.system.controller.admin.sysconfig.vo.SysConfigReqVO;
import com.htyoudao.youdao.module.system.service.sysconfig.SysConfigService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.error;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.SYS_CONFIG_KEY_NOT_EXIST;

@DubboService
@Validated
public class SysConfigApiImpl implements SysConfigApi{

    @Resource
    private SysConfigService sysConfigService;
    @Override
    public CommonResult<Map<String, String>> getBykeys(List<String> keyList) {
        return success(sysConfigService.getBykeys(keyList));
    }

    @Override
    public CommonResult<Boolean> editConfig(ClientSysConfigDTO sysConfig) {
        SysConfigReqVO sysConfigReqVO = BeanUtils.toBean(sysConfig, SysConfigReqVO.class);
        if (sysConfigService.checkSysConfigByConfigKey(sysConfigReqVO)) {
            String msg = String.format(SYS_CONFIG_KEY_NOT_EXIST.getMsg(),sysConfig.getConfigKey());
            error(new ErrorCode(SYS_CONFIG_KEY_NOT_EXIST.getCode(),msg));
        }
        return success(sysConfigService.updateSysConfig(sysConfigReqVO));
    }
}
