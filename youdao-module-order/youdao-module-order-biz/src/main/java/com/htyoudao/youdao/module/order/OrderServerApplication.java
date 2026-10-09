package com.htyoudao.youdao.module.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 项目的启动类
 * <p>
 *
 * @author 0090
 */
@EnableFeignClients(basePackages = "com.htyoudao.youdao.module.order.client")
@SpringBootApplication
public class OrderServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServerApplication.class, args);
    }

}
