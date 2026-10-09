package com.htyoudao.youdao.module.system.dal.dataobject.store;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.framework.tenant.core.db.TenantBaseDO;
import com.htyoudao.youdao.module.system.enums.common.SexEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.apache.dubbo.common.logger.FluentLogger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

/**
 * 门店 DO
 *
 * @author 0090
 */
@TableName(value = "system_store_info", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStoreInfoDO extends BusinessBaseDO {
    /**
     * 门店ID
     */
    @TableId
    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 门店顺序
     */
    private Integer storeSort;

    /**
     * 门店负责人姓名
     */
    private String storeLeader;

    /**
     * 门店负责人电话
     */
    private String storeLeaderPhone;

    /**
     * 门店负责人 id
     */
    private Long userId;

    /**
     * 门店类型（1加盟 2 直营 3 仓库）
     */
    private Integer storeType;

    /**
     * 门店状态（0 正常营业 1 闭店 2 临时闭店）
     */
    private Integer storeStatus;

    /**
     * 门店纬度
     */
    private Double storeLatitude;

    /**
     * 门店经度
     */
    private Double storeLongitude;

    /**
     * 门店营业时间
     */
    private String  storeHours;
    /**
     * 门店是否支持外卖（0支持，1不支持）
     */
    private Integer storeTakeaway;

    /**
     * 门店有效期
     */
    private String storeValidity;

    /**
     * 门店收款方式（门店收款方式（0 二维码，1 现金，2，收款盒子 3 pad扫码 4 待定））
     */
    private String storePayType;

    /**
     * 门店是否支持不付款下单 （0支持 1 不支持）
     */
    private Integer storeWithoutPayment;

    /**
     * 校园配送开关 0支持 1不支持
     */
    private Integer campusDeliveryStatus;

    /**
     * 校园配送补贴
     */
    private BigDecimal campusDeliverySubsidy;

    /**
     * 门店公告
     */
    private String storeAnnouncement;

    /**
     * 门店二维码
     */
    private String storeQrcodeImage;

//    /**
//     * 起送费
//     */
//    private BigDecimal minimumDeliveryFee;

//    /**
//     * 配送费
//     */
//    private BigDecimal deliveryFee;

    /**
     * 门店地址
     */
    private String storeAddress;

    /**
     * 配送员名称
     */
    private String deliveryName;

    /**
     * 配送员电话
     */
    private String deliveryPhone;

//    /**
//     * 打包费
//     */
//    private BigDecimal packingCharge;

    /**
     * 门店等级
     */
    private Integer attribute;

    /**
     * 门店来源 0：0090  1：其他
     */
    private String storeSource;

    /**
     * 模板标识
     */
    private String identificationTemplate;

    /**
     * 小程序门店状态（0 正常营业 1  闭店）
     */
    private Integer miniproStatus;

    /**
     * 所属组织id
     */
    private Long orgId;

    /**
     * 门店电话
     */
    private String storePhone;

    /**
     * 外卖时间
     */
    private String deliveryTime;
    /**
     * 点餐方式 0 全部 1 点餐机  2 小程序
     */
    private Integer orderType;
    /**
     * 营业状态  0 正常营业  1 休息
     */
    private Integer openStatus;
    /**
     * 项目id
     */
    private Long businessId;

    /**
     * 省
     */
    private String storeProvince;

    /**
     * 市
     */
    @TableField(value = "store_city")
    private String storeCity;

    /**
     * 区
     */
    private String storeDistrict;
    /**
     * 小票模板
     */
    private String printerTemplate;

    /**
     * 门店名称
     */
    @TableField(exist = false)
    private String orgName;





    /**
     * 门店所属总部名称
     */
    private String storeHeadquartersName;

    /**
     * 门店所属总部 id
     */
    private Long storeHeadquartersId;

    /**
     * 门店所属大区名称
     */
    private String storeRegionName;

    /**
     * 门店所属大区 id
     */
    private Long storeRegionId;

    /**
     * 门店所属省级名称
     */
    private String storeProvinceName;

    /**
     * 门店所属省级 id
     */
    private Long storeProvinceId;

    /**
     * 门店所属市级名称
     */
    private String storeCityName;

    /**
     * 门店所属市级 id
     */
    private Long storeCityId;

    /**
     * 门店所属区级名称
     */
    private String storeAreaName;

    /**
     * 门店所属区级 id
     */
    private Long storeAreaId;

    /**
     * 门店上级负责人姓名
     */
    private String storeSuperiorName;

    /**
     * 门店上级负责人 Id
     */
    private Long storeSuperiorId;

    /**
     * 门店上级负责人电话
     */
    private String storeSuperiorPhone;

    /**
     * 起送费
     */
    private BigDecimal minimumDeliveryFee;

    /**
     * 配送费
     */
    private BigDecimal deliveryFee;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private Integer delFlag;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 是否是分店（1 是 2 否）
     */
    private Integer isBranch;

    /**
     * 关联门店 ID
     */
    private Long associationStoreId;

    /**
     * 关联门店名称
     */
    private String associationStoreName;

    /**
     * 打包费
     */
    private BigDecimal packingCharge;

    /**
     * 角色 id
     */
    private Long roleId;

    /**
     * 最终部门id
     */
    private Long deptId;

    /**
     * 是否是全部套餐 0是 1否
     */
    private Integer isAllProduct;

    /**
     * 是否是全部商品 0是 1否 2啥也不选
     */
    private Integer isAllCommdity;

    /**
     * 是否和配送路线相同 0：是 1：否
     */
    private Integer isSameLine;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 项目id
     */
    private Long projectId;

    /**
     * 项目编号
     */
    private String projectCode;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 配送线路id
     */
    private Long deliveryLineId;

    /**
     * 配送线路名称
     */
    private String deliveryLineName;

    /**
     * 起订金额
     */
    private BigDecimal startBuyAmount;

    /**
     * 预存款金额
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private BigDecimal advanceAmount;

    /**
     * 授信额度
     */
    private BigDecimal believeAmount;

    /**
     * 供应链收货地址
     */
    private String supplyAddress;

    /**
     * 供应链门店状态 1正常 2停用
     */
    private Integer useStatus;

    /**
     * 项目归属ID
     */
    private Long projectOwnerShip;

    /**
     * 门店状态（1 经营中 2 闭店）
     */
    private Integer isOpenStatus;

    /**
     * 额外营业时间
     */
    private String storeHoursExtra;

    /**
     * 外卖额外时间
     */
    private String deliveryTimeExtra;

    /**
     * 打包费设置
     */
    private String packageSetting;

    /**
     * 外卖开始时间
     */
    private LocalTime deliveryStartTime;

    /**
     * 外卖结束时间
     */
    private LocalTime deliveryEndTime;
    /**
     * 抖音id
     */
    private Long tiktokId;
//    @TableField(exist = false)
//    private Boolean isFlag = true;
    /**
     * 是否开启密码
     */
    private Integer isOpenPassword;
    /**
     * 美团id
     */
    private String meituanId;
    /**
     * 饿了么id
     */
    private String hungryId;

    /**
     * 高峰时段
     */
    private String peakHours;
    /**
     * 出餐时间
     */
    private String mealTime;

    /**
     * 是否弹出提示窗  0 否 1是
     */
    private Integer isPrompt;
    /**
     * 弹窗提示文案
     */
    private String promptText;

}
