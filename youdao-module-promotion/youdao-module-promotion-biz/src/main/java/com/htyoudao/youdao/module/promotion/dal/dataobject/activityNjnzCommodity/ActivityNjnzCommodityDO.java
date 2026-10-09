package com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnzCommodity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;

/**
 * @author villky
 * 营销活动关联商品表
 */
@TableName("activity_njnz_commodity")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActivityNjnzCommodityDO extends BusinessBaseDO {
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
