package com.htyoudao.youdao.module.order.controller.app.order.VO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 拼单加入
 * </p>
 *
 * @author zhangjihe
 * @since 2024-11-17
 */
@Data
public class SplicingOrderMemberAddReqVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6116092909800456727L;

    private String mainId;

    private String openId;

    private String commodityInfo;
}
