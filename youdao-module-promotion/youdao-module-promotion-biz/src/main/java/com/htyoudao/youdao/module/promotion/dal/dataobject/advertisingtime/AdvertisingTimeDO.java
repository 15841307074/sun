package com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingtime;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 浮窗位置
 */
@TableName("advertising_time")
@Data
public class AdvertisingTimeDO extends BusinessBaseDO {


    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "广告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long advertisingId;



    @Schema(description = "开始时间段", requiredMode = Schema.RequiredMode.REQUIRED)
    private String startTimeSegment;

    @Schema(description = "结束时间段", requiredMode = Schema.RequiredMode.REQUIRED)
    private String endTimeSegment;


}
