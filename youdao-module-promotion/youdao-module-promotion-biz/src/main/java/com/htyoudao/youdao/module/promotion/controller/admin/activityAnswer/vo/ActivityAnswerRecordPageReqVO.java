package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerRecordPageReqVO extends PageParam {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "会员手机号")
    private Long memberMobile;

    @Schema(description = "参与门店ID")
    private Long storeId;

    @Schema(description = "答题状态 0未完成 1已完成")
    private Integer status;

    @Schema(description = "答题状态 0未完成 1已完成，兼容前端字段")
    private Integer answerStatus;

    @Schema(description = "答题状态集合 0未完成 1已完成")
    private List<Integer> statusList;

    @Schema(description = "参与开始时间，支持 yyyy-MM-dd / yyyy-MM-dd HH:mm / yyyy-MM-dd HH:mm:ss")
    private String startTime;

    @Schema(description = "参与结束时间，支持 yyyy-MM-dd / yyyy-MM-dd HH:mm / yyyy-MM-dd HH:mm:ss")
    private String endTime;

    /** 获取格式化后的查询开始时间。 */
    public LocalDateTime getStartTime() {
        return ActivityAnswerTimeParser.parseStart(startTime);
    }

    /** 获取格式化后的查询结束时间。 */
    public LocalDateTime getEndTime() {
        return ActivityAnswerTimeParser.parseEnd(endTime);
    }

    /** 获取单个答题状态查询值。 */
    public Integer getQueryStatus() {
        return answerStatus != null ? answerStatus : status;
    }

    /** 获取答题状态集合查询值。 */
    public List<Integer> getQueryStatuses() {
        if (statusList != null && !statusList.isEmpty()) {
            return statusList.stream()
                    .filter(item -> item != null && (item == 0 || item == 1))
                    .distinct()
                    .toList();
        }
        Integer queryStatus = getQueryStatus();
        return queryStatus == null ? null : List.of(queryStatus);
    }
}
