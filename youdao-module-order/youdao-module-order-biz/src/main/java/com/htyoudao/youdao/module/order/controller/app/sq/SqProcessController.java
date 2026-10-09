package com.htyoudao.youdao.module.order.controller.app.sq;

import com.htyoudao.youdao.module.order.controller.app.sq.VO.ProcessCodesReq;
import com.htyoudao.youdao.module.order.controller.app.sq.VO.ProcessCodesResp;
import com.htyoudao.youdao.module.order.controller.app.sq.VO.ProcessCodesWithAppidReq;
import com.htyoudao.youdao.module.order.controller.app.sq.service.SqProcessService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order/sq")
@RequiredArgsConstructor
public class SqProcessController {

    private final SqProcessService sqProcessService;

    @PermitAll
    @PostMapping("/process")
    public ProcessCodesResp process(@RequestBody @Valid ProcessCodesReq req) {
        return sqProcessService.processCodes(req.getCodes());
    }

    @PermitAll
    @GetMapping("/process")
    public ProcessCodesResp processGet(@RequestParam("code") @NotBlank(message = "code 不能为空") String code) {
        return sqProcessService.processCodes(List.of(code));
    }

    @PermitAll
    @PostMapping("/process-appid-config")
    public ProcessCodesResp processAppidConfig(@RequestBody @Valid ProcessCodesWithAppidReq req) {
        return sqProcessService.processCodesWithAppid(req.getCodes(), req.getAppid());
    }

    @PermitAll
    @GetMapping("/process-appid-config")
    public ProcessCodesResp processAppidConfigGet(
            @RequestParam("code") @NotBlank(message = "code 不能为空") String code,
            @RequestParam(value = "appid", required = false) String appid) {
        if (appid == null || appid.isBlank()) {
            appid = "wx3b84773f5f12d87f";
        }
        return sqProcessService.processCodesWithAppid(List.of(code), appid);
    }
}
