package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 优惠券分享子新增/修改 Request VO")
@Data
public class ShareSaveReqVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "15697")
    private Long id;

    @Schema(description = "优惠券id", requiredMode = Schema.RequiredMode.REQUIRED, example = "5133")
    @NotNull(message = "优惠券id不能为空")
    private Long couponId;

    @Schema(description = "会员id", example = "1890")
    private Long memberId;

    @Schema(description = "会员手机号")
    private String memberMobile;

    @Schema(description = "项目归属ID")
    private Long projectOwnerShip;

    @Schema(description = "创建人", example = "王五")
    private String createUserName;

    @Schema(description = "修改人", example = "王五")
    private String updateUserName;

    @Schema(description = "逻辑删除")
    private Integer isDelete;

    @Schema(description = "发放数量")
    private Integer couponNum;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享内容")
    private String shareNote;

    @Schema(description = "分享图片", example = "https://www.iocoder.cn")
    private String shareLittleImgUrl;

    @Schema(description = "小程序分享路径", example = "https://www.iocoder.cn")
    private String wxShareUrl;

    @Schema(description = "H5分享路径", example = "https://www.iocoder.cn")
    private String htmlShareUrl;

    @Schema(description = "二维码路径", example = "https://www.iocoder.cn")
    private String erCodeUrl;

    @Schema(description = "海报路径", example = "https://www.iocoder.cn")
    private String postersImgUrl;

    @Schema(description = "分享大图", example = "https://www.iocoder.cn")
    private String shareLargeImgUrl;

}