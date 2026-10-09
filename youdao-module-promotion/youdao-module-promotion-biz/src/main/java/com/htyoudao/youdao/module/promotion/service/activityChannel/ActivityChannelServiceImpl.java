package com.htyoudao.youdao.module.promotion.service.activityChannel;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.enums.NumberBooleanEnum;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivityChannelSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.WechatJumpParam;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannel.ActivityChannelDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannelName.ActivityChannelNameDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityChannel.ActivityChannelMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityChannelName.ActivityChannelNameMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponpackage.CouponPackageMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcoupon.GoodCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotterySettingsMapper;
import com.htyoudao.youdao.module.promotion.enums.channel.ActivityChannelTypeEnum;
import com.htyoudao.youdao.module.promotion.service.activityChannelName.ActivityChannelNameService;
import com.htyoudao.youdao.module.promotion.util.wechatLink.GenerateUrlLink;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class ActivityChannelServiceImpl implements ActivityChannelService{


    @Resource
    private  ActivityChannelMapper activityChannelMapper;
    //秒杀短链
    @Value("${activity.seckill.sortPath}")
    private String seckillSortPath;

    @Value("${activity.default.appletName}")
    private  String appletName;

    @Value("${activity.default.socialName}")
    private String socialName;
    //秒杀 h5
    @Value("${activity.seckill.h5Path}")
    private String seckillH5Path;

    @Value("${activity.default.h5Name}")
    private String h5Name;
    //转盘短链
    @Value("${activity.lottery.turntable.sortPath}")
    private String turntableSortPath;
    //转盘 h5
    @Value("${activity.lottery.turntable.h5Path}")
    private String turntableH5Path;

    //福袋短链
    @Value("${activity.lottery.fortune.sortPath}")
    private  String fortuneSortPath;
    //福袋 h5
    @Value("${activity.lottery.fortune.h5Path}")
    private String fortuneH5Path;

    //集点短链
    @Value("${activity.collect.sortPath}")
    private String collectSortPath;

    //集点 h5
    @Value("${activity.collect.h5Path}")
    private String collectH5Path;

    //集卡短链
    @Value("${activity.card.sortPath}")
    private String cardSortPath;

    //集卡 h5
    @Value("${activity.card.h5Path}")
    private String cardH5Path;

    //优惠卷
    @Value("${activity.coupon.sortPath}")
    private String couponSortPath;
    //优惠卷 h5
    @Value("${activity.coupon.h5Path}")
    private String couponH5Path;

    //优惠卷包
    @Value("${activity.couponPackage.sortPath:}")
    private String couponPackageSortPath;
    //优惠卷包 h5
    @Value("${activity.couponPackage.h5Path:}")
    private String couponPackageH5Path;

    //集卡短链
    @Value("${activity.cq.sortPath}")
    private String cqSortPath;

    //集卡 h5
    @Value("${activity.cq.h5Path}")
    private String cqH5Path;
    //答题短链
    @Value("${activity.dt.sortPath}")
    private String dtSortPath;

    //答题 h5
    @Value("${activity.dt.h5Path}")
    private String dtH5Path;

    //签到活动短链
    @Value("${activity.sign.sortPath:}")
    private String signSortPath;

    //签到活动 h5
    @Value("${activity.sign.h5Path:}")
    private String signH5Path;

    //问卷短链
    @Value("${activity.wj.sortPath}")
    private String wjSortPath;

    //问卷 h5
    @Value("${activity.wj.h5Path}")
    private String wjH5Path;

    //投票短链
    @Value("${activity.vote.sortPath}")
    private String voteSortPath;

    //投票H5
    @Value("${activity.vote.h5Path}")
    private String voteH5Path;

    @Resource
    private GenerateUrlLink generateUrlLink;

    @Resource
    private ActivityChannelNameMapper activityChannelNameMapper;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private LotterySettingsMapper lotterySettingsMapper;

    @Resource
    private GoodCouponMapper goodCouponMapper;

    @Resource
    private CouponPackageMapper couponPackageMapper;

    @Override
    public List<ActivityChannelDO> selectByActivityId(Long id) {
        LambdaQueryWrapper<ActivityChannelDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityChannelDO::getActivityId, id);
        return activityChannelMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteByActivityId(Long id) {
        LambdaQueryWrapper<ActivityChannelDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityChannelDO::getActivityId, id);
        activityChannelMapper.delete(queryWrapper);
    }

    @Override
    public void createBatch(List<ActivityChannelDO> activityChannelDOList) {
        activityChannelMapper.insertBatch(activityChannelDOList);
    }

    @Override
    public void updateBatch(List<ActivityChannelDO> activityChannelDOList1) {
        activityChannelMapper.updateBatch(activityChannelDOList1);
    }

    /**
     * 新建活动生成短链
     * @param activityId
     */
    @Override
    public void createChannelDO(Long activityId,int type) {

        //新建两个默认的推广链接
        List<ActivityChannelDO> activityChannelDOList = getActivityChannelDOS(activityId, appletName, socialName,h5Name);
        //批量插入推广链接
        createBatch(activityChannelDOList);
        //将插入但没有短链的推广赋值推广
      //  List<ActivityChannelDO> activityChannelDOList1 = selectByActivityId(activityId);
        //赋值短链
        try {
            buildLink(activityChannelDOList, activityId,type);
        } catch (Exception e) {
            log.error("buildLinkError", e);
        }

        //更新赋值后的推广
        updateBatch(activityChannelDOList);
    }



  /*  @Override
    public void createAndUpdateChannel(List<ActivityChannelSaveReqVO> activityChannelList, Long activityId,int type) {
        List<ActivityChannelDO> activityChannelDOList = new ArrayList<>();

        List<ActivityChannelDO> insertList = new ArrayList<>();

        List<ActivityChannelDO> updateList = new ArrayList<>();

        List<Integer> surplusIds = new ArrayList<>();
        //将推广进餐转化为推广对象
        activityChannelList.forEach(channel -> {
            if (ObjectUtil.isNotEmpty(channel.getId())){
                surplusIds.add(channel.getId());
            }
            ActivityChannelDO activityChannelDO = new ActivityChannelDO();
            channel.setActivityId(activityId);
            BeanUtils.copyProperties(channel, activityChannelDO);
            activityChannelDOList.add(activityChannelDO);
        });
        //删除多于 Ids
        deleteSurplusIds(activityId,surplusIds);

        for (ActivityChannelDO activityChannelDO : activityChannelDOList) {
            if (ObjectUtil.isEmpty(activityChannelDO.getId())) {
                insertList.add(activityChannelDO);
            }else {

                //有 id 有链接的
                updateList.add(activityChannelDO);
            }
        }
        //更新之前的推广
        if (ObjectUtil.isNotEmpty(updateList)) {
            updateBatch(updateList);
        }
        //实现新的推广
        if (ObjectUtil.isNotEmpty(insertList)) {
            //新建空的推广
            insertList.forEach(activityChannelDO -> {activityChannelDO.setIsDefault(NumberBooleanEnum.FALSE.getNumberValue());});
            insertList.forEach(activityChannelDO -> {activityChannelDO.setActivityId(activityId);});
            createBatch(insertList);

            //赋值短链
            try {
                buildLink(insertList, activityId, type);
            } catch (Exception e) {
                log.error("buildLinkError", e);
            }
            //修改入库
            updateBatch(insertList);


        }

    }

   */
    @Override
    public  void  creatChannle(Long businessId,String sortPath){
        extractedUrl( businessId, sortPath);
    }



    private void buildLink(List<ActivityChannelDO> noLinkDO, Long activityId,int type) {
        Long businessId = BusinessContextHolder.getRequiredBusinessId();

        String h5Path = "";
        String sortPath = "";

        if (type == ActivityChannelTypeEnum.SEC_KILL.getCode()) {
            h5Path = seckillH5Path;
            sortPath = seckillSortPath;
        }else if (type == ActivityChannelTypeEnum.TURNTABLE.getCode()) {
            h5Path= turntableH5Path;
            sortPath= turntableSortPath;
        }else if (type == ActivityChannelTypeEnum.FORTUNE.getCode()) {
            h5Path = fortuneH5Path;
            sortPath = fortuneSortPath;
        }else if (type == ActivityChannelTypeEnum.COLLECT.getCode()) {
            h5Path = collectH5Path;
            sortPath = collectSortPath;
        }else if (type == ActivityChannelTypeEnum.COUPON.getCode()) {
            h5Path = couponH5Path;
            sortPath = couponSortPath;
        }else if (type == ActivityChannelTypeEnum.COUPON_PACKAGE.getCode()) {
            h5Path = couponPackageH5Path;
            sortPath = couponPackageSortPath;
        }else if (type == ActivityChannelTypeEnum.CARD.getCode()) {
            h5Path = cardH5Path;
            sortPath = cardSortPath;
        }else if (type == ActivityChannelTypeEnum.CQ.getCode()) {
            h5Path = cqH5Path;
            sortPath = cqSortPath;
        }else if (type == ActivityChannelTypeEnum.SIGN.getCode()) {
            h5Path = signH5Path;
            sortPath = signSortPath;
        }else if (type == ActivityChannelTypeEnum.WJ.getCode()) {
            h5Path = wjH5Path;
            sortPath = wjSortPath;
        }else if (type == ActivityChannelTypeEnum.DT.getCode()) {
            h5Path = dtH5Path;
            sortPath = dtSortPath;
        }else if (type == ActivityChannelTypeEnum.VOTE.getCode()) {
            h5Path = voteH5Path;
            sortPath = voteSortPath;
        }



        for (ActivityChannelDO activityChannelDO : noLinkDO) {
            if (activityChannelDO.getChannelName().equals(h5Name)){
                //h5链接
                String longUrl = h5Path+activityChannelDO.getActivityId()+"&channelId="+activityChannelDO.getChannelId();
                activityChannelDO.setLongUrl(longUrl);

                String sortUrl = generateUrlLink.getSortUrl(longUrl);
                activityChannelDO.setLinkUrl(sortUrl);

            }else if (activityChannelDO.getChannelName().equals(socialName)){
                //社群链接 直接拼
                activityChannelDO.setLinkUrl(sortPath+"?id="+activityId+"&channelId="+activityChannelDO.getChannelId());
            }else if (activityChannelDO.getChannelName().equals(appletName)){
                // 小程序链接 生成长链再短链
                extracted(activityId, activityChannelDO, businessId,sortPath);
            }else {
                //其他渠道链接
                activityChannelDO.setIsDefault(NumberBooleanEnum.FALSE.getNumberValue());
                extracted(activityId, activityChannelDO, businessId,sortPath);
            }

        }
    }

    /**
     * 生成长链再短链
     * @param activityId
     * @param activityChannelDO
     * @param businessId
     */
    private void extracted(Long activityId, ActivityChannelDO activityChannelDO, Long businessId,String sortPath) {
        String longUrl = generateUrlLink.generateUrlLink(WechatJumpParam.builder().path(sortPath).businessId(businessId).
                query("id="+ activityId + "&channelId="+activityChannelDO.getChannelId()).build());
        activityChannelDO.setLongUrl(longUrl);
        String sortUrl = generateUrlLink.getSortUrl(longUrl);
        activityChannelDO.setLinkUrl(sortUrl);
    }
    private void extractedUrl( Long businessId,String sortPath) {
        String longUrl = generateUrlLink.generateUrlLink(WechatJumpParam.builder().path(sortPath).businessId(businessId).
             build());
        String sortUrl = generateUrlLink.getSortUrl(longUrl);
        System.out.println(sortUrl);
    }
    private List<ActivityChannelDO> findNoLinkDo(Long activityId) {
        LambdaQueryWrapper<ActivityChannelDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityChannelDO::getActivityId, activityId);
        queryWrapper.isNull(ActivityChannelDO::getLinkUrl);
        return activityChannelMapper.selectList(queryWrapper);
    }

    private List<ActivityChannelDO> getActivityChannelDOS(Long activityId, String appletName, String socialName, String h5Name) {
        List<ActivityChannelDO> activityChannelDOList = new ArrayList<>();


        Map<String, Long> nameCollect = ensureDefaultChannelNames(appletName, socialName, h5Name);
        ActivityChannelDO activityChannelDO2 = new ActivityChannelDO();
        activityChannelDO2.setActivityId(activityId);
        activityChannelDO2.setChannelName(socialName);
        activityChannelDO2.setIsDefault(NumberBooleanEnum.TRUE.getNumberValue());
        activityChannelDO2.setChannelId(nameCollect.get(socialName));
        activityChannelDOList.add(activityChannelDO2);

        ActivityChannelDO activityChannelDO3 = new ActivityChannelDO();
        activityChannelDO3.setActivityId(activityId);
        activityChannelDO3.setChannelName(h5Name);
        activityChannelDO3.setIsDefault(NumberBooleanEnum.TRUE.getNumberValue());
        activityChannelDO3.setChannelId(nameCollect.get(h5Name));
        activityChannelDOList.add(activityChannelDO3);

        ActivityChannelDO activityChannelDO1 = new ActivityChannelDO();
        activityChannelDO1.setActivityId(activityId);
        activityChannelDO1.setChannelName(appletName);
        activityChannelDO1.setIsDefault(NumberBooleanEnum.TRUE.getNumberValue());
        activityChannelDO1.setChannelId(nameCollect.get(appletName));
        activityChannelDOList.add(activityChannelDO1);



        return activityChannelDOList;
    }

    private Map<String, Long> ensureDefaultChannelNames(String appletName, String socialName, String h5Name) {
        List<String> requiredNames = Arrays.asList(appletName, socialName, h5Name);
        LambdaQueryWrapper<ActivityChannelNameDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityChannelNameDO::getIsDefault, 1);
        List<ActivityChannelNameDO> activityChannelNameDOS = activityChannelNameMapper.selectList(queryWrapper);
        Map<String, ActivityChannelNameDO> channelNameMap = activityChannelNameDOS.stream()
                .filter(item -> item.getName() != null)
                .collect(Collectors.toMap(ActivityChannelNameDO::getName, item -> item, (left, right) -> left));

        for (String requiredName : requiredNames) {
            if (!channelNameMap.containsKey(requiredName)) {
                ActivityChannelNameDO dataObject = new ActivityChannelNameDO();
                dataObject.setName(requiredName);
                dataObject.setIsDefault(NumberBooleanEnum.TRUE.getNumberValue());
                dataObject.setIsEnable(NumberBooleanEnum.TRUE.getNumberValue());
                activityChannelNameMapper.insert(dataObject);
                channelNameMap.put(requiredName, dataObject);
            }
        }

        return channelNameMap.values().stream()
                .collect(Collectors.toMap(ActivityChannelNameDO::getName, ActivityChannelNameDO::getId, (left, right) -> left));
    }

    /**
     * 删除多余的 Ids
     * @param activityId
     * @param notInIds
     */
    private void deleteSurplusIds(Long activityId, List<Integer> notInIds) {
        LambdaQueryWrapper<ActivityChannelDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityChannelDO::getActivityId, activityId);
        queryWrapper.notIn(ActivityChannelDO::getId, notInIds);
        //因为未启用的不展示，所以前端传回来的一定没有 未启用的 ，防止未启用的删除
        queryWrapper.ne(ActivityChannelDO::getIsEnable, 0);
        activityChannelMapper.delete(queryWrapper);
    }

    /**
     * 参数只有id
     * @param id
     * @return
     */
    @Override
    public List<ActivityChannelDO> selectByChannelId(Long id) {
        LambdaQueryWrapper<ActivityChannelDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityChannelDO::getChannelId, id);
        return activityChannelMapper.selectList(queryWrapper);
    }

    @Override
    public List<ActivityChannelDO> updateStatusByChannelId(Long id, Integer isEnable) {
        LambdaUpdateWrapper<ActivityChannelDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ActivityChannelDO::getIsEnable, isEnable);
        updateWrapper.eq(ActivityChannelDO::getChannelId, id);
        activityChannelMapper.update(updateWrapper);

        return selectByChannelId(id);
    }

    @Override
    public List<ActivityChannelDO> selectByActivityIdWithIsEnable(Long id) {
        LambdaQueryWrapper<ActivityChannelDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityChannelDO::getActivityId, id);
        queryWrapper.eq(ActivityChannelDO::getIsEnable, NumberBooleanEnum.TRUE.getNumberValue());
        return activityChannelMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteByChannelId(Long id) {
        LambdaQueryWrapper<ActivityChannelDO> queryWrapper = new LambdaQueryWrapper<>();
       queryWrapper.eq(ActivityChannelDO::getChannelId, id);
        activityChannelMapper.delete(queryWrapper);
    }

    @Override
    public void updateNameByChannelId(Long id, String name) {
        LambdaUpdateWrapper<ActivityChannelDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ActivityChannelDO::getChannelName, name);
        updateWrapper.eq(ActivityChannelDO::getChannelId, id);
        activityChannelMapper.update(updateWrapper);
    }


    @Override
    public void refreshByActivityId(Long activityId, List<Long> channelIds, Integer type) {

        LambdaQueryWrapper<ActivityChannelDO> queryWrapperChannel = new LambdaQueryWrapper<>();
        queryWrapperChannel.eq(ActivityChannelDO::getActivityId, activityId);
        List<ActivityChannelDO> activityChannelDOList1 = activityChannelMapper.selectList(queryWrapperChannel);

        List<Long> channelIds1 = activityChannelDOList1.stream().map(ActivityChannelDO::getChannelId).toList();

        LambdaQueryWrapper<ActivityChannelNameDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityChannelNameDO::getIsEnable, 1);
        queryWrapper.notIn(ActivityChannelNameDO::getId, channelIds1);
       // queryWrapper.eq(ActivityChannelNameDO::getIsDefault, 0);
        List<ActivityChannelNameDO> activityChannelNameDOS = activityChannelNameMapper.selectList(queryWrapper);
        if (ObjectUtil.isNotEmpty(activityChannelNameDOS)) {
            List<ActivityChannelDO> activityChannelDOList = new ArrayList<>();

            for (ActivityChannelNameDO activityChannelNameDO : activityChannelNameDOS) {
                ActivityChannelDO activityChannelDO = new ActivityChannelDO();
                activityChannelDO.setActivityId(activityId);
                activityChannelDO.setChannelName(activityChannelNameDO.getName());
                activityChannelDO.setChannelId(activityChannelNameDO.getId());
                activityChannelDO.setIsEnable(1);
                activityChannelDOList.add(activityChannelDO);
            }


           createBatch(activityChannelDOList);

            buildLink(activityChannelDOList,activityId, type);

            updateBatch(activityChannelDOList);
        }
    }

    /**
     * 初始化渠道：清空本表后，按活动类型（秒杀/抽奖/集点）、抽奖设置（转盘/福袋）、优惠券、优惠券包重新生成渠道链接。
     * 渠道名称使用 ActivityChannelNameDO 中 isDefault=1 的默认名称，链接生成逻辑与 createChannelDO 一致。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initialization() {
        // 1. 删除本表所有数据
        LambdaQueryWrapper<ActivityChannelDO> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.apply("1 = 1");
        activityChannelMapper.delete(deleteWrapper);

        // 2. 活动表：只处理 秒杀(2)、、集点(4)，按 ActivityChannelTypeEnum 对应
        List<Integer> activityTypes = Arrays.asList(
                ActivityTypeEnum.SEC_KILL.getCode(),
                ActivityTypeEnum.JD.getCode()
        );
        LambdaQueryWrapper<ActivityDO> activityQuery = new LambdaQueryWrapper<>();
        activityQuery.in(ActivityDO::getActivityType, activityTypes);
        List<ActivityDO> activities = activityMapper.selectList(activityQuery);
        if (ObjectUtil.isNotEmpty(activities)){
            for (ActivityDO activity : activities) {
                int typeCode = activity.getActivityType();
                if (typeCode == ActivityTypeEnum.SEC_KILL.getCode()) {
                    createChannelDO(activity.getId(), ActivityChannelTypeEnum.SEC_KILL.getCode());
                } else if (typeCode == ActivityTypeEnum.JD.getCode()) {
                    createChannelDO(activity.getId(), ActivityChannelTypeEnum.COLLECT.getCode());
                }
            }
        }


        // 3. 抽奖：LotterySettingsDO 中 lotteryType 1=转盘、3=福袋，按 ActivityChannelTypeEnum 对应
        LambdaQueryWrapper<LotterySettingsDO> lotteryQuery = new LambdaQueryWrapper<>();
        lotteryQuery.in(LotterySettingsDO::getLotteryType, 1, 3);
        List<LotterySettingsDO> lotterySettings = lotterySettingsMapper.selectList(lotteryQuery);
        if (ObjectUtil.isNotEmpty(lotterySettings)){
            for (LotterySettingsDO ls : lotterySettings) {
                int channelType = ls.getLotteryType() == 1
                        ? ActivityChannelTypeEnum.TURNTABLE.getCode()
                        : ActivityChannelTypeEnum.FORTUNE.getCode();
                createChannelDO(ls.getActivityId(), channelType);
            }
        }


        // 4. 优惠券：GoodCouponDO 全部生成渠道
        List<GoodCouponDO> goodCoupons = goodCouponMapper.selectList(new LambdaQueryWrapper<GoodCouponDO>());
        if (ObjectUtil.isNotEmpty(goodCoupons)){
            for (GoodCouponDO coupon : goodCoupons) {
                createChannelDO(coupon.getId(), ActivityChannelTypeEnum.COUPON.getCode());
            }
        }


        // 5. 优惠券包：CouponPackageDO 全部生成渠道
        List<CouponPackageDO> couponPackages = couponPackageMapper.selectList(new LambdaQueryWrapper<CouponPackageDO>());
        if (ObjectUtil.isNotEmpty(couponPackages)){
            for (CouponPackageDO pkg : couponPackages) {
                createChannelDO(pkg.getId(), ActivityChannelTypeEnum.COUPON_PACKAGE.getCode());
            }
        }

    }
}

