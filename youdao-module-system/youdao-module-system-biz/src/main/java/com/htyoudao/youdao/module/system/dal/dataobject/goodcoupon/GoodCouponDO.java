package com.htyoudao.youdao.module.system.dal.dataobject.goodcoupon;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 优惠券对象 good_coupon
 *
 * @author ruoyi
 * @date 2024-02-04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("good_coupon")
@Schema(name = "优惠券实体", description = "优惠券实体")
public class GoodCouponDO extends FlexBaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 优惠券名称 */

    /**
     * 优惠券名称
     */
    @ExcelProperty("优惠券名称")
    @Schema(description = "优惠券名称")
    private String couponName;

    /**
     * 优惠券编码
     */
    private String couponCode;

    /**
     * 优惠券类型(0-满减券,1-直减券,2-折扣券)
     */
    @ExcelProperty("优惠券类型(0-满减券,1-直减券,2-折扣券)")
    private Integer couponType;

    /**
     * 使用类型 0 时间段 1 立即生效 2 领取N天后生效
     */
    private Integer useType;

    /**
     * 使用时间信息，根据use_type而定
     * <p>
     * 0 2024-01-01 12:12:12#2024-02-01 12:12:12
     * 1 3
     * 2 2#5
     */
    private String useTime;

    /**
     * 备注
     */
    private String remark;

    private transient String couponTypeName;

    /**
     * 优惠券剩余数目
     */
    @ExcelProperty("优惠券剩余数目")
    private Integer couponNum;

    /**
     * 每人限领数目
     */
    @ExcelProperty("每人限领数目")
    private Integer limitNum;

    /**
     * 发放总量
     */
    @ExcelProperty("发放总量")
    private Integer totalNum;

    /**
     * 指定门店id
     */
    @ExcelProperty("指定门店id")
    private String storeId;

    /**
     * 优惠券有效开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ExcelProperty("优惠券有效开始时间")
    private Date couponStartTime;

    /**
     * 优惠券有效结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ExcelProperty("优惠券有效结束时间")
    private Date couponEndTime;

    /**
     * 已领取数目
     */
    @ExcelProperty("已领取数目")
    private Integer receivedNum;

    /**
     * 已使用数目
     */
    @ExcelProperty("已使用数目")
    private Integer usedNum;

    /**
     * 单品id
     */
    @ExcelProperty("商品id")
    private String singleIds;

    /**
     * 满减金额
     */
    @ExcelProperty("满减金额")
    private BigDecimal fullReduction;

    /**
     * 减少金额
     */
    @ExcelProperty("减少金额")
    private BigDecimal reduceAmount;

    /**
     * 折扣
     */
    @ExcelProperty("折扣")
    private String discount;

    /**
     * 优惠券剩余数目可见性(0-可见,1-不可见)
     */
    private Integer couponNumVisible;


    /**
     * 优惠券状态(0-待使用,1-已使用,2-未开始,3-进行中,4-已到期)
     */
    private Integer couponStatus;

    private transient String couponStatusName;

    /**
     * 领取方式(0-自动发放,1-手动领取)
     */
    private Integer distributionMethod;

    /**
     * 持续时间
     */
    private Integer duration;

    /**
     * 使用规则
     */
    private String useRules;

    /**
     * 优惠券说明
     */
    private String couponExplain;

    /**
     * 是否上架(0-否,1-是)
     */
    private Integer isGround;

    /**
     * 优惠券图片信息
     */
    private String couponImageUrl;

    /**
     * 剩余数量
     */
    private transient Integer surplusNumber;

    /**
     * 指定门店ids
     */
    //@ExcelProperty( "指定门店id")

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private transient List<Long> storeIds;
    /**
     * 商品IDS
     */

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> commodityIds;
    /**
     * 指定组织ids
     */

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private transient List<Long> deptIdList;

    /**
     * 用户优惠券id
     */
//    @Column(isLarge = true,ignore = true)
//    @JsonFormat(shape = JsonFormat.Shape.STRING)
//    private transient Long userCouponId;
//
//    /**
//     * 优惠券门店信息
//     */
//    @Column(isLarge = true,ignore = true)
//    private List<CouponStore> couponStores;
//
//    /**
//     * 优惠券商品信息
//     */
//    @Column(isLarge = true,ignore = true)
//    private List<CouponCommodity> couponCommodities;

    /**
     * 指定组织ids
     */
    //@TableField(exist = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String deptIds;

    /**
     * 单品名字
     */
    private transient String singleNames;

    /**
     * 1通用 2门店
     */
    private Integer isCommon;

    /**
     * 领取人限制 （0 不限制 1 新注册用户 2 老用户 3回归用户）
     */

    private Integer userRestrictions;

    /**
     * 分享设置  0 不允许转发 1 允许转发给好友 2 允许复制链接分享给好友
     */
    private Integer isShare;

    /**
     * 使用门槛类型 （0 无门槛 1 价格门槛 2 商品件数门槛）
     */
    private Integer doorsillType;

    /**
     * 门槛金额/件数
     */
    private BigDecimal doorsill;
    /**
     * 适用商品范围 1 通用 2指定商品可用 3指定商品不可用
     */
    private Integer isCommonStore;

    /**
     * 减免/折扣
     */
    private BigDecimal reliefOrDiscount;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 修改人名称
     */
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
     * 显示时间
     */
    private String showTime;

    /**
     * 版本号（乐观锁）
     */
    //@Column(version = true)
    private Long version;

    /**
     * 会员等级 12345级
     */
    private Integer memberLevel;

    /**
     * 每天限制领取数量  0 不限制
     */
    private Integer dayLimit;

    @Schema(name = "用餐方式 0 全部可用 1堂食可用 2外卖可用", description = "用餐方式 0 全部可用 1堂食可用 2外卖可用")
    private Integer habit;
    /**
     * 优惠卷背景图
     */
    private String couponBgImageUrl;
    /**
     * 指定日期逗号分割
     */
    private String dayNumbers;

    /**
     * 指定周几逗号分割
     */
    private String weekNumbers;

    /**
     * 指定时间段
     */
    private String timeRange;
    /**
     * 是否全天时段 1是 0否
     */
    private Integer isAllDay;

    /**
     * 是否过期
     */
    private Boolean outOfDate;


    private Boolean isUp;
    /**
     * 是否不够了
     */

    private Boolean isSurplus;

//    public void setCouponStatusName(String couponStatusName) {
//        if (ObjectUtil.isNotEmpty(this.couponStartTime) && ObjectUtil.isNotEmpty(this.couponEndTime)) {
//            long currTime = System.currentTimeMillis();
//            long beginTime = this.couponStartTime.getTime();
//            long endTime = this.couponEndTime.getTime();
//            if (currTime >= beginTime && currTime <= endTime) {
//                this.couponStatusName = CouponStatusEnum.PROCEEDING.name();
//            }
//            if (currTime < beginTime) {
//                this.couponStatusName = CouponStatusEnum.NOT_STARTED.name();
//            }
//
//            if (currTime > beginTime) {
//                this.couponStatusName = CouponStatusEnum.EXPIRED.name();
//            }
//        }
//    }

    @TableField(exist = false)
    private List<Integer> dayNumberList;
    @TableField(exist = false)
    private List<Integer> weekNumberList;
    @TableField(exist = false)
    private List<String> timeRangeList;

    public void setDayNumberList(List<Integer> dayNumberList) {
        this.dayNumberList = dayNumberList;
        if(ObjectUtil.isNotEmpty(dayNumberList)){
            this.dayNumbers = dayNumberList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.dayNumbers = "";
        }
    }
    public void setWeekNumberList(List<Integer> weekNumberList) {
        this.weekNumberList = weekNumberList;
        if(ObjectUtil.isNotEmpty(weekNumberList)){
            this.weekNumbers = weekNumberList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.weekNumbers = "";
        }
    }
    public void setTimeRangeList(List<String> timeRangeList) {
        this.timeRangeList = timeRangeList;
        if(ObjectUtil.isNotEmpty(timeRangeList)){
            this.timeRange = timeRangeList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.timeRange = "";
        }
    }
    public void setDayNumbers(String dayNumbers) {
        this.dayNumbers = dayNumbers;
        if(ObjectUtil.isNotEmpty(dayNumbers)){
            String[] array = dayNumbers.split(",");
            this.dayNumberList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                this.dayNumberList.add(Integer.parseInt(array[i]));
            }
        }else {
            this.dayNumberList = null;
        }
    }
    public void setWeekNumbers(String weekNumbers) {
        this.weekNumbers = weekNumbers;
        if(ObjectUtil.isNotEmpty(weekNumbers)){
            String[] array = weekNumbers.split(",");
            this.weekNumberList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                this.weekNumberList.add(Integer.parseInt(array[i]));
            }
        }else {
            this.weekNumbers = null;
        }
    }
    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
        if(ObjectUtil.isNotEmpty(timeRange)){
            String[] array = timeRange.split(",");
            this.timeRangeList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                this.timeRangeList.add(array[i]);
            }
        }else {
            this.timeRangeList = null;
        }
    }
}
