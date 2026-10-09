package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.framework.common.validation.Mobile;
import com.htyoudao.youdao.framework.excel.core.annotations.DictFormat;
import com.htyoudao.youdao.framework.excel.core.convert.DictConvert;
import com.htyoudao.youdao.module.system.controller.admin.tag.vo.TagGroupRespVO;
import com.htyoudao.youdao.module.system.enums.DictTypeConstants;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 门店信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StoreDetailResVO {
    /**
     * 门店ID
     */
    @TableId
    private Long storeId;

    /**
     * 门店名称
     */
    @Schema(description = "门店名称")
    private String storeName;

    /**
     * 门店顺序
     */
    @Schema(description = "门店顺序")
    private Integer storeSort;

    /**
     * 门店负责人姓名
     */
    @Schema(description = "门店负责人姓名")
    private String storeLeader;

    /**
     * 门店负责人电话
     */
    @Schema(description = "门店负责人电话")
    private String storeLeaderPhone;

    /**
     * 门店负责人 id
     */
    @Schema(description = "门店负责人 id")
    private Long userId;

    /**
     * 门店类型（1加盟 2 直营 3 仓库）
     */
    @Schema(description = "门店类型（1加盟 2 直营 3 仓库）")
    private Integer storeType;

    /**
     * 门店状态（0 正常营业 1 闭店）
     */
    @Schema(description = "门店状态（0 正常营业 1 闭店）")
    private Integer storeStatus;

    /**
     * 门店纬度
     */
    @Schema(description = "门店纬度")
    private Double storeLatitude;

    /**
     * 门店经度
     */
    @Schema(description = "门店经度")
    private Double storeLongitude;

    /**
     * 门店营业时间
     */
    @Schema(description = "门店营业时间")
    private String  storeHours;
    /**
     * 门店是否支持外卖（0支持，1不支持）
     */
    @Schema(description = "门店是否支持外卖（0支持，1不支持）")
    private Integer storeTakeaway;

    /**
     * 门店有效期
     */
    @Schema(description = "门店有效期")
    private String storeValidity;

    /**
     * 门店收款方式（0 二维码，1 现金，3 待定）
     */
    @Schema(description = "门店收款方式（0 二维码，1 现金，3 待定）")
    private String storePayType;

    /**
     * 门店是否支持不付款下单 （0支持 1 不支持）
     */
    @Schema(description = "门店是否支持不付款下单 （0支持 1 不支持）")
    private Integer storeWithoutPayment;

    /**
     * 校园配送开关 0支持 1不支持
     */
    @Schema(description = "校园配送开关 0支持 1不支持")
    private Integer campusDeliveryStatus;

    /**
     * 校园配送补贴
     */
    @Schema(description = "校园配送补贴")
    private BigDecimal campusDeliverySubsidy;

    /**
     * 门店公告
     */
    @Schema(description = "门店公告")
    private String storeAnnouncement;

    /**
     * 门店二维码
     */
    @Schema(description = "门店二维码")
    private String storeQrcodeImage;


    /**
     * 门店地址
     */
    @Schema(description = "门店地址")
    private String storeAddress;

    /**
     * 配送员名称
     */
    @Schema(description = "配送员名称")
    private String deliveryName;

    /**
     * 配送员电话
     */
    @Schema(description = "配送员电话")
    private String deliveryPhone;

    /**
     * 打包费
     */
    @Schema(description = "打包费")
    private BigDecimal packingCharge;

    /**
     * 门店等级
     */
    @Schema(description = "门店等级")
    private Integer attribute;

    /**
     * 门店来源 0：0090  1：其他
     */
    @Schema(description = "门店来源 0：0090  1：其他")
    private Integer storeSource;

    /**
     * 模板标识
     */
    @Schema(description = "模板标识")
    private String identificationTemplate;

    /**
     * 小程序门店状态（0 正常营业 1  闭店）
     */

    @Schema(description = "小程序门店状态（0 正常营业 1  闭店）")
    private Integer miniproStatus;

    /**
     * 所属组织id
     */
    @Schema(description = "所属组织id")
    private Long orgId;

    /**
     * 门店电话
     * 格式要求：必须是有效的中国大陆手机号码
     */
    @Schema(description = "门店电话，格式要求：必须是有效的中国大陆手机号码", example = "15601691300")
    private String storePhone;

    /**
     * 外卖时间
     */
    private String deliveryTime;
    /**
     * 点餐方式 0 全部 1 点餐机  2 小程序
     */
    @Schema(description = "点餐方式 0 全部 1 点餐机  2 小程序")
    private Integer orderType;

    /**
     * 营业状态  0 正常营业  1 休息
     */
    @Schema(description = "营业状态  0 正常营业  1 休息")
    private Integer openStatus;

    /**
     * 项目id
     */
    @Schema(description = "项目id")
    private Long businessId;
    /**
     * 省
     */
    @Schema(description = "省")
    private String storeProvince;

    /**
     * 市
     */
    @Schema(description = "市")
    private String storeCity;

    /**
     * 区
     */
    @Schema(description = "区")
    private String storeDistrict;
    /**
     * 配送范围
     */
    private List<SystemStoreDeliveryScopeVO> systemStoreDeliveryScopeList = new ArrayList<>() ;

    /**
     *  价格关系
     */
    private List<SystemStoreExpensesVO> systemStoreExpensesList =  new ArrayList<>() ;
    /**
     *  标签
     */
     private List<TagGroupRespVO> storeTagList =  new ArrayList<>() ;
    /**
     * 三方外部id
     */
    @Schema(description = "tiktokId")
    private Long tiktokId;
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
     * 是否开启密码
     */
    private Integer isOpenPassword;

    /**
     * 加盟商信息
     */
    @Schema(description = "加盟商信息")
    private StoreFranchiseeInfoRespVO franchiseeInfo;

}
