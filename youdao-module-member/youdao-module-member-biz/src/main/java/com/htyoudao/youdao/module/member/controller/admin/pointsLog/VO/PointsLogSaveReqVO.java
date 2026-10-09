package com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsProduct.PointsProductDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
@Schema(description = "管理后台 - 积分记录新增 Request VO")
@Data
public class PointsLogSaveReqVO {


    @Schema(description = "积分商品 id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long productId;

    @Schema(description = "记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pointsLogId;

    @Schema(description = "会员id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memberId;

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderSn;

    @Schema(description = "快递单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String trackingNumber;

    @Schema(description = "收货地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiveAddress;

    @Schema(description = "会员昵称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberNickName;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberMobile;


    @Schema(description = "快递公司", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expressCompany;


    @Schema(description = "订单总价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal orderAmount;

    @Schema(description = "积分记录状态 1 正常 2 过期", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pointsLogStatus;

    @Schema(description = "积分商品价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productPrice;

    @Schema(description = "日志编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String logCode;

    /** 积分变更价格 */
    @Schema(description = "积分变更价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pointsChange;


    @Schema(description = "是否是积分商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isPointsProduct;

    @Schema(description = "积分商品", requiredMode = Schema.RequiredMode.REQUIRED)
    PointsProductDO pointsProductDO;


    @Schema(description = "用户名（登录名称）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberName;


    @Schema(description = "商品类型 1 优惠劵 2 实体积分商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer productType;


    @Schema(description = "过期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd hh:MM:ss")
    private Date expirationTime;


    @Schema(description = "积分类型(1-获取,2-兑换商品消耗,3-过期,4-冻结)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pointsType;

    @Schema(description = "分表字段 电话后两位取模", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer shardingValue;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date createTime;


    @Schema(description = "修改时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;


    @Schema(description = "创建人名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(value = "create_user_name",fill = FieldFill.INSERT)
    private String createUserName;


    @Schema(description = "修改人名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(value = "update_user_name",fill = FieldFill.UPDATE)
    private String updateUserName;

    @Schema(description = "删除标识(0正常 1删除)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isDelete;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long projectOwnerShip;


}
