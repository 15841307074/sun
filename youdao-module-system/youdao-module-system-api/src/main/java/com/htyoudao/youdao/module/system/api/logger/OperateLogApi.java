package com.htyoudao.youdao.module.system.api.logger;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.api.logger.dto.OperateLogCreateReqDTO;
import com.htyoudao.youdao.module.system.api.logger.dto.OperateLogPageReqDTO;
import com.htyoudao.youdao.module.system.api.logger.dto.OperateLogRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "RPC 服务 - 操作日志")
public interface OperateLogApi {

    @Operation(summary = "创建操作日志")
    CommonResult<Boolean> createOperateLog(@Valid @RequestBody OperateLogCreateReqDTO createReqDTO);

    /**
     * 【异步】创建操作日志
     *
     * @param createReqDTO 请求
     */
    @Async
    default void createOperateLogAsync(OperateLogCreateReqDTO createReqDTO) {
        createOperateLog(createReqDTO).checkError();
    }

    @Operation(summary = "获取指定模块的指定数据的操作日志分页")
    CommonResult<PageResult<OperateLogRespDTO>> getOperateLogPage(@SpringQueryMap OperateLogPageReqDTO pageReqDTO);

}
