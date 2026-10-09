package com.htyoudao.youdao.module.promotion.dal.dataobject.couponshare;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 优惠券分享子 DO
 *
 * @author dht
 */
@TableName("coupon_share")
@KeySequence("coupon_share_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponShareDO extends BusinessBaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 优惠券id
     */
    private Long couponId;
    /**
     * 会员id
     */
    private Long memberId;
    /**
     * 会员手机号
     */
    private String memberMobile;
    /**
     * 发放数量
     */
    private Integer couponNum;
    /**
     * 分享标题
     */
    private String shareTitle;
    /**
     * 分享内容
     */
    private String shareNote;
    /**
     * 分享图片
     */
    private String shareLittleImgUrl;
    /**
     * 小程序分享路径
     */
    private String wxShareUrl;
    /**
     * H5分享路径
     */
    private String htmlShareUrl;
    /**
     * 二维码路径
     */
    private String erCodeUrl;
    /**
     * 海报路径
     */
    private String postersImgUrl;
    /**
     * 分享大图
     */
    private String shareLargeImgUrl;

}