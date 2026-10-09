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
import java.time.LocalDateTime;

@TableName(value = "activity_vote_log", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityVoteLogDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 投票活动ID */
    private Long activityId;
    /** 投票选项ID */
    private Long optionId;
    /** 选项名称快照 */
    private String optionName;
    /** 会员ID */
    private Long memberId;
    /** 会员手机号快照 */
    private Long memberMobile;
    /** 会员昵称快照 */
    private String memberName;
    /** 会员头像快照 */
    private String memberAvatar;
    /** 参与门店ID */
    private Long storeId;
    /** 参与门店名称 */
    private String storeName;
    /** 投票时间 */
    private LocalDateTime voteTime;
}
