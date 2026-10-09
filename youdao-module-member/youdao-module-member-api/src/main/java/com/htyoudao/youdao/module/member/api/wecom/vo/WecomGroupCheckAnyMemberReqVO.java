package com.htyoudao.youdao.module.member.api.wecom.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;


/**
 * 检查用户是否在任意一个企业微信群中请求参数
 */
@Data
@Schema(description = "检查用户是否在任意一个企业微信群中请求参数")
public class WecomGroupCheckAnyMemberReqVO implements Serializable {

    @Schema(description = "用户ID")
    @NotBlank(message = "用户ID不能为空")
    private String unionId;
}
