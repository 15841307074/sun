package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerDetailRespVO extends ActivityAnswerSaveOrUpdateReqVO {

    @Schema(description = "指定日期字符串，逗号分隔")
    private String dayNumbers;

    @Schema(description = "指定周几字符串，逗号分隔")
    private String weekNumbers;

    @Schema(description = "答题场次时间段字符串，逗号分隔")
    private String timeRange;

    @Schema(description = "活动开始日期")
    private LocalDate answerStartTime;

    @Schema(description = "活动结束日期")
    private LocalDate answerEndTime;

    @Schema(description = "指定门店列表")
    private List<ActivityAnswerStoreRespVO> storeList;

    @Schema(description = "适用商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityDTO> commodityDTOList = new ArrayList<>();


}
