package com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 集卡助力记录
 */
@Data
@TableName("activity_jk_help")
public class ActivityJkHelpDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动 ID
     */
    private Long activityId;

    /**
     * 邀请人会员 ID
     */
    private Long inviterMemberId;

    /**
     * 被邀请人会员 ID
     */
    private Long inviteeMemberId;

    /**
     * 助力日期
     */
    private LocalDate helpDate;
}
