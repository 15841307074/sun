package com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzCommodity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;

/**
 * 满赠活动关联商品表（参与活动商品）
 */
@TableName("activity_mz_commodity")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMzCommodityDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

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
