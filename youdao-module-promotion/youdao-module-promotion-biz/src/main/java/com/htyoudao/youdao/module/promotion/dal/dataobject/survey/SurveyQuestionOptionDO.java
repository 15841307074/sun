package com.htyoudao.youdao.module.promotion.dal.dataobject.survey;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("survey_question_option")
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionOptionDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属题目ID */
    private Long questionId;

    /** 选项文本，限制50字 */
    private String optionText;

    /** 选项说明文字，限制100字 */
    private String optionDesc;

    /** 选项配图URL */
    private String optionImage;

    /** 是否允许该选项下填空：0-否 1-是 */
    private Integer allowFillBlank;

    /** 排序权重 */
    private Integer sortOrder;
}
