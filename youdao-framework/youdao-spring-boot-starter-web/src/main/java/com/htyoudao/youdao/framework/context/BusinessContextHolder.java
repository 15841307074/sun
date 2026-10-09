package com.htyoudao.youdao.framework.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 项目上下文 Holder
 *
 * @author 0090
 */
public class BusinessContextHolder {

    /**
     * 当前租户编号
     */
    private static final ThreadLocal<Long> BUSINESS_ID = new TransmittableThreadLocal<>();


    /**
     * 获得租户编号
     *
     * @return 租户编号
     */
    public static Long getBusinessId() {
        return BUSINESS_ID.get();
    }

    public static void setBusinessId(Long businessId) {
        BUSINESS_ID.set(businessId);
    }


    /**
     * 获得租户编号。如果不存在，则抛出 NullPointerException 异常
     *
     * @return 租户编号
     */
    public static Long getRequiredBusinessId() {
        Long businessId = getBusinessId();
        if (businessId == null) {
            throw new NullPointerException("BusinessContextHolder 不存在项目编号！");
        }
        return businessId;
    }



    public static void clear() {
        BUSINESS_ID.remove();
    }

}
