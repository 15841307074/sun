package com.htyoudao.youdao.module.promotion.dal.dataobject.points;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 积分记录对象 points_log
 *
 * @author Qizhongnan
 * @date 2024-02-03
 */
@Data
@TableName("points_log")
public class PointsLogDO
{
    @Serial
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long pointsLogId;

    /** 会员ID */
    private Long memberId;

    /** 手机号 */
    private String memberMobile;

    /** 商品名称 */
    private String productName;

    /** 订单编号 */
    private String orderSn;

    /** 订单总价 */
    private BigDecimal orderAmount;
    /**
     * 积分记录状态 1 正常 2 过期
     */
    private Integer pointsLogStatus;


    /** 积分商品ID */
    private Long productId;

    /** 积分商品价格 */
    private Long productPrice;

    private String logCode;

    /** 积分变更价格 */
    private Long pointsChange;
    /** 快递单号 */
    private String trackingNumber;

    /**
     * 快递公司
     */
    private String expressCompany;

    /**
     * 是否是积分商品
     */
    private Integer isPointsProduct;



    /** 用户名（登录名称） */

    private String memberName;

    /** 会员昵称 */

    private String memberNickName;

    /**
     * 收货地址
     */
    private String receiveAddress;

    /** 商品类型 1 优惠劵 2 实体积分商品 3 抽奖获取 */
    private Integer productType;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd hh:MM:ss")
    private Date expirationTime;

    /**
     * 积分类型(1-获取,2-兑换商品消耗,3-过期,4-冻结)
     */
    private Integer pointsType;
    /**
     * 分表字段 电话后两位取模
     */
    private Integer shardingValue;
    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    @ExcelIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    @ExcelIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name",fill = FieldFill.INSERT)
    @ExcelIgnore
    private String createUserName;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name",fill = FieldFill.UPDATE)
    @ExcelIgnore
    private String updateUserName;

    /**
     * 删除标识(0正常 1删除)
     */
    private Integer isDelete;
    /**
     * 项目id
     */
    private Long projectOwnerShip;
    /**
     * 项目id
     */
    private Long businessId;

}
