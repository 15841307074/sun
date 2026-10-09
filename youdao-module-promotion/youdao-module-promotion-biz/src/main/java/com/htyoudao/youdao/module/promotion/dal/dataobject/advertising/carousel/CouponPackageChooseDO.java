package com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.carousel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("coupon_package")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CouponPackageChooseDO extends BusinessBaseDO implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 优惠券包名称
     * */
    private String packageName;

    /**
     * 优惠券包备注
     */
    private String remark;

    /**
     * 领取限制 0 不限制 1 新注册用户 2 老用户 3 回归用户
     */
    private Integer userRestrictions;

    /**
     * 已领取数目
     */
    private Integer receivedNum;

    /**
     * 剩余数目
     */
    private Integer packageNum;

    /**
     * 0 普通券包 1 周周惠券包
     */
    private Integer packageType;

    /**
     * 是否上架(0-否,1-是)
     */
    private Integer isGround;

}
