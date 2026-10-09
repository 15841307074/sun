package com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryHelp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDate;

@Data
@TableName("lottery_help")
public class LotteryHelpDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long activityId;

    private Long inviterMemberId;

    private Long inviteeMemberId;

    private LocalDate helpDate;

    /**
     * 按天/按场次作用域唯一键。
     * 按天时为 yyyyMMdd，按场次时为 yyyyMMdd_HHmmHHmm / ALLDAY。
     */
    private String scopeKey;
}
