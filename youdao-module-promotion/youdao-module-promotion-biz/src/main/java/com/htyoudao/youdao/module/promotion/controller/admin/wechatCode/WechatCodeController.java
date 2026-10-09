package com.htyoudao.youdao.module.promotion.controller.admin.wechatCode;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.controller.app.wechatjump.vo.AppCodeReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.wechatjump.vo.WechatJumpReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.wechatjump.vo.WechatJumpRespVO;
import com.htyoudao.youdao.module.promotion.service.jump.JumpService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.WX_UNLIMITED_MINI_PROGRAM_CODE;

/**
 * @author dht
 */
@Tag(name = "小程序 - 跳转")
@RestController
@RequestMapping("/promotion/wechatJump")
@Validated
@Slf4j
public class WechatCodeController {

    @Resource
    private JumpService jumpService;

    @PostMapping("/getAppCode")
    public void getAppCode(@Valid @RequestBody AppCodeReqVO appCodeReqVO, HttpServletResponse response) throws IOException {
        byte[] imageBytes = jumpService.getUnlimitedMiniProgramCode(appCodeReqVO);

        if(imageBytes != null){
            response.setContentType("image/png"); // 小程序码为PNG格式
            response.setContentLength(imageBytes.length);
            response.getOutputStream().write(imageBytes);
            response.getOutputStream().flush();
        }else{
            throw exception(WX_UNLIMITED_MINI_PROGRAM_CODE);
        }
    }
}
