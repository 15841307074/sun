package com.htyoudao.youdao.module.system.controller.app.store.vo.DC;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.SystemStoreDeliveryScopeVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.SystemStoreExpensesVO;
import com.htyoudao.youdao.module.system.controller.admin.tag.vo.TagGroupRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StoreInfoDCRespVo {

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

    @Schema(description = "门店顺序")
    private Integer storeSort;

    /**
     * 门店状态（0 正常营业 1 闭店）
     */
    @Schema(description = "门店状态（0 正常营业 1 闭店）")
    private Integer storeStatus;

    /**
     * 门店负责人姓名
     */
    @Schema(description = "门店负责人姓名")
    private String storeLeader;

    @Schema(description = "门店负责人电话")
    private String storeLeaderPhone;

    /**
     * 门店负责人 id
     */
    @Schema(description = "门店负责人 id")
    private Long userId;

    /**
     * 门店电话
     * 格式要求：必须是有效的中国大陆手机号码
     */
    @Schema(description = "门店电话，格式要求：必须是有效的中国大陆手机号码", example = "15601691300")
    private String storePhone;

    /**
     * 营业状态  0 正常营业  1 休息
     */
    @Schema(description = "营业状态  0 正常营业  1 休息")
    private Integer openStatus;


    /**
     * 门店营业时间
     */
    @Schema(description = "门店营业时间")
    private String  storeHours;

    /**
     * 门店公告
     */
    @Schema(description = "门店公告")
    private String storeAnnouncement;

    /**
     * 门店地址
     */
    @Schema(description = "门店地址")
    private String storeAddress;

    /**
     * 门店收款方式（0 二维码，1 现金，3 待定）
     */
    @Schema(description = "门店收款方式（0 二维码，1 现金，3 待定）")
    private String storePayType;

    /**
     * 门店是否支持外卖（0支持，1不支持）
     */
    @Schema(description = "门店是否支持外卖（0支持，1不支持）")
    private Integer storeTakeaway;

    /**
     * 校园配送开关：0支持 1不支持
     */
    @Schema(description = "校园配送开关：0支持 1不支持")
    private Integer campusDeliveryStatus;

    /**
     * 校园配送补贴
     */
    @Schema(description = "校园配送补贴")
    private BigDecimal campusDeliverySubsidy;

    /**
     * 代取起送费
     */
    @Schema(description = "代取起送费")
    private BigDecimal campusMinimumDeliveryFee;

    /**
     * 校园配送费不再配置，固定返回0
     */
    @Schema(description = "校园配送费不再配置，固定返回0")
    private BigDecimal campusDeliveryFee;

    /**
     * 校园配送费计算方式不再配置，固定返回null
     */
    @Schema(description = "校园配送费计算方式不再配置，固定返回null")
    private Integer campusDeliveryCalculationType;

    /**
     * 门店二维码
     */
    @Schema(description = "门店二维码")
    private String storeQrcodeImage;

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
     * 外卖时间
     */
    @Schema(description = "外卖时间")
    private String deliveryTime;

    /**
     *  价格关系
     */
    @Schema(description = "价格关系")
    private List<SystemStoreExpensesVO> systemStoreExpensesList =  new ArrayList<>() ;

    @Schema(description = "组织Id")
    private Long orgId;

    /**
     * 配送范围
     */
    private List<SystemStoreDeliveryScopeVO> systemStoreDeliveryScopeList = new ArrayList<>() ;

    /**
     *  标签
     */
    private List<TagGroupRespVO> storeTagList =  new ArrayList<>() ;
    /**
     * 是否开启密码
     */
    private Integer isOpenPassword;
    /**
     * 点餐方式 0 全部 1 点餐机
     */
    @Schema(description = "点餐方式 0 全部 1 点餐机 ")
    private Integer orderType;
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
     * 是否是全部套餐 0是 1不是 供应链用
     */
    @Schema(description = "是否是全部套餐 0是 1不是")
    private Integer isAllProduct;

    /**
     * 是否是全部套餐 0是 1不是  供应链用
     */
    @Schema(description = "是否是全部商品 0是 1不是")
    private Integer isAllCommdity;
    @Schema(description = "门店背景图片")
    private String storeBackgroundImage;
}
