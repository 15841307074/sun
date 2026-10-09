package com.htyoudao.youdao.module.member.util;




import com.htyoudao.youdao.module.member.util.redis.RedisCache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SensitiveWordFilter {

    private static List<String> sensitiveWords = new ArrayList<>();

    private final static String filePath = "https://zjhb0090-local.oss-cn-beijing.aliyuncs.com/0090/sensitive/sensitive.txt";

    // 从文件中加载敏感词列表，文件中每行一个敏感词
    public static void loadSensitiveWordsFromFile(RedisCache redisCache) {

        sensitiveWords = redisCache.getCacheList(RedisKey.SENSITIVE);
        if (sensitiveWords.isEmpty() && sensitiveWords.size() == 0) {

            try {
                URL url = new URL(filePath);
                try (InputStream is = url.openStream();
                     BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        //  System.out.println("敏感词替换前：" + line.trim());
                        String s = removeTrailingComma(line.trim());
                        sensitiveWords.add(s);
                    }
                }
                if (redisCache != null) {
                    redisCache.setCacheList(RedisKey.SENSITIVE, sensitiveWords);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // 检测文本中是否包含敏感词的方法，这里考虑了大小写情况，只要文本中出现了敏感词（忽略大小写）就返回true
    public static boolean containsSensitiveWord(RedisCache redisCache, String text) {
        loadSensitiveWordsFromFile(redisCache);
        for (String sensitiveWord : sensitiveWords) {
            if (text.toLowerCase().contains(sensitiveWord.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // 替换敏感词为指定字符（这里默认用*号）的方法，同样考虑大小写情况进行准确替换
    public static String replaceSensitiveWords(RedisCache redisCache, String text) {
        loadSensitiveWordsFromFile(redisCache);
        System.out.println("敏感词替换前55：" + sensitiveWords.size());
        for (String sensitiveWord : sensitiveWords) {
            String lowerCaseText = text.toLowerCase();
            String lowerCaseSensitiveWord = sensitiveWord.toLowerCase();
            int index = lowerCaseText.indexOf(lowerCaseSensitiveWord);
            while (index != -1) {
                StringBuilder replaceStr = new StringBuilder();
                for (int i = 0; i < sensitiveWord.length(); i++) {
                    replaceStr.append("*");
                }
                text = text.substring(0, index) + replaceStr.toString() + text.substring(index + sensitiveWord.length());
                lowerCaseText = text.toLowerCase();
                index = lowerCaseText.indexOf(lowerCaseSensitiveWord);
            }
        }
        return text;
    }

    public static String removeTrailingComma(String input) {
        // 判断字符串是否为空，如果为空则直接返回空字符串
        if (input == null) {
            return "";
        }
        // 获取字符串长度
        int length = input.length();
        // 如果长度大于0且最后一个字符是逗号，则使用substring方法截取掉逗号
        if (length > 0 && input.charAt(length - 1) == ',') {
            return input.substring(0, length - 1);
        }
        // 如果结尾不是逗号或者字符串为空字符串，则直接返回原字符串
        return input;
    }


}
