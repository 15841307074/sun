package com.htyoudao.youdao.module.promotion.controller.admin;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * details
 *
 * @author liuzhaowang
 */
@Tag(name = "测试")
@RestController
@RequestMapping("/promotion")
public class TestController {

    @GetMapping("/test")
    @Operation(summary = "测试")
    @PermitAll
    public CommonResult<String> promotionTest() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return CommonResult.success("test rest");
    }
}
