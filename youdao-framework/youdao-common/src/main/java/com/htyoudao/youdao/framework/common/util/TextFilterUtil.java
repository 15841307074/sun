package com.htyoudao.youdao.framework.common.util;

import java.util.regex.Pattern;

public class TextFilterUtil {

    // 黑名单：匹配 emoji + 特殊符号
    private static final Pattern EMOJI_PATTERN = Pattern.compile(
            "[\uD83C-\uDBFF\uDC00-\uDFFF]|" + // surrogate 区域
                    "[\u2600-\u27BF]|" +              // 杂项符号
                    "[\uFE0F]|" +                     // 变体选择符
                    "[\u200D]"                        // Zero Width Joiner
    );

    // 白名单：只允许中英文、数字、标点、空格
    private static final Pattern NORMAL_CHAR_PATTERN = Pattern.compile(
            "[^\\u4E00-\\u9FA5a-zA-Z0-9\\p{Punct}\\s]"
    );

    /**
     * 黑名单过滤：移除 emoji 和大部分表情
     */
    public static String removeEmojis(String source) {
        if (source == null) {
            return null;
        }
        return EMOJI_PATTERN.matcher(source).replaceAll("");
    }

    /**
     * 白名单过滤：只保留中英文、数字、常见标点
     */
    public static String keepNormalChars(String source) {
        if (source == null) {
            return null;
        }
        return NORMAL_CHAR_PATTERN.matcher(source).replaceAll("");
    }

    /**
     * 通用过滤：根据模式决定处理逻辑
     * @param source 原始字符串
     * @param whitelistMode true = 白名单模式，false = 黑名单模式
     */
    public static String filter(String source, boolean whitelistMode) {
        if (whitelistMode) {
            return keepNormalChars(source);
        } else {
            return removeEmojis(source);
        }
    }
}

