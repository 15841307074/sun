package com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.module.member.controller.app.pointsProduct.VO.ImageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class WxPointsLogDetailDTO {

    /** 记录ID */
    @Schema(description = "记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pointsLogId;


    @Schema(description = "日志编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String logCode;

    @Schema(description = "兑换记录编号")
    private String exchangeRecordNo;

    @Schema(description = "兑换状态：1兑换成功")
    private Integer exchangeStatus;

    @Schema(description = "兑换状态名称")
    private String exchangeStatusName;
    //商品详情图
    @Schema(description = "商品详情图", requiredMode = Schema.RequiredMode.REQUIRED)
    List<ImageDTO> productDetailImage;
//
    @Schema(description = "商品头部图片", requiredMode = Schema.RequiredMode.REQUIRED)
    List<ImageDTO> productHeaderImages;

    /** 商品描述 */
    @Schema(description = "商品描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productDescription;

    @Schema(description = "商品图片", requiredMode = Schema.RequiredMode.REQUIRED)
    ImageDTO productThumbnail;

    @Schema(description = "商品名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productName;

    //积分变动时间
    @Schema(description = "积分变动时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date pointLogCreateTime;

    /** 积分商品价格 */
    @Schema(description = "积分商品价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productPrice;


    @Schema(description = "商品类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private int productType;

    @Schema(description = "商品类型名称：优惠券、实物")
    private String productTypeName;

    @Schema(description = "兑换数量，积分商品每次固定兑换1份")
    private Integer productQuantity;

    @Schema(description = "本次兑换消耗积分")
    private Long consumePoints;

    /**
     * 收货地址
     */
    @Schema(description = "收货地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiveAddress;

    /** 快递单号 */
    @Schema(description = "快递单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String trackingNumber;

    /**
     * 快递公司
     */
    @Schema(description = "快递公司", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expressCompany;

//    /**
//     * 实物发货状态(1-未发货,2-已发货,3-已收货)
//     */
//    private int sendStatus;

    /**
     * 发货状态
     */
    @Schema(description = "发货状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sendStatusName;

    /**
     * 收货人
     */
    @Schema(description = "收货人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberNickName;

    /**
     * 收货人手机号
     */
    @Schema(description = "收货人手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberMobile;


}
