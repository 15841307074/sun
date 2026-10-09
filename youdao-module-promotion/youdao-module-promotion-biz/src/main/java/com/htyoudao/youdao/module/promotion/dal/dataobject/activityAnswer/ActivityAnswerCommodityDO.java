package com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@TableName("activity_answer_commodity")
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerCommodityDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 599272681144168375L;
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 商品ID
     */
    private Long commodityId;
}
