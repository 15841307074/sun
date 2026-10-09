package com.htyoudao.youdao.framework.excel.core.context;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-27
 */
public class RequestContextHolder {

    private static final InheritableThreadLocal<RequestContext> contextHolder = new InheritableThreadLocal<>();

    public static void setContext(RequestContext context) {
        contextHolder.set(context);
    }

    public static RequestContext getContext() {
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove();
    }
}
