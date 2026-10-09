package com.htyoudao.youdao.module.order.controller.app.report;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.order.controller.app.report.VO.ReportReqVO;
import com.htyoudao.youdao.module.order.controller.app.report.VO.ReportRespVO;
import com.htyoudao.youdao.module.order.controller.app.report.VO.ReportSelectRespVO;
import com.htyoudao.youdao.module.order.service.report.ReportSerive;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "报表", description = "报表")
@RestController("appReportController")
@RequestMapping("/order/report")
public class ReportController {

    @Resource
    private ReportSerive reportSerive;

    @PostMapping("/total/list")
    public CommonResult<ReportRespVO> totalList(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.totalList(reqVO));
    }

    @PostMapping("/warehouse/list")
    public CommonResult<ReportRespVO> warehouseList(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.warehouseList(reqVO));
    }

    @PostMapping("/commodity/list")
    public CommonResult<ReportRespVO> commodityList(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.commodityList(reqVO));
    }

    @PostMapping("/commodity/page/list")
    public CommonResult<ReportRespVO> commodityPageList(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.commodityPageList(reqVO));
    }

    @PostMapping("/store/list")
    public CommonResult<ReportRespVO> storeList(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.storeList(reqVO));
    }

    @PostMapping("/store/page/list")
    public CommonResult<ReportRespVO> storePage(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.storePage(reqVO));
    }

    @PostMapping("/commodity/top")
    public CommonResult<ReportRespVO> commodityTop(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.commodityTop(reqVO));
    }

    @PostMapping("/store/saleNum/proportion")
    public CommonResult<ReportRespVO> saleNumProportion(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.saleNumProportion(reqVO));
    }

    @PostMapping("/store/nums")
    public CommonResult<ReportRespVO> getAppSalesNums(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.getAppSalesNums(reqVO));
    }

    @PostMapping("/stastics/list")
    public CommonResult<ReportRespVO> weekStastics(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.weekStastics(reqVO));
    }

    @PostMapping("/stastics/page/list")
    public CommonResult<ReportRespVO> weekStasticsPage(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.weekStasticsPage(reqVO));
    }

    @PostMapping("/buy/proportion")
    public CommonResult<List<Map<String, Object>>> getBuyProportion(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.getBuyProportion(reqVO));
    }

    @PostMapping("/buy/page/proportion")
    public CommonResult<ReportRespVO> getBuyProportionPage(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.getBuyProportionPage(reqVO));
    }

    @GetMapping("/getLastYearWeeks")
    public CommonResult<List<Map<String, String>>> getLastYearWeeks() {
        return CommonResult.success(reportSerive.getLastYearWeeks());
    }

    @GetMapping("/getLastYearMonths")
    public CommonResult<List<Map<String, String>>> getLastYearMonths() {
        return CommonResult.success(reportSerive.getLastYearMonths());
    }

    @GetMapping("/store/select")
    public CommonResult<List<ReportSelectRespVO>> getStoreList() {
        return CommonResult.success(reportSerive.getStoreList());
    }

    @GetMapping("/warehouse/select")
    public CommonResult<List<ReportSelectRespVO>> getWarehouseList() {
        return CommonResult.success(reportSerive.getWarehouseList());
    }
}
