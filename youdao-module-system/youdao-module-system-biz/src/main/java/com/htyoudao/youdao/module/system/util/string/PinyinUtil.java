package com.htyoudao.youdao.module.system.util.string;


import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinUtil {

    public static String toPinyin(String chinese) {
        chinese = chinese.replaceAll("\\d", "");
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        StringBuilder sb = new StringBuilder();
        char[] chars = chinese.toCharArray();
        for (char c : chars) {
            String[] pinyinArray = null;
            try {
                pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
            if (pinyinArray != null) {
               sb.append(pinyinArray[0]);
            } else {
                sb.append(c);
            }
        }

        switch (sb.toString()){
            case "zhongqingshi":
                sb = new StringBuilder("chongqingshi");
                break;
            case "zhangchunshi":
                sb = new StringBuilder("changchunshi");
                break;
            case "zhangzhishi":
                sb = new StringBuilder("changzhishi");
                break;
            case "zhongqing":
                sb = new StringBuilder("chongqing");
                break;
            case "zhangshashi":
                sb = new StringBuilder("changshashi");
                break;
            case "zhangzhoushi":
                sb = new StringBuilder("changzhoushi");
                break;
            case "shenfangshi":
                sb = new StringBuilder("shifangshi");
                break;
            case "yushanshi":
                sb = new StringBuilder("leshanshi");
                break;

            case "junxianshi":
                sb = new StringBuilder("xunxianshi");
                break;
            case "shamenshi":
                sb = new StringBuilder("xiamenshi");
                break;
            case "weilishi":
                sb = new StringBuilder("yulishi");
                break;
            case "zhaoyangshi":
                sb = new StringBuilder("chaoyangshi");
                break;
            case "danxianshi":
                sb = new StringBuilder("shanxianshi");
                break;
            default:
                break;
        }


        return sb.toString();
    }

    public static String getFirstLetter(String chineseText) {
        StringBuilder firstLetters = new StringBuilder();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        for (char c : chineseText.toCharArray()) {
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]")) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        firstLetters.append(pinyinArray[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                firstLetters.append(c);
            }
        }

        return firstLetters.toString();
    }

    public static String getFirstTwoLowerCaseLetters(String chineseText) {
        StringBuilder firstTwoLowerCaseLetters = new StringBuilder();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);


        for (char c : chineseText.toCharArray()) {
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]")) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray!= null && pinyinArray.length > 0) {
                        char firstLetter = pinyinArray[0].charAt(0);
                        firstTwoLowerCaseLetters.append(Character.toLowerCase(firstLetter));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                firstTwoLowerCaseLetters.append(Character.toLowerCase(c));
            }
        }

        return firstTwoLowerCaseLetters.toString();
    }

    public static String getFirstLettersWithoutDigits(String input) {
        StringBuilder result = new StringBuilder();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        char[] charArray = input.toCharArray();
        for (char c : charArray) {
            if (!Character.isDigit(c)) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray!= null && pinyinArray.length > 0) {
                        result.append(pinyinArray[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            }
        }
        return result.toString();
    }
}
