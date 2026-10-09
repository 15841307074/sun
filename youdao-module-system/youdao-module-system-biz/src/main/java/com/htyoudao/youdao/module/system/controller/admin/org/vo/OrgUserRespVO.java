package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 组织机构树 Request VO")
@Data
@ToString(callSuper = true)
public class OrgUserRespVO {

    @Schema(description = "userId", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    private Long userId;

    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    private String nickname;
}
