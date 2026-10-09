package com.htyoudao.youdao.framework.datapermission.core.util;

import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.datapermission.core.aop.DataPermissionContextHolder;
import lombok.SneakyThrows;
import org.apache.dubbo.rpc.Invoker;

import java.util.concurrent.Callable;

/**
 * 数据权限 Util
 *
 * @author 0090
 */
public class DataPermissionUtils {

    private static DataPermission DATA_PERMISSION_DISABLE;

    @DataPermission(enable = false)
    @SneakyThrows
    private static DataPermission getDisableDataPermissionDisable() {
        if (DATA_PERMISSION_DISABLE == null) {
            DATA_PERMISSION_DISABLE = DataPermissionUtils.class
                    .getDeclaredMethod("getDisableDataPermissionDisable")
                    .getAnnotation(DataPermission.class);
        }
        return DATA_PERMISSION_DISABLE;
    }

    /**
     * 忽略数据权限，执行对应的逻辑
     *
     * @param runnable 逻辑
     */
    public static void executeIgnore(Runnable runnable) {
        DataPermission dataPermission = getDisableDataPermissionDisable();
        DataPermissionContextHolder.add(dataPermission);
        try {
            // 执行 runnable
            runnable.run();
        } finally {
            DataPermissionContextHolder.remove();
        }
    }

    /**
     * 忽略数据权限，执行对应的逻辑
     *
     * @param callable 逻辑
     * @return 执行结果
     */
    @SneakyThrows
    public static <T> T executeIgnore(Callable<T> callable) {
        DataPermission dataPermission = getDisableDataPermissionDisable();
        DataPermissionContextHolder.add(dataPermission);
        try {
            // 执行 callable
            return callable.call();
        } finally {
            DataPermissionContextHolder.remove();
        }
    }

    /**
     * rpc忽略数据权限，执行对应的逻辑
     *
     * @param callable 逻辑
     * @return 执行结果
     */
    @SneakyThrows
    public static <T> T executeRpcIgnore(Callable<T> callable, Invoker<?> invoker) {
        DataPermission dataPermission = getDisableDataPermissionDisable();
        DataPermissionContextHolder.add(dataPermission);
        try {
            // 执行 callable
            return callable.call();
        } finally {
            String inner = "injvm";
            if (!inner.equals(invoker.getUrl().getProtocol())) {
                DataPermissionContextHolder.remove();
            }
        }
    }

}
