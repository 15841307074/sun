package com.htyoudao.youdao.module.order.controller.app.sq.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LogUtil {

    public static Map<String, String> maskSensitiveParams(Map<String, String> params) {
        Map<String, String> copy = new HashMap<>(params);
        mask(copy, "sign");
        mask(copy, "secret");
        mask(copy, "publicKey");
        return copy;
    }

    public static Object maskSensitiveInfo(Object obj) {
        if (obj instanceof JSONObject json) {
            JSONObject cp = new JSONObject(json);
            if (cp.containsKey("sign")) cp.put("sign", "***");
            if (cp.containsKey("publicKey")) cp.put("publicKey", "***");
            return cp;
        }
        return obj;
    }

    private static void mask(Map<String, String> map, String key) {
        if (map.containsKey(key) && map.get(key) != null) {
            map.put(key, "***");
        }
    }
}
