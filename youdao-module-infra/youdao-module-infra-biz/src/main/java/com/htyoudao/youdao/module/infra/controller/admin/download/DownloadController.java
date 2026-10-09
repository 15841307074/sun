package com.htyoudao.youdao.module.infra.controller.admin.download;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.infra.controller.admin.download.vo.FileDownloadPageReqVO;
import com.htyoudao.youdao.module.infra.controller.admin.download.vo.FileDownloadPageRespVO;
import com.htyoudao.youdao.module.infra.controller.admin.download.vo.FileDownloadReqVO;
import com.htyoudao.youdao.module.infra.dal.dataobject.download.FileDownloadDO;
import com.htyoudao.youdao.module.infra.service.download.DownloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 文件下载任务")
@RestController
@RequestMapping("/infra/download")
@Validated
@Slf4j
public class DownloadController {

    @Resource
    private DownloadService downloadService;

    @PostMapping("/create")
    @Operation(summary = "文件下载任务", description = "新增")
    public CommonResult<String> create(@RequestBody FileDownloadReqVO createReqVO) throws Exception {
        downloadService.create(createReqVO);
        return CommonResult.success("OK");
    }

    @PostMapping("/update")
    @Operation(summary = "文件下载任务", description = "修改")
    public CommonResult<String> update(@RequestBody FileDownloadReqVO createUpdateDTO) throws Exception {
        downloadService.update(createUpdateDTO);
        return CommonResult.success("OK");
    }

    @GetMapping("/page")
    @Operation(summary = "文件下载任务列表", description = "查询")
    public CommonResult<PageResult<FileDownloadPageRespVO>> getDownloadPage(FileDownloadPageReqVO pageReqVO) throws Exception {
        PageResult<FileDownloadDO> downloadPage = downloadService.getDownloadPage(pageReqVO);
        return success(BeanUtils.toBean(downloadPage, FileDownloadPageRespVO.class));
    }
}
