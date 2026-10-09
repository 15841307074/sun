package com.htyoudao.youdao.module.infra.api.download;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.infra.api.download.dto.FileDownloadReqDTO;
import com.htyoudao.youdao.module.infra.controller.admin.download.vo.FileDownloadReqVO;
import com.htyoudao.youdao.module.infra.service.download.DownloadService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

@DubboService
@Validated
public class DownloadApiImpl implements DownloadApi {

    @Resource
    private DownloadService downloadService;

    @Override
    public CommonResult<Long> createDownload(FileDownloadReqDTO downloadReqDTO) {
        FileDownloadReqVO downloadReqVO = new FileDownloadReqVO();
        BeanUtils.copyProperties(downloadReqDTO, downloadReqVO);
        return CommonResult.success( downloadService.create(downloadReqVO));
    }

    @Override
    public CommonResult<String> updateDownload(FileDownloadReqDTO downloadReqDTO) {
        FileDownloadReqVO downloadReqVO = new FileDownloadReqVO();
        BeanUtils.copyProperties(downloadReqDTO, downloadReqVO);
        downloadService.update(downloadReqVO);
        return CommonResult.success("OK");
    }
}
