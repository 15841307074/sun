package com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

@TableName(value = "activity_vote_option", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityVoteOptionDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 投票活动ID */
    private Long activityId;
    /** 投票选项名称 */
    private String optionName;
    /** 投票选项主图 */
    private String optionUrl;
    /** 投票选项详情图 */
    private String optionDetailUrl;
    /** 详情 */
    private String voteDetail;
    /** 创建人姓名 */
    private String createUserName;
    /** 修改人姓名 */
    private String updateUserName;
}
