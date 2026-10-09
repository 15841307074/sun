package com.htyoudao.youdao.module.order.controller.app.sq.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static String dateTimeNow() {
        return LocalDateTime.now().format(FMT);
    }
}
