package com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("activity_answer_record_detail")
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerRecordDetailDO extends BusinessBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "活动主表ID")
    private Long activityId;

    @Schema(description = "答题记录ID")
    private Long recordId;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员手机号快照")
    private Long memberMobile;

    @Schema(description = "题目ID")
    private Long questionId;

    @Schema(description = "题目类型 1选择题")
    private Integer questionType;

    @Schema(description = "题目名称")
    private String questionTitle;

    @Schema(description = "题目提示文案")
    private String questionTips;

    @Schema(description = "选项JSON")
    private String optionsJson;

    @Schema(description = "正确答案选项编码")
    private String correctAnswer;

    @Schema(description = "是否已作答 0未作答 1已作答")
    private Integer isAnswered;

    @Schema(description = "用户选择答案编码")
    private String selectedAnswer;

    @Schema(description = "答题结果 0错误 1正确")
    private Integer answerResult;

    @Schema(description = "本题配置答题时间快照，单位秒")
    private Long answerTime;
}
