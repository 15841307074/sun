package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "描述规则")
public class DescriptionRuleVO {

    /**
     * 0 不允许填写 1 允许填写
     */
    private Integer descriptionFlag;

    /**
     * 合格 0选填 1必填
     */
    private Integer qualified;

    /**
     * 不合格 0选填 1必填
     */
    private Integer unqualified;

    /**
     * 不适用 0选填 1必填
     */
    private Integer inapplicability;

}
