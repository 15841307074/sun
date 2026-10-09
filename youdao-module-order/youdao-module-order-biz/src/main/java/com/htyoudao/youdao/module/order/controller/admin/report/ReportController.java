package com.htyoudao.youdao.module.order.controller.admin.report;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.order.service.report.ReportExportService;
import com.htyoudao.youdao.module.order.controller.app.report.VO.ReportReqVO;
import com.htyoudao.youdao.module.order.controller.app.report.VO.ReportRespVO;
import com.htyoudao.youdao.module.order.controller.app.report.VO.ReportSelectRespVO;
import com.htyoudao.youdao.module.order.service.report.ReportSerive;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "管理后台 - 报表", description = "报表")
@RestController("adminReportController")
@RequestMapping("/order/admin/report")
public class ReportController {

    @Resource
    private ReportSerive reportSerive;
    @Resource
    private ReportExportService reportExportService;

    /**
     * 发货线
     *
     * @param reqVO
     * @return
     */
    @PostMapping("/total/list")
    public CommonResult<ReportRespVO> totalList(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.totalList(reqVO));
    }

    @PostMapping("/total/export")
    public CommonResult<String> exportTotal(@Valid @RequestBody ReportReqVO reqVO) {
        reportExportService.exportTotal(reqVO);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    /**
     * 仓库数据
     *
     * @param reqVO
     * @return
     */
    @PostMapping("/warehouse/list")
    public CommonResult<ReportRespVO> warehouseList(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.warehouseList(reqVO));
    }

    @PostMapping("/warehouse/export")
    public CommonResult<String> exportWarehouse(@Valid @RequestBody ReportReqVO reqVO) {
        reportExportService.exportWarehouse(reqVO);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    @PostMapping("/commodity/list")
    public CommonResult<ReportRespVO> commodityList(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.commodityList(reqVO));
    }

    /**
     * 单品数据
     *
     * @param reqVO
     * @return
     */
    @PostMapping("/commodity/page/list")
    public CommonResult<ReportRespVO> commodityPageList(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.pcCommodityPageList(reqVO));
    }

    @PostMapping("/commodity/page/export")
    public CommonResult<String> exportCommodityPage(@Valid @RequestBody ReportReqVO reqVO) {
        reportExportService.exportCommodityPage(reqVO);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    @PostMapping("/store/list")
    public CommonResult<ReportRespVO> storeList(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.storeList(reqVO));
    }

    /**
     * 门店销售数据
     *
     * @param reqVO
     * @return
     */
    @PostMapping("/store/page/list")
    public CommonResult<ReportRespVO> storePage(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.storePage(reqVO));
    }

    @PostMapping("/store/page/export")
    public CommonResult<String> exportStorePage(@Valid @RequestBody ReportReqVO reqVO) {
        reportExportService.exportStorePage(reqVO);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    /**
     * 商品TOP10
     *
     * @param reqVO
     * @return
     */
    @PostMapping("/commodity/top")
    public CommonResult<ReportRespVO> commodityTop(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.commodityTop(reqVO));
    }

    @PostMapping("/commodity/top/export")
    public CommonResult<String> exportCommodityTop(@Valid @RequestBody ReportReqVO reqVO) {
        reportExportService.exportCommodityTop(reqVO);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    /**
     * 点餐单数占比
     *
     * @param reqVO
     * @return
     */
    @PostMapping("/store/saleNum/proportion")
    public CommonResult<ReportRespVO> saleNumProportion(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.saleNumProportion(reqVO));
    }

    @PostMapping("/store/saleNum/proportion/export")
    public CommonResult<String> exportSaleNumProportion(@Valid @RequestBody ReportReqVO reqVO) {
        reportExportService.exportSaleNumProportion(reqVO);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    /**
     * 小程序门店使用数
     *
     * @param reqVO
     * @return
     */
    @PostMapping("/store/nums")
    public CommonResult<ReportRespVO> getAppSalesNums(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.getAppSalesNums(reqVO));
    }

    @PostMapping("/store/nums/export")
    public CommonResult<String> exportAppSalesNums(@Valid @RequestBody ReportReqVO reqVO) {
        reportExportService.exportAppSalesNums(reqVO);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    @PostMapping("/stastics/list")
    public CommonResult<ReportRespVO> weekStastics(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.weekStastics(reqVO));
    }

    /**
     * 门店采购订单分析
     *
     * @param reqVO
     * @return
     */
    @PostMapping("/stastics/page/list")
    public CommonResult<ReportRespVO> weekStasticsPage(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.weekStasticsPage(reqVO));
    }

    @PostMapping("/stastics/page/export")
    public CommonResult<String> exportWeekStasticsPage(@Valid @RequestBody ReportReqVO reqVO) {
        reportExportService.exportStasticsPage(reqVO);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    @PostMapping("/buy/proportion")
    public CommonResult<List<Map<String, Object>>> getBuyProportion(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.getBuyProportion(reqVO));
    }

    /**
     * 门店采购占比
     *
     * @param reqVO
     * @return
     */
    @PostMapping("/buy/page/proportion")
    public CommonResult<ReportRespVO> getBuyProportionPage(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.getBuyProportionPage(reqVO));
    }

    @PostMapping("/buy/page/proportion/export")
    public CommonResult<String> exportBuyProportionPage(@Valid @RequestBody ReportReqVO reqVO) {
        reportExportService.exportBuyProportionPage(reqVO);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    /**
     * 门店单品采购金额
     *
     * @param reqVO
     * @return
     */
    @PostMapping("/buy/page/amount")
    public CommonResult<ReportRespVO> getBuyAmountPage(@Valid @RequestBody ReportReqVO reqVO) {
        return CommonResult.success(reportSerive.getBuyAmountPage(reqVO));
    }

    @PostMapping("/buy/page/amount/export")
    public CommonResult<String> exportBuyAmountPage(@Valid @RequestBody ReportReqVO reqVO) {
        reportExportService.exportBuyAmountPage(reqVO);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
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
        return CommonResult.success(reportSerive.getStasticsStoreList());
    }

    @GetMapping("/warehouse/select")
    public CommonResult<List<ReportSelectRespVO>> getWarehouseList() {
        return CommonResult.success(reportSerive.getWarehouseList());
    }

    @PermitAll
    @PostMapping("/temp/buy/amount/month")
    public CommonResult<String> reportBuyAmountMonth() {
        reportSerive.reportBuyAmountMonth();
        return CommonResult.success("OK");
    }
}
