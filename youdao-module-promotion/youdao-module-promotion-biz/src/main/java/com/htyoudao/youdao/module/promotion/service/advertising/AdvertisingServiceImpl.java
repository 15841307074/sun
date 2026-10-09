package com.htyoudao.youdao.module.promotion.service.advertising;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.redis.core.utils.RedissonUtils;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.member.api.crowd.CrowdApi;
import com.htyoudao.youdao.module.member.api.crowd.vo.CustomCrowdRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.VO.*;
import com.htyoudao.youdao.module.promotion.controller.app.advertising.VO.AdvertisingConfigReqVo;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryPrizeReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.AdvertisingDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingImage.AdvertisingImageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingfloat.AdvertisingFloatDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingstore.AdvertisingForStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingtime.AdvertisingTimeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.advertising.AdvertisingMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.advertisingImage.AdvertisingImageMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.advertisingtime.AdvertisingTimeMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotterySettingsMapper;
import com.htyoudao.youdao.module.promotion.enums.AdvertisingPositionStateEnum;
import com.htyoudao.youdao.module.promotion.service.advertisingfloat.AdvertisingFloatService;
import com.htyoudao.youdao.module.promotion.service.advertisingstore.AdvertisingForStoreService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotterySettingService;
import com.htyoudao.youdao.module.promotion.util.DateUtils;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import com.htyoudao.youdao.module.promotion.util.redis.RedisForAppletAd;
import com.htyoudao.youdao.module.system.api.orgstore.OrgStoreApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.api.storeinfo.StoreInfoApi;
import com.htyoudao.youdao.module.system.api.storeinfo.dto.StorePageListReqVO;
import com.htyoudao.youdao.module.system.api.storeinfo.dto.StorePageResVO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants.*;
import static com.htyoudao.youdao.module.promotion.enums.AdvertisingStoreEnum.ADVERTISING_STORE;

@Service
public class AdvertisingServiceImpl extends ServiceImpl<AdvertisingMapper, AdvertisingDO> implements AdvertisingService {
    @Resource
    private AdvertisingForStoreService advertisingForStoreService;

    @Resource
    private AdvertisingFloatService advertisingFloatService;

    @Resource
    private RedisForAppletAd redisForAppletAd;


    @DubboReference
    private OrgStoreApi orgStoreApi;

    @DubboReference
    private StoreInfoApi storeInfoApi;

    @Resource
    private AdvertisingMapper advertisingMapper;

    @Resource
    private AdvertisingImageMapper advertisingImageMapper;

    @Resource
    private AdvertisingTimeMapper advertisingTimeMapper;


    @DubboReference
    private StoreApi storeApi;

    @DubboReference
    private CrowdApi crowdApi;

//    @DubboReference
//    private TagApi tagApi;


    public static String APPLET_AD = "APPLET_AD";

    public static String MEMBER_AD = "MEMBER_AD";

    @Resource
    RedisTemplate redisTemplate;

    public static String SPREAD = "SpreadItsTail";

    @Resource
    private RedisCache redisCache;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private LotterySettingsMapper lotterySettingsMapper;


    @Override
    public PageResult<AdvertisingPageRespVO> getPage(AdvertisingPageReqVO advertising) {

        Set<Long> advertisingAllIds = new HashSet<>();
        Long businessId = BusinessContextHolder.getBusinessId();
        LambdaQueryWrapper<AdvertisingDO> advertisingLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (advertising != null) {
            // 选择区域
            if (ObjectUtil.isNotEmpty(advertising.getOrgId())) {
                CommonResult<List<Long>> listCommonResult = orgStoreApi.selectByOrgStoreList(advertising.getOrgId());
                LambdaQueryWrapper<AdvertisingDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(AdvertisingDO::getIsAllStores,1);
                List<AdvertisingDO> advertisingDOS = advertisingMapper.selectList(wrapper);
                if (!StringUtils.isEmpty(listCommonResult.getData())) {
                    List<Long> data = listCommonResult.getData();
                    if (data.size() > 0) {

                        createTagFlag(advertising, businessId, advertisingAllIds);


                        LambdaQueryWrapper<AdvertisingForStoreDO> storeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
                        storeDOLambdaQueryWrapper.in(AdvertisingForStoreDO::getStoreId, data);
                        List<AdvertisingForStoreDO> storeDOS = advertisingForStoreService.list(storeDOLambdaQueryWrapper);
                        if (storeDOS.size() > 0) {
                            Set<Long> advertisingIds = storeDOS.stream().map(mm -> mm.getAdvertisingId()).collect(Collectors.toSet());
                            advertisingAllIds.addAll(advertisingIds);
                            if(ObjectUtil.isNotEmpty(advertisingDOS)){
                                advertisingAllIds.addAll(advertisingDOS.stream().map(mm->mm.getId()).collect(Collectors.toList()));
                            }
                        }else{
                            if(ObjectUtil.isNotEmpty(advertisingDOS)){
                                advertisingAllIds.addAll(advertisingDOS.stream().map(mm->mm.getId()).collect(Collectors.toList()));
                            }
                        }

                    }else{
                        //查询符合标签的门店列标
                        createTagFlag(advertising, businessId, advertisingAllIds);
                        LambdaQueryWrapper<AdvertisingForStoreDO> storeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
                        storeDOLambdaQueryWrapper.eq(AdvertisingForStoreDO::getStoreId, advertising.getOrgId());
                        List<AdvertisingForStoreDO> storeDOS = advertisingForStoreService.list(storeDOLambdaQueryWrapper);
                        if(ObjectUtil.isNotEmpty(storeDOS)){
                            advertisingAllIds.addAll(storeDOS.stream().map(mm->mm.getAdvertisingId()).collect(Collectors.toList()));

                        }
                        if(ObjectUtil.isNotEmpty(advertisingDOS)){
                            advertisingAllIds.addAll(advertisingDOS.stream().map(mm->mm.getId()).collect(Collectors.toList()));
                        }
                    }
                }else{

                    //查询符合标签的门店列标
                    createTagFlag(advertising, businessId, advertisingAllIds);
                    LambdaQueryWrapper<AdvertisingForStoreDO> storeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    storeDOLambdaQueryWrapper.eq(AdvertisingForStoreDO::getStoreId, advertising.getOrgId());
                    AdvertisingForStoreDO advertisingForStoreServiceOne = advertisingForStoreService.getOne(storeDOLambdaQueryWrapper);
                    if(advertisingForStoreServiceOne!=null){
                        advertisingAllIds.add(advertisingForStoreServiceOne.getAdvertisingId());
                    }

                    if(ObjectUtil.isNotEmpty(advertisingDOS)){
                        advertisingAllIds.addAll(advertisingDOS.stream().map(mm->mm.getId()).collect(Collectors.toList()));
                    }
                }
            }


            if (advertisingAllIds.size() == 0) {
                if (!StringUtils.isEmpty(advertising.getOrgId()) || !StringUtils.isEmpty(advertising.getStoreId())) {
                    return new PageResult<>();
                }
            }
            if (advertisingAllIds.size() > 0) {
                advertisingLambdaQueryWrapper.in(AdvertisingDO::getId, advertisingAllIds);
            }


            if (ObjectUtil.isNotEmpty(advertising.getAdInfoPosition())) {
                advertisingLambdaQueryWrapper.eq(AdvertisingDO::getAdInfoPosition, advertising.getAdInfoPosition());
            }


            if (ObjectUtil.isNotEmpty(advertising.getAdName())) {
                advertisingLambdaQueryWrapper.like(AdvertisingDO::getAdName, advertising.getAdName());
            }

            if (ObjectUtil.isNotEmpty(advertising.getIsOpen())) {
                List<Integer> isOpen = advertising.getIsOpen();
                advertisingLambdaQueryWrapper.in(AdvertisingDO::getIsOpen, isOpen);
            }
        }
        advertisingLambdaQueryWrapper.eq(AdvertisingDO::getBusinessId, businessId);
        advertisingLambdaQueryWrapper.orderByDesc(AdvertisingDO::getCreateTime);

        Page<AdvertisingDO> objectPage = new Page<>(advertising.getPageNo(), advertising.getPageSize());

        Page<AdvertisingDO> advertisingPage = advertisingMapper.selectPage(objectPage, advertisingLambdaQueryWrapper);
        List<AdvertisingDO> records = advertisingPage.getRecords();
        List<AdvertisingPageRespVO> list = new ArrayList<>();

        if (records.size() > 0) {
            for (AdvertisingDO record : records) {
                AdvertisingPageRespVO advertisingPageRespVO = new AdvertisingPageRespVO();
                advertisingPageRespVO.setId(record.getId());
                advertisingPageRespVO.setAdName(record.getAdName());
                advertisingPageRespVO.setIsOpen(record.getIsOpen());
                advertisingPageRespVO.setAdInfoPosition(record.getAdInfoPosition());
                LambdaQueryWrapper<AdvertisingImageDO> imWrapper = new LambdaQueryWrapper<AdvertisingImageDO>();
                imWrapper.eq(AdvertisingImageDO::getAdvertisingId, record.getId());
                List<AdvertisingImageDO> advertisingImageDOS = advertisingImageMapper.selectList(imWrapper);
                advertisingPageRespVO.setImageDOList(advertisingImageDOS);
                advertisingPageRespVO.setIsAllStores(record.getIsAllStores());
                if(record.getApplicationScope().equals(1)){
                    if (record.getIsAllStores().equals(1)) {
                        advertisingPageRespVO.setApplicableStore("全门店");
                    } else {
                        List<AdvertisingForStoreDO> storeDOS = advertisingForStoreService.list(new LambdaQueryWrapper<AdvertisingForStoreDO>().eq(AdvertisingForStoreDO::getAdvertisingId, record.getId()));
                        if (storeDOS.size() > 0) {
                            List<Long> storeIds = storeDOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                            CommonResult<List<StoreInfoDTO>> storesByStoreIds = storeApi.getStoresByStoreIds(storeIds);
                            List<StoreInfoDTO> dataList = storesByStoreIds.getData();
                            advertisingPageRespVO.setStoreInfoDTOS(dataList);

                        }
                    }
                }else{
                    //按标签
                    List<Long> tagList = record.getTagList();
                    if(ObjectUtil.isNotEmpty(tagList)){
                        List<StoreInfoDTO> storeInfoDTOS = storeApi.selectAdvertisingStoreList(tagList);
                        if(ObjectUtil.isNotEmpty(storeInfoDTOS)){
                            advertisingPageRespVO.setStoreInfoDTOS(storeInfoDTOS);
                        }
                    }
                }

                advertisingPageRespVO.setStartTime(record.getStartTime());
                advertisingPageRespVO.setEndTime(record.getEndTime());
                advertisingPageRespVO.setIsAllTime(record.getIsAllTime());
                advertisingPageRespVO.setDayNumberList(record.getDayNumberList());
                advertisingPageRespVO.setWeekNumberList(record.getWeekNumberList());
                advertisingPageRespVO.setShowTimeType(record.getShowTimeType());
                advertisingPageRespVO.setTimeDOList(advertisingTimeMapper.selectList(new LambdaQueryWrapper<AdvertisingTimeDO>().eq(AdvertisingTimeDO::getAdvertisingId, record.getId())));
                list.add(advertisingPageRespVO);
            }

        }


        PageResult<AdvertisingPageRespVO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setTotal(advertisingPage.getTotal());

        return pageResult;


    }

    private void createTagFlag(AdvertisingPageReqVO advertising, Long businessId, Set<Long> advertisingAllIds) {
        //查询符合标签的门店列标
        LambdaQueryWrapper<AdvertisingDO> doLambdaQueryWrapper = new LambdaQueryWrapper<>();
        doLambdaQueryWrapper.eq(AdvertisingDO::getApplicationScope, 2);
        doLambdaQueryWrapper.eq(AdvertisingDO::getBusinessId, businessId);
        List<AdvertisingDO> tagAdvertisingList = this.list(doLambdaQueryWrapper);

        if(ObjectUtil.isNotEmpty(tagAdvertisingList)){
            for (AdvertisingDO advertisingDO : tagAdvertisingList) {
                List<Long> tagList = advertisingDO.getTagList();
                if(ObjectUtil.isNotEmpty(tagList)){
                    for (Long tag : tagList) {
                        Boolean flag = storeApi.isTag(advertising.getOrgId(), tag);
                        if(flag){
                            advertisingAllIds.add(advertisingDO.getId());
                            break;
                        }

                    }

                }

            }

        }
    }


    @Override
    public AdvertisingRespVO getInfo(Long id) {
        AdvertisingRespVO respVO = new AdvertisingRespVO();
        AdvertisingDO advertising = advertisingMapper.selectById(id);


        if (advertising.getAdInfoPosition().equals(AdvertisingPositionStateEnum.FLOATING_WINDOW.getStatus())) {
            LambdaQueryWrapper<AdvertisingFloatDO> floatLambdaQueryWrapper = new LambdaQueryWrapper<>();
            floatLambdaQueryWrapper.eq(AdvertisingFloatDO::getAdvertisingId, advertising.getId());
            List<AdvertisingFloatDO> listF = advertisingFloatService.list(floatLambdaQueryWrapper);
            if (ObjectUtil.isNotEmpty(listF)) {
                advertising.setFloatingWindowDisplay(listF.stream().map(AdvertisingFloatDO::getFloatingWindowDisplay).collect(Collectors.toList()));
            }
        }

        BeanUtils.copyProperties(advertising, respVO);
        if(advertising.getTriggerCondition().equals(5)){
            List<CrowdDataRespVo> crowdList = new ArrayList<>();
            List<Long> crowdIds = advertising.getCrowdIds();
            if(ObjectUtil.isNotEmpty(crowdIds)){
                for (Long crowdId : crowdIds) {
                    CrowdDataRespVo crowdDataRespVo = new CrowdDataRespVo();
                    crowdDataRespVo.setCrowdId(crowdId);
                    CommonResult<CustomCrowdRespVO> byId = crowdApi.getById(crowdId);
                    CustomCrowdRespVO data = byId.getData();
                    if(ObjectUtil.isNotEmpty(data)){
                        crowdDataRespVo.setCrowdName(data.getCrowdName());
                    }
                    crowdList.add(crowdDataRespVo);
                    respVO.setCrowdList(crowdList);
                }
            }
        }
//        if(advertising.getApplicationScope().equals(2)){
//            List<AdvertisingTagRespVO> tagNameList = new ArrayList<>();
//            List<Long> tagList = advertising.getTagList();
//            if(ObjectUtil.isNotEmpty(tagList)){
//                for (Long aLong : tagList) {
//                    AdvertisingTagRespVO advertisingTagRespVO = new AdvertisingTagRespVO();
//                    String byTagName = tagApi.getByTagName(aLong);
//                    if(byTagName!=null){
//                        advertisingTagRespVO.setTagId(aLong);
//                        advertisingTagRespVO.setTagName(byTagName);
//                        tagNameList.add(advertisingTagRespVO);
//
//                    }
//                }
//                respVO.setTagNameList(tagNameList);
//            }
//
//        }
        
        
        if (advertising.getIsAllStores().equals(2)) {
            LambdaQueryWrapper<AdvertisingForStoreDO> storeLambdaQueryWrapper = new LambdaQueryWrapper<>();
            storeLambdaQueryWrapper.eq(AdvertisingForStoreDO::getAdvertisingId, advertising.getId());
            List<AdvertisingForStoreDO> list = advertisingForStoreService.list(storeLambdaQueryWrapper);
            if (ObjectUtil.isNotEmpty(list)) {
                List<Long> collect = list.stream().map(AdvertisingForStoreDO::getStoreId).collect(Collectors.toList());
                respVO.setStoreIds(collect);
                CommonResult<List<StoreInfoDTO>> storesByStoreIds = storeApi.getStoresByStoreIds(collect);
                List<StoreInfoDTO> dataList = storesByStoreIds.getData();
                respVO.setStoreInfoDTOS(dataList);
            }
        }
        LambdaQueryWrapper<AdvertisingImageDO> imageDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        imageDOLambdaQueryWrapper.eq(AdvertisingImageDO::getAdvertisingId, id);
        List<AdvertisingImageDO> advertisingImageDOS = advertisingImageMapper.selectList(imageDOLambdaQueryWrapper);
        if(ObjectUtil.isNotEmpty(advertisingImageDOS)){
            for (AdvertisingImageDO advertisingImageDO : advertisingImageDOS) {
                if(ObjectUtil.isNotEmpty(advertisingImageDO.getJumpLocation())){
                    if(advertisingImageDO.getJumpLocation().equals(7)){
                        if(ObjectUtil.isNotEmpty(advertisingImageDO.getActivityId())){
                            if(advertisingImageDO.getActivityType().equals("2") || advertisingImageDO.getActivityType().equals("4")|| advertisingImageDO.getActivityType().equals("6")|| advertisingImageDO.getActivityType().equals("7")){
                                ActivityDO activityDO = activityMapper.selectById(advertisingImageDO.getActivityId());
                                if(activityDO!=null){
                                    advertisingImageDO.setActivityName(activityDO.getActivityName());
                                }

                            }else if(advertisingImageDO.getActivityType().equals("3")){
//                                LotterySettingsDO lotterySettingsDO = lotterySettingsMapper.selectById(advertisingImageDO.getActivityId());
//                                if(lotterySettingsDO!=null){
//                                    advertisingImageDO.setActivityName(lotterySettingsDO.getLotteryTitle());
//                                    advertisingImageDO.setActivityId(lotterySettingsDO.getActivityId().toString());
//                                }
                                ActivityDO activityDO = activityMapper.selectById(advertisingImageDO.getActivityId());
                                if(activityDO!=null){
                                    advertisingImageDO.setActivityName(activityDO.getActivityName());
                                }
                            }else{
                                ActivityDO activityDO = activityMapper.selectById(advertisingImageDO.getActivityId());
                                if(activityDO!=null){
                                    advertisingImageDO.setActivityName(activityDO.getActivityName());
                                }
                            }

                        }
                    }
                }


            }
        }
        respVO.setImageDOList(advertisingImageDOS);
        LambdaQueryWrapper<AdvertisingTimeDO> timeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        timeDOLambdaQueryWrapper.eq(AdvertisingTimeDO::getAdvertisingId, id);
        List<AdvertisingTimeDO> advertisingTimeDOS = advertisingTimeMapper.selectList(timeDOLambdaQueryWrapper);
        respVO.setTimeVOList(advertisingTimeDOS);
        return respVO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = PROMOTION_ADVERTISING_TYPE, subType = PROMOTION_ADVERTISING_CREATE_TYPE, bizNo = "{{#advertising.id}}", success = PROMOTION_ADVERTISING_CREATE_SUCCESS)
    public void createAdvertising(AdvertisingSaveReqVO saveReqVO) {

        Long businessId = BusinessContextHolder.getBusinessId();
        AdvertisingDO advertisingDO = new AdvertisingDO();
        BeanUtils.copyProperties(saveReqVO, advertisingDO);
        if(saveReqVO.getApplicationScope().equals(2)){
            advertisingDO.setIsAllStores(2);
        }
        advertisingDO.setIsOpen(2);
        if(ObjectUtil.isNotEmpty(saveReqVO.getTriggerCondition())){
            if (saveReqVO.getTriggerCondition().equals(5)) {
                List<Long> crowdIds = saveReqVO.getCrowdIds();
                if (ObjectUtil.isNotEmpty(crowdIds)) {
                    String crowds = crowdIds.toString().replace("[", "").replace("]", "").replace(" ","");
                    advertisingDO.setCrowds(crowds);
                }
            }
        }
        this.save(advertisingDO);
        LogRecordContext.putVariable("advertising", advertisingDO);
        redisForAppletAd.delAllStore(businessId);

        if(saveReqVO.getApplicationScope().equals(1)){
            // 门店列表
            if (saveReqVO.getIsAllStores().equals(2)) {
                List<Long> storeIds = saveReqVO.getStoreIds();
                List<AdvertisingForStoreDO> advertisingForStoreList = new ArrayList<>();
                for (Long storeId : storeIds) {
                    AdvertisingForStoreDO advertisingForStore = new AdvertisingForStoreDO();
                    advertisingForStore.setAdvertisingId(advertisingDO.getId());
                    advertisingForStore.setStoreId(storeId);
                    advertisingForStore.setDeleted(false);
                    advertisingForStore.setBusinessId(businessId);
                    advertisingForStoreList.add(advertisingForStore);
                }
                if (ObjectUtil.isNotEmpty(advertisingForStoreList)) {
                    advertisingForStoreService.saveBatch(advertisingForStoreList);
                }
            }
        }


        // 浮窗
        if (saveReqVO.getAdInfoPosition().equals(AdvertisingPositionStateEnum.FLOATING_WINDOW.getStatus())) {
            List<Integer> floatingWindowDisplay = saveReqVO.getFloatingWindowDisplay();
            List<AdvertisingFloatDO> floatingWindowDisplayList = new ArrayList<>();
            for (Integer i : floatingWindowDisplay) {
                AdvertisingFloatDO advertisingFloat = new AdvertisingFloatDO();
                advertisingFloat.setFloatingWindowDisplay(i);
                advertisingFloat.setAdvertisingId(advertisingDO.getId());
                advertisingFloat.setDeleted(false);
                advertisingFloat.setBusinessId(businessId);
                floatingWindowDisplayList.add(advertisingFloat);
            }
            if (ObjectUtil.isNotEmpty(floatingWindowDisplayList)) {
                advertisingFloatService.saveBatch(floatingWindowDisplayList);
            }
        }

        if (!StringUtils.isEmpty(saveReqVO.getImageDOList())) {
            if (saveReqVO.getImageDOList().size() > 0) {
                List<AdvertisingImageDO> imageDOList = saveReqVO.getImageDOList();
                for (AdvertisingImageDO advertisingImageDO : imageDOList) {
                    advertisingImageDO.setAdvertisingId(advertisingDO.getId());
                    advertisingImageDO.setBusinessId(businessId);
                    advertisingImageDO.setCreateTime(LocalDateTime.now());
                    advertisingImageDO.setDeleted(false);
                    advertisingImageDO.setUpdateTime(LocalDateTime.now());
                    if (!StringUtils.isEmpty(advertisingImageDO.getCouponId())) {
                        advertisingImageDO.setCouponId(advertisingImageDO.getCouponId());
                    }
                    advertisingImageMapper.insert(advertisingImageDO);
                }
            }
        }


        if (saveReqVO.getShowTimeType().equals(2)) {
            if (!StringUtils.isEmpty(saveReqVO.getTimeVOList())) {
                if (saveReqVO.getTimeVOList().size() > 0) {
                    List<TimeVO> timeVOList = saveReqVO.getTimeVOList();
                    for (TimeVO timeVO : timeVOList) {
                        AdvertisingTimeDO advertisingTimeDO = new AdvertisingTimeDO();
                        advertisingTimeDO.setAdvertisingId(advertisingDO.getId());
                        advertisingTimeDO.setStartTimeSegment(timeVO.getStartTimeSegment());
                        advertisingTimeDO.setEndTimeSegment(timeVO.getEndTimeSegment());
                        advertisingTimeDO.setCreateTime(LocalDateTime.now());
                        advertisingTimeDO.setDeleted(false);
                        advertisingTimeDO.setBusinessId(businessId);
                        advertisingTimeMapper.insert(advertisingTimeDO);
                    }

                }
            }
        }


    }

    /**
     * 点餐页商品分类的 banner   同一商品分类 不能同时存在.
     *
     * @param advertising
     */
    public void isTimeConflictSeven(AdvertisingDO advertising) {
        if (advertising.getAdInfoPosition().equals(7)) {
            // 广告位置
            Integer adInfoPosition = advertising.getAdInfoPosition();

            // 分类 id
            Long categoryId = advertising.getCategoryId();
            if (ObjectUtil.isEmpty(categoryId)) {
                throw exception(ADVERTISEMENT_CATEGORY_NOT_SELECT);
            }
            LambdaQueryWrapper<AdvertisingDO> advertisingLambdaQueryWrapper = new LambdaQueryWrapper<>();
            advertisingLambdaQueryWrapper.eq(AdvertisingDO::getAdInfoPosition, adInfoPosition);
//            advertisingLambdaQueryWrapper.eq(AdvertisingDO::getIsDelete,0);
            advertisingLambdaQueryWrapper.eq(AdvertisingDO::getCategoryId, categoryId);
            if (ObjectUtil.isNotEmpty(advertising.getId())) {
                advertisingLambdaQueryWrapper.notIn(AdvertisingDO::getId, advertising.getId());
            }
            List<AdvertisingDO> list = this.list(advertisingLambdaQueryWrapper);
            if (ObjectUtil.isNotEmpty(list)) {
                throw exception(ADVERTISEMENT_REPEAT);
            }

        }
    }
//    public void isTimeConflictFive(AdvertisingDO advertising){
//        if (advertising.getAdInfoPosition().equals(5)) {
//            //如果是点餐弹屏
//                Integer adInfoPosition = advertising.getAdInfoPosition();
//            //触发条件设置 1.所有用户 2.新用户 3.老用户 4.回归用户
//            Integer triggerCondition = advertising.getTriggerCondition();
//
//            LambdaQueryWrapper<AdvertisingDO> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(AdvertisingDO::getAdInfoPosition,adInfoPosition);
//            queryWrapper.eq(AdvertisingDO::getTriggerCondition,triggerCondition);
////            queryWrapper.eq(AdvertisingDO::getIsDelete,0);
//            if (ObjectUtil.isNotEmpty(advertising.getId())){
//                queryWrapper.notIn(AdvertisingDO::getId,advertising.getId());
//            }
//            List<AdvertisingDO> list = this.list(queryWrapper);
//
//            if (ObjectUtil.isNotEmpty(list)){
//                if(advertising.getIsAllTime().equals(1)){
//                    throw exception(ADVERTISEMENT_TIME_CONFLICT);
//                }
//
//                for (AdvertisingDO advertising1 : list) {
//                     if (advertising1.getIsAllTime().equals(1)){
//                         throw exception(ADVERTISEMENT_TIME_CONFLICT);
//                     }else {
//                       if (isTimeRangeConflict(advertising.getStartTime(),advertising.getEndTime(),advertising1.getStartTime(),advertising1.getEndTime())){
//                           throw exception(ADVERTISEMENT_TIME_CONFLICT);
//                       }
//                     }
//                }
//            }
//        }
//    }


    /**
     * 修改广告
     *
     * @param
     */
    @Override
    @LogRecord(type = PROMOTION_ADVERTISING_TYPE, subType = PROMOTION_ADVERTISING_UPDATE_TYPE, bizNo = "{{#advertising.id}}", success = PROMOTION_ADVERTISING_UPDATE_SUCCESS)
    @Transactional(rollbackFor = Exception.class)
    public void updateAdvertising(AdvertisingSaveReqVO updateVo) {
        AdvertisingDO advertisingDO = new AdvertisingDO();
        Long businessId = BusinessContextHolder.getBusinessId();
        BeanUtils.copyProperties(updateVo, advertisingDO);
        if(updateVo.getApplicationScope().equals(2)){
            advertisingDO.setIsAllStores(2);
        }
        advertisingDO.setBusinessId(businessId);
        if(ObjectUtil.isNotEmpty(updateVo.getTriggerCondition())){
            if (updateVo.getTriggerCondition().equals(5)) {
                List<Long> crowdIds = updateVo.getCrowdIds();
                if (ObjectUtil.isNotEmpty(crowdIds)) {
                    String crowds = crowdIds.toString().replace("[", "").replace("]", "").replace(" ","");
                    advertisingDO.setCrowds(crowds);
                }
            }
        }

        advertisingMapper.updateById(advertisingDO);
        LogRecordContext.putVariable("advertising", advertisingDO);
        // 处理广告门店
        LambdaQueryWrapper<AdvertisingForStoreDO> storeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        storeLambdaQueryWrapper.eq(AdvertisingForStoreDO::getAdvertisingId, updateVo.getId());
        advertisingForStoreService.remove(storeLambdaQueryWrapper);
        if(updateVo.getApplicationScope().equals(1)){
            if (updateVo.getIsAllStores().equals(2)) {

                List<Long> storeIds = updateVo.getStoreIds();

                if (ObjectUtil.isNotEmpty(storeIds)) {
                    List<AdvertisingForStoreDO> storeForStoreList = new ArrayList<>();
                    for (Long storeId : storeIds) {


                        AdvertisingForStoreDO advertisingForStore = new AdvertisingForStoreDO();

                        advertisingForStore.setAdvertisingId(updateVo.getId());
                        advertisingForStore.setStoreId(storeId);
                        advertisingForStore.setBusinessId(businessId);

                        advertisingForStore.setDeleted(false);
                        storeForStoreList.add(advertisingForStore);
                    }
                    advertisingForStoreService.saveBatch(storeForStoreList);
                } else {
                    throw exception(ADVERTISEMENT_STORE_ID_ILLEGAL);
                }

            }
        }

        redisForAppletAd.delAllStore(businessId);
//        if (advertising.getAdPosition().equals(9)){
//            redisTemplate.delete(SPREAD);
//        }

        if (updateVo.getAdInfoPosition().equals(AdvertisingPositionStateEnum.FLOATING_WINDOW.getStatus())) {
            // 处理浮窗
            LambdaQueryWrapper<AdvertisingFloatDO> floatLambdaQueryWrapper = new LambdaQueryWrapper<>();
            floatLambdaQueryWrapper.eq(AdvertisingFloatDO::getAdvertisingId, advertisingDO.getId());
            advertisingFloatService.remove(floatLambdaQueryWrapper);
            List<Integer> floatingWindowDisplay = updateVo.getFloatingWindowDisplay();
            if (ObjectUtil.isNotEmpty(floatingWindowDisplay)) {
                List<AdvertisingFloatDO> floatingWindowDisplayList = new ArrayList<>();
                for (Integer i : floatingWindowDisplay) {
                    AdvertisingFloatDO advertisingFloat = new AdvertisingFloatDO();
                    advertisingFloat.setBusinessId(businessId);
                    advertisingFloat.setFloatingWindowDisplay(i);
                    advertisingFloat.setAdvertisingId(advertisingDO.getId());
                    advertisingFloat.setDeleted(false);
                    floatingWindowDisplayList.add(advertisingFloat);
                }
                advertisingFloatService.saveBatch(floatingWindowDisplayList);
            } else {
                throw exception(ADVERTISEMENT_FLOATING_ILLEGAL);
            }
        }

        // 处理图片
        LambdaQueryWrapper<AdvertisingImageDO> imageDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        imageDOLambdaQueryWrapper.eq(AdvertisingImageDO::getAdvertisingId, updateVo.getId());
        advertisingImageMapper.delete(imageDOLambdaQueryWrapper);
        if (!StringUtils.isEmpty(updateVo.getImageDOList())) {
            if (updateVo.getImageDOList().size() > 0) {
                List<AdvertisingImageDO> imageDOList = updateVo.getImageDOList();
                for (AdvertisingImageDO advertisingImageDO : imageDOList) {
                    advertisingImageDO.setId(null);
                    advertisingImageDO.setBusinessId(businessId);
                    advertisingImageDO.setDeleted(false);
                    advertisingImageDO.setAdvertisingId(advertisingDO.getId());
                    LocalDateTime now = LocalDateTime.now();
                    advertisingImageDO.setCreateTime(now);
                    advertisingImageDO.setUpdateTime(now);
                    if(advertisingImageDO.getImageStatus().equals(1)){
                        advertisingImageDO.setDynamicImage(null);
                    }
                    advertisingImageMapper.insert(advertisingImageDO);
                }
            }
        }


        // 处理时间段
        LambdaQueryWrapper<AdvertisingTimeDO> timeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        timeDOLambdaQueryWrapper.eq(AdvertisingTimeDO::getAdvertisingId, updateVo.getId());
        advertisingTimeMapper.delete(timeDOLambdaQueryWrapper);

        if (updateVo.getShowTimeType().equals(2)) {
            if (!StringUtils.isEmpty(updateVo.getTimeVOList())) {
                if (updateVo.getTimeVOList().size() > 0) {
                    List<TimeVO> timeVOList = updateVo.getTimeVOList();
                    for (TimeVO timeVO : timeVOList) {
                        AdvertisingTimeDO advertisingTimeDO = new AdvertisingTimeDO();
                        advertisingTimeDO.setAdvertisingId(advertisingDO.getId());
                        advertisingTimeDO.setStartTimeSegment(timeVO.getStartTimeSegment());
                        advertisingTimeDO.setEndTimeSegment(timeVO.getEndTimeSegment());
                        advertisingTimeDO.setDeleted(false);
                        advertisingTimeDO.setBusinessId(businessId);

                        advertisingTimeMapper.insert(advertisingTimeDO);
                    }

                }

            }

        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = PROMOTION_ADVERTISING_TYPE, subType = PROMOTION_ADVERTISING_DELETE_TYPE, bizNo = "{{#advertising.id}}", success = PROMOTION_ADVERTISING_DELETE_SUCCESS)
    public void deleteAdvertising(Long id) {
        Long businessId = BusinessContextHolder.getBusinessId();
        redisForAppletAd.delAllStore(businessId);
        LambdaQueryWrapper<AdvertisingDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdvertisingDO::getId,id);
        AdvertisingDO advertisingDO = advertisingMapper.selectOne(wrapper);
        if(advertisingDO!=null){
            LogRecordContext.putVariable("advertising", advertisingDO);
        }
        LambdaQueryWrapper<AdvertisingImageDO> imageDOLambdaUpdateWrapper = new LambdaQueryWrapper<>();
        imageDOLambdaUpdateWrapper.eq(AdvertisingImageDO::getAdvertisingId, id);
        advertisingImageMapper.delete(imageDOLambdaUpdateWrapper);


        LambdaQueryWrapper<AdvertisingTimeDO> timeDOLambdaUpdateWrapper = new LambdaQueryWrapper<>();
        timeDOLambdaUpdateWrapper.eq(AdvertisingTimeDO::getAdvertisingId, id);
        advertisingTimeMapper.delete(timeDOLambdaUpdateWrapper);

        LambdaQueryWrapper<AdvertisingFloatDO> floatLambdaUpdateWrapper = new LambdaQueryWrapper<>();
        floatLambdaUpdateWrapper.eq(AdvertisingFloatDO::getAdvertisingId, id);
        advertisingFloatService.remove(floatLambdaUpdateWrapper);

        LambdaQueryWrapper<AdvertisingForStoreDO> storeLambdaUpdateWrapper = new LambdaQueryWrapper<>();
        storeLambdaUpdateWrapper.eq(AdvertisingForStoreDO::getAdvertisingId, id);
        advertisingForStoreService.remove(storeLambdaUpdateWrapper);


        LambdaQueryWrapper<AdvertisingDO> UpdateWrapper = new LambdaQueryWrapper<>();
        UpdateWrapper.eq(AdvertisingDO::getId, id);
        this.remove(UpdateWrapper);

    }

    @Override
    @LogRecord(type = PROMOTION_ADVERTISING_TYPE, subType = PROMOTION_ADVERTISING_STATUS_TYPE, bizNo = "{{#advertising.id}}", success = PROMOTION_ADVERTISING_STATUS_SUCCESS)
    public void updateAdvertisingStatus(AdvertisingStatusReqVO advertisingStatusReqVO) {
        Long businessId = BusinessContextHolder.getBusinessId();

        AdvertisingDO advertisingDO = advertisingMapper.selectById(advertisingStatusReqVO.getId());
        LogRecordContext.putVariable("advertising", advertisingStatusReqVO);
        Integer beginTime = 0;
        Integer endTime = 0;
        if (!StringUtils.isEmpty(advertisingDO.getStartTime())) {
            beginTime = Integer.parseInt(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, advertisingDO.getStartTime()).replace("-", ""));
        }
        if (!StringUtils.isEmpty(advertisingDO.getEndTime())) {
            endTime = Integer.parseInt(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, advertisingDO.getEndTime()).replace("-", ""));
        }

        if (advertisingStatusReqVO.getIsOpen().equals(1)) {
            if (advertisingDO.getAdInfoPosition().equals(AdvertisingPositionStateEnum.SPLASH_SCREEN_ADVERTISEMENT.getStatus())) {
                LambdaQueryWrapper<AdvertisingDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(AdvertisingDO::getAdInfoPosition, AdvertisingPositionStateEnum.SPLASH_SCREEN_ADVERTISEMENT.getStatus());
                lambdaQueryWrapper.eq(AdvertisingDO::getIsOpen, 1);
                lambdaQueryWrapper.eq(AdvertisingDO::getBusinessId, businessId);
                List<AdvertisingDO> advertisingDOS = advertisingMapper.selectList(lambdaQueryWrapper);
                if (advertisingDOS.size() > 0) {
                    for (AdvertisingDO aDo : advertisingDOS) {
                        if (advertisingDO.getIsAllTime().equals(1) || aDo.getIsAllTime().equals(1)) {
                            throw exception(ADVERTISEMENT_TIME_NOT);
                        }
                        // 历史数据
                        Integer begin = 0;
                        Integer end = 0;
                        if (!StringUtils.isEmpty(aDo.getStartTime())) {
                            begin = Integer.parseInt(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, aDo.getStartTime()).replace("-", ""));
                        }
                        if (!StringUtils.isEmpty(aDo.getEndTime())) {
                            end = Integer.parseInt(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, aDo.getEndTime()).replace("-", ""));
                        }
                        if (beginTime > 0 && begin > 0) {
                            if (beginTime >= begin && begin <= endTime) {
                                if (advertisingDO.getShowTimeType().equals(1)) {
                                    throw exception(ADVERTISEMENT_TIME_NOT);
                                }

                            } else if (beginTime <= end && endTime >= end) {
                                if (advertisingDO.getShowTimeType().equals(1)) {
                                    throw exception(ADVERTISEMENT_TIME_NOT);
                                }
                            }
                        }

                    }
                }

            }
        }


        LambdaUpdateWrapper<AdvertisingDO> advertisingLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        advertisingLambdaUpdateWrapper.eq(AdvertisingDO::getId, advertisingStatusReqVO.getId());
        advertisingLambdaUpdateWrapper.set(AdvertisingDO::getIsOpen, advertisingStatusReqVO.getIsOpen());
        this.update(advertisingLambdaUpdateWrapper);
        redisForAppletAd.delAllStore(businessId);
//        if(advertisingDO!=null){
//            AdvertisingDO aa = new AdvertisingDO();
//            aa.setId(advertisingDO.getId());
//            aa.setIsOpen(advertisingStatusReqVO.getIsOpen());
//            LogRecordContext.putVariable("advertising", aa);
//        }

    }


    @Override
    public List<AdvertisingConfigReqVo> appletGetAdvertisingNew(AdvertisingVO advertisingVO) {


        Long businessId = BusinessContextHolder.getBusinessId();
        List<AdvertisingConfigReqVo> configReqVoList = new ArrayList<>();

        Long storeId = ADVERTISING_STORE.getStatus();
        Map<Integer, List<AdvertisingRespVO>> integerListMap = new HashMap<>();

        if (!StringUtils.isEmpty(advertisingVO.getStoreId())) {
            storeId = advertisingVO.getStoreId();
            String adKey = APPLET_AD + businessId + ":" + storeId;
            try {
                // Object o = redisTemplate.opsForValue().get(adKey);
                Object o = RedissonUtils.getCacheObject(adKey);
                if (ObjectUtil.isEmpty(o)) {

                    integerListMap = appletGetAllAdvertising(storeId, businessId);
                    if (ObjectUtil.isEmpty(integerListMap)) {
                        AdvertisingDTO advertisingDTO = new AdvertisingDTO();
                        // redisTemplate.opsForValue().set(adKey, advertisingDTO);
                        RedissonUtils.setCacheObject(adKey, advertisingDTO);
                        return new ArrayList<>();
                    } else {
                        AdvertisingDTO advertisingDTO = new AdvertisingDTO();
                        advertisingDTO.setIntegerListMap(integerListMap);
                        // redisTemplate.opsForValue().set(adKey, JSONObject.toJSONString(advertisingDTO));
                        RedissonUtils.setCacheObject(adKey, JSONObject.toJSONString(advertisingDTO));
                    }
                    matchAdvertisement(integerListMap, integerListMap);

                } else {

                    AdvertisingDTO r = com.alibaba.fastjson2.JSONObject.parseObject(o.toString(), AdvertisingDTO.class);
                    Map<Integer, List<AdvertisingRespVO>> rIntegerListMap = r.getIntegerListMap();
                    matchAdvertisement(rIntegerListMap, integerListMap);

                }
            } catch (Exception e) {
                Map<Integer, List<AdvertisingRespVO>> rintegerListMap = appletGetAllAdvertising(storeId, businessId);
                matchAdvertisement(rintegerListMap, integerListMap);

            }
            Map<Integer, List<AdvertisingRespVO>> outegerListMap = new HashMap<>();
            if (ObjectUtil.isNotEmpty(advertisingVO.getLocationIds())) {
                for (Integer locationId : advertisingVO.getLocationIds()) {
                    List<AdvertisingRespVO> adDTOS = integerListMap.get(locationId);


                    if (ObjectUtil.isNotEmpty(adDTOS)) {
                        List<AdvertisingImageDO> imageDOList = new ArrayList<>();
                        AdvertisingConfigReqVo advertisingConfigReqVo = new AdvertisingConfigReqVo();

                        for (AdvertisingRespVO adDTO : adDTOS) {
                            List<AdvertisingImageDO> imageDOListIds = adDTO.getImageDOList();
                            //判断人群
                            if(ObjectUtil.isNotEmpty(adDTO.getTriggerCondition())){
                                if(adDTO.getTriggerCondition().equals(5)){
                                    if(ObjectUtil.isNotEmpty(advertisingVO.getMemberId())){
                                        List<Object> objectList = new ArrayList<>();
                                        List<Long> crowdIds = adDTO.getCrowdIds();
                                        for (Long crowdId : crowdIds) {
                                            int shardIndex = (int) (advertisingVO.getMemberId() % 10);
                                            String shardKey = "member_crowd:" + crowdId + "_shard_" + shardIndex;
                                            Object hashValue = redisCache.getHashValue(shardKey, advertisingVO.getMemberId().toString());
                                            if(ObjectUtil.isNotEmpty(hashValue)){
                                                objectList.add(hashValue);
                                            }
                                        }
                                        if(ObjectUtil.isEmpty(objectList)){
                                            continue;
                                        }
                                    }

                                }
                            }


                            // 如果 appSource 等于 2，则删除 jumpLocation = 5 的数据
                            if (advertisingVO.getAppSource() != null && advertisingVO.getAppSource() == 2) {
                                imageDOListIds.removeIf(imageDO -> imageDO.getJumpLocation() != null && imageDO.getJumpLocation() == 5);
                            }

                            for (AdvertisingImageDO imageDOListId : imageDOListIds) {
                                imageDOListId.setRuler(adDTO.getRuler());
                                imageDOListId.setTriggerCondition(adDTO.getTriggerCondition());
                                imageDOListId.setFloatingWindowDisplay(adDTO.getFloatingWindowDisplay());
                                imageDOListId.setPopStyle(adDTO.getPopStyle());
                                imageDOListId.setArrangeMethod(adDTO.getArrangeMethod());
                                imageDOListId.setAdInfoPosition(locationId);
                            }
                            advertisingConfigReqVo.setDuration(adDTO.getDuration());
                            if(adDTO.getRuler().equals(1)&&!StringUtils.isEmpty(advertisingVO.getMemberId())){
                                String mKey = MEMBER_AD+adDTO.getId() + businessId + adDTO.getRuler()+":" + advertisingVO.getMemberId();
                                Object cacheObject = RedissonUtils.getCacheObject(mKey);
                                if (!ObjectUtil.isEmpty(cacheObject)) {
                                    imageDOList.addAll(new ArrayList<>());
                                }else{
                                    imageDOList.addAll(imageDOListIds);
                                }
                            } else if (adDTO.getRuler().equals(2)&&!StringUtils.isEmpty(advertisingVO.getMemberId())) {
                                String mKey = MEMBER_AD+adDTO.getId() + businessId + adDTO.getRuler()+":" + advertisingVO.getMemberId();
                                Object cacheObject = RedissonUtils.getCacheObject(mKey);
                                LocalDate today = LocalDate.now();
                                Integer dayOfMonth = today.getDayOfMonth();
                                if (!ObjectUtil.isEmpty(cacheObject)) {
                                    if(dayOfMonth.equals(cacheObject)){
                                        imageDOList.addAll(new ArrayList<>());
                                    }else{
                                        imageDOList.addAll(imageDOListIds);
                                    }
                                }else{
                                    imageDOList.addAll(imageDOListIds);
                                }
                            } else{
                                imageDOList.addAll(imageDOListIds);
                            }



                            advertisingConfigReqVo.setPositionType(adDTO.getPositionType());
                            advertisingConfigReqVo.setDuration(adDTO.getDuration());
                        }

                        advertisingConfigReqVo.setAdInfoPosition(locationId);
                        advertisingConfigReqVo.setImageDOList(imageDOList);
                        advertisingConfigReqVo.setCurrentTime(new Date());

                        outegerListMap.put(locationId, adDTOS);
                        configReqVoList.add(advertisingConfigReqVo);


                    }
                    if(!StringUtils.isEmpty(advertisingVO.getMemberId())){
                        for (AdvertisingConfigReqVo advertisingConfigReqVo : configReqVoList) {
                            List<AdvertisingImageDO> imageDOList = advertisingConfigReqVo.getImageDOList();

                            for (AdvertisingImageDO advertisingImageDO : imageDOList) {
                                if(!advertisingImageDO.getRuler().equals(3)){
                                    LocalDate today = LocalDate.now();
                                    Integer dayOfMonth = today.getDayOfMonth();
                                    String mKey = MEMBER_AD+advertisingImageDO.getAdvertisingId() + businessId + advertisingImageDO.getRuler()+":" + advertisingVO.getMemberId();
                                    RedissonUtils.setCacheObject(mKey, dayOfMonth);
                                }

                            }
                        }
                    }
                }

                return configReqVoList;
            } else {
                return new ArrayList<>();
            }
        } else {
            String adKey = APPLET_AD + businessId + ":" + storeId;
            // 没有进到门店直接访问页面
            try {
                Object o = RedissonUtils.getCacheObject(adKey);
                if (ObjectUtil.isEmpty(o)) {

                    integerListMap = appletGetAllAdvertisingTwo(businessId);
                    if (ObjectUtil.isEmpty(integerListMap)) {
                        AdvertisingDTO advertisingDTO = new AdvertisingDTO();
                        // redisTemplate.opsForValue().set(adKey, advertisingDTO);
                        RedissonUtils.setCacheObject(adKey, advertisingDTO);
                        return new ArrayList<>();
                    } else {
                        AdvertisingDTO advertisingDTO = new AdvertisingDTO();
                        advertisingDTO.setIntegerListMap(integerListMap);
                        // redisTemplate.opsForValue().set(adKey, JSONObject.toJSONString(advertisingDTO));
                        RedissonUtils.setCacheObject(adKey, JSONObject.toJSONString(advertisingDTO));
                    }
                    matchAdvertisement(integerListMap, integerListMap);

                } else {
                    AdvertisingDTO r = com.alibaba.fastjson2.JSONObject.parseObject(o.toString(), AdvertisingDTO.class);
                    Map<Integer, List<AdvertisingRespVO>> rIntegerListMap = r.getIntegerListMap();
                    matchAdvertisement(rIntegerListMap, integerListMap);

                }
            } catch (Exception e) {
                Map<Integer, List<AdvertisingRespVO>> rintegerListMap = appletGetAllAdvertising(storeId, businessId);
                matchAdvertisement(rintegerListMap, integerListMap);

            }
            if (ObjectUtil.isNotEmpty(advertisingVO.getLocationIds())) {
                for (Integer locationId : advertisingVO.getLocationIds()) {
                    List<AdvertisingRespVO> adDTOS = integerListMap.get(locationId);
                    if (ObjectUtil.isNotEmpty(adDTOS)) {
                        List<AdvertisingImageDO> imageDOList = new ArrayList<>();
                        AdvertisingConfigReqVo advertisingConfigReqVo = new AdvertisingConfigReqVo();
                        for (AdvertisingRespVO adDTO : adDTOS) {
                            List<AdvertisingImageDO> imageDOListIds = adDTO.getImageDOList();

                            if(ObjectUtil.isNotEmpty(adDTO.getTriggerCondition())){
                                if(adDTO.getTriggerCondition().equals(5)){
                                    if(ObjectUtil.isNotEmpty(advertisingVO.getMemberId())){
                                        List<Object> objectList = new ArrayList<>();
                                        List<Long> crowdIds = adDTO.getCrowdIds();
                                        for (Long crowdId : crowdIds) {
                                            int shardIndex = (int) (advertisingVO.getMemberId() % 10);
                                            String shardKey = "member_crowd:" + crowdId + "_shard_" + shardIndex;
                                            Object hashValue = redisCache.getHashValue(shardKey, advertisingVO.getMemberId().toString());
                                            if(ObjectUtil.isNotEmpty(hashValue)){
                                                objectList.add(hashValue);
                                            }
                                        }
                                        if(ObjectUtil.isEmpty(objectList)){
                                            continue;
                                        }
                                    }

                                }
                            }

                            // 如果 appSource 等于 2，则删除 jumpLocation = 5 的数据
                            if (advertisingVO.getAppSource() != null && advertisingVO.getAppSource() == 2) {
                                imageDOListIds.removeIf(imageDO -> imageDO.getJumpLocation() != null && imageDO.getJumpLocation() == 5);
                            }

                            for (AdvertisingImageDO imageDOListId : imageDOListIds) {
                                imageDOListId.setRuler(adDTO.getRuler());
                                imageDOListId.setTriggerCondition(adDTO.getTriggerCondition());
                                imageDOListId.setFloatingWindowDisplay(adDTO.getFloatingWindowDisplay());
                                imageDOListId.setPopStyle(adDTO.getPopStyle());
                                imageDOListId.setArrangeMethod(adDTO.getArrangeMethod());
                                imageDOListId.setAdInfoPosition(locationId);
                            }
                            advertisingConfigReqVo.setDuration(adDTO.getDuration());
                            if(adDTO.getRuler().equals(1)&&!StringUtils.isEmpty(advertisingVO.getMemberId())){
                                String mKey = MEMBER_AD+adDTO.getId() + businessId + adDTO.getRuler()+":" + advertisingVO.getMemberId();
                                Object cacheObject = RedissonUtils.getCacheObject(mKey);
                                if (!ObjectUtil.isEmpty(cacheObject)) {
                                    imageDOList.addAll(new ArrayList<>());
                                }else{
                                    imageDOList.addAll(imageDOListIds);
                                }
                            }else if (adDTO.getRuler().equals(2)&&!StringUtils.isEmpty(advertisingVO.getMemberId())) {
                                String mKey = MEMBER_AD+adDTO.getId() + businessId + adDTO.getRuler()+":" + advertisingVO.getMemberId();
                                Object cacheObject = RedissonUtils.getCacheObject(mKey);
                                LocalDate today = LocalDate.now();
                                Integer dayOfMonth = today.getDayOfMonth();
                                if (!ObjectUtil.isEmpty(cacheObject)) {
                                    if(dayOfMonth.equals(cacheObject)){
                                        imageDOList.addAll(new ArrayList<>());
                                    }else{
                                        imageDOList.addAll(imageDOListIds);
                                    }
                                }else{
                                    imageDOList.addAll(imageDOListIds);
                                }
                            }else{
                                imageDOList.addAll(imageDOListIds);
                            }

//                            imageDOList.addAll(imageDOListIds);
                            advertisingConfigReqVo.setPositionType(adDTO.getPositionType());
                            advertisingConfigReqVo.setDuration(adDTO.getDuration());
                        }

                        advertisingConfigReqVo.setAdInfoPosition(locationId);
                        advertisingConfigReqVo.setImageDOList(imageDOList);
                        advertisingConfigReqVo.setCurrentTime(new Date());
                        configReqVoList.add(advertisingConfigReqVo);

                    }

                    if(!StringUtils.isEmpty(advertisingVO.getMemberId())){
                        for (AdvertisingConfigReqVo advertisingConfigReqVo : configReqVoList) {
                            List<AdvertisingImageDO> imageDOList = advertisingConfigReqVo.getImageDOList();

                            for (AdvertisingImageDO advertisingImageDO : imageDOList) {
                                if(!advertisingImageDO.getRuler().equals(3)){
                                    Date date = new Date();
                                    String mKey = MEMBER_AD+advertisingImageDO.getAdvertisingId() + businessId + advertisingImageDO.getRuler()+":" + advertisingVO.getMemberId();
                                    RedissonUtils.setCacheObject(mKey, date);
                                }

                            }
                        }
                    }

                }
                return configReqVoList;
            } else {
                return new ArrayList<>();
            }

        }

    }


    private void matchAdvertisement(Map<Integer, List<AdvertisingRespVO>> rIntegerListMap, Map<Integer, List<AdvertisingRespVO>> integerListMap) {
        for (Integer i : rIntegerListMap.keySet()) {

            List<AdvertisingRespVO> allList = new ArrayList<>();
            List<AdvertisingRespVO> advertisingRespVOS = rIntegerListMap.get(i);
            if (!StringUtils.isEmpty(advertisingRespVOS)) {
                for (AdvertisingRespVO advertisingRespVO : advertisingRespVOS) {

                    if (advertisingRespVO.getIsAllTime().equals(1)) {
                        allList.add(advertisingRespVO);
                    } else if (advertisingRespVO.getIsAllTime().equals(2) && advertisingRespVO.getShowTimeType().equals(1)) {
                        int a = 0;
                        // 获取当前日期
                        Integer currDate = Integer.parseInt(DateUtils.getDate().replace("-", ""));
                        if (ObjectUtil.isNotEmpty(advertisingRespVO.getStartTime())) {
                            Integer begin = Integer.parseInt(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, advertisingRespVO.getStartTime()).replace("-", ""));
                            // 结束日期

                            Integer end = Integer.parseInt(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, advertisingRespVO.getEndTime()).replace("-", ""));
                            if (begin <= currDate && currDate <= end) {
                                Calendar now = Calendar.getInstance();
                                now.setTime(new Date());
                                // 当前日
                                int day = now.get(Calendar.DAY_OF_MONTH);
                                // 当前周几
                                boolean isFirstSunday = (now.getFirstDayOfWeek() == Calendar.SUNDAY);
                                // 获取周几
                                int weekDay = now.get(Calendar.DAY_OF_WEEK);
                                // 若一周第一天为星期天，则-1
                                if (isFirstSunday) {
                                    weekDay = weekDay - 1;
                                    if (weekDay == 0) {
                                        weekDay = 7;
                                    }
                                }
                                if (ObjectUtil.isNotEmpty(advertisingRespVO.getDayNumberList())) {
                                    if (advertisingRespVO.getDayNumberList().contains(day)) {
//                                        BuildData(advertisingRespVO);
                                        allList.add(advertisingRespVO);
                                        a++;
                                    }
                                }
                                if (ObjectUtil.isNotEmpty(advertisingRespVO.getWeekNumberList())) {
                                    if (advertisingRespVO.getWeekNumberList().contains(weekDay)) {
                                        if (a == 0) {
//                                            BuildData(advertisingRespVO);
                                            allList.add(advertisingRespVO);

                                        }
                                    }
                                }

                                if(ObjectUtil.isEmpty(advertisingRespVO.getDayNumberList()) && ObjectUtil.isEmpty(advertisingRespVO.getWeekNumberList())){
                                    allList.add(advertisingRespVO);
                                }


                            }

                        } else {
                            Calendar now = Calendar.getInstance();
                            now.setTime(new Date());
                            // 当前日
                            int day = now.get(Calendar.DAY_OF_MONTH);
                            // 当前周几
                            boolean isFirstSunday = (now.getFirstDayOfWeek() == Calendar.SUNDAY);
                            // 获取周几
                            int weekDay = now.get(Calendar.DAY_OF_WEEK);
                            // 若一周第一天为星期天，则-1
                            if (isFirstSunday) {
                                weekDay = weekDay - 1;
                                if (weekDay == 0) {
                                    weekDay = 7;
                                }
                            }
                            if (ObjectUtil.isNotEmpty(advertisingRespVO.getDayNumberList())) {
                                if (advertisingRespVO.getDayNumberList().contains(day)) {
//                                    BuildData(advertisingRespVO);
                                    allList.add(advertisingRespVO);
                                    a++;
                                }
                            }
                            if (ObjectUtil.isNotEmpty(advertisingRespVO.getWeekNumberList())) {
                                if (advertisingRespVO.getWeekNumberList().contains(weekDay)) {
                                    if (a == 0) {
//                                        BuildData(advertisingRespVO);
                                        allList.add(advertisingRespVO);

                                    }
                                }
                            }

                        }
                    } else if (advertisingRespVO.getIsAllTime().equals(2) && advertisingRespVO.getShowTimeType().equals(2)) {
                        int b = 0;
                        // 获取当前日期
                        Integer currDate = Integer.parseInt(DateUtils.getDate().replace("-", ""));
                        if (ObjectUtil.isNotEmpty(advertisingRespVO.getStartTime())) {
                            Integer begin = Integer.parseInt(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, advertisingRespVO.getStartTime()).replace("-", ""));
                            // 结束日期

                            Integer end = Integer.parseInt(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, advertisingRespVO.getEndTime()).replace("-", ""));
                            if (begin <= currDate && currDate <= end) {
                                Calendar now = Calendar.getInstance();
                                now.setTime(new Date());
                                // 当前日
                                int day = now.get(Calendar.DAY_OF_MONTH);
                                // 当前周几

                                boolean isFirstSunday = (now.getFirstDayOfWeek() == Calendar.SUNDAY);
                                // 获取周几
                                int weekDay = now.get(Calendar.DAY_OF_WEEK);
                                // 若一周第一天为星期天，则-1
                                if (isFirstSunday) {
                                    weekDay = weekDay - 1;
                                    if (weekDay == 0) {
                                        weekDay = 7;
                                    }
                                }
                                if (ObjectUtil.isNotEmpty(advertisingRespVO.getDayNumberList())) {
                                    if (advertisingRespVO.getDayNumberList().contains(day)) {
                                        extracted(advertisingRespVO, now, allList);
                                        b++;
                                    }
                                }
                                if (ObjectUtil.isNotEmpty(advertisingRespVO.getWeekNumberList())) {
                                    if (advertisingRespVO.getWeekNumberList().contains(weekDay)) {
                                        if (b == 0) {
                                            extracted(advertisingRespVO, now, allList);
                                            b++;
                                        }
                                    }
                                }


                                if (b == 0) {
                                    extracted(advertisingRespVO, now, allList);
                                }

                            }
                        } else {
                            Calendar now = Calendar.getInstance();
                            now.setTime(new Date());
                            // 当前日
                            int day = now.get(Calendar.DAY_OF_MONTH);
                            // 当前周几

                            boolean isFirstSunday = (now.getFirstDayOfWeek() == Calendar.SUNDAY);
                            // 获取周几
                            int weekDay = now.get(Calendar.DAY_OF_WEEK);
                            // 若一周第一天为星期天，则-1
                            if (isFirstSunday) {
                                weekDay = weekDay - 1;
                                if (weekDay == 0) {
                                    weekDay = 7;
                                }
                            }
                            if (ObjectUtil.isNotEmpty(advertisingRespVO.getDayNumberList())) {
                                if (advertisingRespVO.getDayNumberList().contains(day)) {
                                    extracted(advertisingRespVO, now, allList);
                                    b++;
                                }
                            }
                            if (ObjectUtil.isNotEmpty(advertisingRespVO.getWeekNumberList())) {
                                if (advertisingRespVO.getWeekNumberList().contains(weekDay)) {
                                    if (b == 0) {
                                        extracted(advertisingRespVO, now, allList);
                                        b++;
                                    }
                                }
                            }
                            if (b == 0) {
                                extracted(advertisingRespVO, now, allList);
                            }
                        }
                    }
                }
                if(ObjectUtil.isNotEmpty(allList)){
                    allList = allList.stream()
                            .sorted(Comparator.comparing(AdvertisingRespVO::getAdvertisingSort,
                                            Comparator.nullsLast(Comparator.naturalOrder()))
                                    .thenComparing(AdvertisingRespVO::getCreateTime,
                                            Comparator.nullsLast(Comparator.reverseOrder())))
                            .collect(Collectors.toList());
                }

                List<AdvertisingRespVO> advertisingRespVOList = new ArrayList<>();
                AdvertisingRespVO advertisingRespVO = new AdvertisingRespVO();
                List<AdvertisingImageDO> imageDOList = new ArrayList<>();
                // 广告图片不能超过5个
                Integer count = 0;
                for (AdvertisingRespVO respVO : allList) {
                    count = count + respVO.getImagesCount();
                }
                if (!i.equals(AdvertisingPositionStateEnum.SPLASH_SCREEN_ADVERTISEMENT.getStatus()) || !i.equals(AdvertisingPositionStateEnum.FLOATING_WINDOW.getStatus())) {
                    if (count <= 10) {


//                        for (AdvertisingRespVO respVO : allList) {
//                            imageDOList.addAll(respVO.getImageDOList());
//                        }
//                        advertisingRespVO.setImageDOList(imageDOList);
//                        advertisingRespVOList.add(advertisingRespVO);
                        integerListMap.put(i, allList);
                    } else {
                        List<AdvertisingRespVO> aList = new ArrayList<>();
                        Integer aa = 0;
                        for (AdvertisingRespVO respVO : allList) {
                            aa = aa + respVO.getImagesCount();

                            if (aa == 10) {
                                aList.add(respVO);
                                break;
                            } else if (aa > 10) {
                                List<AdvertisingImageDO> newImages = new ArrayList<>();
                                List<AdvertisingImageDO> imageDOLists = respVO.getImageDOList();
                                Integer bb = aa - imageDOLists.size();
                                Integer cc = 10 - bb;
                                for (int j = 0; j < cc; j++) {
                                    AdvertisingImageDO advertisingImageDO = imageDOLists.get(j);
                                    newImages.add(advertisingImageDO);
                                }
                                respVO.setImageDOList(newImages);
                                respVO.setImagesCount(newImages.size());
                                aList.add(respVO);
                                break;
                            }
                            aList.add(respVO);
                        }

//                        for (AdvertisingRespVO respVO : aList) {
//                            imageDOList.addAll(respVO.getImageDOList());
//                        }
//                        advertisingRespVO.setImageDOList(imageDOList);
//                        advertisingRespVOList.add(advertisingRespVO);

                        integerListMap.put(i, aList);
//                        integerListMap.put(i,aList);
                    }
                } else {
                    integerListMap.put(i, new ArrayList<>());
                }

            } else {
//                List<AdvertisingRespVO> advertisingRespVOList = new ArrayList<>();
//                AdvertisingRespVO advertisingRespVO = new AdvertisingRespVO();
//                List<AdvertisingImageDO> imageDOList = new ArrayList<>();
//                for (AdvertisingRespVO respVO : allList) {
//                    imageDOList.addAll(respVO.getImageDOList());
//                }
//
//                advertisingRespVO.setImageDOList(imageDOList);
//                advertisingRespVOList.add(advertisingRespVO);
                integerListMap.put(i, allList);

            }

        }
    }

    @Override
    public PageResult<StorePageResVO> selectByStoreList(AdvertisingStorePageReqVO storePageReqVO) {
        if (!StringUtils.isEmpty(storePageReqVO.getAdvertisingId())) {
            LambdaQueryWrapper<AdvertisingForStoreDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AdvertisingForStoreDO::getAdvertisingId, storePageReqVO.getAdvertisingId());
            List<AdvertisingForStoreDO> storeDOS = advertisingForStoreService.list(wrapper);
            if (storeDOS.size() > 0) {
                List<Long> collect = storeDOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                storePageReqVO.setStoreIds(collect);
            }

        } else {
            storePageReqVO.setStoreIds(new ArrayList<>());
        }
        StorePageListReqVO storePageListReqVO = new StorePageListReqVO();
        BeanUtils.copyProperties(storePageReqVO, storePageListReqVO);
        Long userId = WebFrameworkUtils.getLoginUserId();
        if(!StringUtils.isEmpty(userId)){
            storePageListReqVO.setUserIds(userId);
        }
        CommonResult<PageResult<StorePageResVO>> pageResultCommonResult = storeInfoApi.chooseStore(storePageListReqVO);
        PageResult<StorePageResVO> data = pageResultCommonResult.getData();
        return data;
    }

    @Override
    public List<StoreInfoDTO> selectCheckedStoreList(AdvertisingStorePageReqVO storePageReqVO) {
        Long advertisingId = storePageReqVO.getAdvertisingId();
        List<AdvertisingForStoreDO> storeDOS = advertisingForStoreService.list(new LambdaQueryWrapper<AdvertisingForStoreDO>().eq(AdvertisingForStoreDO::getAdvertisingId, advertisingId));
        if (storeDOS.size() > 0) {
            List<Long> collect = storeDOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
            CommonResult<List<StoreInfoDTO>> storesByStoreIds = storeApi.getStoresByStoreIds(collect);
            List<StoreInfoDTO> dataList = storesByStoreIds.getData();
            return dataList;
        }

        return new ArrayList<>();
    }


    // 获取门店下所有符合条件的广告
    public List<AdvertisingRespVO> getAdByStoreId(Long storeId, Long businessId) {
        List<AdvertisingRespVO> allList = new ArrayList<>();

        // 查找该门店的广告
        List<Long> adIds = new ArrayList<>();

        // 查找符合全门店的的现在时间符合的广告
        LambdaQueryWrapper<AdvertisingDO> lq = new LambdaQueryWrapper<>();
        lq.eq(AdvertisingDO::getIsAllStores, 1);
        lq.eq(AdvertisingDO::getIsOpen, 1);
        lq.eq(AdvertisingDO::getApplicationScope, 1);
        lq.eq(AdvertisingDO::getBusinessId, businessId);

        List<AdvertisingDO> adList = this.list(lq);
        if (ObjectUtil.isNotEmpty(storeId)) {
            LambdaQueryWrapper<AdvertisingForStoreDO> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AdvertisingForStoreDO::getStoreId, storeId);
            lqw.eq(AdvertisingForStoreDO::getBusinessId, businessId);
            List<AdvertisingForStoreDO> list = advertisingForStoreService.list(lqw);
            // 拿到特指该门店的广告 id
            if (ObjectUtil.isNotEmpty(list)) {
                adIds = list.stream().map(AdvertisingForStoreDO::getAdvertisingId).distinct().collect(Collectors.toList());
            }

            //查询符合标签的门店列标
            LambdaQueryWrapper<AdvertisingDO> doLambdaQueryWrapper = new LambdaQueryWrapper<>();
            doLambdaQueryWrapper.eq(AdvertisingDO::getIsOpen, 1);
            doLambdaQueryWrapper.eq(AdvertisingDO::getApplicationScope, 2);
            doLambdaQueryWrapper.eq(AdvertisingDO::getBusinessId, businessId);
            List<AdvertisingDO> tagAdvertisingList = this.list(doLambdaQueryWrapper);

            if(ObjectUtil.isNotEmpty(tagAdvertisingList)){
                for (AdvertisingDO advertisingDO : tagAdvertisingList) {
                    List<Long> tagList = advertisingDO.getTagList();
                    if(ObjectUtil.isNotEmpty(tagList)){
                        for (Long tag : tagList) {
                            Boolean flag = storeApi.isTag(storeId, tag);
                            if(flag){
                                adList.add(advertisingDO);
                                break;
                            }

                        }

                    }

                }

            }
        }
        // 再把这个符合该门店广告放一起
        if (ObjectUtil.isNotEmpty(adIds)) {
            LambdaQueryWrapper<AdvertisingDO> lq2 = new LambdaQueryWrapper<>();
            lq2.in(AdvertisingDO::getId, adIds);
            lq2.eq(AdvertisingDO::getIsOpen, 1);
            lq2.eq(AdvertisingDO::getBusinessId, businessId);
            adList.addAll(this.list(lq2));
        }

        for (AdvertisingDO advertisingDO : adList) {
            AdvertisingRespVO advertisingRespVO = BuildDataTwo(advertisingDO);
            allList.add(advertisingRespVO);


        }

        return allList;
    }


    private AdvertisingRespVO BuildDataTwo(AdvertisingDO advertisingDO) {
        AdvertisingRespVO advertisingRespVO = new AdvertisingRespVO();
        BeanUtils.copyProperties(advertisingDO, advertisingRespVO);
        LambdaQueryWrapper<AdvertisingImageDO> imageDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        imageDOLambdaQueryWrapper.eq(AdvertisingImageDO::getAdvertisingId, advertisingRespVO.getId());
        List<AdvertisingImageDO> advertisingImageDOS = advertisingImageMapper.selectList(imageDOLambdaQueryWrapper);
        advertisingRespVO.setImageDOList(advertisingImageDOS);
        advertisingRespVO.setImagesCount(advertisingImageDOS.size());
        LambdaQueryWrapper<AdvertisingTimeDO> timeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        timeDOLambdaQueryWrapper.eq(AdvertisingTimeDO::getAdvertisingId, advertisingRespVO.getId());
        List<AdvertisingTimeDO> advertisingTimeDOS = advertisingTimeMapper.selectList(timeDOLambdaQueryWrapper);
        advertisingRespVO.setTimeVOList(advertisingTimeDOS);
        return advertisingRespVO;

    }

    private void extracted(AdvertisingRespVO advertisingDO, Calendar now, List<AdvertisingRespVO> allList) {
        int parseInt = 0;
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        if (minute < 10) {
            String h = String.valueOf(hour);
            String m = "0" + String.valueOf(minute);
            parseInt = Integer.parseInt(h + m);
        } else {
            String h = String.valueOf(hour);
            String m = String.valueOf(minute);
            parseInt = Integer.parseInt(h + m);
        }

        LambdaQueryWrapper<AdvertisingTimeDO> timeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        timeDOLambdaQueryWrapper.eq(AdvertisingTimeDO::getAdvertisingId, advertisingDO.getId());
        List<AdvertisingTimeDO> advertisingTimeDOS = advertisingTimeMapper.selectList(timeDOLambdaQueryWrapper);
        for (AdvertisingTimeDO advertisingTimeDO : advertisingTimeDOS) {
            String startTimeSegment = advertisingTimeDO.getStartTimeSegment();
            String endTimeSegment = advertisingTimeDO.getEndTimeSegment();
            String startNum = startTimeSegment.replace(":", "");
            String endNum = endTimeSegment.replace(":", "");
            int parseInt1 = Integer.parseInt(startNum);
            int parseInt2 = Integer.parseInt(endNum);
            if (parseInt1 < parseInt && parseInt2 > parseInt) {
                allList.add(advertisingDO);
            }
        }
    }

    // 门店获取广告
    public Map<Integer, List<AdvertisingRespVO>> appletGetAllAdvertising(Long storeId, Long businessId) {
        Map<Integer, List<AdvertisingRespVO>> map = new HashMap<>();
        // 通过门店  获取所有的广告
        List<AdvertisingRespVO> adList = getAdByStoreId(storeId, businessId);
        // 通过门店  获取所有的广告
        if (ObjectUtil.isNotEmpty(adList)) {
            // 获取到每个位置的值分组列表
            Map<Integer, List<AdvertisingRespVO>> collect = adList.stream().collect(Collectors.groupingBy(AdvertisingRespVO::getAdInfoPosition));
            // 拿到了该门店下所有位置
            List<Integer> adPosition = adList.stream().map(AdvertisingRespVO::getAdInfoPosition).distinct().collect(Collectors.toList());

            // 如果有浮窗
            List<Integer> locationForLast = new ArrayList<>();

            for (Integer i : adPosition) {
                locationForLast.add(i);
                // getAdPosition 字段的浮窗位置是 7
//                if (!i.equals(AdvertisingPositionStateEnum.FLOATING_WINDOW.getStatus())){
//
//                }
            }
            for (Integer i : locationForLast) {
                List<AdvertisingRespVO> advertisings1 = collect.get(i);
                if (ObjectUtil.isNotEmpty(advertisings1)) {
                    advertisings1.sort(new Comparator<AdvertisingRespVO>() {
                        @Override
                        public int compare(AdvertisingRespVO o1, AdvertisingRespVO o2) {
                            return o2.getCreateTime().compareTo(o1.getCreateTime());
                        }
                    });
                    map.put(i, advertisings1);
                }

            }


            if (adPosition.contains(AdvertisingPositionStateEnum.FLOATING_WINDOW.getStatus())) {
                // 如果有浮窗
                List<AdvertisingRespVO> advertisings = collect.get(AdvertisingPositionStateEnum.FLOATING_WINDOW.getStatus());
                if (!StringUtils.isEmpty(advertisings)) {
                    if (advertisings.size() > 0) {
                        for (AdvertisingRespVO advertising : advertisings) {
                            LambdaQueryWrapper<AdvertisingFloatDO> wrapper = new LambdaQueryWrapper<>();
                            wrapper.eq(AdvertisingFloatDO::getAdvertisingId, advertising.getId());
                            List<AdvertisingFloatDO> list = advertisingFloatService.list(wrapper);
                            if (ObjectUtil.isNotEmpty(list)) {
                                advertising.setFloatingWindowDisplay(list.stream().map(mm -> mm.getFloatingWindowDisplay()).collect(Collectors.toList()));
                            }
                        }
                    }

                }
//                //拿到浮窗的 id
//                List<Long> fuAdIds = advertisings.stream().map(AdvertisingRespVO::getId).collect(Collectors.toList());
//                LambdaQueryWrapper<AdvertisingFloatDO> floatLambdaQueryWrapper = new LambdaQueryWrapper<>();
//                floatLambdaQueryWrapper.in(AdvertisingFloatDO::getAdvertisingId,fuAdIds);
//                List<AdvertisingFloatDO> list = advertisingFloatService.list(floatLambdaQueryWrapper);
//                Map<Long, List<AdvertisingFloatDO>> collect1 = list.stream().collect(Collectors.groupingBy(AdvertisingFloatDO::getAdvertisingId));
//                for (AdvertisingRespVO advertising : advertisings) {
//
//                    //如果该浮窗的位置不是全部
//                    //查询到该门店的浮窗设置
//                    List<AdvertisingFloatDO> advertisingFloats = collect1.get(advertising.getId());
//                    //拿到这个浮窗的所有位置
//                    List<Integer> list1 = advertisingFloats.stream().map(AdvertisingFloatDO::getFloatingWindowDisplay).collect(Collectors.toList());
//                    for (Integer i : list1) {
//                        //这里面可能会有前面 map 没有的页面
//                        List<AdvertisingRespVO> adDTOS = map.get(i);
//                        if (ObjectUtil.isNotEmpty(adDTOS)){
//                            adDTOS.add(advertising);
//                        }else {
//                            List<AdvertisingRespVO> adDTOS1 = new ArrayList<>();
//                            adDTOS1.add(advertising);
//                            map.put(i,adDTOS1);
//                        }
//                    }
////                    if (advertising.getFloatingIsAll().equals(2)){
////
////
////
////                    }else {
////                        //如果该浮窗的位置是全部
////
////
////                        for (Integer i : locationForLast) {
////                            List<AdvertisingRespVO> adDTOS = map.get(i);
////
////                            adDTOS.add(advertising);
////                        }
////
////                        List<Integer> allLocation = Arrays.asList(1, 2, 3, 4, 5, 6);
////                        List<Integer> is = new ArrayList<>();
////                        for (Integer i : allLocation) {
////                            if (!locationForLast.contains(i)){
////                                is.add(i);
////                            }
////                        }
////                        // allLocation.removeAll(locationForLast);
////                        if (ObjectUtil.isNotEmpty(is)){
////                            List<AdvertisingRespVO> adDTOS = new ArrayList<>();
////                            for (Integer i : is) {
////                                adDTOS.add(advertising);
////                                map.put(i,adDTOS);
////                            }
////                        }
////
////                    }
//                }
            }

        }
//        if (ObjectUtil.isNotEmpty(adList)){
//            //获取到每个位置的值分组列表
//            Map<Integer, List<AdvertisingRespVO>> collect = adList.stream().collect(Collectors.groupingBy(AdvertisingRespVO::getAdInfoPosition));
//            //拿到了该门店下所有位置
//            List<Integer> adPosition = adList.stream().map(AdvertisingRespVO::getAdInfoPosition).distinct().collect(Collectors.toList());
//
//            //如果有浮窗
//            List<Integer> locationForLast = new ArrayList<>();
//
//            for (Integer i : adPosition) {
//                //getAdPosition 字段的浮窗位置是 7
//                if (!i.equals(AdvertisingPositionStateEnum.FLOATING_WINDOW.getStatus())){
//                    locationForLast.add(i);
//                }
//            }
//            for (Integer i : locationForLast) {
//                List<AdvertisingRespVO> advertisings1 = collect.get(i);
//                if (ObjectUtil.isNotEmpty(advertisings1)){
//                    advertisings1.sort(new Comparator<AdvertisingRespVO>() {
//                        @Override
//                        public int compare(AdvertisingRespVO o1, AdvertisingRespVO o2) {
//                            return o2.getCreateTime().compareTo(o1.getCreateTime());
//                        }
//                    });
//                    map.put(i, advertisings1);
//                }
//
//            }
//
//
//            if (adPosition.contains(AdvertisingPositionStateEnum.FLOATING_WINDOW.getStatus())){
//                //如果有浮窗
//                List<AdvertisingRespVO> advertisings = collect.get(AdvertisingPositionStateEnum.FLOATING_WINDOW.getStatus());
//                //拿到浮窗的 id
//                List<Long> fuAdIds = advertisings.stream().map(AdvertisingRespVO::getId).collect(Collectors.toList());
//                LambdaQueryWrapper<AdvertisingFloatDO> floatLambdaQueryWrapper = new LambdaQueryWrapper<>();
//                floatLambdaQueryWrapper.in(AdvertisingFloatDO::getAdvertisingId,fuAdIds);
//                List<AdvertisingFloatDO> list = advertisingFloatService.list(floatLambdaQueryWrapper);
//                Map<Long, List<AdvertisingFloatDO>> collect1 = list.stream().collect(Collectors.groupingBy(AdvertisingFloatDO::getAdvertisingId));
//                for (AdvertisingRespVO advertising : advertisings) {
//
//                    if (advertising.getFloatingIsAll().equals(2)){
//                        //如果该浮窗的位置不是全部
//                        //查询到该门店的浮窗设置
//                        List<AdvertisingFloatDO> advertisingFloats = collect1.get(advertising.getId());
//                        //拿到这个浮窗的所有位置
//                        List<Integer> list1 = advertisingFloats.stream().map(AdvertisingFloatDO::getFloatingWindowDisplay).collect(Collectors.toList());
//                        for (Integer i : list1) {
//                            //这里面可能会有前面 map 没有的页面
//                            List<AdvertisingRespVO> adDTOS = map.get(i);
//                            if (ObjectUtil.isNotEmpty(adDTOS)){
//                                adDTOS.add(advertising);
//                            }else {
//                             List<AdvertisingRespVO> adDTOS1 = new ArrayList<>();
//                                adDTOS1.add(advertising);
//                                map.put(i,adDTOS1);
//                            }
//                        }
//
//
//                    }else {
//                        //如果该浮窗的位置是全部
//
//
//                        for (Integer i : locationForLast) {
//                            List<AdvertisingRespVO> adDTOS = map.get(i);
//
//                            adDTOS.add(advertising);
//                        }
//
//                        List<Integer> allLocation = Arrays.asList(1, 2, 3, 4, 5, 6);
//                        List<Integer> is = new ArrayList<>();
//                        for (Integer i : allLocation) {
//                            if (!locationForLast.contains(i)){
//                                is.add(i);
//                            }
//                        }
//                       // allLocation.removeAll(locationForLast);
//                        if (ObjectUtil.isNotEmpty(is)){
//                            List<AdvertisingRespVO> adDTOS = new ArrayList<>();
//                            for (Integer i : is) {
//
//
//                                adDTOS.add(advertising);
//                                map.put(i,adDTOS);
//                            }
//                        }
//
//                    }
//                }
//            }
//
//        }


        return map;
    }


    public Map<Integer, List<AdvertisingRespVO>> appletGetAllAdvertisingTwo(Long buId) {
        List<AdvertisingRespVO> adList = new ArrayList<>();
        // 查找符合全门店的的现在时间符合的广告
        LambdaQueryWrapper<AdvertisingDO> lq = new LambdaQueryWrapper<>();
        lq.eq(AdvertisingDO::getIsAllStores, 1);
        lq.eq(AdvertisingDO::getIsOpen, 1);
        lq.eq(AdvertisingDO::getApplicationScope,1);
        lq.eq(AdvertisingDO::getBusinessId, buId);

        List<AdvertisingDO> lists = this.list(lq);
        for (AdvertisingDO advertisingDO : lists) {
            AdvertisingRespVO advertisingRespVO = BuildDataTwo(advertisingDO);
            adList.add(advertisingRespVO);
        }

        Map<Integer, List<AdvertisingRespVO>> map = new HashMap<>();
        // 通过门店  获取所有的广告
        if (ObjectUtil.isNotEmpty(adList)) {
            // 获取到每个位置的值分组列表
            Map<Integer, List<AdvertisingRespVO>> collect = adList.stream().collect(Collectors.groupingBy(AdvertisingRespVO::getAdInfoPosition));
            // 拿到了该门店下所有位置
            List<Integer> adPosition = adList.stream().map(AdvertisingRespVO::getAdInfoPosition).distinct().collect(Collectors.toList());

            // 如果有浮窗
            List<Integer> locationForLast = new ArrayList<>();

            for (Integer i : adPosition) {
                locationForLast.add(i);
                // getAdPosition 字段的浮窗位置是 7
//                if (!i.equals(AdvertisingPositionStateEnum.FLOATING_WINDOW.getStatus())){
//
//                }
            }
            for (Integer i : locationForLast) {
                List<AdvertisingRespVO> advertisings1 = collect.get(i);
                if (ObjectUtil.isNotEmpty(advertisings1)) {
                    advertisings1.sort(new Comparator<AdvertisingRespVO>() {
                        @Override
                        public int compare(AdvertisingRespVO o1, AdvertisingRespVO o2) {
                            return o2.getCreateTime().compareTo(o1.getCreateTime());
                        }
                    });
                    map.put(i, advertisings1);
                }

            }


            if (adPosition.contains(AdvertisingPositionStateEnum.FLOATING_WINDOW.getStatus())) {
                // 如果有浮窗
                List<AdvertisingRespVO> advertisings = collect.get(AdvertisingPositionStateEnum.FLOATING_WINDOW.getStatus());
                if (!StringUtils.isEmpty(advertisings)) {
                    if (advertisings.size() > 0) {
                        for (AdvertisingRespVO advertising : advertisings) {
                            LambdaQueryWrapper<AdvertisingFloatDO> wrapper = new LambdaQueryWrapper<>();
                            wrapper.eq(AdvertisingFloatDO::getAdvertisingId, advertising.getId());
                            List<AdvertisingFloatDO> list = advertisingFloatService.list(wrapper);
                            if (ObjectUtil.isNotEmpty(list)) {
                                advertising.setFloatingWindowDisplay(list.stream().map(mm -> mm.getFloatingWindowDisplay()).collect(Collectors.toList()));
                            }
                        }
                    }

                }
//                //拿到浮窗的 id
//                List<Long> fuAdIds = advertisings.stream().map(AdvertisingRespVO::getId).collect(Collectors.toList());
//                LambdaQueryWrapper<AdvertisingFloatDO> floatLambdaQueryWrapper = new LambdaQueryWrapper<>();
//                floatLambdaQueryWrapper.in(AdvertisingFloatDO::getAdvertisingId,fuAdIds);
//                List<AdvertisingFloatDO> list = advertisingFloatService.list(floatLambdaQueryWrapper);
//                Map<Long, List<AdvertisingFloatDO>> collect1 = list.stream().collect(Collectors.groupingBy(AdvertisingFloatDO::getAdvertisingId));
//                for (AdvertisingRespVO advertising : advertisings) {
//
//                    //如果该浮窗的位置不是全部
//                    //查询到该门店的浮窗设置
//                    List<AdvertisingFloatDO> advertisingFloats = collect1.get(advertising.getId());
//                    //拿到这个浮窗的所有位置
//                    List<Integer> list1 = advertisingFloats.stream().map(AdvertisingFloatDO::getFloatingWindowDisplay).collect(Collectors.toList());
//                    for (Integer i : list1) {
//                        //这里面可能会有前面 map 没有的页面
//                        List<AdvertisingRespVO> adDTOS = map.get(i);
//                        if (ObjectUtil.isNotEmpty(adDTOS)){
//                            adDTOS.add(advertising);
//                        }else {
//                            List<AdvertisingRespVO> adDTOS1 = new ArrayList<>();
//                            adDTOS1.add(advertising);
//                            map.put(i,adDTOS1);
//                        }
//                    }
////                    if (advertising.getFloatingIsAll().equals(2)){
////
////
////
////                    }else {
////                        //如果该浮窗的位置是全部
////
////
////                        for (Integer i : locationForLast) {
////                            List<AdvertisingRespVO> adDTOS = map.get(i);
////
////                            adDTOS.add(advertising);
////                        }
////
////                        List<Integer> allLocation = Arrays.asList(1, 2, 3, 4, 5, 6);
////                        List<Integer> is = new ArrayList<>();
////                        for (Integer i : allLocation) {
////                            if (!locationForLast.contains(i)){
////                                is.add(i);
////                            }
////                        }
////                        // allLocation.removeAll(locationForLast);
////                        if (ObjectUtil.isNotEmpty(is)){
////                            List<AdvertisingRespVO> adDTOS = new ArrayList<>();
////                            for (Integer i : is) {
////                                adDTOS.add(advertising);
////                                map.put(i,adDTOS);
////                            }
////                        }
////
////                    }
//                }
            }

        }

        return map;
    }


//    /**
//     * 新用户判断
//     * @param wxMember wxMember
//     * @return boolean
//     */
//    public boolean userTypeSevenDay(WxMember wxMember) {
//        // 获取当前时间
//        Date date = new Date();
//        Date registerTime = wxMember.getRegisterTime();
//        if (ObjectUtil.isEmpty(registerTime)) {
//            return true;
//        }
//        long diffInMillis = Math.abs(date.getTime() - registerTime.getTime());
//        // 获取两个日期时间的毫秒差值，取绝对值避免顺序影响结果
//        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
//        return diffInDays <= 7;
//    }
//
//    /**
//     * 老 用户判断
//     * @param userDate date
//     * @param userDate 天数
//     * @return boolean
//     */
//    public boolean userType(Date userDate) {
//        // 获取当前时间
//        if(ObjectUtil.isEmpty(userDate)){
//            return false;
//        }
//        Date date = new Date();
//        long diffInMillis = Math.abs(date.getTime() - userDate.getTime());
//        // 获取两个日期时间的毫秒差值，取绝对值避免顺序影响结果
//        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
//        return diffInDays > 7;
//    }
//
//    /**
//     * 回归用户判断
//     * @param wxMember wxMember
//     * @return boolean
//     */
//    public boolean userTypeThirtyDay(WxMember wxMember) {
//        Date finalOrderFinishTime = wxMember.getFinalOrderFinishTime();
//        Date registerTime = wxMember.getRegisterTime();
//        log.info("判断是否是回归用户,{},{}",finalOrderFinishTime,registerTime);
//        if(ObjectUtil.isEmpty(finalOrderFinishTime) || ObjectUtil.isEmpty(registerTime)){
//            return false;
//        }
//        // 获取当前时间
//        Date date = new Date();
//        long diffInMillis = Math.abs(date.getTime() - finalOrderFinishTime.getTime());
//        long registerMillis = Math.abs(date.getTime() - registerTime.getTime());
//        // 获取两个日期时间的毫秒差值，取绝对值避免顺序影响结果
//        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
//        long registerInDays = registerMillis / (1000 * 60 * 60 * 24);
//        return diffInDays > 14 && registerInDays > 7;
//    }


    @Override
    public void execAdvertising() {
        Long businessId = BusinessContextHolder.getBusinessId();

        LambdaQueryWrapper<AdvertisingDO> lq = new LambdaQueryWrapper<>();
        lq.eq(AdvertisingDO::getDeleted, 0);
        List<AdvertisingDO> list = this.list(lq);
        for (AdvertisingDO advertising : list) {
            Integer isOpen = getIsOpen(advertising);
            LambdaUpdateWrapper<AdvertisingDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(AdvertisingDO::getIsOpen, isOpen);
            updateWrapper.eq(AdvertisingDO::getId, advertising.getId());
            this.update(updateWrapper);

        }


        redisForAppletAd.delAllStore(businessId);


    }

    @Override
    public void deleteByStoreId(Long advertisingId, Long storeId) {
        LambdaQueryWrapper<AdvertisingForStoreDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdvertisingForStoreDO::getAdvertisingId, advertisingId);
        wrapper.eq(AdvertisingForStoreDO::getStoreId, storeId);
        advertisingForStoreService.remove(wrapper);
    }

    @Override
    public void deleteRedis() {
        Long businessId = BusinessContextHolder.getBusinessId();
        redisForAppletAd.delAllStore(businessId);
    }

    @Override
    @Transactional
    public Boolean advertisingDataConversion() {
        List<AdvertisingDO> advertisingDOS = advertisingMapper.selectList(null);
        advertisingDOS.parallelStream().forEach(this::processAdvertisingRecord);
        return true;
    }

    private void processAdvertisingRecord(AdvertisingDO advertisingDO) {
        try {
            updateAdvertisingRecord(advertisingDO);
            AdvertisingImageDO advertisingImageDO = createAdvertisingImage(advertisingDO);
            advertisingImageMapper.insert(advertisingImageDO);
            if(ObjectUtil.isNotEmpty(advertisingDO.getFloatingIsAll())){
                if(advertisingDO.getFloatingIsAll().equals(1)){
                    AdvertisingFloatDO advertisingFloatDO = new AdvertisingFloatDO();
                    advertisingFloatDO.setAdvertisingId(advertisingDO.getId());
                    advertisingFloatDO.setDeleted(false);
                    advertisingFloatDO.setFloatingWindowDisplay(0);
                    advertisingFloatDO.setBusinessId(10L);
                    advertisingFloatService.save(advertisingFloatDO);
                }
            }

        } catch (Exception e) {
        }
    }

    private void updateAdvertisingRecord(AdvertisingDO advertisingDO) {
        advertisingDO.setBusinessId(advertisingDO.getProjectOwnerShip());
        advertisingDO.setShowTimeType(1);
        advertisingDO.setTriggerCondition(ObjectUtil.isNotEmpty(advertisingDO.getTriggerCondition()) ?
                advertisingDO.getTriggerCondition() : 1);
        advertisingDO.setPopStyle(ObjectUtil.isNotEmpty(advertisingDO.getPopStyle()) ?
                advertisingDO.getPopStyle() : 1);
//        if(advertisingDO.getIsDelete().equals(2)){
//            advertisingDO.setDeleted(true);
//        }else{
//            advertisingDO.setDeleted(false);
//        }

        advertisingDO.setRuler(3);
        advertisingDO.setDisplayPeriod(ObjectUtil.isNotEmpty(advertisingDO.getStartTime()) ? 1 : 2);

        if (ObjectUtil.isNotEmpty(advertisingDO.getCreateUserName())) {
            advertisingDO.setCreator(advertisingDO.getCreateUserName());
        }
        if (ObjectUtil.isNotEmpty(advertisingDO.getUpdateUserName())) {
            advertisingDO.setUpdater(advertisingDO.getUpdateUserName());
        }

        processAdInfoPosition(advertisingDO);

        advertisingMapper.updateById(advertisingDO);
    }

    private void processAdInfoPosition(AdvertisingDO advertisingDO) {
        if (ObjectUtil.isNotEmpty(advertisingDO.getAdInfoPosition())) {
            Integer position = advertisingDO.getAdInfoPosition();
            if (position.equals(4)) {
                advertisingDO.setAdInfoPosition(12);
                advertisingDO.setPositionType(4);
            } else if (position.equals(1) || position.equals(2)) {
                advertisingDO.setPositionType(1);
            } else if (position.equals(5) || position.equals(6) || position.equals(7)) {
                advertisingDO.setPositionType(2);
            } else if (position.equals(8) || position.equals(9) || position.equals(10)) {
                advertisingDO.setPositionType(3);
            } else if (position.equals(11)) {
                advertisingDO.setPositionType(4);
            } else if(position.equals(3)){
                advertisingDO.setAdInfoPosition(4);
                advertisingDO.setPositionType(1);
            }
        }
    }

    private AdvertisingImageDO createAdvertisingImage(AdvertisingDO advertisingDO) {
        AdvertisingImageDO advertisingImageDO = new AdvertisingImageDO();
        advertisingImageDO.setAdUrl(advertisingDO.getAdUrl());
        advertisingImageDO.setIsImageJump(advertisingDO.getIsImageJump());

        processJumpLocation(advertisingDO, advertisingImageDO);

        advertisingImageDO.setAdvertisingId(advertisingDO.getId());
        advertisingImageDO.setDeleted(false);
        advertisingImageDO.setBusinessId(advertisingDO.getProjectOwnerShip());
        advertisingImageDO.setCreateTime(advertisingDO.getCreateTime());
        advertisingImageDO.setUpdateTime(advertisingDO.getUpdateTime());

        if (ObjectUtil.isNotEmpty(advertisingDO.getCreateUserName())) {
            advertisingImageDO.setCreator(advertisingDO.getCreateUserName());
        }
        if (ObjectUtil.isNotEmpty(advertisingDO.getUpdateUserName())) {
            advertisingImageDO.setUpdater(advertisingDO.getUpdateUserName());
        }

        return advertisingImageDO;
    }

    private void processJumpLocation(AdvertisingDO advertisingDO, AdvertisingImageDO advertisingImageDO) {
        if (ObjectUtil.isNotEmpty(advertisingDO.getJumpLocation())) {
            Integer location = advertisingDO.getJumpLocation();
            if (location.equals(13)) {
                advertisingImageDO.setJumpLocation(6);
                advertisingImageDO.setCouponBagId("5");
//                if (ObjectUtil.isNotEmpty(advertisingDO.getJumpLocationId())) {
//                    advertisingImageDO.setCouponBagId(advertisingDO.getJumpLocationId().toString());
//                }
            } else if (location.equals(0)) {
                advertisingImageDO.setJumpLocation(0);
            }else if (location.equals(1)) {
                advertisingImageDO.setJumpLocation(1);
                advertisingImageDO.setCouponId(advertisingDO.getJumpLocationId().toString());
            } else if (location >= 3 && location <= 9) {
                advertisingImageDO.setJumpLocation(4);
                advertisingImageDO.setJumpUrl(advertisingDO.getJumpUrl());
            } else if (location.equals(10)) {
                advertisingImageDO.setJumpLocation(2);
                advertisingImageDO.setCouponBagId(advertisingDO.getJumpLocationId().toString());
            } else if (location.equals(11)) {
                advertisingImageDO.setJumpLocation(4);
                advertisingImageDO.setJumpUrl(advertisingDO.getJumpUrl());
            }
        }
    }


    public Integer getIsOpen(AdvertisingDO advertising) {

        Integer isOpen = 1;
        if (advertising.getIsAllTime().equals(1)) {
            return isOpen;
        }
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        // 当前日期
        Integer currDate = Integer.parseInt(DateUtils.getDate().replace("-", ""));
        // 当前日
        int day = now.get(Calendar.DAY_OF_MONTH);
        // 当前周几
        boolean isFirstSunday = (now.getFirstDayOfWeek() == Calendar.SUNDAY);
        // 当前周几
//        int week = now.get(Calendar.DAY_OF_WEEK);
        int week = now.get(Calendar.DAY_OF_WEEK);
        // 若一周第一天为星期天，则-1
        if (isFirstSunday) {
            week = week - 1;
            if (week == 0) {
                week = 7;
            }
        }


        if (ObjectUtil.isNotEmpty(advertising.getStartTime())) {
            Integer begin = Integer.parseInt(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, advertising.getStartTime()).replace("-", ""));
            // 结束日期

            Integer end = Integer.parseInt(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, advertising.getEndTime()).replace("-", ""));

            if (begin <= currDate && currDate <= end) {
                // 在日期范围内
                isOpen = 1;
            } else {
                isOpen = 2;
            }
        }
        if (isOpen == 1) {
            if (ObjectUtil.isNotEmpty(advertising.getDayNumberList())) {
                if (advertising.getDayNumberList().contains(day)) {
                    isOpen = 1;
                } else {
                    isOpen = 2;
                }
            }
        }

        if (isOpen == 1) {
            if (ObjectUtil.isNotEmpty(advertising.getWeekNumberList())) {
                if (advertising.getWeekNumberList().contains(week)) {
                    isOpen = 1;
                } else {
                    isOpen = 2;
                }
            }
        }
        return isOpen;
    }


}
