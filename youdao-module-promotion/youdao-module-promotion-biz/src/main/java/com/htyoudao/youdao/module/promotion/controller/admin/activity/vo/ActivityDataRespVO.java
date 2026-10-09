package com.htyoudao.youdao.module.promotion.controller.admin.activity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
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
public class ActivityDataRespVO {


    @Schema(description = "主键", requiredMode = RequiredMode.REQUIRED)
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;


    @Schema(description = "活动名称", requiredMode = RequiredMode.REQUIRED)
    private String activityName;


    @Schema(description = "活动类型 1（n件n折）", requiredMode = RequiredMode.REQUIRED)
    private Integer activityType;

    @Schema(description = "是否上架(0不开启 1开启)")
    private Integer isEnabled;


}
