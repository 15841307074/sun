package com.htyoudao.youdao.module.errand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 项目的启动类
 * <p>
 *
 * @author 0090
 */
@SpringBootApplication
@EnableScheduling
public class ErrandServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErrandServerApplication.class, args);
    }

}
