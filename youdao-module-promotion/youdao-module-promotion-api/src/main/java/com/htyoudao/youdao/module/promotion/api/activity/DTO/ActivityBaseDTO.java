package com.htyoudao.youdao.module.promotion.api.activity.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-06-07
 */
@Data
public class ActivityBaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1902145026211745857L;

    /**
     * 活动类型
     */
    private Integer activityType;

    /** 是否允许叠加其他优惠：0-否，1-是。 */
    private Integer discountStackable;

}
