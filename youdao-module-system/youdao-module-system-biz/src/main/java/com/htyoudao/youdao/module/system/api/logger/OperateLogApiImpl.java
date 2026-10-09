package com.htyoudao.youdao.module.system.api.logger;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.api.logger.dto.OperateLogCreateReqDTO;
import com.htyoudao.youdao.module.system.api.logger.dto.OperateLogPageReqDTO;
import com.htyoudao.youdao.module.system.api.logger.dto.OperateLogRespDTO;
import com.htyoudao.youdao.module.system.dal.dataobject.logger.OperateLogDO;
import com.htyoudao.youdao.module.system.service.logger.OperateLogService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
@Validated
public class OperateLogApiImpl implements OperateLogApi {

    @Resource
    private OperateLogService operateLogService;

    @Override
    public CommonResult<Boolean> createOperateLog(OperateLogCreateReqDTO createReqDTO) {
        operateLogService.createOperateLog(createReqDTO);
        return success(true);
    }

    @Override
    public CommonResult<PageResult<OperateLogRespDTO>> getOperateLogPage(OperateLogPageReqDTO pageReqDTO) {
        PageResult<OperateLogDO> operateLogPage = operateLogService.getOperateLogPage(pageReqDTO);
        return success(BeanUtils.toBean(operateLogPage, OperateLogRespDTO.class));
    }

}
