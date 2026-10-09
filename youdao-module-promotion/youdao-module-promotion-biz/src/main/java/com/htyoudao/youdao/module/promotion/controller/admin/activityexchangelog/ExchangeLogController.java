package com.htyoudao.youdao.module.promotion.controller.admin.activityexchangelog;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.controller.admin.activityexchangelog.vo.ExchangeLogPageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityexchangelog.vo.ExchangeLogRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.exchangelog.ActivityExchangeLogDO;
import com.htyoudao.youdao.module.promotion.service.exchangelog.ExchangeLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;


/**
 * @author dht
 */
@Tag(name = "管理后台 - 活动的兑换记录")
@RestController
@RequestMapping("/promotion/exchange-log")
@Validated
public class ExchangeLogController {

    @Resource
    private ExchangeLogService exchangeLogService;

    @GetMapping("/get")
    @Operation(summary = "获得活动的兑换记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    //@PreAuthorize("@ss.hasPermission('activity:exchange-log:query')")
    public CommonResult<ExchangeLogRespVO> getExchangeLog(@RequestParam("id") Long id) {
        ActivityExchangeLogDO exchangeLog = exchangeLogService.getExchangeLog(id);
        return success(BeanUtils.toBean(exchangeLog, ExchangeLogRespVO.class));
    }

    @PostMapping("/getMemberNum")
    @Operation(summary = "获得活动的参与人数(兑换次数就是分页的total)")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    //@PreAuthorize("@ss.hasPermission('activity:exchange-log:query')")
    public CommonResult<Integer> getMemberNum(@Valid @RequestBody ExchangeLogPageReqVO pageReqVO) {
        Integer num = exchangeLogService.getMemberNum(pageReqVO);
        return success(num);
    }

    @PostMapping("/page")
    @Operation(summary = "获得活动的兑换记录分页")
    //@PreAuthorize("@ss.hasPermission('activity:exchange-log:query')")
    public CommonResult<PageResult<ExchangeLogRespVO>> getExchangeLogPage(@Valid @RequestBody ExchangeLogPageReqVO pageReqVO) {
        PageResult<ActivityExchangeLogDO> pageResult = exchangeLogService.getExchangeLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ExchangeLogRespVO.class));
    }

    @PostMapping("/export")
    @Operation(summary = "导出")
    //@PreAuthorize("@ss.hasPermission('activity:exchange-log:query')")
    public CommonResult<Void> export(@Valid @RequestBody ExchangeLogPageReqVO pageReqVO) {
        exchangeLogService.export(pageReqVO);
        return success(null);
    }

}