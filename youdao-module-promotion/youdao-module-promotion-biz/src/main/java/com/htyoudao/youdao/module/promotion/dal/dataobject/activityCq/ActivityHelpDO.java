package com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDate;

@Data
@TableName("activity_help")
public class ActivityHelpDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动id
     */
    private Long activityId;

    /**
     * 邀请人会员id
     */
    private Long inviterMemberId;

    /**
     * 被邀请人会员id
     */
    private Long inviteeMemberId;

    /**
     * 助力日期
     */
    private LocalDate helpDate;

    /**
     * 按天作用域key
     */
    private String scopeKey;
}
