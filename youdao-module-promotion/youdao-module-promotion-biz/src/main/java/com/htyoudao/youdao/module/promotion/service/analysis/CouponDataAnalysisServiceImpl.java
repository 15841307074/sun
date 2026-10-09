package com.htyoudao.youdao.module.promotion.service.analysis;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.mybatis.core.query.QueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.promotion.controller.admin.analysis.vo.*;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponDateRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreAndOrgDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponDataAnalysisBySourceDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponRecordDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponStoreIdDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcoupon.GoodCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercouponrecord.UserCouponRecordDataAnanlysisBySourceMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercouponrecord.UserCouponRecordMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercouponrecord.UserCouponStoreIdMapper;
import com.htyoudao.youdao.module.promotion.enums.CouponEventType;
import com.htyoudao.youdao.module.promotion.enums.advertising.carousel.CouponISCommonEnum;
import com.htyoudao.youdao.module.promotion.service.couponstore.CouponStoreService;
import com.htyoudao.youdao.module.promotion.service.goodcoupon.ICouponEventService;
import com.htyoudao.youdao.module.system.api.org.OrgApi;
import com.htyoudao.youdao.module.system.api.org.dto.OrgRespDTO;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.api.storeinfo.StoreInfoApi;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CouponDataAnalysisServiceImpl implements CouponDataAnalysisService {


    @Resource
    private UserCouponMapper userCouponMapper;

    @Resource
    private UserCouponRecordMapper userCouponRecordMapper;

    @Resource
    private UserCouponRecordDataAnanlysisBySourceMapper userCouponRecordDataAnanlysisBySourceMapper;

    @Resource
    private UserCouponStoreIdMapper userCouponStoreIdMapper;

    @DubboReference
    private OrgApi orgApi;

    @DubboReference
    private StoreInfoApi storeInfoApi;

    @DubboReference
    private StoreApi storeApi;

    @Resource
    private CouponStoreService couponStoreService;

    @Resource
    private ExcelActionService excelActionService;
    @Autowired
    private GoodCouponMapper goodCouponMapper;

    @Resource
    private ICouponEventService couponEventService;


    @Override
    @DS(DsNameConstants.SHARDING)
    public CouponDataAnalysisRespVO getDataAnalysis(Long id) {

        // 获取登陆人可见所有门店
        /* 优惠券数据分析 放开权限
        Long userId = WebFrameworkUtils.getLoginUserId();
        Set<Long> storeIdList = storeApi.getAllStoreIdByUser(userId).getCheckedData();*/

        CouponDataAnalysisRespVO result = CouponDataAnalysisRespVO.initVO();
        // 领券总次数
        LambdaQueryWrapper<UserCouponDO> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(UserCouponDO::getCouponId, id);
        /*// 查询登录人范围的用户领取信息
        userWrapper.in(UserCouponDO::getStoreId, storeIdList);*/
        buildReceived(result, userWrapper);

        // 其他数据分析 现调整为实时
//        LocalDateTime yesterdayLastSecond = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.DAYS)
//                .plusDays(1).minusSeconds(1);

        LambdaQueryWrapper<UserCouponRecordDO> wrapper = new LambdaQueryWrapper<>();
//        wrapper.le(UserCouponRecordDO::getCreateTime, yesterdayLastSecond);
        wrapper.eq(UserCouponRecordDO::getCouponId, id);
        /*// 查询登陆人范围的用户用券信息
        wrapper.in(UserCouponRecordDO::getStoreId, storeIdList);*/
        buildData(result, wrapper);

        Map<String, Long> couponShare = couponEventService.statPvUv(CouponEventType.COUPON_SHARE, id.toString(), LocalDateTime.now().minusYears(1), LocalDateTime.now(), BusinessContextHolder.getBusinessId());
//        if(ObjectUtil.isNotEmpty(couponShare)){
//            result.setPv(couponShare.get("pv"));
//            result.setUv(couponShare.get("uv"));
//        }
        Map<String, Long> couponView = couponEventService.statPvUv(CouponEventType.COUPON_VIEW, id.toString(), LocalDateTime.now().minusYears(1), LocalDateTime.now(), BusinessContextHolder.getBusinessId());
        if(ObjectUtil.isNotEmpty(couponView)){
            result.setPv(couponView.get("pv"));
            result.setUv(couponView.get("uv"));
        }
        result.setCouponShareNum(couponShare.get("pv"));

        return result;
    }
    @DS(DsNameConstants.SHARDING)
    @Override
    public PageResult<CouponDataAnalysisRespVO> getDataByStoreAndOrg(CouponDataAnalysisReqVO couponDataAnalysisReqVO) {
/*

        Long userId = WebFrameworkUtils.getLoginUserId();
        Set<Long> storeIdList = storeApi.getAllStoreIdByUser(userId).getCheckedData();
*/

        // 获取选择组织ID
        Long orgId = couponDataAnalysisReqVO.getOrgId();
        Set<OrgRespDTO> allOrg = null;
        Long[] orgArr;
        if(ObjectUtil.isNotEmpty(orgId)){
            allOrg = getAllOrg(orgId);
            orgArr = allOrg.stream()
                    .map(OrgRespDTO::getId)
                    .toArray(Long[]::new);
        } else {
            orgArr = null;
        }

        // 设置分页对象 初始化
        PageResult<CouponDataAnalysisRespVO> pageResult = new PageResult<>();
        pageResult.setTotal(0L);
        List<CouponStoreAndOrgDO> storeInfoDTOList = null;
        // 优惠券是2 门店券 获取所有门店 通过优惠券门店关系表
        if (CouponISCommonEnum.PART.getValue().equals( couponDataAnalysisReqVO.getIsCommon())){
            storeInfoDTOList = couponStoreService.getStoresByCouponIdAndName(couponDataAnalysisReqVO.getCouponId(),couponDataAnalysisReqVO.getStoreName());
        }
        // 优惠券是1 通用 获取所有门店 门店info表
        if(CouponISCommonEnum.ALL.getValue().equals( couponDataAnalysisReqVO.getIsCommon())){
            CommonResult<List<StoreInfoDTO>> storesByName = storeInfoApi.getStoresByName(couponDataAnalysisReqVO.getStoreName());
            List<StoreInfoDTO> data = storesByName.getData();
            storeInfoDTOList = BeanUtils.toBean(data, CouponStoreAndOrgDO.class);
        }
        if(storeInfoDTOList == null){
            return pageResult;
        }/*else{
            // 过滤门店为登录人范围内门店
            storeInfoDTOList = storeInfoDTOList.stream().filter(ele -> storeIdList.contains(ele.getStoreId())).collect(Collectors.toList());
        }*/

        // 门店筛选 获取满足组织条件的门店

        List<CouponStoreAndOrgDO> stores;

        // 搜索的门店总数 集合类型优化
        if(CollectionUtil.isNotEmpty(allOrg)){
            Set<Long> orgIdSet = new HashSet<>(Arrays.asList(orgArr));
            stores = storeInfoDTOList.stream().filter(ele -> orgIdSet.contains(ele.getOrgId())).collect(Collectors.toList());
        }else {
            stores = storeInfoDTOList;
        }
        // 将列表转换为只包含 storeId 的 List<Long>
        /*List<Long> storeIdList = storeInfoDTOList.stream()
                .map(CouponStoreAndOrgDO::getStoreId)
                .collect(Collectors.toList());*/
        // 统计优惠券已使用过的门店 按用券总成交额倒叙排列
        List<UserCouponStoreIdDO> usedStoreIdList = null;
        // 按照渠道获取用户优惠券使用情况
        QueryWrapperX<UserCouponStoreIdDO> userCouponRecordWrapper = new QueryWrapperX<>();
        userCouponRecordWrapper.select("""
                store_id,
                IFNULL(SUM(total_amount),0)   as turnover
                """);
        userCouponRecordWrapper.eq("coupon_id", couponDataAnalysisReqVO.getCouponId());
//        userCouponRecordWrapper.in("store_id", storeIdList);
        userCouponRecordWrapper.groupBy("store_id");
        userCouponRecordWrapper.orderByDesc("turnover");
        usedStoreIdList = userCouponStoreIdMapper.selectList(userCouponRecordWrapper);

        // 将storeInfoDTOList进行排序 storeInfoDTOList内已使用的门店排在前面 使用量降序 未使用的排在后面
        orderStoreInfoDTOList(stores,usedStoreIdList);
        // 获取分页门店
        if(CollectionUtil.isNotEmpty(stores)){

            // 获取页面需要展示的门店
            long current = couponDataAnalysisReqVO.getPageNo();
            long size = couponDataAnalysisReqVO.getPageSize();
            long start = (current-1) * size ;
            long end = (start + size)>stores.size()?stores.size():(start + size) ;
            List<CouponStoreAndOrgDO> storeList = stores.subList((int) start, (int) end);

            //
            List<CouponDataAnalysisRespVO> resultList = new ArrayList<>();
            List<Long> storeIds = storeList.stream().map(CouponStoreDO::getStoreId).toList();

            QueryWrapperX<UserCouponDO> userWrapper = new QueryWrapperX<>();
            userWrapper.eq("coupon_id", couponDataAnalysisReqVO.getCouponId());
            userWrapper.isNotNull("store_id");
            userWrapper.in("store_id", storeIds);
            userWrapper.groupBy("store_id");
            userWrapper.select("store_id as storeId,count(id) as receiveNum");
            List<Map<String, Object>> maps = userCouponMapper.selectMaps(userWrapper);
            Map<Long, Long> storeReceNumMap = new HashMap<>(8);
            for (Map<String, Object> map : maps) {
                Object storeIdObj = map.get("storeId");
                Object receNumObj = map.get("receiveNum");

                Long storeId = null;
                Long receNum = null;

                // 处理 storeId
                if (storeIdObj instanceof Number) {
                    storeId = ((Number) storeIdObj).longValue();
                } else if (storeIdObj instanceof String) {
                    try {
                        storeId = Long.valueOf((String) storeIdObj);
                    } catch (NumberFormatException e) {
                        continue; // 跳过格式错误的数据
                    }
                }

                // 处理 receNum
                if (receNumObj instanceof Number) {
                    receNum = ((Number) receNumObj).longValue();
                } else if (receNumObj instanceof String) {
                    try {
                        receNum = Long.valueOf((String) receNumObj);
                    } catch (NumberFormatException e) {
                        receNum = 0L; // 默认值
                    }
                }

                if (storeId != null && receNum != null) {
                    storeReceNumMap.put(storeId, receNum);
                }
            }
            // 优化：一次性批量聚合查询所有门店的用券记录，替代N+1循环查询
            Map<Long, Map<String, Object>> storeAggregateMap = new HashMap<>(storeIds.size());
            if (CollectionUtil.isNotEmpty(storeIds)) {
                List<Map<String, Object>> aggregateList = userCouponRecordMapper.selectAggregateByStoresAndCoupon(
                        couponDataAnalysisReqVO.getCouponId(), storeIds);
                for (Map<String, Object> aggMap : aggregateList) {
                    Object storeIdObj = aggMap.get("storeId");
                    Long aggStoreId = null;
                    if (storeIdObj instanceof Number) {
                        aggStoreId = ((Number) storeIdObj).longValue();
                    } else if (storeIdObj instanceof String) {
                        try {
                            aggStoreId = Long.valueOf((String) storeIdObj);
                        } catch (NumberFormatException e) {
                            continue;
                        }
                    }
                    if (aggStoreId != null) {
                        storeAggregateMap.put(aggStoreId, aggMap);
                    }
                }
            }
            for (CouponStoreDO storeInfo : storeList) {
                CouponDataAnalysisRespVO result = CouponDataAnalysisRespVO.initVO();
                result.setStoreId(storeInfo.getStoreId());
                result.setStoreName(storeInfo.getStoreName());

                // 从批量聚合结果中获取数据，替代单次buildData调用
                Map<String, Object> aggData = storeAggregateMap.get(storeInfo.getStoreId());
                if (aggData != null) {
                    int orderNum = ((Number) aggData.get("orderNum")).intValue();
                    BigDecimal totalAmount = new BigDecimal(aggData.get("turnover").toString());
                    BigDecimal couponAmount = new BigDecimal(aggData.get("offerTotal").toString());
                    int itemNum = ((Number) aggData.get("itemNum")).intValue();

                    result.setUsage(BigDecimal.valueOf(orderNum));
                    result.setTurnover(totalAmount);
                    result.setOfferTotal(couponAmount);
                    result.setOrderNum(orderNum);
                    result.setItemNum(itemNum);

                    // 费效比 = 优惠金额 * 100 / 成交额
                    if (totalAmount.compareTo(BigDecimal.ZERO) != 0) {
                        BigDecimal cost = couponAmount.multiply(new BigDecimal("100"))
                                .divide(totalAmount, 2, RoundingMode.HALF_UP);
                        result.setCost(cost);
                    }

                    // 用券笔单价 = 成交额 / 付款单数
                    if (orderNum > 0) {
                        BigDecimal singlePrice = totalAmount.divide(new BigDecimal(orderNum), 2, RoundingMode.HALF_UP);
                        result.setSinglePrice(singlePrice);
                    }
                }

                if(storeReceNumMap.containsKey(storeInfo.getStoreId())){
                    result.setReceiveNum(storeReceNumMap.get(storeInfo.getStoreId()));
                }
                resultList.add(result);

            }
            pageResult.setList(resultList);
            pageResult.setTotal((long) stores.size());
        }
        return pageResult;
    }

    @Override
    public void couponDataDownload(CouponDataDownloadReqVO couponDataDownloadReqVO) {
        String fileName = "优惠券数据汇总--" + couponDataDownloadReqVO.getCouponId();
        Integer type = couponDataDownloadReqVO.getType();
        if(Objects.equals(type, 0)){
            excelActionService.exportAsyncExcel(CouponDataExportExcel.class, param -> this.getDownloadDataByDay(couponDataDownloadReqVO), fileName);
        }else {
            excelActionService.exportAsyncExcel(CouponDataExportExcel.class, param -> this.getDownloadDataByMonth(couponDataDownloadReqVO), fileName);
        }
    }


    @DS(DsNameConstants.SHARDING)
    public List<GoodCouponDateRespVO> getDownloadDataByDay(CouponDataDownloadReqVO couponDataDownloadReqVO) {
        // 查询原始按天按门店的数据（可能不包含某些日期或门店的组合）
        List<GoodCouponDateRespVO> rawList = userCouponRecordMapper.getDataByDay(couponDataDownloadReqVO);

        // 准备日期列表（按天，从开始日期到结束日期，包含结束）
        LocalDate startDate = couponDataDownloadReqVO.getStartTime().toLocalDate();
        LocalDate endDate = couponDataDownloadReqVO.getEndTime().toLocalDate();
        List<String> dateList = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            dateList.add(d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        // 构建 (storeId, date) -> record 映射，便于快速查找
        Map<String, GoodCouponDateRespVO> recordMap = new HashMap<>();
        Set<Long> storeIdSet = new LinkedHashSet<>();
        for (GoodCouponDateRespVO vo : rawList) {
            String key = vo.getCreateDate() + "__" + vo.getStoreId();
            recordMap.put(key, vo);
            storeIdSet.add(vo.getStoreId());
        }

        // 查询每个门店每天的领取数（receiveNum）
        QueryWrapperX<UserCouponDO> userWrapper = new QueryWrapperX<>();
        userWrapper.eq("coupon_id", couponDataDownloadReqVO.getCouponId());
        userWrapper.between("coupon_create_time", couponDataDownloadReqVO.getStartTime(), couponDataDownloadReqVO.getEndTime());
        userWrapper.groupBy("store_id,DATE(coupon_create_time)");
        userWrapper.select("store_id as storeId,DATE(coupon_create_time) as createDate,count(id) as receiveNum");
        List<Map<String, Object>> receiveMaps = userCouponMapper.selectMaps(userWrapper);
        Map<String, Long> storeDayReceNumMap = new HashMap<>(16);
        for (Map<String, Object> map : receiveMaps) {
            Object storeIdObj = map.get("storeId");
            Object createDateObj = map.get("createDate");
            Object receNumObj = map.get("receiveNum");

            Long storeId = null;
            Long receNum = null;
            if (storeIdObj instanceof Number) {
                storeId = ((Number) storeIdObj).longValue();
            } else if (storeIdObj instanceof String) {
                try {
                    storeId = Long.valueOf((String) storeIdObj);
                } catch (NumberFormatException ignored) {
                }
            }
            if (receNumObj instanceof Number) {
                receNum = ((Number) receNumObj).longValue();
            } else if (receNumObj instanceof String) {
                try {
                    receNum = Long.valueOf((String) receNumObj);
                } catch (NumberFormatException ignored) {
                    receNum = 0L;
                }
            }
            String createDate = createDateObj == null ? null : String.valueOf(createDateObj);
            if (storeId != null && createDate != null && receNum != null) {
                storeIdSet.add(storeId);
                storeDayReceNumMap.put(createDate + "__" + storeId, receNum);
            }
        }

        // 如果没有任何数据，返回所有日期对应一个空行（保持兼容之前的行为）
        if (CollectionUtil.isEmpty(rawList) && CollectionUtil.isEmpty(storeIdSet)) {
            List<GoodCouponDateRespVO> emptyResult = new ArrayList<>();
            for (String dateStr : dateList) {
                GoodCouponDateRespVO result = new GoodCouponDateRespVO();
                result.setStoreId(0L);
                result.setStoreName("无");
                result.setCreateDate(dateStr);
                result.setReceiveNum(0L);
                result.setOrderNum(0);
                result.setUseNum(0);
                result.setTurnover(BigDecimal.ZERO);
                result.setOfferTotal(BigDecimal.ZERO);
                result.setCost(BigDecimal.ZERO);
                result.setSinglePrice(BigDecimal.ZERO);
                result.setItemNum(0);
                result.setOldCustom(0);
                result.setNewCustom(0);
                result.setUsedRate(BigDecimal.ZERO);
                result.setCouponSource(0);
                emptyResult.add(result);
            }
            return emptyResult;
        }

        // 获取门店名称映射（包含消费或领取出现过的门店）
        List<Long> storeIds = storeIdSet.stream().toList();
        Map<Long, String> storeNameMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(storeIds)) {
            CommonResult<List<StoreInfoDTO>> storesByStoreIds = storeApi.getStoresByStoreIds(storeIds);
            List<StoreInfoDTO> storeInfoDTOS = storesByStoreIds.getData();
            if (CollectionUtil.isNotEmpty(storeInfoDTOS)) {
                storeNameMap = storeInfoDTOS.stream().collect(Collectors.toMap(StoreInfoDTO::getStoreId, StoreInfoDTO::getStoreName));
            }
        }

        // 按日期为外层、门店为内层，构建完整输出列表（天 门店 领取数量 消费）
        List<GoodCouponDateRespVO> resultList = new ArrayList<>();
        for (String dateStr : dateList) {
            for (Long storeId : storeIds) {
                String key = dateStr + "__" + storeId;
                GoodCouponDateRespVO record = recordMap.get(key);
                if (record == null) {
                    record = new GoodCouponDateRespVO();
                    record.setStoreId(storeId);
                    record.setCreateDate(dateStr);
                    record.setStoreName(storeNameMap.getOrDefault(storeId, "无"));
                    record.setReceiveNum(storeDayReceNumMap.getOrDefault(key, 0L));
                    record.setOrderNum(0);
                    record.setUseNum(0);
                    record.setTurnover(BigDecimal.ZERO);
                    record.setOfferTotal(BigDecimal.ZERO);
                    record.setCost(BigDecimal.ZERO);
                    record.setSinglePrice(BigDecimal.ZERO);
                    record.setItemNum(0);
                    record.setOldCustom(0);
                    record.setNewCustom(0);
                    record.setUsedRate(BigDecimal.ZERO);
                    record.setCouponSource(0);
                } else {
                    // 填充门店名与领取数（领取数按天按门店统计）
                    record.setStoreName(storeNameMap.getOrDefault(storeId, record.getStoreName()));
                    record.setReceiveNum(storeDayReceNumMap.getOrDefault(key, record.getReceiveNum() == null ? 0L : record.getReceiveNum()));
                    // 计算单价、费效比等，注意除以0的保护
                    BigDecimal totalAmount = record.getTurnover() == null ? BigDecimal.ZERO : record.getTurnover();
                    Integer orderNum = record.getOrderNum() == null ? 0 : record.getOrderNum();
                    if (orderNum == 0) {
                        record.setSinglePrice(BigDecimal.ZERO);
                    } else {
                        record.setSinglePrice(totalAmount.divide(new BigDecimal(orderNum), 2, RoundingMode.HALF_UP));
                    }
                    BigDecimal couponAmount = record.getOfferTotal() == null ? BigDecimal.ZERO : record.getOfferTotal();
                    if (totalAmount.compareTo(BigDecimal.ZERO) != 0) {
                        BigDecimal multiplied = couponAmount.multiply(new BigDecimal("100"));
                        record.setCost(multiplied.divide(totalAmount, 2, RoundingMode.HALF_UP));
                    } else {
                        record.setCost(BigDecimal.ZERO);
                    }
                }
                resultList.add(record);
            }
        }
        return resultList;
    }


    @DS(DsNameConstants.SHARDING)
    public List<GoodCouponDateRespVO> getDownloadDataByMonth(CouponDataDownloadReqVO couponDataDownloadReqVO) {
        List<GoodCouponDateRespVO> rawList = userCouponRecordMapper.getDataByMonth(couponDataDownloadReqVO);
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth startMonth = YearMonth.from(couponDataDownloadReqVO.getStartTime());
        YearMonth endMonth = YearMonth.from(couponDataDownloadReqVO.getEndTime());
        List<String> monthList = new ArrayList<>();
        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
            monthList.add(month.format(monthFormatter));
        }

        QueryWrapperX<UserCouponDO> userWrapper = new QueryWrapperX<>();
        userWrapper.eq("coupon_id", couponDataDownloadReqVO.getCouponId());
        userWrapper.between("coupon_create_time", couponDataDownloadReqVO.getStartTime(), couponDataDownloadReqVO.getEndTime());
        userWrapper.groupBy("store_id,DATE_FORMAT(coupon_create_time, '%Y-%m')");
        userWrapper.select("store_id as storeId,DATE_FORMAT(coupon_create_time, '%Y-%m') as createDate,count(id) as receiveNum");
        List<Map<String, Object>> receiveMaps = userCouponMapper.selectMaps(userWrapper);

        Map<String, GoodCouponDateRespVO> recordMap = new HashMap<>();
        Set<Long> storeIdSet = new LinkedHashSet<>();
        for (GoodCouponDateRespVO record : rawList) {
            String key = record.getCreateDate() + "__" + record.getStoreId();
            recordMap.put(key, record);
            if (record.getStoreId() != null) {
                storeIdSet.add(record.getStoreId());
            }
        }

        Map<String, Long> storeMonthReceiveMap = new HashMap<>();
        for (Map<String, Object> map : receiveMaps) {
            Object storeIdObj = map.get("storeId");
            Object createDateObj = map.get("createDate");
            Object receiveNumObj = map.get("receiveNum");

            Long storeId = null;
            Long receiveNum = 0L;
            if (storeIdObj instanceof Number) {
                storeId = ((Number) storeIdObj).longValue();
            } else if (storeIdObj instanceof String) {
                try {
                    storeId = Long.valueOf((String) storeIdObj);
                } catch (NumberFormatException ignored) {
                }
            }
            if (receiveNumObj instanceof Number) {
                receiveNum = ((Number) receiveNumObj).longValue();
            } else if (receiveNumObj instanceof String) {
                try {
                    receiveNum = Long.valueOf((String) receiveNumObj);
                } catch (NumberFormatException ignored) {
                    receiveNum = 0L;
                }
            }
            String createDate = createDateObj == null ? null : String.valueOf(createDateObj);
            if (storeId != null && createDate != null) {
                storeIdSet.add(storeId);
                storeMonthReceiveMap.put(createDate + "__" + storeId, receiveNum);
            }
        }

        if (CollectionUtil.isEmpty(rawList) && CollectionUtil.isEmpty(storeIdSet)) {
            List<GoodCouponDateRespVO> emptyResult = new ArrayList<>();
            for (String month : monthList) {
                GoodCouponDateRespVO result = new GoodCouponDateRespVO();
                result.setStoreId(0L);
                result.setStoreName("无");
                result.setCreateDate(month);
                result.setReceiveNum(0L);
                result.setOrderNum(0);
                result.setUseNum(0);
                result.setTurnover(BigDecimal.ZERO);
                result.setOfferTotal(BigDecimal.ZERO);
                result.setCost(BigDecimal.ZERO);
                result.setSinglePrice(BigDecimal.ZERO);
                result.setItemNum(0);
                result.setOldCustom(0);
                result.setNewCustom(0);
                result.setUsedRate(BigDecimal.ZERO);
                result.setCouponSource(0);
                emptyResult.add(result);
            }
            return emptyResult;
        }

        List<Long> storeIds = storeIdSet.stream().sorted().toList();
        Map<Long, String> storeNameMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(storeIds)) {
            CommonResult<List<StoreInfoDTO>> storesByStoreIds = storeApi.getStoresByStoreIds(storeIds);
            List<StoreInfoDTO> storeInfoDTOS = storesByStoreIds.getData();
            if (CollectionUtil.isNotEmpty(storeInfoDTOS)) {
                storeNameMap = storeInfoDTOS.stream().collect(Collectors.toMap(StoreInfoDTO::getStoreId, StoreInfoDTO::getStoreName));
            }
        }

        List<GoodCouponDateRespVO> resultList = new ArrayList<>();
        for (String month : monthList) {
            for (Long storeId : storeIds) {
                String key = month + "__" + storeId;
                GoodCouponDateRespVO record = recordMap.get(key);
                if (record == null) {
                    record = new GoodCouponDateRespVO();
                    record.setStoreId(storeId);
                    record.setCreateDate(month);
                    record.setOrderNum(0);
                    record.setUseNum(0);
                    record.setTurnover(BigDecimal.ZERO);
                    record.setOfferTotal(BigDecimal.ZERO);
                    record.setItemNum(0);
                    record.setOldCustom(0);
                    record.setNewCustom(0);
                    record.setUsedRate(BigDecimal.ZERO);
                    record.setCouponSource(0);
                }

                record.setStoreName(storeNameMap.getOrDefault(storeId, record.getStoreName() == null ? "无" : record.getStoreName()));
                record.setReceiveNum(storeMonthReceiveMap.getOrDefault(key, 0L));

                BigDecimal totalAmount = record.getTurnover() == null ? BigDecimal.ZERO : record.getTurnover();
                Integer orderNum = record.getOrderNum() == null ? 0 : record.getOrderNum();
                if (orderNum == 0) {
                    record.setSinglePrice(BigDecimal.ZERO);
                } else {
                    record.setSinglePrice(totalAmount.divide(new BigDecimal(orderNum), 2, RoundingMode.HALF_UP));
                }

                BigDecimal couponAmount = record.getOfferTotal() == null ? BigDecimal.ZERO : record.getOfferTotal();
                if (totalAmount.compareTo(BigDecimal.ZERO) != 0) {
                    record.setCost(couponAmount.multiply(new BigDecimal("100")).divide(totalAmount, 2, RoundingMode.HALF_UP));
                } else {
                    record.setCost(BigDecimal.ZERO);
                }
                resultList.add(record);
            }
        }
        return resultList;
    }

    @Override
    public void exportListByStoreAndOrg(CouponDataAnalysisReqVO couponDataAnalysisReqVO) {
        Page<CouponDataAnalysisRespVO> page = new Page<>(1, 5000);
        GoodCouponDO goodCouponDO = goodCouponMapper.selectById(couponDataAnalysisReqVO.getCouponId());
        String fileName = "优惠券按门店分析--" + goodCouponDO.getId();
        excelActionService.exportAsyncExcel(CouponDataSumExportExcel.class, param -> this.queryCouponData(couponDataAnalysisReqVO), fileName);
    }

    /**
     * 导出查询门店分析
     * @param couponDataAnalysisReqVO
     * @return
     */
    @Override
    public List<CouponDataAnalysisRespVO> queryCouponData(CouponDataAnalysisReqVO couponDataAnalysisReqVO) {

        // 获取选择组织ID
        Long orgId = couponDataAnalysisReqVO.getOrgId();
        Set<OrgRespDTO> allOrg = null;
        Long[] orgArr;
        if(ObjectUtil.isNotEmpty(orgId)){
            allOrg = getAllOrg(orgId);
            orgArr = allOrg.stream()
                    .map(OrgRespDTO::getId)
                    .toArray(Long[]::new);
        } else {
            orgArr = null;
        }

        List<CouponStoreAndOrgDO> storeInfoDTOList = null;
        // 优惠券是2 门店券 获取所有门店 通过优惠券门店关系表
        if (CouponISCommonEnum.PART.getValue().equals( couponDataAnalysisReqVO.getIsCommon())){
            storeInfoDTOList = couponStoreService.getStoresByCouponIdAndName(couponDataAnalysisReqVO.getCouponId(),couponDataAnalysisReqVO.getStoreName());
        }
        // 优惠券是1 通用 获取所有门店 门店info表
        if(CouponISCommonEnum.ALL.getValue().equals( couponDataAnalysisReqVO.getIsCommon())){
            CommonResult<List<StoreInfoDTO>> storesByName = storeInfoApi.getStoresByName(couponDataAnalysisReqVO.getStoreName());
            List<StoreInfoDTO> data = storesByName.getData();
            storeInfoDTOList = BeanUtils.toBean(data, CouponStoreAndOrgDO.class);
        }
        if(storeInfoDTOList == null){
            return new ArrayList<>();
        }/*else{
            // 过滤门店为登录人范围内门店
            storeInfoDTOList = storeInfoDTOList.stream().filter(ele -> storeIdList.contains(ele.getStoreId())).collect(Collectors.toList());
        }*/

        // 门店筛选 获取满足组织条件的门店

        List<CouponStoreAndOrgDO> stores;

        // 搜索的门店总数
        if(CollectionUtil.isNotEmpty(allOrg)){
            stores = storeInfoDTOList.stream().filter(ele -> ArrayUtil.contains(orgArr,ele.getOrgId())).collect(Collectors.toList());
        }else {
            stores = storeInfoDTOList;
        }
        // 统计优惠券已使用过的门店 按用券总成交额倒叙排列
        List<UserCouponStoreIdDO> usedStoreIdList = null;
        // 按照渠道获取用户优惠券使用情况
        QueryWrapperX<UserCouponStoreIdDO> userCouponRecordWrapper = new QueryWrapperX<>();
        userCouponRecordWrapper.select("""
                store_id,
                IFNULL(SUM(total_amount),0)   as turnover
                """);
        userCouponRecordWrapper.eq("coupon_id", couponDataAnalysisReqVO.getCouponId());
        userCouponRecordWrapper.between("create_time",couponDataAnalysisReqVO.getStartTime(),couponDataAnalysisReqVO.getEndTime());
        userCouponRecordWrapper.groupBy("store_id");
        userCouponRecordWrapper.orderByDesc("turnover");
        usedStoreIdList = userCouponStoreIdMapper.selectList(userCouponRecordWrapper);

        // 将storeInfoDTOList进行排序 storeInfoDTOList内已使用的门店排在前面 使用量降序 未使用的排在后面
        orderStoreInfoDTOList(stores,usedStoreIdList);
        List<CouponDataAnalysisRespVO> resultList = new ArrayList<>();
        // 获取分页门店
        if(CollectionUtil.isNotEmpty(stores)){

            List<Long> storeIds = stores.stream().map(CouponStoreDO::getStoreId).toList();
            QueryWrapperX<UserCouponDO> userWrapper = new QueryWrapperX<>();
            userWrapper.eq("coupon_id", couponDataAnalysisReqVO.getCouponId());
            userWrapper.in("store_id", storeIds);
            userWrapper.between("coupon_create_time",couponDataAnalysisReqVO.getStartTime(),couponDataAnalysisReqVO.getEndTime());
            userWrapper.groupBy("store_id");
            userWrapper.select("store_id as storeId,count(id) as receiveNum");
            List<Map<String, Object>> maps = userCouponMapper.selectMaps(userWrapper);
            Map<Long, Long> storeReceNumMap = new HashMap<>(8);
            for (Map<String, Object> map : maps) {
                Object storeIdObj = map.get("storeId");
                Object receNumObj = map.get("receiveNum");

                Long storeId = null;
                Long receNum = null;

                // 处理 storeId
                if (storeIdObj instanceof Number) {
                    storeId = ((Number) storeIdObj).longValue();
                } else if (storeIdObj instanceof String) {
                    try {
                        storeId = Long.valueOf((String) storeIdObj);
                    } catch (NumberFormatException e) {
                        continue; // 跳过格式错误的数据
                    }
                }

                // 处理 receNum
                if (receNumObj instanceof Number) {
                    receNum = ((Number) receNumObj).longValue();
                } else if (receNumObj instanceof String) {
                    try {
                        receNum = Long.valueOf((String) receNumObj);
                    } catch (NumberFormatException e) {
                        receNum = 0L; // 默认值
                    }
                }

                if (storeId != null && receNum != null) {
                    storeReceNumMap.put(storeId, receNum);
                }
            }

            for (CouponStoreDO storeInfo : stores) {
                // 领券总次数 暂时不设计
                CouponDataAnalysisRespVO result = CouponDataAnalysisRespVO.initVO();
                result.setStoreId(storeInfo.getStoreId());
                result.setStoreName(storeInfo.getStoreName());
                LambdaQueryWrapper<UserCouponRecordDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(UserCouponRecordDO::getCouponId, couponDataAnalysisReqVO.getCouponId());
                wrapper.eq(UserCouponRecordDO::getStoreId, storeInfo.getStoreId());
                wrapper.between(UserCouponRecordDO::getCreateTime,couponDataAnalysisReqVO.getStartTime(),couponDataAnalysisReqVO.getEndTime());
                buildData(result, wrapper);
                if(storeReceNumMap.containsKey(storeInfo.getStoreId())){
                    result.setReceiveNum(storeReceNumMap.get(storeInfo.getStoreId()));
                }
                resultList.add(result);

            }
        }
        return resultList;
    }

    private void orderStoreInfoDTOList(List<CouponStoreAndOrgDO> storeInfoDTOList, List<UserCouponStoreIdDO> usedStoreIdList) {
        // 1. 将usedStoreIdList转换为Map<storeId, 优先级>，优先级为元素在列表中的索引
        Map<Long, Integer> priorityMap = new HashMap<>();
        for (int i = 0; i < usedStoreIdList.size(); i++) {
            priorityMap.put(usedStoreIdList.get(i).getStoreId(), i);
        }

        // 2. 记录原始列表中每个元素的索引（用于保持非置顶元素的顺序）
        Map<CouponStoreAndOrgDO, Integer> originalIndexMap = new HashMap<>();
        for (int i = 0; i < storeInfoDTOList.size(); i++) {
            originalIndexMap.put(storeInfoDTOList.get(i), i);
        }

        // 3. 自定义排序逻辑
        storeInfoDTOList.sort((a, b) -> {
            boolean aInUsed = priorityMap.containsKey(a.getStoreId());
            boolean bInUsed = priorityMap.containsKey(b.getStoreId());

            // 情况1：a和b都在used列表中，按used列表中的顺序（索引升序）排列
            if (aInUsed && bInUsed) {
                return Integer.compare(priorityMap.get(a.getStoreId()),
                        priorityMap.get(b.getStoreId()));
            }
            // 情况2：a在used列表中，b不在，a排前面
            else if (aInUsed) {
                return -1;
            }
            // 情况3：b在used列表中，a不在，b排前面
            else if (bInUsed) {
                return 1;
            }
            // 情况4：a和b都不在used列表中，保持原顺序
            else {
                return originalIndexMap.get(a) - originalIndexMap.get(b);
            }
        });
    }

    @DS(DsNameConstants.SHARDING)
    @Override
    public PageResult<CouponDataAnalysisRespVO> getDataBySource(Long id) {
/*

        // 获取登陆人可见所有门店
        Long userId = WebFrameworkUtils.getLoginUserId();
        Set<Long> storeIdList = storeApi.getAllStoreIdByUser(userId).getCheckedData();
*/

        // 按照渠道获取用户优惠券领取量
        List<CouponDataAnalysisRespVO> result = new ArrayList<>();
        QueryWrapperX<UserCouponDO> userCouponWrapper = new QueryWrapperX<>();
        userCouponWrapper.select("""
                IFNULL(coupon_source,999)   as couponSource,
                count(1) as receivedNum
                """);
        userCouponWrapper.eq("coupon_id", id);
//        userCouponWrapper.in("store_id", storeIdList);
        userCouponWrapper.groupBy("coupon_source");
        List<UserCouponDO> userCoupons = userCouponMapper.selectList(userCouponWrapper);
        Map<Integer, Integer> userCouponMap = userCoupons.stream().collect(Collectors.toMap(UserCouponDO::getCouponSource, UserCouponDO::getReceivedNum));

        // 按照渠道获取用户优惠券使用情况
        QueryWrapperX<UserCouponDataAnalysisBySourceDO> userCouponRecordWrapper = new QueryWrapperX<>();
        userCouponRecordWrapper.select("""
                IFNULL(SUM(total_amount),0)   as turnover,
                IFNULL(SUM(coupon_amount),0) as offerTotal,
                IFNULL(COUNT(id),0) as orderNum,
                IFNULL(SUM(item_num),0) as itemNum,
                IFNULL(coupon_source,999)   as couponSource
                """);
        userCouponRecordWrapper.eq("coupon_id", id);
//        userCouponRecordWrapper.in("store_id", storeIdList);
        userCouponRecordWrapper.orderByDesc("turnover");
        userCouponRecordWrapper.groupBy("coupon_source");
        List<UserCouponDataAnalysisBySourceDO> userCouponRecordDOS = userCouponRecordDataAnanlysisBySourceMapper.selectList(userCouponRecordWrapper);
        for (UserCouponDataAnalysisBySourceDO userCouponRecordDO : userCouponRecordDOS) {
            CouponDataAnalysisRespVO couponDataAnalysisRespVO = CouponDataAnalysisRespVO.initVO();
            // 渠道名称
            couponDataAnalysisRespVO.setCouponSource(userCouponRecordDO.getCouponSource());
            // 优惠券总领取
            couponDataAnalysisRespVO.setReceived(BigDecimal.valueOf(userCouponMap.get(userCouponRecordDO.getCouponSource())==null?0:userCouponMap.get(userCouponRecordDO.getCouponSource())==null?0:userCouponMap.get(userCouponRecordDO.getCouponSource())));
            // 移除 剩下渠道未使用的
            userCouponMap.remove(userCouponRecordDO.getCouponSource());
            // 优惠券使用数量
            couponDataAnalysisRespVO.setUsage(BigDecimal.valueOf(userCouponRecordDO.getOrderNum()==null?0:userCouponRecordDO.getOrderNum()));
            // 用券总成交额
            couponDataAnalysisRespVO.setTurnover(userCouponRecordDO.getTurnover());
            // 优惠总金额
            couponDataAnalysisRespVO.setOfferTotal(userCouponRecordDO.getOfferTotal());
            // 付款单数
            couponDataAnalysisRespVO.setOrderNum(userCouponRecordDO.getOrderNum());
            // 商品件数
            couponDataAnalysisRespVO.setItemNum(userCouponRecordDO.getItemNum());
            // 费效比
            BigDecimal couponAmount = userCouponRecordDO.getOfferTotal();
            BigDecimal multiplied = couponAmount.multiply(new BigDecimal("100"));
            // 单价
            BigDecimal totalAmount = userCouponRecordDO.getTurnover();
            // 用券笔单价
            Integer orderNum = userCouponRecordDO.getOrderNum();
            BigDecimal singelPrice = totalAmount.divide(new BigDecimal(orderNum), 2, RoundingMode.HALF_UP);
            couponDataAnalysisRespVO.setSinglePrice(singelPrice);
            // 费效比
            if (totalAmount.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal cost = multiplied.divide(totalAmount, 2, RoundingMode.HALF_UP);
                couponDataAnalysisRespVO.setCost(cost);
            } else {
                couponDataAnalysisRespVO.setCost(BigDecimal.ZERO);
            }
            // 使用率
            // 用券率 查询user_coupon 分表聚合统计
            BigDecimal receivedNum = couponDataAnalysisRespVO.getReceived();
            if (ObjectUtil.isNotEmpty(receivedNum)) {
                if (receivedNum.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal usedRate = new BigDecimal(orderNum).multiply(new BigDecimal("100"))
                            .divide(receivedNum, 2, RoundingMode.HALF_UP);
                    couponDataAnalysisRespVO.setUsedRate(usedRate);
                } else {
                    couponDataAnalysisRespVO.setUsedRate(BigDecimal.ZERO);
                }
            }
            result.add(couponDataAnalysisRespVO);
        }

        // 有渠道领取 但是该渠道的优惠券不存在使用记录
        if(!userCouponMap.isEmpty()){
            userCouponMap.forEach((key, value) -> {
                CouponDataAnalysisRespVO couponDataAnalysisRespVO = CouponDataAnalysisRespVO.initVO();
                couponDataAnalysisRespVO.setCouponSource(key);
                couponDataAnalysisRespVO.setReceived(BigDecimal.valueOf(value));
                result.add(couponDataAnalysisRespVO);
            });
        }
        PageResult<CouponDataAnalysisRespVO> pageResult = new PageResult<>();

        pageResult.setTotal((long) result.size());
        pageResult.setList(result);

        return pageResult;
    }

    @Override
    public void couponDataDownloadBySource(CouponDataDownloadReqVO couponDataDownloadReqVO) {
        Page<CouponDataAnalysisRespVO> page = new Page<>(1, 5000);
        GoodCouponDO goodCouponDO = goodCouponMapper.selectById(couponDataDownloadReqVO.getCouponId());
        String fileName = "优惠券按渠道分析--" + goodCouponDO.getId();
        List<Integer> integers = List.of(0, 1);
        if(integers.contains(couponDataDownloadReqVO.getType())){
            excelActionService.exportAsyncExcel(CouponDataSourceExportExcel.class, param -> this.couponDataBySource(couponDataDownloadReqVO), fileName);
        }else {
            excelActionService.exportAsyncExcel(CouponDataSourceSumExportExcel.class, param -> this.couponDataBySourceSum(couponDataDownloadReqVO), fileName);
        }

    }



    //@Override
    public List<CouponDataSourceExportExcel> couponDataBySource(CouponDataDownloadReqVO couponDataDownloadReqVO) {
        boolean byDay = Objects.equals(couponDataDownloadReqVO.getType(), 0);
        List<GoodCouponDateRespVO> rawList = byDay
                ? userCouponRecordMapper.getDataByDaySource(couponDataDownloadReqVO)
                : userCouponRecordMapper.getDataByMonthSource(couponDataDownloadReqVO);
        List<String> timeList = buildSourceTimeList(couponDataDownloadReqVO, byDay);

        QueryWrapperX<UserCouponDO> userCouponWrapper = new QueryWrapperX<>();
        userCouponWrapper.eq("coupon_id", couponDataDownloadReqVO.getCouponId());
        userCouponWrapper.between("coupon_create_time", couponDataDownloadReqVO.getStartTime(), couponDataDownloadReqVO.getEndTime());
        if (byDay) {
            userCouponWrapper.groupBy("DATE(coupon_create_time),IFNULL(coupon_source,999)");
            userCouponWrapper.select("IFNULL(coupon_source,999) as couponSource, DATE(coupon_create_time) as createDate, count(1) as receivedNum");
        } else {
            userCouponWrapper.groupBy("DATE_FORMAT(coupon_create_time, '%Y-%m'),IFNULL(coupon_source,999)");
            userCouponWrapper.select("IFNULL(coupon_source,999) as couponSource, DATE_FORMAT(coupon_create_time, '%Y-%m') as createDate, count(1) as receivedNum");
        }
        List<Map<String, Object>> receiveMaps = userCouponMapper.selectMaps(userCouponWrapper);

        Map<String, GoodCouponDateRespVO> recordMap = new HashMap<>();
        Set<Integer> sourceSet = new LinkedHashSet<>();
        for (GoodCouponDateRespVO record : rawList) {
            Integer couponSource = record.getCouponSource() == null ? 999 : record.getCouponSource();
            record.setCouponSource(couponSource);
            recordMap.put(record.getCreateDate() + "__" + couponSource, record);
            sourceSet.add(couponSource);
        }

        Map<String, Long> sourceReceiveMap = new HashMap<>();
        for (Map<String, Object> map : receiveMaps) {
            Object couponSourceObj = map.get("couponSource");
            Object createDateObj = map.get("createDate");
            Object receivedNumObj = map.get("receivedNum");

            Integer couponSource = 999;
            if (couponSourceObj instanceof Number) {
                couponSource = ((Number) couponSourceObj).intValue();
            } else if (couponSourceObj instanceof String) {
                try {
                    couponSource = Integer.valueOf((String) couponSourceObj);
                } catch (NumberFormatException ignored) {
                    couponSource = 999;
                }
            }

            Long receivedNum = 0L;
            if (receivedNumObj instanceof Number) {
                receivedNum = ((Number) receivedNumObj).longValue();
            } else if (receivedNumObj instanceof String) {
                try {
                    receivedNum = Long.valueOf((String) receivedNumObj);
                } catch (NumberFormatException ignored) {
                    receivedNum = 0L;
                }
            }

            String createDate = createDateObj == null ? null : String.valueOf(createDateObj);
            if (createDate != null) {
                sourceSet.add(couponSource);
                sourceReceiveMap.put(createDate + "__" + couponSource, receivedNum);
            }
        }

        if (CollectionUtil.isEmpty(rawList) && CollectionUtil.isEmpty(sourceSet)) {
            List<CouponDataSourceExportExcel> emptyResult = new ArrayList<>();
            for (String time : timeList) {
                CouponDataSourceExportExcel couponDataSourceExportExcel = new CouponDataSourceExportExcel();
                couponDataSourceExportExcel.setCouponSource(0);
                couponDataSourceExportExcel.setCreateDate(time);
                couponDataSourceExportExcel.setReceived(BigDecimal.ZERO);
                couponDataSourceExportExcel.setUsedRate(BigDecimal.ZERO);
                couponDataSourceExportExcel.setSinglePrice(BigDecimal.ZERO);
                couponDataSourceExportExcel.setCost(BigDecimal.ZERO);
                couponDataSourceExportExcel.setOrderNum(0);
                couponDataSourceExportExcel.setItemNum(0);
                couponDataSourceExportExcel.setTurnover(BigDecimal.ZERO);
                couponDataSourceExportExcel.setOfferTotal(BigDecimal.ZERO);
                couponDataSourceExportExcel.setUseNum(0);
                emptyResult.add(couponDataSourceExportExcel);
            }
            return emptyResult;
        }

        List<Integer> sourceList = sourceSet.stream().sorted().toList();
        List<CouponDataSourceExportExcel> result = new ArrayList<>();
        for (String time : timeList) {
            for (Integer couponSource : sourceList) {
                String key = time + "__" + couponSource;
                GoodCouponDateRespVO record = recordMap.get(key);
                if (record == null) {
                    record = new GoodCouponDateRespVO();
                    record.setCreateDate(time);
                    record.setCouponSource(couponSource);
                    record.setUseNum(0);
                    record.setOrderNum(0);
                    record.setTurnover(BigDecimal.ZERO);
                    record.setOfferTotal(BigDecimal.ZERO);
                    record.setItemNum(0);
                }

                CouponDataSourceExportExcel couponDataAnalysisRespVO = new CouponDataSourceExportExcel();
                couponDataAnalysisRespVO.setCreateDate(time);
                couponDataAnalysisRespVO.setCouponSource(couponSource);
                BigDecimal received = BigDecimal.valueOf(sourceReceiveMap.getOrDefault(key, 0L));
                couponDataAnalysisRespVO.setReceived(received);
                couponDataAnalysisRespVO.setUseNum(record.getUseNum() == null ? 0 : record.getUseNum());
                BigDecimal totalAmount = record.getTurnover() == null ? BigDecimal.ZERO : record.getTurnover();
                couponDataAnalysisRespVO.setTurnover(totalAmount);
                BigDecimal couponAmount = record.getOfferTotal() == null ? BigDecimal.ZERO : record.getOfferTotal();
                couponDataAnalysisRespVO.setOfferTotal(couponAmount);
                Integer orderNum = record.getOrderNum() == null ? 0 : record.getOrderNum();
                couponDataAnalysisRespVO.setOrderNum(orderNum);
                couponDataAnalysisRespVO.setItemNum(record.getItemNum() == null ? 0 : record.getItemNum());

                if (orderNum == 0) {
                    couponDataAnalysisRespVO.setSinglePrice(BigDecimal.ZERO);
                } else {
                    couponDataAnalysisRespVO.setSinglePrice(totalAmount.divide(new BigDecimal(orderNum), 2, RoundingMode.HALF_UP));
                }

                if (totalAmount.compareTo(BigDecimal.ZERO) != 0) {
                    couponDataAnalysisRespVO.setCost(couponAmount.multiply(new BigDecimal("100")).divide(totalAmount, 2, RoundingMode.HALF_UP));
                } else {
                    couponDataAnalysisRespVO.setCost(BigDecimal.ZERO);
                }

                if (received.compareTo(BigDecimal.ZERO) != 0) {
                    couponDataAnalysisRespVO.setUsedRate(new BigDecimal(orderNum).multiply(new BigDecimal("100")).divide(received, 2, RoundingMode.HALF_UP));
                } else {
                    couponDataAnalysisRespVO.setUsedRate(BigDecimal.ZERO);
                }
                result.add(couponDataAnalysisRespVO);
            }
        }
        return result;
    }

    private List<String> buildSourceTimeList(CouponDataDownloadReqVO couponDataDownloadReqVO, boolean byDay) {
        List<String> timeList = new ArrayList<>();
        if (byDay) {
            LocalDate startDate = couponDataDownloadReqVO.getStartTime().toLocalDate();
            LocalDate endDate = couponDataDownloadReqVO.getEndTime().toLocalDate();
            DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                timeList.add(date.format(dayFormatter));
            }
            return timeList;
        }

        YearMonth startMonth = YearMonth.from(couponDataDownloadReqVO.getStartTime());
        YearMonth endMonth = YearMonth.from(couponDataDownloadReqVO.getEndTime());
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
            timeList.add(month.format(monthFormatter));
        }
        return timeList;
    }


    //@Override
    public List<CouponDataSourceSumExportExcel> couponDataBySourceSum(CouponDataDownloadReqVO couponDataDownloadReqVO) {

        List<GoodCouponDateRespVO> list = new ArrayList<>();
        list = userCouponRecordMapper.getDataBySourceSum(couponDataDownloadReqVO);

        QueryWrapperX<UserCouponDO> userCouponWrapper = new QueryWrapperX<>();
        userCouponWrapper.select("""
                IFNULL(coupon_source,999)   as couponSource,
                count(1) as receivedNum
                """);
        userCouponWrapper.eq("coupon_id", couponDataDownloadReqVO.getCouponId());
        userCouponWrapper.between("coupon_create_time",couponDataDownloadReqVO.getStartTime(),couponDataDownloadReqVO.getEndTime());
        userCouponWrapper.groupBy("coupon_source");
        List<UserCouponDO> userCoupons = userCouponMapper.selectList(userCouponWrapper);
        Map<Integer, Integer> userCouponMap = userCoupons.stream().collect(Collectors.toMap(UserCouponDO::getCouponSource, UserCouponDO::getReceivedNum));


        List<CouponDataSourceSumExportExcel> result = new ArrayList<>();
        for (GoodCouponDateRespVO userCouponRecordDO : list) {
            CouponDataSourceSumExportExcel couponDataAnalysisRespVO = new CouponDataSourceSumExportExcel();
            // 渠道名称
            couponDataAnalysisRespVO.setCouponSource(userCouponRecordDO.getCouponSource());
            // 优惠券总领取
            couponDataAnalysisRespVO.setReceived(BigDecimal.valueOf(userCouponMap.get(userCouponRecordDO.getCouponSource())==null?0:userCouponMap.get(userCouponRecordDO.getCouponSource())==null?0:userCouponMap.get(userCouponRecordDO.getCouponSource())));
            // 移除 剩下渠道未使用的
            userCouponMap.remove(userCouponRecordDO.getCouponSource());
            // 优惠券使用数量
            couponDataAnalysisRespVO.setUseNum(userCouponRecordDO.getOrderNum());
            // 用券总成交额
            couponDataAnalysisRespVO.setTurnover(userCouponRecordDO.getTurnover());
            // 优惠总金额
            couponDataAnalysisRespVO.setOfferTotal(userCouponRecordDO.getOfferTotal());
            // 付款单数
            couponDataAnalysisRespVO.setOrderNum(userCouponRecordDO.getOrderNum());
            // 商品件数
            couponDataAnalysisRespVO.setItemNum(userCouponRecordDO.getItemNum());
            // 费效比
            BigDecimal couponAmount = userCouponRecordDO.getOfferTotal();
            BigDecimal multiplied = couponAmount.multiply(new BigDecimal("100"));
            // 单价
            BigDecimal totalAmount = userCouponRecordDO.getTurnover();
            // 用券笔单价
            Integer orderNum = userCouponRecordDO.getOrderNum();
            BigDecimal singelPrice = totalAmount.divide(new BigDecimal(orderNum), 2, RoundingMode.HALF_UP);
            couponDataAnalysisRespVO.setSinglePrice(singelPrice);
            // 费效比
            if (totalAmount.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal cost = multiplied.divide(totalAmount, 2, RoundingMode.HALF_UP);
                couponDataAnalysisRespVO.setCost(cost);
            } else {
                couponDataAnalysisRespVO.setCost(BigDecimal.ZERO);
            }
            // 使用率
            // 用券率 查询user_coupon 分表聚合统计
            BigDecimal receivedNum = couponDataAnalysisRespVO.getReceived();
            if (ObjectUtil.isNotEmpty(receivedNum)) {
                if (receivedNum.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal usedRate = new BigDecimal(orderNum).multiply(new BigDecimal("100"))
                            .divide(receivedNum, 2, RoundingMode.HALF_UP);
                    couponDataAnalysisRespVO.setUsedRate(usedRate);
                } else {
                    couponDataAnalysisRespVO.setUsedRate(BigDecimal.ZERO);
                }
            }
            result.add(couponDataAnalysisRespVO);
        }
        return result;
    }

    private Set<OrgRespDTO> getAllOrg(Long orgId){
        CommonResult<Set<OrgRespDTO>> childOrgList = orgApi.getChildOrgList(orgId);
        return childOrgList.getData();
    }

    private void buildData(CouponDataAnalysisRespVO result, LambdaQueryWrapper<UserCouponRecordDO> wrapper) {
        List<UserCouponRecordDO> userCouponRecords = userCouponRecordMapper.selectList(wrapper);

        if (CollectionUtil.isEmpty(userCouponRecords)) {
            return;
        }
        result.setUsage(BigDecimal.valueOf(userCouponRecords.size()));

        // 用券总成交额
        BigDecimal totalAmount = userCouponRecords.stream().map(UserCouponRecordDO::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        result.setTurnover(totalAmount);

        // 优惠总金额
        BigDecimal couponAmount = userCouponRecords.stream().map(UserCouponRecordDO::getCouponAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        result.setOfferTotal(couponAmount);

        BigDecimal multiplied = couponAmount.multiply(new BigDecimal("100"));
        // 费效比
        if (totalAmount.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal cost = multiplied.divide(totalAmount, 2, RoundingMode.HALF_UP);
            result.setCost(cost);
        } else {
            result.setCost(BigDecimal.ZERO);
        }

        // 付款单数  暂时与优惠券 用券总次数一致  后期有可能随业务要求改变
        int orderNum = userCouponRecords.size();
        result.setOrderNum(orderNum);

        // 用券率 查询user_coupon 分表聚合统计
        BigDecimal receivedNum = result.getReceived();
        if (ObjectUtil.isNotEmpty(receivedNum)) {
            if (receivedNum.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal usedRate = new BigDecimal(orderNum).multiply(new BigDecimal("100"))
                        .divide(receivedNum, 2, RoundingMode.HALF_UP);
                result.setUsedRate(usedRate);
            } else {
                result.setUsedRate(BigDecimal.ZERO);
            }
        }

        // 用券笔单价
        BigDecimal singelPrice = totalAmount.divide(new BigDecimal(orderNum), 2, RoundingMode.HALF_UP);
        result.setSinglePrice(singelPrice);

        // 购买商品件数
        int commodityNum = userCouponRecords.stream().mapToInt(UserCouponRecordDO::getItemNum).sum();
        result.setItemNum(commodityNum);

    }

    private void buildReceived(CouponDataAnalysisRespVO result, LambdaQueryWrapper<UserCouponDO> userWrapper) {
        Long received = userCouponMapper.selectCount(userWrapper);
        result.setReceived(BigDecimal.valueOf(received));
    }
}
