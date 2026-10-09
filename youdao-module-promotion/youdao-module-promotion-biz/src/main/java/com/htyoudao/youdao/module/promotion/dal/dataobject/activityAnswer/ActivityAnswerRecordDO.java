package com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("activity_answer_record")
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerRecordDO extends BusinessBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "答题编号")
    private String answerNo;

    @Schema(description = "活动主表ID")
    private Long activityId;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员手机号")
    private Long memberMobile;

    @Schema(description = "活动场次/周期标识")
    private String periodKey;

    @Schema(description = "会员昵称")
    private String memberName;

    @Schema(description = "参与门店ID")
    private Long storeId;

    @Schema(description = "参与门店名称")
    private String storeName;

    @Schema(description = "题目数量")
    private Integer questionCount;

    @Schema(description = "正确数量")
    private Integer correctCount;

    @Schema(description = "错误数量")
    private Integer wrongCount;

    @Schema(description = "正确率")
    private BigDecimal accuracy;

    @Schema(description = "答题状态 0未完成 1已完成")
    private Integer status;

    @Schema(description = "取消状态 0未取消 1已取消")
    private Integer cancelStatus;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;
}
