package com.htyoudao.youdao.module.system.service.complant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.htyoudao.youdao.module.system.dal.dataobject.complaint.ComplaintDO;
import com.htyoudao.youdao.module.system.dal.mysql.complaint.ComplaintMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Validated
public class ComplaintAppServiceImpl implements ComplaintAppService {
    @Resource
    private ComplaintMapper complaintMapper;


    @Override
    public List<String> getComplaintListByMemberId(Long memberId) {
        // 获取当前日期
        LocalDate today = LocalDate.now();
        Date todayStart = Date.from(today.minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date todayEnd = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        // 查询最近3天内的投诉订单
        List<ComplaintDO> complaints = complaintMapper.selectList(new LambdaQueryWrapper<ComplaintDO>()
                .eq(ComplaintDO::getMemberId, memberId)
                .between(ComplaintDO::getCreateTime, todayStart, todayEnd));
        // 将订单号转换为字符串列表
        return complaints.stream()
                .map(ComplaintDO::getOrderSn)
                .collect(Collectors.toList());
    }
    @Override
    public Map<Integer, Map<String, Object>> getComplaintAggByStore(Long storeId) {
        // 1. 参数校验
        if (storeId == null ) {
            throw new IllegalArgumentException("门店ID不能为空");
        }

        // 2. 计算时间范围（逻辑不变）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate today = LocalDate.now();
        // 上周时间范围
        LocalDate lastWeekMonday = today.minusWeeks(1).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate lastWeekSunday = today.minusWeeks(1).with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
        String lastWeekStart = lastWeekMonday.atStartOfDay().format(formatter);
        String lastWeekEnd = lastWeekSunday.atTime(23, 59, 59).format(formatter);
        // 上上周时间范围
        LocalDate beforeLastWeekMonday = lastWeekMonday.minusWeeks(1);
        LocalDate beforeLastWeekSunday = lastWeekSunday.minusWeeks(1);
        String beforeLastWeekStart = beforeLastWeekMonday.atStartOfDay().format(formatter);
        String beforeLastWeekEnd = beforeLastWeekSunday.atTime(23, 59, 59).format(formatter);

        // 3. 统计数据（逻辑不变）
        Map<Integer, Integer> lastWeekCountMap = countComplaintByType(storeId, lastWeekStart, lastWeekEnd);
        Map<Integer, Integer> beforeLastWeekCountMap = countComplaintByType(storeId, beforeLastWeekStart, beforeLastWeekEnd);

        // 4. 构建Map类型的返回结果（核心改动）
        Map<Integer, Map<String, Object>> resultMap = new HashMap<>();
        // 投诉类型映射：key=编码，value=名称
        Map<Integer, String> typeNameMap = new HashMap<>();
        typeNameMap.put(0, "其他投诉");
        typeNameMap.put(1, "产品投诉");
        typeNameMap.put(2, "服务投诉");
        typeNameMap.put(3, "卫生投诉");

        for (Map.Entry<Integer, String> entry : typeNameMap.entrySet()) {
            Integer typeCode = entry.getKey();
            String typeName = entry.getValue();

            // 获取上周/上上周数量（默认0）
            Integer lastWeekCount = lastWeekCountMap.getOrDefault(typeCode, 0);
            Integer beforeLastWeekCount = beforeLastWeekCountMap.getOrDefault(typeCode, 0);

            // 计算环比（避免除零，保留2位小数）
            Double ringRatio = 0.0;
            if (beforeLastWeekCount != 0) {
                BigDecimal ratio = new BigDecimal(lastWeekCount - beforeLastWeekCount)
                        .divide(new BigDecimal(beforeLastWeekCount), 4, RoundingMode.HALF_UP);
                ringRatio = ratio.setScale(2, RoundingMode.HALF_UP).doubleValue();
            }

            // 构建内层统计详情Map
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("typeName", typeName); // 投诉类型名称（可选，提升可读性）
            detailMap.put("lastWeekCount", lastWeekCount); // 上周数量
            detailMap.put("beforeLastWeekCount", beforeLastWeekCount); // 上上周数量
            detailMap.put("ringRatio", ringRatio); // 环比变化率

            // 放入外层Map：key=投诉类型编码
            resultMap.put(typeCode, detailMap);
        }

        return resultMap;
    }

    /**
     * 时间范围、投诉类型统计数量（基于MyBatis-Plus实现）
     * @param storeId 门店ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return key: business_type, value: 投诉数量
     */
    private Map<Integer, Integer> countComplaintByType(Long storeId, String startDate, String endDate) {
        QueryWrapper<ComplaintDO> queryWrapper = new QueryWrapper<ComplaintDO>()
                .select("business_type, COUNT(id) AS count")
                .eq("deleted", 0)
                .eq("store_id", storeId)
                .between("create_time", startDate, endDate)
                .groupBy("business_type");

        // 执行查询
        List<Map<String, Object>> resultList = complaintMapper.selectMaps(queryWrapper);

        // 转换为Map（逻辑不变）
        Map<Integer, Integer> countMap = new HashMap<>();
        for (Map<String, Object> map : resultList) {
            Integer businessType = (Integer) map.get("business_type");
            Integer count = ((Number) map.get("count")).intValue();
            countMap.put(businessType, count);
        }
        return countMap;
    }
}