package com.htyoudao.youdao.framework.excel.core.context;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-27
 */
@Builder
@Data
public class RequestContext implements Serializable {
    @Serial
    private static final long serialVersionUID = 7620368468058477594L;
    private String creator;
    private Long businessId;
}
