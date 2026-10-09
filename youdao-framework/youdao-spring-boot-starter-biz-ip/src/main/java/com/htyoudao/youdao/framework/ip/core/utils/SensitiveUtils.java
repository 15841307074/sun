package com.htyoudao.youdao.framework.ip.core.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SensitiveUtils {

    /**
     * 初始化 SEARCHER
     */
    @SuppressWarnings("InstantiationOfUtilityClass")
    private final static SensitiveUtils INSTANCE = new SensitiveUtils();

    /**
     * 敏感词 内存缓存，提升访问速度
     */
    private static List<String> sensitives;

    private SensitiveUtils() {

        sensitives = new ArrayList<>();
        String path = "sensitive.txt";
//        readLinesFromResource(path);
        readLinesFromResourceUtil(path);
        log.info("读取了 " + sensitives.size() + " 行数据");

    }
    public void readLinesFromResource(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sensitives.add(line);
            }
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("敏感词库文件读取失败" + resourcePath, e);
        }
    }

    public void readLinesFromResourceUtil(String path){
        try (InputStream is = ResourceUtil.getStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sensitives.add(line);
            }
        }catch (IOException | NullPointerException e) {
            throw new RuntimeException("敏感词库文件读取失败" + path, e);
        }
    }

    public static Boolean checkSensitive(String text) {
        return  sensitives.contains(text);
    }

    /**
     * 对传入文本进行敏感词替换，命中的敏感词会按原长度替换为 *。
     */
    public static String replaceSensitiveWords(String text) {
        if (text == null || text.isEmpty() || sensitives == null || sensitives.isEmpty()) {
            return text;
        }
        String result = text;
        for (String sensitive : sensitives) {
            if (sensitive == null || sensitive.isEmpty() || !result.contains(sensitive)) {
                continue;
            }
            result = result.replace(sensitive, buildMask(sensitive.length()));
        }
        return result;
    }

    /**
     * 构建指定长度的脱敏掩码。
     */
    private static String buildMask(int length) {
        if (length <= 0) {
            return "";
        }
        return "*".repeat(length);
    }

}
