package com.htyoudao.youdao.module.errand.controller.app.errandRunner.VO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * App 骑手入驻申请请求 VO。
 */
@Data
@Schema(description = "App - 骑手入驻申请 Request VO")
public class AppErrandRunnerApplyReqVO {

    @NotNull(message = "门店不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "门店 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2056265154128035842")
    private Long storeId;

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18888888888")
    private String phone;

    @NotBlank(message = "学号不能为空")
    @Schema(description = "学号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20260001")
    @Length(min = 2, max = 20, message = "学号长度为 2-20 位")
    private String studentNo;

    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])([0-2]\\d|3[0-1])\\d{3}[0-9Xx]$", message = "请输入正确的身份证号")
    @Schema(description = "身份证号", requiredMode = Schema.RequiredMode.REQUIRED, example = "341200200001010022")
    private String idCardNo;

    @NotNull(message = "性别不能为空")
    @Schema(description = "性别：0未知 1男 2女", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer gender;

    @NotBlank(message = "身份证正面不能为空")
    @Schema(description = "身份证正面图片地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String idCardFront;

    @NotBlank(message = "身份证反面不能为空")
    @Schema(description = "身份证反面图片地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String idCardBack;

    @NotBlank(message = "学生证照片不能为空")
    @Schema(description = "学生证照片地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String studentCardImg;

}
