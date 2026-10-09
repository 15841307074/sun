package com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 活动查询 Response VO")
@Data
public class ActivityPageRespVO {

    /**
     * 主键
     */
    @Schema(description = "主键", requiredMode = RequiredMode.REQUIRED)
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "优惠类型 1（1第二件半件，2买一送一，3自定义优惠）", requiredMode = RequiredMode.REQUIRED)
    private Integer discountType;

    @Schema(description = "活动名称", requiredMode = RequiredMode.REQUIRED)
    private String activityName;

    @Schema(description = "开始日期", requiredMode = RequiredMode.REQUIRED)
    private Date startDate;

    @Schema(description = "结束日期", requiredMode = RequiredMode.REQUIRED)
    private Date endDate;

    @Schema(description = "活动状态(1 未开始 2 进行中 3 已结束)", requiredMode = RequiredMode.REQUIRED)
    private Integer activityStatus;

    @Schema(description = "是否全门店 (1是 0否)", requiredMode = RequiredMode.REQUIRED)
    private Integer activityStore;

    @Schema(description = "应用范围 0 门店 1 标签")
    private Integer appScope;

    @Schema(description = "活动类型 1（n件n折）", requiredMode = RequiredMode.REQUIRED)
    private Integer activityType;

    @Schema(description = "是否上架(0不开启 1开启)")
    private Integer isEnabled;

    @Schema(description = "活动备注")
    private String activityRemark;

    @Schema(description = "指定时间段")
    private String timeRange;

    @Schema(description = "指定时间段")
    private List<String> timeRangeList;


    @Schema(description = "指定日期")
    private String dayNumbers;

    @Schema(description = "指定日期")
    private List<Integer> dayNumberList;

    @Schema(description = "指定周几")
    private String weekNumbers;

    @Schema(description = "指定周几")
    private List<Integer> weekNumberList;


    @Schema(description = "适用门店", requiredMode = RequiredMode.REQUIRED)
    private List<StoreInfoDTO> storeInfoDTOS = new ArrayList<>();

    @Schema(description = "抽奖活动类型")
    private Integer lotteryType;


    @Schema(description = "抽奖活动Id")
    private Long lotterySettingId;

}
