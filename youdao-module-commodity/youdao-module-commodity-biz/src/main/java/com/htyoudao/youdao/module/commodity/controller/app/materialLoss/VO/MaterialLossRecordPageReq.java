package com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class MaterialLossRecordPageReq extends PageParam {


    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;


    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private String startDate;


    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String endDate;


    @Schema(description = "损耗类型：1样余 2炸糊 3餐品不达标 4丢餐 5员工餐 6留样 7试餐、尝餐 8活动赠送 9外卖损耗", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer lossType;


}
