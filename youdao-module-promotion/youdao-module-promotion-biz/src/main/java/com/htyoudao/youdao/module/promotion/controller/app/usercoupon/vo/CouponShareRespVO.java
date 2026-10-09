package com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author dht
 */
@Data
public class CouponShareRespVO {

    @Schema(description = "优惠券id", example = "5133")
    private Long couponId;

    @Schema(description = "会员id", example = "1890")
    private Long memberId;

    @Schema(description = "会员手机号")
    private String memberMobile;

    @Schema(description = "项目归属ID")
    private Long projectOwnerShip;

    @Schema(description = "创建人", example = "王五")
    private String createUserName;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

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
