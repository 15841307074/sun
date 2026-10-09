package com.htyoudao.youdao.framework.security.core.rpc;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author lqman
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    property = "@class"
)
public abstract class LoginUserMixin {
    // 空实现
}
