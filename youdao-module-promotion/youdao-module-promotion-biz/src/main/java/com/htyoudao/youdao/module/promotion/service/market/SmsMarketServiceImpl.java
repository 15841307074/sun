package com.htyoudao.youdao.module.promotion.service.market;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.member.api.crowd.CrowdApi;
import com.htyoudao.youdao.module.member.api.crowd.dto.CrowdNameDTO;
import com.htyoudao.youdao.module.member.api.wx.WxActionApi;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberCrowdDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.order.api.order.BzOrderApi;
import com.htyoudao.youdao.module.promotion.constant.CouponSourceConstant;
import com.htyoudao.youdao.module.promotion.constant.WechatJumpConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.ShortUrlRequest;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.ShortUrlResponse;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.WechatJumpParam;
import com.htyoudao.youdao.module.promotion.controller.admin.market.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcouponpackage.GoodCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.market.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponpackage.CouponPackageMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcoupon.GoodCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcouponpackage.GoodCouponPackageMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.market.SmsMarketMapper;
import com.htyoudao.youdao.module.promotion.service.pvstatistics.PvStatisticsService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.UserCouponShardService;
import com.htyoudao.youdao.module.promotion.service.wechat.ShortUrlService;
import com.htyoudao.youdao.module.promotion.service.wechat.WeChatService;
import com.htyoudao.youdao.module.promotion.util.CouponTimeUtil;
import com.htyoudao.youdao.module.promotion.util.DateUtils;
import com.htyoudao.youdao.module.promotion.util.TimeRangeChecker;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

/**
 * @author dht
 */
@Service
@Slf4j
@RefreshScope
public class SmsMarketServiceImpl extends ServiceImpl<SmsMarketMapper,  SmsMarketDO> implements SmsMarketService{

    @Resource
    private SmsMarketDeptService smsMarketDeptService;

    @Resource
    private SmsMarketStoreService smsMarketStoreService;

    @Resource
    private SmsMarketMapper smsMarketMapper;


    @DubboReference
    private WxActionApi wxActionApi;

    @DubboReference
    private WxMemberApi wxMemberApi;

    @DubboReference
    private BzOrderApi bzOrderApi;

    private static final String MARKET_USER_KEY = "market:user:";

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Value("${market.sms.signName}")
    private String signName;

    @Value("${market.sms.templateCode}")
    private String templateCode;

    @Value("${market.sms.templateParam}")
    private String templateParam;

    @Value("#{'${market.storeIds}'.split(',')}")
    private List<String> storeIds;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private SmsMarketPhoneService  smsMarketPhoneService;

    @Resource
    private PvStatisticsService pvStatisticsService;

    private static final RateLimiter couponPackageLimiter = RateLimiter.create(0.67);
    @Autowired
    private RedisCache redisCache;

    @Resource
    private CouponPackageMapper couponPackageMapper;

    @Resource
    private GoodCouponPackageMapper goodCouponPackageMapper;

    @Resource
    private GoodCouponMapper goodCouponMapper;

    @Resource
    private UserCouponShardService userCouponShardService;

    @Resource
    private SmsService smsService;

    @Resource
    private SmsTemplateService  smsTemplateService;

    @Resource
    private IdentifierGenerator identifierGenerator;

    @Value("${coupon.sort.sortPath}")
    private String sortPath;

    @Value("${wechat.schemeInfo.envVersion}")
    private String envVersion;

//    @Value("${wechat.access.token}")
//    private String accessToken;

    @Resource
    private WeChatService weChatService;

    private static final String GENERATE_URL_LINK = "https://api.weixin.qq.com/wxa/generatescheme?access_token=";

    private final static String URL = "https://api.weixin.qq.com/wxa/genwxashortlink?access_token=";

    @Resource
    private ShortUrlService shortUrlService;

    @DubboReference
    private CrowdApi crowdApi;

    @Override
    public PageResult<SmsMarketRespVO> selectSmsMarketListPage(SmsMarketReqVO smsMarket) {
        QueryWrapper<SmsMarketDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByDesc(SmsMarketDO::getCreateTime)
        // 计划名称
        .like(ObjectUtil.isNotEmpty(smsMarket.getPlanName()),SmsMarketDO::getPlanName,smsMarket.getPlanName())
        // 发送场景
        .eq(ObjectUtil.isNotEmpty(smsMarket.getSendingScenario()),SmsMarketDO::getSendingScenario,smsMarket.getSendingScenario())
        // 发送人群
        .eq( ObjectUtil.isNotEmpty(smsMarket.getSendPeople()),SmsMarketDO::getSendPeople,smsMarket.getSendPeople())
        // 发送时间
        .ge(ObjectUtil.isNotEmpty(smsMarket.getStartSendTime()),SmsMarketDO::getSendTime,smsMarket.getStartSendTime())
        .le(ObjectUtil.isNotEmpty(smsMarket.getEndSendTime()),SmsMarketDO::getSendTime,smsMarket.getEndSendTime())
        // 人群id
        .like(ObjectUtil.isNotEmpty(smsMarket.getCrowdId()),SmsMarketDO::getCrowdId,smsMarket.getCrowdId())

        // 发送方式
        .eq(ObjectUtil.isNotEmpty(smsMarket.getSendMethod()),SmsMarketDO::getSendMethod,smsMarket.getSendMethod())
        .eq(ObjectUtil.isNotEmpty(smsMarket.getSendStatus()),SmsMarketDO::getSendStatus,smsMarket.getSendStatus());

        // 查询门店
        if(ObjectUtil.isNotEmpty(smsMarket.getDesStoreId())){
            QueryWrapper<SmsMarketStoreDO> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.lambda().eq(SmsMarketStoreDO::getStoreId, smsMarket.getDesStoreId());
            List<SmsMarketStoreDO> list = smsMarketStoreService.list(queryWrapper1);
            if(ObjectUtil.isNotEmpty(list)){
                List<Long> marketIds = list.stream().map(SmsMarketStoreDO::getSmsMarketId).toList();
                queryWrapper.and(
                        wrapper -> wrapper.lambda().in(SmsMarketDO::getId, marketIds)
                                .or().like(SmsMarketDO::getDesStore, 0)
                );
            }
        }

        // 查询部门
        if(ObjectUtil.isNotEmpty(smsMarket.getDesDeptId())){
            QueryWrapper<SmsMarketDeptDO> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.lambda().eq(SmsMarketDeptDO::getDeptId, smsMarket.getDesStoreId());
            List<SmsMarketDeptDO> list = smsMarketDeptService.list(queryWrapper2);
            if(ObjectUtil.isNotEmpty(list)){
                List<Long> marketIds = list.stream().map(SmsMarketDeptDO::getSmsMarketId).toList();
                queryWrapper.and(
                        wrapper -> wrapper.lambda().in(SmsMarketDO::getId, marketIds)
                                .or().like(SmsMarketDO::getDesDept, 0)
                );
            }
        }

         Page<SmsMarketDO> smsMarketPage = new Page<>(smsMarket.getPageNo(),smsMarket.getPageSize());
         Page<SmsMarketDO> page = this.page(smsMarketPage, queryWrapper);

         PageResult<SmsMarketRespVO> result = new PageResult<>();
         result.setTotal(page.getTotal());
         result.setList(page.getRecords().stream().map(item -> BeanUtils.toBean(item, SmsMarketRespVO.class)).collect(Collectors.toList()));



        List<SmsMarketRespVO> records = result.getList();
        if(CollectionUtil.isNotEmpty(records)){
            List<Long> ids = records.stream().map(SmsMarketRespVO::getId).toList();
            List<Long> allCrowd = records.stream()
                    .filter(item -> item.getCrowdId() != null && !item.getCrowdId().trim().isEmpty()) // 过滤null或空crowdId
                    .peek(item -> {
                        List<Long> longList = Arrays.stream(item.getCrowdId().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(Long::parseLong)
                                .toList();
                        item.setCrowdIds(longList);
                    })
                    .flatMap(item -> item.getCrowdIds().stream())
                    .collect(Collectors.toList());
            Map<Long, String> crowdNameMap = new HashMap<>();
            if(CollectionUtils.isNotEmpty(allCrowd)){
                List<CrowdNameDTO> crowdNames = crowdApi.getByIds(allCrowd);
                if(CollectionUtil.isNotEmpty(crowdNames)){
                    crowdNameMap = crowdNames.stream().collect(Collectors.toMap(CrowdNameDTO::getId, CrowdNameDTO::getCrowdName));
                }
            }


            QueryWrapper<SmsMarketStoreDO> queryWrapper3 = new QueryWrapper<>();
            queryWrapper3.lambda().in(SmsMarketStoreDO::getSmsMarketId, ids);
            List<SmsMarketStoreDO> list = smsMarketStoreService.list(queryWrapper3);
            Map<Long, List<SmsMarketStoreDO>> collect = list.stream().collect(Collectors.groupingBy(SmsMarketStoreDO::getSmsMarketId));
            for (SmsMarketRespVO item : records) {
                List<SmsMarketStoreDO> smsMarketStores = collect.get(item.getId());
                item.setSmsMarketStores(smsMarketStores);
                Map<Long, String> finalCrowdNameMap = crowdNameMap;
                String crowdName = Optional.ofNullable(item.getCrowdIds())
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .filter(Objects::nonNull)
                        .map(crowdId -> finalCrowdNameMap.getOrDefault(crowdId, "未知人群"))
                        .filter(name -> !name.trim().isEmpty())
                        .collect(Collectors.joining(", "));
                item.setCrowdName(crowdName);
            }
        }
        return result;
    }

    @Override
    public SmsMarketRespVO selectById(Long id) {
        SmsMarketDO smsMarket = this.getById(id);
        if(ObjectUtil.isEmpty(smsMarket)){
            return new SmsMarketRespVO();
        }
        SmsMarketRespVO smsMarketVO = BeanUtils.toBean(smsMarket, SmsMarketRespVO.class);
        Integer desDept = smsMarket.getDesDept();
        if(ObjectUtil.isNotEmpty(desDept) && desDept == 1){
            QueryWrapper <SmsMarketDeptDO> queryWrapper = new QueryWrapper<>();
             queryWrapper.lambda().eq(SmsMarketDeptDO::getSmsMarketId, id);
            List<SmsMarketDeptDO> depts = smsMarketDeptService.list(queryWrapper);
            if(CollectionUtil.isNotEmpty(depts)){
                smsMarketVO.setDesDeptIds(depts.stream().map(SmsMarketDeptDO::getDeptId).toList());
            }

        }

        Integer desStore = smsMarket.getDesStore();
        if(ObjectUtil.isNotEmpty(desStore) && desStore == 1){
            QueryWrapper <SmsMarketStoreDO> queryWrapper = new QueryWrapper<>();
             queryWrapper.lambda().eq(SmsMarketStoreDO::getSmsMarketId, id);
            List<SmsMarketStoreDO> stores = smsMarketStoreService.list(queryWrapper);
            if(CollectionUtil.isNotEmpty(stores)){
                smsMarketVO.setDesStoreIds(stores.stream().map(SmsMarketStoreDO::getStoreId).toList());
            }
        }
        return smsMarketVO;
    }

    @Override
    public List<SmsTemplateRespVO> getTemplateList() {
        QueryWrapper<SmsTemplateDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByDesc(SmsTemplateDO::getCreateTime);
        List<SmsTemplateDO> list = smsTemplateService.list(queryWrapper);
        return BeanUtils.toBean(list, SmsTemplateRespVO.class);
    }

    @Override
    public Boolean insert(SmsMarketSaveReqVO smsMarket) {
        String planName = smsMarket.getPlanName();
        if(ObjectUtil.isNotEmpty(planName)){
            QueryWrapper<SmsMarketDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(SmsMarketDO::getPlanName, planName);
            long count = this.count(queryWrapper);
            if(count > 0){
                throw exception(PLAN_NAME_EXIST);
            }
        }
        long id = identifierGenerator.nextId(null).longValue();

        Long couponPackageId = smsMarket.getCouponPackageId();
        Long userId = WebFrameworkUtils.getLoginUserId();
        Long businessId = BusinessContextHolder.getRequiredBusinessId();

        SmsMarketDO smsMarketEntity = new SmsMarketDO();
        BeanUtils.copyProperties(smsMarket,smsMarketEntity);
        if(smsMarket.getDesStore() == 1){
            smsMarketEntity.setDesDept(2);
        } else if (smsMarket.getDesStore() == 0) {
            smsMarketEntity.setDesDept(0);
            smsMarketEntity.setDesStore(2);
        }

        try {
            String longUrl = this.generateUrlLink(WechatJumpParam.builder().path(sortPath).businessId(businessId).query("id=" + couponPackageId+ "&marketId=" + id).build());
            ShortUrlResponse response = getSortUrl(longUrl);
            String sortUrl = response.getShortUrl();
            String shortCode = response.getShortCode();
            smsMarketEntity.setTemplateContent(smsMarket.getTemplateContent() +" "+ sortUrl);
            smsMarketEntity.setShortCode(shortCode);
        }catch (Exception e){
            throw exception(WECHAT_TOKEN_ERROR);
        }

        smsMarketEntity.setId(id);
        boolean save = this.save(smsMarketEntity);
        Integer desDept = smsMarketEntity.getDesDept();
        if(ObjectUtil.isNotEmpty(desDept) && desDept == 1){
            List<SmsMarketDeptDO> desDepts = smsMarket.getSmsMarketDepts();
            if(CollectionUtil.isNotEmpty(desDepts)){
                List<SmsMarketDeptDO> smsMarketDepts = desDepts.stream().map(item -> {
                    SmsMarketDeptDO smsMarketDept = new SmsMarketDeptDO();
                    smsMarketDept.setDeptId(item.getDeptId());
                    smsMarketDept.setSmsMarketId(id);
                    return smsMarketDept;
                }).toList();
                smsMarketDeptService.insertBatch(smsMarketDepts);
            }
        }

        Integer desStore = smsMarketEntity.getDesStore();
        if(ObjectUtil.isNotEmpty(desStore) && desStore == 1){
            List<SmsMarketStoreDO> desStores = smsMarket.getSmsMarketStores();
            if(CollectionUtil.isNotEmpty(desStores)){
                List<SmsMarketStoreDO> smsMarketStores = desStores.stream().map(item -> {
                    SmsMarketStoreDO smsMarketStore = new SmsMarketStoreDO();
                    smsMarketStore.setStoreId(item.getStoreId());
                    smsMarketStore.setStoreName(item.getStoreName());
                    smsMarketStore.setSmsMarketId(id);
                    return smsMarketStore;
                }).toList();
                smsMarketStoreService.insertBatch(smsMarketStores);
            }
        }
        return save;
    }

    @Override
    public Boolean edit(SmsMarketSaveReqVO smsMarket) {

        String planName = smsMarket.getPlanName();
        if(ObjectUtil.isNotEmpty(planName)){
            QueryWrapper<SmsMarketDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(SmsMarketDO::getPlanName, planName);
            queryWrapper.lambda().ne(SmsMarketDO::getId, smsMarket.getId());
            long count = this.count(queryWrapper);
            if(count > 0){
                throw exception(PLAN_NAME_EXIST);
            }
        }

        SmsMarketDO smsMarketEntity = new SmsMarketDO();
        BeanUtils.copyProperties(smsMarket,smsMarketEntity);
        if(smsMarket.getDesStore() == 1){
            smsMarketEntity.setDesDept(2);
        } else if (smsMarket.getDesStore() == 0) {
            smsMarketEntity.setDesDept(0);
            smsMarketEntity.setDesStore(2);
        }


        Long couponPackageId = smsMarket.getCouponPackageId();
        Long businessId = BusinessContextHolder.getRequiredBusinessId();
        try {
            String longUrl = this.generateUrlLink(WechatJumpParam.builder().path(sortPath).businessId(businessId).query("id=" + couponPackageId+ "&marketId=" + smsMarket.getId()).build());
            ShortUrlResponse response = getSortUrl(longUrl);
            String sortUrl = response.getShortUrl();
            String shortCode = response.getShortCode();
            Long templateId = smsMarketEntity.getTemplateId();
            SmsTemplateDO smsTemplate = smsTemplateService.getById(templateId);
            smsMarketEntity.setTemplateContent(smsTemplate.getTemplateContent() +" "+ sortUrl);
            smsMarketEntity.setShortCode(shortCode);
        }catch (Exception e){
            throw exception(WECHAT_TOKEN_ERROR);
        }

        boolean save = this.updateById(smsMarketEntity);
        Long id = smsMarketEntity.getId();
        Integer desDept = smsMarketEntity.getDesDept();
        if(ObjectUtil.isNotEmpty(desDept) && desDept == 1){
            List<SmsMarketDeptDO> desDepts = smsMarket.getSmsMarketDepts();
            if(CollectionUtil.isNotEmpty(desDepts)){
                List<SmsMarketDeptDO> smsMarketDepts = desDepts.stream().map(item -> {
                    SmsMarketDeptDO smsMarketDept = new SmsMarketDeptDO();
                    smsMarketDept.setDeptId(item.getDeptId());
                    smsMarketDept.setSmsMarketId(id);
                    return smsMarketDept;
                }).toList();
                QueryWrapper<SmsMarketDeptDO> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(SmsMarketDeptDO::getSmsMarketId,id);
                smsMarketDeptService.remove(queryWrapper);
                smsMarketDeptService.insertBatch(smsMarketDepts);
            }
        }

        Integer desStore = smsMarketEntity.getDesStore();
        if(ObjectUtil.isNotEmpty(desStore) && desStore == 1){
            List<SmsMarketStoreDO> desStores = smsMarket.getSmsMarketStores();
            if(CollectionUtil.isNotEmpty(desStores)){
                List<SmsMarketStoreDO> smsMarketStores = desStores.stream().map(item -> {
                    SmsMarketStoreDO smsMarketStore = new SmsMarketStoreDO();
                    smsMarketStore.setStoreId(item.getStoreId());
                    smsMarketStore.setStoreName(item.getStoreName());
                    smsMarketStore.setSmsMarketId(id);
                    smsMarketStore.setId(null);
                    return smsMarketStore;
                }).toList();
                QueryWrapper<SmsMarketStoreDO> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(SmsMarketStoreDO::getSmsMarketId,id);
                smsMarketStoreService.remove(queryWrapper);
                smsMarketStoreService.insertBatch(smsMarketStores);
            }
        }
        return save;
    }

    @Override
    public Boolean copy(Long id) {
        return null;
    }

    @Override
    public Boolean sendMessage(SmsSendMessageReqVO sendMessage) {
        List<String> groupedPhoneNumbers = new ArrayList<>();
        Integer sendMethod = sendMessage.getSendMethod();
        Long id = sendMessage.getId();
        SmsMarketDO byId = getById(id);
        if(sendMethod == 1){
            QueryWrapper<SmsMarketDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(SmsMarketDO::getSendTime, sendMessage.getSendTime());
            // 未发送
            queryWrapper.lambda().eq(SmsMarketDO::getSendStatus, 1);
            queryWrapper.lambda().ne(SmsMarketDO::getId, id);
            if(this.count(queryWrapper) > 0){
                throw exception(TIME_ERROR);
            }
            Long userId = WebFrameworkUtils.getLoginUserId();
            TimeRangeChecker.timeCheck(sendMessage.getSendTime());
            byId.setSendTime(sendMessage.getSendTime());
            byId.setSendMethod(sendMethod);
            byId.setOperUser(userId.toString());
            return updateById(byId);
        }
//        else {

//            groupedPhoneNumbers.add("13194235615");
//            groupedPhoneNumbers.add("13543437336");
//            groupedPhoneNumbers.add("13940562934");
//            groupedPhoneNumbers.add("18501662924");
//            groupedPhoneNumbers.add("18202489118");
//            groupedPhoneNumbers.add("15640572470");
//            groupedPhoneNumbers.add("18940536673");
//            groupedPhoneNumbers.add("17612441124");
//            groupedPhoneNumbers.add("15841307074");
//            groupedPhoneNumbers.add("18624318321");
//            groupedPhoneNumbers.add("13149747939");
//            groupedPhoneNumbers.add("15545443203");
//            groupedPhoneNumbers.add("18947601124");
//            groupedPhoneNumbers.add("13252851232");
//            groupedPhoneNumbers.add("17610690903");
//            groupedPhoneNumbers.add("17190015121");
//            groupedPhoneNumbers.add("15042917353");
//            groupedPhoneNumbers.add("18524326910");
//            groupedPhoneNumbers.add("18642076369");
//            groupedPhoneNumbers.add("13352401243");
//            groupedPhoneNumbers.add("18641987745");
//            groupedPhoneNumbers.add("18525078452");
//            groupedPhoneNumbers.add("13390243210");
//            groupedPhoneNumbers.add("18512496031");
//            groupedPhoneNumbers.add("13166707512");
//            groupedPhoneNumbers.add("17741332030");
//            groupedPhoneNumbers.add("13074136625");

//            groupedPhoneNumbers.add("13194235615");
//            groupedPhoneNumbers.add("13543437336");
//            groupedPhoneNumbers.add("13940562934");
//            groupedPhoneNumbers.add("18501662924");
//            groupedPhoneNumbers.add("18202489118");
//            groupedPhoneNumbers.add("15640572470");
//            groupedPhoneNumbers.add("18940536673");
//            groupedPhoneNumbers.add("17612441124");
//            groupedPhoneNumbers.add("15841307074");
//            groupedPhoneNumbers.add("18624318321");
//            groupedPhoneNumbers.add("13149747939");
//            groupedPhoneNumbers.add("15545443203");
//            groupedPhoneNumbers.add("18947601124");
//            groupedPhoneNumbers.add("13252851232");
//            groupedPhoneNumbers.add("17610690903");
//            groupedPhoneNumbers.add("17190015121");
//            groupedPhoneNumbers.add("15042917353");
//            groupedPhoneNumbers.add("18524326910");
//            groupedPhoneNumbers.add("18642076369");
//            groupedPhoneNumbers.add("15304001307");
//
//            extracted(groupedPhoneNumbers, id);
//            updateChain().of(SmsMarket.class).set(SmsMarket::getSendTime,LocalDateTime.now())
//                    .set(SmsMarket::getSendNum,groupedPhoneNumbers.size())
//                    .eq(SmsMarket::getId,id).update();
//        }



//        if(1 == 1){
//            return true;
//        }
        TimeRangeChecker.timeCheckScheduled(LocalDateTime.now());
        byId.setSendTime(LocalDateTime.now());
        byId.setSendMethod(sendMethod);
        Long userId = WebFrameworkUtils.getLoginUserId();
        byId.setOperUser(userId.toString());
        return extracted(id, byId);
    }


    private boolean extracted(Long id, SmsMarketDO byId) {
        Set<String> result = new HashSet<>();

        Integer sendPeople = byId.getSendPeople();
        if(sendPeople == 0){
            Set<Long> ids = getMemberIds();
            Map<Integer, List<Long>> grouped = ids.stream()
                    .collect(Collectors.groupingBy(
                            num -> (int)(num % 10),
                            Collectors.toList()
                    ));
            grouped.forEach((key, value) -> {
                Set<String> list = wxMemberApi.getSmsPhone(key, value);
                result.addAll(list);
            });
            redisCache.opsForSet(MARKET_USER_KEY + id, ids);
        }else {
            String crowdId = byId.getCrowdId();
            Set<Long> ids = new HashSet<>();
            List<WxMemberCrowdDTO> wxMemberDTOS = crowdApi.getMemberDataByCrowdId(crowdId);
            wxMemberDTOS.forEach(wxMemberDTO -> {
                result.add(wxMemberDTO.getMemberMobile());
                ids.add(wxMemberDTO.getMemberId());
            });
            redisCache.opsForSet(MARKET_USER_KEY + id, ids);
        }
        boolean b = false;

        result.add("13194235615");
        result.add("13543437336");
        result.add("13940562934");

        List<String> phoneNumbers = new ArrayList<>(result);
        int size = phoneNumbers.size();
        int failNum = 0;

        int batchSize = 100;
        // 使用 ListUtils.partition 分组
        List<List<String>> groupedPhoneNumbers = ListUtils.partition(phoneNumbers, batchSize);

        String shortCode = byId.getShortCode();
        for (List<String> groupedPhoneNumber : groupedPhoneNumbers) {

            try {
                List<String> signNames = Collections.nCopies(groupedPhoneNumber.size(), signName);
                Map<String, String> stringStringMap = objectMapper.readValue(templateParam, new TypeReference<Map<String, String>>() {});
                stringStringMap.put("code", shortCode);
                List<Map<String, String>> templateParams = Collections.nCopies(groupedPhoneNumber.size(), stringStringMap);
                String s = smsService.sendBatch(groupedPhoneNumber, signNames, templateCode, templateParams);
                if("fail".equals(s)){
                    failNum += groupedPhoneNumber.size();
                    List<SmsMarketPhoneDO> list = groupedPhoneNumber.stream().map(item -> {
                        SmsMarketPhoneDO smsMarketPhone = new SmsMarketPhoneDO();
                        smsMarketPhone.setPhone(item);
                        smsMarketPhone.setSmsTemplateId(id);
                        smsMarketPhone.setTimes(1);
                        return smsMarketPhone;
                    }).toList();
                    smsMarketPhoneService.insertBatch(list);
                }else {
                    byId.setSendStatus(0);
                }
            }catch (Exception e){
                List<SmsMarketPhoneDO> list = groupedPhoneNumber.stream().map(item -> {
                    SmsMarketPhoneDO smsMarketPhone = new SmsMarketPhoneDO();
                    smsMarketPhone.setPhone(item);
                    smsMarketPhone.setSmsTemplateId(id);
                    smsMarketPhone.setTimes(1);
                    return smsMarketPhone;
                }).toList();
                smsMarketPhoneService.insertBatch(list);
            }
        }
        byId.setSendNum(size - failNum);
        this.updateById(byId);
        return b;
    }

    /**
     * 获取指定日期之前的所有会员id
     * @return
     */
    private Set<Long> getMemberIds() {
        List<Long> collect = storeIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // 获取当前年月
        YearMonth currentYearMonth = YearMonth.now();

        // 设置起始年月 (2025年2月)
        YearMonth startYearMonth = YearMonth.of(2025, 2);

        // 生成月份列表
        List<String> months = generateMonths(startYearMonth, currentYearMonth);

        Set<Long> phoneNumbers = new HashSet<>();
        // 遍历月份列表
        for (String month : months) {
            // 获取指定月份的会员手机号
            log.info(">>> 获取指定门店，storeIds={}", storeIds);

            Set<Long> memberPhoneNumbers = bzOrderApi.getHisPhones(month,collect);
            phoneNumbers.addAll(memberPhoneNumbers);
        }

        LocalDate afterDayDate = DateUtils.getAfterDayDate(8);
        String afterDayDateToString = DateUtils.getAfterDayDateToString(8);
        Set<Long> memberPhoneNumbers = bzOrderApi.getWeekPhones(afterDayDateToString,afterDayDate,collect);
        Set<Long> difference = CollectionUtils.subtract(phoneNumbers, memberPhoneNumbers).stream().collect(Collectors.toSet());
        return difference;
    }



    @Override
    public Boolean scheduledSend() {

        String time = getTime();
        QueryWrapper<SmsMarketDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SmsMarketDO::getSendTime, time)
        .eq(SmsMarketDO::getSendStatus, 1)
        .eq(SmsMarketDO::getSendMethod,1);
        SmsMarketDO smsMarket = getOne(queryWrapper);

        if(ObjectUtil.isEmpty(smsMarket)){
            return Boolean.TRUE;
        }

        Long id = smsMarket.getId();
        List<String> groupedPhoneNumbers = new ArrayList<>();
        groupedPhoneNumbers.add("13194235615");
        groupedPhoneNumbers.add("13543437336");
        groupedPhoneNumbers.add("13940562934");

        groupedPhoneNumbers.add("15304001307");
        groupedPhoneNumbers.add("18202489118");
        groupedPhoneNumbers.add("15640572470");
        groupedPhoneNumbers.add("18940536673");

        List<WxMemberDTO> wxMembers = wxMemberApi.getMemberByMobiles(groupedPhoneNumbers);
        List<Long> ids = wxMembers.stream().map(WxMemberDTO::getMemberId).toList();
        redisCache.opsForSet(MARKET_USER_KEY + id, new HashSet<>(ids));
        SmsMarketDO byId = this.getById(id);
        String shortCode = byId.getShortCode();
        try {
            List<String> signNames = Collections.nCopies(groupedPhoneNumbers.size(), signName);
            Map<String, String> stringStringMap = objectMapper.readValue(templateParam, new TypeReference<Map<String, String>>() {});
            stringStringMap.put("code", shortCode);
            List<Map<String, String>> templateParams = Collections.nCopies(groupedPhoneNumbers.size(), stringStringMap);

            log.info("发送短信：{}{}{}{}",signNames, groupedPhoneNumbers, templateCode, templateParams);
            String s = smsService.sendBatch(groupedPhoneNumbers, signNames, templateCode, templateParams);
            if("fail".equals(s)){
                List<SmsMarketPhoneDO> list = groupedPhoneNumbers.stream().map(item -> {
                    SmsMarketPhoneDO smsMarketPhone = new SmsMarketPhoneDO();
                    smsMarketPhone.setPhone(item);
                    smsMarketPhone.setSmsTemplateId(id);
                    return smsMarketPhone;
                }).toList();
                smsMarketPhoneService.insertBatch(list);
                throw exception(SEND_ERROR);
            }
            return Boolean.TRUE;
        }catch (Exception e){
            return Boolean.FALSE;
        }
    }

    @Override
    public Boolean preSend(PreMessageReqVO preMessage) {
        Long id = preMessage.getId();
        List<String> groupedPhoneNumbers = new ArrayList<>(List.of(preMessage.getMobile()));
        groupedPhoneNumbers.add("13194235615");
        groupedPhoneNumbers.add("13543437336");
        groupedPhoneNumbers.add("13940562934");

//        groupedPhoneNumbers.add("13352401243");
//        groupedPhoneNumbers.add("18202489118");
//        groupedPhoneNumbers.add("15640572470");
//        groupedPhoneNumbers.add("18940536673");
//
//        groupedPhoneNumbers.add("17612441124");
//        groupedPhoneNumbers.add("15841307074");
//
//        groupedPhoneNumbers.add("18624318321");
//        groupedPhoneNumbers.add("13149747939");
//        groupedPhoneNumbers.add("15545443203");
//        groupedPhoneNumbers.add("18947601124");
//
//        groupedPhoneNumbers.add("13252851232");
//        groupedPhoneNumbers.add("17610690903");
//        groupedPhoneNumbers.add("17190015121");
//        groupedPhoneNumbers.add("15042917353");
//        groupedPhoneNumbers.add("18524326910");
//
//        groupedPhoneNumbers.add("18642076369");

        return extracted(groupedPhoneNumbers, id);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean extracted(List<String> groupedPhoneNumbers, Long id) {
        // 参数校验
        if (CollectionUtils.isEmpty(groupedPhoneNumbers)) {
            throw new IllegalArgumentException("手机号列表不能为空");
        }
        if (id == null) {
            throw new IllegalArgumentException("营销活动ID不能为空");
        }

        // 1. 获取微信会员信息
        List<WxMemberDTO> wxMembers = wxMemberApi.getMemberByMobiles(groupedPhoneNumbers);
        Set<Long> memberIds = wxMembers.stream()
                .map(WxMemberDTO::getMemberId)
                .collect(Collectors.toSet());

        // 2. 获取营销活动信息
        SmsMarketDO smsMarket = this.getById(id);
        if (smsMarket == null) {
            throw exception(SEND_ERROR); // 自定义异常
        }

        // 3. 准备短信参数
        Map<String, String> templateParams = prepareTemplateParams(smsMarket.getShortCode());
        List<String> signNames = Collections.nCopies(groupedPhoneNumbers.size(), signName);
        List<Map<String, String>> paramsList = Collections.nCopies(groupedPhoneNumbers.size(), templateParams);

        // 4. 发送短信
        try {
            log.info("准备发送短信，活动ID: {}, 手机号数量: {}", id, groupedPhoneNumbers.size());
            String sendResult = smsService.sendBatch(groupedPhoneNumbers, signNames, templateCode, paramsList);

            if ("fail".equals(sendResult)) {
                log.error("短信发送失败，活动ID: {}", id);
                handleFailedSend(groupedPhoneNumbers, id);
                throw exception(SEND_ERROR);
            }

            // 5. 发送成功后的处理
            MarketUpdatedEvent event = new MarketUpdatedEvent(groupedPhoneNumbers, id, memberIds);
            handleSuccessfulSend(event);
            return true;
        } catch (Exception e) {
            log.error("短信发送异常，活动ID: {}, 错误信息: {}", id, e.getMessage(), e);
            handleFailedSend(groupedPhoneNumbers, id);
            throw exception(SEND_ERROR);
        }
    }


    /**
     * 准备短信模板参数
     */
    private Map<String, String> prepareTemplateParams(String shortCode) {
        try {
            Map<String, String> params = objectMapper.readValue(templateParam,
                    new TypeReference<Map<String, String>>() {});
            params.put("code", shortCode);
            return params;
        } catch (JsonProcessingException e) {
            log.error("解析短信模板参数失败", e);
            throw exception(SEND_ERROR);
        }
    }


    /**
     * 处理发送成功后的逻辑
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSuccessfulSend(MarketUpdatedEvent event) {
        List<String> phoneNumbers = event.getPhoneNumbers();
        Long marketId = event.getMarketId();
        Set<Long> memberIds = event.getMemberIds();
        // 更新发送统计（异步执行）
//        CompletableFuture.runAsync(() -> {
//            updateChain().of(SmsMarket.class)
//                    .set(SmsMarket::getSendTime, LocalDateTime.now())
//                    .set(SmsMarket::getSendNum, phoneNumbers.size())
//                    .eq(SmsMarket::getId, marketId)
//                    .update();
//        });
        try {
            redisCache.opsForSet(MARKET_USER_KEY + marketId, memberIds);
            log.info("Redis缓存更新成功，活动ID: {}", marketId);
        } catch (Exception e) {
            log.error("Redis缓存更新失败，活动ID: {}", marketId, e);
        }
//
//        // Redis缓存更新（事务提交后执行）
//        TransactionSynchronizationManager.registerSynchronization(
//                new TransactionSynchronization() {
//                    @Override
//                    public void afterCommit() {
//
//                    }
//                }
//        );
    }


    /**
     * 处理发送失败的逻辑
     */
    private void handleFailedSend(List<String> phoneNumbers, Long marketId) {
        List<SmsMarketPhoneDO> failedRecords = phoneNumbers.stream()
                .map(phone -> new SmsMarketPhoneDO()
                        .setPhone(phone)
                        .setSmsTemplateId(marketId))
                .collect(Collectors.toList());

        // 批量插入失败记录
        try {
            smsMarketPhoneService.saveBatch(failedRecords);
            log.info("保存失败记录成功，数量: {}", failedRecords.size());
        } catch (Exception e) {
            log.error("保存失败记录异常", e);
        }
    }

//
//    private boolean extracted(List<String> groupedPhoneNumbers, Long id) {
//        List<WxMemberDTO> wxMembers = wxMemberApi.getMemberByMobiles(groupedPhoneNumbers);
//        List<Long> ids = wxMembers.stream().map(WxMemberDTO::getMemberId).toList();
//        redisCache.opsForSet(MARKET_USER_KEY + id, new HashSet<>(ids));
//
//        SmsMarketDO byId = this.getById(id);
//        String shortCode = byId.getShortCode();
//
//        try {
//            List<String> signNames = Collections.nCopies(groupedPhoneNumbers.size(), signName);
//            Map<String, String> stringStringMap = objectMapper.readValue(templateParam, new TypeReference<Map<String, String>>() {});
//            stringStringMap.put("code", shortCode);
//            List<Map<String, String>> templateParams = Collections.nCopies(groupedPhoneNumbers.size(), stringStringMap);
//            log.info("发送短信：{}{}{}{}",signNames, groupedPhoneNumbers, templateCode, templateParams);
//            String s = smsService.sendBatch(groupedPhoneNumbers, signNames, templateCode, templateParams);
//            if("fail".equals(s)){
//                List<SmsMarketPhoneDO> list = groupedPhoneNumbers.stream().map(item -> {
//                    SmsMarketPhoneDO smsMarketPhone = new SmsMarketPhoneDO();
//                    smsMarketPhone.setPhone(item);
//                    smsMarketPhone.setSmsTemplateId(id);
//                    return smsMarketPhone;
//                }).toList();
//                smsMarketPhoneService.insertBatch(list);
//                throw exception(SEND_ERROR);
//            }
////            updateChain().of(SmsMarket.class).set(SmsMarket::getSendTime,LocalDateTime.now())
////                    .set(SmsMarket::getSendNum,groupedPhoneNumbers.size())
////                    .eq(SmsMarket::getId,id).update();
//            return true;
//        }catch (Exception e){
//            List<SmsMarketPhoneDO> list = groupedPhoneNumbers.stream().map(item -> {
//                SmsMarketPhoneDO smsMarketPhone = new SmsMarketPhoneDO();
//                smsMarketPhone.setPhone(item);
//                smsMarketPhone.setSmsTemplateId(id);
//                return smsMarketPhone;
//            }).toList();
//            smsMarketPhoneService.insertBatch(list);
//            throw exception(SEND_ERROR);
//        }
//    }
//


    @Override
    public SmsMarketPageViewVO getPv(Long id) {
        SmsMarketPageViewVO smsMarketPageViewVO = new SmsMarketPageViewVO();
        Integer totalPv = pvStatisticsService.getTotalPv(id);
        Long totalUv = pvStatisticsService.getTotalUv(id);
        smsMarketPageViewVO.setPv(totalPv);
        smsMarketPageViewVO.setUv(totalUv);
        return smsMarketPageViewVO;
    }

    @Override
    public Boolean insertPv(AddPvReqVO reqVO) {
        pvStatisticsService.recordVisit(reqVO);
        return Boolean.TRUE;
    }

    @Override
    public Boolean claimCouponPackage(ClaimCouponPackageReqVO reqVO) {
        // 限流
        boolean acquire = couponPackageLimiter.tryAcquire();
        if (!acquire) {
            throw exception(CLAIM_COUPON_LIMITER);
        }
        Long userId = reqVO.getMemberId();
        Boolean exists = redisCache.setIsMember(MARKET_USER_KEY + reqVO.getMarketId(), userId);
        if (!exists){
            throw exception(MEMBER_NOT_ALLOWED);
        }

        Long packageId = reqVO.getPackageId();

        // 查询用户信息
        CommonResult<WxMemberDTO> result = wxMemberApi.getMemberById(userId);
        WxMemberDTO wxMember = result.getData();
        if (ObjectUtil.isEmpty(wxMember)) {
            throw exception(COUPON_PACKAGE_NO_MOBILE);
        }
        String memberMobile = wxMember.getMemberMobile();
        if (ObjectUtil.isEmpty(memberMobile)) {
            throw exception(COUPON_PACKAGE_NO_MOBILE);
        }
        // 优惠券包id
        if (ObjectUtil.isEmpty(packageId)) {
            throw exception(COUPON_EXPIRED);
        }

        // 判断是否领取过
        if (pvStatisticsService.isClaimCouponPackage(packageId, userId)) {
            throw exception(COUPON_OVER_LIMIT_0);
        }

        CouponPackageDO couponPackage = couponPackageMapper.selectById(packageId);
        Integer packageNum = couponPackage.getPackageNum();
        if(packageNum <= 0){
            throw exception(COUPON_OVER);
        }
        // 查询优惠券包
        QueryWrapper<GoodCouponPackageDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(GoodCouponPackageDO::getId, packageId);
        List<GoodCouponPackageDO> goodCouponPackages = goodCouponPackageMapper.selectList(queryWrapper);
        List<Long> couponIds = goodCouponPackages.stream().map(goodCouponPackage -> goodCouponPackage.getCouponId()).toList();
        QueryWrapper<GoodCouponDO> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.lambda().in(GoodCouponDO::getId, couponIds);
        List<GoodCouponDO> list = goodCouponMapper.selectList(queryWrapper1);
        Map<Long, Integer> couponNumMap = goodCouponPackages.stream().collect(Collectors.toMap(GoodCouponPackageDO::getCouponId, GoodCouponPackageDO::getNum));
        List<UserCouponDO> userCoupons = new ArrayList<>();
        UserCouponDO userCoupon = new UserCouponDO();
        try {
            for (GoodCouponDO goodCoupon : list) {
                Long goodCouponId = goodCoupon.getId();
                Integer num = couponNumMap.get(goodCoupon.getId());
                for (int i = 0; i < num; i++) {
                    userCoupon = new UserCouponDO();
                    BeanUtil.copyProperties(goodCoupon, userCoupon);
                    userCoupon.setUserId(wxMember.getMemberId());
                    userCoupon.setCouponId(goodCouponId);
                    userCoupon.setCreateTime(LocalDateTime.now());
                    userCoupon.setCouponUseTime(goodCoupon.getUseTime());
                    userCoupon.setUseTime(null);
                    userCoupon.setCouponCreateTime(goodCoupon.getCreateTime());
                    userCoupon.setId(null);
                    userCoupon.setIsUsed(0);
                    CouponTimeUtil.parseCouponTime(goodCoupon, userCoupon);
                    userCoupon.setDistributionMethod(0L);
                    userCoupon.setMemberMobile(memberMobile);
                    userCoupon.setCouponSource(CouponSourceConstant.COUPON_SOURCE_2);
                    userCoupon.setPackageId(packageId);
                    userCoupons.add(userCoupon);
                }

                log.info("claimCouponPackage接口,{}优惠券领取数量+{}", goodCoupon.getCouponName(), num);

                UpdateWrapper<GoodCouponDO> updateWrapper = new UpdateWrapper<>();
                updateWrapper.lambda().eq(GoodCouponDO::getId, goodCouponId);
                log.info("claimCouponPackage接口,{}优惠券领取数量+{}", goodCoupon.getCouponName(), num);
                updateWrapper.setSql("received_num = received_num +" + num);
                updateWrapper.setSql("coupon_num = coupon_num -" + num);
                goodCouponMapper.update(updateWrapper);
            }
            try {
                // 优惠券包减1
                UpdateWrapper<CouponPackageDO> updateWrapper = new UpdateWrapper<>();
                updateWrapper.lambda().eq(CouponPackageDO::getId, packageId);
                updateWrapper.setSql("received_num = received_num +1");
                updateWrapper.setSql("package_num = package_num -1");
                couponPackageMapper.update(updateWrapper);

                pvStatisticsService.claimCouponPackage(packageId, userId);
                return saveUserCoupon(userCoupons);
            } catch (Exception e) {
                throw exception(MEMBER_NOT_ALLOWED);
            }
        } catch (Exception e) {
            log.warn("优惠券包： {}当前领取人数过多，请稍后重试！", packageId);
            throw new RuntimeException("系统繁忙，请稍后重试！");
        }
    }

    public boolean saveUserCoupon(List<UserCouponDO> userCoupons) {
        return userCouponShardService.insertBatch(userCoupons);
    }

    @Override
    public Boolean testSend(PreMessageReqVO preMessage) {
        return null;
    }

    /**
     * 生成小程序短链接
     * @param wechatJump wechatJump
     * @return String
     */
    public String generateUrlLink(WechatJumpParam wechatJump) {
        int j = 0;
        //String wechatToken = weChatService.getWechatToken(true, wechatJump.getBusinessId().toString());
        //String token = "CLOUD_SECRET_REQUIRED";
        Long businessId = BusinessContextHolder.getRequiredBusinessId();
        //String accessToken = weChatService.getWechatToken(true, String.valueOf(businessId));
        String wechatToken = wxActionApi.getWechatToken(businessId);
        JSONObject json = postWxGenerateUrl(wechatJump,wechatToken);
        String code = json.getString(WechatJumpConstants.WECHAT_JUMP_ERRCODE);
        while (j < WechatJumpConstants.WECHAT_JUMP_TIMES && code.equals(WechatJumpConstants.WECHAT_JUMP_ERROR_CODE_40001)) {
            log.info("token失效重新获取：{}", json.toJSONString());
            j++;
            json = postWxGenerateUrl(wechatJump, weChatService.getWechatToken(false, wechatJump.getBusinessId().toString()));
            code = json.getString(WechatJumpConstants.WECHAT_JUMP_ERRCODE);
        }
        log.info("generateUrlLink返回地址：{}", json.toJSONString());
        return json.getString(WechatJumpConstants.WECHAT_JUMP_OPEN_LINK);
    }

    public JSONObject postWxGenerateUrl(WechatJumpParam wechatJump, String token) {
        Map<String, Object> params = new LinkedHashMap<>();
        Map<String, Object> jumpWxa = new LinkedHashMap<>();
        jumpWxa.put("path", wechatJump.getPath());
        jumpWxa.put("query", wechatJump.getQuery());
        jumpWxa.put("env_version", envVersion);
        params.put("jump_wxa", jumpWxa);
        params.put("expire_type", 0);
        // 固定参数
        params.put("expire_time", System.currentTimeMillis() / 1000 + 30 * 24 * 60 * 60);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(params);
            String post = HttpUtil.post(GENERATE_URL_LINK + token, json);
            //ResponseEntity<String> responseEntity = restTemplate.postForEntity(GENERATE_URL_LINK + token, httpEntity, String.class);
            return JSONObject.parseObject(post);
        }catch (Exception e){
            return null;
        }

    }

    private ShortUrlResponse getSortUrl(String longUrl) {
        ShortUrlRequest request = ShortUrlRequest.builder()
                .longUrl(longUrl)
                .tags(new ArrayList<>())
                .forwardQuery(true)
                .build();
        return shortUrlService.createShortUrl(request);
    }

    private String getTime(){
        LocalDateTime now = LocalDateTime.now();
        // 方法 2.1：直接格式化秒为 00
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00");
        return now.format(formatter);
    }


    /**
     * 生成指定时间范围内的月份列表
     * @param start
     * @param end
     * @return String
     */
    public List<String> generateMonths(YearMonth start, YearMonth end) {
        List<String> months = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

        // 确保开始日期不晚于结束日期
        if (start.isAfter(end)) {
            System.out.println("起始日期晚于结束日期，将交换两者");
            YearMonth temp = start;
            start = end;
            end = temp;
        }

        // 遍历从开始到结束的每个月
        YearMonth current = start;
        while (!current.isAfter(end)) {
            months.add(current.format(formatter));
            current = current.plusMonths(1);
        }

        return months;
    }

}
