package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo;

import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecorditem.vo.StoreInspectionRecordItemRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "app - 巡店记录点检分类分组 Response VO")
@Data
public class StoreInspectionRecordTypeGroupRespVO {

    @Schema(description = "点检项目大类ID", example = "1001")
    private Long typeId;

    @Schema(description = "点检项目大类名称", example = "产品质量")
    private String typeName;

    @Schema(description = "已点检数", example = "3")
    private Integer checkedCount;

    @Schema(description = "分类总点检数", example = "6")
    private Integer totalCount;

    @Schema(description = "分类实际得分", example = "20")
    private Integer actualScore;

    @Schema(description = "分类总分", example = "30")
    private Integer totalScore;

    @Schema(description = "分类得分率", example = "66.67")
    private BigDecimal scoreRate;

    @Schema(description = "奖惩金额", example = "30088")
    private BigDecimal rewardAmount;

    @Schema(description = "巡店定位", example = "0090")
    private String storePatrolPosition;

    @Schema(description = "该分类下的点检明细")
    private List<StoreInspectionRecordItemRespVO> detail;
}
