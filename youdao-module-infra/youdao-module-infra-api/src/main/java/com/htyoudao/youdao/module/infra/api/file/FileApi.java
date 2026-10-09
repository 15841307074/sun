package com.htyoudao.youdao.module.infra.api.file;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.infra.api.file.dto.FileCreateReqDTO;
import com.htyoudao.youdao.module.infra.api.file.dto.FileMultiPartCreateReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "RPC 服务 - 文件")
public interface FileApi {

    /**
     * 保存文件，并返回文件的访问路径
     *
     * @param content 文件内容
     * @return 文件路径
     */
    default String createFile(byte[] content) {
        return createFile(null, null, content);
    }

    /**
     * 保存文件，并返回文件的访问路径
     *
     * @param path 文件路径
     * @param content 文件内容
     * @return 文件路径
     */
    default String createFile(String path, byte[] content) {
        return createFile(null, path, content);
    }

    /**
     * 保存文件，并返回文件的访问路径
     *
     * @param name 原文件名称
     * @param path 文件路径
     * @param content 文件内容
     * @return 文件路径
     */
    default String createFile(@RequestParam("name") String name,
                              @RequestParam("path") String path,
                              @RequestParam("content") byte[] content) {
        return createFile(new FileCreateReqDTO().setName(name).setPath(path).setContent(content)).getCheckedData();
    }

    @Operation(summary = "保存文件，并返回文件的访问路径")
    CommonResult<String> createFile(@Valid @RequestBody FileCreateReqDTO createReqDTO);

    @Operation(summary = "分片保存文件，并返回文件的访问路径")
    CommonResult<String> multiPartCreateFile(@RequestBody FileMultiPartCreateReqDTO createReqDTO);

}
