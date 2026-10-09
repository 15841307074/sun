package com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("activity_answer_question")
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerQuestionDO extends BusinessBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "活动主表ID")
    private Long activityId;

    @Schema(description = "题目类型 1选择题")
    private Integer questionType;

    @Schema(description = "题干")
    private String questionTitle;

    @Schema(description = "题目提示文案")
    private String questionTips;

    @Schema(description = "选项JSON")
    private String optionsJson;

    @Schema(description = "正确答案选项编码")
    private String correctAnswer;

    @Schema(description = "每题答题时间 单位秒 0不限制")
    private Integer answerTime;
}
