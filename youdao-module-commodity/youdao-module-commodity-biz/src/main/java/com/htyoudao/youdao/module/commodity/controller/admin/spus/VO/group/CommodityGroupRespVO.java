package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.group;

import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.single.CommoditySingleRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 Group Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityGroupRespVO {

    /** 主键，套餐内容分组ID */

    @Schema(description = "主键，套餐内容分组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long groupId;





    /** 排序 */
    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    private int sort;

    /** 分组名称 */
    @Schema(description = "分组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityGroupName;

    /** 必选商品数量 */
    @Schema(description = "必选商品数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long choose;

    /** 分组属性(本组商品是否包含以下所有单品) 1 可选 2 固定 3 加价组 */
    @Schema(description = "分组属性(本组商品是否包含以下所有单品) 1 可选 2 固定 3 加价组", requiredMode = Schema.RequiredMode.REQUIRED)
    private int attribute;



    @Schema(description = "关联商品 Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;

    /**
     * 分组状态 1上 0 下
     */
    @Schema(description = "分组状态 1上 0 下", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;
    /**
     * 套餐分组里可选的数量
     */
    @Schema(description = "套餐分组里可选的数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer commodityStoreGroupChoose;
    /**
     * 同一商品是否可选多份 1 是 0否
     */
    @Schema(description = "同一商品是否可选多份 1 是 0否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer chooseMany;

    @Schema(description = "分组里的单品集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommoditySingleRespVO> commoditySingles =new ArrayList<>();
}
