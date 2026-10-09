package com.htyoudao.youdao.module.order.service.report;

import com.alibaba.excel.write.handler.WriteHandler;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.util.date.DateUtils;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.module.order.controller.app.report.VO.ReportListRespVO;
import com.htyoudao.youdao.module.order.controller.app.report.VO.ReportReqVO;
import com.htyoudao.youdao.module.order.controller.app.report.VO.ReportRespVO;
import com.htyoudao.youdao.module.order.service.report.export.HeaderMergeSheetWriteHandler;
import jakarta.annotation.Resource;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ReportExportService {

    private static final long EXPORT_PAGE_SIZE = 2000L;
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("0.##");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00");
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("¥#,##0.00");

    @Resource
    private ReportSerive reportSerive;
    @Resource
    private ExcelActionService<Object> excelActionService;

    public void exportTotal(ReportReqVO reqVO) {
        excelActionService.exportAsyncExcel(
                buildPage(),
                page -> page.getCurrent() > 1 ? Collections.emptyList() : buildTotalRows(reportSerive.totalList(copyReqVO(reqVO)).getList()),
                "发货线数",
                buildTotalHead(reqVO),
                buildMergeHandlers(
                        new CellRangeAddress(0, 1, 0, 0),
                        new CellRangeAddress(0, 1, 7, 7)
                )
        );
    }

    public void exportWarehouse(ReportReqVO reqVO) {
        excelActionService.exportAsyncExcel(
                buildPage(),
                page -> page.getCurrent() > 1 ? Collections.emptyList() : buildWarehouseRows(reportSerive.warehouseList(copyReqVO(reqVO)).getList()),
                "仓库数据",
                buildWarehouseHead(reqVO),
                buildMergeHandlers(
                        new CellRangeAddress(0, 1, 0, 0),
                        new CellRangeAddress(0, 1, 7, 7)
                )
        );
    }

    public void exportCommodityPage(ReportReqVO reqVO) {
        excelActionService.exportAsyncExcel(
                buildPage(),
                page -> buildCommodityRows(reportSerive.pcCommodityPageList(buildPageReq(reqVO, page)).getList()),
                "单品数据",
                buildCommodityHead(reqVO),
                buildMergeHandlers(
                        new CellRangeAddress(0, 1, 0, 0),
                        new CellRangeAddress(0, 1, 1, 1),
                        new CellRangeAddress(0, 1, 6, 6)
                )
        );
    }

    public void exportStorePage(ReportReqVO reqVO) {
        excelActionService.exportAsyncExcel(
                buildPage(),
                page -> buildStoreRows(reportSerive.storePage(buildPageReq(reqVO, page)).getList()),
                "门店销售数据",
                buildStoreHead(reqVO),
                buildMergeHandlers(
                        new CellRangeAddress(0, 1, 0, 0),
                        new CellRangeAddress(0, 1, 5, 5)
                )
        );
    }

    public void exportCommodityTop(ReportReqVO reqVO) {
        excelActionService.exportAsyncExcel(
                buildPage(),
                page -> page.getCurrent() > 1 ? Collections.emptyList() : buildCommodityTopRows(reportSerive.commodityTop(copyReqVO(reqVO)).getList()),
                "单品TOP10",
                singleHead("单品", "本周销售额", "上周销售额"),
                Collections.emptyList()
        );
    }

    public void exportSaleNumProportion(ReportReqVO reqVO) {
        excelActionService.exportAsyncExcel(
                buildPage(),
                page -> page.getCurrent() > 1 ? Collections.emptyList() : buildSaleNumProportionRows(reportSerive.saleNumProportion(copyReqVO(reqVO)).getList()),
                "点餐单数占比",
                buildSaleNumProportionHead(reqVO),
                buildMergeHandlers(
                        new CellRangeAddress(0, 1, 0, 0),
                        new CellRangeAddress(0, 1, 3, 3)
                )
        );
    }

    public void exportAppSalesNums(ReportReqVO reqVO) {
        excelActionService.exportAsyncExcel(
                buildPage(),
                page -> page.getCurrent() > 1 ? Collections.emptyList() : buildAppSalesNumRows(reportSerive.getAppSalesNums(copyReqVO(reqVO)).getList()),
                "小程序门店使用数",
                singleHead("日期", "使用数"),
                Collections.emptyList()
        );
    }

    public void exportStasticsPage(ReportReqVO reqVO) {
        excelActionService.exportAsyncExcel(
                buildPage(),
                page -> buildStasticsRows(reportSerive.weekStasticsPage(buildPageReq(reqVO, page)).getList()),
                "门店采购订单分析",
                buildStasticsHead(reqVO),
                buildMergeHandlers(
                        new CellRangeAddress(0, 1, 0, 0),
                        new CellRangeAddress(0, 1, 1, 1)
                )
        );
    }

    public void exportBuyProportionPage(ReportReqVO reqVO) {
        excelActionService.exportAsyncExcel(
                buildPage(),
                page -> buildBuyProportionRows(reportSerive.getBuyProportionPage(buildPageReq(reqVO, page)).getList()),
                "门店采购占比",
                singleHead("仓库", "门店", "单品", "采购占比"),
                Collections.emptyList()
        );
    }

    public void exportBuyAmountPage(ReportReqVO reqVO) {
        excelActionService.exportAsyncExcel(
                buildPage(),
                page -> buildBuyAmountRows(reportSerive.getBuyAmountPage(buildPageReq(reqVO, page)).getList()),
                "门店单品采购",
                singleHead("仓库", "门店", "单品", "金额"),
                Collections.emptyList()
        );
    }

    private Page<List<Object>> buildPage() {
        return new Page<>(1, EXPORT_PAGE_SIZE);
    }

    private ReportReqVO copyReqVO(ReportReqVO reqVO) {
        ReportReqVO target = new ReportReqVO();
        BeanUtils.copyProperties(reqVO, target);
        return target;
    }

    private ReportReqVO buildPageReq(ReportReqVO reqVO, Page<List<Object>> page) {
        ReportReqVO target = copyReqVO(reqVO);
        target.setPageNo(Long.valueOf(page.getCurrent()).intValue());
        target.setPageSize(Long.valueOf(page.getSize()).intValue());
        return target;
    }

    private List<WriteHandler> buildMergeHandlers(CellRangeAddress... regions) {
        List<CellRangeAddress> mergeRegions = new ArrayList<>();
        Collections.addAll(mergeRegions, regions);
        return List.of(new HeaderMergeSheetWriteHandler(mergeRegions));
    }

    private List<List<String>> buildTotalHead(ReportReqVO reqVO) {
        return buildThreeMetricTwoPeriodHead("配送路线", reqVO);
    }

    private List<List<String>> buildWarehouseHead(ReportReqVO reqVO) {
        return buildThreeMetricTwoPeriodHead("仓库", reqVO);
    }

    private List<List<String>> buildCommodityHead(ReportReqVO reqVO) {
        String current = periodLabel(reqVO.getStartDate(), reqVO.getEndDate());
        String compare = periodLabel(reqVO.getOtherTime().getLeft(), reqVO.getOtherTime().getRight());
        List<List<String>> head = new ArrayList<>();
        head.add(List.of("仓库", "仓库"));
        head.add(List.of("单品", "单品"));
        head.add(List.of(current, "销售额"));
        head.add(List.of(current, "占比"));
        head.add(List.of(compare, "销售额"));
        head.add(List.of(compare, "占比"));
        head.add(List.of("环比", "环比"));
        return head;
    }

    private List<List<String>> buildStoreHead(ReportReqVO reqVO) {
        String current = periodLabel(reqVO.getStartDate(), reqVO.getEndDate());
        String compare = periodLabel(reqVO.getOtherTime().getLeft(), reqVO.getOtherTime().getRight());
        List<List<String>> head = new ArrayList<>();
        head.add(List.of("门店", "门店"));
        head.add(List.of(current, "销售额"));
        head.add(List.of(current, "订单数"));
        head.add(List.of(compare, "销售额"));
        head.add(List.of(compare, "订单数"));
        head.add(List.of("环比", "环比"));
        return head;
    }

    private List<List<String>> buildSaleNumProportionHead(ReportReqVO reqVO) {
        String current = periodLabel(reqVO.getStartDate(), reqVO.getEndDate());
        String compare = periodLabel(reqVO.getOtherTime().getLeft(), reqVO.getOtherTime().getRight());
        List<List<String>> head = new ArrayList<>();
        head.add(List.of("区间", "区间"));
        head.add(List.of(current, current));
        head.add(List.of(compare, compare));
        head.add(List.of("环比", "环比"));
        return head;
    }

    private List<List<String>> buildStasticsHead(ReportReqVO reqVO) {
        String current = periodWithDailyLabel(reqVO.getStartDate(), reqVO.getEndDate());
        String compare = periodWithDailyLabel(reqVO.getOtherTime().getLeft(), reqVO.getOtherTime().getRight());
        List<List<String>> head = new ArrayList<>();
        head.add(List.of("仓库", "仓库"));
        head.add(List.of("门店", "门店"));
        head.add(List.of(current, "采购金额"));
        head.add(List.of(current, "销售订单量"));
        head.add(List.of(current, "销售额"));
        head.add(List.of(current, "客单价"));
        head.add(List.of(compare, "采购金额"));
        head.add(List.of(compare, "环比"));
        head.add(List.of(compare, "订单销售量"));
        head.add(List.of(compare, "环比"));
        head.add(List.of(compare, "销售额"));
        head.add(List.of(compare, "环比"));
        head.add(List.of(compare, "客单价"));
        return head;
    }

    private List<List<String>> buildThreeMetricTwoPeriodHead(String idTitle, ReportReqVO reqVO) {
        String current = periodLabel(reqVO.getStartDate(), reqVO.getEndDate());
        String compare = periodLabel(reqVO.getOtherTime().getLeft(), reqVO.getOtherTime().getRight());
        List<List<String>> head = new ArrayList<>();
        head.add(List.of(idTitle, idTitle));
        head.add(List.of(current, "总体销售额"));
        head.add(List.of(current, "销售门店数"));
        head.add(List.of(current, "单店平均金额"));
        head.add(List.of(compare, "总体销售额"));
        head.add(List.of(compare, "销售门店数"));
        head.add(List.of(compare, "单店平均金额"));
        head.add(List.of("环比", "环比"));
        return head;
    }

    private List<List<String>> singleHead(String... names) {
        List<List<String>> head = new ArrayList<>();
        for (String name : names) {
            head.add(List.of(name));
        }
        return head;
    }

    private List<List<Object>> buildTotalRows(List<ReportListRespVO> list) {
        List<List<Object>> rows = new ArrayList<>();
        for (ReportListRespVO item : list) {
            rows.add(List.of(
                    typeLabel(item.getType()),
                    amount(item.getBTotalAmount()),
                    number(item.getBTotalNum()),
                    amount(item.getBAvgAmount()),
                    amount(item.getSTotalAmount()),
                    number(item.getSTotalNum()),
                    amount(item.getSAvgAmount()),
                    plainPercent(item.getTotalAmountMonthOnMonth())
            ));
        }
        return rows;
    }

    private List<List<Object>> buildWarehouseRows(List<ReportListRespVO> list) {
        List<List<Object>> rows = new ArrayList<>();
        for (ReportListRespVO item : list) {
            rows.add(List.of(
                    text(item.getWarehouseName()),
                    amount(item.getBTotalAmount()),
                    number(item.getBTotalNum()),
                    amount(item.getBAvgAmount()),
                    amount(item.getSTotalAmount()),
                    number(item.getSTotalNum()),
                    amount(item.getSAvgAmount()),
                    plainPercent(item.getTotalAmountMonthOnMonth())
            ));
        }
        return rows;
    }

    private List<List<Object>> buildCommodityRows(List<ReportListRespVO> list) {
        List<List<Object>> rows = new ArrayList<>();
        for (ReportListRespVO item : list) {
            rows.add(List.of(
                    text(item.getWarehouseName()),
                    text(item.getCommodityName()),
                    amount(item.getBTotalAmount()),
                    percent(item.getBProportion()),
                    amount(item.getSTotalAmount()),
                    percent(item.getSProportion()),
                    plainPercent(item.getTotalAmountMonthOnMonth())
            ));
        }
        return rows;
    }

    private List<List<Object>> buildStoreRows(List<ReportListRespVO> list) {
        List<List<Object>> rows = new ArrayList<>();
        for (ReportListRespVO item : list) {
            rows.add(List.of(
                    text(item.getStoreName()),
                    amount(item.getBTotalAmount()),
                    number(item.getBTotalNum()),
                    amount(item.getSTotalAmount()),
                    number(item.getSTotalNum()),
                    plainPercent(item.getTotalAmountMonthOnMonth())
            ));
        }
        return rows;
    }

    private List<List<Object>> buildCommodityTopRows(List<ReportListRespVO> list) {
        List<List<Object>> rows = new ArrayList<>();
        for (ReportListRespVO item : list) {
            rows.add(List.of(
                    text(item.getCommodityName()),
                    currency(item.getBTotalAmount()),
                    currency(item.getSTotalAmount())
            ));
        }
        return rows;
    }

    private List<List<Object>> buildSaleNumProportionRows(List<ReportListRespVO> list) {
        List<List<Object>> rows = new ArrayList<>();
        if (list == null || list.size() < 2) {
            return rows;
        }
        ReportListRespVO current = list.get(0);
        ReportListRespVO compare = list.get(1);
        rows.add(List.of("50-100单", longNumber(current.getCnt50_100()), longNumber(compare.getCnt50_100()), rateWithSymbol(current.getCnt50_100(), compare.getCnt50_100())));
        rows.add(List.of("100-200单", longNumber(current.getCnt100_200()), longNumber(compare.getCnt100_200()), rateWithSymbol(current.getCnt100_200(), compare.getCnt100_200())));
        rows.add(List.of("200单以上", longNumber(current.getCnt200Plus()), longNumber(compare.getCnt200Plus()), rateWithSymbol(current.getCnt200Plus(), compare.getCnt200Plus())));
        return rows;
    }

    private List<List<Object>> buildAppSalesNumRows(List<ReportListRespVO> list) {
        List<List<Object>> rows = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            return rows;
        }
        ReportListRespVO current = list.get(0);
        rows.add(List.of(periodLabel(current.getStartDate0(), current.getEndDate0()), number(current.getBTotalNum())));
        if (list.size() > 1) {
            ReportListRespVO compare = list.get(1);
            rows.add(List.of(periodLabel(compare.getStartDate0(), compare.getEndDate0()), number(compare.getBTotalNum())));
            rows.add(List.of("环比", rateWithSymbol(current.getBTotalNum(), compare.getBTotalNum())));
        }
        return rows;
    }

    private List<List<Object>> buildStasticsRows(List<ReportListRespVO> list) {
        List<List<Object>> rows = new ArrayList<>();
        for (ReportListRespVO item : list) {
            rows.add(List.of(
                    text(item.getWarehouseName()),
                    text(item.getStoreName()),
                    amount(item.getBBuyTotalAmount()),
                    number(item.getBTotalNum()),
                    amount(item.getBTotalAmount()),
                    amount(item.getBAvgAmount()),
                    amount(item.getSBuyTotalAmount()),
                    plainPercent(item.getTotalBuyAmountMonthOnMonth()),
                    number(item.getSTotalNum()),
                    plainPercent(item.getTotalNumMonthOnMonth()),
                    amount(item.getSTotalAmount()),
                    plainPercent(item.getTotalAmountMonthOnMonth()),
                    amount(item.getSAvgAmount())
            ));
        }
        return rows;
    }

    private List<List<Object>> buildBuyProportionRows(List<ReportListRespVO> list) {
        List<List<Object>> rows = new ArrayList<>();
        for (ReportListRespVO item : list) {
            rows.add(List.of(
                    text(item.getWarehouseName()),
                    text(item.getStoreName()),
                    text(item.getCommodityName()),
                    percent(item.getBProportion())
            ));
        }
        return rows;
    }

    private List<List<Object>> buildBuyAmountRows(List<ReportListRespVO> list) {
        List<List<Object>> rows = new ArrayList<>();
        for (ReportListRespVO item : list) {
            rows.add(List.of(
                    text(item.getWarehouseName()),
                    text(item.getStoreName()),
                    text(item.getCommodityName()),
                    amount(item.getBBuyTotalAmount())
            ));
        }
        return rows;
    }

    private String text(String value) {
        return value == null ? "" : value;
    }

    private String amount(BigDecimal value) {
        return AMOUNT_FORMAT.format(zero(value));
    }

    private String currency(BigDecimal value) {
        return CURRENCY_FORMAT.format(zero(value));
    }

    private String percent(BigDecimal value) {
        return PERCENT_FORMAT.format(zero(value)) + "%";
    }

    private String plainPercent(BigDecimal value) {
        return PERCENT_FORMAT.format(zero(value));
    }

    private String number(Double value) {
        if (value == null) {
            return "0";
        }
        BigDecimal decimal = BigDecimal.valueOf(value).stripTrailingZeros();
        return decimal.scale() < 0 ? decimal.setScale(0, RoundingMode.HALF_UP).toPlainString() : decimal.toPlainString();
    }

    private String longNumber(Long value) {
        return String.valueOf(value == null ? 0L : value);
    }

    private String rateWithSymbol(Number current, Number compare) {
        BigDecimal compareValue = toBigDecimal(compare);
        if (compareValue.compareTo(BigDecimal.ZERO) == 0) {
            return "0.00%";
        }
        BigDecimal currentValue = toBigDecimal(current);
        BigDecimal rate = currentValue.subtract(compareValue).divide(compareValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        return PERCENT_FORMAT.format(rate) + "%";
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal toBigDecimal(Number number) {
        if (number == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(number));
    }

    private String typeLabel(Integer type) {
        if (type == null) {
            return "";
        }
        return switch (type) {
            case 1 -> "配送线";
            case 2 -> "发货线";
            case 3 -> "外地发货线";
            case 4 -> "220度";
            case 5 -> "总部路线";
            case 6 -> "浪大勺";
            default -> String.valueOf(type);
        };
    }

    private String periodLabel(java.time.LocalDate start, java.time.LocalDate end) {
        return DateUtils.localDateToString(start, DateUtils.YYYY_MM_DD) + "至" + DateUtils.localDateToString(end, DateUtils.YYYY_MM_DD);
    }

    private String periodWithDailyLabel(java.time.LocalDate start, java.time.LocalDate end) {
        return periodLabel(start, end) + "日均";
    }

    private String periodLabel(String start, String end) {
        return start + "至" + end;
    }
}
