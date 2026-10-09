package com.htyoudao.youdao.module.member.api.wecom.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * 检查用户是否在企业微信群中请求参数
 */
@Data
@Schema(description = "检查用户是否在企业微信群中请求参数")
public class WecomGroupCheckMemberReqVO {

    @Schema(description = "用户ID")
    @NotBlank(message = "用户ID不能为空")
    private String unionId;

    @Schema(description = "群ID")
    @NotBlank(message = "群ID不能为空")
    private String chatId;


}
