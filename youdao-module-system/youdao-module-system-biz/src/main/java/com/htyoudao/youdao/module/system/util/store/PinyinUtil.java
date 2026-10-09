package com.htyoudao.youdao.module.system.util.store;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.lang3.StringUtils;

public class PinyinUtil {

    private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();

    static {

        // 设置拼音大小写：UPPERCASE(大写) / LOWERCASE(小写)
        FORMAT.setCaseType(HanyuPinyinCaseType.UPPERCASE);

        // 设置声调格式：WITH_TONE_NUMBER(数字标调) / WITHOUT_TONE(无音调) / WITH_TONE_MARK(显示音调符号)
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        // 设置ü字符的显示格式：WITH_V(显示为v) / WITH_U_AND_COLON(显示为u:) / WITH_U_UNICODE(显示为ü)
        FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    /**
     * 获取中文拼音首字母（如果是英文则返回首字母大写）
     * 数字开头返回#，会排在字母后面
     */
    public static String getFirstLetter(String chinese) {
        if (StringUtils.isBlank(chinese)) {
            return "#";
        }

        // 遍历字符串，找到第一个有效的字符
        for (int i = 0; i < chinese.length(); i++) {
            char currentChar = chinese.charAt(i);

            // 跳过特殊符号（括号、标点等）
            if (isSpecialCharacter(currentChar)) {
                continue;
            }

            // 判断是否为中文字符
            if (Character.toString(currentChar).matches("[\\u4E00-\\u9FA5]+")) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(currentChar, FORMAT);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        return pinyinArray[0].substring(0, 1);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else if (Character.isLetter(currentChar)) {
                // 英文直接返回大写首字母
                return Character.toUpperCase(currentChar) + "";
            } else if (Character.isDigit(currentChar)) {
                // 数字开头返回 "#"，排到最后
                return "#";
            }
        }

        return "#";
    }

    /**
     * 获取完整的拼音（用于更精确排序）
     */
    public static String getFullPinyin(String chinese) {
        if (StringUtils.isBlank(chinese)) {
            return "";
        }
        StringBuilder pinyin = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        pinyin.append(pinyinArray[0]);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    pinyin.append(c);
                }
            } else {
                pinyin.append(c);
            }
        }
        return pinyin.toString();
    }

    /**
     * 判断是否为特殊字符（需要跳过的）
     */
    private static boolean isSpecialCharacter(char c) {
        // 括号、标点符号、空格等
        return c == '(' || c == ')' || c == '（' || c == '）' ||
                c == '[' || c == ']' || c == '【' || c == '】' ||
                c == '{' || c == '}' || c == '《' || c == '》' ||
                c == '，' || c == '。' || c == '、' || c == ' ' ||
                c == '-' || c == '_' || c == '·' || c == '…' ||
                c == '！' || c == '？' || c == '；' || c == '：';
    }

    /**
     * 获取门店名称的首字母
     * 数字或特殊字符开头返回#
     */
    public static String getStoreFirstLetter(String storeName) {
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(storeName)) {
            return "#";
        }

        // 获取第一个字符
        char firstChar = storeName.charAt(0);

        // 判断是否为中文字符（Unicode范围：4E00-9FA5）
        if (firstChar >= 0x4E00 && firstChar <= 0x9FA5) {
            try {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(firstChar, FORMAT);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    return pinyinArray[0].substring(0, 1);
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        }

        // 判断是否为英文字母
        if ((firstChar >= 'A' && firstChar <= 'Z') || (firstChar >= 'a' && firstChar <= 'z')) {
            return Character.toUpperCase(firstChar) + "";
        }

        // 数字或其他字符返回#
        return "#";
    }
}