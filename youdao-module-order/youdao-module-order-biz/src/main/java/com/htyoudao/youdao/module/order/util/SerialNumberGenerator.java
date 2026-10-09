package com.htyoudao.youdao.module.order.util;

import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.lang3.RandomStringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class SerialNumberGenerator {
    private static AtomicInteger counter = new AtomicInteger(0);
    private static final String PATTERN = "yyyyMMddHHmmssSSS";

    private static final String[] counterArr = new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10"};

    public static String generateSerialNumber(String code, LocalDateTime createTime) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
        String randomNumeric = RandomStringUtils.randomNumeric(5);

        Date date = DateUtils.localDateTimeToDate(createTime);

        String formattedDate = sdf.format(date);

//        int currentCounter = counter.incrementAndGet();
//        return code + formattedDate + randomNumeric + String.format("%3d", currentCounter);
        return code + formattedDate + randomNumeric;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {

            System.out.println(generateSerialNumber("ORD", LocalDateTime.now()));
        }
    }

    /**
     * 9999计数之后重新计数
     *
     * @return
     */
    public static String generateSerialNumbers(Integer code) {
        if (counter.get() == code) {
            counter = new AtomicInteger(0);
        }
        int currentCounter = counter.incrementAndGet();
        return String.format("%04d", currentCounter);
    }

    public static String generateSerialNumberFourNew() {
        //生成一个四位随机数
        Random random = new Random();
        String numebr = Integer.toString(random.nextInt(9999));

        while (numebr.length() < 4) {
            numebr += Integer.toString(random.nextInt(10));
        }
        return numebr;
    }

    public static String generateSerialNumberThree(String prevCode) {
        Random random = new Random();
        if (ObjectUtil.isEmpty(prevCode)) {
            char randomUpperCaseLetter = (char) ('A' + random.nextInt(26));
            return randomUpperCaseLetter + "01";
        } else {
            int number = Integer.parseInt(prevCode.substring(1));
            if (number < 99) {
                number++;
                return prevCode.substring(0, 1) + (number >= 10 ? number : "0" + number);
            } else {
                char randomUpperCaseLetter = (char) ('A' + random.nextInt(26));
                while (randomUpperCaseLetter == prevCode.charAt(0) || 'O' == randomUpperCaseLetter) {
                    randomUpperCaseLetter = (char) ('A' + random.nextInt(26));
                }
                return randomUpperCaseLetter + "01";
            }
        }
    }

    /**
     * 随机产生一个5位取餐码,并保存到Redis中,如果已有就从新生成
     */
    public static String generateSerialNumberFive() {
        //生成一个五位随机数
        Random random = new Random();
        return Integer.toString(random.nextInt(90000) + 10000);
    }
}
