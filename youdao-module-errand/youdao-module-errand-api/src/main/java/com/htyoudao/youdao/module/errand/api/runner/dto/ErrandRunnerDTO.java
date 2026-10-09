package com.htyoudao.youdao.module.errand.api.runner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ErrandRunnerDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "跑腿员ID")
    private Long id;

    @Schema(description = "会员ID，对应 wx_member.id")
    private Long memberId;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别：0未知 1男 2女")
    private Integer gender;

    @Schema(description = "审核状态：0待审核 1通过 2失败")
    private Integer auditStatus;

    @Schema(description = "封禁状态：0正常 1封禁")
    private Integer banStatus;

    @Schema(description = "是否有通过首次审核：0否 1通过")
    private Integer firstAuditStatus;
}
