package com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 投票选项删除记录表（物理删除前备份）
 */
@TableName(value = "activity_vote_option_delete_log", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityVoteOptionDeleteLogDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 原选项ID */
    private Long optionId;
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
    /** 删除时间 */
    private LocalDateTime deleteTime;
    /** 操作人 */
    private String operator;
    /** 项目ID */
    private Long businessId;
}
