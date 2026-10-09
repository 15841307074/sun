package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.htyoudao.youdao.framework.common.validation.Mobile;
import com.htyoudao.youdao.module.system.controller.admin.user.vo.user.RoleUserReqVo;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreDeliveryDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreDeliveryScopeDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreExpensesDO;
import com.htyoudao.youdao.module.system.framework.operatelog.core.SexParseFunction;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 -门店创建/修改 Request VO")
@Data
public class StoreSaveReqVO {
    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 门店名称
     */
    @Schema(description = "门店名称")
    @NotNull(message = "门店名称不能为空")
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
    @NotNull(message = "门店负责人姓名不能为空")
    private String storeLeader;

    /**
     * 门店负责人电话
     */
    @Schema(description = "门店负责人电话")
    @NotNull(message = "门店负责人电话不能为空")
    private String storeLeaderPhone;

    /**
     * 门店负责人 id
     */
    @Schema(description = "店长 id")
    @NotNull(message = "店长不能为空")
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
    @Schema(description = "门店收款方式（0 二维码，1 现金，2，收款盒子 3 pad扫码 4 待定）")
    @NotNull(message = "门店收款方式不能为空")
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
     * 配送费
     */
    @Schema(description = "配送费")
    private BigDecimal deliveryFee;

    /**
     * 门店地址
     */
    @Schema(description = "门店地址")
    @NotNull(message = "门店地址不能为空")
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
    @NotNull(message = "组织架构不能为空")
    private Long orgId;

    /**
     * 门店电话
     * 格式要求：必须是有效的中国大陆手机号码
     */
    @Schema(description = "门店电话，格式要求：必须是有效的中国大陆手机号码", example = "15601691300")
    @Mobile
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
     * 门店营业时间
     */
    @Schema(description = "门店营业时间")
    private String  storeHours;
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
    private List<SystemStoreExpensesVO>systemStoreExpensesList =  new ArrayList<>() ;
    /**
     *  标签ids
     */
    private List<Long> tagIds =  new ArrayList<>() ;
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
     * 门店是否支持外卖（0支持，1不支持）
     */
    @Schema(description = "门店是否支持外卖（0开启，1关闭）")
    private Integer isOpenPassword;

    /**
     * 高峰时段
     */
    @Schema(description = "高峰时段")
    private String peakHours;
    /**
     * 出餐时间
     */
    @Schema(description = "出餐时间")
    private String mealTime;
    /**
     * 是否弹出提示窗  0 否 1是
     */
    @Schema(description = "是否弹出提示窗  0 否 1是")
    private Integer isPrompt;
    /**
     * 弹窗提示文案
     */
    @Schema(description = "弹窗提示文案")
    private String promptText;

    /**
     * 加盟商信息；修改门店时不传表示保持原数据不变，传空对象表示清空。
     */
    @Valid
    @Schema(description = "加盟商信息")
    private StoreFranchiseeInfoSaveReqVO franchiseeInfo;
}
