package com.htyoudao.youdao.module.system.api.sms.dto.code;

import com.htyoudao.youdao.framework.common.validation.InEnum;
import com.htyoudao.youdao.framework.common.validation.Mobile;
import com.htyoudao.youdao.module.system.enums.sms.SmsSceneEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@Schema(description = "RPC 服务 - 短信验证码的使用 Request DTO")
@Data
public class SmsCodeUseReqDTO implements Serializable {
    private static final long serialVersionUID = 1L;  // 建议添加 serialVersionUID
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601691300")
    @Mobile
    @NotEmpty(message = "手机号不能为空")
    private String mobile;

    @Schema(description = "发送场景", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "发送场景不能为空")
    @InEnum(SmsSceneEnum.class)
    private Integer scene;

    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotEmpty(message = "验证码")
    private String code;

    @Schema(description = "发送 IP", requiredMode = Schema.RequiredMode.REQUIRED, example = "10.20.30.40")
    @NotEmpty(message = "使用 IP 不能为空")
    private String usedIp;

    // 无参构造函数
    public SmsCodeUseReqDTO() {
    }

    // 全参构造函数（可选）
    public SmsCodeUseReqDTO(String mobile, Integer scene, String code, String usedIp) {
        this.mobile = mobile;
        this.scene = scene;
        this.code = code;
        this.usedIp = usedIp;
    }

    // 链式调用的 setter 方法（如果使用 @Accessors(chain = true) 可以省略）
    public SmsCodeUseReqDTO setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public SmsCodeUseReqDTO setScene(Integer scene) {
        this.scene = scene;
        return this;
    }

    public SmsCodeUseReqDTO setCode(String code) {
        this.code = code;
        return this;
    }

    public SmsCodeUseReqDTO setUsedIp(String usedIp) {
        this.usedIp = usedIp;
        return this;
    }
}
