package com.htyoudao.youdao.module.order.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 生成取餐码
 * </p>
 *
 * @author zhangjihe
 * @since 2025-03-09
 */
public class MealCodeGenerator {

    private static final List<Character> LETTERS = Arrays.asList(
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z'
    );

    public static String generatePickupCode(Long number) {
        // 每个字母最多支持 99 个编号
        int perLetterLimit = 99;

        long letterIndex = (number - 1) / perLetterLimit;
        int codeNumber = (int) ((number - 1) % perLetterLimit + 1);

        if (letterIndex >= LETTERS.size()) {
            throw new IllegalArgumentException("今日取餐码已用尽");
        }

        char letter = LETTERS.get((int) letterIndex);
        // 如 A01, B02 ...
        return String.format("%c%02d", letter, codeNumber);
    }

}