package com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 用户优惠券新增/修改 Request VO")
@Data
public class CouponSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "31674")
    private Long id;

    @Schema(description = "优惠券编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "优惠券编码不能为空")
    private String couponCode;

    @Schema(description = "优惠券名称", example = "赵六")
    private String couponName;

    @Schema(description = "优惠券类型(0-满减券,1-折扣券)", example = "2")
    private Integer couponType;

    @Schema(description = "优惠券剩余数目")
    private Integer couponNum;

    @Schema(description = "每人限领数目", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "每人限领数目不能为空")
    private Integer limitNum;

    @Schema(description = "指定门店id", example = "13134")
    private String storeId;

    @Schema(description = "优惠券有效开始时间")
    private LocalDateTime couponStartTime;

    @Schema(description = "优惠券有效结束时间")
    private LocalDateTime couponEndTime;

    @Schema(description = "已领取数目")
    private Integer receivedNum;

    @Schema(description = "已使用数目")
    private Integer usedNum;

    @Schema(description = "单品id")
    private String singleIds;

    @Schema(description = "满减金额")
    private BigDecimal fullReduction;

    @Schema(description = "减少金额")
    private BigDecimal reduceAmount;

    @Schema(description = "折扣", example = "12537")
    private String discount;

    @Schema(description = "优惠券状态(0-待使用,1-已使用,2-未开始,3-进行中,4-已到期)", example = "2")
    private Integer couponStatus;

    @Schema(description = "是否上架(0-否,1-是)")
    private Integer isGround;

    @Schema(description = "领取方式(0-自动发放,1-手动领取)")
    private Integer distributionMethod;

    @Schema(description = "优惠券说明")
    private String couponExplain;

    @Schema(description = "使用规则")
    private String useRules;

    @Schema(description = "优惠券图片", example = "https://www.iocoder.cn")
    private String couponImageUrl;

    @Schema(description = "持续时间")
    private Integer duration;

    @Schema(description = "创建人", example = "0090")
    private String createUserName;

    @Schema(description = "创建时间")
    private LocalDateTime couponCreateTime;

    @Schema(description = "修改人", example = "李四")
    private String updateUserName;

    @Schema(description = "逻辑删除")
    private Integer isDelete;

    @Schema(description = "使用类型 0 时间段 1 立即生效 2 领取N天后生效", example = "1")
    private Integer useType;

    @Schema(description = "使用时间信息，根据use_type而定")
    private String couponUseTime;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "适用门店范围 1:通用 2:门店券")
    private Integer isCommon;

    @Schema(description = "指定组织ids")
    private String deptIds;

    @Schema(description = "项目id", example = "25419")
    private Long projectId;

    @Schema(description = "项目归属ID")
    private Long projectOwnerShip;

    @Schema(description = "领取人限制 （0 不限制 1 新注册用户 2 老用户）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "领取人限制 （0 不限制 1 新注册用户 2 老用户）不能为空")
    private Boolean userRestrictions;

    @Schema(description = "分享设置  0 不允许转发 1 允许转发给好友 2 允许复制链接分享给好友", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分享设置  0 不允许转发 1 允许转发给好友 2 允许复制链接分享给好友不能为空")
    private Boolean isShare;

    @Schema(description = "使用门槛类型 （0 无门槛 1 价格门槛 2 商品件数门槛）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "使用门槛类型 （0 无门槛 1 价格门槛 2 商品件数门槛）不能为空")
    private Boolean doorsillType;

    @Schema(description = "门槛金额/件数 ")
    private BigDecimal doorsill;

    @Schema(description = "适用商品范围 1 通用 2指定商品可用 3指定商品不可用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "适用商品范围 1 通用 2指定商品可用 3指定商品不可用不能为空")
    private Boolean isCommonStore;

    @Schema(description = "减免/折扣", example = "24242")
    private BigDecimal reliefOrDiscount;

    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @Schema(description = "显示时间")
    private String showTime;

    @Schema(description = "版本号(乐观锁)")
    private Long version;

    @Schema(description = "发放总量")
    private Integer totalNum;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "29012")
    @NotNull(message = "用户id不能为空")
    private Long userId;

    @Schema(description = "是否使用(0-待使用,1-已使用)")
    private Integer isUsed;

    @Schema(description = "到期时间")
    private LocalDateTime expirationTime;

    @Schema(description = "有效开始日期")
    private LocalDateTime vaildStartTime;

    @Schema(description = "使用时间")
    private LocalDateTime useTime;

    @Schema(description = "是否是新用户 0是 1不是")
    private Integer isNew;

    @Schema(description = "优惠券主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "8817")
    @NotNull(message = "优惠券主键不能为空")
    private Long couponId;

    @Schema(description = "用户手机号")
    private String memberMobile;

    @Schema(description = "优惠券来源 0自领 1推广 2积分商城")
    private Integer couponSource;

    @Schema(description = "优惠券包id", example = "268")
    private Long packageId;

    @Schema(description = "优惠券剩余数目可见性(0-可见,1-不可见)")
    private Integer couponNumVisible;

    @Schema(description = "用餐方式 0 全部可用 1堂食可用 2外卖可用")
    private Integer habit;

    @Schema(description = "优惠券背景图片", example = "https://www.iocoder.cn")
    private String couponBgImageUrl;

}