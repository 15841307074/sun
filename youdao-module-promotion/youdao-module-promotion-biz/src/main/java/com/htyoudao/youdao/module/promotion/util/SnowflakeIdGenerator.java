package com.htyoudao.youdao.module.promotion.util;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.htyoudao.youdao.framework.common.util.HttpUtil;

import java.net.InetAddress;
import java.util.Random;


public class SnowflakeIdGenerator implements IdentifierGenerator {
    private static SnowflakeIdWorker snowflakeIdWorker = null;
    @Override
    public Long nextId(Object entity) {
        int workerId = getWorkerId();
        if (ObjectUtil.isEmpty(snowflakeIdWorker)) {
            snowflakeIdWorker = new SnowflakeIdWorker(Long.valueOf(workerId), getDatacenterId());
        }
        return snowflakeIdWorker.nextId();
    }

    private static int getWorkerId() {
        return subPort(HttpUtil.getPort());
    }

    private static int getDatacenterId() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String ipAddress = localHost.getHostAddress();
            String[] arr = ipAddress.split("\\.");
            int total = 0;
            for (int i = 0; i < arr.length; i++) {
                total = total + Integer.parseInt(arr[i]);
            }
            return total%31;
        }catch (Exception e) {
            Random r = new Random();
            return r.nextInt(31);
        }

    }

    /**
     * 当前服务的端口号 取3位求和
     *
     * @param port
     * @return
     */
    private static int sumOfDigits(int port) {
        StringBuilder portStr = new StringBuilder(String.valueOf(port));
        while (portStr.length() < 3) {
            portStr.append("0");
        }
        int subPort = Integer.parseInt(portStr.substring(portStr.length() - 3, portStr.length()));
        int workerId = 0;
        while (subPort > 0) {
            // 取最后一位数字并加到和上
            workerId += subPort % 10;
            // 移除最后一位数字
            subPort /= 10;
        }
        return workerId;
    }

    /**
     * 当前服务的端口号 取3位
     *
     * @param port
     * @return
     */
    private static int subPort(int port) {
        StringBuilder portStr = new StringBuilder(String.valueOf(port));
//        while (portStr.length() < 3) {
//            portStr.insert(0, "0");
//        }
        return Integer.parseInt(portStr.substring(portStr.length() - 1, portStr.length()));
    }

    /**
     * 生成一个 16 位雪花 ID
     *
     * @return
     */
    public static long generateId() {
        if (ObjectUtil.isEmpty(snowflakeIdWorker)) {
            int workerId = getWorkerId();
            snowflakeIdWorker = new SnowflakeIdWorker(Long.valueOf(workerId),  getDatacenterId());
        }
        return snowflakeIdWorker.nextId();
    }
}

