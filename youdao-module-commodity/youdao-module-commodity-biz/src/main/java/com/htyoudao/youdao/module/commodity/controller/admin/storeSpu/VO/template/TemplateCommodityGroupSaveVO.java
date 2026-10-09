package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TemplateCommodityGroupSaveVO {

    @Schema(description = "模板套餐分组名称")
    private Long groupId;

    @Schema(description = "模板套餐分组名称")
    private String commodityGroupName;

    @Schema(description = "模板套餐分组状态")
    private Integer status;

    @Schema(description = "模板套餐分组排序")
    private Integer sort;

    @Schema(description = "模板套餐分组可选数量")
    private Integer choose;

    @Schema(description = "模板套餐分组属性")
    private Integer attribute;

    @Schema(description = "是否可多选")
    private Integer chooseMany;

    /**
     * 关联套餐ID（弃用）
     */
    private Long commoditySetmealId;

    /**
     * 套餐里的单品 ids（弃用）
     */
    private String commoditySingleIds;

    /**
     * 商品Id
     */
    private Long commodityId;


    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 模板商品Id
     */
    private Long commodityTemplateId;



    @Schema(description = "模板套餐分组单品列表")
    private List<TemplateCommoditySingleSaveVO> commodityTemplateSingleList = new ArrayList<>();
}
