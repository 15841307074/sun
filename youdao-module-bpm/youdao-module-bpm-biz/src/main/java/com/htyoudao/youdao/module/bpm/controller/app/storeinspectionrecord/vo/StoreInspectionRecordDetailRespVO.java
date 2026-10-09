package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "app - 巡店记录点检详情 Response VO")
@Data
public class StoreInspectionRecordDetailRespVO {

    /**
     * 门店ID
     */
    private Long storeId;
    /**
     * 门店名称(快照)
     */
    private String storeName;
    /**
     * 使用的模板ID
     */
    private Long templateId;
    /**
     * 模板名称(快照)
     */
    private String templateName;
    /**
     * 状态：0 进行中, 1 已完成, 2 已失效
     */
    private Integer status;
    /**
     * 巡店人ID
     */
    private Long inspectorId;
    /**
     * 巡店人姓名
     */
    private String inspectorName;
    /**
     * 巡店人电话
     */
    private String inspectorPhone;
    /**
     * 是否在范围内 0在 1不在
     */
    private Boolean inScope;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 理论结束时间 = 开始时间+1天
     */
    private LocalDateTime endTime;
    /**
     * 实际结束时间
     */
    private LocalDateTime overTime;


    @Schema(description = "巡店记录ID", example = "1214737")
    private Long recordId;

    @Schema(description = "巡店定位", example = "0090")
    private String storePatrolPosition;

    @Schema(description = "模板预设满分", example = "100")
    private Integer totalScore;

    @Schema(description = "实际得分", example = "86")
    private Integer actualScore;

    @Schema(description = "得分率", example = "86.00")
    private BigDecimal scoreRate;

    @Schema(description = "点检项目总数", example = "20")
    private Integer itemCount;

    @Schema(description = "合格数", example = "10")
    private Integer qualifiedCount;

    @Schema(description = "不合格数", example = "6")
    private Integer unqualifiedCount;

    @Schema(description = "不适用数", example = "4")
    private Integer notApplicableCount;

    @Schema(description = "奖惩金额", example = "30088")
    private BigDecimal rewardAmount;

    private String storePic;

    @Schema(description = "整改意见", example = "0090")
    private String rectificationOpinion;

    @Schema(description = "分类统计列表，第一项为总点检数")
    private List<InspectionTypeStatRespVO> statList;

    @Schema(description = "点检项目分类列表")
    private List<StoreInspectionRecordTypeGroupRespVO> typeList;
}
