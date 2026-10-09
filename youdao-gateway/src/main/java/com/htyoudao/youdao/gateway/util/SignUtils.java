package com.htyoudao.youdao.gateway.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 签名工具类
 * 使用HMAC-SHA256算法进行签名，比MD5更安全
 *
 * <p>签名规则：
 * <ol>
 *   <li>对所有参数按照参数名ASCII码从小到大排序（字典序）</li>
 *   <li>过滤掉值为null、undefined或空字符串的参数</li>
 *   <li>将参数名和参数值拼接成JSON格式，并确保嵌套对象中的字段也按字典序排序</li>
 *   <li>数字类型和布尔类型统一转换为字符串类型</li>
 *   <li>使用HMAC-SHA256算法和密钥对JSON字符串进行签名</li>
 *   <li>对签名结果进行Base64编码并转为大写</li>
 * </ol>
 *
 * <p>前端实现注意事项：
 * <ol>
 *   <li>确保对所有对象字段按字典序排序，包括嵌套对象内的字段</li>
 *   <li>过滤掉值为null、undefined或空字符串的参数</li>
 *   <li>数字类型和布尔类型统一转换为字符串类型</li>
 *   <li>使用相同的编码方式（UTF-8）</li>
 * </ol>
 *
 * <p>JavaScript参考实现：
 * <pre>
 * // 对对象进行深度排序，确保嵌套对象和数组中的对象也按照字典序排序
 * // 同时移除null、undefined和空字符串值，并将数字和布尔值转换为字符串
 * function sortObjectDeep(obj) {
 *   // 处理null、undefined或空字符串
 *   if (obj === null || obj === undefined || obj === '') {
 *     return null;
 *   }
 *
 *   // 处理字符串值 - 检查是否为 'null' 或 'undefined' 或 ''
 *   if (typeof obj === 'string') {
 *     if (obj === 'null' || obj === 'undefined' || obj === '') {
 *       return null;
 *     }
 *     return obj;
 *   }
 *
 *   // 处理数字值 - 统一转换为字符串
 *   if (typeof obj === 'number') {
 *     return String(obj);
 *   }
 *
 *   // 处理布尔值 - 统一转换为字符串
 *   if (typeof obj === 'boolean') {
 *     return String(obj);
 *   }
 *
 *   // 处理数组
 *   if (Array.isArray(obj)) {
 *     const result = [];
 *     for (const item of obj) {
 *       const sortedItem = sortObjectDeep(item);
 *       // 跳过null值
 *       if (sortedItem !== null) {
 *         result.push(sortedItem);
 *       }
 *     }
 *     return result;
 *   }
 *
 *   // 处理对象 - 按字段名排序
 *   if (typeof obj === 'object') {
 *     const keys = Object.keys(obj).sort();
 *     const sortedObj = {};
 *
 *     for (const key of keys) {
 *       const value = sortObjectDeep(obj[key]);
 *       // 跳过null值
 *       if (value !== null) {
 *         sortedObj[key] = value;
 *       }
 *     }
 *
 *     return sortedObj;
 *   }
 *
 *   // 其他类型直接返回
 *   return obj;
 * }
 *
 * // 计算签名
 * function calculateSign(params, secret) {
 *   // 1. 按字段名排序（包括嵌套对象），并过滤空值，数字和布尔值转字符串
 *   const sortedParams = sortObjectDeep(params);
 *
 *   // 2. 转换为JSON字符串
 *   const paramsStr = JSON.stringify(sortedParams);
 *
 *   // 3. 使用HMAC-SHA256签名
 *   const signValue = CryptoJS.HmacSHA256(paramsStr, secret);
 *
 *   // 4. 转为Base64并大写
 *   return CryptoJS.enc.Base64.stringify(signValue).toUpperCase();
 * }
 * </pre>
 *
 * @author lqman
 */
@Slf4j
public class SignUtils {

    /**
     * HMAC-SHA256算法名称
     */
    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * 计算签名（HMAC-SHA256）
     * 使用HMAC-SHA256算法，比MD5更安全
     *
     * @param params     参数
     * @param signSecret 签名密钥
     * @return 签名 (Base64编码)
     */
    public static String calculateSign(TreeMap<String, Object> params, String signSecret) {
        // 对参数进行深度排序，确保嵌套对象和数组中的对象也按照字典序排序
        Object sortedParams = sortObjectDeep(params);

        // 将排序后的参数转换为JSON字符串
        String paramsStr = JSON.toJSONString(sortedParams, SerializerFeature.WriteMapNullValue);

        try {
            // 创建HMAC-SHA256算法的Mac实例
            Mac mac = Mac.getInstance(HMAC_SHA256);
            // 使用密钥初始化Mac对象
            SecretKeySpec secretKeySpec = new SecretKeySpec(signSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);

            // 计算HMAC值
            byte[] hmacBytes = mac.doFinal(paramsStr.getBytes(StandardCharsets.UTF_8));

            // 对结果进行Base64编码并返回
            return Base64.getEncoder().encodeToString(hmacBytes).toUpperCase();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("计算HMAC签名失败", e);
        }
    }

    /**
     * 对对象进行深度排序，确保嵌套对象和数组中的对象也按照字典序排序
     * 同时移除null、undefined和空字符串值，并将数字和布尔值转换为字符串
     *
     * @param obj 需要排序的对象
     * @return 排序后的对象
     */
    public static Object sortObjectDeep(Object obj) {
        // 默认最大递归深度为10，足够处理大多数业务场景
        return sortObjectDeep(obj, 10, new IdentityHashMap<>());
    }

    /**
     * 对对象进行深度排序的内部实现，带有最大递归深度限制和循环引用检测
     *
     * @param obj 需要排序的对象
     * @param maxDepth 最大递归深度
     * @param visited 已访问对象的引用映射，用于检测循环引用
     * @return 排序后的对象
     */
    @SuppressWarnings("unchecked")
    private static Object sortObjectDeep(Object obj, int maxDepth, IdentityHashMap<Object, Boolean> visited) {
        // 常量定义
        final String undefinedStr = "undefined";
        final String nullStr = "null";
        final String emptyStr = "";

        // 1. 空值处理
        if (obj == null) {
            return null;
        }

        // 2. 检查递归深度
        if (maxDepth <= 0) {
            log.warn("达到最大递归深度限制，对象可能未完全排序: {}", obj.getClass().getSimpleName());
            // 对于复杂对象，达到最大深度时返回其字符串表示
            if (obj instanceof Map || obj instanceof List) {
                return "深度超限";
            }
            // 基本类型继续处理
        }

        // 3. 检查循环引用
        if (visited.containsKey(obj)) {
            log.warn("检测到循环引用: {}", obj.getClass().getSimpleName());
            return "循环引用";
        }

        // 4. 字符串特殊值处理
        if (obj instanceof String str) {
            if (undefinedStr.equals(str) || nullStr.equals(str) || emptyStr.equals(str)) {
                return null;
            }
            return str;
        }

        // 5. 数字类型转换为字符串
        if (obj instanceof Number) {
            return obj.toString();
        }

        // 6. 布尔类型转换为字符串
        if (obj instanceof Boolean) {
            return obj.toString();
        }

        // 对于复杂对象，添加到已访问集合
        if (obj instanceof Map || obj instanceof List) {
            visited.put(obj, Boolean.TRUE);
        }

        // 7. 处理Map类型
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            Map<String, Object> sortedMap = new TreeMap<>();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                // 递归处理，减少深度
                Object value = sortObjectDeep(entry.getValue(), maxDepth - 1, visited);
                if (value != null) {
                    sortedMap.put(entry.getKey(), value);
                }
            }

            // 处理完成后从已访问集合中移除，允许同级其他路径访问
            visited.remove(obj);
            return MapUtil.isEmpty(sortedMap) ? null : sortedMap;
        }

        // 8. 处理List类型
        if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            List<Object> sortedList = new ArrayList<>(list.size());

            for (Object item : list) {
                // 递归处理，减少深度
                Object sortedItem = sortObjectDeep(item, maxDepth - 1, visited);
                if (sortedItem != null) {
                    sortedList.add(sortedItem);
                }
            }

            // 处理完成后从已访问集合中移除，允许同级其他路径访问
            visited.remove(obj);
            return CollUtil.isEmpty(sortedList) ? null : sortedList;
        }

        // 9. 其他类型直接返回
        return obj;
    }
}
