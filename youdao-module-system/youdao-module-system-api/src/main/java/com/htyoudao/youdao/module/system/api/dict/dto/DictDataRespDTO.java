package com.htyoudao.youdao.module.system.api.dict.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "RPC 服务 - 字典数据 Response DTO")
@Data
public class DictDataRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7502928882571870592L;
    
    @Schema(description = "字典标签", requiredMode = Schema.RequiredMode.REQUIRED, example = "youdao")
    private String label;

    @Schema(description = "字典值", requiredMode = Schema.RequiredMode.REQUIRED, example = "iocoder")
    private String value;

    @Schema(description = "字典类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "sys_common_sex")
    private String dictType;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status; // 参见 CommonStatusEnum 枚举

}
