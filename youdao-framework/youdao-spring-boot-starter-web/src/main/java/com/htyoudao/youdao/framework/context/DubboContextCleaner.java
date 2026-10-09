package com.htyoudao.youdao.framework.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.extern.slf4j.Slf4j;

/**
 * Dubbo Provider Filter调用深度计数，支持异步、线程池、全局兜底清理
 * @author lqman
 */
@Slf4j
@SuppressWarnings("unused")
public class DubboContextCleaner {
    private static final ThreadLocal<Integer> DEPTH = new TransmittableThreadLocal<>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    private static volatile Runnable clearAction = BusinessContextHolder::clear;

    /**
     * 支持外部注入自定义清理逻辑
     */
    public static void setClearAction(Runnable action) {
        clearAction = action != null ? action : BusinessContextHolder::clear;
    }

    public static void enter() {
        int depth = DEPTH.get() + 1;
        DEPTH.set(depth);
        log.debug("DubboContextCleaner enter, depth={}", depth);
    }

    public static void exit() {
        int depth = DEPTH.get() - 1;
        if (depth <= 0) {
            clearAction.run();
            DEPTH.remove();
            log.debug("DubboContextCleaner clear context, depth={}", depth);
        } else {
            DEPTH.set(depth);
            log.debug("DubboContextCleaner exit, depth={}", depth);
        }
    }

    /**
     * 全局兜底清理（可在JVM关闭钩子、全局异常处理等场景调用）
     */
    public static void clearAll() {
        clearAction.run();
        DEPTH.remove();
        log.warn("DubboContextCleaner 全局兜底清理上下文");
    }
}
