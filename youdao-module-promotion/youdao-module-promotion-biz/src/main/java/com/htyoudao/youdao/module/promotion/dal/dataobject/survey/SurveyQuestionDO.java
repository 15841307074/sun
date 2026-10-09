package com.htyoudao.youdao.module.promotion.dal.dataobject.survey;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("survey_question")
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属问卷ID */
    private Long surveyId;

    /** 题目序号 */
    private Integer questionNo;

    /** 题目标题，限制100字 */
    private String questionTitle;

    /** 题目提示/说明，限制100字 */
    private String questionHint;

    /** 题目类型：1-单选题 2-多选题 3-填空题 */
    private Integer questionType;

    /** 题目配图URL */
    private String questionImage;

    /** 是否必答：0-否 1-是 */
    private Integer isRequired;

    /** 是否显示题目提示：0-否 1-是（开启后 questionHint 生效） */
    private Integer showHint;

    /** 排序权重 */
    private Integer sortOrder;
}
