package com.htyoudao.youdao.module.demo.controller.admin;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.redis.core.utils.RedissonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Collection;

/**
 * details
 *
 * @author liuzhaowang
 */
@Tag(name = "测试")
@RestController
@RequestMapping("/demo")
public class TestController {

    @GetMapping("/test")
    @Operation(summary = "测试")
    @PermitAll
    public CommonResult<String> demoTest() {
        for (int i = 0; i < 5; i++) {
            RedissonUtils.setCacheObject("demo:{demo" + i + "}health-check", "test hash tag redisson cache", Duration.ofSeconds(300));
        }
        Collection<String> keys = RedissonUtils.keys("demo:*");
        System.out.println(keys);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return CommonResult.success("test rest");
    }
}
