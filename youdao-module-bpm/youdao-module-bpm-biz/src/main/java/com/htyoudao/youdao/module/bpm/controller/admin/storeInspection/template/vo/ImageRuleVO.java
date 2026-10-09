package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "图片规则")
public class ImageRuleVO {

    /**
     * 是否允许上传图片：0-不允许 1-允许（默认1）
     */
    private Integer imgFlag = 1; // 默认：允许上传

    /**
     * 图片是否必传：0-不必传(默认) 1-合格必传 2-不合格必传 3-不适用必传
     */
    private String imgState = "0"; // 默认：不必传

    /**
     * 照片上传数量（1~12张，默认1）
     */
    private Integer imgCount = 1; // 默认：1张

    /**
     * 允许本地上传：0-不允许 1-允许(默认1)
     */
    private Integer localImgFlag = 1; // 默认：允许本地上传

}
