package com.htyoudao.youdao.module.system.controller.admin.oss;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.infra.api.file.FileApi;
import com.htyoudao.youdao.module.infra.api.file.dto.FileCreateReqDTO;
import com.htyoudao.youdao.module.infra.api.file.dto.FileMultiPartCreateReqDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "管理后台 - 上传下载")
@RestController
@RequestMapping("/system/oss")
@Validated
public class OSSController {

    @DubboReference
    private FileApi fileApi;

    public OSSController(FileApi fileApi) {
        this.fileApi = fileApi;
    }

    @PostMapping("/uploadFile")
    public CommonResult<String> uploadFile(FileCreateReqDTO file)
    {
        return fileApi.createFile(file);
    }

    @PostMapping("/uploadFiles")
    public CommonResult<String> uploadFiles(FileMultiPartCreateReqDTO files)
    {
        return fileApi.multiPartCreateFile(files);
    }
}
