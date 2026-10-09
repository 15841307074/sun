package com.htyoudao.youdao.module.promotion.controller.app.wechatjump;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.controller.app.wechatjump.vo.WechatJumpReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.wechatjump.vo.WechatJumpRespVO;
import com.htyoudao.youdao.module.promotion.service.jump.JumpService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dht
 */
@Tag(name = "小程序 - 跳转")
@RestController
@RequestMapping("/promotion/wechatJump")
@Validated
@Slf4j
public class WechatJumpController {

    @Resource
    private JumpService jumpService;

    @PostMapping("/jump")
    public CommonResult<WechatJumpRespVO> wechatJump(@RequestBody WechatJumpReqVO wechatJumpReqVO) {
        return CommonResult.success(jumpService.wechatJump(wechatJumpReqVO));
    }
}
