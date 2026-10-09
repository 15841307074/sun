package com.htyoudao.youdao.module.promotion.service.activity;


import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.error;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.LOTTERY_DELETE;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.NJNZ_ISENABLED;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.NJNZ_IS_USE;
import static com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants.PROMOTION_ACTIVITY_CREATE_SUCCESS;
import static com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants.PROMOTION_ACTIVITY_CREATE_TYPE;
import static com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants.PROMOTION_ACTIVITY_DELETE_SUCCESS;
import static com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants.PROMOTION_ACTIVITY_DELETE_TYPE;
import static com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants.PROMOTION_ACTIVITY_STATUS_SUCCESS;
import static com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants.PROMOTION_ACTIVITY_STATUS_TYPE;
import static com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants.PROMOTION_ACTIVITY_TYPE;
import static com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants.PROMOTION_ACTIVITY_UPDATE_SUCCESS;
import static com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants.PROMOTION_ACTIVITY_UPDATE_TYPE;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.module.promotion.controller.admin.activity.vo.ActivityDataRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotterySettingsMapper;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.mzt.logapi.context.LogRecordContext;

import java.util.*;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.constants.RedisKeyConstants;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.util.MyBatisUtils;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.api.enums.IsGroundConstant;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityPageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityPageRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStoreTag.ActivityStoreTagDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityStoreTag.ActivityStoreTagMapper;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryRuntimeLifecycle;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;

import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


@Service
public class ActivityServiceImpl  implements ActivityService  {
    @Resource
    private LotteryRuntimeLifecycle lotteryRuntime;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ActivityStoreService activityStoreService;

    @Resource
    private LotterySettingsMapper lotterySettingsMapper;

    @Resource
    private ActivityStoreTagMapper activityStoreTagMapper;


    @Resource
    private StoreApi storeApi;

    @Override
    public PageResult<ActivityPageRespVO> getPage(ActivityPageReqVO reqVO) {

        List<Long> storeIds = new ArrayList<>();
        if (reqVO.getStoreId() != null){
            storeIds = List.of(reqVO.getStoreId());
        }
        if (reqVO.getOrgId() != null){
            storeIds = storeApi.getStoreIdsByAllOrgId(reqVO.getOrgId()).getCheckedData();
        }


        LambdaQueryWrapperX<ActivityDO> queryWrapper = new LambdaQueryWrapperX();

        if (StringUtils.isNotBlank(reqVO.getActivityName())){
            queryWrapper.and(wrapper ->
                wrapper.like(ActivityDO::getActivityName, reqVO.getActivityName())
                    .or()
                    .like(ActivityDO::getActivityRemark, reqVO.getActivityName())
            );
        }

        queryWrapper.eqIfPresent(ActivityDO::getActivityType, reqVO.getActivityType());
        queryWrapper.inIfPresent(ActivityDO::getIsEnabled, reqVO.getIsEnabled());

        //包含关系 
        queryWrapper.leIfPresent(ActivityDO::getStartDate, reqVO.getEndTime());
        queryWrapper.geIfPresent(ActivityDO::getEndDate, reqVO.getStartTime());

        queryWrapper.apply("a.business_id = {0}", BusinessContextHolder.getRequiredBusinessId());

        Page<ActivityDO> page = MyBatisUtils.buildPage(reqVO);
        Page<ActivityDO> result = activityMapper.pageQuery(page, queryWrapper, storeIds);
        if (result == null || CollectionUtils.isEmpty(result.getRecords())){
            return PageResult.empty();
        }

        // 转换为 VO
        List<ActivityPageRespVO> voList = result.getRecords().stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal());

    }

    @Override
    @LogRecord(type = PROMOTION_ACTIVITY_TYPE, subType = PROMOTION_ACTIVITY_CREATE_TYPE, bizNo = "{{#activity.id}}", success = PROMOTION_ACTIVITY_CREATE_SUCCESS)
    public Long createActivity(ActivityDO activityDO) {
        activityDO.setId(null);
        //check name
        checkNameReplace(null, activityDO.getActivityName());
        //新增活动 默认下架
        activityDO.setIsEnabled(IsGroundConstant.IS_GROUND_0);
        activityMapper.insert(activityDO);
        LogRecordContext.putVariable("activity", activityDO);

        return activityDO.getId();
    }

    private void checkNameReplace(Long id, String name) {
        LambdaQueryWrapperX<ActivityDO> queryWrapperX = new LambdaQueryWrapperX();
        queryWrapperX.neIfPresent(ActivityDO::getId, id);
        queryWrapperX.eq(ActivityDO::getActivityName, name);

        if (activityMapper.exists(queryWrapperX)){
            throw exception(ErrorCodeConstants.MJ_NAME_HAVING);
        }
    }

    @Override
    @LogRecord(type = PROMOTION_ACTIVITY_TYPE, subType = PROMOTION_ACTIVITY_UPDATE_TYPE, bizNo = "{{#activity.id}}", success = PROMOTION_ACTIVITY_UPDATE_SUCCESS)
    public void updateActivity(ActivityDO activityDO) {
        checkNameReplace(activityDO.getId(), activityDO.getActivityName());

        activityDO.setIsEnabled(IsGroundConstant.IS_GROUND_0);
        checkIsEnable(activityDO.getId());
        LogRecordContext.putVariable("activity", activityDO);

        activityMapper.updateById(activityDO);
    }

    private void checkIsEnable(Long id) {
        if (activityMapper.selectById(id).getIsEnabled().equals(IsGroundConstant.IS_GROUND_1)) {
            throw exception(NJNZ_ISENABLED);
        }
    }

    @Override
    @LogRecord(type = PROMOTION_ACTIVITY_TYPE, subType = PROMOTION_ACTIVITY_DELETE_TYPE, bizNo = "{{#id}}", success = PROMOTION_ACTIVITY_DELETE_SUCCESS)
    public void deleteActivity(Long id) {
        //查询是否有用户参与活动 如果有不可以删除 20250701 李岩需求
//        String key = RedisKeyConstants.ACTIVITY_EXIST_KEY + id;
//        String s = stringRedisTemplate.opsForValue().get(key);
//        if (ObjectUtil.isNotEmpty(s)) {
//            throw exception(NJNZ_IS_USE);
//        }

        // 若该活动已下架，允许用户操作删除，20251113 李岩需求
        ActivityDO activityDO = activityMapper.selectById(id);
        if (activityDO == null || Objects.equals(activityDO.getIsEnabled(), 1)) {
            throw exception(NJNZ_IS_USE);
        }

        activityMapper.deleteById(id);
    }

    @Override
    @LogRecord(type = PROMOTION_ACTIVITY_TYPE, subType = PROMOTION_ACTIVITY_STATUS_TYPE, bizNo = "{{#id}}", success = PROMOTION_ACTIVITY_STATUS_SUCCESS)
    public void updateStatus(Long id, Integer status) {
        if (lotteryRuntime.changeState(id, status)) return;
        LambdaUpdateWrapper<ActivityDO> lambdaUpdateWrapper = new LambdaUpdateWrapper();
        lambdaUpdateWrapper.set(ActivityDO::getIsEnabled, status);
        lambdaUpdateWrapper.eq(ActivityDO::getId, id);
        activityMapper.update(lambdaUpdateWrapper);
    }

    @Override
    public ActivityDO selectById(Long id) {
        return activityMapper.selectById(id);
    }

    @Override
    public Map<Long, String> selectActivityByIds(Set<Long> activityIds) {
        List<ActivityDO> activities = activityMapper.selectByIds(activityIds);
        if (CollectionUtils.isEmpty(activities)){
            return Collections.emptyMap();
        }
        return activities.stream()
                .collect(Collectors.toMap(ActivityDO::getId, ActivityDO::getActivityName));
    }

    @Override
    public PageResult<ActivityPageRespVO> selectActivityList(ActivityPageReqVO reqVO) {

        List<Long> storeIds = new ArrayList<>();
        if (reqVO.getStoreId() != null){
            storeIds = List.of(reqVO.getStoreId());
        }
        if (reqVO.getOrgId() != null){
            storeIds = storeApi.getStoreIdsByAllOrgId(reqVO.getOrgId()).getCheckedData();
        }


        LambdaQueryWrapperX<ActivityDO> queryWrapper = new LambdaQueryWrapperX();

        if (StringUtils.isNotBlank(reqVO.getActivityName())){
            queryWrapper.and(wrapper ->
                    wrapper.like(ActivityDO::getActivityName, reqVO.getActivityName())
                            .or()
                            .like(ActivityDO::getActivityRemark, reqVO.getActivityName())
            );
        }
        queryWrapper.in(ActivityDO::getActivityType, 1,2,5);
        queryWrapper.eqIfPresent(ActivityDO::getActivityType, reqVO.getActivityType());
        queryWrapper.inIfPresent(ActivityDO::getIsEnabled, reqVO.getIsEnabled());

        //包含关系
        queryWrapper.leIfPresent(ActivityDO::getStartDate, reqVO.getEndTime());
        queryWrapper.geIfPresent(ActivityDO::getEndDate, reqVO.getStartTime());

        queryWrapper.apply("a.business_id = {0}", BusinessContextHolder.getRequiredBusinessId());

        Page<ActivityDO> page = MyBatisUtils.buildPage(reqVO);
        Page<ActivityDO> result = activityMapper.pageQuery(page, queryWrapper, storeIds);
        if (result == null || CollectionUtils.isEmpty(result.getRecords())){
            return PageResult.empty();
        }

        // 转换为 VO
        List<ActivityPageRespVO> voList = result.getRecords().stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal());
    }


    private ActivityPageRespVO convertToRespVO(ActivityDO activityDO) {
        ActivityPageRespVO respVO = new ActivityPageRespVO();
        BeanUtils.copyProperties(activityDO, respVO);

        //非全门店活动。补充门店信息；标签范围活动按标签反查门店（activity_store 由联动维护，可能滞后不全）
        if (Integer.valueOf(1).equals(activityDO.getAppScope())) {
            respVO.setStoreInfoDTOS(selectStoresByActivityTags(activityDO.getId()));
        } else if (!Objects.equals(activityDO.getActivityStore(), 1)){
            List<StoreInfoDTO> storeInfoDTOS = activityStoreService.storesByActivityId(activityDO.getId());
            respVO.setStoreInfoDTOS(storeInfoDTOS);
        }

        if(ObjectUtil.isNotEmpty(activityDO.getActivityType())){
            if(activityDO.getActivityType().equals(3)){
                LambdaQueryWrapper<LotterySettingsDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(LotterySettingsDO::getActivityId,activityDO.getId());
                LotterySettingsDO lotterySettingsDO = lotterySettingsMapper.selectOne(wrapper);
                if(lotterySettingsDO!=null){
                    respVO.setLotteryType(lotterySettingsDO.getLotteryType());
                    respVO.setLotterySettingId(lotterySettingsDO.getId());
                }
            }
        }


        //设置时间str2array
        setTimeField2List(activityDO, respVO);

        return respVO;
    }

    /**
     * 标签范围活动按活动配置的标签反查当前绑定门店（去重）
     */
    private List<StoreInfoDTO> selectStoresByActivityTags(Long activityId) {
        List<Long> tagIds = activityStoreTagMapper.selectList(new LambdaQueryWrapper<ActivityStoreTagDO>()
                        .eq(ActivityStoreTagDO::getActivityId, activityId))
                .stream()
                .map(ActivityStoreTagDO::getTagId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tagIds)) {
            return new ArrayList<>();
        }
        Map<Long, List<StoreInfoDTO>> storesByTagId = storeApi.getStoreIdsByTagIds(tagIds);
        if (storesByTagId == null || storesByTagId.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, StoreInfoDTO> distinctStores = new LinkedHashMap<>();
        storesByTagId.values().stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(store -> store != null && store.getStoreId() != null)
                .forEach(store -> distinctStores.putIfAbsent(store.getStoreId(), store));
        return new ArrayList<>(distinctStores.values());
    }

    private void setTimeField2List(ActivityDO activityDO, ActivityPageRespVO respVO) {
        if (StringUtils.isNotEmpty(activityDO.getTimeRange())){
            respVO.setTimeRangeList(List.of(activityDO.getTimeRange().split(",")));
        }

        if (StringUtils.isNotEmpty(activityDO.getDayNumbers())){
            List<Integer> dayNumberList = Arrays.stream(activityDO.getDayNumbers().split(","))
                .map(Integer::valueOf)
                .toList();
            respVO.setDayNumberList(dayNumberList);
        }

        if (StringUtils.isNotEmpty(activityDO.getWeekNumbers())){
            List<Integer> weekNumbersList = Arrays.stream(activityDO.getWeekNumbers().split(","))
                .map(Integer::valueOf)
                .toList();
            respVO.setWeekNumberList(weekNumbersList);
        }
    }

    /**
     * 获取活动状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 1-未开始, 2-进行中, 3-已结束
     */
    public int getActivityStatus(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("开始日期和结束日期不能为null");
        }

        Date now = new Date();

        if (now.before(startDate)) {
            return 1; // 未开始
        } else if (now.after(endDate)) {
            return 3; // 已结束
        } else {
            return 2; // 进行中
        }
    }

}
