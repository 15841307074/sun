package com.htyoudao.youdao.module.promotion.dal.dataobject.survey;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("survey_answer_detail")
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswerDetailDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 答卷ID */
    private Long answerId;

    /** 问卷ID（冗余，方便统计） */
    private Long surveyId;

    /** 题目ID */
    private Long questionId;

    /** 题目序号（冗余） */
    private Integer questionNo;

    /** 选中的选项ID列表，逗号分隔（单选/多选） */
    private String selectedOptionIds;

    /** 填空/其他补充文本 */
    private String fillBlankText;
}
