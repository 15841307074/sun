package com.htyoudao.youdao.module.promotion.service.bzsplicing.impl;

import com.alibaba.fastjson2.JSON;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.controller.admin.order.vo.ClientSysConfigParam;
import com.htyoudao.youdao.module.promotion.controller.admin.order.vo.SplicingOrderConfigReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.order.vo.SplicingOrderConfigRespVO;
import com.htyoudao.youdao.module.promotion.service.bzsplicing.BzSplicingOrderServcie;
import com.htyoudao.youdao.module.system.api.sysconfig.SysConfigApi;
import com.htyoudao.youdao.module.system.api.sysconfig.dto.ClientSysConfigDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.htyoudao.youdao.module.system.enums.LogRecordConstants.*;

@Service
@Slf4j
public class BzSplicingOrderServcieImpl implements BzSplicingOrderServcie {

    private static final String SPLICING_ORDER_CONFIG = "splicing.order.config";

    @DubboReference
    private SysConfigApi sysConfigApi;
    @Override
    @LogRecord(type = SYSTEM_SPLICING_ORDER_TYPE, subType = SYSTEM_SPLICING_ORDER_UPDATE_TYPE, bizNo = "1", success = SYSTEM_SPLICING_ORDER_UPDATE_SUCCESS)
    public void configSaveOrUpdate(SplicingOrderConfigReqVO splicingOrderConfigReqVO) {
        String configValue = JSON.toJSONString(splicingOrderConfigReqVO);
        editConfig(new ClientSysConfigParam(SPLICING_ORDER_CONFIG, configValue));
        // 记录操作日志上下文
        LogRecordContext.putVariable("splicingOrderConfig", splicingOrderConfigReqVO);
    }

    @Override
    public SplicingOrderConfigRespVO configInfo() {
        Map<String, String> configMap = getBykeys(Collections.singletonList(SPLICING_ORDER_CONFIG));
        String configValueJson = configMap.get(SPLICING_ORDER_CONFIG);
        return ObjectUtils.isEmpty(configValueJson) ? new SplicingOrderConfigRespVO() : JSON.parseObject(configValueJson, SplicingOrderConfigRespVO.class);
    }

    public Map<String, String> getBykeys(List<String> keyList) {
        log.info(">>> 远程调用system模块【/admin-system/sys-config/getBykeys】入参 keyList={}", keyList);

        CommonResult<Map<String, String>> commonResult = sysConfigApi.getBykeys(keyList);
        Map<String, String> result = commonResult.getData();

        log.info(">>> 远程调用system模块【/admin-system/sys-config/getBykeys】响应 res={}", result);

        commonResult.checkError();
        return result;
    }

    public void editConfig(ClientSysConfigParam splicingOrderConfigReqVO) {
        log.info(">>> 远程调用system模块【/admin-system/sys-config/editConfig】入参 body={}", splicingOrderConfigReqVO);
        ClientSysConfigDTO clientSysConfigDTO = BeanUtils.toBean(splicingOrderConfigReqVO, ClientSysConfigDTO.class);
        CommonResult<Boolean> commonResult = sysConfigApi.editConfig(clientSysConfigDTO);
        log.info(">>> 远程调用system模块【/admin-system/sys-config/editConfig】响应 res={}", commonResult);
        commonResult.checkError();
    }
}
