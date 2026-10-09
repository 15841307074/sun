package com.htyoudao.youdao.module.promotion.api.usercoupon.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 积分商品详情专用的优惠券完整信息。
 *
 * <p>独立于原有 GoodCouponVO，避免积分商城扩展影响现有领券和兑换流程。</p>
 */
@Data
@Schema(description = "积分商品详情优惠券完整信息")
public class PointsProductCouponDetailVO implements Serializable {

    private Long id;
    private String couponCode;
    private String couponName;
    private Integer couponType;
    private Integer couponNum;
    private Integer limitNum;
    private Integer communityFlag;
    private Date couponStartTime;
    private Date couponEndTime;
    private Integer receivedNum;
    private Integer usedNum;
    private String singleIds;
    private Integer fullReduction;
    private BigDecimal reduceAmount;
    private String discount;
    private Integer nameConcatenation;
    private Integer isGround;
    private Integer distributionMethod;
    private String couponExplain;
    private String useRules;
    @Schema(description = "优惠券图片地址")
    private String couponImageUrl;
    private String couponNameColor;
    private Integer useType;
    private String useTime;
    private String remark;
    private Integer isCommon;
    private String deptIds;
    private Integer userRestrictions;
    private Integer isShare;
    private Integer doorsillType;
    private BigDecimal doorsill;
    private Integer isCommonStore;
    private BigDecimal reliefOrDiscount;
    private BigDecimal payAmount;
    private String showTime;
    private String version;
    private Integer totalNum;
    private Integer memberLevel;
    private Integer dayLimit;
    private Integer couponNumVisible;
    private Integer habit;
    private String dayNumbers;
    private String weekNumbers;
    private String timeRange;
    private Integer isAllDay;
    private String couponBgImageUrl;
    private String miniSortUrl;
    private String h5SortUrl;
    private Integer exchangeFlag;
    private Integer claimTimeLimit;
    private String claimTimeSlot;
    private String claimDayNo;
    private String claimWeekNo;
    private String claimTime;
    private Integer storeLimitNum;
    private Integer storeTagFlag;
    private Integer testAfterDate;
    private String communityQrImage;
    private Long projectId;
    private Long businessId;
    private List<CouponStoreInfoVO> couponStores = new ArrayList<>();
    private List<CouponCommodityInfoVO> couponCommodities = new ArrayList<>();
}
