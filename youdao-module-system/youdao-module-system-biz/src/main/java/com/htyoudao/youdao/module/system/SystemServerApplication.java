package com.htyoudao.youdao.module.system;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目的启动类
 *
 *
 * @author 0090
 */
@SpringBootApplication
public class SystemServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemServerApplication.class, args);
    }

}
