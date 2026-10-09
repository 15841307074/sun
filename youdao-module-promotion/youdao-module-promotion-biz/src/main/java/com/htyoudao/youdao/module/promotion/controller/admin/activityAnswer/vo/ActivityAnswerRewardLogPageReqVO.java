package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerRewardLogPageReqVO extends PageParam {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "会员手机号")
    private Long memberMobile;

    @Schema(description = "奖品类型")
    private Integer prizeType;

    @Schema(description = "红包领取状态 1未领取 2已领取 3已失效")
    private Integer claimStatus;

    @Schema(description = "奖品状态 0已发放 1未填写地址 2待发货 3已发货 9退回")
    private Integer prizeState;

    @Schema(description = "收货地址筛选 0全部 1未填写 2已填写")
    private Integer receiveAddressType;

    @Schema(description = "物流单号筛选 0全部 1未填写 2已填写")
    private Integer trackingType;

    @Schema(description = "发放开始时间，支持 yyyy-MM-dd / yyyy-MM-dd HH:mm / yyyy-MM-dd HH:mm:ss")
    private String startTime;

    @Schema(description = "发放结束时间，支持 yyyy-MM-dd / yyyy-MM-dd HH:mm / yyyy-MM-dd HH:mm:ss")
    private String endTime;

    @Schema(description = "发放时间范围 [开始时间, 结束时间]")
    private List<String> activityTime;

    /** 获取查询开始时间。 */
    public LocalDateTime getStartTime() {
        String value = startTime;
        if ((value == null || value.isBlank()) && activityTime != null && !activityTime.isEmpty()) {
            value = activityTime.get(0);
        }
        return ActivityAnswerTimeParser.parseStart(value);
    }

    /** 获取查询结束时间。 */
    public LocalDateTime getEndTime() {
        String value = endTime;
        if ((value == null || value.isBlank()) && activityTime != null && activityTime.size() > 1) {
            value = activityTime.get(1);
        }
        return ActivityAnswerTimeParser.parseEnd(value);
    }

    /** 获取奖品类型查询值。 */
    public Integer getQueryPrizeType() {
        return prizeType;
    }

    /** 将前端收货地址筛选值转换为数据库查询值。 */
    public Integer getQueryAddressStatus() {
        return normalizeFilledStatus(receiveAddressType);
    }

    /** 将前端物流单号筛选值转换为数据库查询值。 */
    public Integer getQueryExpressStatus() {
        return normalizeFilledStatus(trackingType);
    }

    /** 0表示全部，1表示未填写，2表示已填写。 */
    private Integer normalizeFilledStatus(Integer value) {
        if (value == null || value == 0) {
            return null;
        }
        return value == 1 ? 0 : 1;
    }
}
