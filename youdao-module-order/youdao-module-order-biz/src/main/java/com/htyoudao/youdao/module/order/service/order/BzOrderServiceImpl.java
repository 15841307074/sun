package com.htyoudao.youdao.module.order.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.excel.pojo.SearchAfterPage;
import com.htyoudao.youdao.framework.mq.rabbitmq.enums.RabbitMQConstant;
import com.htyoudao.youdao.framework.mq.rabbitmq.service.RabbitMQService;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityConversionDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.MaterialListRespDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.StockChangeVO;
import com.htyoudao.youdao.module.commodity.api.VO.CommoditySplitMaterialReqVO;
import com.htyoudao.youdao.module.commodity.api.VO.MaterialDataVo;
import com.htyoudao.youdao.module.commodity.enums.IsSingleEnum;
import com.htyoudao.youdao.module.commodity.enums.StockChangeEnum;
import com.htyoudao.youdao.module.errand.api.runner.ErrandRunnerApi;
import com.htyoudao.youdao.module.errand.api.runner.dto.ErrandRunnerDTO;
import com.htyoudao.youdao.module.member.api.wx.VO.AppletNoticePushVO;
import com.htyoudao.youdao.module.member.api.wx.WxActionApi;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.dto.MemberOrderDTO;
import com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.order.api.order.dto.BzOrderDTO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.*;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.commodity.CommodityCondimentsVO;
import com.htyoudao.youdao.module.order.controller.app.order.DTO.BzOrderPrintInfoDTO;
import com.htyoudao.youdao.module.order.controller.app.order.DTO.NotifyOrderDTO;
import com.htyoudao.youdao.module.order.controller.app.order.DTO.OrderCountByOrderTypeDTO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.ErrandOrderHallPageRespVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.ErrandOrderHallReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.ErrandOrderHallRespVO;
import com.htyoudao.youdao.module.order.controller.app.pay.VO.OrderPayReqVO;
import com.htyoudao.youdao.module.order.controller.app.pay.VO.XingYiPayReqVO;
import com.htyoudao.youdao.module.order.core.calc.v1.VO.SettlementReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitResVO;
import com.htyoudao.youdao.module.order.core.calc.v1.CalculateCacheService;
import com.htyoudao.youdao.module.order.core.calc.DTO.ActivityDiscountDTO;
import com.htyoudao.youdao.module.order.core.calc.v1.DTO.CalculateCacheDataDTO;
import com.htyoudao.youdao.module.order.core.calc.v1.PriceCalculatorService;
import com.htyoudao.youdao.module.order.core.calc.SeckillCalculatorService;
import com.htyoudao.youdao.module.order.core.calc.v2.CalculateCacheV2Service;
import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.calc.v2.PriceCalculatorV2Service;
import com.htyoudao.youdao.module.order.core.calc.v2.VO.SettlementReqV2VO;
import com.htyoudao.youdao.module.order.core.pay.XingYiPayService;
import com.htyoudao.youdao.module.order.core.submit.DTO.ActivityNjnzInfoDTO;
import com.htyoudao.youdao.module.order.core.submit.factory.OrderSubmitStrategyFactory;
import com.htyoudao.youdao.module.order.core.submit.strategy.IOrderSubmitStrategy;
import com.htyoudao.youdao.module.order.dal.DTO.OrderDetailDTO;
import com.htyoudao.youdao.module.order.dal.dataobject.collection.NotNullHashSet;
import com.htyoudao.youdao.module.order.dal.dataobject.order.*;
import com.htyoudao.youdao.module.order.dal.es.BzOrderDocument;
import com.htyoudao.youdao.module.order.dal.es.BzOrderPointsDocument;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderMapper;
import com.htyoudao.youdao.module.order.enums.*;
import com.htyoudao.youdao.module.order.util.*;
import com.htyoudao.youdao.module.order.service.pay.BzOrderPayService;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityJDCouponPackageRespSaveVO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityJDCouponRespSaveVO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityJDFullRespVO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityJDRespVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.UserCouponApi;
import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.UsedCouponReqVO;
import com.htyoudao.youdao.module.system.api.complaint.ComplaintApi;
import com.htyoudao.youdao.module.system.api.org.OrgApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.htyoudao.youdao.framework.common.constants.RedisKeyConstants.*;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.order.dal.redis.RedisKeyConstants.*;

/**
 * 订单 Service 实现类
 *
 * @author 0090
 */
@RefreshScope
@Service
@DS(DsNameConstants.SHARDING)
@Slf4j
public class BzOrderServiceImpl extends ServiceImpl<BzOrderMapper, BzOrderDO> implements BzOrderService {

    private static final int ERRAND_RUNNER_FIRST_AUDIT_APPROVED = 1;
    private static final int ERRAND_RUNNER_BAN_STATUS_BANNED = 1;
    private static final int ERRAND_HALL_DEFAULT_PAGE_SIZE = 50;
    private static final int ERRAND_HALL_MAX_PAGE_SIZE = 100;
    private static final long ERRAND_HALL_ADDRESS_VISIBLE_CACHE_SECONDS = 30L;
    private static final List<String> ERRAND_ORDER_LIST_SOURCE_INCLUDES = Arrays.asList(
            "orderSn", "memberAvatar", "appointmentTime", "orderState",
            "receiverAreaInfo", "receiverAddress", "orderRemark",
            "errandRewardAmount", "errandStoreSubsidyAmount",
            "deliveryName", "deliveryPhone", "storePhone",
            "takeAwayTel", "receiverMobile", "tableWareNum"
    );
    private static final String ERRAND_ACCEPT_LOCK_KEY = "order:errand:accept:lock:%s";
    private static final String ERRAND_HALL_ADDRESS_VISIBLE_CACHE_KEY = "order:errand:hall:address:visible:%s";
    private static final long ERRAND_ACCEPT_LOCK_SECONDS = 10L;

    @Value("${kafka.producer.orderNotifyTopic}")
    private String orderNotifyTopic;

    @DubboReference
    private OrgApi orgApi;
    @DubboReference
    private ComplaintApi compleplantApi;
    @DubboReference
    private WxMemberApi wxMemberApi;
    @DubboReference
    private UserCouponApi userCouponApi;
    @DubboReference
    private WxActionApi wxActionApi;
    @DubboReference
    private StoreApi storeApi;
    @DubboReference
    private CommodityApi commodityApi;
    @DubboReference
    private ErrandRunnerApi errandRunnerApi;
    @DubboReference
    private ActivityApi activityApi;
    @Resource
    private Validator validator;

    //原材料通知开关 默认关
    @Value("${notifyswitch.rawmaterial:false}")
    private Boolean notifyRawmaterialSwitch;

    @Resource
    private BzOrderMapper bzOrderMapper;
    @Resource
    private HashedWheelTimerProxy hashedWheelTimerProxy;
    @Resource
    private BzOrderProductService bzOrderProductService;
    @Resource
    private BzOrderProductSonService bzOrderProductSonService;
    @Resource
    private BzOrderCondimentsService bzOrderCondimentsService;
    @Resource
    private BzOrderPurchaseService bzOrderPurchaseService;
    @Resource
    private BzOrderLogService bzOrderLogService;
    @Resource
    private ExcelActionService<OrderResVO> orderRespVOExcelActionService;
    @Resource
    private PriceCalculatorService priceCalculatorService;
    @Resource
    private PriceCalculatorV2Service priceCalculatorV2Service;
    @Resource
    private SeckillCalculatorService seckillCalculatorService;
    @Resource
    private CalculateCacheService calcCacheService;
    @Resource
    private CalculateCacheV2Service calcCacheV2Service;
    @Resource
    private OrderSubmitStrategyFactory<SubmitReqVO> strategyFactory;
    @Resource
    protected StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ElasticsearchClient elasticsearchClient;
    @Resource
    private RabbitMQService rabbitMQService;
    @Resource
    private ThreadPoolExecutor ioExecutor;
    @Resource
    private XingYiPayService xingYiPayService;
    @Lazy
    @Resource
    private BzOrderPayService bzOrderPayService;
    @Resource
    private ProxyService proxyService;
    @Resource
    private KafkaProducerUtil kafkaProducerUtil;
    @Resource
    private BulkIngester<BzOrderPointsDocument> bulkIngester;

    @Override
    public void addOrderPoints(OrderDetailDTO detail) {
        //判断是否命中
        List<Long> trueActivityIds = this.getTrueActivityIds(detail);
        log.info("==> 订单命中集点活动ID:trueActivityIds {}", trueActivityIds);
        trueActivityIds.forEach(activityId -> this.putPointsRecordIntoDB(detail.getBzOrderDO(), activityId));
    }

    /**
     * 添加集点记录
     *
     * @param bzOrderDO
     * @param activityId
     */
    private void putPointsRecordIntoDB(BzOrderDO bzOrderDO, Long activityId) {
        //命中加点
        stringRedisTemplate.opsForHash().increment(JD_MEMBER_POINTS_COLLECT + activityId, bzOrderDO.getMemberId().toString(), 1);
        log.info("==> 集点活动新增点数 | activityId {} memberId {}", activityId, bzOrderDO.getMemberId());
        //新增集点记录ES
        bulkIngester.add(op -> op.create(
                idx -> idx.document(this.toBzOrderPointsDocument(bzOrderDO, activityId))
                        .index(BzOrderPointsDocument.class.getAnnotation(Document.class).indexName())
                        .routing(String.valueOf(activityId))
        ));
        log.info("==> 集点活动新增ES | activityId {} memberId {}", activityId, bzOrderDO.getMemberId());
    }

    /**
     * 获取有效活动ID
     *
     * @param detail
     * @return
     */
    private List<Long> getTrueActivityIds(OrderDetailDTO detail) {
        List<BzOrderProductDO> productDOList = detail.getProductDOList();
        BzOrderDO bzOrderDO = detail.getBzOrderDO();
        Long storeId = bzOrderDO.getStoreId();

        Set<Object> activityIds = redisTemplate.opsForHash().keys(JD_ACTIVITY_STORE + storeId);
        if (CollectionUtils.isEmpty(activityIds)) {
            log.info("==> 门店未匹配到集点活动 | orderSn {} | storeId {}", bzOrderDO.getOrderSn(), storeId);
            return new ArrayList<>();
        }

        List<ActivityJDFullRespVO> activityJDRespVOList = new ArrayList<>();
        activityIds.forEach(o -> {
            if (ObjectUtils.isEmpty(o)) {
                return;
            }
            long activityId = Long.parseLong(o.toString());
            Object activityJson = redisTemplate.opsForValue().get(JD_ACTIVITY + activityId);
            if (ObjectUtils.isEmpty(activityJson)) {
                log.info("==> 未查询到集点活动缓存 | orderSn {} | activityId {}", bzOrderDO.getOrderSn(), activityId);
                return;
            }
            try {
                ActivityJDFullRespVO activityJDRespVO = JSON.parseObject(activityJson.toString(), ActivityJDFullRespVO.class);
                activityJDRespVOList.add(activityJDRespVO);
            } catch (Exception e) {
                log.error("==> 集点活动信息转换失败 | activityJDRespVO {}", activityJson);
            }
        });

        if (!CollectionUtils.isEmpty(activityJDRespVOList)) {
            Set<Long> commodityIds = productDOList.stream().map(BzOrderProductDO::getCommodityId).collect(Collectors.toSet());
            return activityJDRespVOList.stream()
                    .filter(activity -> activity.getIsEnabled() == 1)
                    .filter(activity -> {
                        //先判断是否超过上限
                        Object o = stringRedisTemplate.opsForHash().get(JD_MEMBER_POINTS_COLLECT + activity.getId(), bzOrderDO.getMemberId().toString());
                        int pointsNum = o == null ? 0 : Integer.parseInt(o.toString());

                        Integer maxPoints = this.getMaxPoints(activity);
                        if (pointsNum >= maxPoints) {
                            return false;
                        }

                        //判断是否命中商品
                        boolean commodityPresent = false;
                        if (activity.getCollectPointsCommodityType() == 1) {
                            commodityPresent = true;
                        } else {
                            commodityPresent = activity.getActivityJDCommodityRespList().stream()
                                    .anyMatch(commodity -> commodityIds.contains(commodity.getCommodityId()));
                        }

                        //按订单判断
                        boolean orderPresent = activity.getCollectPointsType() != 1 || activity.getCollectPointsThreshold().compareTo(BigDecimal.ZERO) <= 0 || bzOrderDO.getPayAmount().compareTo(activity.getCollectPointsThreshold()) >= 0;

                        //判断时间
                        Date now = new Date();
                        String endStr = DateUtils.dateToStringDate(activity.getEndDate(), "yyyy-MM-dd");
                        Date endDate = com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime("yyyy-MM-dd HH:mm:ss", endStr + " 23:59:59");

                        boolean timePresent = !activity.getStartDate().after(now) && !endDate.before(now);

                        log.info("==> 集点活动命中 | orderSn {} | activityId {} | commodityPresent {} | orderPresent {} | timePresent {}", bzOrderDO.getOrderSn(), activity.getId(), commodityPresent, orderPresent, timePresent);
                        return commodityPresent && orderPresent && timePresent;
                    })
                    .map(ActivityJDRespVO::getId)
                    .toList();
        }
        return new ArrayList<>();
    }

    /**
     * 获取最大集点数
     **/
    private Integer getMaxPoints(ActivityJDFullRespVO activityJDRespVO) {
        List<ActivityJDCouponPackageRespSaveVO> couponPackageList = activityJDRespVO.getCouponPackageList();
        List<ActivityJDCouponRespSaveVO> couponList = activityJDRespVO.getCouponList();

        int maxRedeemPoints =
                Stream.concat(
                                couponPackageList.stream().map(ActivityJDCouponPackageRespSaveVO::getRedeemPoints),
                                couponList.stream().map(ActivityJDCouponRespSaveVO::getRedeemPoints)
                        )
                        .filter(Objects::nonNull)
                        .max(Integer::compareTo)
                        .orElse(0);
        return maxRedeemPoints;
    }

    @Override
    public void delOrderPointsByActivityId(Long activityId) throws IOException {
        //删除缓存
        stringRedisTemplate.delete(JD_MEMBER_POINTS_COLLECT + activityId);
        elasticsearchClient.deleteByQuery(d -> d
                .index(BzOrderPointsDocument.class.getAnnotation(Document.class).indexName())
                .routing(String.valueOf(activityId))
                .query(q -> q
                        .term(t -> t
                                .field("activityId")
                                .value(activityId)
                        )
                )
        );
    }

    @Override
    public void delOrderPointsByOrderId(Long orderId) throws IOException {
        String indexName = BzOrderPointsDocument.class.getAnnotation(Document.class).indexName();
        //先查询当前订单是否满足集点
        SearchResponse<BzOrderPointsDocument> response = elasticsearchClient.search(s -> s
                        .index(indexName)
                        .query(q -> q
                                .term(t -> t
                                        .field("orderId")
                                        .value(orderId)
                                )
                        ),
                BzOrderPointsDocument.class
        );

        List<BzOrderPointsDocument> bzOrderPointsDocumentList = response.hits().hits()
                .stream()
                .map(Hit::source)
                .toList();

        if (ObjectUtils.isEmpty(bzOrderPointsDocumentList)) {
            return;
        }

        bzOrderPointsDocumentList.forEach(bzOrderPointsDocument -> {
            //退款过来的，先去缓存扣点，再删ES
            stringRedisTemplate.opsForHash().increment(JD_MEMBER_POINTS_COLLECT + bzOrderPointsDocument.getActivityId(), bzOrderPointsDocument.getMemberId().toString(), -1);
        });
        elasticsearchClient.deleteByQuery(d -> d
                .index(indexName)
                .query(q -> q
                        .term(t -> t
                                .field("orderId")
                                .value(orderId)
                        )
                )
        );
    }

    private BzOrderPointsDocument toBzOrderPointsDocument(BzOrderDO bzOrderDO, Long activityId) {
        BzOrderPointsDocument bzOrderPointsDocument = new BzOrderPointsDocument();
        BeanUtils.copyProperties(bzOrderDO, bzOrderPointsDocument);
        bzOrderPointsDocument.setActivityId(activityId);
        return bzOrderPointsDocument;
    }

    @Override
    public SearchAfterPage<OrderResVO> pOrderpage(OrderPageReqVO reqVO) {
        SearchAfterPage<OrderResVO> returnObj;

        //不允许深分页
        if (reqVO.getPageNo() > 5000) {
            throw exception(ORDER_PAGE_NOT_MORE);
        }

        if (ObjectUtil.isEmpty(reqVO.getIsStore()) && ObjectUtil.isEmpty(reqVO.getOrgId())) {
            Set<Long> storeIdSet = storeApi.getAllStoreIdByUser(SecurityFrameworkUtils.getLoginUserId()).getCheckedData();
            reqVO.setStoreIds(storeIdSet);
        } else {
            //门店
            if (Objects.equals(reqVO.getIsStore(), OrderConstants.YES)) {
                reqVO.setStoreIds(new HashSet<>());
                reqVO.setStoreId(reqVO.getOrgId());
            } else {
                if (!ObjectUtils.isEmpty(reqVO.getOrgId())) {
                    //组织
                    Set<Long> storeIds = orgApi.getStoreIdListByOrgID(reqVO.getOrgId(), BusinessContextHolder.getBusinessId()).getData();

                    if (CollectionUtils.isEmpty(storeIds)) {
                        return SearchAfterPage.empty();
                    }

                    reqVO.setStoreIds(storeIds);
                    reqVO.setStoreId(null);
                }
            }
        }

//        List<OrderResVO> returnList;
        try {
            //构建查询条件
            SearchRequest.Builder builder = this.getQuerybuilder(reqVO);
            //优先查询ES
            SearchAfterPage<OrderResVO> pageResult = this.searchOrdersFromES(reqVO, builder);
            returnObj = new SearchAfterPage<>(pageResult.getRecords(), pageResult.getTotal());
            returnObj.setSearchAfter(pageResult.getSearchAfter());
        } catch (Exception e) {
            log.warn("查询ES订单异常", e);
            //备用查询Mysql
//            PageResult<BzOrderDO> pageResult = bzOrderMapper.selectPage(reqVO);
//            returnList = BeanCopyUtils.copyBeanList(pageResult.getList(), OrderResVO.class);
            returnObj = new SearchAfterPage<>();
        }

        return returnObj;
    }

    /**
     * 分页查询订单
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    private SearchAfterPage<OrderResVO> searchOrdersFromES(OrderPageReqVO reqVO, SearchRequest.Builder builder) {
        SearchAfterPage<OrderResVO> result = new SearchAfterPage<>();
        // ✅ 排序（建议复合排序确保唯一性）
        builder.sort(s -> s.field(f -> f.field("createTime").order(SortOrder.Desc)));
        builder.sort(s -> s.field(f -> f.field("orderId").order(SortOrder.Desc)));

        // ✅ 公共参数：分页大小
        int pageSize = ObjectUtil.defaultIfNull(reqVO.getPageSize(), 1000);
        builder.size(pageSize);

        // ✅ 判断分页模式
        if (CollUtil.isNotEmpty(reqVO.getSearchAfter())) {
            // ===== SearchAfter 模式 =====
            builder.searchAfter(reqVO.getSearchAfter());
        } else {
            // ===== 普通分页模式 =====
            int from = (ObjectUtil.defaultIfNull(reqVO.getPageNo(), 1) - 1) * pageSize;
            builder.from(from);
        }

        try {
            // 执行查询
            SearchResponse<BzOrderDocument> search = elasticsearchClient.search(builder.build(), BzOrderDocument.class);
            HitsMetadata<BzOrderDocument> hits = search.hits();

            result.setTotal(hits.total() != null ? hits.total().value() : 0);

            if (CollUtil.isNotEmpty(hits.hits())) {
                List<OrderResVO> list = new ArrayList<>();
                for (Hit<BzOrderDocument> hit : hits.hits()) {
                    BzOrderDocument bzOrderDocument = hit.source();
                    if (ObjectUtil.isNotNull(bzOrderDocument)) {
                        final OrderResVO orderResVO = new OrderResVO();
                        BeanUtils.copyProperties(bzOrderDocument, orderResVO);
                        orderResVO.setCreateTime(DateUtils.dateToStringDate(bzOrderDocument.getCreateTime(), DateUtils.YYYY_MM_DD_HH_MM_SS));
                        orderResVO.setActivityDiscountAmount(this.calculateOrderPageActivityDiscountAmount(bzOrderDocument));
                        orderResVO.setImageList(
                                bzOrderDocument.getProducts().stream()
                                        .map(BzOrderDocument.ProductInfo::getGoodsImage)
                                        .collect(Collectors.toSet())
                        );
                        orderResVO.setGoodsNum(bzOrderDocument.getProducts().stream().mapToLong(BzOrderDocument.ProductInfo::getGoodsNum).sum());
                        list.add(orderResVO);
                    }
                }
                result.setRecords(list);

                // ✅ 保存下一页游标
                List<FieldValue> lastSortValues = hits.hits().get(hits.hits().size() - 1).sort();
                result.setSearchAfter(lastSortValues);
            }

        } catch (IOException e) {
            log.warn("==> 查询ES订单异常", e);
        }

        return result;
    }

    private BigDecimal calculateOrderPageActivityDiscountAmount(BzOrderDocument order) {
        BigDecimal amount = this.safeAmount(order.getActivityDiscountAmount())
                .add(this.safeAmount(order.getPromotionDiscountAmount()));
        if (Objects.equals(order.getOrderType(), OrderTypeEnum.ERRAND.getCode())) {
            amount = amount.add(this.safeAmount(order.getErrandStoreSubsidyAmount()));
        }
        return amount;
    }

    /**
     * 构建查询条件 老板助手
     *
     * @param reqVO 查询条件
     * @return 构建后的查询条件
     */
    private SearchRequest.Builder getQuerybuilderForApp(OrderPageReqVO reqVO) {

        //构建ES请求入参
        SearchRequest.Builder Querybuilder = new SearchRequest.Builder();
        Querybuilder.index(BzOrderDocument.class.getAnnotation(Document.class).indexName());
        //精确查询命中总数
        Querybuilder.trackTotalHits(th -> th.enabled(true));

        Querybuilder.query(query -> query.bool(boolQuery -> {

            appendOrderTypeQuery(boolQuery, reqVO.getOrderTypeQueryList());

            if (!ObjectUtils.isEmpty(reqVO.getStoreId())) {
                boolQuery.must(TermQuery.of(field -> field.field("storeId").value(reqVO.getStoreId()))._toQuery());
            }

            if (!CollectionUtils.isEmpty(reqVO.getStoreIds())) {
                boolQuery.filter(m -> m.terms(t -> t.field("storeId").terms(tv -> tv.value(reqVO.getStoreIds().stream().map(FieldValue::of).toList()))));
            }

            if (!ObjectUtils.isEmpty(reqVO.getOrderFrom())) {
                boolQuery.must(TermQuery.of(field -> field.field("orderFrom").value(reqVO.getOrderFrom()))._toQuery());
            }

            appendErrandOrderStateQuery(boolQuery, reqVO.getOrderStateQueryList(), true);

            if (!ObjectUtils.isEmpty(reqVO.getPaymentCode())) {
                boolQuery.must(TermQuery.of(field -> field.field("paymentCode").value(reqVO.getPaymentCode()))._toQuery());
            }

            //订单时间
            reqVO.setStartOrderTime(DateUtils.localDateTimeToString(reqVO.getCreateTime()[0], DateUtils.YYYY_MM_DD_HH_MM_SS));
            reqVO.setEndOrderTime(DateUtils.localDateTimeToString(reqVO.getCreateTime()[1], DateUtils.YYYY_MM_DD_HH_MM_SS));
            if (!ObjectUtils.isEmpty(reqVO.getStartOrderTime()) && !ObjectUtils.isEmpty(reqVO.getEndOrderTime())) {
                boolQuery.filter(m -> m.range(r -> r.date(dr -> dr.field("createTime").gte(String.valueOf(reqVO.getStartOrderTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())).lte(String.valueOf(reqVO.getEndOrderTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())))));
            }

            //活动类型
            if (!ObjectUtils.isEmpty(reqVO.getActivityType())) {
                boolQuery.filter(m -> m.nested(n -> n.path("product").query(nq -> nq.bool(nb -> {
                    nb.must(mm -> mm.term(t -> t.field("product.activityType").value(reqVO.getActivityType())));
                    return nb;
                }))));
            }

            // 通用搜索字符串（模糊匹配 orderSn / expressNumber / takeAwayTel）
            if (!ObjectUtils.isEmpty(reqVO.getSearchStr())) {
                boolQuery.should(WildcardQuery.of(f -> f.field("orderSn").value("*" + reqVO.getSearchStr() + "*"))._toQuery());
                boolQuery.should(WildcardQuery.of(f -> f.field("expressNumber").value("*" + reqVO.getSearchStr() + "*"))._toQuery());
                boolQuery.should(WildcardQuery.of(f -> f.field("takeAwayTel").value("*" + reqVO.getSearchStr() + "*"))._toQuery());
                boolQuery.minimumShouldMatch("1"); // 表示只要匹配到任意一个 should 条件即可
            }


            return boolQuery;
        }));

        return Querybuilder;
    }

    private void appendOrderTypeQuery(BoolQuery.Builder boolQuery, List<Integer> orderTypes) {
        if (CollectionUtils.isEmpty(orderTypes)) {
            return;
        }
        boolQuery.filter(m -> m.terms(t -> t.field("orderType").terms(tv -> tv.value(orderTypes.stream().map(FieldValue::of).toList()))));
    }

    private void appendErrandOrderStateQuery(BoolQuery.Builder boolQuery, List<Integer> orderStates, boolean excludeWaitingAcceptWhenAll) {
        if (CollectionUtils.isEmpty(orderStates)) {
            if (excludeWaitingAcceptWhenAll) {
                boolQuery.mustNot(q -> q.bool(b -> b
                        .must(TermQuery.of(field -> field.field("orderType").value(OrderTypeEnum.ERRAND.getCode()))._toQuery())
                        .must(m -> m.terms(t -> t.field("orderState")
                                .terms(tv -> tv.value(List.of(
                                        FieldValue.of(OrderStateEnum.UNPAID.getCode()),
                                        FieldValue.of(OrderStateEnum.WAITING_ACCEPT.getCode()))))))
                ));
            }
            return;
        }
        if (orderStates.stream().noneMatch(Objects::nonNull)) {
            return;
        }
        boolQuery.must(q -> q.bool(b -> {
            for (Integer orderState : orderStates) {
                if (orderState == null) {
                    continue;
                }
                b.should(s -> s.term(t -> t.field("orderState").value(orderState)));
            }
            b.minimumShouldMatch("1");
            return b;
        }));
    }

    /**
     * 构建查询条件
     *
     * @param reqVO 查询条件
     * @return 构建后的查询条件
     */
    private SearchRequest.Builder getQuerybuilder(OrderPageReqVO reqVO) {

        //构建ES请求入参
        SearchRequest.Builder Querybuilder = new SearchRequest.Builder();
        Querybuilder.index(BzOrderDocument.class.getAnnotation(Document.class).indexName());
        //精确查询命中总数
        Querybuilder.trackTotalHits(th -> th.enabled(true));

        Querybuilder.query(query -> query.bool(boolQuery -> {

            boolQuery.must(TermQuery.of(field -> field.field("businessId").value(BusinessContextHolder.getBusinessId()))._toQuery());

            appendOrderTypeQuery(boolQuery, reqVO.getOrderTypeQueryList());

            if (!ObjectUtils.isEmpty(reqVO.getStoreId())) {
                boolQuery.must(TermQuery.of(field -> field.field("storeId").value(reqVO.getStoreId()))._toQuery());
            }

            if (!ObjectUtils.isEmpty(reqVO.getOrderFrom())) {
                boolQuery.must(TermQuery.of(field -> field.field("orderFrom").value(reqVO.getOrderFrom()))._toQuery());
            }

            if (!ObjectUtils.isEmpty(reqVO.getOrderSn())) {
                boolQuery.must(WildcardQuery.of(field -> field.field("orderSn").value("*" + reqVO.getOrderSn() + "*"))._toQuery());
            }

            if (!ObjectUtils.isEmpty(reqVO.getMobile())) {
                boolQuery.must(WildcardQuery.of(field -> field.field("takeAwayTel").value("*" + reqVO.getMobile() + "*"))._toQuery());
            }

            //渠道归类
            if (!ObjectUtils.isEmpty(reqVO.getChannelType())) {
                boolQuery.must(TermQuery.of(field -> field.field("channelType").value(reqVO.getChannelType()))._toQuery());
            }

            if (!ObjectUtils.isEmpty(reqVO.getPaySn())) {
                boolQuery.must(WildcardQuery.of(field -> field.field("expressNumber").value("*" + reqVO.getPaySn() + "*"))._toQuery());
            }

            appendErrandOrderStateQuery(boolQuery, reqVO.getOrderStateQueryList(), false);

            if (!ObjectUtils.isEmpty(reqVO.getPaymentCode())) {
                boolQuery.must(TermQuery.of(field -> field.field("paymentCode").value(reqVO.getPaymentCode()))._toQuery());
            }

            if (!ObjectUtils.isEmpty(reqVO.getStartOrderTime()) && !ObjectUtils.isEmpty(reqVO.getEndOrderTime())) {
                boolQuery.filter(m -> m.range(r -> r.date(dr -> dr.field("createTime").gte(String.valueOf(reqVO.getStartOrderTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())).lte(String.valueOf(reqVO.getEndOrderTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())))));
            }

            if (!CollectionUtils.isEmpty(reqVO.getStoreIds())) {
                boolQuery.filter(m -> m.terms(t -> t.field("storeId").terms(tv -> tv.value(reqVO.getStoreIds().stream().map(FieldValue::of).toList()))));
            }

            //活动类型
            if (!ObjectUtils.isEmpty(reqVO.getActivityType())) {
                boolQuery.filter(m -> m.nested(n -> n.path("product").query(nq -> nq.bool(nb -> {
                    nb.must(mm -> mm.term(t -> t.field("product.activityType").value(reqVO.getActivityType())));
                    return nb;
                }))));
            }


            //活动ID
            if (!ObjectUtils.isEmpty(reqVO.getActivityId())) {
                boolQuery.filter(m -> m.nested(n -> n.path("product").query(nq -> nq.bool(nb -> nb.must(mm -> mm.term(t -> t.field("product.activityId").value(reqVO.getActivityId())))))));
            }

            //活动名称
            if (!ObjectUtils.isEmpty(reqVO.getActivityName())) {
                boolQuery.filter(m -> m.nested(n -> n.path("product").query(nq -> nq.bool(nb -> nb.must(mm -> mm.term(t -> t.field("product.activityName.keyword").value(reqVO.getActivityName())))))));
            }


            //用户优惠券id
            if (!ObjectUtils.isEmpty(reqVO.getUserCouponId())) {
                boolQuery.filter(m -> m.nested(n -> n.path("product").query(nq -> nq.bool(nb -> nb.must(mm -> mm.term(t -> t.field("product.userCouponId").value(FieldValue.of(reqVO.getUserCouponId().longValue()))))))));
            }

            //优惠券id
            if (!ObjectUtils.isEmpty(reqVO.getCouponId())) {
                boolQuery.filter(m -> m.nested(n -> n.path("product").query(nq -> nq.bool(nb -> nb.must(mm -> mm.term(t -> t.field("product.couponId").value(FieldValue.of(reqVO.getCouponId().longValue()))))))));
            }

            //优惠券名称
            if (!ObjectUtils.isEmpty(reqVO.getCouponName())) {
//                boolQuery.filter(
//                        m -> m.nested(n -> n
//                                .path("product")
//                                .query(nq -> nq.bool(nb -> nb.must(mm ->
//                                        mm.term(t -> t
//                                                .field("product.couponName")
//                                                .value(reqVO.getCouponName())
//                                        )
//                                )))
//                        )
//                );

                boolQuery.must(WildcardQuery.of(field -> field.field("product.couponName").value("*" + reqVO.getCouponName() + "*"))._toQuery());
            }

            return boolQuery;
        }));

        return Querybuilder;
    }

    @Override
    public List<BzOrderPointsDocument> pointsCollectPage(Long activityId) {
        List<BzOrderPointsDocument> returnList = new ArrayList<>();

        try {
            SearchResponse<BzOrderPointsDocument> response =
                    elasticsearchClient.search(s -> s
                                    .index(BzOrderPointsDocument.class.getAnnotation(Document.class).indexName())
                                    // 若写入用了 routing 必须加
                                    .routing(activityId.toString())
                                    .query(q -> q
                                            .bool(b -> b
                                                    .must(m -> m
                                                            .term(t -> t
                                                                    .field("activityId")
                                                                    .value(activityId)
                                                            )
                                                    )
                                                    .must(m -> m
                                                            .term(t -> t
                                                                    .field("memberId")
                                                                    .value(WebFrameworkUtils.getLoginUserId())
                                                            )
                                                    )
                                            )
                                    )
                                    // 不分页：一次最多 10000 条
                                    .size(10000),
                            BzOrderPointsDocument.class
                    );

            returnList = response.hits().hits().stream()
                    .map(Hit::source)
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("==> [poinsCollectPage][查询失败]", e);
        }
        return returnList;
    }

    @Override
    public Integer pointsCollectCount(Long activityId) {
        Object o = stringRedisTemplate.opsForHash().get(JD_MEMBER_POINTS_COLLECT + activityId, WebFrameworkUtils.getLoginUserId().toString());
        return o == null ? 0 : Integer.parseInt(o.toString());
    }

    @Override
    public OrderDetailRspVO getBzOrderInfo(String orderSn) {

        String normalizedOrderSn = this.normalizeOrderSnOrThrow(orderSn);
        BzOrderDO order = this.getOrderOrThrow(normalizedOrderSn);
        OrderDetailRspVO rsp = this.buildBaseOrderDetail(order);

        LocalDateTime[] createTimes = {};
        List<String> orderSns = Collections.singletonList(order.getOrderSn());

        // 1) 拉取明细
        List<BzOrderProductVO> orderProducts = bzOrderProductService.getOrderProductList(orderSns, createTimes);
        List<BzOrderCondimentsDO> condimentList = bzOrderCondimentsService.getOrderCondimentsList(orderSns, createTimes);
        List<BzOrderPurchaseDO> purchases = bzOrderPurchaseService.getOrderPurchaseList(orderSns, createTimes);
        Map<String, List<BzOrderProductSonDO>> sonsMap = this.loadPackageSonsIfNeeded(normalizedOrderSn, orderProducts);

        // 2) 预处理映射
        Map<String, List<BzOrderCondimentsDO>> condimentsByGoodsId = this.groupCondimentsByGoodsId(condimentList);

        // 3) 填充商品明细（小料/套餐子项/属性/活动），并统计
        ProcessProductsResult processed = this.processOrderProducts(orderProducts, condimentsByGoodsId, sonsMap);

        // 4) 合并“拆单商品”（按 sendIntegral 分组的逻辑保持不变）
        List<BzOrderProductVO> mergedProducts = this.mergeSplitProducts(processed.products);

        // 5) 组装订单日志时间点映射
        Map<String, String> stateTimeMap = this.buildOrderStateDateTimeMap(orderSns, createTimes);

        // 6) 回填 rsp
        rsp.setBzOrderPurchaseDOList(purchases);
        rsp.setBzOrderStateDateTimeMap(stateTimeMap);
        rsp.setGoodsNum(processed.goodsNum + purchases.size());
        rsp.setBzOrderProductList(mergedProducts);
        rsp.setActivityName(String.join(",", processed.activityNames));
        rsp.setActivityDiscounts(new ArrayList<>(processed.activityAggMap.values()));

        return rsp;
    }

    private String normalizeOrderSnOrThrow(String orderSn) {
        if (StringUtils.isEmpty(orderSn)) {
            throw exception(ORDER_NOT_EXISTS);
        }
        String normalized = orderSn.trim();
        if (normalized.isEmpty()) {
            throw exception(ORDER_NOT_EXISTS);
        }
        return normalized;
    }

    private BzOrderDO getOrderOrThrow(String orderSn) {
        BzOrderDO order = baseMapper.selectOne(
                new LambdaQueryWrapper<BzOrderDO>().eq(BzOrderDO::getOrderSn, orderSn)
        );
        if (ObjectUtils.isEmpty(order)) {
            throw exception(ORDER_NOT_EXISTS);
        }
        return order;
    }

    private OrderDetailRspVO buildBaseOrderDetail(BzOrderDO order) {
        OrderDetailRspVO rsp = new OrderDetailRspVO();
        BeanUtil.copyProperties(order, rsp, CopyOptions.create().setIgnoreProperties("errandDeliveryImages"));

        // 这两处字段含义看起来不匹配，但保持原逻辑：仅做安全解析
        Double lat = this.tryParseDouble(order.getStoreRemark());
        if (lat != null) {
            rsp.setStoreLatitude(lat);
        }
        Double lng = this.tryParseDouble(order.getExpressName());
        if (lng != null) {
            rsp.setStoreLongitude(lng);
        }

        rsp.setCreateTime(this.formatOrderDetailTime(order.getCreateTime()));
        rsp.setPayTime(this.formatOrderDetailTime(order.getPayTime()));
        rsp.setMakingTime(this.formatOrderDetailTime(order.getMakingTime()));
        rsp.setWaitingTime(this.formatOrderDetailTime(order.getWaitingTime()));
        rsp.setFinishTime(this.formatOrderDetailTime(order.getFinishTime()));
        rsp.setOrderFromName(OrderFromEnum.getMessageByCode(order.getOrderFrom()));
        rsp.setOrderTypeName(OrderTypeEnum.getMsgByCode(order.getOrderType()));
        rsp.setOrderStateName(OrderStateEnum.getMessageByCode(order.getOrderState()));
        rsp.setPhone(order.getStorePhone());
        rsp.setCouponName(order.getTakeAwayAddress());
        rsp.setCouponCode(order.getVoucherCode());
        rsp.setOrderSource(this.getOrderSource(order));
        rsp.setErrandDeliveryImages(this.parseErrandDeliveryImages(order.getErrandDeliveryImages()));

        return rsp;
    }

    private String formatOrderDetailTime(LocalDateTime time) {
        return time == null ? null : DateUtils.localDateTimeToString(time, DateUtils.YYYY_MM_DD_HH_MM_SS);
    }

    private List<String> parseErrandDeliveryImages(String errandDeliveryImages) {
        if (StringUtils.isBlank(errandDeliveryImages)) {
            return Collections.emptyList();
        }
        return Arrays.stream(errandDeliveryImages.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    private Double tryParseDouble(String s) {
        if (ObjectUtil.isEmpty(s)) return null;
        try {
            return Double.valueOf(s);
        } catch (Exception ignore) {
            return null; // 降级：不影响详情返回
        }
    }

    private Map<String, List<BzOrderCondimentsDO>> groupCondimentsByGoodsId(List<BzOrderCondimentsDO> condimentList) {
        if (CollectionUtils.isEmpty(condimentList)) return Collections.emptyMap();
        return condimentList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(BzOrderCondimentsDO::getCommodityGoodsid));
    }

    private Map<String, List<BzOrderProductSonDO>> loadPackageSonsIfNeeded(String orderSn, List<BzOrderProductVO> orderProducts) {
        if (CollectionUtils.isEmpty(orderProducts)) return Collections.emptyMap();

        List<Long> packageProductIds = orderProducts.stream()
                .filter(Objects::nonNull)
                .filter(p -> Objects.equals(p.getIsSingle(), IsSingleEnum.PACKAGE.getCode()))
                .map(BzOrderProductVO::getOrderProductId)
                .filter(Objects::nonNull)
                .toList();

        if (CollectionUtils.isEmpty(packageProductIds)) {
            return Collections.emptyMap();
        }

        List<BzOrderProductSonDO> sons = bzOrderProductSonService.list(
                new LambdaQueryWrapper<BzOrderProductSonDO>().eq(BzOrderProductSonDO::getOrderSn, orderSn)
        );

        if (CollectionUtils.isEmpty(sons)) return Collections.emptyMap();

        return sons.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(e -> String.valueOf(e.getParentGoodsId())));
    }

    private static class ProcessProductsResult {
        long goodsNum;
        List<BzOrderProductVO> products;
        Set<String> activityNames;
        Map<String, ActivityDiscountDTO> activityAggMap;

        private ProcessProductsResult(long goodsNum, List<BzOrderProductVO> products, Set<String> activityTags, Map<String, ActivityDiscountDTO> activityAggMap) {
            this.goodsNum = goodsNum;
            this.products = products;
            this.activityNames = activityTags;
            this.activityAggMap = activityAggMap;
        }
    }

    private ProcessProductsResult processOrderProducts(List<BzOrderProductVO> products,
                                                       Map<String, List<BzOrderCondimentsDO>> condimentsByGoodsId,
                                                       Map<String, List<BzOrderProductSonDO>> sonsMap) {
        if (CollectionUtils.isEmpty(products)) {
            return new ProcessProductsResult(0L, Collections.emptyList(), new NotNullHashSet<>(), Collections.emptyMap());
        }

        long goodsNum = 0L;
        NotNullHashSet<String> activityNames = new NotNullHashSet<>();
        // 聚合后的活动优惠（key -> dto）
        Map<String, ActivityDiscountDTO> activityAggMap = new LinkedHashMap<>();

        for (BzOrderProductVO product : products) {
            if (product == null) continue;

            goodsNum += Optional.ofNullable(product.getGoodsNum()).orElse(0);

            // 单品/套餐处理
            if (Objects.equals(product.getIsSingle(), 1)) {
                this.applyCondimentsToProduct(product, condimentsByGoodsId);
            } else {
                this.applyPackageSonsToProduct(product, sonsMap);
            }

            // 属性格式化
            this.normalizeFlavorFields(product);

            // 活动处理
            this.applyActivityInfo(product, activityNames, activityAggMap);
        }

        return new ProcessProductsResult(goodsNum, products, activityNames, activityAggMap);
    }

    private void applyCondimentsToProduct(BzOrderProductVO product,
                                          Map<String, List<BzOrderCondimentsDO>> condimentsByGoodsId) {
        List<BzOrderCondimentsDO> list = condimentsByGoodsId.get(String.valueOf(product.getOrderProductId()));
        if (ObjectUtil.isEmpty(list)) return;

        List<CommodityCondimentsVO> commodityCondiments = new ArrayList<>();
        List<String> describeList = new ArrayList<>();

        for (BzOrderCondimentsDO c : list) {
            if (c == null) continue;

            CommodityCondimentsVO vo = new CommodityCondimentsVO();
            vo.setPrice(c.getCondimentPrice());
            vo.setCommodityId(c.getCondimentId());
            vo.setCondimentName(c.getCondimentName());
            vo.setImageUrl(c.getCondimentImage());
            vo.setDescription(c.getCondimentDescription());
            vo.setCommoditySkuname(c.getCommoditySkuname());
            vo.setCommodityFeedingname(c.getCondimentDescription());
            vo.setNumber(c.getCondimentNumber());

            // 你原来是往 List 里 add，这里保持
            product.getCondimentNameList().add(c.getCondimentName() + " x " + c.getCondimentNumber());
            product.getCondimentStr().add(c.getCondimentName() + " * " + c.getCondimentNumber());

            describeList.add(c.getCommodityValue());
            describeList.add(c.getCondimentDescription());
            commodityCondiments.add(vo);
        }

        product.setDescribeList(describeList);
        product.setCommodityCondiments(commodityCondiments);
    }

    private void applyPackageSonsToProduct(BzOrderProductVO product,
                                           Map<String, List<BzOrderProductSonDO>> sonsMap) {
        List<BzOrderProductSonDO> sons = sonsMap.get(String.valueOf(product.getOrderProductId()));
        List<BzOrderProductSonDO> safeSons = CollectionUtils.isEmpty(sons) ? new ArrayList<>() : sons;

        //整合属性
        this.splitFlavors(safeSons);

        product.setGroupBzOrderProductList(safeSons);

        if (ObjectUtil.isEmpty(safeSons)) return;

        List<String> describeList = new ArrayList<>();
        for (BzOrderProductSonDO s : safeSons) {
            if (s == null) continue;
            describeList.add(s.getGoodsName() + "*" + s.getGoodsNum());
            product.getSingleListStr().add(s.getGoodsName() + " * " + s.getGoodsNum());
        }
        product.setDescribeList(describeList);
    }

    private void splitFlavors(List<BzOrderProductSonDO> safeSons) {
        safeSons.forEach(son -> {
            String specValues = son.getSpecValues();
            if (!ObjectUtils.isEmpty(specValues) && specValues.contains("@")) {
                String[] split = specValues.split("@");
                son.setSkuName(split[0]);
                //属性 甜度:微甜,辣度:微辣
                String flavorStr = split[1];
                List<Map<String, String>> flavorList = new ArrayList<>();
                if (flavorStr != null && !flavorStr.isEmpty()) {
                    String[] pairs = flavorStr.split(",");
                    for (String pair : pairs) {
                        if (pair == null || pair.trim().isEmpty()) {
                            continue;
                        }
                        String[] kv = pair.split(":", 2);
                        if (kv.length == 2) {
                            String key = kv[0].trim();
                            String value = kv[1].trim();

                            if (!key.isEmpty() && !value.isEmpty()) {
                                Map<String, String> map = new HashMap<>();
                                map.put("name", key);
                                map.put("value", value);
                                flavorList.add(map);
                            }
                        }
                    }
                    son.setFlavors(flavorList);
                }
            }
        });
    }

    private void normalizeFlavorFields(BzOrderProductVO product) {
        String flavorNameStr = product.getFlavorName();
        String flavorValueStr = product.getFlavorValue();

        if (ObjectUtils.isEmpty(flavorNameStr) || ObjectUtils.isEmpty(flavorValueStr)) {
            // 保持你原逻辑：value 清掉、name 保留原样/或空
            product.setFlavorValue(null);
            return;
        }

        List<String> nameList = Arrays.asList(flavorNameStr.split(","));
        List<String> valueList = Arrays.asList(flavorValueStr.split(","));

        int n = Math.min(nameList.size(), valueList.size());
        List<String> pairs = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            pairs.add(nameList.get(i) + ":" + valueList.get(i));
        }

        product.setFlavorValue(null);
        product.setFlavorName(String.join(",", pairs));
    }

    private void applyActivityInfo(BzOrderProductVO product, Set<String> activityNames, Map<String, ActivityDiscountDTO> activityAggMap) {
        product.setIsGetActivity(ObjectUtils.isEmpty(product.getActivityId()) ? OrderConstants.NO : OrderConstants.YES);

        if (ObjectUtils.isEmpty(product.getActivityDiscountDetail())) {
            product.setActivityInfo(new ActivityNjnzInfoDTO());
            return;
        }

        try {
            ActivityNjnzInfoDTO dto = JSON.parseObject(product.getActivityDiscountDetail(), ActivityNjnzInfoDTO.class);
            product.setActivityInfo(dto);

            if (dto != null && !ObjectUtils.isEmpty(dto.getActivityName())) {
                activityNames.add(dto.getActivityName());

                String activityName = dto.getActivityName();
                Long activityId = dto.getActivityId();
                Integer activityType = dto.getActivityType();
                Integer discountType = dto.getDiscountType();
                Integer discountOffer = dto.getDiscountOffer();

                BigDecimal discount = product.getPromotionDiscountAmount();
                // ===== 按 规则生成聚合 key =====
                String key = activityId.toString();
                ActivityDiscountDTO activityDiscountDTO = activityAggMap.get(key);
                if (activityDiscountDTO == null) {
                    activityDiscountDTO = new ActivityDiscountDTO();
                    activityDiscountDTO.setActivityType(activityType);
                    activityDiscountDTO.setDiscountType(discountType);
                    activityDiscountDTO.setDiscountOffer(discountOffer);
                    activityDiscountDTO.setActivityName(activityName);
                    activityDiscountDTO.setActivityTag(ActivityDiscountDTO.buildActivityTag(activityType,
                            discountType, dto.getDiscountItemNum(), dto.getDiscountRate(),
                            dto.getActivityTag()));
                    activityDiscountDTO.setPromotionDiscountAmount(BigDecimal.ZERO);
                    activityAggMap.put(key, activityDiscountDTO);
                }

                activityDiscountDTO.setPromotionDiscountAmount(activityDiscountDTO.getPromotionDiscountAmount().add(discount));
            }
        } catch (Exception e) {
            // 降级：解析失败不影响订单详情
            product.setActivityInfo(new ActivityNjnzInfoDTO());
        }
    }

    private List<BzOrderProductVO> mergeSplitProducts(List<BzOrderProductVO> products) {
        if (CollectionUtils.isEmpty(products)) return Collections.emptyList();

        Map<Integer, List<BzOrderProductVO>> bySendIntegral =
                products.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(
                                BzOrderProductVO::getSendIntegral,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        List<BzOrderProductVO> merged = new ArrayList<>(products.size());

        bySendIntegral.forEach((k, group) -> {
            if (CollectionUtils.isEmpty(group)) return;

            BzOrderProductVO first = group.get(0);

            // 规则：v.size()>1 && discountType!=2 => 合并
            if (this.shouldMergeGroup(group, first)) {
                BzOrderProductVO base = this.pickBaseProductForMerge(group, first);

                BigDecimal totalStrikeAmount = group.stream().map(BzOrderProductVO::getSkuStrikePrice).filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalActivityAmount = group.stream().map(BzOrderProductVO::getActivityDiscountAmount).filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalPromotionAmount = group.stream().map(BzOrderProductVO::getPromotionDiscountAmount).filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                int totalGoodsNum = group.stream().mapToInt(e -> Optional.ofNullable(e.getGoodsNum()).orElse(0)).sum();

                base.setGoodsNum(totalGoodsNum);
                base.setGoodsShowPrice(base.getMoneyAmount().multiply(new BigDecimal(totalGoodsNum)));
                base.setSkuStrikePrice(totalStrikeAmount);
                base.setActivityDiscountAmount(totalActivityAmount);
                base.setPromotionDiscountAmount(totalPromotionAmount);
                base.setIsGetActivity(OrderConstants.NO);

                merged.add(base);
            } else {
                merged.addAll(group);
            }
        });

        return merged;
    }

    private boolean shouldMergeGroup(List<BzOrderProductVO> group, BzOrderProductVO first) {
        if (group.size() <= 1) return false;
        if (first == null || first.getActivityInfo() == null) return false;
        Integer discountType = first.getActivityInfo().getDiscountType();
        return !ObjectUtils.isEmpty(discountType) && discountType != 2;
    }

    private BzOrderProductVO pickBaseProductForMerge(List<BzOrderProductVO> group, BzOrderProductVO fallback) {
        for (BzOrderProductVO e : group) {
            if (e != null && !ObjectUtils.isEmpty(e.getActivityId())) {
                return e;
            }
        }
        return fallback;
    }

    private Map<String, String> buildOrderStateDateTimeMap(List<String> orderSns, LocalDateTime[] createTimes) {
        List<BzOrderLogDO> logs = bzOrderLogService.getOrderLogList(orderSns, createTimes);
        if (CollectionUtils.isEmpty(logs)) return Collections.emptyMap();

        Map<String, List<BzOrderLogDO>> byState = logs.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(e -> String.valueOf(e.getOrderStateLog())));

        return byState.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), this.findLatestLogTime(entry.getValue())))
                .filter(entry -> entry.getValue() != null)
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, entry.getValue()),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private Date findLatestLogTime(List<BzOrderLogDO> logs) {
        if (CollectionUtils.isEmpty(logs)) return null;
        Date latest = null;
        for (BzOrderLogDO l : logs) {
            if (l == null || l.getLogTime() == null) continue;
            if (latest == null || l.getLogTime().after(latest)) {
                latest = l.getLogTime();
            }
        }
        return latest;
    }


    /**
     * 获取订单来源
     *
     * @param bzOrderDO
     * @return
     */
    private Integer getOrderSource(BzOrderDO bzOrderDO) {
        if (ObjectUtils.isEmpty(bzOrderDO)) {
            return 0;
        }

        if (bzOrderDO.getLockState() == 1) {
            return OrderSourceEnum.SECKILL_ORDER.getCode();
        }

        if (!ObjectUtils.isEmpty(bzOrderDO.getExpressCode())) {
            return OrderSourceEnum.SPLICING_ORDER.getCode();
        }

        if (Objects.equals(OrderTypeEnum.ERRAND.getCode(), bzOrderDO.getOrderType())) {
            return OrderSourceEnum.ERRAND_ORDER.getCode();
        }

        if (OrderTypeEnum.TAKEAWAY.getCode() == bzOrderDO.getOrderType()) {
            return OrderSourceEnum.TAKE_OUT_ORDER.getCode();
        }

        // TODO 其他的后续判断
        return 0;
    }

    @Override
    public Page<BzOrderAppListRspVO> cOrderPage(BzOrderAppListReqVO reqVO) {
        Page<BzOrderAppListRspVO> returnPage = new Page<>();

        String openId = SecurityFrameworkUtils.getLoginOpenid();

        //构造入参
        BzOrderReqVO orderReqVO = new BzOrderReqVO();
        orderReqVO.setOpenId(openId);
        orderReqVO.setCreateTime(reqVO.getCreateTime());
        orderReqVO.setOrderStateHistory(reqVO.getOrderStateHistory());

        //分页信息
        Page<Object> pageInfo = PageUtils.getPageInfo();
        pageInfo.setCurrent(reqVO.getPageNo());
        pageInfo.setSize(reqVO.getPageSize());

        Page<BzOrderDO> pageResult = bzOrderMapper.orderPage(pageInfo, orderReqVO);
        List<BzOrderDO> bzOrderList = pageResult.getRecords();
        if (ObjectUtil.isEmpty(bzOrderList)) {
            return new Page<>();
        }

        List<String> orderSns = bzOrderList.stream().map(BzOrderDO::getOrderSn).collect(Collectors.toList());

        //商品信息
        List<BzOrderProductVO> bzOrderProductList = bzOrderProductService.getOrderProductList(orderSns, reqVO.getCreateTime());
        Map<String, List<BzOrderProductVO>> bzOrderProductMap = bzOrderProductList.stream().collect(Collectors.groupingBy(BzOrderProductVO::getOrderSn));

        //加购信息
        List<BzOrderPurchaseDO> bzOrderPurchaseList = bzOrderPurchaseService.getOrderPurchaseList(orderSns, reqVO.getCreateTime());
        Map<String, List<BzOrderPurchaseDO>> bzOrderPurchaseMap = bzOrderPurchaseList.stream().collect(Collectors.groupingBy(BzOrderPurchaseDO::getOrderSn));

        List<BzOrderAppListRspVO> reutrnList = new ArrayList<>();

        for (BzOrderDO i : bzOrderList) {
            BzOrderAppListRspVO bzOrderRspVO = new BzOrderAppListRspVO();
            BeanUtils.copyProperties(i, bzOrderRspVO);

            List<BzOrderProductVO> bzOrderProductVOS = bzOrderProductMap.get(i.getOrderSn());
            long goodsNum = 0L;
            if (!CollectionUtils.isEmpty(bzOrderProductVOS)) {
                for (BzOrderProductVO bzOrderProduct : bzOrderProductVOS) {
                    //商品数量累加
                    goodsNum = goodsNum + bzOrderProduct.getGoodsNum();
                    bzOrderRspVO.getImageList().add(bzOrderProduct.getGoodsImage());
                }
            }
            List<BzOrderPurchaseDO> bzOrderPurchases = bzOrderPurchaseMap.get(i.getOrderSn());

            //商品数量累加
            if (!CollectionUtils.isEmpty(bzOrderPurchases)) {
                goodsNum = goodsNum + bzOrderPurchases.size();
            }
            bzOrderRspVO.setGoodsNum(goodsNum);
            //设置订单类型名字
            bzOrderRspVO.setOrderTypeName(OrderTypeEnum.getMsgByCode(i.getOrderType()));
            bzOrderRspVO.setOrderStateName(OrderStateEnum.getMessageByCode(i.getOrderState()));
            bzOrderRspVO.setCreateTime(DateUtils.localDateTimeToString(i.getCreateTime(), DateUtils.YYYY_MM_DD_HH_MM_SS));

            reutrnList.add(bzOrderRspVO);
        }

        BeanUtils.copyProperties(pageResult, returnPage);
        returnPage.setRecords(reutrnList);
        return returnPage;
    }

    @Override
    public PageResult<BzOrderAppListRspVO> appOrderPage(OrderPageReqVO reqVO) {
        reqVO.setPageNo(ObjectUtils.isEmpty(reqVO.getPageNo()) ? 1 : reqVO.getPageNo());
        reqVO.setPageSize(ObjectUtils.isEmpty(reqVO.getPageSize()) ? 10 : reqVO.getPageSize());

        Long total = 0L;
        List<BzOrderAppListRspVO> returnList;

        if (ObjectUtils.isEmpty(reqVO.getStoreId()) && ObjectUtils.isEmpty(reqVO.getOrgId())) {
            return new PageResult<>();
        }

        if (!ObjectUtils.isEmpty(reqVO.getOrgId())) {
            reqVO.setStoreId(null);
            //组织
            Set<Long> storeIds = orgApi.getStoreIdListByOrgID(reqVO.getOrgId(), BusinessContextHolder.getBusinessId()).getData();
            if (ObjectUtils.isEmpty(storeIds)) {
                return new PageResult<>();
            }
            reqVO.setStoreIds(storeIds);
        }

        try {
            SearchRequest.Builder builder = this.getQuerybuilderForApp(reqVO);
            //优先查询ES
            Page<OrderResVO> pageResult = this.searchOrdersFromES(reqVO, builder);
            total = pageResult.getTotal();

            returnList = BeanCopyUtils.copyBeanList(pageResult.getRecords(), BzOrderAppListRspVO.class);
        } catch (Exception e) {
            log.warn("查询ES订单异常", e);
            //备用查询Mysql
            PageResult<BzOrderDO> pageResult = bzOrderMapper.selectPage(reqVO);
            total = pageResult.getTotal();

            //TODO 图片
            returnList = BeanCopyUtils.copyBeanList(pageResult.getList(), BzOrderAppListRspVO.class);
        }

        return new PageResult<>(returnList, total);
    }

    @Override
    public void exportOrderList(OrderPageReqVO pageReqVO, HttpServletRequest request, HttpServletResponse response) {
        Page<OrderExportVO> page = new Page<>(1, 2000);

        orderRespVOExcelActionService.exportAsyncExcel(OrderExportVO.class, page, this::getOrderExportList, pageReqVO, "订单列表", true);
    }

    /**
     * 订单列表导出数据查询
     *
     * @param reqVO
     * @param page
     * @return
     */
    public SearchAfterPage<OrderExportVO> getOrderExportList(OrderPageReqVO reqVO, Page<OrderExportVO> page) {
        //重新构建分页参数 page只作为中间传递用
        reqVO.setPageNo(Long.valueOf(page.getCurrent()).intValue());
        reqVO.setPageSize(Long.valueOf(page.getSize()).intValue());

        SearchAfterPage<OrderResVO> pageResult = this.pOrderpage(reqVO);
        List<OrderResVO> bzOrderList = pageResult.getList();

        List<OrderExportVO> list = bzOrderList.stream().map(i -> {
            OrderExportVO orderExportVO = new OrderExportVO();
            BeanUtils.copyProperties(i, orderExportVO);
            orderExportVO.setCreateTime(i.getCreateTime());
            orderExportVO.setOrderFrom(OrderFromEnum.getMessageByCode(i.getOrderFrom()));
            orderExportVO.setOrderType(OrderTypeEnum.getMsgByCode(i.getOrderType()));
            orderExportVO.setOrderState(OrderStateEnum.getMessageByCode(i.getOrderState()));
            orderExportVO.setPaymentCode(PaymentMethodEnum.getMsgByCode(Integer.parseInt(i.getPaymentCode())));

            return orderExportVO;
        }).toList();

        SearchAfterPage<OrderExportVO> orderExportVOSearchAfterPage = new SearchAfterPage<>(list, pageResult.getTotal());
        orderExportVOSearchAfterPage.setSearchAfter(pageResult.getSearchAfter());
        return orderExportVOSearchAfterPage;
    }

    @Override
    public Page<BzOrderListRspVO> orderPage(BzOrderReqVO reqVO) {
        Page<BzOrderListRspVO> returnPage = new Page<>();

        LocalDateTime[] createTimes = {reqVO.getStartTime(), reqVO.getEndTime()};
        reqVO.setCreateTime(createTimes);

        Page<Object> pageInfo = PageUtils.getPageInfo();
        pageInfo.setCurrent(reqVO.getPageNo());
        pageInfo.setSize(reqVO.getPageSize());

        Page<BzOrderDO> pageResult = bzOrderMapper.orderPage(pageInfo, reqVO);
        List<BzOrderDO> bzOrderList = pageResult.getRecords();

        if (CollectionUtils.isEmpty(bzOrderList)) {
            return new Page<>();
        }

        List<BzOrderListRspVO> reutrnList = new ArrayList<>();
        for (BzOrderDO i : bzOrderList) {
            BzOrderListRspVO orderDto = new BzOrderListRspVO();
            BeanUtils.copyProperties(i, orderDto);
            orderDto.setPaymentName(PaymentMethodEnum.getMsgByCode(Integer.parseInt(i.getPaymentCode())));
            orderDto.setOrderTypeName(OrderTypeEnum.getMsgByCode(i.getOrderType()));
            if (ObjectUtil.isNotEmpty(i.getOrderFrom())) {
                orderDto.setOrderFromName(OrderFromEnum.getMessageByCode(i.getOrderFrom()));
            }
            //添加架构商品
            orderDto.setCreateTime(DateUtils.localDateTimeToString(i.getCreateTime(), DateUtils.YYYY_MM_DD_HH_MM_SS));
            reutrnList.add(orderDto);
        }

        BeanUtils.copyProperties(pageResult, returnPage);
        returnPage.setRecords(reutrnList);
        return returnPage;
    }

    @Override
    public List<String> selectOrderByMemberIdAndTrhDay(Long memberId, Long storeId) {
        LocalDateTime today = LocalDateTime.now().plusDays(1);
        Integer[] state = new Integer[]{OrderStateEnum.CANCELED.getCode(), OrderStateEnum.PENDING_REFUND.getCode(), OrderStateEnum.UNPAID.getCode()};
        List<String> orderSnList = new ArrayList<>(bzOrderMapper.selectList(new LambdaQueryWrapperX<BzOrderDO>().notIn(BzOrderDO::getOrderState, state).eq(BzOrderDO::getMemberId, memberId).eq(BzOrderDO::getStoreId, storeId).between(BzOrderDO::getCreateTime, today.minusDays(3), today)).stream().map(BzOrderDO::getOrderSn).toList());
        if (ObjectUtil.isNotEmpty(orderSnList)) {
            List<String> sysComplaintList = compleplantApi.getComplaintListByMemberId(memberId).getData();
            if (ObjectUtil.isNotEmpty(sysComplaintList)) {
                orderSnList.removeAll(sysComplaintList); // 现在可以安全地移除
            }
        }
        return orderSnList;
    }

    @Override
    public List<OrderCountByOrderTypeDTO> selectCountByOrderType(BzOrderReqVO reqVO) {
        LocalDateTime[] createTimes = {reqVO.getStartTime(), reqVO.getEndTime()};
        reqVO.setCreateTime(createTimes);
        return bzOrderMapper.selectCountByOrderType(reqVO);
    }

    @Override
    public void changeToGetting(String orderSn) {
        BzOrderDO bzOrderDO = this.getBzOrderDO(orderSn);

        //推送消息
        AppletNoticePushVO appletNoticePush = new AppletNoticePushVO();
        //取餐消息通知
        appletNoticePush.setTemplateType(AppletPushTemplateTypeEnum.PLACE_ORDER_NOTICE.getCode());
        appletNoticePush.setOpenId(bzOrderDO.getOpenId());
        List<String> valueList = new ArrayList<>();
        valueList.add(bzOrderDO.getStoreName());
        valueList.add(bzOrderDO.getPickUpNum());
        wxActionApi.sendAppletNotice(valueList, appletNoticePush);
    }

    @Deprecated
    @Override
    public CalculateCacheDataDTO calculate(SettlementReqVO reqVO) {
        try {
            CalculateCacheDataDTO cacheDataDTO = priceCalculatorService.calculate(reqVO);
            //缓存
            calcCacheService.cacheCalculateData(cacheDataDTO);
            return cacheDataDTO;
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getClass().isNestmateOf(ServiceException.class)) {
                ServiceException ex = (ServiceException) e;
                throw new ServiceException(ex.getCode(), ex.getMessage());
            }
            throw new ServiceException(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
        }
    }

    @Override
    public CalculateCacheDataV2DTO calculateV2(SettlementReqV2VO reqVO) {
        try {
            CalculateCacheDataV2DTO cacheDataDTO = priceCalculatorV2Service.calculate(reqVO);
            //缓存
            calcCacheV2Service.cacheCalculateData(cacheDataDTO);
            return cacheDataDTO;
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getClass().isNestmateOf(ServiceException.class)) {
                ServiceException ex = (ServiceException) e;
                throw new ServiceException(ex.getCode(), ex.getMessage());
            }
            throw new ServiceException(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
        }
    }

    @Override
    public CalculateCacheDataV2DTO seckillCalculateV2(SettlementReqV2VO reqVO) {
        if (ObjectUtils.isEmpty(reqVO.getSeckillInfo())) {
            throw exception(ORDER_SECKILL_INFO_NOT_EXIST);
        }
        Set<ConstraintViolation<SettlementReqV2VO.SeckillInfo>> violations = validator.validate(reqVO.getSeckillInfo());

        if (violations == null) {
            violations = Collections.emptySet();
        }

        if (!violations.isEmpty()) {
            for (ConstraintViolation<SettlementReqV2VO.SeckillInfo> violation : violations) {
                throw new ServiceException(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(), violation.getMessage());
            }
        }

        try {
            //强制秒杀
            reqVO.getCommodityInfos().forEach(i -> i.setIsSeckill(true));
            CalculateCacheDataV2DTO cacheDataDTO = seckillCalculatorService.calculate(reqVO);

            //缓存
            calcCacheV2Service.cacheCalculateData(cacheDataDTO);
            return cacheDataDTO;
        } catch (Exception e) {
            log.warn("秒杀计算异常", e);
            if (e instanceof ServiceException ex) {
                throw new ServiceException(ex.getCode(), ex.getMessage());
            }
            throw new ServiceException(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
        }
    }

    @Override
    public SubmitResVO submitOrder(SubmitReqVO reqVO) {
        //主表，附表 统一使用创建时间
        reqVO.setCreateTime(LocalDateTime.now());

        IOrderSubmitStrategy<SubmitReqVO> strategy = strategyFactory.getStrategy(OrderSourceEnum.getOrderSourceEnumByCode(reqVO.getSource()));
        SubmitResVO result;
        try {
            result = strategy.submit(reqVO);
        } catch (Exception e) {
            e.printStackTrace();
            if (!e.getClass().isNestmateOf(ServiceException.class)) {
//                calcCacheService.setOrderToken(reqVO.getMemberId());
            }
            if (e.getClass().isNestmateOf(ServiceException.class)) {
                ServiceException ex = (ServiceException) e;
                if (!ex.getCode().equals(ErrorCodeConstants.ORDER_SUBMIT_REPEAT.getCode())) {
//                    calcCacheService.setOrderToken(reqVO.getMemberId());
                }
                throw new ServiceException(ex.getCode(), ex.getMessage());
            }
            throw new ServiceException(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
        }
        return result;
    }

    @Override
    public SubmitResVO seckillSubmit(SubmitReqVO reqVO) {
        return this.submitOrder(reqVO);
    }

    /**
     * 测试取餐码
     */
    public static void main1(String[] args) {
//        int THREAD_COUNT = 30;
//        Long TEST_STORE_ID = 12345L;
//        ExecutorService executorService = Executors.newFixedThreadPool(20);
//        Set<String> pickupCodes = new ConcurrentSkipListSet<>();
//
//        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
//
//
//        for (int i = 0; i < THREAD_COUNT; i++) {
//            executorService.submit(() -> {
//                try {
//                    for (int j = 0; j < 50; j++) {
//                        String code = this.getPickupCode(TEST_STORE_ID);
//                        pickupCodes.add(code);
//                    }
//
//                } catch (Exception e) {
//                    System.err.println("取码失败: " + e.getMessage());
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        try {
//            latch.await(); // 等待所有线程完成
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        executorService.shutdown();
//
//        System.out.println("------ 所有取餐码 ------");
//        pickupCodes.forEach(System.out::println);
//
//        System.out.println("总数: " + pickupCodes.size());
    }

    @Override
    public String getPickupCode(Long storeId) {
        // 格式化当前日期：yyyyMMdd，例如 20250612
        String date = LocalDate.now().toString().replace("-", "");
        String redisKey = String.format(PICKUP_CODE_KEY, storeId, date);

        // 自增编号
        Long number = stringRedisTemplate.opsForValue().increment(redisKey);
        if (number == null || number <= 0) {
            throw new IllegalStateException("返回编号无效");
        }

        // 第一次设置过期时间
        if (number == 1) {
            stringRedisTemplate.expire(redisKey, 25, TimeUnit.HOURS);
        }

        // 根据编号生成取餐码（如 A01, A99, B01, ...）
        return MealCodeGenerator.generatePickupCode(number);
    }

    @Override
    public String getWmPickupCode(Long storeId) {
        // 格式化当前日期：yyyyMMdd，例如 20250905
        String date = LocalDate.now().toString().replace("-", "");
        String redisKey = String.format(WM_PICKUP_CODE_KEY, storeId, date);

        // 自增编号
        Long number = stringRedisTemplate.opsForValue().increment(redisKey);
        if (number == null || number <= 0) {
            throw new IllegalStateException("返回编号无效");
        }

        // 第一次设置过期时间，保证每天独立（25小时比24小时多一点，避免跨时区或延迟问题）
        if (number == 1) {
            stringRedisTemplate.expire(redisKey, 25, TimeUnit.HOURS);
        }

        // 超过 999 后重置为 1
        if (number > 999) {
            stringRedisTemplate.opsForValue().set(redisKey, "1");
            number = 1L;
        }

        // 生成外卖单号：WM001 ~ WM999
        return String.format("WM%03d", number);
    }

    @Override
    public String getDqPickupCode(Long storeId) {
        // 格式化当前日期：yyyyMMdd，例如 20250905
        String date = LocalDate.now().toString().replace("-", "");
        String redisKey = String.format(DQ_PICKUP_CODE_KEY, storeId, date);

        // 自增编号
        Long number = stringRedisTemplate.opsForValue().increment(redisKey);
        if (number == null || number <= 0) {
            throw new IllegalStateException("返回编号无效");
        }

        // 第一次设置过期时间，保证每天独立（25小时比24小时多一点，避免跨时区或延迟问题）
        if (number == 1) {
            stringRedisTemplate.expire(redisKey, 25, TimeUnit.HOURS);
        }

        // 超过 999 后重置为 1
        if (number > 999) {
            stringRedisTemplate.opsForValue().set(redisKey, "1");
            number = 1L;
        }

        // 生成代取单号：DQ001 ~ DQ999
        return String.format("DQ%03d", number);
    }

    @Override
    public void updateOrderState(BzOrderDO bzOrderDO) {

        BzOrderLogDO bzOrderLogDO = new BzOrderLogDO();
        bzOrderLogDO.setOrderSn(bzOrderDO.getOrderSn());
        bzOrderLogDO.setOrderStateLog(this.getOrderLogState(bzOrderDO));
        bzOrderLogDO.setLogContent(this.getOrderLogContent(bzOrderDO));
        bzOrderLogDO.setLogTime(new Date());
        bzOrderLogDO.setLogUserId(bzOrderDO.getMemberId());
        bzOrderLogDO.setCreator(bzOrderDO.getCreator());
        bzOrderLogDO.setBusinessId(bzOrderDO.getBusinessId());
        bzOrderLogService.save(bzOrderLogDO);

        //秒杀单回退库存
        proxyService.rollbackSeckill(bzOrderDO);

        baseMapper.updateOrderState(bzOrderDO);
    }

    private Integer getOrderLogState(BzOrderDO order) {
        return order.getOrderState();
    }

    private String getOrderLogContent(BzOrderDO order) {
        return OrderStateEnum.getMessageByCode(order.getOrderState());
    }

    @Override
    public void writeOff(String orderSn, Long storeId) {
        if (ObjectUtil.isEmpty(orderSn) || !orderSn.contains("ORD")) {
            log.info("==> 核销单单号错误 | orderSn {}", orderSn);
            return;
        }

        BzOrderDO bzOrderDO = this.getBzOrderDO(orderSn);

        //禁止夸门店核销
        if (!bzOrderDO.getStoreId().equals(storeId)) {
            throw exception(ORDER_WRITE_OFF_ERROR);
        }

        //必须付款单
        if (OrderStateEnum.UNPAID.getCode() == bzOrderDO.getOrderState() || OrderStateEnum.CANCELED.getCode() == bzOrderDO.getOrderState() || OrderStateEnum.PENDING_REFUND.getCode() == bzOrderDO.getOrderState()) {
            throw exception(ORDER_WRONG_STATE);
        }

        bzOrderDO.setOrderState(OrderStateEnum.COMPLETED.getCode());

        this.updateOrderState(bzOrderDO);

    }

    @Override
    public void completed(String orderSn) {
        BzOrderDO bzOrderDO = this.getBzOrderDO(orderSn);

        //必须付款单
        if (OrderStateEnum.UNPAID.getCode() == bzOrderDO.getOrderState() || OrderStateEnum.CANCELED.getCode() == bzOrderDO.getOrderState() || OrderStateEnum.PENDING_REFUND.getCode() == bzOrderDO.getOrderState()) {
            throw exception(ORDER_WRONG_STATE);
        }

        bzOrderDO.setOrderState(OrderStateEnum.COMPLETED.getCode());
        bzOrderDO.setFinishTime(LocalDateTime.now());

        this.updateOrderState(bzOrderDO);
    }

    @Override
    public void callNumber(String orderSn) {
        BzOrderDO bzOrderDO = this.getBzOrderDO(orderSn);

        //非外卖单
        if (OrderTypeEnum.TAKEAWAY.getCode() == bzOrderDO.getOrderType()) {
            throw exception(ORDER_WRONG_TYPE);
        }

        //必须是待取餐
        if (OrderStateEnum.MAKING.getCode() != bzOrderDO.getOrderState()) {
            throw exception(ORDER_WRONG_STATE);
        }

        bzOrderDO.setOrderState(OrderStateEnum.W_TO_BE_PICKED_UP.getCode());

        this.updateOrderState(bzOrderDO);
    }

    @Override
    public void receiptScan(String orderSn) {

        if (ObjectUtil.isEmpty(orderSn) || !orderSn.contains("ORD")) {
            log.info("==> 小票扫码单号错误 | orderSn {}", orderSn);
            return;
        }

        BzOrderDO bzOrderDO = this.getBzOrderDO(orderSn);

        //非外卖单
        if (OrderTypeEnum.TAKEAWAY.getCode() == bzOrderDO.getOrderType()) {
            throw exception(ORDER_WRONG_TYPE);
        }

        if (OrderStateEnum.PENDING_REFUND.getCode() == bzOrderDO.getOrderState()) {
            return;
        }

        //必须是待取餐
        if (OrderStateEnum.MAKING.getCode() == bzOrderDO.getOrderState()) {
            bzOrderDO.setOrderState(OrderStateEnum.W_TO_BE_PICKED_UP.getCode());
            this.updateOrderState(bzOrderDO);
        }

        //发消息
        rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, bzOrderDO.getStoreId()), "", "#" + bzOrderDO.getPickUpNum() + "#");
    }

    @Override
    public void delivery(String orderSn) {
        BzOrderDO bzOrderDO = this.getBzOrderDO(orderSn);

        //外卖单
        if (OrderTypeEnum.TAKEAWAY.getCode() != bzOrderDO.getOrderType()) {
            throw exception(ORDER_WRONG_TYPE);
        }

        //必须是待配送
        if (OrderStateEnum.W_TO_BE_DELIVERED.getCode() != bzOrderDO.getOrderState()) {
            throw exception(ORDER_WRONG_STATE);
        }

        bzOrderDO.setOrderState(OrderStateEnum.DELIVERED.getCode());

        this.updateOrderState(bzOrderDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptErrandOrder(String orderSn) {
        ErrandRunnerDTO runner = this.getCurrentAvailableErrandRunner();
        this.executeWithErrandAcceptLock(orderSn, () -> doAcceptErrandOrder(orderSn, runner));
    }

    private void doAcceptErrandOrder(String orderSn, ErrandRunnerDTO runner) {
        BzOrderDO order = this.getBzOrderDO(orderSn);
        if (!Objects.equals(OrderTypeEnum.ERRAND.getCode(), order.getOrderType())) {
            throw exception(ORDER_WRONG_TYPE);
        }
        if (order.getErrandGenderLimit() != null
                && order.getErrandGenderLimit() != 0
                && !Objects.equals(order.getErrandGenderLimit(), runner.getGender())) {
            throw exception(ORDER_ERRAND_ACCEPT_GENDER_LIMIT);
        }
        validateCanAcceptErrandOrder(order);

        BzOrderDO update = new BzOrderDO();
        update.setOrderId(order.getOrderId());
        update.setOrderSn(order.getOrderSn());
        update.setCreateTime(order.getCreateTime());
        update.setDeliveryId(runner.getMemberId());
        update.setDeliveryName(runner.getName());
        update.setDeliveryPhone(runner.getPhone());
        update.setOrderState(OrderStateEnum.ACCEPTED.getCode());
        update.setMakingTime(LocalDateTime.now());

        int updated = baseMapper.acceptErrandOrder(update, OrderStateEnum.WAITING_ACCEPT.getCode());
        if (updated != 1) {
            validateCanAcceptErrandOrder(this.getBzOrderDO(orderSn));
            throw exception(ORDER_ERRAND_ACCEPTED_BY_OTHER);
        }

        BzOrderLogDO bzOrderLogDO = new BzOrderLogDO();
        bzOrderLogDO.setOrderSn(order.getOrderSn());
        bzOrderLogDO.setOrderStateLog(OrderStateEnum.ACCEPTED.getCode());
        bzOrderLogDO.setLogContent("跑腿员已接单：" + runner.getName());
        bzOrderLogDO.setLogTime(new Date());
        bzOrderLogDO.setLogUserId(runner.getMemberId());
        bzOrderLogDO.setLogUserName(runner.getName());
        bzOrderLogDO.setCreator(order.getCreator());
        bzOrderLogDO.setBusinessId(order.getBusinessId());
        bzOrderLogService.save(bzOrderLogDO);

        order.setDeliveryId(update.getDeliveryId());
        order.setDeliveryName(update.getDeliveryName());
        order.setDeliveryPhone(update.getDeliveryPhone());
        order.setOrderState(update.getOrderState());
        order.setMakingTime(update.getMakingTime());
        this.sendErrandPrinterAfterAcceptAfterCommit(order);
    }

    private void validateCanAcceptErrandOrder(BzOrderDO order) {
        if (!ObjectUtils.isEmpty(order.getDeliveryId())) {
            throw exception(ORDER_ERRAND_ACCEPTED_BY_OTHER);
        }
        if (Objects.equals(order.getOrderState(), OrderStateEnum.WAITING_ACCEPT.getCode())) {
            return;
        }
        if (Objects.equals(order.getOrderState(), OrderStateEnum.CANCELED.getCode())) {
            throw exception(ORDER_ERRAND_ACCEPT_CANCELED);
        }
        if (Objects.equals(order.getOrderState(), OrderStateEnum.PENDING_REFUND.getCode())) {
            throw exception(ORDER_ERRAND_ACCEPT_REFUNDED);
        }
        if (Objects.equals(order.getOrderState(), OrderStateEnum.ACCEPTED.getCode())) {
            throw exception(ORDER_ERRAND_ACCEPTED_BY_OTHER);
        }
        String orderStateMessage = OrderStateEnum.getMessageByCode(order.getOrderState());
        throw exception(ORDER_ERRAND_ACCEPT_STATE_NOT_SUPPORT,
                StringUtils.isBlank(orderStateMessage) ? "当前状态" : orderStateMessage);
    }

    private void sendErrandPrinterAfterAccept(BzOrderDO order) {
        try {
            OrderDetailDTO detail = this.getDetail(order.getOrderSn());
            this.sendErrandPrinterAfterAccept(order, detail);
        } catch (Exception e) {
            log.error("==> 代取订单接单后打印小票失败，orderSn={}", order.getOrderSn(), e);
        }
    }

    private void sendErrandPrinterAfterAccept(BzOrderDO order, OrderDetailDTO detail) {
        try {
            bzOrderPayService.sendErrandPrinter(order, detail.getProductDOList(), detail.getProductSonDOList(), detail.getPurchaseDOList());
        } catch (Exception e) {
            log.error("==> 代取订单接单后打印小票失败，orderSn={}", order.getOrderSn(), e);
        }
    }

    private void sendErrandPrinterAfterAcceptAfterCommit(BzOrderDO order) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            this.submitErrandPrinterAfterAccept(order);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                submitErrandPrinterAfterAccept(order);
            }
        });
    }

    private void submitErrandPrinterAfterAccept(BzOrderDO order) {
        try {
            OrderDetailDTO detail = this.getDetail(order.getOrderSn());
            ioExecutor.submit(() -> sendErrandPrinterAfterAccept(order, detail));
        } catch (Exception e) {
            log.error("==> 代取订单接单后提交打印任务失败，orderSn={}", order.getOrderSn(), e);
        }
    }

    private void executeWithErrandAcceptLock(String orderSn, Runnable action) {
        String lockKey = String.format(ERRAND_ACCEPT_LOCK_KEY, orderSn);
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, String.valueOf(System.currentTimeMillis()), ERRAND_ACCEPT_LOCK_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            throw exception(ORDER_ERRAND_ACCEPT_LOCK_BUSY);
        }
        try {
            action.run();
        } finally {
            this.releaseErrandAcceptLockAfterTransaction(lockKey);
        }
    }

    private void releaseErrandAcceptLockAfterTransaction(String lockKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            stringRedisTemplate.delete(lockKey);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                stringRedisTemplate.delete(lockKey);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pickupErrandOrder(String orderSn) {
        ErrandRunnerDTO runner = this.getCurrentErrandRunner();
        BzOrderDO order = this.getBzOrderDO(orderSn);
        this.validateErrandRunnerOrder(order, runner);
        validateCanPickupErrandOrder(order);

        BzOrderDO update = new BzOrderDO();
        update.setOrderId(order.getOrderId());
        update.setOrderSn(order.getOrderSn());
        update.setCreateTime(order.getCreateTime());
        update.setOrderState(OrderStateEnum.DELIVERED.getCode());
        update.setWaitingTime(LocalDateTime.now());

        int updated = this.updateErrandOrderStatus(update, runner.getMemberId(), OrderStateEnum.ACCEPTED);
        if (updated != 1) {
            validateCanPickupErrandOrder(this.getBzOrderDO(orderSn));
            throw exception(ORDER_ERRAND_PICKUP_STATE_NOT_SUPPORT, "当前状态");
        }
        this.saveErrandOrderLog(order, runner, OrderStateEnum.DELIVERED.getCode(), "跑腿员已取货：" + runner.getName());
        this.updateErrandPickupOrderStateEsAfterCommit(order);
    }

    private void updateErrandPickupOrderStateEsAfterCommit(BzOrderDO order) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            this.updateErrandPickupOrderStateEs(order);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                updateErrandPickupOrderStateEs(order);
            }
        });
    }

    private void updateErrandPickupOrderStateEs(BzOrderDO order) {
        if (order == null || order.getOrderId() == null) {
            return;
        }
        try {
            String indexName = BzOrderDocument.class.getAnnotation(Document.class).indexName();
            Map<String, Object> doc = new HashMap<>();
            doc.put("orderState", OrderStateEnum.DELIVERED.getCode());
            UpdateResponse<BzOrderDocument> response = elasticsearchClient.update(u -> u
                            .index(indexName)
                            .id(String.valueOf(order.getOrderId()))
                            .doc(doc)
                            .refresh(Refresh.True),
                    BzOrderDocument.class);
            log.info("==> 代取订单已取货后同步ES状态完成 | orderSn={}, orderId={}, result={}",
                    order.getOrderSn(), order.getOrderId(), response.result());
        } catch (Exception e) {
            log.warn("==> 代取订单已取货后同步ES状态失败，等待canal兜底 | orderSn={}, orderId={}",
                    order.getOrderSn(), order.getOrderId(), e);
        }
    }

    private void validateCanPickupErrandOrder(BzOrderDO order) {
        if (Objects.equals(order.getOrderState(), OrderStateEnum.ACCEPTED.getCode())) {
            return;
        }
        if (Objects.equals(order.getOrderState(), OrderStateEnum.CANCELED.getCode())) {
            throw exception(ORDER_ERRAND_PICKUP_CANCELED);
        }
        if (Objects.equals(order.getOrderState(), OrderStateEnum.PENDING_REFUND.getCode())) {
            throw exception(ORDER_ERRAND_PICKUP_REFUNDED);
        }
        String orderStateMessage = OrderStateEnum.getMessageByCode(order.getOrderState());
        throw exception(ORDER_ERRAND_PICKUP_STATE_NOT_SUPPORT,
                StringUtils.isBlank(orderStateMessage) ? "当前状态" : orderStateMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliveredErrandOrder(String orderSn, List<String> deliveryImages) {
        ErrandRunnerDTO runner = this.getCurrentErrandRunner();
        BzOrderDO order = this.getBzOrderDO(orderSn);
        this.validateErrandRunnerOrder(order, runner);
        validateCanDeliveredErrandOrder(order);

        String normalizedImages = this.validateAndNormalizeDeliveryImages(deliveryImages);
        BzOrderDO update = new BzOrderDO();
        update.setOrderId(order.getOrderId());
        update.setOrderSn(order.getOrderSn());
        update.setCreateTime(order.getCreateTime());
        update.setOrderState(OrderStateEnum.COMPLETED.getCode());
        update.setErrandDeliveryImages(normalizedImages);
        update.setFinishTime(LocalDateTime.now());

        int updated = this.updateErrandOrderStatus(update, runner.getMemberId(), OrderStateEnum.DELIVERED);
        if (updated != 1) {
            validateCanDeliveredErrandOrder(this.getBzOrderDO(orderSn));
            throw exception(ORDER_ERRAND_DELIVERED_STATE_NOT_SUPPORT, "当前状态");
        }

        this.saveErrandOrderLog(order, runner, OrderStateEnum.COMPLETED.getCode(), "跑腿员已送达：" + runner.getName());
        this.handleErrandDeliveredReward(order, runner);
    }

    private void validateCanDeliveredErrandOrder(BzOrderDO order) {
        if (Objects.equals(order.getOrderState(), OrderStateEnum.DELIVERED.getCode())) {
            return;
        }
        if (Objects.equals(order.getOrderState(), OrderStateEnum.CANCELED.getCode())) {
            throw exception(ORDER_ERRAND_DELIVERED_CANCELED);
        }
        if (Objects.equals(order.getOrderState(), OrderStateEnum.PENDING_REFUND.getCode())) {
            throw exception(ORDER_ERRAND_DELIVERED_REFUNDED);
        }
        String orderStateMessage = OrderStateEnum.getMessageByCode(order.getOrderState());
        throw exception(ORDER_ERRAND_DELIVERED_STATE_NOT_SUPPORT,
                StringUtils.isBlank(orderStateMessage) ? "当前状态" : orderStateMessage);
    }

    @Override
    public ErrandOrderHallPageRespVO errandOrderHall(ErrandOrderHallReqVO reqVO) {
        if (reqVO == null || ObjectUtils.isEmpty(reqVO.getStoreId())) {
            throw new ServiceException(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "门店ID不能为空");
        }
        Long memberId = WebFrameworkUtils.getLoginUserId();
        boolean showAddress = this.canShowErrandAddressWithCache(memberId);
        int pageSize = this.getErrandHallPageSize(reqVO);

        SearchRequest request = SearchRequest.of(s -> {
            s.index(BzOrderDocument.class.getAnnotation(Document.class).indexName())
                    .source(source -> source.filter(filter -> filter.includes(ERRAND_ORDER_LIST_SOURCE_INCLUDES)))
                    .size(pageSize)
                    .query(q -> q.bool(boolQuery -> {
                        boolQuery.must(TermQuery.of(field -> field.field("storeId").value(reqVO.getStoreId()))._toQuery());
                        boolQuery.must(TermQuery.of(field -> field.field("orderType").value(OrderTypeEnum.ERRAND.getCode()))._toQuery());
                        boolQuery.must(TermQuery.of(field -> field.field("orderState").value(OrderStateEnum.WAITING_ACCEPT.getCode()))._toQuery());
                        if (!ObjectUtils.isEmpty(memberId)) {
                            boolQuery.mustNot(TermQuery.of(field -> field.field("memberId").value(memberId))._toQuery());
                        }
                        return boolQuery;
                    }))
                    .sort(sort -> sort.field(field -> field.field("createTime").order(SortOrder.Desc)))
                    .sort(sort -> sort.field(field -> field.field("orderId").order(SortOrder.Desc)));
            List<FieldValue> searchAfter = this.buildErrandSearchAfter(reqVO.getSearchAfter());
            if (CollUtil.isNotEmpty(searchAfter)) {
                s.searchAfter(searchAfter);
            }
            return s;
        });

        try {
            SearchResponse<BzOrderDocument> search = elasticsearchClient.search(request, BzOrderDocument.class);
            if (search.hits() == null || CollUtil.isEmpty(search.hits().hits())) {
                return ErrandOrderHallPageRespVO.empty();
            }
            List<ErrandOrderHallRespVO> list = new ArrayList<>();
            for (Hit<BzOrderDocument> hit : search.hits().hits()) {
                BzOrderDocument order = hit.source();
                if (order == null) {
                    continue;
                }
                list.add(this.buildErrandOrderResp(order, showAddress));
            }
            ErrandOrderHallPageRespVO result = new ErrandOrderHallPageRespVO(list);
            if (search.hits().hits().size() >= pageSize) {
                result.setSearchAfter(this.unwrapErrandSearchAfter(search.hits().hits().get(search.hits().hits().size() - 1).sort()));
            }
            return result;
        } catch (IOException e) {
            log.warn("==> 查询跑腿接单大厅ES异常 | storeId {}", reqVO.getStoreId(), e);
            return ErrandOrderHallPageRespVO.empty();
        }
    }

    private int getErrandHallPageSize(ErrandOrderHallReqVO reqVO) {
        if (reqVO == null || reqVO.getPageSize() == null || reqVO.getPageSize() <= 0) {
            return ERRAND_HALL_DEFAULT_PAGE_SIZE;
        }
        return Math.min(reqVO.getPageSize(), ERRAND_HALL_MAX_PAGE_SIZE);
    }

    private List<FieldValue> buildErrandSearchAfter(List<Object> searchAfter) {
        if (CollUtil.isEmpty(searchAfter)) {
            return Collections.emptyList();
        }
        return searchAfter.stream()
                .filter(Objects::nonNull)
                .map(this::toFieldValue)
                .collect(Collectors.toList());
    }

    private FieldValue toFieldValue(Object value) {
        if (value instanceof Integer || value instanceof Long) {
            return FieldValue.of(((Number) value).longValue());
        }
        if (value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
            return FieldValue.of(((Number) value).doubleValue());
        }
        if (value instanceof Boolean) {
            return FieldValue.of((Boolean) value);
        }
        return FieldValue.of(String.valueOf(value));
    }

    private List<Object> unwrapErrandSearchAfter(List<FieldValue> searchAfter) {
        if (CollUtil.isEmpty(searchAfter)) {
            return Collections.emptyList();
        }
        return searchAfter.stream()
                .map(this::toSearchAfterValue)
                .collect(Collectors.toList());
    }

    private Object toSearchAfterValue(FieldValue value) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isLong()) {
            return value.longValue();
        }
        if (value.isDouble()) {
            return value.doubleValue();
        }
        if (value.isBoolean()) {
            return value.booleanValue();
        }
        if (value.isString()) {
            return value.stringValue();
        }
        return value.toString();
    }

    @Override
    public ErrandOrderHallPageRespVO myErrandDeliveryOrders(ErrandOrderHallReqVO reqVO) {
        Long memberId = WebFrameworkUtils.getLoginUserId();
        if (ObjectUtils.isEmpty(memberId)) {
            throw new ServiceException(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "用户未登录");
        }
        int pageSize = this.getErrandHallPageSize(reqVO);

//        ErrandRunnerDTO runner = errandRunnerApi.getRunnerByMemberId(memberId).getCheckedData();
//        if (runner == null) {
//            return ErrandOrderHallPageRespVO.empty();
//        }

        SearchRequest request = SearchRequest.of(s -> {
            s.index(BzOrderDocument.class.getAnnotation(Document.class).indexName())
                    .source(source -> source.filter(filter -> filter.includes(ERRAND_ORDER_LIST_SOURCE_INCLUDES)))
                    .size(pageSize)
                    .query(q -> q.bool(boolQuery -> {
                        boolQuery.must(TermQuery.of(field -> field.field("orderType").value(OrderTypeEnum.ERRAND.getCode()))._toQuery());
                        boolQuery.must(TermQuery.of(field -> field.field("deliveryId").value(memberId))._toQuery());
                        boolQuery.mustNot(TermQuery.of(field -> field.field("memberId").value(memberId))._toQuery());
                        if (reqVO != null && reqVO.getOrderState() != null) {
                            boolQuery.must(TermQuery.of(field -> field.field("orderState").value(reqVO.getOrderState()))._toQuery());
                        }
                        return boolQuery;
                    }))
                    .sort(sort -> sort.field(field -> field.field("createTime").order(SortOrder.Desc)))
                    .sort(sort -> sort.field(field -> field.field("orderId").order(SortOrder.Desc)));
            List<FieldValue> searchAfter = reqVO == null ? Collections.emptyList() : this.buildErrandSearchAfter(reqVO.getSearchAfter());
            if (CollUtil.isNotEmpty(searchAfter)) {
                s.searchAfter(searchAfter);
            }
            return s;
        });

        try {
            SearchResponse<BzOrderDocument> search = elasticsearchClient.search(request, BzOrderDocument.class);
            if (search.hits() == null || CollUtil.isEmpty(search.hits().hits())) {
                return ErrandOrderHallPageRespVO.empty();
            }
            List<ErrandOrderHallRespVO> list = new ArrayList<>();
            for (Hit<BzOrderDocument> hit : search.hits().hits()) {
                BzOrderDocument order = hit.source();
                if (order == null) {
                    continue;
                }
                list.add(this.buildErrandOrderResp(order, true));
            }
            ErrandOrderHallPageRespVO result = new ErrandOrderHallPageRespVO(list);
            if (search.hits().hits().size() >= pageSize) {
                result.setSearchAfter(this.unwrapErrandSearchAfter(search.hits().hits().get(search.hits().hits().size() - 1).sort()));
            }
            return result;
        } catch (IOException e) {
            log.warn("==> 查询我的跑腿配送订单ES异常 | memberId {}", memberId, e);
            return ErrandOrderHallPageRespVO.empty();
        }
    }

    /**
     * 检查跑腿员是否有未完结订单
     *
     * @param memberId 会员ID（跑腿员对应的用户ID）
     * @return true-有未完结订单，false-没有未完结订单
     */
    @Override
    public boolean checkRunnerHasUnfinishedOrders(Long memberId) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(BzOrderDocument.class.getAnnotation(Document.class).indexName())
                    .size(1)
                    .query(q -> q.bool(boolQuery -> {
                        boolQuery.must(TermQuery.of(field -> field.field("orderType").value(OrderTypeEnum.ERRAND.getCode()))._toQuery());
                        boolQuery.must(TermQuery.of(field -> field.field("deliveryId").value(memberId))._toQuery());
                        boolQuery.mustNot(TermQuery.of(field -> field.field("memberId").value(memberId))._toQuery());

                        // 查询 orderState 不在 [0, 10, 60, 70] 中的订单（即未完结订单）
                        boolQuery.mustNot(TermsQuery.of(t -> t
                                .field("orderState")
                                .terms(tv -> tv.value(List.of(
                                        FieldValue.of(0),
                                        FieldValue.of(10),
                                        FieldValue.of(60),
                                        FieldValue.of(70)
                                )))
                        )._toQuery());
                        return boolQuery;
                    }))
            );

            SearchResponse<BzOrderDocument> search = elasticsearchClient.search(request, BzOrderDocument.class);
            return search.hits() != null && search.hits().hits() != null && !search.hits().hits().isEmpty();
        } catch (IOException e) {
            log.warn("查询跑腿员未完结订单ES异常 | memberId {}", memberId, e);
            return true;
        }
    }


    private ErrandOrderHallRespVO buildErrandOrderResp(BzOrderDocument order, boolean showAddress) {
        ErrandOrderHallRespVO respVO = new ErrandOrderHallRespVO();
        respVO.setOrderSn(order.getOrderSn());
        respVO.setMemberAvatar(order.getMemberAvatar());
        respVO.setAppointmentTime(order.getAppointmentTime());
        respVO.setOrderState(order.getOrderState());
        respVO.setReceiverAreaInfo(showAddress ? order.getReceiverAreaInfo() : this.maskErrandAddress(order.getReceiverAreaInfo()));
        respVO.setReceiverAddress(showAddress ? order.getReceiverAddress() : this.maskErrandAddress(order.getReceiverAddress()));
        respVO.setOrderRemark(order.getOrderRemark());
        BigDecimal errandRewardAmount = this.safeAmount(order.getErrandRewardAmount());
        BigDecimal errandStoreSubsidyAmount = this.safeAmount(order.getErrandStoreSubsidyAmount());
        respVO.setErrandRewardAmount(errandRewardAmount);
        respVO.setErrandStoreSubsidyAmount(errandStoreSubsidyAmount);
        respVO.setRewardAmount(errandRewardAmount.add(errandStoreSubsidyAmount));
        respVO.setDeliveryName(order.getDeliveryName());
        respVO.setDeliveryPhone(order.getDeliveryPhone());
        respVO.setStorePhone(order.getStorePhone());
        respVO.setTakeAwayTel(order.getTakeAwayTel());
        respVO.setReceiverMobile(order.getReceiverMobile());
        respVO.setGoodsNum(this.sumErrandOrderGoodsNum(order));
        return respVO;
    }

    private Integer sumErrandOrderGoodsNum(BzOrderDocument order) {
        if (order == null) {
            return 0;
        }
        if (order.getTableWareNum() != null && order.getTableWareNum() > 0) {
            return order.getTableWareNum();
        }
        if (CollUtil.isEmpty(order.getProducts())) {
            return 0;
        }
        return order.getProducts().stream()
                .map(BzOrderDocument.ProductInfo::getGoodsNum)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private ErrandRunnerDTO getCurrentAvailableErrandRunner() {
        Long runnerMemberId = WebFrameworkUtils.getLoginUserId();
        if (ObjectUtils.isEmpty(runnerMemberId)) {
            throw new ServiceException(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "用户未登录");
        }

        ErrandRunnerDTO runner = errandRunnerApi.getAvailableRunnerByMemberId(runnerMemberId).getCheckedData();
        if (runner == null) {
            throw new ServiceException(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "跑腿员不存在、未审核通过或已封禁");
        }
        return runner;
    }

    private ErrandRunnerDTO getCurrentErrandRunner() {
        Long runnerMemberId = WebFrameworkUtils.getLoginUserId();
        if (ObjectUtils.isEmpty(runnerMemberId)) {
            throw new ServiceException(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "用户未登录");
        }

        ErrandRunnerDTO runner = errandRunnerApi.getRunnerByMemberId(runnerMemberId).getCheckedData();
        if (runner == null) {
            throw new ServiceException(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "跑腿员不存在");
        }
        return runner;
    }

    private boolean canShowErrandAddress(Long memberId) {
        if (ObjectUtils.isEmpty(memberId)) {
            return false;
        }
        ErrandRunnerDTO runner = errandRunnerApi.getRunnerByMemberId(memberId).getCheckedData();
        return runner != null
                && Objects.equals(runner.getFirstAuditStatus(), ERRAND_RUNNER_FIRST_AUDIT_APPROVED)
                && !Objects.equals(runner.getBanStatus(), ERRAND_RUNNER_BAN_STATUS_BANNED);
    }

    private boolean canShowErrandAddressWithCache(Long memberId) {
        if (ObjectUtils.isEmpty(memberId)) {
            return false;
        }
        String cacheKey = String.format(ERRAND_HALL_ADDRESS_VISIBLE_CACHE_KEY, memberId);
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.isNotBlank(cached)) {
                return Boolean.parseBoolean(cached);
            }
            boolean canShow = this.canShowErrandAddress(memberId);
            stringRedisTemplate.opsForValue().set(cacheKey, String.valueOf(canShow),
                    ERRAND_HALL_ADDRESS_VISIBLE_CACHE_SECONDS, TimeUnit.SECONDS);
            return canShow;
        } catch (Exception e) {
            log.warn("==> 查询跑腿大厅地址展示权限缓存异常 | memberId {}", memberId, e);
            return this.canShowErrandAddress(memberId);
        }
    }

    private String maskErrandAddress(String address) {
        return StringUtils.isBlank(address) ? address : "****";
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private void handleErrandDeliveredReward(BzOrderDO order, ErrandRunnerDTO runner) {
        BigDecimal rewardAmount = this.safeAmount(order.getErrandRewardAmount());
        if (rewardAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        xingYiPayService.xingYiErrandRewardSplit(order);
        Boolean incomeResult = errandRunnerApi.createRewardIncome(runner.getMemberId(), order.getOrderSn(), rewardAmount).getCheckedData();
        if (!Boolean.TRUE.equals(incomeResult)) {
            throw new ServiceException(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(), "跑腿赏金入账失败");
        }
    }

    private void validateErrandRunnerOrder(BzOrderDO order, ErrandRunnerDTO runner) {
        if (!Objects.equals(OrderTypeEnum.ERRAND.getCode(), order.getOrderType())) {
            throw exception(ORDER_WRONG_TYPE);
        }
        if (!Objects.equals(order.getDeliveryId(), runner.getMemberId())) {
            throw exception(ORDER_WRONG_STATE);
        }
    }

    private int updateErrandOrderStatus(BzOrderDO update, Long deliveryId, OrderStateEnum expectedOrderState) {
        return baseMapper.updateErrandOrderStatus(update, deliveryId, expectedOrderState.getCode());
    }

    private String validateAndNormalizeDeliveryImages(List<String> deliveryImages) {
        if (CollectionUtils.isEmpty(deliveryImages)) {
            throw new ServiceException(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "送达照片不能为空");
        }
        List<String> images = deliveryImages.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (images.isEmpty() || images.size() > 3) {
            throw new ServiceException(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "送达照片最多上传3张");
        }
        return String.join(",", images);
    }

    private void saveErrandOrderLog(BzOrderDO order, ErrandRunnerDTO runner, String logContent) {
        this.saveErrandOrderLog(order, runner, order.getOrderState(), logContent);
    }

    private void saveErrandOrderLog(BzOrderDO order, ErrandRunnerDTO runner, Integer orderStateLog, String logContent) {
        BzOrderLogDO bzOrderLogDO = new BzOrderLogDO();
        bzOrderLogDO.setOrderSn(order.getOrderSn());
        bzOrderLogDO.setOrderStateLog(orderStateLog);
        bzOrderLogDO.setLogContent(logContent);
        bzOrderLogDO.setLogTime(new Date());
        bzOrderLogDO.setLogUserId(runner.getMemberId());
        bzOrderLogDO.setLogUserName(runner.getName());
        bzOrderLogDO.setCreator(order.getCreator());
        bzOrderLogDO.setBusinessId(order.getBusinessId());
        bzOrderLogService.save(bzOrderLogDO);
    }

    @Override
    public void cannel(String orderSn) {
        BzOrderDO bzOrderDO = this.getBzOrderDO(orderSn);

        if (this.canRefundWaitingAcceptErrandOrder(bzOrderDO)) {
            bzOrderPayService.refund(buildOrderPayReqVO(orderSn));
            return;
        }

        if (OrderStateEnum.UNPAID.getCode() != bzOrderDO.getOrderState()) {
            throwCancelWrongStateException(bzOrderDO.getOrderState());
        }

        String code = null;
        try {
            CommonResult<StoreDTO> storeResult = storeApi.getStoreByStoreId(bzOrderDO.getStoreId());
            StoreDTO storeDTO = storeResult.getData();
            String terminalSn = Objects.equals(OrderTypeEnum.ERRAND.getCode(), bzOrderDO.getOrderType())
                    ? storeDTO.getTerminalKey()
                    : storeDTO.getTerminalSn();
            XingYiPayReqVO reqVO = XingYiPayReqVO.builder()
                    .paySn(bzOrderDO.getPaySn())
                    .createTime(bzOrderDO.getCreateTime())
                    .terminalSn(terminalSn)
                    .build();
            code = xingYiPayService.xingYiQuery(reqVO);
        } catch (Exception e) {
            log.warn("==> 【订单取消】xingYiQuery异常 {}", e.getMessage());
        }
        if ("000000".equals(code)) {
            throw exception(ORDER_PAYING_000000);
        }

        bzOrderDO.setOrderState(OrderStateEnum.CANCELED.getCode());

        this.updateOrderState(bzOrderDO);

        if (!Objects.equals(bzOrderDO.getOrderFrom(), OrderFromEnum.POINT_SINGLE_MACHINE.getCode())) {
            try {
                activityApi.releaseMzGiftInventory(bzOrderDO.getOrderSn());
            } catch (Exception e) {
                log.error("==> 【订单取消】释放满赠库存失败 orderSn={}", bzOrderDO.getOrderSn(), e);
            }
        }

        if (ObjectUtil.isNotEmpty(bzOrderDO.getUserCouponId())) {
            //超时回退优惠券
            userCouponApi.usedCoupon(UsedCouponReqVO.builder().userCouponId(bzOrderDO.getUserCouponId()).memberId(bzOrderDO.getMemberId()).isUsed(OrderConstants.NO).isRefundAction(OrderConstants.NO).build());
        }

        try {
            log.info("==> 【订单取消】kafka发消息 {}", bzOrderDO.getOrderSn());
            this.notifyOrder(bzOrderDO.getOrderSn(), "DELETE");
        } catch (Exception e) {
            log.info("==> 【订单取消】kafka发消息 失败 {}", bzOrderDO.getOrderSn(), e);
        }
    }

    private void throwCancelWrongStateException(Integer orderState) {
        String orderStateMessage = orderState == null ? null : OrderStateEnum.getMessageByCode(orderState);
        if (ObjectUtil.isEmpty(orderStateMessage)) {
            orderStateMessage = orderState == null ? "状态未知" : "状态码" + orderState;
        }
        throw exception(ORDER_WRONG_STATE.getCode(), "订单" + orderStateMessage + "，不能取消订单");
    }

    private boolean canRefundWaitingAcceptErrandOrder(BzOrderDO order) {
        return Objects.equals(OrderTypeEnum.ERRAND.getCode(), order.getOrderType())
                && Objects.equals(OrderStateEnum.WAITING_ACCEPT.getCode(), order.getOrderState());
    }

    private OrderPayReqVO buildOrderPayReqVO(String orderSn) {
        OrderPayReqVO reqVO = new OrderPayReqVO();
        reqVO.setOrderSn(orderSn);
        return reqVO;
    }

    /**
     * 获取订单
     *
     * @param orderSn
     * @return
     */
    @Override
    public BzOrderDO getBzOrderDO(String orderSn) {
        BzOrderDO bzOrderDO = this.getOne(new LambdaQueryWrapper<BzOrderDO>().eq(BzOrderDO::getOrderSn, orderSn));

        if (ObjectUtils.isEmpty(bzOrderDO)) {
            throw exception(ORDER_NOT_EXISTS);
        }
        return bzOrderDO;
    }

    @Override
    public Integer getOrderStateOrNull(String orderSn) {
        if (StringUtils.isEmpty(orderSn)) return null;
        BzOrderDO order = this.getOne(new LambdaQueryWrapper<BzOrderDO>()
                .select(BzOrderDO::getOrderState)
                .eq(BzOrderDO::getOrderSn, orderSn.trim()));
        return order == null ? null : order.getOrderState();
    }

    /**
     * 获取订单
     *
     * @param orderSn
     * @return
     */
    public BzOrderDO getOneByOrderSn(String orderSn, String createTime) {
        if (ObjectUtils.isEmpty(orderSn) || ObjectUtils.isEmpty(createTime)) {
            throw exception(ORDER_NOT_EXISTS);
        }

        String tableFix = SubTableUtil.getTableListByDateSingle(Strings.EMPTY, DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, createTime));
        BzOrderDO bzOrderDO = baseMapper.selectByOrderSnByTable(tableFix, orderSn);

        if (ObjectUtils.isEmpty(bzOrderDO)) {
            throw exception(ORDER_NOT_EXISTS);
        }
        return bzOrderDO;
    }

    @Override
    public void addOrderTimerTask(BzOrderDO bzOrderDO, Integer time) {
        if (Objects.equals(OrderTypeEnum.ERRAND.getCode(), bzOrderDO.getOrderType())) {
            log.info("==> 代取订单不添加状态自动流转时间轮，orderSn={}", bzOrderDO.getOrderSn());
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        //定时任务执行时间
        long taskExecTime = !ObjectUtils.isEmpty(bzOrderDO.getAppointmentTime()) ? DateUtils.getMinutesBetweenTimes(now, DateUtils.stringToLocalDateTime(bzOrderDO.getAppointmentTime()).plusMinutes(time)) : time;

        log.info("==> 添加时间轮，orderSn={} , 创建时间={}，预约时间={}, taskExecTime={}", bzOrderDO.getOrderSn(), now, bzOrderDO.getAppointmentTime(), taskExecTime);
        //添加时间轮
        hashedWheelTimerProxy.getWheelTimer().newTimeout(t -> {
            log.info("==> 执行时间轮");

            BzOrderDO bzOrderMinutes = this.getOneByOrderSn(bzOrderDO.getOrderSn(), DateUtils.localDateTimeToString(bzOrderDO.getCreateTime(), DateUtils.YYYY_MM_DD_HH_MM_SS));
            String tableFix = SubTableUtil.getTableListByDateSingle(Strings.EMPTY, DateUtils.localDateTimeToDate(bzOrderMinutes.getCreateTime()));

            //仅修改制作中状态
            if (bzOrderMinutes.getOrderState() == OrderStateEnum.MAKING.getCode()) {
                BzOrderDO bzOrdertimer = new BzOrderDO();
                bzOrdertimer.setOrderSn(bzOrderMinutes.getOrderSn());
                bzOrdertimer.setCreateTime(bzOrderMinutes.getCreateTime());
                bzOrdertimer.setCreator(bzOrderMinutes.getCreator());
                bzOrdertimer.setMemberId(bzOrderMinutes.getMemberId());
                bzOrdertimer.setOrderId(bzOrderMinutes.getOrderId());
                bzOrdertimer.setLockState(bzOrderMinutes.getLockState());
                bzOrdertimer.setStoreId(bzOrderMinutes.getStoreId());

                if (bzOrderMinutes.getOrderType() == OrderTypeEnum.TAKEAWAY.getCode()) {
                    //外卖改为待配送
                    bzOrdertimer.setOrderState(OrderStateEnum.W_TO_BE_DELIVERED.getCode());
                } else {
                    //非外卖改为待取餐
                    bzOrdertimer.setOrderState(OrderStateEnum.W_TO_BE_PICKED_UP.getCode());
                }

                BusinessContextHolder.setBusinessId(bzOrderMinutes.getBusinessId());

                proxyService.updateOrderAsync(tableFix, bzOrdertimer);
                // 代取单完成必须由跑腿员确认送达触发，不能走普通订单的自动完成时间轮。
                if (Objects.equals(OrderTypeEnum.ERRAND.getCode(), bzOrderMinutes.getOrderType())) {
                    return;
                }
                //添加核销时间轮
                hashedWheelTimerProxy.getWheelTimer().newTimeout(e -> {
                    log.info("==> 执行已完成时间轮");
                    BzOrderDO bzOrderFinish = new BzOrderDO();
                    bzOrderFinish.setOrderSn(bzOrderDO.getOrderSn());
                    bzOrderFinish.setCreateTime(bzOrderDO.getCreateTime());
                    bzOrderFinish.setCreator(bzOrderMinutes.getCreator());
                    bzOrderFinish.setMemberId(bzOrderMinutes.getMemberId());
                    bzOrderFinish.setOrderId(bzOrderMinutes.getOrderId());
                    bzOrderFinish.setLockState(bzOrderMinutes.getLockState());
                    bzOrderFinish.setStoreId(bzOrderMinutes.getStoreId());

                    BusinessContextHolder.setBusinessId(bzOrderMinutes.getBusinessId());
                    BzOrderDO bzOrderHours = this.getOneByOrderSn(bzOrderDO.getOrderSn(), DateUtils.localDateTimeToString(bzOrderDO.getCreateTime(), DateUtils.YYYY_MM_DD_HH_MM_SS));
                    // 仅修改待配送 和 待取餐状态
                    if (bzOrderHours.getOrderState() == OrderStateEnum.W_TO_BE_DELIVERED.getCode() || bzOrderHours.getOrderState() == OrderStateEnum.W_TO_BE_PICKED_UP.getCode()) {
                        //更新已完成
                        bzOrderFinish.setOrderState(OrderStateEnum.COMPLETED.getCode());
                        bzOrderFinish.setFinishTime(LocalDateTime.now());

                        proxyService.updateOrderAsync(tableFix, bzOrderFinish);
                    }
                }, 1, TimeUnit.HOURS);
            }
        }, taskExecTime, TimeUnit.MINUTES);
    }

    @Override
    public void addErrandWaitingAcceptCancelTimerTask(BzOrderDO bzOrderDO) {
        if (!Objects.equals(OrderTypeEnum.ERRAND.getCode(), bzOrderDO.getOrderType())) {
            return;
        }
        log.info("==> 添加代取订单待接单超时取消时间轮，orderSn={}", bzOrderDO.getOrderSn());
        hashedWheelTimerProxy.getWheelTimer().newTimeout(t -> {
            log.info("==> 执行代取订单待接单超时取消时间轮，orderSn={}", bzOrderDO.getOrderSn());
            try {
                BzOrderDO order = this.getOneByOrderSn(bzOrderDO.getOrderSn(), DateUtils.localDateTimeToString(bzOrderDO.getCreateTime(), DateUtils.YYYY_MM_DD_HH_MM_SS));
                if (order == null) {
                    return;
                }
                if (!Objects.equals(OrderTypeEnum.ERRAND.getCode(), order.getOrderType())) {
                    return;
                }
                boolean waitingAccept = Objects.equals(OrderStateEnum.WAITING_ACCEPT.getCode(), order.getOrderState())
                        && ObjectUtils.isEmpty(order.getDeliveryId());
                if (!waitingAccept) {
                    log.info("==> 代取订单已非待接单状态，不执行超时取消，orderSn={}, orderState={}, deliveryId={}",
                            order.getOrderSn(), order.getOrderState(), order.getDeliveryId());
                    return;
                }
                SpringUtils.getBean(BzOrderService.class).cancelWaitingAcceptErrandOrderAndRefund(order);
            } catch (Exception e) {
                log.error("==> 代取订单待接单超时取消失败，orderSn={}", bzOrderDO.getOrderSn(), e);
            }
        }, 30, TimeUnit.MINUTES);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelWaitingAcceptErrandOrderAndRefund(BzOrderDO sourceOrder) {
        if (sourceOrder == null || sourceOrder.getCreateTime() == null || StringUtils.isBlank(sourceOrder.getOrderSn())) {
            throw exception(ORDER_NOT_EXISTS);
        }

        BzOrderDO order = sourceOrder;
        if (!Objects.equals(OrderTypeEnum.ERRAND.getCode(), order.getOrderType())) {
            throw exception(ORDER_WRONG_TYPE);
        }
        if (!Objects.equals(OrderStateEnum.WAITING_ACCEPT.getCode(), order.getOrderState())
                || !ObjectUtils.isEmpty(order.getDeliveryId())) {
            log.info("==> 代取订单已非待接单状态，不执行取消退款，orderSn={}, orderState={}, deliveryId={}",
                    order.getOrderSn(), order.getOrderState(), order.getDeliveryId());
            return;
        }

        BusinessContextHolder.setBusinessId(order.getBusinessId());
        BzOrderDO update = new BzOrderDO();
        update.setOrderId(order.getOrderId());
        update.setOrderSn(order.getOrderSn());
        update.setCreateTime(order.getCreateTime());
        update.setOrderState(OrderStateEnum.PENDING_REFUND.getCode());

        int updated = baseMapper.refundWaitingAcceptErrandOrder(update, OrderStateEnum.WAITING_ACCEPT.getCode());
        if (updated != 1) {
            log.info("==> 代取订单取消退款状态更新被跳过，orderSn={}", order.getOrderSn());
            return;
        }

        bzOrderPayService.refundWaitingAcceptErrandPayment(order);
        order.setOrderState(update.getOrderState());
        this.saveWaitingAcceptErrandRefundLog(order);

        if (ObjectUtil.isNotEmpty(order.getUserCouponId())) {
            userCouponApi.usedCoupon(UsedCouponReqVO.builder()
                    .userCouponId(order.getUserCouponId())
                    .memberId(order.getMemberId())
                    .isUsed(OrderConstants.NO)
                    .build());
        }

        try {
            log.info("==> 【代取待接单超时取消退款】kafka发消息 {}", order.getOrderSn());
            this.notifyOrder(order.getOrderSn(), "DELETE");
        } catch (Exception e) {
            log.info("==> 【代取待接单超时取消退款】kafka发消息失败 {}", order.getOrderSn(), e);
        }
    }

    private void saveWaitingAcceptErrandRefundLog(BzOrderDO order) {
        BzOrderLogDO bzOrderLogDO = new BzOrderLogDO();
        bzOrderLogDO.setOrderSn(order.getOrderSn());
        bzOrderLogDO.setOrderStateLog(OrderStateEnum.PENDING_REFUND.getCode());
        bzOrderLogDO.setLogContent("代取订单待接单超时自动取消退款");
        bzOrderLogDO.setLogTime(new Date());
        bzOrderLogDO.setLogUserId(order.getMemberId());
        bzOrderLogDO.setCreator(order.getCreator());
        bzOrderLogDO.setBusinessId(order.getBusinessId());
        bzOrderLogService.save(bzOrderLogDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeAcceptedErrandOrder(BzOrderDO sourceOrder) {
        if (sourceOrder == null || sourceOrder.getCreateTime() == null || StringUtils.isBlank(sourceOrder.getOrderSn())) {
            throw exception(ORDER_NOT_EXISTS);
        }

        BzOrderDO order = sourceOrder;
        if (!Objects.equals(OrderTypeEnum.ERRAND.getCode(), order.getOrderType())) {
            throw exception(ORDER_WRONG_TYPE);
        }
        if (!canAutoCompleteErrandOrder(order.getOrderState())
                || ObjectUtils.isEmpty(order.getDeliveryId())) {
            log.info("==> 代取订单已非已接单或配送中待确认收货状态，不执行自动确认收货，orderSn={}, orderState={}, deliveryId={}",
                    order.getOrderSn(), order.getOrderState(), order.getDeliveryId());
            return;
        }

        BusinessContextHolder.setBusinessId(order.getBusinessId());
        BzOrderDO update = new BzOrderDO();
        update.setOrderId(order.getOrderId());
        update.setOrderSn(order.getOrderSn());
        update.setCreateTime(order.getCreateTime());
        update.setOrderState(OrderStateEnum.COMPLETED.getCode());
        update.setFinishTime(LocalDateTime.now());

        OrderStateEnum expectedOrderState = Objects.equals(OrderStateEnum.ACCEPTED.getCode(), order.getOrderState())
                ? OrderStateEnum.ACCEPTED : OrderStateEnum.DELIVERED;
        int updated = this.updateErrandOrderStatus(update, order.getDeliveryId(), expectedOrderState);
        if (updated != 1) {
            log.info("==> 代取订单自动确认收货状态更新被跳过，orderSn={}", order.getOrderSn());
            return;
        }

        order.setOrderState(update.getOrderState());
        order.setFinishTime(update.getFinishTime());
        ErrandRunnerDTO runner = this.buildErrandRunnerFromOrder(order);
        this.saveAcceptedErrandAutoCompleteLog(order, runner);
        this.handleErrandDeliveredReward(order, runner);
    }

    private boolean canAutoCompleteErrandOrder(Integer orderState) {
        return Objects.equals(OrderStateEnum.ACCEPTED.getCode(), orderState)
                || Objects.equals(OrderStateEnum.DELIVERED.getCode(), orderState);
    }

    private ErrandRunnerDTO buildErrandRunnerFromOrder(BzOrderDO order) {
        ErrandRunnerDTO runner = new ErrandRunnerDTO();
        runner.setMemberId(order.getDeliveryId());
        runner.setName(order.getDeliveryName());
        runner.setPhone(order.getDeliveryPhone());
        return runner;
    }

    private void saveAcceptedErrandAutoCompleteLog(BzOrderDO order, ErrandRunnerDTO runner) {
        this.saveErrandOrderLog(order, runner, OrderStateEnum.COMPLETED.getCode(), "代取订单超时自动确认收货");
    }

    @Override
    public void cancelTodayWaitingAcceptErrandOrders() {
        LocalDate today = LocalDate.now();
        LocalDateTime startTime = today.atStartOfDay();
        LocalDateTime endTime = today.plusDays(1).atStartOfDay().minusNanos(1);
        List<BzOrderDO> orders = baseMapper.selectWaitingAcceptErrandOrders(startTime, endTime);
        if (CollUtil.isEmpty(orders)) {
            log.info("==> 当天无已支付待接单代取订单需要兜底取消");
            return;
        }
        log.info("==> 开始兜底取消当天已支付待接单代取订单，数量={}", orders.size());
        for (BzOrderDO order : orders) {
            try {
                SpringUtils.getBean(BzOrderService.class).cancelWaitingAcceptErrandOrderAndRefund(order);
            } catch (Exception e) {
                log.error("==> 兜底取消待接单代取订单失败，orderSn={}", order.getOrderSn(), e);
            }
        }
    }

    @Override
    public void completeTodayAcceptedErrandOrders() {
        LocalDate today = LocalDate.now();
        LocalDateTime startTime = today.atStartOfDay();
        LocalDateTime endTime = today.plusDays(1).atStartOfDay().minusNanos(1);
        List<BzOrderDO> orders = baseMapper.selectAcceptedErrandOrders(startTime, endTime);
        if (CollUtil.isEmpty(orders)) {
            log.info("==> 当天无已接单或配送中待确认收货代取订单需要兜底确认收货");
            return;
        }
        log.info("==> 开始兜底确认收货当天已接单或配送中代取订单，数量={}", orders.size());
        for (BzOrderDO order : orders) {
            try {
                SpringUtils.getBean(BzOrderService.class).completeAcceptedErrandOrder(order);
            } catch (Exception e) {
                log.error("==> 兜底确认收货已接单代取订单失败，orderSn={}", order.getOrderSn(), e);
            }
        }
    }

    /**
     * 十五分钟之后如果还是待付款 则退回优惠券
     *
     * @param bzOrderDO
     */
    @Override
    public void cancelCouponTimeTask(BzOrderDO bzOrderDO, Integer timeOut) {
        log.info("==> 添加取消优惠券时间轮,OrderSn：{}", bzOrderDO.getOrderSn());
        hashedWheelTimerProxy.getWheelTimer().newTimeout(t -> {
            log.info("==> 执行cancelCouponTimeTask时间轮,OrderSn：{}", bzOrderDO.getOrderSn());
            BzOrderDO bzOrderMinutes = this.getOneByOrderSn(bzOrderDO.getOrderSn(), DateUtils.localDateTimeToString(bzOrderDO.getCreateTime(), DateUtils.YYYY_MM_DD_HH_MM_SS));

            boolean needCancel = OrderStateEnum.UNPAID.getCode() == bzOrderMinutes.getOrderState();
            if (needCancel) {
                log.info("==>  15分钟超时，{}", bzOrderMinutes.getOrderSn());
                bzOrderMinutes.setOrderState(OrderStateEnum.CANCELED.getCode());

                BusinessContextHolder.setBusinessId(bzOrderMinutes.getBusinessId());
                String tableFix = SubTableUtil.getTableListByDateSingle(Strings.EMPTY, DateUtils.localDateTimeToDate(bzOrderMinutes.getCreateTime()));
                proxyService.updateOrderAsync(tableFix, bzOrderMinutes);

                if (!Objects.equals(bzOrderMinutes.getOrderFrom(), OrderFromEnum.POINT_SINGLE_MACHINE.getCode())) {
                    try {
                        activityApi.releaseMzGiftInventory(bzOrderMinutes.getOrderSn());
                    } catch (Exception e) {
                        log.error("==> 【超时取消】释放满赠库存失败 orderSn={}", bzOrderMinutes.getOrderSn(), e);
                    }
                }

                //必须是小程序使用优惠券
                if (ObjectUtil.isNotEmpty(bzOrderMinutes.getUserCouponId())) {
                    //超时回退优惠券
                    userCouponApi.usedCoupon(UsedCouponReqVO.builder().userCouponId(bzOrderMinutes.getUserCouponId()).memberId(bzOrderMinutes.getMemberId()).isUsed(OrderConstants.NO).isRefundAction(OrderConstants.NO).build());
                }

                try {
                    log.info("==> 【超时取消】kafka发消息 {}", bzOrderDO.getOrderSn());
                    this.notifyOrder(bzOrderDO.getOrderSn(), "DELETE");
                } catch (Exception e) {
                    log.info("==> 【超时取消】kafka发消息 失败 {}", bzOrderDO.getOrderSn(), e);
                }

            }
        }, ObjectUtils.isEmpty(timeOut) ? 15 : timeOut, TimeUnit.MINUTES);
    }

    @Override
    public void updateFinalOrderFinishTime(Long memberId, BzOrderDO bzOrderDO) {
        //更新会员最后下单时间
        CommonResult<MemberOrderDTO> commonResult = wxMemberApi.updateFinalOrderFinishTime(memberId);
        MemberOrderDTO memberOrderDTO = commonResult.getCheckedData();

        if (ObjectUtil.isNotEmpty(memberOrderDTO)) {
            //是否是新客 0否 1是
            bzOrderDO.setExpressId(memberOrderDTO.getIsNew().longValue());
            //下单间隔天数
            bzOrderDO.setDelayDays(memberOrderDTO.getIntervalDays().longValue());
            //是否会员 0否 1是
            bzOrderDO.setIsSettlement(memberOrderDTO.getIsMember());
        }
    }

    @Override
    @DataPermission(enable = false)
    public List<BzOrderDTO> selectBzOrderStoreData(LocalDateTime yesterdayStart, LocalDateTime yesterdayEnd) {
        return bzOrderMapper.selectBzOrderStoreData(yesterdayStart, yesterdayEnd);
    }

    @Override
    @DataPermission(enable = false)
    public List<BzOrderDTO> selectBzOrderData(LocalDateTime startTime, LocalDateTime endTime) {
        return bzOrderMapper.selectBzOrderData(startTime, endTime);
    }

    @Override
    @DataPermission(enable = false)
    public List<BzOrderDTO> selectBzOrderData2(LocalDateTime startTime, LocalDateTime endTime) {
        return bzOrderMapper.selectBzOrderData2(startTime, endTime);
    }

    @Override
    @DS(DsNameConstants.MASTER)
    public Set<Long> getHisPhones(String date, List<Long> storeIds) {
        return bzOrderMapper.getHisPhones(date, storeIds);
    }

    @Override
    @DS(DsNameConstants.MASTER)
    public Set<Long> getWeekPhones(String date, LocalDate localDate, List<Long> storeIds) {
        return bzOrderMapper.getWeekPhones(date, localDate, storeIds);
    }

    @Override
    public BzOrderPrintInfoDTO getOrderPrintInfo(Long storeId) {
        BzOrderPrintInfoDTO returnObj = new BzOrderPrintInfoDTO();

        String startDateTime = DateUtils.getDate() + DateUtils.T_00_00_00;
        String endDateTime = DateUtils.getDate() + DateUtils.T_23_59_59;


        LocalDateTime start = LocalDateTime.parse(startDateTime, DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS));
        LocalDateTime end = LocalDateTime.parse(endDateTime, DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS));
        LambdaQueryWrapper<BzOrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(BzOrderDO::getCreateTime, start, end);
        queryWrapper.eq(BzOrderDO::getStoreId, storeId);
        queryWrapper.notIn(BzOrderDO::getOrderState, List.of(0, 10, 100));

        returnObj.setOperator(SecurityFrameworkUtils.getLoginUserNickname());
        returnObj.setCurrentTime(DateUtils.localDateTimeToString(LocalDateTime.now(), DateUtils.YYYY_MM_DD_HH_MM_SS));
        returnObj.setStartTime(startDateTime);
        returnObj.setEndTime(endDateTime);

        //查找订单
        List<BzOrderDO> bzOrderVos = baseMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(bzOrderVos)) {
            return returnObj;
        }

        //预定义金额
        BigDecimal turnover = BigDecimal.ZERO, mtTurnover = BigDecimal.ZERO, tiktokTurnover = BigDecimal.ZERO, returnTurnover = BigDecimal.ZERO, wechatTurnover = BigDecimal.ZERO, aliPayTurnover = BigDecimal.ZERO, cashTurnover = BigDecimal.ZERO, activityDiscountTurnover = BigDecimal.ZERO;
        int effectiveOrderQuantity = 0, aliPayQuantity = 0, wechatQuantity = 0, cashQuantity = 0, mtQuantity = 0, tiktokQuantity = 0, tsQuantity = 0, dbQuantity = 0, wmQuantity = 0;

        for (BzOrderDO i : bzOrderVos) {
            if (OrderStateEnum.PENDING_REFUND.getCode() == i.getOrderState()) {
                returnTurnover = returnTurnover.add(i.getOrderAmount());
            }
            if (OrderStateEnum.CANCELED.getCode() != i.getOrderState() && OrderStateEnum.PENDING_REFUND.getCode() != i.getOrderState() && OrderStateEnum.UNPAID.getCode() != i.getOrderState()) {

                //营业额
                turnover = turnover.add(i.getOrderAmount());

                //有效订单
                ++effectiveOrderQuantity;

                //优惠
                activityDiscountTurnover = activityDiscountTurnover.add(i.getActivityDiscountAmount());

                //堂食
                if (OrderTypeEnum.CANTEEN_FOOD.getCode() == i.getOrderType()) {
                    ++tsQuantity;
                }

                //打包
                if (OrderTypeEnum.PACK.getCode() == i.getOrderType()) {
                    ++dbQuantity;
                }

                //外卖
                if (OrderTypeEnum.TAKEAWAY.getCode() == i.getOrderType()) {
                    ++wmQuantity;
                }

                //抖音
                if (OrderFromEnum.TIKTOK.getCode() == i.getOrderFrom()) {
                    ++tiktokQuantity;
                    tiktokTurnover = tiktokTurnover.add(i.getOrderAmount());
                    continue;
                }

                //美团
                if (OrderFromEnum.MEITUAN.getCode() == i.getOrderFrom()) {
                    ++mtQuantity;
                    mtTurnover = mtTurnover.add(i.getOrderAmount());
                    continue;
                }

                //现金
                if ("0".equals(i.getPaymentCode())) {
                    ++cashQuantity;
                    cashTurnover = cashTurnover.add(i.getOrderAmount());
                }
                //微信
                if ("1".equals(i.getPaymentCode())) {
                    ++wechatQuantity;
                    wechatTurnover = wechatTurnover.add(i.getOrderAmount());
                }
                //支付宝
                if ("2".equals(i.getPaymentCode())) {
                    ++aliPayQuantity;
                    aliPayTurnover = aliPayTurnover.add(i.getOrderAmount());
                }
            }
        }

        returnObj.setStoreName(bzOrderVos.get(0).getStoreName());
        returnObj.setActivityDiscountTurnover(activityDiscountTurnover);
        returnObj.setTurnover(turnover);
        returnObj.setAliPayTurnover(aliPayTurnover);
        returnObj.setAliPayQuantity(aliPayQuantity);
        returnObj.setWechatTurnover(wechatTurnover);
        returnObj.setWechatQuantity(wechatQuantity);
        returnObj.setTiktokTurnover(tiktokTurnover);
        returnObj.setTiktokQuantity(tiktokQuantity);
        returnObj.setMeituanTurnover(mtTurnover);
        returnObj.setMeituanQuantity(mtQuantity);
        returnObj.setCashTurnover(cashTurnover);
        returnObj.setCashQuantity(cashQuantity);
        if (effectiveOrderQuantity > 0) {
            returnObj.setAvgTurnover(turnover.divide(new BigDecimal(effectiveOrderQuantity), 2, BigDecimal.ROUND_HALF_UP));
        }
        returnObj.setReturnTurnover(returnTurnover);
        returnObj.setOrderQuantity(effectiveOrderQuantity);
        returnObj.setOrderTurnover(turnover);
        returnObj.setTsQuantity(tsQuantity);
        returnObj.setDbQuantity(dbQuantity);
        returnObj.setWmQuantity(wmQuantity);

        return returnObj;
    }

    @Override
    public OrderDetailDTO getDetail(String orderSn) {
        OrderDetailDTO detail = new OrderDetailDTO();
        detail.setProductDOList(
                bzOrderProductService.list(
                        new LambdaQueryWrapper<BzOrderProductDO>()
                                .eq(BzOrderProductDO::getOrderSn, orderSn)
                )
        );
        detail.setPurchaseDOList(
                bzOrderPurchaseService.list(
                        new LambdaQueryWrapper<BzOrderPurchaseDO>()
                                .eq(BzOrderPurchaseDO::getOrderSn, orderSn)
                )
        );
        detail.setProductSonDOList(
                bzOrderProductSonService.list(
                        new LambdaQueryWrapper<BzOrderProductSonDO>()
                                .eq(BzOrderProductSonDO::getOrderSn, orderSn)
                )
        );
        return detail;
    }

    /**
     * 扣减原材料库存
     *
     * @param bzOrderDO
     * @param detail
     */
    @Override
    public void changeStock(BzOrderDO bzOrderDO, OrderDetailDTO detail, StockChangeEnum stockChangeEnum) {
        if (!notifyRawmaterialSwitch) {
            return;
        }
        long now0 = System.currentTimeMillis();
        //拿到原材料配方
        CommoditySplitMaterialReqVO commoditySplitMaterialReqVO = this.getCommoditySplitMaterialReqVO(bzOrderDO, detail);
        log.info("==> 获取配方 入参 {}", JSON.toJSONString(commoditySplitMaterialReqVO));
        CommonResult<List<MaterialListRespDTO>> listCommonResult = commodityApi.modifyCommodity(commoditySplitMaterialReqVO);

        long cost0 = System.currentTimeMillis() - now0;
        log.info("==> 获取配方 响应 {} 耗时 {}毫秒", JSON.toJSONString(listCommonResult), cost0);

        long now1 = System.currentTimeMillis();
        //扣减库存
        StockChangeVO param = this.getStockChangeVO(bzOrderDO, listCommonResult, stockChangeEnum);

        long cost1 = System.currentTimeMillis() - now1;
        log.info("==> 扣减原材料库存 入参 {} 耗时 {}毫秒", JSON.toJSONString(param), cost1);
        commodityApi.changeStock(param);
    }

    /**
     * 扣减库存入参
     *
     * @param bzOrderDO
     * @param listCommonResult
     * @param stockChangeEnum
     * @return
     */
    private StockChangeVO getStockChangeVO(BzOrderDO bzOrderDO, CommonResult<List<MaterialListRespDTO>> listCommonResult, StockChangeEnum stockChangeEnum) {
        //去扣减原材料库存
        StockChangeVO param = new StockChangeVO();
        param.setStoreId(bzOrderDO.getStoreId());
        param.setType(stockChangeEnum);
        param.setReferenceId(bzOrderDO.getOrderSn());

        List<StockChangeVO.ChangeInfo> changeList = listCommonResult.getData().stream().map(item -> {
            StockChangeVO.ChangeInfo changeInfo = new StockChangeVO.ChangeInfo();
            //转换最小单位数量
            List<CommodityConversionDTO> calUnitList = item.getCalUnitList();
            if (!CollectionUtils.isEmpty(calUnitList)) {
                Map<String, BigDecimal> unitMap = calUnitList.stream().collect(Collectors.toMap(CommodityConversionDTO::getBeforeUnit, CommodityConversionDTO::getAfterNumber));
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(unitMap) && !calUnitList.get(0).getAfterUnit().equals(item.getUsedUnit())) {
                    item.setQuantity(unitMap.get(item.getUsedUnit()).multiply(item.getQuantity()));
                    item.setUsedUnit(calUnitList.get(0).getAfterUnit());
                }
            }
            //负数
            changeInfo.setQuantity(new BigDecimal("-" + item.getQuantity().toString()));
            changeInfo.setRawMaterialId(item.getRawMaterialId());
            changeInfo.setChooseUnit(item.getUsedUnit());
            changeInfo.setNotes(bzOrderDO.getOrderRemark());
            return changeInfo;
        }).toList();

        List<StockChangeVO.ChangeInfo> mChangeList = changeList.stream()
                .collect(Collectors.toMap(
                        StockChangeVO.ChangeInfo::getRawMaterialId,
                        c -> {
                            StockChangeVO.ChangeInfo copy = new StockChangeVO.ChangeInfo();
                            copy.setRawMaterialId(c.getRawMaterialId());
                            copy.setQuantity(c.getQuantity()); // 初始数量
                            copy.setChooseUnit(c.getChooseUnit());
                            copy.setNotes(c.getNotes());
                            return copy;
                        },
                        (c1, c2) -> {
                            // 数量累加
                            c1.setQuantity(c1.getQuantity().add(c2.getQuantity()));
                            return c1;
                        }
                ))
                .values()
                .stream()
                .toList();

        param.setChangeList(mChangeList);
        return param;
    }

    /**
     * 获取商品配方入参
     *
     * @param bzOrderDO
     * @param detail
     * @return
     */
    private CommoditySplitMaterialReqVO getCommoditySplitMaterialReqVO(BzOrderDO bzOrderDO, OrderDetailDTO detail) {
        CommoditySplitMaterialReqVO commoditySplitMaterialReqVO = new CommoditySplitMaterialReqVO();
        commoditySplitMaterialReqVO.setStoreId(bzOrderDO.getStoreId());
        detail.getProductDOList().forEach(productDO -> {
            if (productDO.getIsSingle() == 2) {
                return;
            }
            MaterialDataVo materialDataVo = new MaterialDataVo();
            materialDataVo.setCommodityId(productDO.getCommodityId());
            materialDataVo.setSkuId(productDO.getOriginalSkuId());
            materialDataVo.setQuantity(BigDecimal.valueOf(productDO.getGoodsNum()));
            materialDataVo.setCommodityType(OrderConstants.ONE);
            materialDataVo.setCommodityName(productDO.getGoodsName());
            commoditySplitMaterialReqVO.getMaterialDataVos().add(materialDataVo);
        });
        detail.getPurchaseDOList().forEach(purchaseDO -> {
            MaterialDataVo materialDataVo = new MaterialDataVo();
            materialDataVo.setCommodityId(purchaseDO.getCommodityId());
            materialDataVo.setSkuId(purchaseDO.getOriginalSkuId());
            materialDataVo.setQuantity(BigDecimal.ONE);
            materialDataVo.setCommodityType(OrderConstants.ONE);
            materialDataVo.setCommodityName(purchaseDO.getPurchaseName());
            commoditySplitMaterialReqVO.getMaterialDataVos().add(materialDataVo);
        });
        detail.getProductSonDOList().forEach(productSonDO -> {
            MaterialDataVo materialDataVo = new MaterialDataVo();
            materialDataVo.setCommodityId(productSonDO.getCommodityId());
            materialDataVo.setSkuId(productSonDO.getOriginalSkuId());
            materialDataVo.setQuantity(BigDecimal.valueOf(productSonDO.getGoodsNum()));
            materialDataVo.setCommodityType(OrderConstants.ONE);
            materialDataVo.setCommodityName(productSonDO.getGoodsName());
            commoditySplitMaterialReqVO.getMaterialDataVos().add(materialDataVo);
        });
        return commoditySplitMaterialReqVO;
    }

    public void incrementProductSales(List<BzOrderProductDO> bzOrderProductList, Boolean isRefund) {
        //缓存商品销量+
        if (!com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(bzOrderProductList)) {
            for (BzOrderProductDO bzOrderProductDO : bzOrderProductList) {
                if (bzOrderProductDO.getCommodityId() != null) {
                    this._incrementProductSales(bzOrderProductDO.getCommodityId(), bzOrderProductDO.getGoodsNum(), isRefund);
                }
            }
        }
    }

    @Override
    public void incrementPurchaseSales(List<BzOrderPurchaseDO> purchaseDOList, Boolean isRefund) {
        // 加购商品在订单明细表中一条记录代表 1 份销量
        if (!com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(purchaseDOList)) {
            for (BzOrderPurchaseDO purchaseDO : purchaseDOList) {
                if (purchaseDO.getCommodityId() != null) {
                    this._incrementPurchaseSales(purchaseDO.getCommodityId(), isRefund);
                }
            }
        }
    }

    @Override
    public void _incrementProductSales(Long commodityId, long count, Boolean isRefund) {
        stringRedisTemplate.opsForHash().increment(SAAS_PRODUCT_SALES, commodityId.toString(), isRefund ? -count : count);
    }

    @Override
    public void _incrementPurchaseSales(Long commodityId, Boolean isRefund) {
        stringRedisTemplate.opsForHash().increment(SAAS_PURCHASE_PRODUCT_SALES, commodityId.toString(), isRefund ? -1 : 1);
    }

    @Override
    public void notifyOrder(String orderSn, String type) throws Exception {
        NotifyOrderDTO notifyOrderDTO = NotifyOrderDTO.builder()
                .orderSn(orderSn).type(type).build();
        kafkaProducerUtil.sendMessage(orderNotifyTopic, orderSn, com.alibaba.fastjson.JSON.toJSONString(notifyOrderDTO));
    }

    @Override
    public void putProductEs(String orderSn, String startTime, String endTime) {
        int canQuery = 0;
        LambdaQueryWrapper<BzOrderDO> wrapper = new LambdaQueryWrapper<>();

        if (ObjectUtil.isNotEmpty(orderSn)) {
            wrapper.eq(BzOrderDO::getOrderSn, orderSn);
            canQuery++;
        }

        if (ObjectUtil.isNotEmpty(startTime) && ObjectUtil.isNotEmpty(endTime)) {
            wrapper.between(BzOrderDO::getCreateTime, DateUtils.stringToLocalDateTime(startTime), DateUtils.stringToLocalDateTime(endTime));
            canQuery++;
        }

        wrapper.notIn(BzOrderDO::getOrderState, OrderStateEnum.CANCELED.getCode(), OrderStateEnum.PENDING_REFUND.getCode(), OrderStateEnum.UNPAID.getCode());

        if (canQuery != 1) {
            throw new ServiceException(1000000002, "参数错误");
        }

        // 分页参数 每批 2000 条，避免一次查太多
        int pageSize = 2000;
        int pageNum = 1;

        while (true) {
            Page<BzOrderDO> page = new Page<>(pageNum, pageSize);
            Page<BzOrderDO> result = this.page(page, wrapper);

            List<BzOrderDO> list = result.getRecords();
            if (CollectionUtil.isEmpty(list)) {
                // 没数据了
                break;
            }

            list.forEach(bzOrderDO -> {
                //kafka发消息
                try {
                    log.info("==> 补偿 {}", bzOrderDO.getOrderSn());
                    this.notifyOrder(bzOrderDO.getOrderSn(), "INSERT");
                } catch (Exception e) {
                    log.info("==> 补偿 失败 {}", bzOrderDO.getOrderSn());
                    e.printStackTrace();
                }
            });

            if (list.size() < pageSize) {
                // 最后一页
                break;
            }
            pageNum++;
        }
    }

}
