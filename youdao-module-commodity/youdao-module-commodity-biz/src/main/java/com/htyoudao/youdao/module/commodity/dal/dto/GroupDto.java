package com.htyoudao.youdao.module.commodity.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "app - 套餐下分组 VO")
public class GroupDto implements Serializable {

    @Schema(description = "套餐名称")
    private String groupName;

    @Schema(description = "套餐ID")
    private Long groupId;

    @Schema(description = "必选商品数量")
    private int choose;

    @Schema(description = "是否多选商品")
    private Integer chooseMany;


    @Schema(description = "单品列表")
    private List<SingleDto> singleList = new ArrayList<>();

    @Schema(description = "门店下套餐分组里分组属性（本组单品是否包含以下所有商品）,0是固定搭配 1是可选 2是固定")
    private Integer commodityStoreGroupAttribute;
}
