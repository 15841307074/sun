package com.htyoudao.youdao.framework.common.util;

import java.util.regex.Pattern;

public class EmojiUtils {
    // 优化后的 Emoji 匹配正则：覆盖更全面的 Emoji 范围（含新增表情、特殊符号）
    private static final String EMOJI_REGEX =
            "[\\p{So}" +                  // 其他符号（涵盖大部分基础 Emoji）
                    "\\p{Cn}" +                  // 未分配字符（部分特殊 Emoji 可能归类于此）
                    "\\p{InSupplementaryPrivateUseArea-A}" +  // 补充私有区域 A（特殊 Emoji 区域）
                    "\\p{InSupplementaryPrivateUseArea-B}" +  // 补充私有区域 B（特殊 Emoji 区域）
                    "\\uD83C[\\uDF00-\\uDFFF]" + // 杂项符号与符号字体（如🌍、🎉）
                    "\\uD83D[\\uDC00-\\uDE4F]" + // 表情符号（如😂、👍）
                    "\\uD83D[\\uDE80-\\uDEFF]" + // 交通与地图符号（如🚗、🗺️）
                    "\\uD83E[\\uDD00-\\uDDFF]" + // 装饰符号（如🦄、🥳）
                    "\\uD83E[\\uDE00-\\uDEFF]" + // 情感符号（如🥰、🤯）
                    "]";

    // 预编译正则（避免每次调用时重复编译，提升性能）
    private static final Pattern EMOJI_PATTERN = Pattern.compile(EMOJI_REGEX);

    /**
     * 剔除字符串中的所有 Emoji 字符
     * @param input 原始字符串（可能含 Emoji）
     * @return 剔除 Emoji 后的纯文本字符串（若 input 为 null，返回空字符串）
     */
    public static String removeEmoji(String input) {
        // 处理 null 情况，避免空指针异常
        if (input == null || input.isEmpty()) {
            return "";
        }
        // 将匹配到的 Emoji 替换为空字符串
        return EMOJI_PATTERN.matcher(input).replaceAll("");
    }

    // 测试示例
    public static void main(String[] args) {
        String testStr1 = "Hello 😂 世界！🎉 今天天气真好🌞";
        String testStr2 = "纯文本测试（无 Emoji）";
        String testStr3 = null;

        System.out.println(removeEmoji(testStr1)); // 输出：Hello  世界！ 今天天气真好
        System.out.println(removeEmoji(testStr2)); // 输出：纯文本测试（无 Emoji）
        System.out.println(removeEmoji(testStr3)); // 输出：（空字符串）
    }
}
