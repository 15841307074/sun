package com.htyoudao.youdao.module.infra.api.download;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.infra.api.download.dto.FileDownloadReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-10
 */
@Tag(name = "RPC 服务 - 下载任务")
public interface DownloadApi {


    @Operation(summary = "下载任务新增")
    CommonResult<Long> createDownload(@RequestBody FileDownloadReqDTO downloadReqDTO);

    @Operation(summary = "下载任务更新")
    CommonResult<String> updateDownload(@RequestBody FileDownloadReqDTO downloadReqDTO);
}
