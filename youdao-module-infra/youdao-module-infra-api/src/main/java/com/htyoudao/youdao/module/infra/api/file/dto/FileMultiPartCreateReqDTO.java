package com.htyoudao.youdao.module.infra.api.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;

@Schema(description = "RPC 服务 - 文件创建 Request DTO")
@Builder
@Data
public class FileMultiPartCreateReqDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3684744615017408898L;

    @NotNull
    @Schema(description = "文件对象")
    private File file;

//    @NotBlank
//    @Schema(description = "存储文件路径")
//    private String objectName;

}
