package com.htyoudao.youdao.module.member.api.wxmember.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 会员
 * </p>
 *
 */

@Data
public class WxMemberDataVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会员id
     */
    @Schema(description = "会员id")
    private Long memberId;


    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String memberMobile;





    @Schema(description = "会员等级")
    private Integer memberLevel;


    /**
     * 总积分数量
     */
    private Long integralFrozen;

    private Long businessId;


    /**
     * 会员昵称
     */
    private String memberNickName;







}
