package com.htyoudao.youdao.module.promotion.controller.admin.market;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.market.vo.*;
import com.htyoudao.youdao.module.promotion.service.market.SmsMarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dht
 */
@Tag(name = "管理后台 - 短信营销 ")
@RestController
@RequestMapping("/promotion/sms-market")
@Validated
@Slf4j
public class SmsMarketController {

    @Resource
    private SmsMarketService smsMarketService;

    @GetMapping("/page")
    @Operation(summary = "分页")
    @PreAuthorize("@ss.hasPermission('sms:market:page')")
    public CommonResult<PageResult<SmsMarketRespVO>> list(SmsMarketReqVO smsMarket) {
        return CommonResult.success(smsMarketService.selectSmsMarketListPage(smsMarket));
    }

    @GetMapping("/getById")
    @Operation(summary = "详情")
    //@PreAuthorize("@ss.hasPermission('good:sms-market:info')")
    public CommonResult<SmsMarketRespVO> list(@RequestParam(value = "id", required = false) Long id) {
        return CommonResult.success(smsMarketService.selectById(id));
    }

    @GetMapping("/getTemplateList")
    @Operation(summary = "获取模版列表")
    public CommonResult<List<SmsTemplateRespVO>> getTemplateList() {
        return CommonResult.success(smsMarketService.getTemplateList());
    }

    @PostMapping("/insert")
    @Operation(summary = "新增")
    @PreAuthorize("@ss.hasPermission('good:sms-market:insert')")
    public CommonResult<Boolean> insert(@RequestBody SmsMarketSaveReqVO smsMarket) {
        return CommonResult.success(smsMarketService.insert(smsMarket));
    }

    @PutMapping("/update")
    @Operation(summary = "修改")
    @PreAuthorize("@ss.hasPermission('good:sms-market:insert')")
    public CommonResult<Boolean> update(@RequestBody SmsMarketSaveReqVO smsMarket) {
        return CommonResult.success(smsMarketService.edit(smsMarket));
    }

    @PostMapping("/copy")
    @Operation(summary = "复制")
    @PreAuthorize("@ss.hasPermission('good:sms-market:insert')")
    public CommonResult<Boolean> copy(@RequestParam(value = "id", required = false) Long id) {
        return CommonResult.success(smsMarketService.copy(id));
    }

    @PostMapping("/sendMessage")
    @Operation(summary = "发送短信")
    @PreAuthorize("@ss.hasPermission('sms:market:sendMessage')")
    public CommonResult<Boolean> sendMessage(@Validated @RequestBody SmsSendMessageReqVO sendMessage) {
        return CommonResult.success(smsMarketService.sendMessage(sendMessage));
    }

    @PostMapping("/scheduledSend")
    @Operation(summary = "定时发送 --  后端用")
    public CommonResult<Boolean> scheduledSend() {
        return CommonResult.success(smsMarketService.scheduledSend());
    }

    @PostMapping("/preSend")
    @Operation(summary = "预览发送")
    @PreAuthorize("@ss.hasPermission('sms:market:sendMessage')")
    public CommonResult<Boolean> preSend(@RequestBody PreMessageReqVO preMessage) {
        return CommonResult.success(smsMarketService.preSend(preMessage));
    }

    @PostMapping("/getPv")
    @Operation(summary = "获取Pv")
    public CommonResult<SmsMarketPageViewVO> getPv(@RequestParam(value = "id", required = false) Long id) {
        return CommonResult.success(smsMarketService.getPv(id));
    }

    @PostMapping("/insertPv")
    @Operation(summary = "进入页面,增加pv")
    public CommonResult<Boolean> insertPv(@RequestBody AddPvReqVO reqVO) {
        return CommonResult.success(smsMarketService.insertPv(reqVO));
    }


    @PostMapping("/claimCouponPackage")
    @Operation(summary = "领取优惠券包")
    public CommonResult<Boolean> claimCouponPackage(@RequestBody ClaimCouponPackageReqVO reqVO){
        return CommonResult.success(smsMarketService.claimCouponPackage(reqVO));
    }

    @PostMapping("/testSend")
    @Operation(summary = "测试发送短信")
    public CommonResult<Boolean> testSend(@RequestBody PreMessageReqVO preMessage) {
        return CommonResult.success(smsMarketService.testSend(preMessage));
    }
}
