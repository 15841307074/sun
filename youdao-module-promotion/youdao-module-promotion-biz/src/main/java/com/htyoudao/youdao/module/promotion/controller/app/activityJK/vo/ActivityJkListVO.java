package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkCardDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkPrizeDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 活动详情响应VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "活动详情响应")
public class ActivityJkListVO {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动标题")
    private String activityTitle;
    @Schema(description = "活动封面图")
    private String activityCoverImage;
    @Schema(description = "活动类型 1 转盘 2 九宫格 3 福袋  4 盲盒 5 集卡 ")
    private Long  activityType;

}
