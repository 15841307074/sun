package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "不适用规则")
public class InapplicabilityRuleVO {

    /**
     * 0 不显示 1 显示
     */
    private Integer showFlag;

}
