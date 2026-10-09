package com.htyoudao.youdao.module.promotion.dal.dataobject.couponRedeem;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-07-02
 */
@Data
@TableName("douyin_coupon_redeem_record")
public class DouyinCouponRedeemRecordDO implements Serializable {
    @Serial
    private static final long serialVersionUID = 9052971104695970913L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户优惠券id
     */
    private Long userCouponId;

    /**
     * 券码
     */
    private String douyinCouponCode;

    /**
     * 抖音短链
     */
    private String shortLink;

    /**
     * 抖音订单id
     */
    private String douyinOrderId;

    /**
     * 平台券id
     */
    private Long platformCouponId;

    /**
     * 抖音门店id
     */
    private Long douyinStoreId;

    /**
     * 券码准备响应
     */
    private String prepareResponse;

    /**
     * 券码验券响应
     */
    private String verifyResponse;

    /**
     * 券码状态 0未使用，1已核销
     */
    private Integer status;

    /**
     * 项目 id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long businessId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;
}
