package com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Schema(description = "管理后台 - 积分分页参数 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsLogPageListReqVo {


    @Schema(description = "积分类型(1-获取,2-兑换商品消耗,3-过期,4-冻结)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pointsType;


    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderSn;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private String startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private String endTime;


}
