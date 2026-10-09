package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponcommodity.CouponCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreDO;
import com.htyoudao.youdao.module.promotion.framework.config.NullStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 优惠券 Response VO")
@Data
public class GoodCouponRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "15444")
    private Long id;

    @Schema(description = "优惠券编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String couponCode;

    @Schema(description = "优惠券名字的颜色")
    private String couponNameColor;

    @Schema(description = "优惠券名称", example = "0090")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String couponName;

    @Schema(description = "优惠券类型(0-满减券,1-折扣券)", example = "1")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String couponType;

    /**
     * 兑换券是否需要绑定商品 0否 1是
     */
    private Integer exchangeFlag;

    @Schema(description = "优惠券剩余数目")
    private Integer couponNum;

    @Schema(description = "每人限领数目", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer limitNum;

    @Schema(description = "指定门店id", example = "16720")
    private Long storeId;

    @Schema(description = "0不需要进社群 1需要进社群", example = "0")
    private Integer communityFlag;

    @Schema(description = "优惠券有效开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date couponStartTime;

    @Schema(description = "优惠券有效结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date couponEndTime;

    @Schema(description = "已领取数目")
    private Integer receivedNum;

    @Schema(description = "已使用数目")
    private Integer usedNum;

    @Schema(description = "单品id")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String singleIds;

    @Schema(description = "满减金额")
    private Integer fullReduction;

    @Schema(description = "减少金额")
    private BigDecimal reduceAmount;

    @Schema(description = "绑定门店标签 0 绑定 1 不绑定", example = "1")
    private Integer storeTagFlag;

    @Schema(description = "绑定门店标签ids", example = "1")
    private List<Long> storeTagIds;

    @Schema(description = "折扣", example = "19645")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String discount;

    @Schema(description = "名字拼接,0拼 1不拼", example = "1")
    private Integer nameConcatenation;

    @Schema(description = "是否上架(0-否,1-是)")
    private Integer isGround;

    @Schema(description = "领取方式(0-自动发放,1-手动领取)")
    private Integer distributionMethod;

    @Schema(description = "优惠券说明")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String couponExplain;

    @Schema(description = "使用规则")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String useRules;

    @Schema(description = "优惠券图片", example = "https://www.iocoder.cn")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String couponImageUrl;

    @Schema(description = "持续时间")
    private Integer duration;

    @Schema(description = "创建人", example = "0090")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String createUserName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改人", example = "张三")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String updateUserName;

    @Schema(description = "逻辑删除")
    private Integer isDelete;

    @Schema(description = "使用类型 0 时间段 1 立即生效 2 领取N天后生效", example = "1")
    private Integer useType;

    @Schema(description = "使用时间信息，根据use_type而定")
    private String useTime;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "适用门店范围 1:通用 2:门店券")
    private Integer isCommon;

    @Schema(description = "指定组织ids")
    private String deptIds;

    @Schema(description = "项目id", example = "27303")
    private Long projectId;

    @Schema(description = "项目归属ID")
    private Long projectOwnerShip;

    @Schema(description = "领取人限制 （0 不限制 ，1 新注册用户 ，2 老用户，3回归用户）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer userRestrictions;

    @Schema(description = "分享设置  0 不允许转发 1 允许转发给好友 2 允许复制链接分享给好友", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isShare;

    @Schema(description = "使用门槛类型 （0 无门槛 1 价格门槛 2 商品件数门槛）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer doorsillType;

    @Schema(description = "门槛金额/件数 ")
    private BigDecimal doorsill;

    @Schema(description = "适用商品范围 1 通用 2指定商品可用 3指定商品不可用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isCommonStore;

    @Schema(description = "减免/折扣", example = "20557")
    private BigDecimal reliefOrDiscount;

    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @Schema(description = "显示时间")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String showTime;

    @Schema(description = "人群id")
    private String version;

    @Schema(description = "发放总量")
    private Integer totalNum;

    @Schema(description = "会员等级券 1 2 3 4 5")
    private Integer memberLevel;

    @Schema(description = "每日领取数量限制")
    private Integer dayLimit;

    @Schema(description = "优惠券剩余数目可见性(0-可见,1-不可见)")
    private Integer couponNumVisible;

    @Schema(description = "用餐方式 0 全部可用 1堂食可用 2外卖可用")
    private Integer habit;

    @Schema(description = "指定日期逗号分割")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String dayNumbers;

    @Schema(description = "指定周几逗号分割")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String weekNumbers;

    @Schema(description = "指定时间段")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String timeRange;

    @Schema(description = "是否全天时段")
    private Integer isAllDay;

    @Schema(description = "优惠券背景图片", example = "https://www.iocoder.cn")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String couponBgImageUrl;

    @Schema(description = "微信分享短连接", example = "https://www.iocoder.cn")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String miniSortUrl;

    @Schema(description = "H5分享短连接", example = "https://www.iocoder.cn")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String h5SortUrl;

    @Schema(description = "优惠券适用商品")
    List<CouponCommodityDO> couponCommodities = new ArrayList<>();

    @Schema(description = "优惠券适用门店")
    List<CouponStoreDO> couponStores = new ArrayList<>();

    @Schema(description = "优惠券适用门店")
    List<ExchangeCommodityRespVO> exchangeCommodityList = new ArrayList<>();

    /**
     * 是否过期
     */
    @Schema(description = "是否过期")
    private Boolean outOfDate;

    /**
     * 是否不够了
     */
    @Schema(description = "是否不够了")
    private Boolean isSurplus;

    @Schema(description = "适用商品")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String couponCommodityNames;

    @Schema(description = "适用门店")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String couponStoreNames;

    /**
     * 领取时间限制 0 不限制 1 限制时间
     */
    private Integer claimTimeLimit;

    /**
     * 领取时间 指定日期段 年月日#年月日
     */
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String claimTimeSlot;

    /**
     * 领取时间指定几号 1#2#6
     */
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String claimDayNo;

    /**
     * 领取时间指定周几 1#3
     */
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String claimWeekNo;

    /**
     * 领取时间指定时间 时分秒#时分秒
     */
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String claimTime;

    /**
     * 门店领取限制 0 不限制
     */
    private Integer storeLimitNum;

    @Schema(description = "分享信息")
    private CouponShareRespVO couponShareRespVO;

    /**
     * 失效时间 纯前端用
     */
    private Integer testAfterDate;

    private Long businessId;

    @Schema(description = "社群二维码")
    private String communityQrImage;
}