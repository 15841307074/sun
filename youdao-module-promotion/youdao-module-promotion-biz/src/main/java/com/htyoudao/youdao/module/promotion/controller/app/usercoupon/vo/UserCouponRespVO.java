package com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponshare.CouponShareDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 用户优惠券 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UserCouponRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "31674")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "优惠券编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("优惠券编码")
    private String couponCode;

    @Schema(description = "优惠券名称", example = "赵六")
    @ExcelProperty("优惠券名称")
    private String couponName;

    @Schema(description = "优惠券类型(0-满减券,1-折扣券)", example = "2")
    @ExcelProperty("优惠券类型(0-满减券,1-折扣券)")
    private Integer couponType;

    @Schema(description = "优惠券剩余数目")
    @ExcelProperty("优惠券剩余数目")
    private Integer couponNum;

    @Schema(description = "每人限领数目", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("每人限领数目")
    private Integer limitNum;

    @Schema(description = "指定门店id", example = "13134")
    @ExcelProperty("指定门店id")
    private String storeId;

    @Schema(description = "优惠券有效开始时间")
    @ExcelProperty("优惠券有效开始时间")
    private LocalDateTime couponStartTime;

    @Schema(description = "优惠券有效结束时间")
    @ExcelProperty("优惠券有效结束时间")
    private LocalDateTime couponEndTime;

    @Schema(description = "已领取数目")
    @ExcelProperty("已领取数目")
    private Integer receivedNum;

    @Schema(description = "已使用数目")
    @ExcelProperty("已使用数目")
    private Integer usedNum;

    @Schema(description = "单品id")
    @ExcelProperty("单品id")
    private String singleIds;

    @Schema(description = "满减金额")
    @ExcelProperty("满减金额")
    private BigDecimal fullReduction;

    @Schema(description = "减少金额")
    @ExcelProperty("减少金额")
    private BigDecimal reduceAmount;

    @Schema(description = "折扣", example = "12537")
    @ExcelProperty("折扣")
    private String discount;

    @Schema(description = "优惠券状态(0-待使用,1-已使用,2-未开始,3-进行中,4-已到期)", example = "2")
    @ExcelProperty("优惠券状态(0-待使用,1-已使用,2-未开始,3-进行中,4-已到期)")
    private Integer couponStatus;

    @Schema(description = "是否上架(0-否,1-是)")
    @ExcelProperty("是否上架(0-否,1-是)")
    private Integer isGround;

    @Schema(description = "领取方式(0-自动发放,1-手动领取)")
    @ExcelProperty("领取方式(0-自动发放,1-手动领取)")
    private Integer distributionMethod;

    @Schema(description = "优惠券说明")
    @ExcelProperty("优惠券说明")
    private String couponExplain;

    @Schema(description = "使用规则")
    @ExcelProperty("使用规则")
    private String useRules;

    @Schema(description = "优惠券图片", example = "https://www.iocoder.cn")
    @ExcelProperty("优惠券图片")
    private String couponImageUrl;

    @Schema(description = "持续时间")
    @ExcelProperty("持续时间")
    private Integer duration;

    @Schema(description = "创建人", example = "0090")
    @ExcelProperty("创建人")
    private String createUserName;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime couponCreateTime;

    @Schema(description = "修改人", example = "李四")
    @ExcelProperty("修改人")
    private String updateUserName;

    @Schema(description = "逻辑删除")
    @ExcelProperty("逻辑删除")
    private Integer isDelete;

    @Schema(description = "使用类型 0 时间段 1 立即生效 2 领取N天后生效", example = "1")
    @ExcelProperty("使用类型 0 时间段 1 立即生效 2 领取N天后生效")
    private Integer useType;

    @Schema(description = "使用时间信息，根据use_type而定")
    @ExcelProperty("使用时间信息，根据use_type而定")
    private String couponUseTime;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "适用门店范围 1:通用 2:门店券")
    @ExcelProperty("适用门店范围 1:通用 2:门店券")
    private Integer isCommon;

    @Schema(description = "指定组织ids")
    @ExcelProperty("指定组织ids")
    private String deptIds;

    @Schema(description = "项目id", example = "25419")
    @ExcelProperty("项目id")
    private Long projectId;

    @Schema(description = "项目归属ID")
    @ExcelProperty("项目归属ID")
    private Long projectOwnerShip;

    @Schema(description = "领取人限制 （0 不限制 1 新注册用户 2 老用户）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("领取人限制 （0 不限制 1 新注册用户 2 老用户）")
    private Boolean userRestrictions;

    @Schema(description = "分享设置  0 不允许转发 1 允许转发给好友 2 允许复制链接分享给好友", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("分享设置  0 不允许转发 1 允许转发给好友 2 允许复制链接分享给好友")
    private Boolean isShare;

    @Schema(description = "使用门槛类型 （0 无门槛 1 价格门槛 2 商品件数门槛）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("使用门槛类型 （0 无门槛 1 价格门槛 2 商品件数门槛）")
    private Boolean doorsillType;

    @Schema(description = "门槛金额/件数 ")
    @ExcelProperty("门槛金额/件数 ")
    private BigDecimal doorsill;

    @Schema(description = "适用商品范围 1 通用 2指定商品可用 3指定商品不可用", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("适用商品范围 1 通用 2指定商品可用 3指定商品不可用")
    private Integer isCommonStore;

    @Schema(description = "减免/折扣", example = "24242")
    @ExcelProperty("减免/折扣")
    private BigDecimal reliefOrDiscount;

    @Schema(description = "支付金额")
    @ExcelProperty("支付金额")
    private BigDecimal payAmount;

    @Schema(description = "显示时间")
    @ExcelProperty("显示时间")
    private String showTime;

    @Schema(description = "版本号(乐观锁)")
    @ExcelProperty("版本号(乐观锁)")
    private Long version;

    @Schema(description = "发放总量")
    @ExcelProperty("发放总量")
    private Integer totalNum;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "29012")
    @ExcelProperty("用户id")
    private Long userId;

    @Schema(description = "是否使用(0-待使用,1-已使用)")
    @ExcelProperty("是否使用(0-待使用,1-已使用)")
    private Integer isUsed;

    @Schema(description = "领取时间")
    @ExcelProperty("领取时间")
    private LocalDateTime createTime;

    @Schema(description = "到期时间")
    @ExcelProperty("到期时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd")
    private LocalDateTime expirationTime;

    @Schema(description = "有效开始日期")
    @ExcelProperty("有效开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd")
    private LocalDateTime vaildStartTime;

    @Schema(description = "使用时间")
    @ExcelProperty("使用时间")
    private LocalDateTime useTime;

    @Schema(description = "是否是新用户 0是 1不是")
    @ExcelProperty("是否是新用户 0是 1不是")
    private Integer isNew;

    @Schema(description = "优惠券主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "8817")
    @ExcelProperty("优惠券主键")
    private Long couponId;

    @Schema(description = "用户手机号")
    @ExcelProperty("用户手机号")
    private String memberMobile;

    @Schema(description = "优惠券来源 0自领 1推广 2积分商城")
    @ExcelProperty("优惠券来源 0自领 1推广 2积分商城")
    private Integer couponSource;

    @Schema(description = "优惠券包id", example = "268")
    @ExcelProperty("优惠券包id")
    private Long packageId;

    @Schema(description = "优惠券剩余数目可见性(0-可见,1-不可见)")
    @ExcelProperty("优惠券剩余数目可见性(0-可见,1-不可见)")
    private Integer couponNumVisible;

    @Schema(description = "用餐方式 0 全部可用 1堂食可用 2外卖可用")
    @ExcelProperty("用餐方式 0 全部可用 1堂食可用 2外卖可用")
    private Integer habit;

    @Schema(description = "优惠券背景图片", example = "https://www.iocoder.cn")
    @ExcelProperty("优惠券背景图片")
    private String couponBgImageUrl;

    @ExcelProperty("优惠券分享信息")
    private CouponShareRespVO couponShare;
}