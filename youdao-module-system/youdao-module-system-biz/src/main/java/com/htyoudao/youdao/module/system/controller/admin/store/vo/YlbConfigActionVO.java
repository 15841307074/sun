package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-10-13
 */
@Data
public class YlbConfigActionVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1922154137311665024L;

    @NotNull
    private Long storeId;

    @NotNull
    private Integer reqState;

    @NotNull
    private Integer pushState;
}
