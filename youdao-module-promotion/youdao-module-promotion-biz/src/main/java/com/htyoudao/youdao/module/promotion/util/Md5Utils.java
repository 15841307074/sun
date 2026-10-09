package com.htyoudao.youdao.module.promotion.util;

import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * Md5加密方法
 * @author dht
 */
public class Md5Utils
{
    private static final Logger log = LoggerFactory.getLogger(Md5Utils.class);

    private static byte[] md5(String s)
    {
        MessageDigest algorithm;
        try
        {
            algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(s.getBytes("UTF-8"));
            byte[] messageDigest = algorithm.digest();
            return messageDigest;
        }
        catch (Exception e)
        {
            log.error("MD5 Error...", e);
        }
        return null;
    }

    private static final String toHex(byte hash[])
    {
        if (hash == null)
        {
            return null;
        }
        StringBuffer buf = new StringBuffer(hash.length * 2);
        int i;

        for (i = 0; i < hash.length; i++)
        {
            if ((hash[i] & 0xff) < 0x10)
            {
                buf.append("0");
            }
            buf.append(Long.toString(hash[i] & 0xff, 16));
        }
        return buf.toString();
    }

    public static String hash(String s)
    {
        try
        {
            return new String(toHex(md5(s)).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            log.error("not supported charset...{}", e);
            return s;
        }
    }

    /**
     * 将参数按ASCII码大小升序排列进行拼接
     * @param map
     * @param
     * @return
     */
    public static String mapToMd5String(Map<String, String> map, String secret) {
        StringBuilder result = new StringBuilder();
        try {
            List<Map.Entry<String, String>> infoIds = new ArrayList<>(map.entrySet());
//            // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
//            infoIds.sort(new Comparator<Map.Entry<String, String>>() {
//                @Override
//                public int compare(Map.Entry<String, String> map1, Map.Entry<String, String> map2) {
//                    return (map1.getKey()).compareTo(map2.getKey());
//                }
//            });

            // 构造签名键值对的格式
            for (Map.Entry<String, String> item : infoIds) {
                String key = item.getKey();
                String val = item.getValue();
//                if (key == null || key.equals("")) {
//                    continue;// 跳过这些不签名
//                }
                if (!result.toString().equals("")) {
                    result.append('&'); // 第一个字符串签名不加& 其他加&连接起来参数
                }

//                if (key == null || key.equals("")) {
//                    result.append(key).append("=").append(URLDecoder.decode(val, "utf-8"));
//                }else {

                result.append(key).append("=").append(val);
//                }
            }
            if (!StringUtils.isEmpty(secret)){
//                result.append(secret);
                result.append("&" + "secret" + "=");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result + "&MAC=" + hash(result.toString());
    }

    public static String mapToString(Map<String, String> map, String secret) {
        StringBuilder result = new StringBuilder();
        try {
            List<Map.Entry<String, String>> infoIds = new ArrayList<>(map.entrySet());
            // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
            infoIds.sort(new Comparator<Map.Entry<String, String>>() {
                @Override
                public int compare(Map.Entry<String, String> map1, Map.Entry<String, String> map2) {
                    return (map1.getKey()).compareTo(map2.getKey());
                }
            });
            // 构造签名键值对的格式
            for (Map.Entry<String, String> item : infoIds) {
                String key = item.getKey();
                String val = item.getValue();

                if (!result.toString().equals("")) {
                    result.append('&'); // 第一个字符串签名不加& 其他加&连接起来参数
                }
                result.append(key).append("=").append(val);
//                }
            }
            //result.append("&" + "secret" + "=");
            if (StringUtil.isNotEmpty(secret)){
//                result.append(secret);
                result.append("&" + "secret" + "=" + secret);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
    public static String getMd5Sign(Map<String, String> map, String secret) {
        return hash(mapToString(map, secret));
    }


    public static Map<String, String> convertToMap(String query) {
        Map<String, String> params = new HashMap<>();
        StringTokenizer tokenizer = new StringTokenizer(query, "&");
        while (tokenizer.hasMoreElements()) {
            String pair = tokenizer.nextToken();
            String[] keyValue = pair.split("=");
            String key = keyValue[0];
            String value = keyValue.length > 1 ? keyValue[1] : "";
            params.put(key, value);
        }
        return params;
    }
}
