package com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckillCommodity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;
import java.math.BigDecimal;


@TableName("activity_seckill_commodity")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySeckillCommodityDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -7070525505627059503L;

    // 主键
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    // 秒杀活动ID
    private Long activityId;
    // 商品ID
    private Long commodityId;

    // 秒杀价格（小数点后两位）
    private BigDecimal seckillPrice;


    // 活动库存
    private Integer activityStock;
    // 划线价格（小数点后两位）
    private BigDecimal linePrice;
    // 单品限购数
    private Integer limitPerItem;
    //规格 id
    private Long skuId;
    //商品价格
    private BigDecimal commodityPrice;

    private String commodityName;

}
