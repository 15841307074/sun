package com.htyoudao.youdao.module.promotion.controller.admin.order;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.controller.admin.order.vo.SplicingOrderConfigReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.order.vo.SplicingOrderConfigRespVO;
import com.htyoudao.youdao.module.promotion.service.bzsplicing.BzSplicingOrderServcie;
import com.mzt.logapi.starter.annotation.LogRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.system.enums.LogRecordConstants.*;

@Tag(name = "后台pc - 拼单设置")
@RestController
@RequestMapping("/promotion/splicing-order")
@Validated
public class SplicingOrderController {

    @Resource
    private BzSplicingOrderServcie bzSplicingOrderServcie;

    /**
     * pc端拼单配置
     *
     * @return
     */
    @Schema(description = "pc端拼单配置修改")
    @PutMapping("/pc/config/saveOrUpdate")
    @PreAuthorize("@ss.hasPermission('promotion:splicing:update')")
    public CommonResult<Integer> configSaveOrUpdate(@Valid @RequestBody SplicingOrderConfigReqVO splicingOrderConfigReqVO) {
        bzSplicingOrderServcie.configSaveOrUpdate(splicingOrderConfigReqVO);
        CommonResult<Integer> success = success(null);
        success.setMsg("成功");
        return success;
    }

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
