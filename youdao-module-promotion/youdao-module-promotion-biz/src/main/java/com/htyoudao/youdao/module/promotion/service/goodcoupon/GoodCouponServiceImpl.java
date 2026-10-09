package com.htyoudao.youdao.module.promotion.service.goodcoupon;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.excel.core.service.listenner.GenericExcelListener;
import com.htyoudao.youdao.framework.mybatis.core.query.QueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.member.api.pointsproduct.PointsProductApi;
import com.htyoudao.youdao.module.member.api.wx.WxActionApi;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.VO.GoodCouponCardVO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.GoodCouponDataDTO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.MemberCouponDTO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.GoodCouponVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.CouponCommodityInfoVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.CouponStoreInfoVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.PointsProductCouponDetailVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.VO.AdvertisingStorePageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.tiktokgoodcoupon.vo.TiktokCouponSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.appletnotice.AppletNoticePushVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingstore.AdvertisingForStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstoreclaimnum.CouponStoreClaimNumDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.exchangecommodity.ExchangeCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcouponpackage.GoodCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponstore.CouponStoreMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.exchangecommodity.ExchangeCommodityMapper;
import com.htyoudao.youdao.module.promotion.enums.advertising.carousel.CouponISCommonEnum;
import com.htyoudao.youdao.module.promotion.enums.advertising.carousel.CouponISCommonStoreEnum;
import com.htyoudao.youdao.module.promotion.enums.channel.ActivityChannelTypeEnum;
import com.htyoudao.youdao.module.promotion.service.ActivitySeckillCoupon.ActivitySeckillCouponService;
import com.htyoudao.youdao.module.promotion.service.activityChannel.ActivityChannelService;
import com.htyoudao.youdao.module.promotion.service.activityJD.ActivityJDService;
import com.htyoudao.youdao.module.promotion.service.appletnotice.ICommonService;
import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageService;
import com.htyoudao.youdao.module.promotion.service.couponstoreclaimnum.CouponStoreClaimNumService;
import com.htyoudao.youdao.module.promotion.service.goodcouponpackage.GoodCouponPackageService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.UserCouponService;
import com.htyoudao.youdao.module.promotion.util.*;
import com.htyoudao.youdao.module.system.api.orgstore.OrgStoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreWecomConfigReqDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreWecomConfigResDTO;
import com.htyoudao.youdao.module.system.api.storeinfo.StoreInfoApi;
import com.htyoudao.youdao.module.system.api.storeinfo.dto.StorePageListReqVO;
import com.htyoudao.youdao.module.system.api.storeinfo.dto.StorePageResVO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.validation.Validator;
import com.htyoudao.youdao.module.promotion.constant.GoodCouponConstants;
import com.htyoudao.youdao.module.promotion.constant.UserCouponConstants;
import com.htyoudao.youdao.module.promotion.constant.WechatJumpConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponcommodity.CouponCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponshare.CouponShareDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponRecordDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponcommodity.CouponCommodityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcoupon.GoodCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponMapper;
import com.htyoudao.youdao.module.promotion.enums.AppletPushTemplateTypeEnum;
import com.htyoudao.youdao.module.promotion.enums.CouponStatusEnum;
import com.htyoudao.youdao.module.promotion.enums.CouponTypeEnum;
import com.htyoudao.youdao.module.promotion.service.couponcommodity.CouponCommodityService;
import com.htyoudao.youdao.module.promotion.service.couponshare.CouponShareService;
import com.htyoudao.youdao.module.promotion.service.couponstore.CouponStoreService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.UserCouponShardService;
import com.htyoudao.youdao.module.promotion.service.usercouponrecord.UserCouponRecordService;
import com.htyoudao.youdao.module.promotion.service.wechat.ShortUrlService;
import com.htyoudao.youdao.module.promotion.service.wechat.WeChatService;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.EXCEL_IMPORT_FILE_FAILED;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants.*;

/**
 * 优惠券 Service 实现类
 *
 * @author dht
 */
@Service
@Slf4j
@RefreshScope
public class GoodCouponServiceImpl extends ServiceImpl<GoodCouponMapper, GoodCouponDO> implements GoodCouponService {

    @Resource
    private GoodCouponMapper goodCouponMapper;

    @Resource
    private UserCouponRecordService userCouponRecordService;

    @Resource
    private CouponCommodityService couponCommodityService;

    @Resource
    private CouponStoreService couponStoreService;

    @Resource
    private CouponStoreMapper couponStoreMapper;

    @Resource
    private ActivityChannelService activityChannelService;

    @DubboReference
    private OrgStoreApi orgStoreApi;

    @Value("${coupon.sort.sortPath}")
    private String sortPath;

    @Value("${coupon.sort.h5.host}")
    private String h5Host;

    @Value("${wechat.schemeInfo.envVersion}")
    private String envVersion;

//    @Value("${wechat.access.token}")
//    private String accessToken;

    @Value("${coupon.littlePicUrl}")
    private String littlePicUrl;

    @Value("${coupon.largePicUrl}")
    private String largePicUrl;

    @Resource
    private Validator validator;

    @DubboReference
    private WxActionApi wxActionApi;

    /**
     * 优惠券详情的key
     */
    private static final String COUPON_DATA = "COUPON_DATA:";

    /**
     * 优惠券门店数量的key
     */
    private static final String COUPON_STORE_NUM = "COUPON_STORE_NUM:";


    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    //@Qualifier("remoteRestTemplate")
    private RestTemplate restTemplate;

    @Resource
    private ShortUrlService shortUrlService;

    @Resource
    private WeChatService weChatService;

    @DubboReference
    private StoreApi storeApi;

    @Resource
    private CouponShareService couponShareService;

    private static final String GENERATE_URL_LINK = "https://api.weixin.qq.com/wxa/generatescheme?access_token=";

    private final static String URL = "https://api.weixin.qq.com/wxa/genwxashortlink?access_token=";
    @Autowired
    private CouponCommodityMapper couponCommodityMapper;

    @DubboReference
    private WxMemberApi wxMemberApi;

    @DubboReference
    private PointsProductApi pointsProductApi;

    @Resource
    private UserCouponMapper userCouponMapper;

    @Resource
    private UserCouponService userCouponService;

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserCouponShardService userCouponShardService;

    @Resource
    private IdentifierGenerator identifierGenerator;

    @Resource
    private CouponStoreClaimNumService couponStoreClaimNumService;

    @Resource
    private GoodCouponPackageService goodCouponPackageService;

    @Resource
    @Lazy
    private CouponPackageService couponPackageService;

    @Resource
    private ICommonService commonService;

    private static final String COUPON_DETAIL = "COUPON_DETAIL:";

    private static final String IS_USE = "可用";
    private static final String IS_NOT_USE = "不可用";


    @DubboReference
    private StoreInfoApi storeInfoApi;

    @Resource
    private ActivitySeckillCouponService activitySeckillCouponService;

    @Resource
    private ExchangeCommodityMapper exchangeCommodityMapper;

    @Resource
    private ActivityJDService activityJDService;
    // 批次大小：推荐2000条/批，平衡性能和内存
    private static final int BATCH_SIZE = 2000;
    // 发券重试次数
    private static final int SEND_COUPON_RETRY_TIMES = 3;
    // 重试间隔（毫秒）
    private static final long RETRY_INTERVAL_MS = 500;
    @Override
    public PageResult<GoodCouponPageRespVO> selectGoodCouponListPage(GoodCouponPageReqVO goodCoupon) {
        PageResult<GoodCouponDO> pageResult = goodCouponMapper.selectPage(goodCoupon);
        PageResult<GoodCouponPageRespVO> result = BeanUtils.toBean(pageResult, GoodCouponPageRespVO.class);
        List<GoodCouponPageRespVO> list = result.getList();
        if (CollectionUtil.isNotEmpty(list)) {
            List<Long> ids = list.stream().map(GoodCouponPageRespVO::getId).toList();
            List<UserCouponRecordDO> userCouponRecords = userCouponRecordService.listByCouponIds(ids);
            if (CollectionUtil.isNotEmpty(userCouponRecords)) {
                Map<Long, BigDecimal> sumByGroup = userCouponRecords.stream()
                        .collect(Collectors.groupingBy(
                                UserCouponRecordDO::getCouponId,
                                Collectors.reducing(BigDecimal.ZERO, UserCouponRecordDO::getTotalAmount, BigDecimal::add)
                        ));
                list.forEach(item -> item.setPayAmount(sumByGroup.getOrDefault(item.getId(), BigDecimal.ZERO)));
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = PROMOTION_GOOD_COUPON_TYPE, subType = PROMOTION_GOOD_COUPON_CREATE_SUB_TYPE, bizNo = "{{1}}", success = PROMOTION_GOOD_COUPON_CREATE_SUCCESS)
    public Long createCoupon(GoodCouponSaveReqVO createReqVO) {
        GoodCouponDO goodCouponDO = new GoodCouponDO();
        BeanUtils.copyProperties(createReqVO, goodCouponDO);
        if (ObjectUtil.isEmpty(createReqVO.getUseType()) || ObjectUtil.isEmpty(createReqVO.getUseTime())) {
            throw exception(COUPON_USE_TIME_ERROR);
        }

        goodCouponDO.setCouponName(createReqVO.getCouponName());
        this.parseCouponUseTime(createReqVO, goodCouponDO);

        goodCouponDO.setDistributionMethod(GoodCouponConstants.DISTRIBUTE_METHOD_1);

        if (ObjectUtil.isEmpty(createReqVO.getIsGround())) {
            goodCouponDO.setIsGround(GoodCouponConstants.IS_GROUND_0);
        }

        if (ObjectUtil.isEmpty(createReqVO.getDayLimit())) {
            goodCouponDO.setDayLimit(GoodCouponConstants.DAY_LIMIT_0);
        }

        long time = new DateTime().getTime();
        String couponCode = GoodCouponConstants.COUPON_CODE_PREFIX + time;

        goodCouponDO.setCouponCode(couponCode);
        // 领取数量
        goodCouponDO.setReceivedNum(0);
        // 使用数量
        goodCouponDO.setUsedNum(0);

        goodCouponDO.setCouponNum(createReqVO.getTotalNum());
        if (ObjectUtil.isEmpty(createReqVO.getStoreLimitNum())) {
            //默认不限制
            goodCouponDO.setStoreLimitNum(0);
        }

//        SnowflakeGenerator snowflakeGenerator = new SnowflakeGenerator(6, 1);
//        Long id = snowflakeGenerator.next();
        Number number = identifierGenerator.nextId(goodCouponDO);
        Long id = number.longValue();

        Integer isCommonStore = createReqVO.getIsCommonStore();
        List<CouponCommodityDO> couponCommodities = createReqVO.getCouponCommodities();

        if (isCommonStore != GoodCouponConstants.IS_COMMON_STORE_1) {
            if (ObjectUtil.isNotEmpty(couponCommodities)) {
                //优惠券绑定商品 单一商品才绑定
                if (couponCommodities.size() == 1 && !isCommonStore.equals(GoodCouponConstants.IS_COMMON_STORE_3)) {
                    goodCouponDO.setSingleIds(couponCommodities.get(0).getCommodityId().toString());
                }
                if((Objects.equals(createReqVO.getCouponType(), GoodCouponConstants.COUPON_TYPE_2.toString()) || Objects.equals(createReqVO.getCouponType(), GoodCouponConstants.COUPON_TYPE_5.toString()))
                        && couponCommodities.size() > 1){
                    throw exception(COUPON_COMMODITY_ERROR);
                }
                couponCommodities.forEach(couponCommodity -> {
                    couponCommodity.setCouponId(id);
                    couponCommodity.setType(isCommonStore);
                    couponCommodity.setId(null);
                    couponCommodity.setDeleted(Boolean.FALSE);
                });
                //优惠券与门店关联
                couponCommodityService.insertBatch(couponCommodities);
            }
        }
        goodCouponDO.setId(id);

        List<ExchangeCommodityReqVO> exchangeCommodityList = createReqVO.getExchangeCommodityList();
        if (ObjectUtil.isNotEmpty(exchangeCommodityList) && (goodCouponDO.getCouponType().equals(2) || goodCouponDO.getCouponType().equals(5))) {
            List<ExchangeCommodityDO> list = exchangeCommodityList.stream().map(item -> {
                ExchangeCommodityDO exchangeCommodityDO = new ExchangeCommodityDO();
                exchangeCommodityDO.setCouponId(id);
                exchangeCommodityDO.setCommodityId(item.getCommodityId());
                exchangeCommodityDO.setCommodityName(item.getCommodityName());
                exchangeCommodityDO.setId(null);
                return exchangeCommodityDO;
            }).toList();
            exchangeCommodityMapper.insertBatch(list);
            goodCouponDO.setExchangeFlag(GoodCouponConstants.EXCHANGE_FLAG_1);
        } else {
            goodCouponDO.setExchangeFlag(GoodCouponConstants.EXCHANGE_FLAG_0);
        }

        if(goodCouponDO.getCouponType().equals(5)){
            if(ObjectUtil.isEmpty(createReqVO.getDoorsillType()) || ObjectUtil.isEmpty(createReqVO.getDiscount()) || ObjectUtil.isEmpty(createReqVO.getReduceAmount())){
                throw exception(FIELD_NOT_NULL);
            }
        }

        Long businessId = BusinessContextHolder.getRequiredBusinessId();

        /*try {
            String longUrl = this.generateUrlLink(WechatJumpParam.builder().path(sortPath).businessId(businessId).query("couponId=" + id).build());
            //微信小程序短链接
            goodCouponDO.setMiniSortUrl(this.getSortUrl(longUrl));
            //H5短链接
            goodCouponDO.setH5SortUrl(this.getSortUrl(h5Host + id));
        } catch (Exception e) {
            throw exception(WECHAT_TOKEN_ERROR);
        }*/


        //优惠券绑定门店
        Integer isCommon = createReqVO.getIsCommon();
        Integer storeTagFlag = createReqVO.getStoreTagFlag();
        if(ObjectUtil.isEmpty(storeTagFlag)){
            storeTagFlag = GoodCouponConstants.STORE_TAG_FLAG_1;
            goodCouponDO.setStoreTagFlag(storeTagFlag);
        }else if(storeTagFlag.equals(GoodCouponConstants.STORE_TAG_FLAG_0)){
            goodCouponDO.setIsCommon(GoodCouponConstants.IS_COMMON_2);
        }

        goodCouponMapper.insert(goodCouponDO);

        activityChannelService.createChannelDO(goodCouponDO.getId(), ActivityChannelTypeEnum.COUPON.getCode());



        Map<Long, Integer> redisMap = new HashMap<>();
        if (isCommon.equals(GoodCouponConstants.IS_COMMON_2)) {
            List<CouponStoreDO> couponStores = createReqVO.getCouponStores();
            if (ObjectUtil.isNotEmpty(couponStores)) {
                Integer storeLimitNum = goodCouponDO.getStoreLimitNum();
                if(ObjectUtil.isNotEmpty(storeLimitNum) && storeLimitNum > 0){
                    couponStores.forEach(item -> {
                        item.setId(null);
                        item.setCouponId(id);
                        item.setTotalNum(storeLimitNum);
                        item.setStoreId(item.getStoreId());
                        item.setDeleted(Boolean.FALSE);
                        redisMap.put(item.getStoreId(), item.getTotalNum());
                    });
                    couponStoreService.insertBatch(couponStores);
                    CouponCountUtil.initCouponStoreQuantities(redisTemplate,id,redisMap);
                }else{
                    couponStores.forEach(item -> {
                        item.setId(null);
                        item.setCouponId(id);
                        item.setStoreId(item.getStoreId());
                        item.setDeleted(Boolean.FALSE);
                    });
                    couponStoreService.insertBatch(couponStores);
                }

            }
        }


        if (storeTagFlag.equals(GoodCouponConstants.STORE_TAG_FLAG_0)) {
            List<CouponStoreDO> couponStores = new ArrayList<>();
            List<Long> storeTagIds = createReqVO.getStoreTagIds();
            if(CollectionUtil.isNotEmpty(storeTagIds)){
                Map<Long, List<StoreInfoDTO>> storeIdsByTagIdsMap = storeApi.getStoreIdsByTagIds(storeTagIds);
                Integer storeLimitNum = goodCouponDO.getStoreLimitNum();
                if(ObjectUtil.isNotEmpty(storeLimitNum) && storeLimitNum > 0){
                    storeIdsByTagIdsMap.forEach((k,v)->{
                        if(CollectionUtil.isEmpty(v)){
                            CouponStoreDO couponStoreDO = new CouponStoreDO();
                            couponStoreDO.setCouponId(id);
                            couponStoreDO.setStoreId(0L);
                            couponStoreDO.setStoreName("");
                            couponStoreDO.setDeleted(Boolean.FALSE);
                            couponStoreDO.setTotalNum(storeLimitNum);
                            couponStoreDO.setTagId(k);
                            couponStores.add(couponStoreDO);
                            redisMap.put(0L, storeLimitNum);
                        }else {
                            for (StoreInfoDTO store : v) {
                                CouponStoreDO couponStoreDO = new CouponStoreDO();
                                couponStoreDO.setCouponId(id);
                                couponStoreDO.setStoreId(store.getStoreId());
                                couponStoreDO.setStoreName(store.getStoreName());
                                couponStoreDO.setDeleted(Boolean.FALSE);
                                couponStoreDO.setTotalNum(storeLimitNum);
                                couponStoreDO.setTagId(k);
                                couponStores.add(couponStoreDO);
                                redisMap.put(store.getStoreId(), storeLimitNum);
                            }
                        }
                    });
                    CouponCountUtil.initCouponStoreQuantities(redisTemplate,id,redisMap);
                }else{
                    storeIdsByTagIdsMap.forEach((k,v)->{
                        if(CollectionUtil.isEmpty(v)){
                            CouponStoreDO couponStoreDO = new CouponStoreDO();
                            couponStoreDO.setCouponId(id);
                            couponStoreDO.setStoreId(0L);
                            couponStoreDO.setStoreName("");
                            couponStoreDO.setDeleted(Boolean.FALSE);
                            couponStoreDO.setTagId(k);
                            couponStores.add(couponStoreDO);
                        }else {
                            for (StoreInfoDTO store : v) {
                                CouponStoreDO couponStoreDO = new CouponStoreDO();
                                couponStoreDO.setCouponId(id);
                                couponStoreDO.setStoreId(store.getStoreId());
                                couponStoreDO.setStoreName(store.getStoreName());
                                couponStoreDO.setDeleted(Boolean.FALSE);
                                couponStoreDO.setTagId(k);
                                couponStores.add(couponStoreDO);
                            }
                        }
                    });
                }
            }
            couponStoreService.insertBatch(couponStores);
        }

        CouponCountUtil.setCouponDataDetail(redisTemplate, id, goodCouponDO);
        ShareSaveReqVO shareSaveReqVO = new ShareSaveReqVO();
        shareSaveReqVO.setCouponId(id);
        shareSaveReqVO.setShareTitle(goodCouponDO.getCouponName());
        couponShareService.insert(shareSaveReqVO);
        LogRecordContext.putVariable("createReqVO", createReqVO);
        return id;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = PROMOTION_GOOD_COUPON_TYPE, subType = PROMOTION_GOOD_COUPON_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}", success = PROMOTION_GOOD_COUPON_UPDATE_SUCCESS)
    public Integer updateCoupon(GoodCouponSaveReqVO updateReqVO) {
        Long couponId = updateReqVO.getId();
        GoodCouponDO one = goodCouponMapper.selectById(couponId);
        GoodCouponDO goodCouponDO = BeanUtils.toBean(updateReqVO, GoodCouponDO.class);

        Integer totalNum = updateReqVO.getTotalNum();
        Integer couponNum = one.getCouponNum();
        Integer receivedNum = one.getReceivedNum();
        Integer totalNum1 = one.getTotalNum();
        Integer userRestrictions = updateReqVO.getUserRestrictions();

//        if (!userRestrictions.equals(GoodCouponConstants.USER_RESTRICTIONS_3) && !userRestrictions.equals(GoodCouponConstants.USER_RESTRICTIONS_4)) {
//            if (!totalNum.equals(couponNum)) {
//                throw exception(COUPON_CAN_NOT_EDIT);
//            }
//        }

        if (totalNum < receivedNum) {
            throw exception(COUPON_NUM_ERROR);
        }
        log.info("totalNum:{},couponNum:{},totalNum1:{}", totalNum, couponNum, totalNum1);
        int i = couponNum - totalNum1 + totalNum;
        log.info("i:{}", i);
        goodCouponDO.setCouponNum(i);
        this.parseCouponUseTime(updateReqVO, goodCouponDO);
        couponCommodityService.deleteByCouponId(couponId);
        couponStoreService.deleteByCouponId(couponId);
        List<CouponCommodityDO> couponCommodities = updateReqVO.getCouponCommodities();
        //优惠券绑定商品
        Integer isCommonStore = updateReqVO.getIsCommonStore();
        if (isCommonStore != 1) {
            if (ObjectUtil.isNotEmpty(couponCommodities)) {
                //优惠券绑定商品 单一商品才绑定
                if (couponCommodities.size() == 1 && !isCommonStore.equals(GoodCouponConstants.IS_COMMON_STORE_3)) {
                    goodCouponDO.setSingleIds(couponCommodities.get(0).getCommodityId().toString());
                }
                if (couponCommodities.size() > 1 && !isCommonStore.equals(GoodCouponConstants.IS_COMMON_STORE_3)) {
                    goodCouponDO.setSingleIds(null);
                }
                couponCommodities.forEach(couponCommodity -> {
                    couponCommodity.setCouponId(goodCouponDO.getId());
                    couponCommodity.setType(isCommonStore);
                    couponCommodity.setDeleted(Boolean.FALSE);
                    couponCommodity.setId(null);
                });
                couponCommodityService.insertBatch(couponCommodities);
            }
        }else {
            goodCouponDO.setSingleIds(null);
        }
        //优惠券绑定门店
        Integer isCommon = updateReqVO.getIsCommon();
        Integer storeTagFlag = updateReqVO.getStoreTagFlag();
        if(ObjectUtil.isEmpty(storeTagFlag)){
            storeTagFlag = GoodCouponConstants.STORE_TAG_FLAG_1;
            goodCouponDO.setStoreTagFlag(storeTagFlag);
        }

        Map<Long, Integer> redisMap = new HashMap<>();
        if (isCommon.equals(GoodCouponConstants.IS_COMMON_2)) {
            List<CouponStoreDO> couponStores = updateReqVO.getCouponStores();
            if (ObjectUtil.isNotEmpty(couponStores)) {
                Integer storeLimitNum = goodCouponDO.getStoreLimitNum();
                if(ObjectUtil.isNotEmpty(storeLimitNum) && storeLimitNum > 0){
                    couponStores.forEach(item -> {
                        item.setCouponId(couponId);
                        item.setDeleted(Boolean.FALSE);
                        item.setStoreId(item.getStoreId());
                        item.setTotalNum(storeLimitNum);
                        item.setId(null);
                        redisMap.put(item.getStoreId(), item.getTotalNum());
                    });
                    CouponCountUtil.initCouponStoreQuantities(redisTemplate,couponId,redisMap);
                }else {
                    couponStores.forEach(item -> {
                        item.setCouponId(couponId);
                        item.setDeleted(Boolean.FALSE);
                        item.setStoreId(item.getStoreId());
                        item.setId(null);
                    });
                }
                couponStoreService.insertBatch(couponStores);
            }
        }

        if (storeTagFlag.equals(GoodCouponConstants.STORE_TAG_FLAG_0)) {
            List<CouponStoreDO> couponStores = new ArrayList<>();
            List<Long> storeTagIds = updateReqVO.getStoreTagIds();
            if(CollectionUtil.isNotEmpty(storeTagIds)){
                Map<Long, List<StoreInfoDTO>> storeIdsByTagIdsMap = storeApi.getStoreIdsByTagIds(storeTagIds);
                Integer storeLimitNum = goodCouponDO.getStoreLimitNum();
                if(ObjectUtil.isNotEmpty(storeLimitNum) && storeLimitNum > 0){
                    storeIdsByTagIdsMap.forEach((k,v)->{
                        if(CollectionUtil.isEmpty(v)){
                            CouponStoreDO couponStoreDO = new CouponStoreDO();
                            couponStoreDO.setCouponId(couponId);
                            couponStoreDO.setStoreId(0L);
                            couponStoreDO.setStoreName("");
                            couponStoreDO.setDeleted(Boolean.FALSE);
                            couponStoreDO.setTotalNum(storeLimitNum);
                            couponStoreDO.setTagId(k);
                            couponStores.add(couponStoreDO);
                            CouponCountUtil.initCouponStoreQuantities(redisTemplate,couponId,redisMap);
                        }else {
                            for (StoreInfoDTO store : v) {
                                CouponStoreDO couponStoreDO = new CouponStoreDO();
                                couponStoreDO.setCouponId(couponId);
                                couponStoreDO.setStoreId(store.getStoreId());
                                couponStoreDO.setStoreName(store.getStoreName());
                                couponStoreDO.setDeleted(Boolean.FALSE);
                                couponStoreDO.setTagId(k);
                                couponStores.add(couponStoreDO);
                                CouponCountUtil.initCouponStoreQuantities(redisTemplate,couponId,redisMap);
                            }
                        }
                    });
                }else {
                    storeIdsByTagIdsMap.forEach((k,v)->{
                        if(CollectionUtil.isEmpty(v)){
                            CouponStoreDO couponStoreDO = new CouponStoreDO();
                            couponStoreDO.setCouponId(couponId);
                            couponStoreDO.setStoreId(0L);
                            couponStoreDO.setStoreName("");
                            couponStoreDO.setDeleted(Boolean.FALSE);
                            couponStoreDO.setTotalNum(storeLimitNum);
                            couponStoreDO.setTagId(k);
                            couponStores.add(couponStoreDO);
                        }else {
                            for (StoreInfoDTO store : v) {
                                CouponStoreDO couponStoreDO = new CouponStoreDO();
                                couponStoreDO.setCouponId(couponId);
                                couponStoreDO.setStoreId(store.getStoreId());
                                couponStoreDO.setStoreName(store.getStoreName());
                                couponStoreDO.setDeleted(Boolean.FALSE);
                                couponStoreDO.setTagId(k);
                                couponStores.add(couponStoreDO);
                            }
                        }
                    });
                }
            }
            goodCouponDO.setIsCommon(GoodCouponConstants.IS_COMMON_2);
            couponStoreService.insertBatch(couponStores);
        }
        //goodCouponDO.setCouponNum(updateReqVO.getTotalNum());
        if (ObjectUtil.isEmpty(updateReqVO.getDayLimit())) {
            // 没有设置日限
            goodCouponDO.setDayLimit(0);
        }

        List<ExchangeCommodityReqVO> exchangeCommodityList = updateReqVO.getExchangeCommodityList();
        LambdaQueryWrapper<ExchangeCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExchangeCommodityDO::getCouponId, couponId);
        exchangeCommodityMapper.delete(queryWrapper);
        if (ObjectUtil.isNotEmpty(exchangeCommodityList) && (goodCouponDO.getCouponType().equals(2) || goodCouponDO.getCouponType().equals(5))) {
            List<ExchangeCommodityDO> list = exchangeCommodityList.stream().map(item -> {
                ExchangeCommodityDO exchangeCommodityDO = new ExchangeCommodityDO();
                exchangeCommodityDO.setCouponId(couponId);
                exchangeCommodityDO.setCommodityId(item.getCommodityId());
                exchangeCommodityDO.setCommodityName(item.getCommodityName());
                exchangeCommodityDO.setId(null);
                return exchangeCommodityDO;
            }).toList();
            exchangeCommodityMapper.insertBatch(list);
            goodCouponDO.setExchangeFlag(GoodCouponConstants.EXCHANGE_FLAG_1);
        } else {
            goodCouponDO.setExchangeFlag(GoodCouponConstants.EXCHANGE_FLAG_0);
        }

        redisTemplate.opsForValue().set(COUPON_DETAIL + couponId, goodCouponDO);
        LogRecordContext.putVariable("updateReqVO", updateReqVO);
        ShareSaveReqVO shareSaveReqVO = new ShareSaveReqVO();
        shareSaveReqVO.setCouponId(couponId);
        shareSaveReqVO.setShareTitle(updateReqVO.getCouponName());
        couponShareService.updateTitle(shareSaveReqVO);
        int i1 = goodCouponMapper.updateById(goodCouponDO);
        activitySeckillCouponService.reloadActivityCache(couponId);
        activityJDService.updateCoupon(couponId);
        return i1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = PROMOTION_GOOD_COUPON_TYPE, subType = PROMOTION_GOOD_COUPON_DELETE_SUB_TYPE, bizNo = "{{#id}}", success = PROMOTION_GOOD_COUPON_DELETE_SUCCESS)
    public void deleteCoupon(Long id) {
        LogRecordContext.putVariable("id", id);
//        GoodCouponDO goodCouponDO = goodCouponMapper.selectById(id);
//        if(!Objects.equals(goodCouponDO.getTotalNum(),goodCouponDO.getCouponNum())){
//            throw exception(CLAIMED_CAN_NOT_DELETE);
//        }
//        CommonResult<Long> countByCouponCode = pointsProductApi.getCountByCouponCode(goodCouponDO.getCouponCode());
//        Long count = countByCouponCode.getData();
//        if(count > 0){
//            throw exception(PRODUCT_IS_USED);
//        }
//        List<GoodCouponPackageDO> goodCouponPackageDOS = goodCouponPackageService.selectListByCouponId(id);
//        if(CollectionUtil.isNotEmpty(goodCouponPackageDOS)){
//            List<Long> packageIds = goodCouponPackageDOS.stream().map(item -> item.getPackageId()).toList();
//            long l = couponPackageService.selectCountByIds(packageIds);
//            if(l > 0){
//                throw exception(PACKAGE_IS_USED);
//            }
//        }


        activitySeckillCouponService.reloadActivityCache(id);
        activityJDService.updateCoupon(id);
        goodCouponMapper.deleteById(id);
        //todo 删除优惠券关联的优惠券商品和优惠券门店 积分商城 会员卡 小程序广告
    }

    @Override
    public GoodCouponRespVO getCouponById(Long id) {
        GoodCouponDO goodCouponDO = goodCouponMapper.selectById(id);
        if (ObjectUtil.isNotEmpty(goodCouponDO)) {
            GoodCouponRespVO goodCouponRespVO = BeanUtils.toBean(goodCouponDO, GoodCouponRespVO.class);
            if (!goodCouponDO.getIsCommonStore().equals(GoodCouponConstants.IS_COMMON_STORE_1)) {
                goodCouponRespVO.setCouponCommodities(couponCommodityService.selectByCouponId(id));
            }
            if (!goodCouponDO.getIsCommon().equals(GoodCouponConstants.IS_COMMON_1) || goodCouponDO.getStoreTagFlag().equals(GoodCouponConstants.STORE_TAG_FLAG_0)) {
                List<CouponStoreDO> couponStoreDOS = couponStoreService.selectByCouponId(id);
                if(goodCouponDO.getStoreTagFlag().equals(GoodCouponConstants.STORE_TAG_FLAG_0)){
                    List<Long> list = couponStoreDOS.stream().map(CouponStoreDO::getTagId).distinct().toList();
                    goodCouponRespVO.setStoreTagIds(list);
                }
                goodCouponRespVO.setCouponStores(couponStoreDOS);
            }
            if ((goodCouponDO.getCouponType().equals(2) || goodCouponDO.getCouponType().equals(5))
                    && goodCouponDO.getExchangeFlag().equals(GoodCouponConstants.EXCHANGE_FLAG_1)) {
                LambdaQueryWrapper<ExchangeCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ExchangeCommodityDO::getCouponId, id);
                List<ExchangeCommodityDO> exchangeCommodityDOS = exchangeCommodityMapper.selectList(queryWrapper);
                List<ExchangeCommodityRespVO> bean = BeanUtils.toBean(exchangeCommodityDOS, ExchangeCommodityRespVO.class);
                goodCouponRespVO.setExchangeCommodityList(bean);
            }
            return goodCouponRespVO;
        }
        return new GoodCouponRespVO();
    }

    @Override
    public List<String> getCommunityQrImage(Long couponId) {
        if(ObjectUtil.isEmpty(couponId)){
            return List.of();
        }
        GoodCouponDO goodCoupon = goodCouponMapper.selectById(couponId);
        if (ObjectUtil.isNotEmpty(goodCoupon)) {
            return ImageValueParseUtil.parseImageValues(goodCoupon.getCommunityQrImage());
        }
        return List.of();
    }

    @Override
    public GoodCouponRespVO getAppCouponById(Long id) {
       // GoodCouponDO goodCouponDO = goodCouponMapper.selectById(id);
        GoodCouponDO goodCouponDO = goodCouponMapper.selectOne(
                Wrappers.lambdaQuery(GoodCouponDO.class)
                        .eq(GoodCouponDO::getId, id)
                        .last("LIMIT 1")
        );
        if (ObjectUtil.isNotEmpty(goodCouponDO)) {
            GoodCouponRespVO goodCouponRespVO = BeanUtils.toBean(goodCouponDO, GoodCouponRespVO.class);
            CouponShareRespVO couponShareRespVO = couponShareService.getByCouponId(id);
            goodCouponRespVO.setCouponShareRespVO(couponShareRespVO);
            return goodCouponRespVO;
        } else {
            throw exception(COUPON_TIME_OUT);
        }
    }

    @Override
    @LogRecord(type = PROMOTION_GOOD_COUPON_TYPE, subType = PROMOTION_GOOD_COUPON_GROUND_SUB_TYPE, bizNo = "{{#id}}", success = PROMOTION_GOOD_COUPON_GROUND_SUCCESS)
    public Integer updateIsGround(Long id) {
        GoodCouponDO goodCoupon = goodCouponMapper.selectById(id);
        Integer isGround = goodCoupon.getIsGround();
        LambdaUpdateWrapper<GoodCouponDO> updateWrapper = Wrappers.lambdaUpdate(GoodCouponDO.class)
                .eq(GoodCouponDO::getId, id);

        if (isGround.equals(GoodCouponConstants.IS_GROUND_0)) {
            updateWrapper.set(GoodCouponDO::getIsGround, GoodCouponConstants.IS_GROUND_1);
            LogRecordContext.putVariable("ground", "上架了" + goodCoupon.getCouponName());
            // 如果门店限制了领取数量
            if (ObjectUtil.notEqual(GoodCouponConstants.STORE_LIMIT_0, goodCoupon.getStoreLimitNum())) {
                Boolean b = CouponCountUtil.couponHasKey(redisTemplate, id);
                if (!b) {
                    // 初始化门店领取数量
                    Map<Long, Integer> redisMap = new HashMap<>();
                    List<CouponStoreClaimNumDO> list = couponStoreClaimNumService.getByCouponId(id);
                    if (ObjectUtil.isNotEmpty(list)) {
                        for (CouponStoreClaimNumDO couponStoreClaimNumDO : list) {
                            Long storeId = couponStoreClaimNumDO.getStoreId();
                            Integer claimedNum = couponStoreClaimNumDO.getClaimNum();
                            redisMap.put(storeId, claimedNum);
                        }
                        CouponCountUtil.initCouponClaimNum(redisTemplate, id, redisMap);
                    }
                }
            }
        } else {
            LogRecordContext.putVariable("ground", "下架了" + goodCoupon.getCouponName());
            updateWrapper.set(GoodCouponDO::getIsGround, GoodCouponConstants.IS_GROUND_0);
            // 删除门店领取数量
            if (ObjectUtil.notEqual(GoodCouponConstants.STORE_LIMIT_0, goodCoupon.getStoreLimitNum())) {
                CouponCountUtil.expireThreeDays(redisTemplate, id);
            }
        }
        int update = goodCouponMapper.update(updateWrapper);
        activitySeckillCouponService.reloadActivityCache(id);
        activityJDService.updateCoupon(id);
        return update;
    }

    @Override
    public Integer editCouponNum(CouponNumUpdateReqVO couponNumUpdateReqVO) {
        Long couponId = couponNumUpdateReqVO.getCouponId();
        Integer num = couponNumUpdateReqVO.getNum();

        GoodCouponDO goodCoupon = goodCouponMapper.selectById(couponId);
        Integer totalNum = goodCoupon.getTotalNum();
        Integer couponNum = goodCoupon.getCouponNum();
        Integer receivedNum = goodCoupon.getReceivedNum();
        if (num < receivedNum) {
            throw exception(COUPON_NUM_ERROR);
        }
        LambdaUpdateWrapper<GoodCouponDO> updateWrapper = Wrappers.lambdaUpdate(GoodCouponDO.class)
                .eq(GoodCouponDO::getId, couponId);
        updateWrapper.set(GoodCouponDO::getTotalNum, num);
        updateWrapper.set(GoodCouponDO::getCouponNum, couponNum - totalNum + num);
        return goodCouponMapper.update(updateWrapper);
    }

    @Override
    @LogRecord(type = PROMOTION_GOOD_COUPON_TYPE, subType = PROMOTION_GOOD_COUPON_COPY_SUB_TYPE, bizNo = "{{#goodCoupon.id}}", success = PROMOTION_GOOD_COUPON_COPY_SUCCESS)
    public Long copy(Long id) {
        GoodCouponRespVO goodCoupon = getCouponById(id);
        GoodCouponSaveReqVO createReqVO = BeanUtils.toBean(goodCoupon, GoodCouponSaveReqVO.class);
        createReqVO.setId(null);
        createReqVO.setCouponCode(null);
        createReqVO.setIsGround(GoodCouponConstants.IS_GROUND_0);
        createReqVO.setCouponName(goodCoupon.getCouponName());
        createReqVO.setCouponStores(couponStoreService.selectByCouponId(id));
        createReqVO.setCouponCommodities(couponCommodityService.selectByCouponId(id));
        createReqVO.setCouponNameColor(goodCoupon.getCouponNameColor());
        // 名称拼接 默认开
        createReqVO.setNameConcatenation(0);
        LogRecordContext.putVariable("goodCoupon", goodCoupon);
        return createCoupon(createReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer rebindCommodity(RebindCommodityReqVO rebindCommodity) {
        List<CouponCommodityDO> couponCommodities = rebindCommodity.getCouponCommodities();

        Long id = rebindCommodity.getId();
        couponCommodityService.deleteByCouponId(id);
        Integer isCommonStore = rebindCommodity.getIsCommonStore();

        LambdaUpdateWrapper<GoodCouponDO> updateWrapper = Wrappers.lambdaUpdate(GoodCouponDO.class)
                .eq(GoodCouponDO::getId, id);
        int result = 0;
        if (isCommonStore != 1) {
            //优惠券绑定商品 单一商品才绑定
            if (couponCommodities.size() == 1 && !isCommonStore.equals(GoodCouponConstants.IS_COMMON_STORE_3)) {
                updateWrapper.set(GoodCouponDO::getSingleIds, couponCommodities.get(0).getCommodityId().toString());
                result = goodCouponMapper.update(updateWrapper);
            }
        }
        couponCommodities.forEach(couponCommodity -> {
            couponCommodity.setCouponId(id);
            couponCommodity.setType(isCommonStore);
            couponCommodity.setId(null);
            couponCommodity.setDeleted(Boolean.FALSE);
        });

        couponCommodityService.insertBatch(couponCommodities);
        return result;
    }

    @Override
    public GoodCouponDateRespVO selectDataById(Long id) {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        Object cacheObject = hashOperations.get(COUPON_DATA, id.toString());
        GoodCouponDateRespVO goodCouponDateVO = new GoodCouponDateRespVO();
        if (ObjectUtil.isNotEmpty(cacheObject)) {
            goodCouponDateVO = new ObjectMapper().convertValue(cacheObject, GoodCouponDateRespVO.class);
            return goodCouponDateVO;
        } else {

            LocalDateTime yesterdayLastSecond = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.DAYS)
                    .plusDays(1).minusSeconds(1);
            List<UserCouponRecordDO> userCouponRecords = userCouponRecordService.getYesterDayDataList(yesterdayLastSecond, id);
            if (CollectionUtil.isEmpty(userCouponRecords)) {
                return new GoodCouponDateRespVO();
            }


            // 支付金额
            BigDecimal totalAmount = userCouponRecords.stream().map(UserCouponRecordDO::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            goodCouponDateVO.setTurnover(totalAmount);
            // 优惠金额
            BigDecimal couponAmount = userCouponRecords.stream().map(UserCouponRecordDO::getCouponAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            goodCouponDateVO.setOfferTotal(couponAmount);
            BigDecimal multiplied = couponAmount.multiply(new BigDecimal("100"));
            // 费效比
            if (totalAmount.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal cost = multiplied.divide(totalAmount, 2, RoundingMode.HALF_UP);
                goodCouponDateVO.setCost(cost);
            } else {
                goodCouponDateVO.setCost(BigDecimal.ZERO);
            }
            // 订单数
            int orderNum = userCouponRecords.size();
            goodCouponDateVO.setOrderNum(orderNum);

            //使用率
            GoodCouponDO goodCoupon = goodCouponMapper.selectById(id);
            Integer receivedNum = goodCoupon.getReceivedNum();
            if (ObjectUtil.isNotEmpty(receivedNum)) {
                if (receivedNum != 0) {
                    BigDecimal usedRate = new BigDecimal(orderNum).multiply(new BigDecimal("100"))
                            .divide(new BigDecimal(receivedNum), 2, RoundingMode.HALF_UP);
                    goodCouponDateVO.setUsedRate(usedRate);
                } else {
                    goodCouponDateVO.setUsedRate(BigDecimal.ZERO);
                }
            }

            //单价
            BigDecimal singlePrice = totalAmount.divide(new BigDecimal(orderNum), 2, RoundingMode.HALF_UP);
            goodCouponDateVO.setSinglePrice(singlePrice);
            // 购买数量
            int commodityNum = userCouponRecords.stream().mapToInt(UserCouponRecordDO::getItemNum).sum();
            goodCouponDateVO.setItemNum(commodityNum);
            hashOperations.put(COUPON_DATA, id.toString(), goodCouponDateVO);
        }
        return goodCouponDateVO;
    }

    @Override
    public GoodCouponDateRespV2VO selectDataByIdV2(GoodCouponDateReqVO goodCouponDateReqVO) {
        LambdaQueryWrapper<UserCouponRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        List<StoreInfoDTO> stores;
        Map<Long, String> map = new HashMap<>(8);
        String storeName = goodCouponDateReqVO.getStoreName();
        if (ObjectUtil.isNotEmpty(storeName)) {
            CommonResult<List<StoreInfoDTO>> storeListByName = storeApi.getStoreListByName(storeName);
            stores = storeListByName.getData();
            if (CollectionUtil.isEmpty(stores)) {
                throw exception(NO_THIS_STORE);
            }
            List<Long> storeIds = stores.stream().map(StoreInfoDTO::getStoreId).toList();
            queryWrapper.in(UserCouponRecordDO::getStoreId, storeIds);
            goodCouponDateReqVO.setStoreIds(storeIds);
            map = stores.stream().collect(Collectors.toMap(StoreInfoDTO::getStoreId, StoreInfoDTO::getStoreName));
        }

        Long couponId = goodCouponDateReqVO.getCouponId();
        GoodCouponDO goodCoupon = goodCouponMapper.selectById(couponId);
        Integer isCommon = goodCoupon.getIsCommon();
        GoodCouponDateRespV2VO goodCouponDateV2VO = new GoodCouponDateRespV2VO();
        if (isCommon != 1) {
            long l = couponStoreService.selectCountByCouponId(couponId);
            goodCouponDateV2VO.setStoreNum(l);
        }
        Page<GoodCouponDateRespVO> page = new Page<>(goodCouponDateReqVO.getPageNo(), goodCouponDateReqVO.getPageSize());
        Page<GoodCouponDateRespVO> result = userCouponRecordService.listPageByCouponId(page, goodCouponDateReqVO);
        List<GoodCouponDateRespVO> list = result.getRecords();
        if (CollectionUtil.isNotEmpty(list)) {
            List<Long> storeIds = list.stream().map(GoodCouponDateRespVO::getStoreId).toList();
            stores = JSON.parseArray(JSON.toJSONString(storeApi.getStoresByStoreIds(storeIds)), StoreInfoDTO.class);
            if (ObjectUtil.isNotEmpty(storeName)) {
                map = stores.stream().collect(Collectors.toMap(StoreInfoDTO::getStoreId, StoreInfoDTO::getStoreName));
            }
            for (GoodCouponDateRespVO record : list) {
                record.setStoreName(map.get(record.getStoreId()));
                //单价
                BigDecimal totalAmount = record.getTurnover();
                Integer orderNum = record.getOrderNum();
                BigDecimal singlePrice = totalAmount.divide(new BigDecimal(orderNum), 2, RoundingMode.HALF_UP);
                record.setSinglePrice(singlePrice);
                // 费效比
                BigDecimal couponAmount = record.getOfferTotal();
                BigDecimal multiplied = couponAmount.multiply(new BigDecimal("100"));
                // 费效比
                if (totalAmount.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal cost = multiplied.divide(totalAmount, 2, RoundingMode.HALF_UP);
                    record.setCost(cost);
                } else {
                    record.setCost(BigDecimal.ZERO);
                }
            }
        }
        PageResult<GoodCouponDateRespVO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setTotal(result.getTotal());
        goodCouponDateV2VO.setPageResult(pageResult);
        return goodCouponDateV2VO;
    }

    @Override
    public Boolean addShare(ShareSaveReqVO shareSaveReqVO) {
        return couponShareService.saveOrUpdate(shareSaveReqVO);
    }

    @Override
    public CouponShareRespVO getShareDetail(ShareDetailReqVO shareDetailReqVO) {
        ObjectMapper objectMapper = new ObjectMapper();
        String pageUrl = shareDetailReqVO.getPageUrl();
        Long id = shareDetailReqVO.getId();
        CouponShareRespVO couponShareRespVO = couponShareService.getByCouponId(id);
        //CouponShareRespVO couponShareRespVO = BeanUtils.toBean(couponShare, CouponShareRespVO.class);
        Long businessId = BusinessContextHolder.getRequiredBusinessId();
        //String accessToken = weChatService.getWechatToken(true, String.valueOf(businessId));
        String wechatToken = wxActionApi.getWechatToken(businessId);
        String url = URL + wechatToken;
        Map<String, Object> params = new HashMap<>(8);
        params.put("page_url", pageUrl + "?id=" + id);
        params.put("page_title", "0090");
        String json = null;
        try {
            json = objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            log.error("wechat解析json异常{}", e.getMessage());
        }
        String post = HttpUtil.post(url, json);
        try {
            Map<String, Object> map = objectMapper.readValue(post, new TypeReference<>() {
            });
            String link = (String) map.get("link");
            if (ObjectUtil.isEmpty(couponShareRespVO)) {
                couponShareRespVO = new CouponShareRespVO();
                couponShareRespVO.setWxShareUrl(link);
            } else {
                couponShareRespVO.setWxShareUrl(link);
            }
        } catch (JsonProcessingException e) {
            log.error("wechat解析json异常{}", e.getMessage());
        }
        return couponShareRespVO;
    }

    @Override
    public List<GoodCouponRespVO> getCouponListByIntegral() {
        QueryWrapper<GoodCouponDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(GoodCouponDO::getIsCommon, 1).eq(GoodCouponDO::getIsGround, 1)
                .eq(GoodCouponDO::getIsCommonStore, 1).ne(GoodCouponDO::getUseType, 0);
        queryWrapper.orderByDesc("create_time");
        List<GoodCouponDO> list = goodCouponMapper.selectList(queryWrapper);
        return BeanUtils.toBean(list, GoodCouponRespVO.class);
    }

    @Override
    public boolean updateByWrapper(UpdateWrapper<GoodCouponDO> goodCouponUpdateWrapper) {
        return goodCouponMapper.update(goodCouponUpdateWrapper) > 0;
    }

    @Override
    public GoodCouponRespVO getCouponByCode(String couponCode) {
        LambdaQueryWrapper<GoodCouponDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(GoodCouponDO::getCouponCode, couponCode);
        GoodCouponDO goodCouponDO = goodCouponMapper.selectOne(queryWrapper);
        return BeanUtil.toBean(goodCouponDO, GoodCouponRespVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = PROMOTION_GOOD_COUPON_TYPE, subType = PROMOTION_GOOD_COUPON_ISSUE_SUB_TYPE, bizNo = "{{#goodCoupon.id}}", success = PROMOTION_GOOD_COUPON_ISSUE_SUCCESS)
    public Boolean issueCoupon(IssueCouponReqVO issueCouponReqVO) {
        //优惠券
        Long couponId = issueCouponReqVO.getCouponId();
        GoodCouponDO goodCouponDO = goodCouponMapper.selectById(couponId);
        LogRecordContext.putVariable("goodCoupon", goodCouponDO);
        String transLock = "LOCK_COUPON#" + couponId;
        RLock fairMethodLock = redissonClient.getLock(transLock);

        GoodCouponRespVO goodCoupon = BeanUtil.toBean(goodCouponDO, GoodCouponRespVO.class);
//        String transLock = "LOCK_COUPON#" + couponId;
//        RLock fairMethodLock = redissonClient.getLock(transLock);
        if (ObjectUtil.isEmpty(goodCoupon)) {
            throw exception(COUPON_NOT_EXISTS);
        }
        String commodityCanUse = "全部可用";
        if (goodCoupon.getIsCommon() == 1) {
            switch (goodCoupon.getIsCommonStore()) {
                case 1:
                    break;
                case 2:
                    List<CouponCommodityDO> listCan = couponCommodityService.selectByCouponId(goodCoupon.getId());
                    commodityCanUse = "可用商品:" + listCan.stream().
                            map(CouponCommodityDO::getCommodityName)
                            .collect(Collectors.joining(","));
                    break;
                case 3:
                    QueryWrapper<CouponCommodityDO> queryWrapper = new QueryWrapper<>();
                    queryWrapper.lambda().eq(CouponCommodityDO::getId, goodCoupon.getId());
                    List<CouponCommodityDO> listNotCan = couponCommodityMapper.selectList(queryWrapper);
                    commodityCanUse = "不可用商品:" + listNotCan.stream().
                            map(CouponCommodityDO::getCommodityName)
                            .collect(Collectors.joining(","));
                    break;
                default:
                    break;
            }
        }

        List<UserCouponDO> userCoupons = new ArrayList<>();
        Integer num = issueCouponReqVO.getCouponNum();

        if (goodCoupon.getIsGround() != 1) {
            throw exception(COUPON_NOT_ON_SHELF);
        }
        Date couponEndTime = goodCoupon.getCouponEndTime();
        if (goodCoupon.getUseType() == 0 && DateUtil.compare(new Date(), couponEndTime) > 0) {
            throw exception(COUPON_EXPIRED);
        }
        Integer couponNum = goodCoupon.getCouponNum();
        Integer limitNum = goodCoupon.getLimitNum();
        if (limitNum < issueCouponReqVO.getCouponNum()) {
            throw exception(COUPON_OVER_LIMIT_0, "优惠券限量每人发" + limitNum + "张");
        }
        if (couponNum < issueCouponReqVO.getCouponNum()) {
            throw exception(COUPON_NO_REST,goodCoupon.getCouponName());
        }
        //会员
        String memberMobile = issueCouponReqVO.getMemberMobile();
        WxMemberDTO wxMember = wxMemberApi.getMemberByMobile(memberMobile);
        if (ObjectUtil.isEmpty(wxMember.getMemberId())) {
            throw exception(COUPON_NO_USER);
        }
//        if (ObjectUtil.isEmpty(wxMember)) {
//            throw exception(COUPON_NO_USER);
//        }

        //用户优惠券数量
        userCouponShardService.userCouponCount(goodCoupon, couponId, wxMember, num);
        try {
            fairMethodLock.lock();
            UserCouponDO userCoupon = new UserCouponDO();

            for (int i = 0; i < num; i++) {
                userCoupon = new UserCouponDO();
                BeanUtil.copyProperties(goodCoupon, userCoupon);
                userCoupon.setUserId(wxMember.getMemberId());
                userCoupon.setCouponId(couponId);
                userCoupon.setCouponUseTime(goodCoupon.getUseTime());
                userCoupon.setCouponCreateTime(LocalDateTime.now());
                userCoupon.setUseTime(null);
                userCoupon.setId(null);
                CouponTimeUtil.parseCouponTime(goodCoupon, userCoupon);
                userCoupon.setIsUsed(0);
                userCoupon.setDistributionMethod(0L);
                userCoupon.setMemberMobile(memberMobile);
                userCoupon.setMemberName(wxMember.getMemberNickName());
                userCoupon.setCouponSource(UserCouponConstants.COUPON_SOURCE_6);
                userCoupon.setDeleted(Boolean.FALSE);
                userCoupons.add(userCoupon);
                extracted(goodCoupon, userCoupon, commodityCanUse, wxMember);
            }
            UpdateWrapper<GoodCouponDO> goodCouponUpdateWrapper = new UpdateWrapper<>();
            goodCouponUpdateWrapper.setSql("received_num = received_num + " + num);
            goodCouponUpdateWrapper.setSql("coupon_num = coupon_num - " + num);
            goodCouponUpdateWrapper.eq("id", couponId);
            int i = goodCouponMapper.update(goodCouponUpdateWrapper);

            threadPoolTaskExecutor.submit(() -> {
                Boolean b = userCouponShardService.insertBatch(userCoupons);
            });
            threadPoolTaskExecutor.submit(() -> {
                for (UserCouponDO userCouponDO : userCoupons) {
                    AppletNoticePushVO appletNoticePush = new AppletNoticePushVO();

                    //取餐消息通知
                    //appletNoticePush.setBusinessId(String.valueOf(userCoupon.getBusinessId()));
                    appletNoticePush.setTemplateType(AppletPushTemplateTypeEnum.PLACE_COUPON_RECEIVE.getCode());
                    appletNoticePush.setOpenId(userCouponDO.getOpenId());
                    List<String> valueList = new ArrayList<>();
                    valueList.add(userCouponDO.getCouponName());
                    valueList.add(userCouponDO.getCouponTypeName());
                    valueList.add(DateUtils.dateToString(userCouponDO.getExpirationTime(), "yyyy年MM月dd日 HH:mm"));
                    valueList.add(userCouponDO.getCommodityNameStr());
                    commonService.sendAppletNotice(valueList, appletNoticePush);
                }
            });
//            if (!b) {
//                log.warn("优惠券单笔发放： {}优惠券数量已被他人修改，请重试！", couponId);
//                throw new RuntimeException("优惠券： " + goodCoupon.getCouponName() + "当前领取人数过多，请稍后重试！");
//            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (ObjectUtil.isNotNull(fairMethodLock) && fairMethodLock.isLocked() && fairMethodLock.isHeldByCurrentThread()) {
                fairMethodLock.unlock();
            }
        }
    }

//    @DS(DsNameConstants.SHARDING)
//    private void extracted(GoodCouponRespVO goodCoupon, Long couponId, WxMemberDTO wxMember, Integer num) {
//        if (goodCoupon.getLimitNum() != 100) {
//            QueryWrapper<UserCouponDO> qw = new QueryWrapper<>();
//            qw.eq("coupon_id", couponId);
//            qw.eq("user_id", wxMember.getMemberId());
//            long l = userCouponMapper.selectCount(qw);
//            if (l >= goodCoupon.getLimitNum() + num) {
//                throw exception(COUPON_OVER_LIMIT_0);
//            }
//        }
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    //@LogRecord(type = PROMOTION_GOOD_COUPON_TYPE, subType = PROMOTION_GOOD_COUPON_ISSUE_SUB_TYPE, bizNo = "{{#goodCoupon.id}}", success = PROMOTION_GOOD_COUPON_ISSUE_SUCCESS)
    public Boolean importCouponExl(MultipartFile file, int num, Long couponId) {
        UserCouponDO userCoupon = new UserCouponDO();
        List<UserCouponDO> userCoupons = new ArrayList<>();
        String transLock = "LOCK_COUPON#" + couponId;
        RLock fairMethodLock = redissonClient.getLock(transLock);
        //优惠券信息
        GoodCouponDO goodCouponDO = goodCouponMapper.selectById(couponId);
        LogRecordContext.putVariable("goodCoupon", goodCouponDO);

        GoodCouponRespVO goodCoupon = BeanUtil.toBean(goodCouponDO, GoodCouponRespVO.class);


        if (ObjectUtil.isEmpty(goodCoupon)) {
            throw exception(COUPON_NOT_EXISTS);
        }
        //判断优惠券是否上架
        if (goodCoupon.getIsGround() != 1) {
            throw exception(COUPON_NOT_ON_SHELF);
        }
        //使用时间是否过期
        Date couponEndTime = goodCoupon.getCouponEndTime();
        if (goodCoupon.getUseType() == 0 && DateUtil.compare(new Date(), couponEndTime) > 0) {
            throw exception(COUPON_EXPIRED);
        }
        //领取数量限制
        Integer limitNum = goodCoupon.getLimitNum();
        if (num > limitNum) {
            throw exception(COUPON_OVER_LIMIT_0, "发放数量不能超过" + limitNum);
        }


        try {
            //上锁
            fairMethodLock.lock();
            QueryWrapper<CouponCommodityDO> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(CouponCommodityDO::getCouponId, couponId);
            List<CouponCommodityDO> couponCommoditieList = couponCommodityMapper.selectList(wrapper);
            goodCoupon.setCouponCommodities(couponCommoditieList);

            //读取excel
            List<MemberExlVo> memberExlVos = readExcelNew(file, MemberExlVo.class);
            if (CollectionUtil.isEmpty(memberExlVos)) {
                throw exception(COUPON_NO_FILE_DATA);
            }
            List<String> memberMobiles = memberExlVos.stream().map(MemberExlVo::getMemberMobile).collect(Collectors.toList());

            //过滤空数据
            Filter<String> predicate = ObjectUtil::isNotEmpty;
            List<String> filter = CollectionUtil.filter(memberMobiles, predicate);
            if (CollectionUtil.isEmpty(filter)) {
                throw exception(COUPON_NO_FILE_DATA);
            }
            //去重
            CollectionUtils.select(memberMobiles, item -> {
                boolean isDuplicate = CollectionUtils.cardinality(item, memberMobiles) > 1;
                if (isDuplicate && memberMobiles.contains(item)) {
                    throw exception(COUPON_FILE_DATA_SAME, item);
                }
                return false;
            });

            //查询用户
            List<WxMemberDTO> memberList = wxMemberApi.getMemberByMobiles(memberMobiles);
            Map<String, WxMemberDTO> memberMap = memberList.stream().collect(Collectors.toMap(WxMemberDTO::getMemberMobile, p -> p));
            Integer couponNum = goodCoupon.getCouponNum();
            int changedNum = num * memberList.size();
            if (couponNum < changedNum) {
                throw exception(COUPON_NO_REST,goodCouponDO.getCouponName());
            }

            //验证用户是否注册
            if (CollectionUtil.isNotEmpty(memberMap) && memberMobiles.size() != memberMap.size()) {
                List<String> noRegister = memberMobiles.stream().filter(item -> !memberMap.containsKey(item)).collect(Collectors.toList());
                String result = String.join(",", noRegister);
                throw exception(MEMBER_NO_REGISTER, result);
            } else {
                for (String item : memberMobiles) {
                    WxMemberDTO wxMember = memberMap.get(item);
                    for (int i = 0; i < num; i++) {
                        userCoupon = new UserCouponDO();
                        BeanUtil.copyProperties(goodCoupon, userCoupon);
                        userCoupon.setUserId(wxMember.getMemberId());
                        userCoupon.setCouponId(couponId);
                        userCoupon.setCouponUseTime(goodCoupon.getUseTime());
                        userCoupon.setUseTime(null);
                        userCoupon.setCouponCreateTime(LocalDateTime.now());
                        userCoupon.setId(null);
                        userCoupon.setIsUsed(0);
                        userCoupon.setMemberName(wxMember.getMemberNickName());
                        userCoupon.setCommodityNameStr(goodCoupon.getCouponCommodities().stream().map(CouponCommodityDO::getCommodityName).collect(Collectors.joining(" ")));
                        CouponTimeUtil.parseCouponTime(goodCoupon, userCoupon);
                        userCoupon.setDistributionMethod(0L);
                        userCoupon.setMemberMobile(item);
                        userCoupon.setCouponSource(UserCouponConstants.COUPON_SOURCE_6);
                        userCoupon.setDeleted(Boolean.FALSE);
                        userCoupons.add(userCoupon);
                    }
                }
            }
            //更新库存
            UpdateWrapper<GoodCouponDO> goodCouponUpdateWrapper = new UpdateWrapper<>();
            goodCouponUpdateWrapper.setSql("received_num = received_num + " + changedNum);
            goodCouponUpdateWrapper.setSql("coupon_num = coupon_num - " + changedNum);
            goodCouponUpdateWrapper.eq("id", couponId);
            int i = goodCouponMapper.update(goodCouponUpdateWrapper);
            if (i == 0) {
                log.warn("优惠券： {}库存数量已被他人修改，请重试！", couponId);
                throw new RuntimeException("优惠券： " + userCoupon.getCouponName() + "当前领取人数过多，请稍后重试！");
            }
            Map<Long, List<UserCouponDO>> ageGroupMap = userCoupons.stream()
                    .collect(Collectors.groupingBy(UserCouponDO::getUserId));
            this.insertBatchByThread(ageGroupMap);
            return Boolean.TRUE;

        } finally {
            if (ObjectUtil.isNotNull(fairMethodLock) && fairMethodLock.isLocked() && fairMethodLock.isHeldByCurrentThread()) {
                fairMethodLock.unlock();
            }
        }
    }

    @Override
    public List<GoodCouponDO> getByIds(List<Long> couponIds) {
        QueryWrapper<GoodCouponDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", couponIds);
        return goodCouponMapper.selectList(queryWrapper);
    }

    @Override
    public Map<Long, Long> getCouponReceiveCountByStore(List<Long> couponIds, List<Long> storeIds,
                                                        LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return Map.of();
        }
        QueryWrapperX<UserCouponDO> wrapper = new QueryWrapperX<>();
        wrapper.select("store_id as storeId", "count(1) as receivedNum");
        wrapper.eq("business_id", BusinessContextHolder.getRequiredBusinessId());
        if (CollectionUtil.isNotEmpty(couponIds)) {
            wrapper.in("coupon_id", couponIds);
        }
        wrapper.between("coupon_create_time", startTime, endTime);
        if (CollectionUtil.isNotEmpty(storeIds)) {
            wrapper.in("store_id", storeIds);
        }
        wrapper.groupBy("store_id");

        List<Map<String, Object>> rows = userCouponMapper.selectMaps(wrapper);
        if (CollectionUtil.isEmpty(rows)) {
            return Map.of();
        }

        Map<Long, Long> result = new HashMap<>(rows.size());
        for (Map<String, Object> row : rows) {
            Long storeId = toLongValue(row.getOrDefault("storeId", row.get("store_id")));
            Long receivedNum = toLongValue(row.getOrDefault("receivedNum", row.get("received_num")));
            if (storeId != null) {
                result.put(storeId, receivedNum == null ? 0L : receivedNum);
            }
        }
        return result;
    }

    private Long toLongValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && StringUtils.isNotBlank(text)) {
            return Long.valueOf(text);
        }
        return null;
    }

    /**
     * 读取excel 下面的不太好使
     *
     * @param file
     * @param clazz
     * @return
     */
    private List<MemberExlVo> readExcelNew(MultipartFile file, Class<MemberExlVo> clazz) {
        GenericExcelListener<MemberExlVo> listener = new GenericExcelListener<>(validator);
        try {
            EasyExcel.read(file.getInputStream(), clazz, listener).sheet().doRead();
        } catch (Exception e) {
            throw new RuntimeException("Excel解析失败", e);
        }

        //强校验 必填项是否填写
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(listener.getErrors())) {
            String errorMsg = Joiner.on("\n\r").join(listener.getErrors());
            throw new ServiceException(EXCEL_IMPORT_FILE_FAILED.getCode(), errorMsg);
        }
        //导入内容
        return listener.getSuccessList();
    }

    /**
     * 读取excel 下面的不太好使
     *
     * @param file
     * @param
     * @return
     */
    public List<String> readExcel(MultipartFile file) {
        List<String> memberMobiles = new ArrayList<>();
        try {
            // 读取Excel文件
            EasyExcel.read(file.getInputStream(), MemberExlVo.class, new AnalysisEventListener<MemberExlVo>() {
                @Override
                public void invoke(MemberExlVo data, AnalysisContext context) {
                    memberMobiles.add(data.getMemberMobile());
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {

                }

                @Override
                public void onException(Exception exception, AnalysisContext context) {
                    log.error("有异常");
                    // 如果是某一个单元格的转换异常 能获取到具体行号
                    // 如果要获取头的信息 配合invokeHeadMap使用
                    if (exception instanceof ExcelDataConvertException excelDataConvertException) {
                        log.warn("第{}行，第{}列解析异常，数据为:{}");
                        Integer columnIndex = excelDataConvertException.getColumnIndex();
                        ++columnIndex;
                        Integer rowIndex = excelDataConvertException.getRowIndex();
                        throw new RuntimeException("第" + rowIndex + "行" +
                                "，第" + columnIndex + "列读取错误");
                    }
                }
            }).excelType(ExcelTypeEnum.XLSX).sheet().doRead();
            return memberMobiles;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void insertBatchByThread(Map<Long, List<UserCouponDO>> map) {
        log.info("开始批量插入优惠券insertBatchByThread");
        map.forEach((k, v) -> {
            threadPoolTaskExecutor.submit(() -> {
                userCouponShardService.insertBatch(v);
            });
            threadPoolTaskExecutor.submit(() -> {
                for (UserCouponDO userCoupon : v) {
                    AppletNoticePushVO appletNoticePush = new AppletNoticePushVO();

                    //取餐消息通知
                    //appletNoticePush.setBusinessId(String.valueOf(userCoupon.getBusinessId()));
                    appletNoticePush.setTemplateType(AppletPushTemplateTypeEnum.PLACE_COUPON_RECEIVE.getCode());
                    appletNoticePush.setOpenId(userCoupon.getOpenId());
                    List<String> valueList = new ArrayList<>();
                    valueList.add(userCoupon.getCouponName());
                    valueList.add(userCoupon.getCouponTypeName());
                    valueList.add(DateUtils.dateToString(userCoupon.getExpirationTime(), "yyyy年MM月dd日 HH:mm"));
                    valueList.add(userCoupon.getCommodityNameStr());
                    commonService.sendAppletNotice(valueList, appletNoticePush);
                }
            });
        });
    }

    @Override
    public Long countByCrowdId(Long crowdId) {
        return goodCouponMapper.selectCount(new QueryWrapper<GoodCouponDO>().eq("version", crowdId).eq("user_restrictions", 4));
    }

    private void extracted(GoodCouponRespVO goodCoupon, UserCouponDO userCoupon, String commodityCanUse, WxMemberDTO wxMember) {
        if (goodCoupon.getIsCommon() == 1) {
            final UserCouponDO finalUserCoupon = userCoupon;
            final String finalCommodityCanUse = commodityCanUse;
            threadPoolTaskExecutor.submit(() -> {
                AppletNoticePushVO appletNoticePush = new AppletNoticePushVO();
                //取餐消息通知
                //appletNoticePush.setBusinessId(String.valueOf(BusinessContextHolder.getRequiredBusinessId()));
                appletNoticePush.setTemplateType(AppletPushTemplateTypeEnum.PLACE_COUPON_RECEIVE.getCode());
                appletNoticePush.setOpenId(wxMember.getOpenid());
                List<String> valueList = new ArrayList<>();
                valueList.add(finalUserCoupon.getCouponName());
                valueList.add(CouponTypeEnum.getMessageByCode(finalUserCoupon.getCouponType()));
                valueList.add(DateUtils.dateToString(finalUserCoupon.getExpirationTime(), "yyyy年MM月dd日 HH:mm"));
                valueList.add(finalCommodityCanUse);
                commonService.sendAppletNotice(valueList, appletNoticePush);
            });
        }
    }

    @Override
    public List<GoodCouponRespVO> getCouponListByActivity() {
        LambdaQueryWrapper<GoodCouponDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(GoodCouponDO::getIsCommon, 1)
                .eq(GoodCouponDO::getUseType, 1)
                .eq(GoodCouponDO::getIsGround, 1);
        List<GoodCouponDO> goodCouponDOS = goodCouponMapper.selectList(lambdaQueryWrapper);

        return goodCouponDOS.stream()
                .map(ele -> {
                    GoodCouponRespVO goodCouponRespVO = BeanUtils.toBean(ele, GoodCouponRespVO.class);
                    return goodCouponRespVO;
                })
                .toList();
    }

    @Override
    public String getLittlePic() {
        return littlePicUrl;
    }

    @Override
    public String getLargePic() {
        return largePicUrl;
    }

    /**
     * 根据时效判断开始和过期时间
     *
     * @param goodCoupon   goodCoupon
     * @param goodCouponDO goodCouponDO
     */
    private void parseCouponUseTime(GoodCouponSaveReqVO goodCoupon, GoodCouponDO goodCouponDO) {
        //解析优惠券的开始结束时间
        if (goodCoupon.getUseType().equals(GoodCouponConstants.USE_TYPE_0)) {
            String[] split = goodCoupon.getUseTime().split("#");
            goodCouponDO.setCouponStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[0] + DateUtils.T_00_00_00));
            goodCouponDO.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[1] + DateUtils.T_23_59_59));
        }
        //立即生效
        if (goodCoupon.getUseType().equals(GoodCouponConstants.USE_TYPE_1)) {
            goodCouponDO.setCouponStartTime(new Date());
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.parseInt(goodCoupon.getUseTime()) - 1), DateUtils.YYYY_MM_DD);
            goodCouponDO.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
        //领券后N天生效
        if (goodCoupon.getUseType().equals(GoodCouponConstants.USE_TYPE_2)) {
            String[] split = goodCoupon.getUseTime().split("#");
            String startTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.parseInt(split[0])), DateUtils.YYYY_MM_DD);
            goodCouponDO.setCouponStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + DateUtils.T_00_00_00));
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.parseInt(split[0])).plusDays(Integer.parseInt(split[1]) - 1), DateUtils.YYYY_MM_DD);
            goodCouponDO.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
    }

    /**
     * 生成小程序短链接
     *
     * @param wechatJump wechatJump
     * @return String
     */
    @Override
    public String generateUrlLink(WechatJumpParam wechatJump) {
        int j = 0;
        //String wechatToken = weChatService.getWechatToken(true, wechatJump.getBusinessId().toString());
        //String token = "CLOUD_SECRET_REQUIRED";
        Long businessId = BusinessContextHolder.getRequiredBusinessId();
        //String accessToken = weChatService.getWechatToken(true, String.valueOf(businessId));
        String wechatToken = wxActionApi.getWechatToken(businessId);
        JSONObject json = postWxGenerateUrl(wechatJump, wechatToken);
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
        } catch (Exception e) {
            return null;
        }

    }

    private String getSortUrl(String longUrl) {
        ShortUrlRequest request = ShortUrlRequest.builder()
                .longUrl(longUrl)
                .tags(new ArrayList<>())
                .forwardQuery(true)
                .build();

        ShortUrlResponse response = shortUrlService.createShortUrl(request);
        return response.getShortUrl();
    }

    @Override
    public List<GoodCouponDO> getListByCouponIds(List<Long> couponIds) {
        LambdaQueryWrapper<GoodCouponDO> goodCouponDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        goodCouponDOLambdaQueryWrapper.in(GoodCouponDO::getId, couponIds);
        return goodCouponMapper.selectList(goodCouponDOLambdaQueryWrapper);
    }

    @Override
    public PageResult<GoodCouponRespVO> goodCouponPage(Integer pageNum, Integer pageSize, String couponName, String remark) {
        PageResult<GoodCouponRespVO> pageResult = new PageResult<>();
        LambdaQueryWrapper<GoodCouponDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(GoodCouponDO::getIsGround, GoodCouponConstants.IS_GROUND_1);
        lambdaQueryWrapper.and(wrapper ->
                wrapper.eq(GoodCouponDO::getUseType, 0).gt(GoodCouponDO::getCouponEndTime, new Date())
                        .or()
                        .in(GoodCouponDO::getUseType, 1, 2, 3)
        );
        lambdaQueryWrapper.in(GoodCouponDO::getCouponType, List.of(0, 1, 2));

        lambdaQueryWrapper.and(wrapper ->
                wrapper.ne(GoodCouponDO::getUseRules, GoodCouponConstants.USE_RULE_1)
                        .or()
                        .isNull(GoodCouponDO::getUseRules));

        if (StringUtils.isNotEmpty(remark)) {
            lambdaQueryWrapper.and(wrapper -> wrapper.like(GoodCouponDO::getRemark, remark));
        }

        if (StringUtils.isNotEmpty(couponName)) {
            lambdaQueryWrapper.and(wrapper -> wrapper.like(GoodCouponDO::getCouponName, couponName));
        }

        lambdaQueryWrapper.orderByDesc(GoodCouponDO::getCreateTime);

        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNum);
        pageParam.setPageSize(pageSize);
        PageResult<GoodCouponDO> goodCouponDOPageResult = goodCouponMapper.selectPage(pageParam, lambdaQueryWrapper);
        pageResult.setTotal(goodCouponDOPageResult.getTotal());
        pageResult.setList(new ArrayList<>());
        if (ObjectUtil.isNotEmpty(goodCouponDOPageResult.getList())) {

            List<GoodCouponRespVO> goodCouponRespVOList = new ArrayList<>();
            for (GoodCouponDO goodCouponDO : goodCouponDOPageResult.getList()) {
                GoodCouponRespVO goodCouponRespVO = new GoodCouponRespVO();
                BeanUtils.copyProperties(goodCouponDO, goodCouponRespVO);
                goodCouponRespVOList.add(goodCouponRespVO);
            }

            pageResult.setList(goodCouponRespVOList);
        }

        return pageResult;
    }

    @Override
    public boolean updateBatch(List<GoodCouponDO> goodCoupons) {
        return goodCouponMapper.updateBatch(goodCoupons);
    }

    @Override
    public void updateReceivedNumAndCouponNumById(int sendNum, Long goodCouponId) {
        UpdateWrapper<GoodCouponDO> goodCouponUpdateWrapper = new UpdateWrapper<>();
        goodCouponUpdateWrapper.setSql("received_num = received_num +" + sendNum);
        goodCouponUpdateWrapper.setSql("coupon_num = coupon_num -" + sendNum);

        goodCouponUpdateWrapper.lambda().eq(GoodCouponDO::getId, goodCouponId);
        goodCouponMapper.update(goodCouponUpdateWrapper);
    }

    @Override
    public GoodCouponVO selectCouponByCode(String couponCode) {
        LambdaQueryWrapper<GoodCouponDO> goodCouponDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        goodCouponDOLambdaQueryWrapper.eq(GoodCouponDO::getCouponCode, couponCode);
        GoodCouponDO goodCouponDO = goodCouponMapper.selectOne(goodCouponDOLambdaQueryWrapper);
        GoodCouponVO goodCouponVO = BeanUtils.toBean(goodCouponDO, GoodCouponVO.class);
        return goodCouponVO;
    }

    /**
     * 按优惠券编码查询积分商品详情专用的完整信息，不改变原优惠券查询方法。
     */
    @Override
    public PointsProductCouponDetailVO selectPointsProductCouponByCode(String couponCode) {
        if (cn.hutool.core.util.StrUtil.isBlank(couponCode)) {
            log.warn("积分商品详情查询优惠券失败，优惠券编码为空");
            return null;
        }
        PointsProductCouponDetailVO result =
                goodCouponMapper.selectPointsProductCouponByCode(couponCode.trim());
        if (result == null) {
            log.warn("积分商品详情未查询到优惠券，couponCode={}", couponCode);
            return null;
        }
        result.setCouponStores(BeanUtils.toBean(
                couponStoreService.selectByCouponId(result.getId()), CouponStoreInfoVO.class));
        result.setCouponCommodities(BeanUtils.toBean(
                couponCommodityService.selectByCouponId(result.getId()), CouponCommodityInfoVO.class));
        return result;
    }

    @Override
    public GoodCouponRespVO goodCouponInfo(Long id) {
        GoodCouponRespVO goodCouponRespVO = new GoodCouponRespVO();
        LambdaQueryWrapper<GoodCouponDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodCouponDO::getId, id);
        GoodCouponDO goodCouponDO = goodCouponMapper.selectOne(wrapper);
        BeanUtils.copyProperties(goodCouponDO, goodCouponRespVO);
        goodCouponRespVO.setCouponStores(couponStoreService.selectByCouponId(id));
        goodCouponRespVO.setCouponCommodities(couponCommodityService.selectByCouponId(id));
        return goodCouponRespVO;
    }


    @Override
    public PageResult<GoodCouponRespVO> getGoodCouponPage(GoodCouponGetPageReqVO goodCouponPageReqVO) {
        LocalDateTime start = LocalDateTime.now();
        log.info("getGoodCouponPage,{}", start);
        PageResult<GoodCouponRespVO> pageResult = new PageResult<>();

        LambdaQueryWrapper<GoodCouponDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        List<Long> storeIds = new ArrayList<>();
        //  List<Long> goodCouponIds = new ArrayList<>();

        //获取搜索区域范围的门店
        if (ObjectUtil.isNotEmpty(goodCouponPageReqVO.getOrgId())) {
            CommonResult<List<Long>> listCommonResult = orgStoreApi.selectByOrgStoreList(goodCouponPageReqVO.getOrgId());
            if (ObjectUtil.isNotEmpty(listCommonResult.getData())) {
                storeIds.addAll(listCommonResult.getData());
            } else {
                lambdaQueryWrapper.eq(GoodCouponDO::getIsCommon, 1);
            }

        }


        //获取门店 id
        if (ObjectUtil.isNotEmpty(goodCouponPageReqVO.getStoreId())) {
            storeIds.add(goodCouponPageReqVO.getStoreId());
        }
        //查询优惠卷门店表
        if (ObjectUtil.isNotEmpty(storeIds)) {
            List<CouponStoreDO> storeDOList = couponStoreService.selectByStoreIds(storeIds);
            if (ObjectUtil.isNotEmpty(storeDOList)) {
                // goodCouponIds.addAll(storeDOList.stream().map(CouponStoreDO::getCouponId).toList()) ;
                lambdaQueryWrapper.and(
                        wrapper -> wrapper.in(GoodCouponDO::getId, storeDOList.stream().map(CouponStoreDO::getCouponId).toList())
                                .or().eq(GoodCouponDO::getIsCommon, 1)
                );
            } else {
                lambdaQueryWrapper.eq(GoodCouponDO::getIsCommon, 1);
            }
        }
        //查询优惠券商品表
        if (ObjectUtil.isNotEmpty(goodCouponPageReqVO.getCommodityId())) {

            List<CouponCommodityDO> couponCommodityDOList = couponCommodityService.selectByCommodityId(goodCouponPageReqVO.getCommodityId());
            if (ObjectUtil.isNotEmpty(couponCommodityDOList)) {
                // goodCouponIds.addAll(couponCommodityDOList.stream().map(CouponCommodityDO::getCouponId).toList());
                lambdaQueryWrapper.and(
                        wrapper -> wrapper.in(GoodCouponDO::getId, couponCommodityDOList.stream().map(CouponCommodityDO::getCouponId).toList())
                                .or().eq(GoodCouponDO::getIsCommonStore, 1)
                );
            } else {
                lambdaQueryWrapper.eq(GoodCouponDO::getIsCommonStore, 1);
            }
        }

        PageParam pageParam = new PageParam();
        pageParam.setPageNo(goodCouponPageReqVO.getPageNo());
        pageParam.setPageSize(goodCouponPageReqVO.getPageSize());



        /*if (ObjectUtil.isNotEmpty(goodCouponIds)){
           // lambdaQueryWrapper.in(GoodCouponDO::getId, goodCouponIds);
            lambdaQueryWrapper.and(
                    wrapper -> wrapper.in(GoodCouponDO::getId, goodCouponIds)
                            .or().eq(GoodCouponDO::getIsCommon, 1)
            );
        }*/
        // 处理remark的正确方式
        if (ObjectUtil.isNotEmpty(goodCouponPageReqVO.getRemark())) {
            lambdaQueryWrapper.and(wrapper -> wrapper
                    .like(GoodCouponDO::getRemark, goodCouponPageReqVO.getRemark())
                    .or()
                    .like(GoodCouponDO::getCouponName, goodCouponPageReqVO.getRemark())
            );
        }
        if (ObjectUtil.isNotEmpty(goodCouponPageReqVO.getUserRestrictions())) {
            lambdaQueryWrapper.eq(GoodCouponDO::getUserRestrictions, goodCouponPageReqVO.getUserRestrictions());
        }
        if (ObjectUtil.isNotEmpty(goodCouponPageReqVO.getCouponType())) {
            if (Objects.equals(goodCouponPageReqVO.getCouponType(), GoodCouponConstants.COUPON_TYPE_3)) {
                lambdaQueryWrapper.eq(GoodCouponDO::getCouponType, GoodCouponConstants.COUPON_TYPE_0);
                lambdaQueryWrapper.eq(GoodCouponDO::getUseRules, GoodCouponConstants.USE_RULE_1);
            } else if (Objects.equals(goodCouponPageReqVO.getCouponType(), GoodCouponConstants.COUPON_TYPE_4)) {
                lambdaQueryWrapper.eq(GoodCouponDO::getCouponType, GoodCouponConstants.COUPON_TYPE_2);
                lambdaQueryWrapper.eq(GoodCouponDO::getUseRules, GoodCouponConstants.USE_RULE_1);
            } else {
                lambdaQueryWrapper.eq(GoodCouponDO::getCouponType, goodCouponPageReqVO.getCouponType());
                lambdaQueryWrapper.and(
                        wrapper -> wrapper.isNull(GoodCouponDO::getUseRules)
                                .or().ne(GoodCouponDO::getUseRules, GoodCouponConstants.USE_RULE_1)
                );
            }
        }
        if (ObjectUtil.isNotEmpty(goodCouponPageReqVO.getIsGround())) {
            lambdaQueryWrapper.eq(GoodCouponDO::getIsGround, goodCouponPageReqVO.getIsGround());
        }

        if (ObjectUtil.isNotEmpty(goodCouponPageReqVO.getIsCommon())) {
            lambdaQueryWrapper.eq(GoodCouponDO::getIsCommon, goodCouponPageReqVO.getIsCommon());
        }


        lambdaQueryWrapper.orderByDesc(GoodCouponDO::getCreateTime);
        PageResult<GoodCouponDO> goodCouponDOPageResult = goodCouponMapper.selectPage(pageParam, lambdaQueryWrapper);
        log.info("getGoodCouponPage,查完优惠券{}", Duration.between(start, LocalDateTime.now()).toMillis());
        pageResult.setTotal(goodCouponDOPageResult.getTotal());
        pageResult.setList(new ArrayList<>());
        if (ObjectUtil.isNotEmpty(goodCouponDOPageResult.getList())) {
            List<GoodCouponRespVO> goodCouponRespVOList = new ArrayList<>();


            List<GoodCouponDO> goodCouponDOS = goodCouponDOPageResult.getList();

            List<Long> respGoodCouponIds = goodCouponDOS.stream().map(GoodCouponDO::getId).toList();

            List<CouponStoreDO> storeDOList = couponStoreService.selectByCouponIds(respGoodCouponIds);

            //获取整页优惠券相关门店信息
            Map<Long, List<CouponStoreDO>> couponStoreMap = new HashMap<>();

            if (ObjectUtil.isNotEmpty(storeDOList)) {
                couponStoreMap = storeDOList.stream().collect(Collectors.groupingBy(CouponStoreDO::getCouponId));
            }
            log.info("getGoodCouponPage,查完门店{}", Duration.between(start, LocalDateTime.now()).toMillis());
            List<CouponCommodityDO> couponCommodityDOList = couponCommodityService.selectByCouponIds(respGoodCouponIds);

            //获取整页优惠券相关商品信息
            Map<Long, List<CouponCommodityDO>> couponCommodityMap = new HashMap<>();

            if (ObjectUtil.isNotEmpty(couponCommodityDOList)) {
                couponCommodityMap = couponCommodityDOList.stream().collect(Collectors.groupingBy(CouponCommodityDO::getCouponId));
            }
            log.info("getGoodCouponPage,查完商品{}", Duration.between(start, LocalDateTime.now()).toMillis());
            //获取整页优惠券相关金额信息
            List<CouponAmountVO> userCouponRecordDOS = userCouponRecordService.selectCouponAmount(respGoodCouponIds);

            Map<Long, BigDecimal> userCouponRecordMap = new HashMap<>();

            if (ObjectUtil.isNotEmpty(userCouponRecordDOS)) {
                userCouponRecordMap = userCouponRecordDOS.stream()
                        .collect(Collectors.toMap(
                                CouponAmountVO::getId,   // Key 提取方式
                                CouponAmountVO::getAmount, // Value 提取方式
                                (oldValue, newValue) -> newValue // 如果Key冲突，保留新值（可选）
                        ));
            }

            log.info("getGoodCouponPage,查完消费记录{}", Duration.between(start, LocalDateTime.now()).toMillis());
            for (GoodCouponDO goodCouponDO : goodCouponDOPageResult.getList()) {
                GoodCouponRespVO goodCouponRespVO = new GoodCouponRespVO();
                BeanUtils.copyProperties(goodCouponDO, goodCouponRespVO);
                //处理整页优惠券相关门店信息
                if (goodCouponDO.getIsCommon().equals(CouponISCommonEnum.ALL.getValue())) {
                    goodCouponRespVO.setCouponStoreNames("全部门店可用");
                } else {
                    List<CouponStoreDO> storeDOList1 = couponStoreMap.get(goodCouponDO.getId());


                    String storeName = "";

                    if (ObjectUtil.isNotEmpty(storeDOList1)) {
                        List<String> storeNameList = storeDOList1.stream().filter(item -> ObjectUtil.isNotEmpty(item.getStoreName())).map(CouponStoreDO::getStoreName).toList();

                        if (storeNameList.size() <= 2) {
                            storeName = String.join("，", storeNameList);
                        } else {
                            // 店铺数量大于2，取前两个店铺名拼接
                            storeName = String.join("，", storeNameList.subList(0, 2)) + "等" + storeNameList.size() + "个门店";
                        }
                    }
                    goodCouponRespVO.setCouponStores(storeDOList1);
                    goodCouponRespVO.setCouponStoreNames(storeName);

                }


                //处理整页优惠券相关商品信息
                if (goodCouponDO.getIsCommonStore().equals(CouponISCommonStoreEnum.DEFAULT.getValue())) {
                    goodCouponRespVO.setCouponCommodityNames("全部商品可用");
                } else {
                    List<CouponCommodityDO> couponCommodityDOList1 = couponCommodityMap.get(goodCouponDO.getId());

                    String commodityName = "";
                    if (ObjectUtil.isNotEmpty(couponCommodityDOList1)) {
                        List<String> commodityNameList = couponCommodityDOList1.stream().map(CouponCommodityDO::getCommodityName).toList();

                        if (commodityNameList.size() <= 2) {
                            commodityName = String.join("，", commodityNameList);
                        } else {
                            commodityName = String.join("，", commodityNameList.subList(0, 2) + "等" + commodityNameList.size() + "个商品");
                        }
                    }

                    if (goodCouponDO.getIsCommonStore().equals(CouponISCommonStoreEnum.PART.getValue())) {
                        commodityName = commodityName + IS_USE;
                    } else {
                        commodityName = commodityName + IS_NOT_USE;
                    }
                    goodCouponRespVO.setCouponCommodities(couponCommodityDOList1);
                    goodCouponRespVO.setCouponCommodityNames(commodityName);
                }
                //处理整页优惠券相关金额信息
                if (ObjectUtil.isNotEmpty(userCouponRecordMap)) {
                    goodCouponRespVO.setPayAmount(userCouponRecordMap.get(goodCouponDO.getId()));
                } else {
                    goodCouponRespVO.setPayAmount(BigDecimal.ZERO);
                }
                goodCouponRespVOList.add(goodCouponRespVO);
            }
            log.info("getGoodCouponPage,处理完所有{}", Duration.between(start, LocalDateTime.now()).toMillis());
            pageResult.setList(goodCouponRespVOList);
        }


        return pageResult;
    }


    @Override
    public CommonResult<Boolean> updateGoodCoupon(GoodCouponCardVO cardVO) {
        LambdaUpdateWrapper<GoodCouponDO> qw = new LambdaUpdateWrapper<GoodCouponDO>();
        qw.eq(GoodCouponDO::getCouponCode, cardVO.getCouponCode());
        GoodCouponDO goodCoupon = goodCouponMapper.selectOne(qw);
        if (!ObjectUtil.isEmpty(goodCoupon)) {
            LambdaUpdateWrapper<GoodCouponDO> queryWrapperOld = new LambdaUpdateWrapper<GoodCouponDO>();
            queryWrapperOld.eq(GoodCouponDO::getMemberLevel, cardVO.getMemberLevel());
            GoodCouponDO goodCouponOld = new GoodCouponDO();
            goodCouponOld.setMemberLevel(0);
            goodCouponMapper.update(goodCouponOld, queryWrapperOld);
            goodCoupon.setMemberLevel(cardVO.getMemberLevel());
            goodCouponMapper.update(goodCoupon, qw);
            return CommonResult.success(true);
        }
        return CommonResult.success(false);
    }

    @Override
    public PageResult<GoodCouponPageRespVO> couponPageWithPoints(GoodCouponPageReqVO reqVO) {
        LambdaQueryWrapper<GoodCouponDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodCouponDO::getIsCommon, 1).eq(GoodCouponDO::getIsGround, 1)
                .eq(GoodCouponDO::getIsCommonStore, 1).ne(GoodCouponDO::getUseType, 0);
        queryWrapper.orderByDesc(GoodCouponDO::getCreateTime);
        queryWrapper.like(ObjectUtil.isNotEmpty(reqVO.getCouponName()),GoodCouponDO::getCouponName, reqVO.getCouponName());
        queryWrapper.like(ObjectUtil.isNotEmpty(reqVO.getCouponType()),GoodCouponDO::getCouponType, reqVO.getCouponType());
        PageResult<GoodCouponDO> pageResult = goodCouponMapper.selectPage(reqVO, queryWrapper);
        PageResult<GoodCouponPageRespVO> result = BeanUtils.toBean(pageResult, GoodCouponPageRespVO.class);
        return result;
    }

    @Override
    public List<StoreWecomConfigResDTO> selectStoreList(StoreWecomConfigReqDTO reqDTO) {
        if (reqDTO != null) {
            Long couponId = reqDTO.getCouponId();
            if (!org.springframework.util.StringUtils.isEmpty(couponId)) {
                LambdaQueryWrapper<CouponStoreDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(CouponStoreDO::getCouponId, couponId);
                List<CouponStoreDO> couponStoreDOS = couponStoreMapper.selectList(wrapper);
                if (!org.springframework.util.StringUtils.isEmpty(couponStoreDOS)) {
                    List<Long> collect = couponStoreDOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                    reqDTO.setStoreIdList(collect);
                    CommonResult<List<StoreWecomConfigResDTO>> listCommonResult = storeApi.selectByCouponStoreList(reqDTO);
                    List<StoreWecomConfigResDTO> data = listCommonResult.getData();
                    if (!org.springframework.util.StringUtils.isEmpty(data)) {
                        return data;
                    } else {
                        return new ArrayList<>();
                    }
                }

            }
        }
        return new ArrayList<>();
    }

    @Override
    public void nocCouponStoreNum() {
        QueryWrapper<GoodCouponDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().ne(GoodCouponDO::getStoreLimitNum, GoodCouponConstants.STORE_LIMIT_0);
        //queryWrapper.lambda().eq(GoodCouponDO::getIsGround, GoodCouponConstants.IS_GROUND_1);
        List<GoodCouponDO> goodCouponDOList = this.list(queryWrapper);
        for (GoodCouponDO goodCouponDO : goodCouponDOList) {
            if (goodCouponDO.getId() == 1921802863810600995L) {
                log.info("goodCouponDO.getId() = " + goodCouponDO.getId());
            }
            Map<Long, Integer> allData = CouponCountUtil.getAllData(redisTemplate, goodCouponDO.getId());
            if (ObjectUtil.isNotEmpty(allData)) {
                allData.forEach((k, v) -> {
                    goodCouponMapper.upsertCouponStoreClaim(goodCouponDO.getId(), k, v);
                });
            }
        }
    }

    @Override
    public Void scheduledSend() {
        QueryWrapper<GoodCouponDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(GoodCouponDO::getIsGround, GoodCouponConstants.IS_GROUND_1);
        queryWrapper.lambda().eq(GoodCouponDO::getDistributionMethod, GoodCouponConstants.DISTRIBUTE_METHOD_0);
        List<GoodCouponDO> goodCoupons = this.list(queryWrapper);

        for (GoodCouponDO goodCoupon : goodCoupons) {
            // 判断今天是否该发券
            Boolean b = CouponScheduledTimeUtil.validateCouponTime(goodCoupon);
            if (!b) {
                log.info("优惠券{}不发放", goodCoupon.getId());
                break;
            }
            // 获取优惠券的会员等级
            Integer memberLevel = goodCoupon.getMemberLevel();
            // 获取此会员等级的会员
            CommonResult<List<WxMemberDTO>> result = wxMemberApi.getMemberByMemberLevel(memberLevel);
            List<WxMemberDTO> members = result.getData();

            // 获取会员的数量 优惠券库存够不够
            int size = members.size();
            Integer couponNum = goodCoupon.getCouponNum();
            if (size > couponNum) {
                break;
            }
            log.info("优惠券{}发放", goodCoupon.getId());
            List<UserCouponDO> list = new ArrayList<>();
            UserCouponDO userCouponDO = new UserCouponDO();
            for (int i = 0; i < size; i++) {
                userCouponDO = new UserCouponDO();
                BeanUtils.copyProperties(goodCoupon, userCouponDO);
                GoodCouponRespVO goodCouponRespVO = BeanUtils.toBean(goodCoupon, GoodCouponRespVO.class);
                WxMemberDTO member = members.get(i);
                Long memberId = member.getMemberId();
                Long couponId = goodCoupon.getId();
                userCouponDO.setCouponCode(goodCoupon.getCouponCode());
                userCouponDO.setId(null);
                userCouponDO.setUserId(memberId);
                userCouponDO.setCouponId(goodCoupon.getId());
                userCouponDO.setIsUsed(UserCouponConstants.IS_USED_0);
                userCouponDO.setCouponUseTime(goodCoupon.getUseTime());
                userCouponDO.setUseTime(null);
                userCouponDO.setCouponCreateTime(goodCoupon.getCreateTime());

                CouponTimeUtil.parseCouponTime(goodCouponRespVO, userCouponDO);
                userCouponDO.setUseTime(null);
                userCouponDO.setCreateTime(LocalDateTime.now());
                userCouponDO.setUpdateTime(LocalDateTime.now());
                userCouponDO.setMemberMobile(member.getMemberMobile());
                userCouponDO.setDeleted(Boolean.FALSE);
                list.add(userCouponDO);
            }

            userCouponShardService.insertBatch(list);
            UpdateWrapper<GoodCouponDO> goodCouponUpdateWrapper = new UpdateWrapper<>();
            goodCouponUpdateWrapper.setSql("received_num = received_num + " + size);
            goodCouponUpdateWrapper.setSql("coupon_num = coupon_num - " + size);
            goodCouponUpdateWrapper.eq("id", goodCoupon.getId());
            goodCouponMapper.update(goodCouponUpdateWrapper);
        }
        return null;
    }

    @Override
    public Boolean judgmentTime(Long id) {
        GoodCouponDO goodCouponDO = goodCouponMapper.selectById(id);
        if (ObjectUtil.isEmpty(goodCouponDO)) {
            return Boolean.FALSE;
        }
        return CouponScheduledTimeUtil.validateCouponTime(goodCouponDO);
    }

    @Override
    public GoodCouponDataDTO getByIdCoupon(Long couponId) {
        GoodCouponDataDTO goodCouponDataDTO = new GoodCouponDataDTO();
        GoodCouponDO goodCouponDO = goodCouponMapper.selectById(couponId);
        if (goodCouponDO == null) {
            return goodCouponDataDTO;
        }
        goodCouponDataDTO.setId(goodCouponDO.getId());
        goodCouponDataDTO.setCouponName(goodCouponDO.getCouponName());
        goodCouponDataDTO.setIsCommon(goodCouponDO.getIsCommon());
        LambdaQueryWrapper<CouponStoreDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponStoreDO::getCouponId, couponId);
        List<CouponStoreDO> couponStoreDOS = couponStoreMapper.selectList(queryWrapper);
        if (ObjectUtil.isNotEmpty(couponStoreDOS)) {
            List<Long> collect = couponStoreDOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
            goodCouponDataDTO.setStoreIdList(collect);
        }
        return goodCouponDataDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = PROMOTION_GOOD_COUPON_TYPE, subType = PROMOTION_GOOD_COUPON_CREATE_SUB_TYPE, bizNo = "1", success = PROMOTION_GOOD_COUPON_CREATE_SUCCESS)
    public Integer saveTiktokGoodCoupon(TiktokCouponSaveReqVO reqVO) {
        GoodCouponDO goodCouponDO = BeanUtils.toBean(reqVO, GoodCouponDO.class);

        goodCouponDO.setUsedNum(0);
        long time = new DateTime().getTime();
        String couponCode = GoodCouponConstants.COUPON_CODE_PREFIX + time;
        // 优惠券编码
        goodCouponDO.setCouponCode(couponCode);
        // 优惠券剩余数量
        goodCouponDO.setCouponNum(reqVO.getTotalNum());
        // 发放总量
        goodCouponDO.setTotalNum(reqVO.getTotalNum());
        goodCouponDO.setIsGround(GoodCouponConstants.IS_GROUND_0);
        goodCouponDO.setDoorsillType(UserCouponConstants.DOOR_SILL_TYPE_3);
        goodCouponDO.setUseRules(GoodCouponConstants.USE_RULE_1);
        goodCouponDO.setLimitNum(reqVO.getLimitNum());
        Number number = identifierGenerator.nextId(goodCouponDO);
        Long id = number.longValue();
        // 优惠券id
        goodCouponDO.setId(id);

        List<CouponCommodityDO> couponCommodities = reqVO.getCouponCommodities();
        Integer isCommonStore = reqVO.getIsCommonStore();
        if (isCommonStore != GoodCouponConstants.IS_COMMON_STORE_1) {
            if (ObjectUtil.isNotEmpty(couponCommodities)) {
                //优惠券绑定商品 单一商品才绑定
                if (couponCommodities.size() == 1 && !isCommonStore.equals(GoodCouponConstants.IS_COMMON_STORE_3)) {
                    goodCouponDO.setSingleIds(couponCommodities.get(0).getCommodityId().toString());
                }
                couponCommodities.forEach(couponCommodity -> {
                    couponCommodity.setCouponId(id);
                    couponCommodity.setType(isCommonStore);
                    couponCommodity.setId(null);
                    couponCommodity.setDeleted(Boolean.FALSE);
                });
                //优惠券与门店关联
                couponCommodityService.insertBatch(couponCommodities);
            }
        }

        goodCouponDO.setDistributionMethod(GoodCouponConstants.DISTRIBUTE_METHOD_1);

        //优惠券绑定门店
        Integer isCommon = reqVO.getIsCommon();
        List<CouponStoreDO> couponStores = reqVO.getCouponStores();
        if (!isCommon.equals(GoodCouponConstants.IS_COMMON_1)) {
            if (ObjectUtil.isNotEmpty(couponStores)) {
                couponStores.forEach(item -> {
                    item.setId(null);
                    item.setCouponId(id);
                    item.setTotalNum(goodCouponDO.getStoreLimitNum());
                    item.setDeleted(Boolean.FALSE);
                    //redisMap.put(item.getStoreId(), item.getTotalNum());
                });
                couponStoreService.insertBatch(couponStores);
                //CouponCountUtil.initCouponStoreQuantities(redisTemplate,id,redisMap);
            }
        }
        //优惠券领取人群限制
        goodCouponDO.setUserRestrictions(GoodCouponConstants.USER_RESTRICTIONS_0);
        //优惠券是否可分享
        goodCouponDO.setIsShare(GoodCouponConstants.IS_SHARE_0);
        //每人每天领取数量限制
        goodCouponDO.setDayLimit(GoodCouponConstants.DAY_LIMIT_0);
        //优惠券数量剩余显示
        goodCouponDO.setCouponNumVisible(GoodCouponConstants.COUPON_NUM_VISIBLE_1);
        //领取时间限制
        goodCouponDO.setClaimTimeLimit(GoodCouponConstants.CLAIM_TIME_LIMIT_0);

        goodCouponDO.setStoreLimitNum(GoodCouponConstants.STORE_LIMIT_0);
        LogRecordContext.putVariable("createReqVO", reqVO);
        return goodCouponMapper.insert(goodCouponDO);
    }

    @Override
    @LogRecord(type = PROMOTION_GOOD_COUPON_TYPE, subType = PROMOTION_GOOD_COUPON_CREATE_SUB_TYPE, bizNo = "{{#createReqVO.id}}", success = PROMOTION_GOOD_COUPON_CREATE_SUCCESS)
    @Transactional(rollbackFor = Exception.class)
    public Integer updateTiktokGoodCoupon(TiktokCouponSaveReqVO reqVO) {
        Long couponId = reqVO.getId();
        GoodCouponDO one = goodCouponMapper.selectById(couponId);

        Integer totalNum = reqVO.getTotalNum();
        Integer couponNum = one.getCouponNum();
        Integer receivedNum = one.getReceivedNum();
        Integer totalNum1 = one.getTotalNum();
        if (totalNum < receivedNum) {
            throw exception(COUPON_NUM_ERROR);
        }
        int i = couponNum - totalNum1 + totalNum;

        LambdaUpdateWrapper<GoodCouponDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(GoodCouponDO::getId, couponId);
        wrapper.set(GoodCouponDO::getTotalNum, totalNum);
        wrapper.set(GoodCouponDO::getCouponNum, i);
        wrapper.set(GoodCouponDO::getCouponName, reqVO.getCouponName());
        wrapper.set(GoodCouponDO::getCouponExplain, reqVO.getCouponExplain());
        wrapper.set(GoodCouponDO::getCouponImageUrl, reqVO.getCouponImageUrl());
        wrapper.set(GoodCouponDO::getCouponType, reqVO.getCouponType());
        wrapper.set(GoodCouponDO::getReliefOrDiscount, reqVO.getReliefOrDiscount());
        wrapper.set(GoodCouponDO::getRemark, reqVO.getRemark());
        wrapper.set(GoodCouponDO::getHabit, reqVO.getHabit());
        wrapper.set(GoodCouponDO::getIsCommon, reqVO.getIsCommon());
        wrapper.set(GoodCouponDO::getIsCommonStore, reqVO.getIsCommonStore());
        wrapper.set(GoodCouponDO::getUseRules, GoodCouponConstants.USE_RULE_1);
        wrapper.set(GoodCouponDO::getLimitNum, reqVO.getLimitNum());
        wrapper.set(GoodCouponDO::getDoorsillType, UserCouponConstants.DOOR_SILL_TYPE_3);
        couponCommodityService.deleteByCouponId(couponId);
        couponStoreService.deleteByCouponId(couponId);
        List<CouponCommodityDO> couponCommodities = reqVO.getCouponCommodities();
        //优惠券绑定商品
        Integer isCommonStore = reqVO.getIsCommonStore();
        if (isCommonStore != 1) {
            if (ObjectUtil.isNotEmpty(couponCommodities)) {
                //优惠券绑定商品 单一商品才绑定
                if (couponCommodities.size() == 1 && !isCommonStore.equals(GoodCouponConstants.IS_COMMON_STORE_3)) {
                    //goodCouponDO.setSingleIds(couponCommodities.get(0).getCommodityId().toString());
                    wrapper.set(GoodCouponDO::getSingleIds, couponCommodities.get(0).getCommodityId().toString());
                }
                if (couponCommodities.size() > 1 && !isCommonStore.equals(GoodCouponConstants.IS_COMMON_STORE_3)) {
                    //goodCouponDO.setSingleIds(null);
                    wrapper.set(GoodCouponDO::getSingleIds, null);

                }
                couponCommodities.forEach(couponCommodity -> {
                    couponCommodity.setCouponId(couponId);
                    couponCommodity.setType(isCommonStore);
                    couponCommodity.setDeleted(Boolean.FALSE);
                    couponCommodity.setId(null);
                });
                couponCommodityService.insertBatch(couponCommodities);
            }
        }

        //优惠券绑定门店
        Integer isCommon = reqVO.getIsCommon();
        if (isCommon != 1) {
            List<CouponStoreDO> couponStores = reqVO.getCouponStores();
            if (ObjectUtil.isNotEmpty(couponStores)) {
                couponStores.forEach(item -> {
                    item.setCouponId(couponId);
                    item.setDeleted(Boolean.FALSE);
                    item.setId(null);
                });
                couponStoreService.insertBatch(couponStores);
            }
        }

        LogRecordContext.putVariable("createReqVO", reqVO);
        return goodCouponMapper.update(wrapper);
    }

    @Override
    public PageResult<StorePageResVO> selectByStoreList(AdvertisingStorePageReqVO storePageReqVO) {
        StorePageListReqVO storePageListReqVO = new StorePageListReqVO();
        BeanUtils.copyProperties(storePageReqVO, storePageListReqVO);
        storePageListReqVO.setStoreStatus(0);
        CommonResult<PageResult<StorePageResVO>> pageResultCommonResult = storeInfoApi.chooseStoreForTiktok(storePageListReqVO);
        return pageResultCommonResult.getData();
    }

    @Override
    @LogRecord(type = PROMOTION_TIK_TOK_GOOD_COUPON_COPY_SUB_TYPE, subType = PROMOTION_GOOD_COUPON_COPY_SUB_TYPE, bizNo = "{{#id}}", success = PROMOTION_TIK_TOK_GOOD_COUPON_COPY_SUCCESS)
    public Integer tiktokCopy(Long id) {
        GoodCouponRespVO goodCoupon = getCouponById(id);
        TiktokCouponSaveReqVO createReqVO = BeanUtils.toBean(goodCoupon, TiktokCouponSaveReqVO.class);
        createReqVO.setId(null);
        createReqVO.setCouponName(goodCoupon.getCouponName());
        createReqVO.setCouponStores(couponStoreService.selectByCouponId(id));
        createReqVO.setCouponCommodities(couponCommodityService.selectByCouponId(id));
        LogRecordContext.putVariable("id", id);
        return saveTiktokGoodCoupon(createReqVO);
    }

    @Override
    public Integer updateAllH5() {
        List<GoodCouponDO> goodCouponDOS = goodCouponMapper.selectList();
        for (GoodCouponDO item : goodCouponDOS) {
            LambdaUpdateWrapper<GoodCouponDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(GoodCouponDO::getId, item.getId());
            wrapper.set(GoodCouponDO::getH5SortUrl, this.getSortUrl(h5Host + item.getId()));
            goodCouponMapper.update(wrapper);
        }
        return 0;
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    @DataPermission(enable = false)
    public void memberDayCoupon() {
        // 遍历所有分片（0-9）
        for (int shard = 0; shard < 10; shard++) {
            log.info("开始处理分片{}的会员发券逻辑", shard);
            Long lastCursorId = 0L; // 游标初始值（雪花ID均大于0）
            boolean hasMoreData = true;

            try {
                // 游标分页循环：直到查不到数据为止
                while (hasMoreData) {
                    // ******** 游标分页查询当前分片的一批数据 ********
                    Map<Integer, List<WxMemberDTO>> levelMemberMap =
                            wxMemberApi.getMembersMapByShard(shard, lastCursorId, BATCH_SIZE);

                    // 当前批次无数据，终止当前分片的循环
                    if (levelMemberMap.isEmpty()) {
                        hasMoreData = false;
                        log.info("分片{}：游标{}后无更多数据，处理完成", shard, lastCursorId);
                        continue;
                    }

                    // ******** 构建当前批次的发券DTO，并立即发券 ********
                    Set<MemberCouponDTO> batchCouponSet = new HashSet<>();
                    Long currentBatchMaxId = 0L; // 记录当前批次最大ID（更新游标用）
                    for (Map.Entry<Integer, List<WxMemberDTO>> entry : levelMemberMap.entrySet()) {
                        Integer memberLevel = entry.getKey();
                        List<WxMemberDTO> memberList = entry.getValue();
                        List<Long> couponIds = LEVEL_COUPON_MAP.get(memberLevel);

                        // 空值校验
                        if (Objects.isNull(memberLevel) || memberLevel == 1
                                || CollectionUtils.isEmpty(memberList)
                                || CollectionUtils.isEmpty(couponIds)) {
                            log.info("分片{}：等级{}无有效会员/优惠券配置，跳过", shard, memberLevel);
                            continue;
                        }

                        // 构建DTO并记录当前批次最大ID
                        for (WxMemberDTO memberDTO : memberList) {
                            if (Objects.isNull(memberDTO) || Objects.isNull(memberDTO.getMemberId())
                                    || Objects.isNull(memberDTO.getMemberId())) { // 依赖DTO中的主键ID
                                continue;
                            }
                            // 更新当前批次最大ID（用于下一批游标）
                            if (memberDTO.getMemberId() > currentBatchMaxId) {
                                currentBatchMaxId = memberDTO.getMemberId();
                            }
                            MemberCouponDTO couponDTO = new MemberCouponDTO();
                            couponDTO.setMemberId(memberDTO.getMemberId());
                            couponDTO.setMemberMobile(memberDTO.getMemberMobile());
                            couponDTO.setMemberName(memberDTO.getMemberName());
                            couponDTO.setCouponIds(couponIds);
                            batchCouponSet.add(couponDTO);
                        }
                    }

                    // ******** 批次级发券（核心优化：不累积，批处理批发）********
                    if (!CollectionUtils.isEmpty(batchCouponSet)) {
                        boolean sendSuccess = false;
                        int retryCount = 0;
                        // 批次发券重试逻辑
                        while (retryCount < SEND_COUPON_RETRY_TIMES && !sendSuccess) {
                            try {
                                userCouponService.sendCoupons(batchCouponSet);
                                sendSuccess = true;
                                log.info("分片{}：游标{}~{}批次发券成功，共{}条",
                                        shard, lastCursorId, currentBatchMaxId, batchCouponSet.size());
                            } catch (Exception e) {
                                retryCount++;
                                log.error("分片{}：游标{}~{}批次发券失败（重试第{}次）：{}",
                                        shard, lastCursorId, currentBatchMaxId, retryCount, e.getMessage(), e);
                                // 指数退避重试
                                try {
                                    Thread.sleep(RETRY_INTERVAL_MS * retryCount);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                        }
                        // 最终失败记录，人工处理
                        if (!sendSuccess) {
                            log.error("分片{}：游标{}~{}批次发券最终失败，共{}条，需人工处理",
                                    shard, lastCursorId, currentBatchMaxId, batchCouponSet.size());
                            // 可选：记录失败数据到日志/数据库
                        }
                    }

                    // 更新游标：下一批从当前批次最大ID开始
                    lastCursorId = currentBatchMaxId;
                    // 批次间休眠，降低数据库压力
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (Exception e) {
                log.error("分片{}整体处理异常：{}", shard, e.getMessage(), e);
                // 分片级异常不终止整体流程，继续处理下一个分片
                continue;
            }
            log.info("分片{}处理完成", shard);
        }
        log.info("所有分片会员日发券逻辑处理完成");

    }
    private static final Map<Integer, List<Long>> LEVEL_COUPON_MAP = new HashMap<>();
    static {
        // LV1-LV4：单券
        LEVEL_COUPON_MAP.put(2, Collections.singletonList(1996868154509328386L));
        LEVEL_COUPON_MAP.put(3, Arrays.asList(1996868154509328386l,1996870490468229121L));
        LEVEL_COUPON_MAP.put(4, Arrays.asList(1996868154509328386L,1996870490468229121L,1996871013686685698L));
        // LV5：双券（两个优惠券ID）
        LEVEL_COUPON_MAP.put(5, Arrays.asList(1996868154509328386L,1996870490468229121L,1996871013686685698L,1996871290141646850L));
    }

}
