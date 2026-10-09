package com.htyoudao.youdao.module.order.util;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class HashedWheelTimerProxy {

    private HashedWheelTimer wheelTimer = new HashedWheelTimer();

    private Map<String, Timeout> timeTaskMap = new HashMap();
}
