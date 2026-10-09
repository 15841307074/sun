package com.htyoudao.youdao.module.promotion.controller.app.order;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.controller.admin.order.vo.SplicingOrderConfigReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.order.vo.SplicingOrderConfigRespVO;
import com.htyoudao.youdao.module.promotion.service.bzsplicing.BzSplicingOrderServcie;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "后台pc - app拼单设置")
@RestController
@RequestMapping("/promotion/splicing-order")
@Validated
public class SplicingAppOrderController {

    @Resource
    private BzSplicingOrderServcie bzSplicingOrderServcie;

    /**
     * 拼单配置查询
     *
     * @return
     */
    @Operation(summary = "拼单配置查询")
    @GetMapping("/config/info")
    public CommonResult<SplicingOrderConfigRespVO> configInfo() {
        return success(bzSplicingOrderServcie.configInfo());
    }
}
