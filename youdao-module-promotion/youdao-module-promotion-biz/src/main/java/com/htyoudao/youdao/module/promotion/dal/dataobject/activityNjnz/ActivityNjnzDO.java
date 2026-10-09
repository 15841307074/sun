package com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnz;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;

/**
 * @author villky
 * 营销活动表
 */
@TableName(value = "activity_njnz", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityNjnzDO  extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -7070525505627059503L;
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long activityId;

    private Integer discountType;

    /**
     * 优惠第几件
     */
    private Integer discountItemNum;

    /**
     * 优惠打几折
     */
    private Double discountRate;

    /**
     * 活动商品（1全部商品可用 2指定商品可用）
     */
    private Integer activityProduct;


}