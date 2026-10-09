package com.htyoudao.youdao.module.promotion.service.couponpackage;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.generator.SnowflakeGenerator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.util.concurrent.RateLimiter;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.security.core.LoginUser;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.api.enums.IsGroundConstant;
import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.MemberCouponDTO;
import com.htyoudao.youdao.module.promotion.constant.*;
import com.htyoudao.youdao.module.promotion.controller.admin.couponPackageShare.VO.CouponPackageShareSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.*;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.ShortUrlRequest;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.ShortUrlResponse;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.WechatJumpParam;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponCountRespVo;
import com.htyoudao.youdao.module.promotion.controller.app.couponpackage.vo.ClaimCouponPackageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponcommodity.CouponCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstoreclaimnum.CouponStoreClaimNumDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcouponpackage.GoodCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.packagestoreclaimnum.PackageStoreClaimNumDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage.UserCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponpackage.CouponPackageMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcouponpackage.GoodCouponPackageMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercouponpackage.UserCouponPackageMapper;
import com.htyoudao.youdao.module.promotion.enums.channel.ActivityChannelTypeEnum;
import com.htyoudao.youdao.module.promotion.service.activityChannel.ActivityChannelService;
import com.htyoudao.youdao.module.promotion.service.activityJD.ActivityJDService;
import com.htyoudao.youdao.module.promotion.service.couponPackageShare.CouponPackageShareService;
import com.htyoudao.youdao.module.promotion.service.couponpackage.claimcouponpackage.ClaimPackageService;
import com.htyoudao.youdao.module.promotion.service.goodcoupon.GoodCouponService;
import com.htyoudao.youdao.module.promotion.service.goodcouponpackage.GoodCouponPackageService;
import com.htyoudao.youdao.module.promotion.service.packagestoreclaimnum.PackageStoreClaimNumService;
import com.htyoudao.youdao.module.promotion.service.pvstatistics.PvStatisticsService;
import com.htyoudao.youdao.module.promotion.service.userCouponPackage.UserCouponPackageService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.UserCouponMasterService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.UserCouponShardService;
import com.htyoudao.youdao.module.promotion.service.wechat.ShortUrlService;
import com.htyoudao.youdao.module.promotion.util.CouponCountUtil;
import com.htyoudao.youdao.module.promotion.util.CouponPackageCountUtil;
import com.htyoudao.youdao.module.promotion.util.CouponTimeUtil;
import com.htyoudao.youdao.module.promotion.util.DateUtils;
import com.htyoudao.youdao.module.promotion.util.ImageValueParseUtil;
import com.htyoudao.youdao.module.promotion.util.page.PageUtils;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants.*;

/**
 * 优惠券包 Service 实现类
 *
 * @author dht
 */
@Service
@Slf4j
@RefreshScope
public class CouponPackageServiceImpl implements CouponPackageService {


    @Resource
    private GoodCouponService goodCouponService;

    @Resource
    private GoodCouponPackageMapper goodCouponPackageMapper;

    @Resource
    private CouponPackageMapper couponPackageMapper;

    @Resource
    private GoodCouponPackageService goodCouponPackageService;

    @Resource
    private ActivityChannelService activityChannelService;

    /**
     * 优惠券包短链接
     */
    @Value("${couponPackage.sort.sortPath}")
    private String sortPath;

    /**
     * 优惠券包短链接H5
     */
    @Value("${couponPackage.sort.h5.host}")
    private String h5Host;

    @Resource
    private ShortUrlService shortUrlService;

    private static final RateLimiter couponPackageLimiter = RateLimiter.create(0.67);


    @DubboReference
    private WxMemberApi wxMemberApi;

    @Resource
    private UserCouponPackageMapper userCouponPackageMapper;

    @Resource
    private UserCouponMapper userCouponMapper;

    @Resource
    @Qualifier("claimPackageEmpty")
    private ClaimPackageService claimPackageService;

    @Resource
    @Qualifier("promotionThreadPool") // 注入配置的全局线程池
    private Executor promotionThreadPool;

    @Resource
    private UserCouponMasterService userCouponMasterService;

    /**
     * 周周券包id
     */
    //@Resource
    @Value("${couponPackage.zzPackageId}")
    private String zzPackageId;

    @Resource
    private RedisCache redisCache;

    @Resource
    private PvStatisticsService pvStatisticsService;

    private static final String MARKET_USER_KEY = "market:user:";

    @Resource
    private UserCouponShardService userCouponShardService;

    @Resource
    private UserCouponPackageService userCouponPackageService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;


    @Resource
    private PackageStoreClaimNumService packageStoreClaimNumService;


    @Resource
    private CouponPackageShareService couponPackageShareService;

    @Resource
    private CouponPackageMasterService couponPackageMasterService;

    @Resource
    private ActivityJDService activityJDService;



    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = PROMOTION_GOOD_COUPON_PACKAGE_TYPE, subType = PROMOTION_GOOD_COUPON_PACKAGE_CREATE_SUB_TYPE, bizNo = "{{1}}", success = PROMOTION_GOOD_COUPON_PACKAGE_CREATE_SUCCESS)
    public Boolean insert(CouponPackageSaveReqVO createReqVO) {
        Integer userRestrictions = createReqVO.getUserRestrictions();
        if(Objects.equals(userRestrictions,CouponPackageConstant.TARGET_AUDIENCE_4)){
            QueryWrapper<CouponPackageDO> qw = new QueryWrapper<>();
            qw.lambda().eq(CouponPackageDO::getUserRestrictions,CouponPackageConstant.TARGET_AUDIENCE_4);
            Long l = couponPackageMapper.selectCount(qw);
            if(l > 0){
                throw exception(COUPON_PACKAGE_ONE_MEMBERDAY);
            }
        }
        CouponPackageDO couponPackage = new CouponPackageDO();
        BeanUtil.copyProperties(createReqVO, couponPackage);
        couponPackage.setPackageNum(couponPackage.getTotalNum());
        couponPackage.setReceivedNum(0);

        //领取时间判断
        if(Objects.equals(couponPackage.getClaimTimeLimit(), GoodCouponConstants.CLAIM_TIME_LIMIT_1)){
            if(ObjectUtil.isEmpty(couponPackage.getClaimTimeSlot()) &&
                ObjectUtil.isEmpty(couponPackage.getClaimDayNo()) &&
                ObjectUtil.isEmpty(couponPackage.getClaimTime()) &&
                ObjectUtil.isEmpty(couponPackage.getClaimWeekNo())
            ){
                throw exception(COUPON_PACKAGE_CLAIM_TIME_NULL);
            }
        }



        SnowflakeGenerator snowflakeGenerator = new SnowflakeGenerator();
        Long id = snowflakeGenerator.next();
        couponPackage.setId(id);

        List<GoodCouponPackageSaveReqVO> goodCouponPackages = createReqVO.getGoodCouponPackages();
        if (CollectionUtil.isEmpty(goodCouponPackages)) {
            throw exception(COUPON_PACKAGE_NUM_NULL);
        } else {
            if (goodCouponPackages.size() > GoodCouponPackageConstants.GOOD_COUPON_PACKAGE_SIZE) {
                throw exception(COUPON_PACKAGE_NUM_SIZE);
            }
            //判断优惠券数量是否大于 券包里券的数量 * 优惠券包数量
            Map<Long, Integer> collect = goodCouponPackages.stream().collect(Collectors.toMap(GoodCouponPackageSaveReqVO::getCouponId, GoodCouponPackageSaveReqVO::getNum));
            List<Long> couponIds = goodCouponPackages.stream().map(GoodCouponPackageSaveReqVO::getCouponId).toList();
            List<GoodCouponDO> goodCoupons = goodCouponService.getByIds(couponIds);
            List<String> couponNames = new ArrayList<>();
            for (GoodCouponDO goodCoupon : goodCoupons) {
                if (goodCoupon.getCouponNum() < collect.get(goodCoupon.getId()) * couponPackage.getPackageNum()) {
                    couponNames.add(goodCoupon.getCouponName());
                }
            }
            if (CollectionUtil.isNotEmpty(couponNames)) {
                String message = String.join(",", couponNames);
                //String format = String.format("优惠券--%s数量不足,请修改优惠券数量!", );
                throw exception(COUPON_PACKAGE_NUM_ERROR,message);
            }
        }

        /*try{
            String longUrl = goodCouponService.generateUrlLink(WechatJumpParam.builder().path(sortPath).businessId(BusinessContextHolder.getBusinessId())
                    .query("id=" + id)
                    //.query("couponId=" + id)
                    .build());
            //微信小程序短链接
            couponPackage.setMiniSortUrl(this.getSortUrl(longUrl));
            //H5短链接
            couponPackage.setH5SortUrl(this.getSortUrl(h5Host + id));
        }catch (Exception e){
            throw exception(WECHAT_TOKEN_ERROR);
        }
*/
        boolean save = couponPackageMapper.insert(couponPackage) > 0;

        activityChannelService.createChannelDO(couponPackage.getId(), ActivityChannelTypeEnum.COUPON_PACKAGE.getCode());


        for (GoodCouponPackageSaveReqVO goodCouponPackage : goodCouponPackages) {
            Integer num = goodCouponPackage.getNum();
            if (num > 5) {
                throw exception(COUPON_PACKAGE_SINGLE_NUM_LIMIT);
            }
            goodCouponPackage.setPackageId(id);
            goodCouponPackage.setDeleted(Boolean.FALSE);
        }
        List<GoodCouponPackageDO> bean = BeanUtils.toBean(goodCouponPackages, GoodCouponPackageDO.class);
        goodCouponPackageMapper.insertBatchSomeColumn(bean);
        LogRecordContext.putVariable("packageName", createReqVO.getPackageName());
        CouponPackageShareSaveReqVO shareSaveReqVO = new CouponPackageShareSaveReqVO();
        shareSaveReqVO.setPackageId(id);
        shareSaveReqVO.setShareTitle(createReqVO.getPackageName());
        couponPackageShareService.insert(shareSaveReqVO);
        return save;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = PROMOTION_GOOD_COUPON_PACKAGE_TYPE, subType = PROMOTION_GOOD_COUPON_PACKAGE_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}", success = PROMOTION_GOOD_COUPON_PACKAGE_UPDATE_SUCCESS)
    public Boolean update(CouponPackageSaveReqVO updateReqVO) {
        Integer userRestrictions = updateReqVO.getUserRestrictions();
        if(Objects.equals(userRestrictions, CouponPackageConstant.TARGET_AUDIENCE_4)){
            QueryWrapper<CouponPackageDO> qw = new QueryWrapper<>();
            qw.lambda().eq(CouponPackageDO::getUserRestrictions,CouponPackageConstant.TARGET_AUDIENCE_4);
            Long l = couponPackageMapper.selectCount(qw);
            if(l > 0){
                throw exception(COUPON_PACKAGE_ONE_MEMBERDAY);
            }
        }
        Long id = updateReqVO.getId();
        CouponPackageDO one = couponPackageMapper.selectById(id);
        Integer totalNum = updateReqVO.getTotalNum();
        Integer couponNum = one.getPackageNum();
        Integer receivedNum = one.getReceivedNum();
        Integer totalNum1 = one.getTotalNum();
//        if (!totalNum.equals(couponNum)) {
//            throw exception(COUPON_PACKAGE_CAN_NOT_UPDATE);
//        }
        if (totalNum < receivedNum) {
            throw exception(COUPON_PACKAGE_QUANTITY_ERROR_ENUM);
        }
        int i = couponNum - totalNum1 + totalNum;
        CouponPackageDO couponPackage = new CouponPackageDO();
        BeanUtil.copyProperties(updateReqVO, couponPackage);
        couponPackage.setPackageNum(i);

        //领取时间判断
        if(Objects.equals(couponPackage.getClaimTimeLimit(), GoodCouponConstants.CLAIM_TIME_LIMIT_1)){
            if(ObjectUtil.isEmpty(couponPackage.getClaimTimeSlot()) &&
                    ObjectUtil.isEmpty(couponPackage.getClaimDayNo()) &&
                    ObjectUtil.isEmpty(couponPackage.getClaimTime()) &&
                    ObjectUtil.isEmpty(couponPackage.getClaimWeekNo())
            ){
                throw exception(COUPON_PACKAGE_CLAIM_TIME_NULL);
            }
        }


        List<GoodCouponPackageSaveReqVO> goodCouponPackages = updateReqVO.getGoodCouponPackages();
        if (CollectionUtil.isEmpty(goodCouponPackages)) {
            throw exception(COUPON_PACKAGE_NUM_NULL);
        } else {
            if (goodCouponPackages.size() > GoodCouponPackageConstants.GOOD_COUPON_PACKAGE_SIZE) {
                throw exception(COUPON_PACKAGE_NUM_SIZE);
            }
            for (GoodCouponPackageSaveReqVO goodCouponPackage : goodCouponPackages) {
                Integer num = goodCouponPackage.getNum();
                if (num > 5) {
                    throw exception(COUPON_PACKAGE_SINGLE_NUM_LIMIT);
                }
                goodCouponPackage.setDeleted(Boolean.FALSE);
                goodCouponPackage.setPackageId(id);
            }

            Map<Long, Integer> collect = goodCouponPackages.stream().collect(Collectors.toMap(GoodCouponPackageSaveReqVO::getCouponId, GoodCouponPackageSaveReqVO::getNum));
            List<Long> couponIds = goodCouponPackages.stream().map(GoodCouponPackageSaveReqVO::getCouponId).toList();
            List<GoodCouponDO> goodCoupons = goodCouponService.getByIds(couponIds);
            List<String> couponNames = new ArrayList<>();
            for (GoodCouponDO goodCoupon : goodCoupons) {
                if (goodCoupon.getCouponNum() < collect.get(goodCoupon.getId()) * couponPackage.getPackageNum()) {
                    couponNames.add(goodCoupon.getCouponName());
                }
            }
            if (CollectionUtil.isNotEmpty(couponNames)) {
                throw exception(COUPON_PACKAGE_NUM_ERROR, String.format("优惠券--%s数量不足,请修改优惠券数量!", String.join(",", couponNames)));
            }
        }
        //couponPackage.setPackageNum(couponPackage.getTotalNum());

        boolean update = couponPackageMapper.updateById(couponPackage) > 0;

        QueryWrapper<GoodCouponPackageDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(GoodCouponPackageDO::getPackageId, id);
        goodCouponPackageMapper.delete(queryWrapper);
        List<GoodCouponPackageDO> bean = BeanUtils.toBean(goodCouponPackages, GoodCouponPackageDO.class);
        goodCouponPackageMapper.insertBatch(bean);
        LogRecordContext.putVariable("updateReqVO", updateReqVO);
        CouponPackageShareSaveReqVO shareSaveReqVO = new CouponPackageShareSaveReqVO();
        shareSaveReqVO.setPackageId(id);
        shareSaveReqVO.setShareTitle(updateReqVO.getPackageName());
        couponPackageShareService.updateTitle(shareSaveReqVO);
        activityJDService.updateCouponPackage(id);
        return update;
    }

    @Override
    public Boolean claimCouponPackage(ClaimCouponPackageReqVO claimCouponPackageReqVO) {
        // 限流
        boolean acquire = couponPackageLimiter.tryAcquire();
        if (!acquire) {
            throw exception(CLAIM_COUPON_LIMITER);
        }

        Long packageId = claimCouponPackageReqVO.getPackageId();
        Long userId = claimCouponPackageReqVO.getMemberId();
        CouponPackageDO couponPackage = couponPackageMapper.selectById(packageId);
        // 查询用户信息
        CommonResult<WxMemberDTO> result = wxMemberApi.getMemberById(userId);
        WxMemberDTO wxMember = result.getData();
        if (ObjectUtil.isEmpty(wxMember)) {
            throw exception(COUPON_REG_ERROR);
        }
        String memberMobile = wxMember.getMemberMobile();
        if (ObjectUtil.isEmpty(memberMobile)) {
            throw exception(COUPON_REG_ERROR);
        }

        // 查询优惠券包
        QueryWrapper<GoodCouponPackageDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(GoodCouponPackageDO::getPackageId, packageId);
        List<GoodCouponPackageDO> goodCouponPackages = goodCouponPackageMapper.selectList(queryWrapper);
        List<Long> couponIds = goodCouponPackages.stream().map(GoodCouponPackageDO::getCouponId).toList();

        List<GoodCouponDO> list = goodCouponService.getByIds(couponIds);
        Map<Long, Integer> couponNumMap = goodCouponPackages.stream().collect(Collectors.toMap(GoodCouponPackageDO::getCouponId, GoodCouponPackageDO::getNum));
        List<UserCouponDO> userCoupons = new ArrayList<>();
        claimPackageService.claimCouponPackage(couponPackage, wxMember);
        try {
            // 领取优惠券包
            claimCouponPackageExtracted(list, couponNumMap, wxMember, memberMobile, packageId, userCoupons, claimCouponPackageReqVO);
            LambdaUpdateWrapper<CouponPackageDO> couponPackageUpdateWrapper = Wrappers.lambdaUpdate(CouponPackageDO.class)
                    .eq(CouponPackageDO::getId, packageId);
            couponPackageUpdateWrapper.setSql("received_num = received_num + 1");
            couponPackageUpdateWrapper.setSql("package_num = package_num - 1");
            this.updateNum(couponPackageUpdateWrapper);
            // 插入用户优惠券
            UserCouponPackageDO userCouponPackage = new UserCouponPackageDO();
            userCouponPackage.setUserId(wxMember.getMemberId());
            userCouponPackage.setPackageId(packageId);
            userCouponPackage.setBusinessId(couponPackage.getBusinessId());
            userCouponPackage.setPackageSource(UserCouponConstants.COUPON_SOURCE_0);
            userCouponPackage.setMemberMobile(memberMobile);
            userCouponPackage.setMemberNickName(wxMember.getMemberNickName());
            return saveUserCoupon(userCoupons, userCouponPackage);
        } catch (Exception e) {
            log.warn("优惠券包： {}当前领取人数过多，请稍后重试！", packageId);
            throw exception(COUPON_CLAIM_ERROR);
        }
    }

    @Override
    public Boolean claimCouponPackage(WxMemberDTO wxMember,Long packageId,Integer source,Integer marketId, Long storeId) {
        CouponPackageDO couponPackage = couponPackageMapper.selectById(packageId);
        Integer isGround = couponPackage.getIsGround();
        if (isGround == 0) {
            throw exception(COUPON_PACKAGE_NOT_ON_SHELF);
        }
        // 查询优惠券包
        QueryWrapper<GoodCouponPackageDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(GoodCouponPackageDO::getPackageId, packageId);
        List<GoodCouponPackageDO> goodCouponPackages = goodCouponPackageMapper.selectList(queryWrapper);
        List<Long> couponIds = goodCouponPackages.stream().map(GoodCouponPackageDO::getCouponId).toList();

        List<GoodCouponDO> list = goodCouponService.getByIds(couponIds);
        Map<Long, Integer> couponNumMap = goodCouponPackages.stream().collect(Collectors.toMap(GoodCouponPackageDO::getCouponId, GoodCouponPackageDO::getNum));

        List<UserCouponDO> userCoupons = new ArrayList<>();
        for (GoodCouponDO goodCoupon : list) {
            Long goodCouponId = goodCoupon.getId();
            Integer num = couponNumMap.get(goodCoupon.getId());
            UserCouponDO userCoupon;
            for (int i = 0; i < num; i++) {
                userCoupon = new UserCouponDO();
                BeanUtil.copyProperties(goodCoupon, userCoupon);
                userCoupon.setUserId(wxMember.getMemberId());
                userCoupon.setCouponId(goodCouponId);
                userCoupon.setCouponUseTime(goodCoupon.getUseTime());
                userCoupon.setUseTime(null);
                userCoupon.setCouponCreateTime(LocalDateTime.now());
                userCoupon.setCreateTime(goodCoupon.getCreateTime());
                userCoupon.setId(null);
                userCoupon.setIsUsed(0);
                CouponTimeUtil.parseCouponTime(goodCoupon, userCoupon);
                userCoupon.setDistributionMethod(0L);
                userCoupon.setMemberMobile(wxMember.getMemberMobile());
                userCoupon.setCouponSource(source);
                userCoupon.setPackageId(packageId);
                userCoupon.setDeleted(false);
                userCoupon.setStoreId(storeId);
                userCoupon.setCreateTime(LocalDateTime.now());
                userCoupon.setCouponCreateTime(LocalDateTime.now());
                userCoupons.add(userCoupon);
            }
        }
        UserCouponPackageDO userCouponPackage = new UserCouponPackageDO();
        userCouponPackage.setUserId(wxMember.getMemberId());
        userCouponPackage.setPackageId(packageId);
        userCouponPackage.setBusinessId(couponPackage.getBusinessId());
        userCouponPackage.setPackageSource(source);
        userCouponPackage.setMemberMobile(wxMember.getMemberMobile());
        userCouponPackage.setMemberNickName(wxMember.getMemberNickName());
        return saveUserCoupon(userCoupons, userCouponPackage);
    }

    private void updateNum(LambdaUpdateWrapper<CouponPackageDO> couponPackageUpdateWrapper) {
        couponPackageMapper.update(null, couponPackageUpdateWrapper);
    }

    @Override
    public Boolean claimZZCouponPackage(ClaimCouponPackageReqVO claimCouponPackageReqVO) {

        // 限流
        boolean acquire = couponPackageLimiter.tryAcquire();
        if (!acquire) {
            throw exception(CLAIM_COUPON_LIMITER);
        }


        Long packageId = claimCouponPackageReqVO.getPackageId();
        Long userId = claimCouponPackageReqVO.getMemberId();
        CouponPackageDO couponPackage = couponPackageMapper.selectById(packageId);
        // 查询用户信息
        CommonResult<WxMemberDTO> result = wxMemberApi.getMemberById(userId);
        WxMemberDTO wxMember = result.getData();
        if (ObjectUtil.isEmpty(wxMember)) {
            throw exception(COUPON_REG_ERROR);
        }
        String memberMobile = wxMember.getMemberMobile();
        if (ObjectUtil.isEmpty(memberMobile)) {
            throw exception(COUPON_REG_ERROR);
        }
        // 优惠券包id
        if (ObjectUtil.isEmpty(packageId)) {
            packageId = Long.valueOf(this.zzPackageId);
        }
        Date lastSecondOfLastWeek = DateUtils.getLastSecondOfLastWeek();
        // 判断是否领取过
        QueryWrapper<UserCouponDO> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.lambda().eq(UserCouponDO::getUserId, userId);
        queryWrapper1.lambda().eq(UserCouponDO::getPackageId, packageId);
        queryWrapper1.lambda().ge(UserCouponDO::getVaildStartTime, lastSecondOfLastWeek);
        if (userCouponMapper.selectCount(queryWrapper1) > 0) {
            throw exception(COUPON_PACKAGE_OVER_LIMIT_WEEK);
        }


        // 查询优惠券包
        QueryWrapper<GoodCouponPackageDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(GoodCouponPackageDO::getPackageId, packageId);
        List<GoodCouponPackageDO> goodCouponPackages = goodCouponPackageMapper.selectList(queryWrapper);
        List<Long> couponIds = goodCouponPackages.stream().map(GoodCouponPackageDO::getCouponId).toList();

        List<GoodCouponDO> list = goodCouponService.getByIds(couponIds);
        Map<Long, Integer> couponNumMap = goodCouponPackages.stream().collect(Collectors.toMap(GoodCouponPackageDO::getCouponId, GoodCouponPackageDO::getNum));
        List<UserCouponDO> userCoupons = new ArrayList<>();
        try {
            // 领取优惠券包
            claimZZCouponPackageExtracted(list, couponNumMap, wxMember, memberMobile, packageId, userCoupons);
            // 更新优惠券包数量
            LambdaUpdateWrapper<CouponPackageDO> couponPackageUpdateWrapper = Wrappers.lambdaUpdate(CouponPackageDO.class)
                    .eq(CouponPackageDO::getId, packageId);
            couponPackageUpdateWrapper.setSql("received_num = received_num + 1");
            couponPackageUpdateWrapper.setSql("package_num = package_num - 1");
            this.updateNum(couponPackageUpdateWrapper);

            // 插入用户优惠券
            UserCouponPackageDO userCouponPackage = new UserCouponPackageDO();
            userCouponPackage.setUserId(wxMember.getMemberId());
            userCouponPackage.setPackageId(packageId);
            userCouponPackage.setBusinessId(couponPackage.getBusinessId());
            userCouponPackage.setPackageSource(UserCouponConstants.COUPON_SOURCE_0);
            userCouponPackage.setMemberMobile(memberMobile);
            userCouponPackage.setMemberNickName(wxMember.getMemberNickName());
            return saveUserCoupon(userCoupons, userCouponPackage);
        } catch (Exception e) {
            log.warn("优惠券包： {}当前领取人数过多，请稍后重试！", packageId);
            throw exception(COUPON_CLAIM_ERROR);
        }
    }

    @Override
    public Boolean claimSmsCouponPackage(ClaimCouponPackageReqVO claimCouponPackageReqVO) {
        // 限流
        boolean acquire = couponPackageLimiter.tryAcquire();
        if (!acquire) {
            throw exception(CLAIM_COUPON_LIMITER);
        }
        Long userId = claimCouponPackageReqVO.getMemberId();
        Boolean exists = redisCache.setIsMember(MARKET_USER_KEY + claimCouponPackageReqVO.getMarketId(), userId);
        if (!exists){
            throw exception(COUPON_PACKAGE_USER_NOT_EXISTS);
        }

        Long packageId = claimCouponPackageReqVO.getPackageId();
        CouponPackageDO couponPackage = couponPackageMapper.selectById(packageId);
        // 查询用户信息
        CommonResult<WxMemberDTO> result = wxMemberApi.getMemberById(userId);
        WxMemberDTO wxMember = result.getData();
        if (ObjectUtil.isEmpty(wxMember)) {
            throw exception(COUPON_REG_ERROR);
        }
        String memberMobile = wxMember.getMemberMobile();
        if (ObjectUtil.isEmpty(memberMobile)) {
            throw exception(COUPON_REG_ERROR);
        }
        // 优惠券包id
        if (ObjectUtil.isEmpty(packageId)) {
            throw exception(COUPON_PACKAGE_EXPIRED);
        }

        // 判断是否领取过
        if (pvStatisticsService.isClaimCouponPackage(packageId, userId)) {
            throw exception(COUPON_PACKAGE_OVER_LIMIT);
        }


        // 查询优惠券包
        QueryWrapper<GoodCouponPackageDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(GoodCouponPackageDO::getPackageId, packageId);
        List<GoodCouponPackageDO> goodCouponPackages = goodCouponPackageMapper.selectList(queryWrapper);
        List<Long> couponIds = goodCouponPackages.stream().map(GoodCouponPackageDO::getCouponId).toList();

        List<GoodCouponDO> list = goodCouponService.getByIds(couponIds);
        Map<Long, Integer> couponNumMap = goodCouponPackages.stream().collect(Collectors.toMap(GoodCouponPackageDO::getCouponId, GoodCouponPackageDO::getNum));

        List<UserCouponDO> userCoupons = new ArrayList<>();
        try {
            claimCouponPackageExtracted(list, couponNumMap, wxMember, memberMobile, packageId, userCoupons, claimCouponPackageReqVO);
            pvStatisticsService.claimCouponPackage(packageId, userId);

            UserCouponPackageDO userCouponPackage = new UserCouponPackageDO();
            userCouponPackage.setUserId(wxMember.getMemberId());
            userCouponPackage.setPackageId(packageId);
            userCouponPackage.setBusinessId(couponPackage.getBusinessId());
            userCouponPackage.setPackageSource(UserCouponConstants.COUPON_SOURCE_0);
            userCouponPackage.setMemberMobile(memberMobile);
            userCouponPackage.setMemberNickName(wxMember.getMemberNickName());
            return saveUserCoupon(userCoupons, userCouponPackage);
        } catch (Exception e) {
            throw exception(COUPON_CLAIM_ERROR);
        }
    }

    /**
     * 领取优惠券包
     * @param list List<GoodCouponDO>
     * @param couponNumMap couponNumMap
     * @param wxMember wxMember
     * @param memberMobile memberMobile
     * @param packageId packageId
     * @param userCoupons userCoupons
     */
    private void claimCouponPackageExtracted(List<GoodCouponDO> list, Map<Long, Integer> couponNumMap, WxMemberDTO wxMember, String memberMobile, Long packageId, List<UserCouponDO> userCoupons,ClaimCouponPackageReqVO claimCouponPackageReqVO) {
        UserCouponDO userCoupon;
        for (GoodCouponDO goodCoupon : list) {
            Long goodCouponId = goodCoupon.getId();
            Integer num = couponNumMap.get(goodCoupon.getId());
            int couponNum = goodCoupon.getCouponNum();
            if (couponNum < num) {
                goodCouponService.updateIsGround(goodCouponId);
            }
            for (int i = 0; i < num; i++) {
                userCoupon = new UserCouponDO();
                BeanUtil.copyProperties(goodCoupon, userCoupon);
                userCoupon.setUserId(wxMember.getMemberId());
                userCoupon.setCouponId(goodCouponId);
                userCoupon.setCouponUseTime(goodCoupon.getUseTime());
                userCoupon.setUseTime(null);
                userCoupon.setCouponCreateTime(LocalDateTime.now());
                userCoupon.setCreateTime(goodCoupon.getCreateTime());
                userCoupon.setId(null);
                userCoupon.setIsUsed(0);
                CouponTimeUtil.parseCouponTime(goodCoupon, userCoupon);
                userCoupon.setDistributionMethod(0L);
                userCoupon.setMemberMobile(memberMobile);
                userCoupon.setCouponSource(UserCouponConstants.COUPON_SOURCE_0);
                userCoupon.setPackageId(packageId);
                userCoupon.setDeleted(false);
                userCoupon.setStoreId(claimCouponPackageReqVO.getStoreId());
                userCoupons.add(userCoupon);
            }
            UpdateWrapper<GoodCouponDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().eq(GoodCouponDO::getId, goodCouponId);
            log.info("claimCouponPackage接口,{}优惠券领取数量+{}", goodCoupon.getCouponName(), num);
            updateWrapper.setSql("received_num = received_num +" + num);
            updateWrapper.setSql("coupon_num = coupon_num -" + num);
            goodCouponService.updateByWrapper(updateWrapper);
        }
    }

    /**
     * 领取周周优惠券包
     * @param list List<GoodCouponDO>
     * @param couponNumMap couponNumMap
     * @param wxMember wxMember
     * @param memberMobile memberMobile
     * @param packageId packageId
     * @param userCoupons userCoupons
     */
    private void claimZZCouponPackageExtracted(List<GoodCouponDO> list,Map<Long, Integer> couponNumMap,WxMemberDTO wxMember,String memberMobile,Long packageId,List<UserCouponDO> userCoupons){
        UserCouponDO userCoupon;
        Date lastSecondOfWeek = DateUtils.getLastSecondOfWeek();
        for (GoodCouponDO goodCoupon : list) {
            Long goodCouponId = goodCoupon.getId();
            Integer num = couponNumMap.get(goodCoupon.getId());
            int couponNum = goodCoupon.getCouponNum();
            if (couponNum < num) {
                goodCouponService.updateIsGround(goodCouponId);
            }
            for (int i = 0; i < num; i++) {
                userCoupon = new UserCouponDO();
                BeanUtil.copyProperties(goodCoupon, userCoupon);
                userCoupon.setUserId(wxMember.getMemberId());
                userCoupon.setCouponId(goodCouponId);
                userCoupon.setCouponUseTime(goodCoupon.getUseTime());
                userCoupon.setUseTime(null);
                userCoupon.setCouponCreateTime(LocalDateTime.now());
                userCoupon.setCreateTime(goodCoupon.getCreateTime());
                userCoupon.setId(null);
                userCoupon.setIsUsed(0);
                CouponTimeUtil.parseCouponTime(goodCoupon, userCoupon);
                userCoupon.setDistributionMethod(0L);
                userCoupon.setMemberMobile(memberMobile);
                userCoupon.setCouponSource(UserCouponConstants.COUPON_SOURCE_0);
                userCoupon.setPackageId(packageId);
                userCoupon.setExpirationTime(lastSecondOfWeek);
                userCoupon.setDeleted(false);
                userCoupons.add(userCoupon);
            }
            UpdateWrapper<GoodCouponDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().eq(GoodCouponDO::getId, goodCouponId);
            log.info("claimCouponPackage接口,{}优惠券领取数量+{}", goodCoupon.getCouponName(), num);
            updateWrapper.setSql("received_num = received_num +" + num);
            updateWrapper.setSql("coupon_num = coupon_num -" + num);
            goodCouponService.updateByWrapper(updateWrapper);
        }
    }

    public boolean saveUserCoupon(List<UserCouponDO> userCoupons, UserCouponPackageDO userCouponPackage) {
        userCouponPackageService.insert(userCouponPackage);
        return userCouponShardService.insertBatch(userCoupons);
    }

    /**
     * 根据会员id生成分片值
     *
     * @param memberId memberId
     * @return Integer
     */
    private Integer makeShardingValueForWxById(Long memberId) {
        return Integer.parseInt(String.valueOf(memberId % 10));
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
    public PageResult<CouponPackageRespVO> getPageCouponPackage(GetCouponPackagePageReqVO pageReqVO) {

        List<Long> packageIds = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(pageReqVO.getCouponId())){

           List<GoodCouponPackageDO> goodCouponPackageDOS =  goodCouponPackageService.selectListByCouponId(pageReqVO.getCouponId());
           if (ObjectUtil.isNotEmpty(goodCouponPackageDOS)){
               packageIds.addAll(goodCouponPackageDOS.stream().map(GoodCouponPackageDO::getPackageId).toList());
           }
        }


        PageResult<CouponPackageRespVO> pageResult = new PageResult<>();
        LambdaQueryWrapper<CouponPackageDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(CouponPackageDO::getCreateTime);
        if (ObjectUtil.isNotEmpty(packageIds)) {
            queryWrapper.in(CouponPackageDO::getId, packageIds);
        }


        if (ObjectUtil.isNotEmpty(pageReqVO.getRemark())) {
            queryWrapper.like(CouponPackageDO::getRemark, pageReqVO.getRemark()).or().like(CouponPackageDO::getPackageName, pageReqVO.getRemark());
        }
        if (ObjectUtil.isNotEmpty(pageReqVO.getIsGround())){
            queryWrapper.eq(CouponPackageDO::getIsGround,pageReqVO.getIsGround());
        }

        if (ObjectUtil.isNotEmpty(pageReqVO.getUserRestrictions())){
            queryWrapper.eq(CouponPackageDO::getUserRestrictions,pageReqVO.getUserRestrictions());
        }

        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageReqVO.getPageNo());
        pageParam.setPageSize(pageReqVO.getPageSize());
        PageResult<CouponPackageDO> couponPackageDOPageResult = couponPackageMapper.selectPage(pageParam, queryWrapper);



        pageResult.setTotal(couponPackageDOPageResult.getTotal());
        List<CouponPackageRespVO> couponPackageRespVOList = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(couponPackageDOPageResult.getList())) {

            List<CouponPackageDO>  list = couponPackageDOPageResult.getList();
            List<Long> ids = list.stream().map(CouponPackageDO::getId).toList();
            LambdaQueryWrapper<GoodCouponPackageDO> goodCouponPackageQueryWrapper = new LambdaQueryWrapper<>();
            goodCouponPackageQueryWrapper.in(GoodCouponPackageDO::getPackageId,ids);
            List<GoodCouponPackageDO> goodCouponPackageDOS = goodCouponPackageMapper.selectList(goodCouponPackageQueryWrapper);
            List<Long> couponIds = goodCouponPackageDOS.stream().map(GoodCouponPackageDO::getCouponId).toList();


           List<GoodCouponDO> goodCouponDOList =  goodCouponService.getListByCouponIds(couponIds);


            List<GoodCouponRespVO> goodCouponRespVOS = BeanUtils.toBean(goodCouponDOList, GoodCouponRespVO.class);


            Date  date = new Date();
            Map<Long, GoodCouponRespVO> goodCouponRespVOMap = goodCouponRespVOS.stream().collect(Collectors.toMap(GoodCouponRespVO::getId, c -> c));
            List<GoodCouponPackageRespVO> goodCouponPackageRespVOS = new ArrayList<>();
            for (GoodCouponPackageDO goodCouponPackageDO : goodCouponPackageDOS) {
                GoodCouponPackageRespVO goodCouponPackageRespVO = new GoodCouponPackageRespVO();
                BeanUtils.copyProperties(goodCouponPackageDO, goodCouponPackageRespVO);
                goodCouponPackageRespVO.setGoodCouponRespVO(goodCouponRespVOMap.get(goodCouponPackageDO.getCouponId()));
                goodCouponPackageRespVOS.add(goodCouponPackageRespVO);
            }
            Map<Long, List<GoodCouponPackageRespVO>> goodCouponPackageMap = goodCouponPackageRespVOS.stream().collect(Collectors.groupingBy(GoodCouponPackageRespVO::getPackageId));
//            log.info("goodCouponPackageMap:{}", goodCouponPackageMap);

            for (CouponPackageDO couponPackageDO : list) {
                CouponPackageRespVO couponPackageRespVO = new CouponPackageRespVO();
                BeanUtils.copyProperties(couponPackageDO,couponPackageRespVO);
                List<GoodCouponPackageRespVO> goodCouponPackageRespVOS1 = goodCouponPackageMap.get(couponPackageDO.getId());
                if (ObjectUtil.isNotEmpty(goodCouponPackageRespVOS1)) {
                    couponPackageRespVO.setGoodCouponPackageRespVOS(goodCouponPackageRespVOS1);
                    for (GoodCouponPackageRespVO goodCouponPackageRespVO : goodCouponPackageRespVOS1) {
                        Integer num = goodCouponPackageRespVO.getNum();
                        GoodCouponRespVO goodCouponRespVO = goodCouponPackageRespVO.getGoodCouponRespVO();
                        if (ObjectUtil.isNotEmpty(goodCouponRespVO)) {
                            if (goodCouponRespVO.getUseType()==0){
                                Date couponEndTime = goodCouponRespVO.getCouponEndTime();
                                goodCouponRespVO.setOutOfDate(date.compareTo(couponEndTime)>0);
                            }
                            goodCouponRespVO.setIsSurplus(goodCouponRespVO.getCouponNum()<num);
                        }
                        goodCouponPackageRespVO.setGoodCouponRespVO(goodCouponRespVOMap.get(goodCouponPackageRespVO.getCouponId()));
                    }
                }
                couponPackageRespVOList.add(couponPackageRespVO);

            }


        }
        pageResult.setList(couponPackageRespVOList);

        return pageResult;
    }

    @Override
    public CouponPackageRespVO selectById(Long id) {

        CouponPackageDO couponPackageDO = couponPackageMapper.selectById(id);
        if (ObjectUtil.isEmpty(couponPackageDO)) {
            throw exception(COUPON_PACKAGE_NOT_EXISTS);
        }
        CouponPackageRespVO couponPackageRespVO = new CouponPackageRespVO();
        BeanUtils.copyProperties(couponPackageDO, couponPackageRespVO);
        List<GoodCouponPackageDO> goodCouponPackageDOS = goodCouponPackageService.selectListByPackageId(couponPackageDO.getId());
        List<GoodCouponPackageRespVO> goodCouponPackages = BeanUtils.toBean(goodCouponPackageDOS, GoodCouponPackageRespVO.class);

        if (ObjectUtil.isNotEmpty(goodCouponPackages)) {
            List<Long> couponIds = goodCouponPackages.stream().map(GoodCouponPackageRespVO::getCouponId).toList();

           List<GoodCouponDO> goodCouponDOList =  goodCouponService.getListByCouponIds(couponIds);

            List<GoodCouponRespVO> goodCoupons = BeanUtils.toBean(goodCouponDOList, GoodCouponRespVO.class);
            Map<Long, GoodCouponRespVO> collect = goodCoupons.stream().collect(Collectors.toMap(GoodCouponRespVO::getId, c -> c));
            for (GoodCouponPackageRespVO goodCouponPackage : goodCouponPackages) {
                goodCouponPackage.setGoodCouponRespVO(collect.get(goodCouponPackage.getCouponId()));
            }
            couponPackageRespVO.setGoodCouponPackageRespVOS(goodCouponPackages);
        }
        return couponPackageRespVO;
    }

    @Override
    public List<String> getCommunityQrImage(Long couponId) {
        if(ObjectUtil.isEmpty(couponId)){
            return List.of();
        }
        CouponPackageDO couponPackage = couponPackageMapper.selectById(couponId);
        if (ObjectUtil.isNotEmpty(couponPackage)) {
            return ImageValueParseUtil.parseImageValues(couponPackage.getCommunityQrImage());
        }
        return List.of();
    }

    @Override
    public Map<Long, String> getPackageNameMap(List<Long> packageIds) {
        if (CollectionUtil.isEmpty(packageIds)) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<CouponPackageDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CouponPackageDO::getId, packageIds);
        List<CouponPackageDO> couponPackages = couponPackageMapper.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(couponPackages)) {
            return Collections.emptyMap();
        }
        return couponPackages.stream().collect(Collectors.toMap(
                CouponPackageDO::getId,
                CouponPackageDO::getPackageName,
                (v1, v2) -> v1
        ));
    }

    @Override
    @LogRecord(type = PROMOTION_GOOD_COUPON_PACKAGE_TYPE, subType = PROMOTION_GOOD_COUPON_PACKAGE_DELETE_SUB_TYPE, bizNo = "{{#id}}", success = PROMOTION_GOOD_COUPON_PACKAGE_DELETE_SUCCESS)
    public void removeById(Long id) {
//        CouponPackageDO couponPackageDO = couponPackageMapper.selectById(id);
//        if(!Objects.equals(couponPackageDO.getTotalNum(),couponPackageDO.getPackageNum())){
//            throw exception(PACKAGE_CLAIMED_CAN_NOT_DELETE);
//        }
//        if(Objects.equals(couponPackageDO.getIsGround(),GoodCouponPackageConstants.IS_GROUND_1)){
//            throw exception(COUPON_PACKAGE_GROUND_ERROR_ENUM);
//        }
        LogRecordContext.putVariable("id", id);
        couponPackageMapper.deleteById(id);
    }

    @Override
    public void editPackageNum(CouponPackageUpdateNumVO couponPackageAO) {
        Long id = couponPackageAO.getId();
        Integer num = couponPackageAO.getPackageNum();

        CouponPackageDO couponPackageDO = couponPackageMapper.selectById(id);
        Integer totalNum = couponPackageDO.getTotalNum();
        Integer couponNum = couponPackageDO.getPackageNum();
        Integer receivedNum = couponPackageDO.getReceivedNum();

        if (num < receivedNum) {
            throw exception(COUPON_PACKAGE_QUANTITY_ERROR_ENUM);
        }

        couponPackageDO = new CouponPackageDO();
        couponPackageDO.setId(id);
        couponPackageDO.setTotalNum(num);
        int editNum = couponNum - totalNum + num;
        couponPackageDO.setPackageNum(editNum);
        if (editNum > 0) {
            List<GoodCouponPackageDO> goodCouponPackageDOS = goodCouponPackageService.selectListByPackageId(id);
            Map<Long, Integer> collect = goodCouponPackageDOS.stream().collect(Collectors.toMap(GoodCouponPackageDO::getCouponId, GoodCouponPackageDO::getNum));
            List<Long> couponIds = goodCouponPackageDOS.stream().map(GoodCouponPackageDO::getCouponId).toList();

            List<GoodCouponDO> goodCouponDOList = goodCouponService.getByIds(couponIds);

            List<String> couponNames = new ArrayList<>();

            for (GoodCouponDO goodCouponDO : goodCouponDOList) {
                if (goodCouponDO.getCouponNum() < collect.get(goodCouponDO.getId()) * couponPackageDO.getPackageNum()) {
                    couponNames.add(goodCouponDO.getCouponName());
                }
            }
            if (CollectionUtil.isNotEmpty(couponNames)) {
                throw exception(COUPON_PACKAGE_QUANTITY_LACK_MODIFY_ERROR_ENUM);//优惠卷数量不足，请修改优惠卷数量
            }
        }

        couponPackageMapper.updateById(couponPackageDO);

    }

    @Override
    @LogRecord(type = PROMOTION_GOOD_COUPON_PACKAGE_TYPE, subType = PROMOTION_GOOD_COUPON_PACKAGE_GROUND_SUB_TYPE, bizNo = "{{#ground}}", success = PROMOTION_GOOD_COUPON_PACKAGE_GROUND_SUCCESS)
    public void updateIsGround(CouponPackageUpdateGroundVO couponPackageUpdateGroundVO) {
        Long id = couponPackageUpdateGroundVO.getId();
        CouponPackageDO couponPackageDO = couponPackageMapper.selectById(id);
        CouponPackageDO couponPackageDO2 = new CouponPackageDO();

        Integer isGround = couponPackageDO.getIsGround();
        if (isGround == IsGroundConstant.IS_GROUND_0) {

            List<GoodCouponPackageDO> goodCouponPackages = goodCouponPackageService.selectListByPackageId(id);
            if (CollectionUtil.isEmpty(goodCouponPackages)) {
                throw exception(COUPON_PACKAGE_EMPTY_CANNOT_ENABLE_ERROR_ENUM);
            }
            int sum = goodCouponPackages.stream()
                    .mapToInt(GoodCouponPackageDO::getNum) // 提取 price 字段
                    .sum();
            if (sum * goodCouponPackages.size() < 2) {
                throw exception(COUPON_PACKAGE_EMPTY_CANNOT_ENABLE_ERROR_ENUM);
            }

            Map<Long, Integer> collect = goodCouponPackages.stream().collect(Collectors.toMap(GoodCouponPackageDO::getCouponId, GoodCouponPackageDO::getNum));
            List<Long> couponIds = goodCouponPackages.stream().map(GoodCouponPackageDO::getCouponId).toList();
            List<GoodCouponDO> goodCoupons = goodCouponService.getByIds(couponIds);

            List<String> couponNames = new ArrayList<>();
            List<String> outOfDate = new ArrayList<>();
            for (GoodCouponDO goodCoupon : goodCoupons) {
                if (goodCoupon.getCouponNum() < collect.get(goodCoupon.getId()) * couponPackageDO.getPackageNum()) {
                    couponNames.add(goodCoupon.getCouponName());
                }
                if (ObjectUtil.equals(goodCoupon.getUseType(), 0)) {
                    Date couponEndTime = goodCoupon.getCouponEndTime();
                    if (DateUtil.compare(new Date(), couponEndTime) > 0) {
                        outOfDate.add(goodCoupon.getCouponName());
                    }
                }
            }

            if (CollectionUtil.isNotEmpty(couponNames)) {
                throw exception(COUPON_PACKAGE_QUANTITY_LACK_MODIFY_ERROR_ENUM);
            }
            if (CollectionUtil.isNotEmpty(outOfDate)) {
                throw exception(COUPON_PACKAGE_COUPONS_EXPIRED_ERROR_ENUM);
            }
            couponPackageDO2 = new CouponPackageDO();
            couponPackageDO2.setId(id);
            couponPackageDO2.setIsGround(IsGroundConstant.IS_GROUND_1);
            LogRecordContext.putVariable("ground", "上架了"+couponPackageDO.getPackageName());
        }else {
            couponPackageDO2 = new CouponPackageDO();
            couponPackageDO2.setId(id);
            couponPackageDO2.setIsGround(IsGroundConstant.IS_GROUND_0);
            LogRecordContext.putVariable("ground", "下架了"+couponPackageDO.getPackageName());
        }

        couponPackageMapper.updateById(couponPackageDO2);
        activityJDService.updateCouponPackage(id);
    }

    @Override
    public void updateById(CouponPackageDO couponPackage) {
        couponPackageMapper.updateById(couponPackage);
    }

    @Override
    public void updateIsGroundById(Long packageId, int isGround0) {
        LambdaUpdateWrapper<CouponPackageDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(CouponPackageDO::getIsGround, isGround0);
        updateWrapper.eq(CouponPackageDO::getId, packageId);
        couponPackageMapper.update(updateWrapper);
        activityJDService.updateCouponPackage(packageId);
    }

    @Override
    public void updateReceivedNumAndPackageNumById(Integer num, Long packageId) {
        UpdateWrapper<CouponPackageDO> couponPackageUpdateWrapper = new UpdateWrapper<>();
        couponPackageUpdateWrapper.setSql("received_num = received_num + " + num);
        couponPackageUpdateWrapper.setSql("package_num = package_num - " + num);
        couponPackageUpdateWrapper.lambda().eq(CouponPackageDO::getId,packageId);
        couponPackageMapper.update(couponPackageUpdateWrapper);
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public PageResult<CouponPackageCollectRespVO> couponPackageList(CouponPackageCollectReqVO collectReqVO) {
        LambdaQueryWrapper<UserCouponPackageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCouponPackageDO::getPackageId,collectReqVO.getCouponId());
        if(!StringUtils.isEmpty(collectReqVO.getMemberName())){
            // 原来是name or mobile 现在只要mobile
            wrapper.eq(ObjectUtil.isNotEmpty(collectReqVO.getMemberName()),UserCouponPackageDO::getMemberMobile,collectReqVO.getMemberName());
        }
        wrapper.orderByDesc(UserCouponPackageDO::getCreateTime);
        Page<UserCouponPackageDO> userCouponPackageDOPage = userCouponPackageMapper.selectPage(PageUtils.getPageInfo(), wrapper);
        PageResult<CouponPackageCollectRespVO> pageResult = new PageResult<>();
        List<CouponPackageCollectRespVO> couponPackageCollectRespVOS = new ArrayList<>();
        for (UserCouponPackageDO record : userCouponPackageDOPage.getRecords()) {
            CouponPackageCollectRespVO couponPackageCollectRespVO = new CouponPackageCollectRespVO();
            BeanUtils.copyProperties(record,couponPackageCollectRespVO);
            couponPackageCollectRespVO.setMemberName(record.getMemberNickName());
            couponPackageCollectRespVOS.add(couponPackageCollectRespVO);

        }
        pageResult.setList(couponPackageCollectRespVOS);
        pageResult.setTotal(userCouponPackageDOPage.getTotal());
        return pageResult;
    }

    @Override
    public CouponPackageDO couponPackageInfo(Long id) {
        LambdaQueryWrapper<CouponPackageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponPackageDO::getId,id);
        CouponPackageDO couponPackageDO = couponPackageMapper.selectOne(wrapper);
        return couponPackageDO;
    }

    @Override
    public CouponPackageCountRespVo getCouponPackageCount(Long id) {
        CouponPackageCountRespVo couponPackageCountRespVo= new CouponPackageCountRespVo();
        Integer a = 0;
        LambdaQueryWrapper<CouponPackageDO> wrapper = new LambdaQueryWrapper<CouponPackageDO>();
        wrapper.eq(CouponPackageDO::getId,id);
        List<CouponPackageDO> userCouponDOS = couponPackageMapper.selectList(wrapper);
        for (CouponPackageDO userCouponDO : userCouponDOS) {
            a = a+userCouponDO.getReceivedNum();
        }
        couponPackageCountRespVo.setReceivedNum(a);
        return couponPackageCountRespVo;
    }

    @Override
    public void nocCouponStoreNum() {
        QueryWrapper<CouponPackageDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().ne(CouponPackageDO::getStoreLimitNum, GoodCouponConstants.STORE_LIMIT_0);
        List<CouponPackageDO> couponPackages = couponPackageMapper.selectList(queryWrapper);
        for (CouponPackageDO couponPackageDO : couponPackages) {
            if(couponPackageDO.getId() == 1921802863810600995L){
                log.info("goodCouponDO.getId() = " + couponPackageDO.getId());
            }
            Map<Long, Integer> allData = CouponPackageCountUtil.getAllData(redisTemplate, couponPackageDO.getId());
            if (ObjectUtil.isNotEmpty(allData)){
                allData.forEach((k,v)->{
                    couponPackageMapper.upsertPackageStoreClaim(couponPackageDO.getId(), k, v);
                });
            }
        }
    }

    @Override
    public long selectCountByIds(List<Long> packageIds) {
        QueryWrapper<CouponPackageDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(CouponPackageDO::getId, packageIds);
        return couponPackageMapper.selectCount(queryWrapper);
    }

    @Override
    @DS(DsNameConstants.MASTER)
    public Boolean sendCoupon(Set<MemberCouponDTO> memberList, Long packageId, Integer num) {
        CouponPackageRespVO couponPackageRespVO = this.selectById(packageId);

        List<UserCouponDO> userCoupons = new ArrayList<>();
        List<UserCouponPackageDO> userCouponPackages = new ArrayList<>();
        if (ObjectUtil.isEmpty(couponPackageRespVO)) {
            throw exception(COUPON_PACKAGE_NOT_EXISTS);
        }
        CouponPackageDO couponPackage = new CouponPackageDO();
        couponPackage.setId(couponPackageRespVO.getId());
        couponPackage.setIsShare(couponPackageRespVO.getIsShare());
        couponPackage.setIsGround(couponPackageRespVO.getIsGround());
        couponPackage.setPackageNum(couponPackageRespVO.getPackageNum());


        //判断优惠券是否上架
        if (couponPackage.getIsGround() != IsGroundConstant.IS_GROUND_1) {
            throw exception(COUPON_PACKAGE_NOT_ON_SHELF);
        }

        List<GoodCouponPackageDO> goodCouponPackages = goodCouponPackageService.selectListByPackageId(packageId);


        List<Long> couponIds = goodCouponPackages.stream().map(goodCouponPackage -> goodCouponPackage.getCouponId()).toList();
        List<GoodCouponDO> list = goodCouponService.getByIds(couponIds);
        Map<Long, Integer> couponNumMap = goodCouponPackages.stream().collect(Collectors.toMap(GoodCouponPackageDO::getCouponId, GoodCouponPackageDO::getNum));

        //人数
        int size = memberList.size();

        Integer packageNum = couponPackage.getPackageNum();
        if (packageNum < size * num) {
            throw exception(COUPON_PACKAGE_NUM_ERROR);
        }
        UserCouponDO userCoupon = new UserCouponDO();
        //UserCouponPackageDO userCouponPackage = new UserCouponPackageDO();
        Map<Long, Integer> goodCouponNumMap = new HashMap<>();
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        String creator = loginUser.getId().toString();
        for (GoodCouponDO goodCoupon : list) {
            Long goodCouponId = goodCoupon.getId();
            //包中优惠券数量
            int packNum = couponNumMap.get(goodCoupon.getId());
            //优惠券数量
            int couponNum = goodCoupon.getCouponNum();
            // 总发放数量
            int totalSendNum = size * packNum * num;
            if (totalSendNum > couponNum) {
                throw exception(COUPON_NO_REST,"优惠券数量不足" + goodCoupon.getCouponName());
            }
            goodCouponNumMap.put(goodCouponId, totalSendNum);
            for (MemberCouponDTO wxMember : memberList) {
                for (int i = 0; i < num * packNum; i++) {
                    userCoupon = new UserCouponDO();
                    BeanUtil.copyProperties(goodCoupon, userCoupon);
                    userCoupon.setUserId(wxMember.getMemberId());
                    userCoupon.setCouponId(goodCoupon.getId());
                    userCoupon.setCouponUseTime(goodCoupon.getUseTime());
                    userCoupon.setUseTime(null);
                    userCoupon.setCouponCreateTime(LocalDateTime.now());
                    userCoupon.setId(null);
                    userCoupon.setIsUsed(0);
                    userCoupon.setMemberName(wxMember.getMemberName());
                    //userCoupon.setCommodityNameStr(goodCoupon.getCouponCommodities().stream().map(CouponCommodityDO::getCommodityName).collect(Collectors.joining(" ")));
                    //userCoupon.setCommodityNameStr(goodCoupon.getCouponCommodities().stream().map(CouponCommodityDO::getCommodityName).collect(Collectors.joining(" ")));
                    CouponTimeUtil.parseCouponTime(goodCoupon, userCoupon);
                    userCoupon.setDistributionMethod(0L);
                    userCoupon.setMemberMobile(wxMember.getMemberMobile());
                    userCoupon.setCouponSource(UserCouponConstants.COUPON_SOURCE_6);
                    userCoupon.setDeleted(Boolean.FALSE);
                    userCoupon.setCreator(creator);
                    userCoupon.setBusinessId(goodCoupon.getBusinessId());
                    userCoupons.add(userCoupon);
                    // 优惠券包关系表
//                    userCouponPackage.setBusinessId(goodCoupon.getBusinessId());
//                    userCouponPackage.setUserId(wxMember.getMemberId());
//                    userCouponPackage.setPackageId(packageId);
//                    userCouponPackage.setPackageSource(CouponSourceConstant.COUPON_SOURCE_1);
//                    userCouponPackage.setMemberMobile(wxMember.getMemberMobile());
//                    userCouponPackage.setMemberNickName(wxMember.getMemberName());
//                    userCouponPackage.setBusinessId(goodCoupon.getBusinessId());
//                    userCouponPackage.setCreator(creator);
//                    userCouponPackages.add(userCouponPackage);
                }
            }
            goodCouponService.updateReceivedNumAndCouponNumById(totalSendNum,goodCouponId);
        }

        Map<Long, List<UserCouponDO>> collect = userCoupons.stream()
                .collect(Collectors.groupingBy(
                        coupon -> (coupon.getUserId() % 10)
                ));
        Map<Long, List<UserCouponPackageDO>> collect2 = userCouponPackages.stream()
                .collect(Collectors.groupingBy(
                        coupon -> (coupon.getUserId() % 10)
                ));
        for (Map.Entry<Long, List<UserCouponDO>> entry : collect.entrySet()) {
            Long shardingValue = entry.getKey();
            List<UserCouponDO> insertList = entry.getValue();
            promotionThreadPool.execute(() -> {userCouponMasterService.insertBatch(shardingValue,insertList);});

        }
//        for(Long shardingValue = 0L; shardingValue < 10L; shardingValue++){
//            List<UserCouponDO> insertList = collect.get(shardingValue);
//            if(CollectionUtil.isNotEmpty(insertList)){
//                Long finalShardingValue = shardingValue;
//                promotionThreadPool.execute(() -> {userCouponMasterService.insertBatch(finalShardingValue,insertList);});
//            }
////            List<UserCouponPackageDO> userCouponPackageDOS = collect2.get(shardingValue);
////            if(CollectionUtil.isNotEmpty(userCouponPackageDOS)){
////                Long finalShardingValue = shardingValue;
////                promotionThreadPool.execute(() -> {couponPackageMasterService.batchInsert(finalShardingValue,userCouponPackageDOS);});
////            }
//        }
        return Boolean.TRUE;
    }


    /**
     * 根据时效判断开始和过期时间 版本2
     *
     * @param goodCoupon
     */
    private void parseCouponTime(GoodCouponDO goodCoupon, UserCouponDO userCoupon) {
        //解析优惠券的开始结束时间
        if (goodCoupon.getUseType() == 0) {
            String[] split = goodCoupon.getUseTime().split("#");
            goodCoupon.setCouponStartTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, split[0] + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_00_00_00));
            goodCoupon.setCouponEndTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, split[1] + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_23_59_59));
            userCoupon.setVaildStartTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, split[0] + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_00_00_00));
            userCoupon.setExpirationTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, split[1] + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_23_59_59));
        }
        //立即生效
        if (goodCoupon.getUseType() == 1) {
            goodCoupon.setCouponStartTime(new Date());
            String endTime = com.htyoudao.youdao.framework.common.util.date.DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(goodCoupon.getUseTime()) - 1), com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponEndTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_23_59_59));
            userCoupon.setVaildStartTime(new Date());
            userCoupon.setExpirationTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_23_59_59));

        }
        //领券后N天生效
        if (goodCoupon.getUseType() == 2) {
            String[] split = goodCoupon.getUseTime().split("#");
            String startTime = com.htyoudao.youdao.framework.common.util.date.DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(split[0])), com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponStartTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_00_00_00));
            userCoupon.setVaildStartTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_00_00_00));
            String endTime = com.htyoudao.youdao.framework.common.util.date.DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(split[0])).plusDays(Integer.valueOf(split[1]) - 1), com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponEndTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_23_59_59));
            userCoupon.setExpirationTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_23_59_59));
        }
    }

    @Override
    public Integer updateAllH5() {
        List<CouponPackageDO> couponPackageDOS = couponPackageMapper.selectList();
        for (CouponPackageDO item : couponPackageDOS) {
            LambdaUpdateWrapper<CouponPackageDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(CouponPackageDO::getId, item.getId());
            wrapper.set(CouponPackageDO::getH5SortUrl, this.getSortUrl(h5Host + item.getId()));
            couponPackageMapper.update(wrapper);
        }
        return 0;
    }
}
