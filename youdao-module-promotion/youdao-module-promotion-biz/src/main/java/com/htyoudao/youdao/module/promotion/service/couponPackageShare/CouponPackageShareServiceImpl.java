package com.htyoudao.youdao.module.promotion.service.couponPackageShare;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.util.date.DateUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;

import com.htyoudao.youdao.framework.excel.core.service.listenner.GenericExcelListener;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.ExceptionUtil.CouponException;
import com.htyoudao.youdao.module.promotion.api.enums.IsGroundConstant;
import com.htyoudao.youdao.module.promotion.constant.CouponSourceConstant;
import com.htyoudao.youdao.module.promotion.controller.admin.couponPackageShare.VO.*;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.CouponPackageRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.MemberExlVo;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponPackageShare.CouponPackageShareDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcouponpackage.GoodCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage.UserCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponPackageShare.CouponPackageShareMapper;
import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageService;
import com.htyoudao.youdao.module.promotion.service.goodcoupon.GoodCouponService;
import com.htyoudao.youdao.module.promotion.service.goodcouponpackage.GoodCouponPackageService;
import com.htyoudao.youdao.module.promotion.service.userCouponPackage.UserCouponPackageService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.UserCouponService;
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.poi.ss.formula.functions.T;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.extension.toolkit.ChainWrappers.updateChain;
import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.EXCEL_IMPORT_FILE_FAILED;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

@Service
@Slf4j
@Lazy
public class CouponPackageShareServiceImpl implements CouponPackageShareService {

    @Resource
    private CouponPackageShareMapper couponPackageShareMapper;

    @Resource
    private CouponPackageService couponPackageService;

    @DubboReference
    private WxMemberApi wxMemberApi;

    @Resource
    private GoodCouponPackageService goodCouponPackageService;

    @Resource
    private GoodCouponService goodCouponService;



    @Resource
    private UserCouponService userCouponService;


    @Value("${coupon.littlePicUrl}")
    private String littlePicUrl;

    @Value("${coupon.largePicUrl}")
    private String largePicUrl;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserCouponPackageService userCouponPackageService;

    private final static String URL = "https://api.weixin.qq.com/wxa/genwxashortlink?access_token=";

    @Resource
    private Validator validator;

    @Override
    public void createCouponPackageShare(CouponPackageSaveReqVO couponPackageSaveReqVO) {
        CouponPackageShareDO couponPackageShareDO = new CouponPackageShareDO();
        BeanUtils.copyProperties(couponPackageSaveReqVO, couponPackageShareDO);
        couponPackageShareMapper.insertOrUpdate(couponPackageShareDO);

    }

    @Override
    public CouponPackageShareRespVO getPackageShareDetail(CouponPackageDetailReqVO couponPackageDetailReqVO) {

        CouponPackageShareRespVO couponPackageShareRespVO = new CouponPackageShareRespVO();

        ObjectMapper objectMapper = new ObjectMapper();
        String pageUrl = couponPackageDetailReqVO.getPageUrl();
        Long id = couponPackageDetailReqVO.getPackageId();

        LambdaQueryWrapper<CouponPackageShareDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponPackageShareDO::getPackageId, id);
        CouponPackageShareDO couponPackageShareDO = couponPackageShareMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNotEmpty(couponPackageShareDO)) {
            BeanUtils.copyProperties(couponPackageShareDO, couponPackageShareRespVO);
        }

/*        String wechatToken = commonService.getWechatToken(true, String.valueOf(projectOwnerShip));
        String accessToken = wechatToken;*/
        String accessToken = "CLOUD_SECRET_REQUIRED";
        String url = URL + accessToken;
        Map<String, Object> params = new HashMap<>();
        params.put("page_url", pageUrl + "?id=" + id);
        params.put("page_title", "0090");
        String json = null;
        try {
            json = objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            log.warn("wechat解析json异常{}", e.getMessage());
            throw exception(COUPON_PACKAGE_SHARE_WECHAT_JSON_PARSING_EXCEPTION_ERROR);
        }
        String post = HttpUtil.post(url, json);
        try {
            Map<String, Object> map = objectMapper.readValue(post, Map.class);
            String link = (String) map.get("link");
            couponPackageShareRespVO.setWxShareUrl(link);
        } catch (JsonProcessingException e) {
            log.warn("wechat解析json异常{}", e.getMessage());
            throw exception(COUPON_PACKAGE_SHARE_WECHAT_JSON_PARSING_EXCEPTION_ERROR);
        }

        return couponPackageShareRespVO;
    }

    @Override
    public void issueCoupon(CouponPackageSendAO couponPackageSendAO) {
        //优惠券
        Long packageId = couponPackageSendAO.getPackageId();
        CouponPackageRespVO couponPackage = couponPackageService.selectById(packageId);



        List<UserCouponDO> userCoupons = new ArrayList<>();
        Integer num = couponPackageSendAO.getCouponNum();
        if (ObjectUtil.isEmpty(couponPackage)) {
            throw exception(COUPON_PACKAGE_NOT_EXISTS);
        }
        if (couponPackage.getIsGround() != 1) {
            throw exception(COUPON_NOT_ON_SALE);
        }

        Integer packageNum = couponPackage.getPackageNum();
        Integer limitNum = couponPackage.getLimitNum();
        if (limitNum < num) {

            throw exception(COUPON_PACKAGE_OVER_LIMIT,"优惠券包限量每人发" + limitNum + "张");
        }
        if (packageNum < num) {
            throw exception(COUPON_NO_REST);
        }
        //会员
        String memberMobile = couponPackageSendAO.getMemberMobile();
        WxMemberDTO wxMember = wxMemberApi.getMemberByMobile(memberMobile);
        if (ObjectUtil.isEmpty(wxMember) || ObjectUtil.isEmpty(wxMember.getMemberId())) {
            throw exception(COUPON_NO_USER);
        }


        // 查询优惠券包

        List<GoodCouponPackageDO> goodCouponPackages =goodCouponPackageService.selectListByPackageId(packageId);


        Long businessId = goodCouponPackages.get(0).getBusinessId();

        List<Long> couponIds = goodCouponPackages.stream().map(goodCouponPackage -> goodCouponPackage.getCouponId()).toList();
        List<GoodCouponDO> list = goodCouponService.getByIds(couponIds);

        Map<Long, Integer> couponNumMap = goodCouponPackages.stream().collect(Collectors.toMap(GoodCouponPackageDO::getCouponId, GoodCouponPackageDO::getNum));
        List<GoodCouponDO> goodCoupons = new ArrayList<>();
        UserCouponDO userCoupon = new UserCouponDO();
        GoodCouponDO updateCoupon = new GoodCouponDO();

        try {
            for (GoodCouponDO goodCoupon : list) {
                Long goodCouponId = goodCoupon.getId();
                int packNum = couponNumMap.get(goodCoupon.getId());
                int couponNum = goodCoupon.getCouponNum();
                int sendNum = packNum * num;
                if (couponNum < sendNum) {
                    couponPackageService.updateIsGroundById(packageId, IsGroundConstant.IS_GROUND_0);
                    log.error(goodCoupon.getCouponName()+"优惠劵数量不足发放");
                    throw exception(COUPON_NO_REST);
                }
                for (int i = 0; i < sendNum; i++) {
                    userCoupon = new UserCouponDO();
                    BeanUtil.copyProperties(goodCoupon, userCoupon);
                    userCoupon.setUserId(wxMember.getMemberId());
                    userCoupon.setCouponId(goodCouponId);
                  //  userCoupon.setCreateTime(DateUtils.getNowDate());
                    userCoupon.setCouponUseTime(goodCoupon.getUseTime());
                    userCoupon.setUseTime(null);
                    userCoupon.setCouponCreateTime(LocalDateTime.now());
                    userCoupon.setCreateTime(goodCoupon.getCreateTime());
                    userCoupon.setId(null);
                    userCoupon.setIsUsed(0);
                    //userCoupon.setCommodityNameStr(goodCoupon.getCouponCommodities().stream().map(CouponCommodity::getCommodityName).collect(Collectors.joining(" ")));
                    parseCouponTime(goodCoupon, userCoupon);
                    userCoupon.setDistributionMethod(0L);
                    userCoupon.setBusinessId(businessId);
                    userCoupon.setMemberMobile(memberMobile);
                    userCoupon.setCouponSource(CouponSourceConstant.COUPON_SOURCE_1);
                    userCoupon.setPackageId(packageId);
                    userCoupons.add(userCoupon);
                }


                 goodCouponService.updateReceivedNumAndCouponNumById(sendNum,goodCouponId);

            }



            couponPackageService.updateReceivedNumAndPackageNumById(num,packageId);


            UserCouponPackageDO userCouponPackage = new UserCouponPackageDO();
            userCouponPackage.setUserId(wxMember.getMemberId());
            userCouponPackage.setPackageId(packageId);
            userCouponPackage.setBusinessId(businessId);
            userCouponPackage.setPackageSource(CouponSourceConstant.COUPON_SOURCE_1);

            userCouponPackage.setMemberMobile(memberMobile);
            userCouponPackage.setMemberNickName(wxMember.getMemberNickName());


            userCouponService.insertBatch(userCoupons);

            userCouponPackageService.insert(userCouponPackage);

        } catch (ServiceException e) {
            e.printStackTrace();
            // 特定业务异常直接抛出
            throw e;
        } catch (Exception e) {
            log.error("优惠券包： " + packageId + "当前领取人数过多，请稍后重试！");
            throw new RuntimeException("系统繁忙，请稍后重试！");
        }
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
            goodCoupon.setCouponStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[0] + DateUtils.T_00_00_00));
            goodCoupon.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[1] + DateUtils.T_23_59_59));
            userCoupon.setVaildStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[0] + DateUtils.T_00_00_00));
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[1] + DateUtils.T_23_59_59));
        }
        //立即生效
        if (goodCoupon.getUseType() == 1) {
            goodCoupon.setCouponStartTime(new Date());
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(goodCoupon.getUseTime()) - 1), DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
            userCoupon.setVaildStartTime(new Date());
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));

        }
        //领券后N天生效
        if (goodCoupon.getUseType() == 2) {
            String[] split = goodCoupon.getUseTime().split("#");
            String startTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(split[0])), DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + DateUtils.T_00_00_00));
            userCoupon.setVaildStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + DateUtils.T_00_00_00));
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(split[0])).plusDays(Integer.valueOf(split[1]) - 1), DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
    }




    @Override
    public void importCouponExl(MultipartFile file, int num, Long packageId) {
        UserCouponDO userCoupon = new UserCouponDO();
        List<UserCouponDO> userCoupons = new ArrayList<>();
        CouponPackageDO entity = new CouponPackageDO();
        List<GoodCouponDO> goodCoupons = new ArrayList<>();

        List<UserCouponPackageDO> userCouponPackages = new ArrayList<>();
        UserCouponPackageDO userCouponPackage = new UserCouponPackageDO();

        String transLock = "LOCK_COUPON#" + packageId;
        RLock fairMethodLock = redissonClient.getLock(transLock);
        //优惠券信息
        CouponPackageRespVO couponPackageRespVO = couponPackageService.selectById(packageId);
        if (ObjectUtil.isEmpty(couponPackageRespVO)) {
            throw exception(COUPON_NOT_EXISTS);
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


        try {
            //上锁
            fairMethodLock.lock();


            //读取excel
            List<MemberExlVo> wxMemberDTOList = readExcelNew(file,MemberExlVo.class);
            if (CollectionUtil.isEmpty(wxMemberDTOList)) {
                throw exception(COUPON_NO_FILE_DATA);
            }
            List<String> memberMobiles = wxMemberDTOList.stream().map(MemberExlVo::getMemberMobile).toList();


            //过滤空数据
            Filter<String> predicate = item -> ObjectUtil.isNotEmpty(item);
            List<String> filter = CollectionUtil.filter(memberMobiles, predicate);
            if (CollectionUtil.isEmpty(filter)) {
                throw exception(COUPON_NO_FILE_DATA);
            }
            //去重
            CollectionUtils.select(memberMobiles, item -> {
                boolean isDuplicate = CollectionUtils.cardinality(item, memberMobiles) > 1;
                if (isDuplicate && memberMobiles.contains(item)) {

                    throw exception(COUPON_FILE_DATA_SAME,"请勿导入重复数据" + item);
                }
                return false;
            });

            //查询用户

            List<WxMemberDTO> memberList = wxMemberApi.getMemberByMobiles(memberMobiles);
            Map<String, WxMemberDTO> memberMap = memberList.stream().collect(Collectors.toMap(WxMemberDTO::getMemberMobile, p -> p));


            if (CollectionUtil.isNotEmpty(memberMap) && memberMobiles.size() != memberMap.size()) {
                for (String item : memberMobiles) {
                    if (!memberMap.containsKey(item)) {

                        throw exception(COUPON_PACKAGE_NO_MOBILE,"手机号:" + item + "未注册");
                    }
                }
            } else {
                // 查询优惠券包

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

                Map<Long, Integer> goodCouponNumMap = new HashMap<>();
                GoodCouponDO goodCoupon1 = new GoodCouponDO();
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
                    for (WxMemberDTO wxMember : memberList) {
                        for (int i = 0; i < num * packNum; i++) {
                            userCoupon = new UserCouponDO();
                            BeanUtil.copyProperties(goodCoupon, userCoupon);
                            userCoupon.setUserId(wxMember.getMemberId());
                            userCoupon.setCouponId(goodCouponId);
                            //userCoupon.setCreateTime(DateUtils.getNowDate());
                            userCoupon.setCouponUseTime(goodCoupon.getUseTime());
                            userCoupon.setUseTime(null);
                            userCoupon.setCreateTime(goodCoupon.getCreateTime());
                            userCoupon.setCouponCreateTime(LocalDateTime.now());

                            userCoupon.setId(null);
                            userCoupon.setIsUsed(0);
                            //userCoupon.setCommodityNameStr(goodCoupon.getCouponCommodities().stream().map(CouponCommodity::getCommodityName).collect(Collectors.joining(" ")));
                            parseCouponTime(goodCoupon, userCoupon);
                            userCoupon.setDistributionMethod(0L);
                            userCoupon.setMemberMobile(wxMember.getMemberMobile());
                            userCoupon.setCouponSource(CouponSourceConstant.COUPON_SOURCE_1);
                            userCoupon.setPackageId(packageId);
                            userCoupon.setBusinessId(goodCoupon.getBusinessId());
                            userCoupons.add(userCoupon);
                            // 优惠券包关系表
                            userCouponPackage.setBusinessId(goodCoupon.getBusinessId());
                            userCouponPackage.setUserId(wxMember.getMemberId());
                            userCouponPackage.setPackageId(packageId);
                            userCouponPackage.setPackageSource(CouponSourceConstant.COUPON_SOURCE_1);

                            userCouponPackage.setMemberMobile(wxMember.getMemberMobile());
                            userCouponPackage.setMemberNickName(wxMember.getMemberNickName());
                        }
                    }

                  //  goodCoupon1 = new GoodCouponDO();
       /*             UpdateWrapper<GoodCouponDO> goodCouponUpdateWrapper = UpdateWrapper.of(goodCoupon1);
                    goodCouponUpdateWrapper.setRaw("received_num", "received_num +" + totalSendNum);
                    goodCouponUpdateWrapper.setRaw("coupon_num", "coupon_num -" + totalSendNum);
                    GoodCoupon goodCouponEntity = goodCouponUpdateWrapper.toEntity();
                    goodCouponEntity.setId(goodCouponId);
                    goodCouponEntity.setUpdateTime(new Date());
                    goodCoupons.add(goodCouponEntity);*/

                    goodCouponService.updateReceivedNumAndCouponNumById(totalSendNum,goodCouponId);

                }
                //优惠券包
/*                CouponPackage couponPackage1 = new CouponPackage();
                UpdateWrapper<CouponPackage> couponPackageUpdateWrapper = UpdateWrapper.of(couponPackage1);
                couponPackageUpdateWrapper.setRaw("received_num", "received_num +" + size * num);
                couponPackageUpdateWrapper.setRaw("package_num", "package_num -" + size * num);
                entity = couponPackageUpdateWrapper.toEntity();
                entity.setId(packageId);
                entity.setUpdateTime(new Date());*/


                couponPackageService.updateReceivedNumAndPackageNumById(num,packageId);
            }


            userCouponService.insertBatch(userCoupons);
            userCouponPackageService.insert(userCouponPackage);

        } finally {
            if (ObjectUtil.isNotNull(fairMethodLock) && fairMethodLock.isLocked() && fairMethodLock.isHeldByCurrentThread()) {
                fairMethodLock.unlock();
            }
        }
    }

    private List<MemberExlVo> readExcelNew(MultipartFile file,Class<MemberExlVo> clazz) {
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
                    if (exception instanceof ExcelDataConvertException) {
                        ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException) exception;
                        log.error("第{}行，第{}列解析异常，数据为:{}");
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
    public String getLargePic() {
        return largePicUrl;
    }

    @Override
    public String getLittlePic() {
        return littlePicUrl;
    }

    @Override
    public void insert(CouponPackageShareSaveReqVO shareSaveReqVO) {
        CouponPackageShareDO bean = BeanUtils.toBean(shareSaveReqVO, CouponPackageShareDO.class);
        couponPackageShareMapper.insert(bean);
    }

    @Override
    public void updateTitle(CouponPackageShareSaveReqVO shareSaveReqVO) {
        LambdaUpdateWrapper<CouponPackageShareDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CouponPackageShareDO::getPackageId,shareSaveReqVO.getPackageId());
        updateWrapper.set(CouponPackageShareDO::getShareTitle,shareSaveReqVO.getShareTitle());
        couponPackageShareMapper.update(updateWrapper);
    }
}
