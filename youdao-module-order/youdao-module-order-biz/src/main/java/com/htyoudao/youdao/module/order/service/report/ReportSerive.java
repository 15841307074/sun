package com.htyoudao.youdao.module.order.service.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.date.DateUtils;
import com.htyoudao.youdao.module.order.controller.app.report.VO.ReportListRespVO;
import com.htyoudao.youdao.module.order.controller.app.report.VO.ReportReqVO;
import com.htyoudao.youdao.module.order.controller.app.report.VO.ReportRespVO;
import com.htyoudao.youdao.module.order.controller.app.report.VO.ReportSelectRespVO;
import com.htyoudao.youdao.module.order.dal.DTO.OrderReportDTO;
import com.htyoudao.youdao.module.order.dal.DTO.OrderReportWithRateDTO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.*;
import com.htyoudao.youdao.module.order.dal.mysql.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RefreshScope
@Slf4j
@Service
public class ReportSerive {

    @Resource
    private ReportOrderMapper reportOrderMapper;
    @Resource
    private ReportTotalMapper reportTotalMapper;
    @Resource
    private ReportWarehouseMapper reportWarehouseMapper;
    @Resource
    private ReportCommodityMapper reportCommodityMapper;
    @Resource
    private ReportStoreSalesMapper reportStoreSalesMapper;
    @Resource
    private ReportStasticsMapper reportStasticsMapper;
    @Resource
    private ReportAppSalesnumMapper reportAppSalesnumMapper;
    @Resource
    private ReportBuyProportionMapper reportBuyProportionMapper;
    @Resource
    private ReportBuyAmountMapper reportBuyAmountMapper;

    public ReportRespVO totalList(ReportReqVO reqVO) {
        ReportRespVO returnObj = buildBaseReportResp(reqVO);
        List<ReportListRespVO> returnList = new ArrayList<>();

        LambdaQueryWrapper<ReportTotalDO> queryWrapper0 = new LambdaQueryWrapper<>();
        queryWrapper0.eq(ReportTotalDO::getStartDate, reqVO.getStartDate());
        queryWrapper0.eq(ReportTotalDO::getEndDate, reqVO.getEndDate());
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr())) {
            queryWrapper0.eq(ReportTotalDO::getType, Integer.valueOf(reqVO.getSearchStr()));
        }
        queryWrapper0.eq(ReportTotalDO::getNumberType, reqVO.getNumberType());
        queryWrapper0.orderBy(true, true, ReportTotalDO::getType);
        queryWrapper0.last("ORDER BY FIELD(type, " + Arrays.asList(1, 2, 3, 4, 5, 6).stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ")) + ")");
        List<ReportTotalDO> reportTotalDOS0 = reportTotalMapper.selectList(queryWrapper0);

        Pair<LocalDate, LocalDate> otherTime = reqVO.getOtherTime();
        LambdaQueryWrapper<ReportTotalDO> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(ReportTotalDO::getStartDate, otherTime.getLeft());
        queryWrapper1.eq(ReportTotalDO::getEndDate, otherTime.getRight());
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr())) {
            queryWrapper1.eq(ReportTotalDO::getType, Integer.valueOf(reqVO.getSearchStr()));
        }
        queryWrapper1.eq(ReportTotalDO::getNumberType, reqVO.getNumberType());
        List<ReportTotalDO> reportTotalDOS1 = reportTotalMapper.selectList(queryWrapper1);

        Map<Integer, ReportTotalDO> collect1 = reportTotalDOS1.stream()
                .collect(Collectors.toMap(ReportTotalDO::getType, r -> r, (old, newR) -> newR));
        reportTotalDOS0.forEach(item -> {
            ReportListRespVO reportListRespVO = new ReportListRespVO();
            reportListRespVO.setType(item.getType());
            reportListRespVO.setBTotalAmount(item.getTotalAmount());
            reportListRespVO.setBTotalNum(roundWhole(item.getTotalNum()));
            reportListRespVO.setBAvgAmount(reportListRespVO.getBTotalNum() == 0 ? BigDecimal.ZERO :
                    reportListRespVO.getBTotalAmount().divide(BigDecimal.valueOf(reportListRespVO.getBTotalNum()), 2, RoundingMode.HALF_UP));

            ReportTotalDO reportTotalD1 = collect1.get(item.getType());
            if (ObjectUtils.isEmpty(reportTotalD1)) {
                reportTotalD1 = new ReportTotalDO();
                reportTotalD1.setTotalAmount(BigDecimal.ZERO);
                reportTotalD1.setTotalNum(0d);
            }
            reportListRespVO.setSTotalAmount(reportTotalD1.getTotalAmount());
            reportListRespVO.setSTotalNum(roundWhole(reportTotalD1.getTotalNum()));
            reportListRespVO.setSAvgAmount(reportListRespVO.getSTotalNum() == 0 ? BigDecimal.ZERO :
                    reportListRespVO.getSTotalAmount().divide(BigDecimal.valueOf(reportListRespVO.getSTotalNum()), 2, RoundingMode.HALF_UP));
            reportListRespVO.setTotalAmountMonthOnMonth(calculateRate(reportListRespVO.getBTotalAmount(), reportListRespVO.getSTotalAmount()));
            returnList.add(reportListRespVO);
        });

        returnObj.setList(returnList);
        return returnObj;
    }

    public ReportRespVO warehouseList(ReportReqVO reqVO) {
        ReportRespVO returnObj = buildBaseReportResp(reqVO);
        List<ReportListRespVO> returnList = new ArrayList<>();

        LambdaQueryWrapper<ReportWarehouseDO> queryWrapper0 = new LambdaQueryWrapper<>();
        queryWrapper0.eq(ReportWarehouseDO::getStartDate, reqVO.getStartDate());
        queryWrapper0.eq(ReportWarehouseDO::getEndDate, reqVO.getEndDate());
        queryWrapper0.eq(ReportWarehouseDO::getNumberType, reqVO.getNumberType());
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr())) {
            queryWrapper0.eq(ReportWarehouseDO::getWarehouseName, reqVO.getSearchStr());
        }
        queryWrapper0.orderByDesc(ReportWarehouseDO::getTotalAmount);
        List<ReportWarehouseDO> reportWarehouseDOS0 = reportWarehouseMapper.selectList(queryWrapper0);

        Pair<LocalDate, LocalDate> otherTime = reqVO.getOtherTime();
        LambdaQueryWrapper<ReportWarehouseDO> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(ReportWarehouseDO::getStartDate, otherTime.getLeft());
        queryWrapper1.eq(ReportWarehouseDO::getEndDate, otherTime.getRight());
        queryWrapper1.eq(ReportWarehouseDO::getNumberType, reqVO.getNumberType());
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr())) {
            queryWrapper1.eq(ReportWarehouseDO::getWarehouseName, reqVO.getSearchStr());
        }
        List<ReportWarehouseDO> reportWarehouseDOS1 = reportWarehouseMapper.selectList(queryWrapper1);

        Map<String, ReportWarehouseDO> collect1 = reportWarehouseDOS1.stream()
                .collect(Collectors.toMap(ReportWarehouseDO::getWarehouseName, r -> r, (old, newR) -> newR));
        reportWarehouseDOS0.forEach(item -> {
            ReportListRespVO reportListRespVO = new ReportListRespVO();
            reportListRespVO.setWarehouseName(item.getWarehouseName());
            reportListRespVO.setBTotalAmount(item.getTotalAmount());
            reportListRespVO.setBTotalNum(roundWhole(item.getTotalNum()));
            reportListRespVO.setBAvgAmount(reportListRespVO.getBTotalNum() == 0 ? BigDecimal.ZERO :
                    reportListRespVO.getBTotalAmount().divide(BigDecimal.valueOf(reportListRespVO.getBTotalNum()), 2, RoundingMode.HALF_UP));

            ReportWarehouseDO reportWarehouseD1 = collect1.get(item.getWarehouseName());
            if (ObjectUtils.isEmpty(reportWarehouseD1)) {
                reportWarehouseD1 = new ReportWarehouseDO();
                reportWarehouseD1.setTotalAmount(BigDecimal.ZERO);
                reportWarehouseD1.setTotalNum(0d);
            }
            reportListRespVO.setSTotalAmount(reportWarehouseD1.getTotalAmount());
            reportListRespVO.setSTotalNum(roundWhole(reportWarehouseD1.getTotalNum()));
            reportListRespVO.setSAvgAmount(reportListRespVO.getSTotalNum() == 0 ? BigDecimal.ZERO :
                    reportListRespVO.getSTotalAmount().divide(BigDecimal.valueOf(reportListRespVO.getSTotalNum()), 2, RoundingMode.HALF_UP));
            reportListRespVO.setTotalAmountMonthOnMonth(calculateRate(reportListRespVO.getBTotalAmount(), reportListRespVO.getSTotalAmount()));
            returnList.add(reportListRespVO);
        });

        returnObj.setList(returnList);
        return returnObj;
    }

    public ReportRespVO commodityList(ReportReqVO reqVO) {
        ReportRespVO returnObj = buildBaseReportResp(reqVO);
        List<ReportListRespVO> returnList = new ArrayList<>();

        List<ReportCommodityDO> currentList = selectCommodityList(reqVO, false).getList();
        List<ReportCommodityDO> compareList = selectCompareCommodityList(reqVO);
        Map<String, ReportCommodityDO> collect1 = compareList.stream()
                .collect(Collectors.toMap(r -> r.getWarehouseName() + "_" + r.getCommodityName(), r -> r, (old, newR) -> newR));

        currentList.forEach(item -> {
            if (ObjectUtils.isEmpty(item.getProportion())) {
                item.setProportion(BigDecimal.ZERO);
            }
            ReportListRespVO reportListRespVO = new ReportListRespVO();
            reportListRespVO.setWarehouseName(item.getWarehouseName());
            reportListRespVO.setCommodityName(item.getCommodityName());
            reportListRespVO.setBTotalAmount(item.getTotalAmount());
            reportListRespVO.setBProportion(item.getProportion().multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));

            ReportCommodityDO reportCommodityD1 = collect1.get(item.getWarehouseName() + "_" + item.getCommodityName());
            if (ObjectUtils.isEmpty(reportCommodityD1) || ObjectUtils.isEmpty(reportCommodityD1.getProportion())) {
                reportCommodityD1 = new ReportCommodityDO();
                reportCommodityD1.setProportion(BigDecimal.ZERO);
                reportCommodityD1.setTotalAmount(BigDecimal.ZERO);
            }
            reportListRespVO.setSTotalAmount(reportCommodityD1.getTotalAmount());
            reportListRespVO.setSProportion(reportCommodityD1.getProportion().multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
            reportListRespVO.setTotalAmountMonthOnMonth(calculateRate(reportListRespVO.getBTotalAmount(), reportListRespVO.getSTotalAmount()));
            returnList.add(reportListRespVO);
        });

        returnObj.setList(returnList);
        return returnObj;
    }

    public ReportRespVO commodityPageList(ReportReqVO reqVO) {
        return pcCommodityPageList(reqVO);
    }

    public ReportRespVO pcCommodityPageList(ReportReqVO reqVO) {
        ReportRespVO returnObj = buildBaseReportResp(reqVO);
        List<ReportListRespVO> returnList = new ArrayList<>();

        PageResult<ReportCommodityDO> pageResult = selectCommodityList(reqVO, true);
        List<ReportCommodityDO> compareList = selectCompareCommodityList(reqVO);
        Map<String, ReportCommodityDO> collect1 = compareList.stream()
                .collect(Collectors.toMap(r -> r.getWarehouseName() + "_" + r.getCommodityName(), r -> r, (old, newR) -> newR));

        pageResult.getList().forEach(item -> {
            if (ObjectUtils.isEmpty(item.getProportion())) {
                item.setProportion(BigDecimal.ZERO);
            }
            ReportListRespVO reportListRespVO = new ReportListRespVO();
            reportListRespVO.setWarehouseName(item.getWarehouseName());
            reportListRespVO.setCommodityName(item.getCommodityName());
            reportListRespVO.setBTotalAmount(item.getTotalAmount());
            reportListRespVO.setBProportion(item.getProportion().multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));

            ReportCommodityDO reportCommodityD1 = collect1.get(item.getWarehouseName() + "_" + item.getCommodityName());
            if (ObjectUtils.isEmpty(reportCommodityD1) || ObjectUtils.isEmpty(reportCommodityD1.getProportion())) {
                reportCommodityD1 = new ReportCommodityDO();
                reportCommodityD1.setProportion(BigDecimal.ZERO);
                reportCommodityD1.setTotalAmount(BigDecimal.ZERO);
            }
            reportListRespVO.setSTotalAmount(reportCommodityD1.getTotalAmount());
            reportListRespVO.setSProportion(reportCommodityD1.getProportion().multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
            reportListRespVO.setTotalAmountMonthOnMonth(calculateRate(reportListRespVO.getBTotalAmount(), reportListRespVO.getSTotalAmount()));
            returnList.add(reportListRespVO);
        });

        returnObj.setList(returnList);
        returnObj.setTotal(pageResult.getTotal());
        return returnObj;
    }

    public ReportRespVO storeList(ReportReqVO reqVO) {
        ReportRespVO returnObj = buildBaseReportResp(reqVO);
        List<ReportListRespVO> returnList = new ArrayList<>();

        List<ReportStoreSalesDO> reportStoreSalesDOList0 = selectStoreSalesList(reqVO, false).getList();
        List<ReportStoreSalesDO> reportStoreSalesDOList1 = selectCompareStoreSalesList(reqVO);
        Map<String, ReportStoreSalesDO> collect1 = reportStoreSalesDOList1.stream()
                .collect(Collectors.toMap(ReportStoreSalesDO::getStoreName, r -> r, (old, newR) -> newR));

        reportStoreSalesDOList0.forEach(item -> {
            ReportListRespVO reportListRespVO = buildStoreSalesResp(item, collect1.get(item.getStoreName()));
            if (reportListRespVO.getBTotalNum() == 0 && reportListRespVO.getSTotalNum() == 0) {
                return;
            }
            returnList.add(reportListRespVO);
        });

        returnObj.setList(returnList);
        return returnObj;
    }

    public ReportRespVO storePage(ReportReqVO reqVO) {
        ReportRespVO returnObj = buildBaseReportResp(reqVO);
        List<ReportListRespVO> returnList = new ArrayList<>();

        PageResult<ReportStoreSalesDO> pageResult = selectStoreSalesList(reqVO, true);
        List<ReportStoreSalesDO> reportStoreSalesDOList1 = selectCompareStoreSalesList(reqVO);
        Map<String, ReportStoreSalesDO> collect1 = reportStoreSalesDOList1.stream()
                .collect(Collectors.toMap(ReportStoreSalesDO::getStoreName, r -> r, (old, newR) -> newR));

        pageResult.getList().forEach(item -> returnList.add(buildStoreSalesResp(item, collect1.get(item.getStoreName()))));
        returnObj.setList(returnList);
        returnObj.setTotal(pageResult.getTotal());
        return returnObj;
    }

    public ReportRespVO commodityTop(ReportReqVO reqVO) {
        ReportRespVO returnObj = buildBaseReportResp(reqVO);
        List<ReportListRespVO> list = commodityList(reqVO).getList();
        List<ReportListRespVO> top10 = list.stream()
                .collect(Collectors.groupingBy(
                        ReportListRespVO::getCommodityName,
                        Collectors.reducing((a, b) -> {
                            ReportListRespVO r = new ReportListRespVO();
                            r.setCommodityName(a.getCommodityName());
                            r.setBTotalAmount(a.getBTotalAmount().add(b.getBTotalAmount()));
                            r.setSTotalAmount(a.getSTotalAmount().add(b.getSTotalAmount()));
                            return r;
                        })
                ))
                .values().stream()
                .map(Optional::get)
                .sorted(Comparator.comparing(ReportListRespVO::getBTotalAmount).reversed())
                .limit(10)
                .toList();

        List<String> x = new ArrayList<>();
        List<Map<String, BigDecimal>> y = new ArrayList<>();
        top10.forEach(reportListRespVO -> {
            x.add(reportListRespVO.getCommodityName());
            Map<String, BigDecimal> map = new HashMap<>();
            map.put("b", reportListRespVO.getBTotalAmount());
            map.put("s", reportListRespVO.getSTotalAmount());
            y.add(map);
        });

        returnObj.setList(top10);
        returnObj.setX(x);
        returnObj.setY(y);
        return returnObj;
    }

    public ReportRespVO saleNumProportion(ReportReqVO reqVO) {
        ReportRespVO returnObj = new ReportRespVO();
        List<ReportListRespVO> returnList = new ArrayList<>();

        List<ReportStoreSalesDO> reportStoreDOS0 = selectStoreSalesByPeriod(reqVO.getStartDate(), reqVO.getEndDate(), reqVO.getNumberType());
        ReportListRespVO reportListRespVO0 = buildSaleNumRangeResp(reportStoreDOS0);
        reportListRespVO0.setStartDate0(DateUtils.localDateToString(reqVO.getStartDate(), DateUtils.YYYY_MM_DD));
        reportListRespVO0.setEndDate0(DateUtils.localDateToString(reqVO.getEndDate(), DateUtils.YYYY_MM_DD));
        returnList.add(reportListRespVO0);

        Pair<LocalDate, LocalDate> otherTime = reqVO.getOtherTime();
        List<ReportStoreSalesDO> reportStoreDOS1 = selectStoreSalesByPeriod(otherTime.getLeft(), otherTime.getRight(), reqVO.getNumberType());
        ReportListRespVO reportListRespVO1 = buildSaleNumRangeResp(reportStoreDOS1);
        reportListRespVO1.setStartDate0(DateUtils.localDateToString(otherTime.getLeft(), DateUtils.YYYY_MM_DD));
        reportListRespVO1.setEndDate0(DateUtils.localDateToString(otherTime.getRight(), DateUtils.YYYY_MM_DD));
        returnList.add(reportListRespVO1);

        returnObj.setList(returnList);
        return returnObj;
    }

    public ReportRespVO getAppSalesNums(ReportReqVO reqVO) {
        ReportRespVO returnObj = new ReportRespVO();
        List<ReportListRespVO> returnList = new ArrayList<>();

        ReportAppSalesnumDO reportAppSalesnumDOS0 = selectAppSalesNum(reqVO.getStartDate(), reqVO.getEndDate(), reqVO.getNumberType());
        Pair<LocalDate, LocalDate> otherTime = reqVO.getOtherTime();
        ReportAppSalesnumDO reportAppSalesnumDOS1 = selectAppSalesNum(otherTime.getLeft(), otherTime.getRight(), reqVO.getNumberType());

        ReportListRespVO reportListRespVO = new ReportListRespVO();
        reportListRespVO.setBTotalNum(roundWhole(reportAppSalesnumDOS0.getSaleNumber()));
        reportListRespVO.setStartDate0(DateUtils.localDateToString(reqVO.getStartDate(), DateUtils.YYYY_MM_DD));
        reportListRespVO.setEndDate0(DateUtils.localDateToString(reqVO.getEndDate(), DateUtils.YYYY_MM_DD));
        returnList.add(reportListRespVO);

        ReportListRespVO reportListRespVO1 = new ReportListRespVO();
        reportListRespVO1.setBTotalNum(roundWhole(reportAppSalesnumDOS1.getSaleNumber()));
        reportListRespVO1.setStartDate0(DateUtils.localDateToString(otherTime.getLeft(), DateUtils.YYYY_MM_DD));
        reportListRespVO1.setEndDate0(DateUtils.localDateToString(otherTime.getRight(), DateUtils.YYYY_MM_DD));
        returnList.add(reportListRespVO1);

        returnObj.setList(returnList);
        return returnObj;
    }

    public ReportRespVO weekStastics(ReportReqVO reqVO) {
        ReportRespVO returnObj = buildBaseReportResp(reqVO);
        List<ReportListRespVO> returnList = new ArrayList<>();

        List<ReportStasticsDO> reportStasticsDOS0 = selectStasticsList(reqVO, false).getList();
        List<ReportStasticsDO> reportStasticsDOS1 = selectCompareStasticsList(reqVO);
        Map<String, ReportStasticsDO> collect1 = reportStasticsDOS1.stream()
                .collect(Collectors.toMap(r -> r.getWarehouseName() + "_" + r.getStoreName(), r -> r, (old, newR) -> newR));
        reportStasticsDOS0.forEach(item -> returnList.add(buildStasticsResp(item, collect1.get(item.getWarehouseName() + "_" + item.getStoreName()))));

        returnObj.setList(returnList);
        return returnObj;
    }

    public ReportRespVO weekStasticsPage(ReportReqVO reqVO) {
        ReportRespVO returnObj = buildBaseReportResp(reqVO);
        List<ReportListRespVO> returnList = new ArrayList<>();

        PageResult<ReportStasticsDO> pageResult = selectStasticsList(reqVO, true);
        List<ReportStasticsDO> reportStasticsDOS1 = selectCompareStasticsList(reqVO);
        Map<String, ReportStasticsDO> collect1 = reportStasticsDOS1.stream()
                .collect(Collectors.toMap(r -> r.getWarehouseName() + "_" + r.getStoreName(), r -> r, (old, newR) -> newR));
        pageResult.getList().forEach(item -> returnList.add(buildStasticsResp(item, collect1.get(item.getWarehouseName() + "_" + item.getStoreName()))));

        returnObj.setList(returnList);
        returnObj.setTotal(pageResult.getTotal());
        return returnObj;
    }

    public List<Map<String, Object>> getBuyProportion(ReportReqVO reqVO) {
        LambdaQueryWrapper<ReportBuyProportionDO> queryWrapper0 = buildBuyProportionQuery(reqVO);
        queryWrapper0.last("limit 1000");

        List<Map<String, Object>> returnList = new ArrayList<>();
        reportBuyProportionMapper.selectMaps(queryWrapper0).forEach(i -> {
            Map<String, Object> map = new HashMap<>();
            map.put("storeName", i.get("store_name"));
            map.put("commodityName", i.get("commodity_name"));
            map.put("proportion", i.get("proportion"));
            returnList.add(map);
        });
        return returnList;
    }

    public ReportRespVO getBuyProportionPage(ReportReqVO reqVO) {
        ReportRespVO returnObj = new ReportRespVO();
        PageResult<ReportBuyProportionDO> pageResult = reportBuyProportionMapper.selectPage(reqVO, buildBuyProportionQuery(reqVO));
        List<ReportListRespVO> list = pageResult.getList().stream().map(i -> {
            ReportListRespVO reportListRespVO = new ReportListRespVO();
            reportListRespVO.setWarehouseName(i.getWarehouseName());
            reportListRespVO.setStoreName(i.getStoreName());
            reportListRespVO.setCommodityName(i.getCommodityName());
            reportListRespVO.setBProportion(i.getProportion());
            return reportListRespVO;
        }).toList();

        returnObj.setList(list);
        returnObj.setTotal(pageResult.getTotal());
        return returnObj;
    }

    public ReportRespVO getBuyAmountPage(ReportReqVO reqVO) {
        ReportRespVO returnObj = new ReportRespVO();
        PageResult<ReportBuyAmountDO> pageResult = reportBuyAmountMapper.selectPage(reqVO, buildBuyAmountQuery(reqVO));
        List<ReportListRespVO> list = pageResult.getList().stream().map(i -> {
            ReportListRespVO reportListRespVO = new ReportListRespVO();
            reportListRespVO.setWarehouseName(i.getWarehouseName());
            reportListRespVO.setStoreName(i.getStoreName());
            reportListRespVO.setCommodityName(i.getCommodityName());
            reportListRespVO.setBBuyTotalAmount(i.getAmount());
            return reportListRespVO;
        }).toList();

        returnObj.setList(list);
        returnObj.setTotal(pageResult.getTotal());
        return returnObj;
    }

    public List<Map<String, String>> getLastYearWeeks() {
        return DateUtils.getLastYearWeeks();
    }

    public List<Map<String, String>> getLastYearMonths() {
        return DateUtils.getLastYearMonths();
    }

    public List<ReportSelectRespVO> getStoreList() {
        return reportOrderMapper.getReportStoreList();
    }

    public List<ReportSelectRespVO> getStasticsStoreList() {
        return reportStasticsMapper.selectStoreList();
    }

    public List<ReportSelectRespVO> getWarehouseList() {
        return reportOrderMapper.getReportWarehouseList();
    }

    @Value("${report.switch.monthComplete:false}")
    private Boolean reportSwitchMonthComplete;

    @Value("${report.switch.weekComplete:false}")
    private Boolean reportSwitchWeekComplete;

    private ReportRespVO buildBaseReportResp(ReportReqVO reqVO) {
        ReportRespVO returnObj = new ReportRespVO();
        Pair<LocalDate, LocalDate> otherTime = reqVO.getOtherTime();
        returnObj.setStartDate0(DateUtils.localDateToString(reqVO.getStartDate(), DateUtils.YYYY_MM_DD));
        returnObj.setEndDate0(DateUtils.localDateToString(reqVO.getEndDate(), DateUtils.YYYY_MM_DD));
        returnObj.setStartDate1(DateUtils.localDateToString(otherTime.getLeft(), DateUtils.YYYY_MM_DD));
        returnObj.setEndDate1(DateUtils.localDateToString(otherTime.getRight(), DateUtils.YYYY_MM_DD));
        return returnObj;
    }

    private PageResult<ReportCommodityDO> selectCommodityList(ReportReqVO reqVO, boolean paged) {
        LambdaQueryWrapper<ReportCommodityDO> queryWrapper0 = new LambdaQueryWrapper<>();
        queryWrapper0.eq(ReportCommodityDO::getStartDate, reqVO.getStartDate());
        queryWrapper0.eq(ReportCommodityDO::getEndDate, reqVO.getEndDate());
        queryWrapper0.eq(ReportCommodityDO::getNumberType, reqVO.getNumberType());
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr())) {
            queryWrapper0.eq(ReportCommodityDO::getWarehouseName, reqVO.getSearchStr());
        }
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr1())) {
            queryWrapper0.like(ReportCommodityDO::getCommodityName, reqVO.getSearchStr1());
        }
        queryWrapper0.orderByDesc(ReportCommodityDO::getTotalAmount);
        if (paged) {
            return reportCommodityMapper.selectPage(reqVO, queryWrapper0);
        }
        return new PageResult<>(reportCommodityMapper.selectList(queryWrapper0), (long) reportCommodityMapper.selectCount(queryWrapper0));
    }

    private List<ReportCommodityDO> selectCompareCommodityList(ReportReqVO reqVO) {
        Pair<LocalDate, LocalDate> otherTime = reqVO.getOtherTime();
        LambdaQueryWrapper<ReportCommodityDO> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(ReportCommodityDO::getStartDate, otherTime.getLeft());
        queryWrapper1.eq(ReportCommodityDO::getEndDate, otherTime.getRight());
        queryWrapper1.eq(ReportCommodityDO::getNumberType, reqVO.getNumberType());
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr())) {
            queryWrapper1.eq(ReportCommodityDO::getWarehouseName, reqVO.getSearchStr());
        }
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr1())) {
            queryWrapper1.like(ReportCommodityDO::getCommodityName, reqVO.getSearchStr1());
        }
        return reportCommodityMapper.selectList(queryWrapper1);
    }

    private PageResult<ReportStoreSalesDO> selectStoreSalesList(ReportReqVO reqVO, boolean paged) {
        LambdaQueryWrapper<ReportStoreSalesDO> queryWrapper0 = new LambdaQueryWrapper<>();
        queryWrapper0.eq(ReportStoreSalesDO::getStartDate, reqVO.getStartDate());
        queryWrapper0.eq(ReportStoreSalesDO::getEndDate, reqVO.getEndDate());
        queryWrapper0.eq(ReportStoreSalesDO::getNumberType, reqVO.getNumberType());
        filterNotEmptyStoreName(queryWrapper0);
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr1())) {
            queryWrapper0.eq(ReportStoreSalesDO::getStoreName, reqVO.getSearchStr1());
        }
        if (paged) {
            return reportStoreSalesMapper.selectPage(reqVO, queryWrapper0);
        }
        return new PageResult<>(reportStoreSalesMapper.selectList(queryWrapper0), (long) reportStoreSalesMapper.selectCount(queryWrapper0));
    }

    private List<ReportStoreSalesDO> selectCompareStoreSalesList(ReportReqVO reqVO) {
        Pair<LocalDate, LocalDate> otherTime = reqVO.getOtherTime();
        LambdaQueryWrapper<ReportStoreSalesDO> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(ReportStoreSalesDO::getStartDate, otherTime.getLeft());
        queryWrapper1.eq(ReportStoreSalesDO::getEndDate, otherTime.getRight());
        queryWrapper1.eq(ReportStoreSalesDO::getNumberType, reqVO.getNumberType());
        filterNotEmptyStoreName(queryWrapper1);
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr1())) {
            queryWrapper1.eq(ReportStoreSalesDO::getStoreName, reqVO.getSearchStr1());
        }
        return reportStoreSalesMapper.selectList(queryWrapper1);
    }

    private void filterNotEmptyStoreName(LambdaQueryWrapper<ReportStoreSalesDO> queryWrapper) {
        queryWrapper.isNotNull(ReportStoreSalesDO::getStoreName);
        queryWrapper.apply("TRIM(store_name) != ''");
    }

    private ReportListRespVO buildStoreSalesResp(ReportStoreSalesDO current, ReportStoreSalesDO compare) {
        ReportListRespVO reportListRespVO = new ReportListRespVO();
        reportListRespVO.setWarehouseName(current.getStoreName());
        reportListRespVO.setStoreName(current.getStoreName());
        reportListRespVO.setBTotalAmount(current.getTotalAmount());
        reportListRespVO.setBTotalNum(roundWhole(current.getTotalNum()));
        reportListRespVO.setBAvgAmount(reportListRespVO.getBTotalNum() == 0 ? BigDecimal.ZERO :
                reportListRespVO.getBTotalAmount().divide(BigDecimal.valueOf(reportListRespVO.getBTotalNum()), 2, RoundingMode.HALF_UP));

        if (ObjectUtils.isEmpty(compare)) {
            compare = new ReportStoreSalesDO();
            compare.setTotalAmount(BigDecimal.ZERO);
            compare.setTotalNum(0d);
        }
        reportListRespVO.setSTotalAmount(compare.getTotalAmount());
        reportListRespVO.setSTotalNum(roundWhole(compare.getTotalNum()));
        reportListRespVO.setSAvgAmount(reportListRespVO.getSTotalNum() == 0 ? BigDecimal.ZERO :
                reportListRespVO.getSTotalAmount().divide(BigDecimal.valueOf(reportListRespVO.getSTotalNum()), 2, RoundingMode.HALF_UP));
        reportListRespVO.setTotalAmountMonthOnMonth(calculateRate(reportListRespVO.getBTotalAmount(), reportListRespVO.getSTotalAmount()));
        return reportListRespVO;
    }

    private List<ReportStoreSalesDO> selectStoreSalesByPeriod(LocalDate startDate, LocalDate endDate, Integer numberType) {
        LambdaQueryWrapper<ReportStoreSalesDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReportStoreSalesDO::getStartDate, startDate);
        queryWrapper.eq(ReportStoreSalesDO::getEndDate, endDate);
        queryWrapper.eq(ReportStoreSalesDO::getNumberType, numberType);
        return reportStoreSalesMapper.selectList(queryWrapper);
    }

    private ReportListRespVO buildSaleNumRangeResp(List<ReportStoreSalesDO> list) {
        ReportListRespVO reportListRespVO = new ReportListRespVO();
        if (list.isEmpty()) {
            return reportListRespVO;
        }
        long cnt50_100 = list.stream()
                .filter(x -> averagePerWeek(x.getTotalNum()).compareTo(new BigDecimal("50")) >= 0
                        && averagePerWeek(x.getTotalNum()).compareTo(new BigDecimal("100")) <= 0)
                .count();
        long cnt100_200 = list.stream()
                .filter(x -> averagePerWeek(x.getTotalNum()).compareTo(new BigDecimal("100")) > 0
                        && averagePerWeek(x.getTotalNum()).compareTo(new BigDecimal("200")) <= 0)
                .count();
        long cnt200Plus = list.stream()
                .filter(x -> averagePerWeek(x.getTotalNum()).compareTo(new BigDecimal("200")) > 0)
                .count();

        reportListRespVO.setCnt50_100(cnt50_100);
        reportListRespVO.setCnt100_200(cnt100_200);
        reportListRespVO.setCnt200Plus(cnt200Plus);
        return reportListRespVO;
    }

    private ReportAppSalesnumDO selectAppSalesNum(LocalDate startDate, LocalDate endDate, Integer numberType) {
        LambdaQueryWrapper<ReportAppSalesnumDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReportAppSalesnumDO::getStartDate, startDate);
        queryWrapper.eq(ReportAppSalesnumDO::getEndDate, endDate);
        queryWrapper.eq(ReportAppSalesnumDO::getNumberType, numberType);
        ReportAppSalesnumDO data = reportAppSalesnumMapper.selectOne(queryWrapper);
        if (ObjectUtils.isEmpty(data)) {
            data = new ReportAppSalesnumDO();
            data.setSaleNumber(0d);
        }
        return data;
    }

    private PageResult<ReportStasticsDO> selectStasticsList(ReportReqVO reqVO, boolean paged) {
        LambdaQueryWrapper<ReportStasticsDO> queryWrapper0 = new LambdaQueryWrapper<>();
        queryWrapper0.eq(ReportStasticsDO::getStartDate, reqVO.getStartDate());
        queryWrapper0.eq(ReportStasticsDO::getEndDate, reqVO.getEndDate());
        queryWrapper0.eq(ReportStasticsDO::getNumberType, reqVO.getNumberType());
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr())) {
            queryWrapper0.eq(ReportStasticsDO::getWarehouseName, reqVO.getSearchStr());
        }
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr1())) {
            queryWrapper0.like(ReportStasticsDO::getStoreName, reqVO.getSearchStr1());
        }
        if (paged) {
            return reportStasticsMapper.selectPage(reqVO, queryWrapper0);
        }
        return new PageResult<>(reportStasticsMapper.selectList(queryWrapper0), (long) reportStasticsMapper.selectCount(queryWrapper0));
    }

    private List<ReportStasticsDO> selectCompareStasticsList(ReportReqVO reqVO) {
        Pair<LocalDate, LocalDate> otherTime = reqVO.getOtherTime();
        LambdaQueryWrapper<ReportStasticsDO> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(ReportStasticsDO::getStartDate, otherTime.getLeft());
        queryWrapper1.eq(ReportStasticsDO::getEndDate, otherTime.getRight());
        queryWrapper1.eq(ReportStasticsDO::getNumberType, reqVO.getNumberType());
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr())) {
            queryWrapper1.eq(ReportStasticsDO::getWarehouseName, reqVO.getSearchStr());
        }
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr1())) {
            queryWrapper1.like(ReportStasticsDO::getStoreName, reqVO.getSearchStr1());
        }
        return reportStasticsMapper.selectList(queryWrapper1);
    }

    private ReportListRespVO buildStasticsResp(ReportStasticsDO current, ReportStasticsDO compare) {
        ReportListRespVO reportListRespVO = new ReportListRespVO();
        reportListRespVO.setWarehouseName(current.getWarehouseName());
        reportListRespVO.setStoreName(current.getStoreName());
        reportListRespVO.setBAvgAmount(current.getSaleNumber() == 0 ? BigDecimal.ZERO :
                current.getSaleAmount().divide(BigDecimal.valueOf(current.getSaleNumber()), 2, RoundingMode.HALF_UP));
        reportListRespVO.setBTotalAmount(current.getSaleAmount().setScale(2, RoundingMode.HALF_UP));
        reportListRespVO.setBBuyTotalAmount(current.getBuyAmount().setScale(2, RoundingMode.HALF_UP));
        reportListRespVO.setBTotalNum(BigDecimal.valueOf(current.getSaleNumber()).setScale(1, RoundingMode.HALF_UP).doubleValue());

        if (ObjectUtils.isEmpty(compare) || (ObjectUtils.isEmpty(compare.getBuyAmount()) && ObjectUtils.isEmpty(compare.getSaleAmount()))) {
            compare = new ReportStasticsDO();
            compare.setBuyAmount(BigDecimal.ZERO);
            compare.setSaleAmount(BigDecimal.ZERO);
            compare.setSaleNumber(0d);
        }
        reportListRespVO.setSAvgAmount(compare.getSaleNumber() == 0 ? BigDecimal.ZERO :
                compare.getSaleAmount().divide(BigDecimal.valueOf(compare.getSaleNumber()), 2, RoundingMode.HALF_UP));
        reportListRespVO.setSTotalAmount(compare.getSaleAmount().setScale(2, RoundingMode.HALF_UP));
        reportListRespVO.setSBuyTotalAmount(compare.getBuyAmount().setScale(2, RoundingMode.HALF_UP));
        reportListRespVO.setSTotalNum(BigDecimal.valueOf(compare.getSaleNumber()).setScale(1, RoundingMode.HALF_UP).doubleValue());
        reportListRespVO.setTotalAmountMonthOnMonth(calculateRate(reportListRespVO.getBTotalAmount(), reportListRespVO.getSTotalAmount()));
        reportListRespVO.setTotalNumMonthOnMonth(reportListRespVO.getSTotalNum() == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(reportListRespVO.getBTotalNum() - reportListRespVO.getSTotalNum())
                        .divide(BigDecimal.valueOf(reportListRespVO.getSTotalNum()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")));
        reportListRespVO.setTotalBuyAmountMonthOnMonth(calculateRate(reportListRespVO.getBBuyTotalAmount(), reportListRespVO.getSBuyTotalAmount()));
        return reportListRespVO;
    }

    private LambdaQueryWrapper<ReportBuyProportionDO> buildBuyProportionQuery(ReportReqVO reqVO) {
        LambdaQueryWrapper<ReportBuyProportionDO> queryWrapper0 = new LambdaQueryWrapper<>();
        queryWrapper0.eq(ReportBuyProportionDO::getStartDate, reqVO.getStartDate());
        queryWrapper0.eq(ReportBuyProportionDO::getEndDate, reqVO.getEndDate());
        queryWrapper0.eq(ReportBuyProportionDO::getNumberType, reqVO.getNumberType());
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr())) {
            queryWrapper0.eq(ReportBuyProportionDO::getStoreName, reqVO.getSearchStr());
        }
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr1())) {
            queryWrapper0.eq(ReportBuyProportionDO::getWarehouseName, reqVO.getSearchStr1());
        }
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr2())) {
            queryWrapper0.eq(ReportBuyProportionDO::getCommodityName, reqVO.getSearchStr2());
        }
        queryWrapper0.orderByDesc(ReportBuyProportionDO::getStoreName, ReportBuyProportionDO::getProportion);
        return queryWrapper0;
    }

    private LambdaQueryWrapper<ReportBuyAmountDO> buildBuyAmountQuery(ReportReqVO reqVO) {
        LambdaQueryWrapper<ReportBuyAmountDO> queryWrapper0 = new LambdaQueryWrapper<>();
        queryWrapper0.eq(ReportBuyAmountDO::getStartDate, reqVO.getStartDate());
        queryWrapper0.eq(ReportBuyAmountDO::getEndDate, reqVO.getEndDate());
        queryWrapper0.eq(ReportBuyAmountDO::getNumberType, reqVO.getNumberType());
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr())) {
            queryWrapper0.eq(ReportBuyAmountDO::getStoreName, reqVO.getSearchStr());
        }
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr1())) {
            queryWrapper0.eq(ReportBuyAmountDO::getWarehouseName, reqVO.getSearchStr1());
        }
        if (!ObjectUtils.isEmpty(reqVO.getSearchStr2())) {
            queryWrapper0.eq(ReportBuyAmountDO::getCommodityName, reqVO.getSearchStr2());
        }
        queryWrapper0.orderByDesc(ReportBuyAmountDO::getStoreName, ReportBuyAmountDO::getAmount);
        return queryWrapper0;
    }

    private Double roundWhole(Double value) {
        return BigDecimal.valueOf(ObjectUtils.isEmpty(value) ? 0d : value).setScale(0, RoundingMode.HALF_UP).doubleValue();
    }

    private BigDecimal calculateRate(BigDecimal current, BigDecimal compare) {
        if (compare.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return current.subtract(compare).divide(compare, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    private BigDecimal averagePerWeek(Double totalNum) {
        return BigDecimal.valueOf(totalNum).divide(new BigDecimal(7), 10, RoundingMode.HALF_UP);
    }

    /**
     * 总体-周报
     */
    public void reportTotalWeek() {
        if (reportSwitchWeekComplete) {
            for (int i = 1; i <= 55; i++) {
                this.reportTotal(1, i);
            }
        } else {
            this.reportTotal(1, 1);
        }
    }

    /**
     * 总体-月报
     */
    public void reportTotalMonth() {
        if (reportSwitchMonthComplete) {
            for (int i = 1; i <= 17; i++) {
                this.reportTotal(2, i);
            }
        } else {
            this.reportTotal(2, 1);
        }
    }

    void reportTotal(Integer numberType, Integer n) {
        Result result = this.getResult(numberType, n);

        List<OrderReportDTO> reportTotal = new ArrayList<>();
        List<OrderReportDTO> reportTotalFHX = reportOrderMapper.getReportTotalFHX(result.tableFix0, result.tableFix1, result.sTimeStr, result.eTimeStr, numberType);
        List<OrderReportDTO> reportTotalPSX = reportOrderMapper.getReportTotalPSX(result.tableFix0, result.tableFix1, result.sTimeStr, result.eTimeStr, numberType);
        List<OrderReportDTO> reportTotalWDX = reportOrderMapper.getReportTotalWDX(result.tableFix0, result.tableFix1, result.sTimeStr, result.eTimeStr, numberType);
        List<OrderReportDTO> reportTotal220 = reportOrderMapper.getReportTotal220(result.tableFix0, result.tableFix1, result.sTimeStr, result.eTimeStr, numberType);
        List<OrderReportDTO> reportTotalZBX = reportOrderMapper.getReportTotalZBX(result.tableFix0, result.tableFix1, result.sTimeStr, result.eTimeStr, numberType);
        List<OrderReportDTO> reportTotalLDS = reportOrderMapper.getReportTotalLDS(result.tableFix0, result.tableFix1, result.sTimeStr, result.eTimeStr, numberType);

        reportTotal.addAll(reportTotalFHX);
        reportTotal.addAll(reportTotalPSX);
        reportTotal.addAll(reportTotalWDX);
        reportTotal.addAll(reportTotal220);
        reportTotal.addAll(reportTotalZBX);
        reportTotal.addAll(reportTotalLDS);

        List<ReportTotalDO> reportTotalDOList = reportTotal.stream().map(i -> {
            ReportTotalDO reportTotalDO = new ReportTotalDO();
            BeanUtils.copyProperties(i, reportTotalDO);
            reportTotalDO.setTotalAmount(ObjectUtils.isEmpty(i.getTotalAmount()) ? BigDecimal.ZERO : i.getTotalAmount());
            reportTotalDO.setTotalNum(ObjectUtils.isEmpty(i.getTotalNum()) ? 0d : i.getTotalNum());
            reportTotalDO.setStartDate(result.sDate);
            reportTotalDO.setEndDate(result.eDate);
            return reportTotalDO;
        }).toList();

        reportTotalMapper.delete(
                new LambdaQueryWrapper<ReportTotalDO>()
                        .eq(ReportTotalDO::getStartDate, result.sDate)
                        .eq(ReportTotalDO::getEndDate, result.eDate)
        );
        reportTotalMapper.insert(reportTotalDOList);
    }


    /**
     * 仓库-周报
     */
    public void reportWarehouseWeek() {
        if (reportSwitchWeekComplete) {
            for (int i = 1; i <= 55; i++) {
                this.reportWarehouse(1, i);
            }
        } else {
            this.reportWarehouse(1, 1);
        }
    }

    /**
     * 仓库-月报
     */
    public void reportWarehouseMonth() {
        if (reportSwitchMonthComplete) {
            for (int i = 1; i <= 17; i++) {
                this.reportWarehouse(2, i);
            }
        } else {
            this.reportWarehouse(2, 1);
        }
    }

    void reportWarehouse(Integer numberType, Integer n) {
        Result result = this.getResult(numberType, n);

        List<OrderReportDTO> reportTotal = reportOrderMapper.getReportWarehouse(result.tableFix0, result.tableFix1, result.sTimeStr, result.eTimeStr, numberType);

        List<ReportWarehouseDO> reportWarehouseDOList = reportTotal.stream().map(i -> {
            ReportWarehouseDO reportWarehouseDO = new ReportWarehouseDO();
            BeanUtils.copyProperties(i, reportWarehouseDO);
            reportWarehouseDO.setTotalAmount(ObjectUtils.isEmpty(i.getTotalAmount()) ? BigDecimal.ZERO : i.getTotalAmount());
            reportWarehouseDO.setTotalNum(ObjectUtils.isEmpty(i.getTotalNum()) ? 0d : i.getTotalNum());
            reportWarehouseDO.setStartDate(result.sDate);
            reportWarehouseDO.setEndDate(result.eDate);
            return reportWarehouseDO;
        }).toList();

        reportWarehouseMapper.delete(
                new LambdaQueryWrapper<ReportWarehouseDO>()
                        .eq(ReportWarehouseDO::getStartDate, result.sDate)
                        .eq(ReportWarehouseDO::getEndDate, result.eDate)
        );
        reportWarehouseMapper.insert(reportWarehouseDOList);
    }

    /**
     * 单品-周报
     */
    public void reportCommodityWeek() {
        if (reportSwitchWeekComplete) {
            for (int i = 1; i <= 55; i++) {
                this.reportCommodity(1, i);
            }
        } else {
            this.reportCommodity(1, 1);
        }
    }

    /**
     * 单品-月报
     */
    public void reportCommodityMonth() {
        if (reportSwitchMonthComplete) {
            for (int i = 1; i <= 17; i++) {
                this.reportCommodity(2, i);
            }
        } else {
            this.reportCommodity(2, 1);
        }
    }

    void reportCommodity(Integer numberType, Integer n) {
        Result result = this.getResult(numberType, n);
        List<OrderReportDTO> reportTotal = reportOrderMapper.getReportCommodity(result.tableFix0(), result.tableFix1(), result.sTimeStr(), result.eTimeStr(), numberType);

        List<ReportCommodityDO> reportCommodityDOList = reportTotal.stream().map(i -> {
            ReportCommodityDO reportCommodityDO = new ReportCommodityDO();
            BeanUtils.copyProperties(i, reportCommodityDO);
            reportCommodityDO.setTotalAmount(ObjectUtils.isEmpty(i.getTotalAmount()) ? BigDecimal.ZERO : i.getTotalAmount());
            reportCommodityDO.setProportion(ObjectUtils.isEmpty(i.getProportion()) ? BigDecimal.ZERO : i.getProportion());
            reportCommodityDO.setStartDate(result.sDate());
            reportCommodityDO.setEndDate(result.eDate());
            return reportCommodityDO;
        }).toList();

        reportCommodityMapper.delete(
                new LambdaQueryWrapper<ReportCommodityDO>()
                        .eq(ReportCommodityDO::getStartDate, result.sDate())
                        .eq(ReportCommodityDO::getEndDate, result.eDate())
        );
        reportCommodityMapper.insert(reportCommodityDOList);
    }

    /**
     * 门店销售-周报
     */
    public void reportStoreSalesWeek() {
        if (reportSwitchWeekComplete) {
            for (int i = 1; i <= 55; i++) {
                this.reportStoreSales(1, i);
            }
        } else {
            this.reportStoreSales(1, 1);
        }
    }

    /**
     * 门店销售-周报
     */
    public void reportStoreSalesMonth() {
        if (reportSwitchMonthComplete) {
            for (int i = 1; i <= 17; i++) {
                this.reportStoreSales(2, i);
            }
        } else {
            this.reportStoreSales(2, 1);
        }
    }

    void reportStoreSales(Integer numberType, Integer n) {
        Result result = this.getResult(numberType, n);
        List<OrderReportDTO> reportTotal = reportOrderMapper.getReportStoreSales(result.tableFix0(), result.tableFix1(), result.sTimeStr(), result.eTimeStr(), numberType);
        List<ReportStoreSalesDO> reportStoreSalesDOList = reportTotal.stream().map(i -> {
            ReportStoreSalesDO reportStoreSalesDO = new ReportStoreSalesDO();
            BeanUtils.copyProperties(i, reportStoreSalesDO);
            reportStoreSalesDO.setTotalAmount(ObjectUtils.isEmpty(i.getTotalAmount()) ? BigDecimal.ZERO : i.getTotalAmount());
            reportStoreSalesDO.setTotalNum(ObjectUtils.isEmpty(i.getTotalNum()) ? 0d : i.getTotalNum());
            reportStoreSalesDO.setStartDate(result.sDate());
            reportStoreSalesDO.setEndDate(result.eDate());
            return reportStoreSalesDO;
        }).toList();

        reportStoreSalesMapper.delete(
                new LambdaQueryWrapper<ReportStoreSalesDO>()
                        .eq(ReportStoreSalesDO::getStartDate, result.sDate())
                        .eq(ReportStoreSalesDO::getEndDate, result.eDate())
        );
        reportStoreSalesMapper.insert(reportStoreSalesDOList);
    }

    /**
     * 订单分析-周报
     */
    public void reportStasticsWeek() {
        if (reportSwitchWeekComplete) {
            for (int i = 1; i <= 55; i++) {
                this.reportStastics(1, i);
            }
        } else {
            this.reportStastics(1, 1);
        }
    }

    /**
     * 订单分析-月报
     */
    public void reportStasticsMonth() {
        if (reportSwitchMonthComplete) {
            for (int i = 1; i <= 17; i++) {
                this.reportStastics(2, i);
            }
        } else {
            this.reportStastics(2, 1);
        }
    }

    void reportStastics(Integer numberType, Integer n) {
        Result result = this.getResult(numberType, n);
        List<OrderReportDTO> reportTotal = reportOrderMapper.getReportStastics(result.tableFix0(), result.tableFix1(), result.sTimeStr(), result.eTimeStr(), numberType);
        List<ReportStasticsDO> reportStasticsDOList = reportTotal.stream().map(i -> {
            ReportStasticsDO reportStasticsDO = new ReportStasticsDO();
            BeanUtils.copyProperties(i, reportStasticsDO);
            reportStasticsDO.setSaleAmount(ObjectUtils.isEmpty(i.getTotalAmount()) ? BigDecimal.ZERO : i.getTotalAmount());
            reportStasticsDO.setSaleNumber(ObjectUtils.isEmpty(i.getTotalNum()) ? 0d : Double.valueOf(i.getTotalNum()));
            reportStasticsDO.setBuyAmount(ObjectUtils.isEmpty(i.getBuyAmount()) ? BigDecimal.ZERO : i.getBuyAmount());
            reportStasticsDO.setStartDate(result.sDate());
            reportStasticsDO.setEndDate(result.eDate());
            return reportStasticsDO;
        }).toList();

        reportStasticsMapper.delete(
                new LambdaQueryWrapper<ReportStasticsDO>()
                        .eq(ReportStasticsDO::getStartDate, result.sDate())
                        .eq(ReportStasticsDO::getEndDate, result.eDate())
        );
        reportStasticsMapper.insert(reportStasticsDOList);
    }

    public void reportAppSalesnumWeek() {
        if (reportSwitchWeekComplete) {
            for (int i = 1; i <= 55; i++) {
                this.reportAppSalesnum(1, i);
            }
        } else {
            this.reportAppSalesnum(1, 1);
        }
    }

    public void reportAppSalesnumMonth() {
        if (reportSwitchMonthComplete) {
            for (int i = 1; i <= 17; i++) {
                this.reportAppSalesnum(2, i);
            }
        } else {
            this.reportAppSalesnum(2, 1);
        }
    }

    void reportAppSalesnum(Integer numberType, Integer n) {
        Result result = this.getResult(numberType, n);
        Integer appSalesNum = reportOrderMapper.getAppSalesNum(result.tableFix0(), result.tableFix1(), result.sTimeStr(), result.eTimeStr());

        ReportAppSalesnumDO reportAppSalesnumDO = new ReportAppSalesnumDO();
        reportAppSalesnumDO.setSaleNumber(Double.valueOf(appSalesNum));
        reportAppSalesnumDO.setNumberType(numberType);
        reportAppSalesnumDO.setStartDate(result.sDate);
        reportAppSalesnumDO.setEndDate(result.eDate);

        reportAppSalesnumMapper.delete(
                new LambdaQueryWrapper<ReportAppSalesnumDO>()
                        .eq(ReportAppSalesnumDO::getStartDate, result.sDate())
                        .eq(ReportAppSalesnumDO::getEndDate, result.eDate())
        );
        reportAppSalesnumMapper.insert(reportAppSalesnumDO);
    }

    public void reportBuyProportionMonth() {
        if (reportSwitchMonthComplete) {
            for (int i = 1; i <= 17; i++) {
                this.reportBuyProportion(2, i);
            }
        } else {
            this.reportBuyProportion(2, 1);
        }
    }

    void reportBuyProportion(Integer numberType, Integer n) {
        Result result = this.getResult(numberType, n);
        List<OrderReportDTO> orderReportDTOS = reportOrderMapper.getReportBuyProportion(result.tableFix0(), result.sTimeStr(), result.eTimeStr(), numberType);
        List<OrderReportWithRateDTO> orderReportWithRateDTOS = this.calcRate(orderReportDTOS);

        List<ReportBuyProportionDO> reportBuyProportionDOList = orderReportWithRateDTOS.stream().map(i -> {
            ReportBuyProportionDO reportAppSalesnumDO = new ReportBuyProportionDO();
            reportAppSalesnumDO.setProportion(i.getRate());
            reportAppSalesnumDO.setWarehouseName(i.getWarehouseName());
            reportAppSalesnumDO.setStoreName(i.getStoreName());
            reportAppSalesnumDO.setCommodityName(i.getCommodityName());
            reportAppSalesnumDO.setNumberType(numberType);
            reportAppSalesnumDO.setStartDate(result.sDate);
            reportAppSalesnumDO.setEndDate(result.eDate);
            return reportAppSalesnumDO;
        }).toList();

        reportBuyProportionMapper.delete(
                new LambdaQueryWrapper<ReportBuyProportionDO>()
                        .eq(ReportBuyProportionDO::getStartDate, result.sDate())
                        .eq(ReportBuyProportionDO::getEndDate, result.eDate())
        );
        reportBuyProportionMapper.insert(reportBuyProportionDOList);
    }

    public void reportBuyAmountMonth() {
        if (reportSwitchMonthComplete) {
            for (int i = 1; i <= 20; i++) {
                this.reportBuyAmount(2, i);
            }
        } else {
            this.reportBuyAmount(2, 1);
        }
    }

    void reportBuyAmount(Integer numberType, Integer n) {
        Result result = this.getResult(numberType, n);
        List<OrderReportDTO> orderReportDTOS = reportOrderMapper.getReportBuyProportion(result.tableFix0(), result.sTimeStr(), result.eTimeStr(), numberType);

        List<ReportBuyAmountDO> reportBuyAmountDOList = orderReportDTOS.stream().map(i -> {
            ReportBuyAmountDO reportBuyAmountDO = new ReportBuyAmountDO();
            reportBuyAmountDO.setAmount(ObjectUtils.isEmpty(i.getBuyAmount()) ? BigDecimal.ZERO : i.getBuyAmount());
            reportBuyAmountDO.setWarehouseName(i.getWarehouseName());
            reportBuyAmountDO.setStoreName(i.getStoreName());
            reportBuyAmountDO.setCommodityName(i.getCommodityName());
            reportBuyAmountDO.setNumberType(numberType);
            reportBuyAmountDO.setStartDate(result.sDate);
            reportBuyAmountDO.setEndDate(result.eDate);
            return reportBuyAmountDO;
        }).toList();

        reportBuyAmountMapper.delete(
                new LambdaQueryWrapper<ReportBuyAmountDO>()
                        .eq(ReportBuyAmountDO::getStartDate, result.sDate())
                        .eq(ReportBuyAmountDO::getEndDate, result.eDate())
        );
        reportBuyAmountMapper.insert(reportBuyAmountDOList);
    }

    /**
     * 采购比例-月报
     */
    private List<OrderReportWithRateDTO> calcRate(List<OrderReportDTO> list) {

        // 1. 按 storeName 分组
        Map<String, List<OrderReportDTO>> byStoreMap = list.stream()
                .collect(Collectors.groupingBy(OrderReportDTO::getStoreName));

        List<OrderReportWithRateDTO> result = new ArrayList<>();

        // 2. 遍历每个门店，先算总额，再算占比
        for (Map.Entry<String, List<OrderReportDTO>> entry : byStoreMap.entrySet()) {
            List<OrderReportDTO> storeItems = entry.getValue();

            // 求该门店的采购总额
            BigDecimal storeTotal = storeItems.stream()
                    .map(OrderReportDTO::getBuyAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 3. 计算占比并返回新对象
            for (OrderReportDTO item : storeItems) {
                OrderReportWithRateDTO dto = new OrderReportWithRateDTO();
                BeanUtils.copyProperties(item, dto);

                if (storeTotal.compareTo(BigDecimal.ZERO) == 0) {
                    dto.setRate(BigDecimal.ZERO);
                } else {
                    dto.setRate(
                            item.getBuyAmount()
                                    .divide(storeTotal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) // 保留 4 位小数
                    );
                }

                result.add(dto);
            }
        }

        return result;
    }


    private Result getResult(Integer numberType, int n) {
        Map<String, LocalDate> lastStartAndEnd;
        Map<String, String> lastYYYYMMStartAndEnd;
        if (numberType == 1) {
            lastStartAndEnd = DateUtils.getLastWeekStartAndEnd(n);
            lastYYYYMMStartAndEnd = DateUtils.getLastYYYYMMStartAndEndForWeek(lastStartAndEnd.get("end"));
        } else {
            lastStartAndEnd = DateUtils.getLastMonthStartAndEnd(n);
            lastYYYYMMStartAndEnd = DateUtils.getLastYYYYMMStartAndEndForMonth(n);
        }

        LocalDate sDate = lastStartAndEnd.get("start");
        LocalDate eDate = lastStartAndEnd.get("end");

        String sDateStr = DateUtils.localDateToString(sDate, DateUtils.YYYY_MM_DD);
        String eDateStr = DateUtils.localDateToString(eDate, DateUtils.YYYY_MM_DD);

        String sTimeStr = sDateStr + " 00:00:00";
        String eTimeStr = eDateStr + " 23:59:59";

        String tableFix0 = "_" + lastYYYYMMStartAndEnd.get("start");
        String tableFix1 = "_" + lastYYYYMMStartAndEnd.get("end");
        return new Result(sDate, eDate, sTimeStr, eTimeStr, tableFix0, tableFix1);
    }

    private record Result(LocalDate sDate, LocalDate eDate, String sTimeStr, String eTimeStr, String tableFix0,
                          String tableFix1) {
    }
}
