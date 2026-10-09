package com.htyoudao.youdao.module.infra.api.file;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.infra.api.file.dto.FileCreateReqDTO;
import com.htyoudao.youdao.module.infra.api.file.dto.FileMultiPartCreateReqDTO;
import com.htyoudao.youdao.module.infra.service.file.FileService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
@Validated
public class FileApiImpl implements FileApi {

    @Resource
    private FileService fileService;

    @Override
    public CommonResult<String> createFile(FileCreateReqDTO createReqDTO) {
        return success(fileService.createFile(createReqDTO.getName(), createReqDTO.getPath(),
                createReqDTO.getContent()));
    }

    @Override
    public CommonResult<String> multiPartCreateFile(FileMultiPartCreateReqDTO createReqDTO) {
        return success(fileService.multiPartCreateFile(createReqDTO.getFile()));
    }
}
