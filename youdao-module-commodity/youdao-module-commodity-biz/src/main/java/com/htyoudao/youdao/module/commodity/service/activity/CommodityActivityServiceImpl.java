package com.htyoudao.youdao.module.commodity.service.activity;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityActivityDTO;
import com.htyoudao.youdao.module.commodity.api.VO.CommodityActivityVO;
import com.htyoudao.youdao.module.commodity.constant.CommodityActivityConstant;
import com.htyoudao.youdao.module.commodity.controller.admin.activity.vo.CommodityActivityPageReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.activity.vo.CommodityActivityRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.activity.vo.CommodityActivitySaveReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySpus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.activity.CommodityActivityDO;
import com.htyoudao.youdao.module.commodity.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.commodity.service.spus.ICommoditySpusService;
import com.htyoudao.youdao.module.commodity.util.ConvertUtil;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.*;

/**
 * 商品活动 Service 实现类
 *
 * @author hhhh
 */
@Service
@Validated
@Slf4j
public class CommodityActivityServiceImpl implements CommodityActivityService {

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private ICommoditySpusService commoditySpursService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = COMMODITY_ACTIVITY_TYPE, subType = COMMODITY_ACTIVITY_CREATE_SUB_TYPE, bizNo = "{{#commodityIds}}", success = COMMODITY_ACTIVITY_CREATE_SUCCESS)
    public Boolean createActivity(CommodityActivitySaveReqVO createReqVO) {

        List<Long> commodityIds = createReqVO.getCommodityIds();
        LambdaQueryWrapper<CommodityActivityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommodityActivityDO::getIsActive, 1);
        queryWrapper.in(CommodityActivityDO::getCommodityId, commodityIds);
        List<CommodityActivityDO> activityList = activityMapper.selectList(queryWrapper);

        List<Long> activityCommodityIds = activityList.stream().map(CommodityActivityDO::getCommodityId).toList();
        //从商品ID列表中移除已存在的商品ID
        commodityIds.removeAll(activityCommodityIds);
        //若commodityIds为空，则返回 表示全部重复
        if (CollectionUtil.isEmpty(commodityIds)) {
            throw exception(ACTIVITY_ALL_EXISTS);
        }


        List<CommoditySpus> spurs = commoditySpursService.getSpursByCommodityIds(commodityIds);
        //若spurs为空，则返回 表示商品不存在
        if (CollectionUtil.isEmpty(spurs)) {
            throw exception(ACTIVITY_COMMODITY_NOT_EXISTS);
        }

        Map<Long, CommoditySpus> spusMap = spurs.stream().collect(Collectors.toMap(CommoditySpus::getCommodityId, e -> e));
        List<CommodityActivityDO> commodityActivities = new ArrayList<>();
        for (Long commodityId : commodityIds) {
            //若commodityId 与 spursMap 匹配，则给该商品添加到活动商品列表中
            if (!spusMap.containsKey(commodityId)) {
                continue;
            }
            CommodityActivityDO activity = new CommodityActivityDO();
            CommoditySpus commoditySpus = spusMap.get(commodityId);
            //补充活动商品信息
            activity.setCommodityId(commodityId);
            activity.setCategoryId(commoditySpus.getCategoryId());
            activity.setCommodityName(commoditySpus.getCommodityName());
            if (ObjectUtil.isNotEmpty(commoditySpus.getImageUrl())) {
                List<String> imageUrls = ConvertUtil.convertStringToListS(commoditySpus.getImageUrl());
                activity.setThumbnailUrl(imageUrls.isEmpty() ? Strings.EMPTY : imageUrls.get(0));
            }
            activity.setIsSingle(commoditySpus.getIsSingle());
            activity.setSkuIds(commoditySpus.getSkuIds());
            //活动类型（如 1-抽奖,2-折扣,3-促销）
            activity.setActivityType(CommodityActivityConstant.ACTIVITY_TYPE_1);
            activity.setActivityStartDate(null);
            activity.setActivityEndDate(null);
            activity.setActivityDescription(null);
            //是否启用（1 表示启用，0 表示停用）
            activity.setIsActive(CommodityActivityConstant.IS_ACTIVE_1);
            activity.setDeleted(Boolean.FALSE);
            commodityActivities.add(activity);
        }
        log.info("批量插入活动商品:service.commodityActivities={}", commodityActivities);
        LogRecordContext.putVariable("commodityIds", commodityIds);
        return activityMapper.insertBatchSomeColumn(commodityActivities) > 0;

    }

    @Override
    @LogRecord(type = COMMODITY_ACTIVITY_TYPE, subType = COMMODITY_ACTIVITY_DELETE_SUB_TYPE, bizNo = "{{#activityId}}", success = COMMODITY_ACTIVITY_DELETE_SUCCESS)
        public void deleteActivity(Long activityId) {
        // 校验存在
        validateActivityExists(activityId);
        // 删除
        activityMapper.deleteById(activityId);
    }

    private void validateActivityExists(Long id) {
        if (activityMapper.selectById(id) == null) {
            throw exception(ACTIVITY_NOT_EXISTS);
        }
    }

    @Override
    public PageResult<CommodityActivityDO> getActivityPage(CommodityActivityPageReqVO pageReqVO) {
        pageReqVO.setIsActive(CommodityActivityConstant.IS_ACTIVE_1);
        return activityMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CommodityActivityRespVO> selcetSpusForActivity() {

        List<CommodityActivityRespVO> commodityForActivityVOList = new ArrayList<>();
        LambdaQueryWrapper<CommoditySpus> commoditySpuWrapper = new LambdaQueryWrapper<>();
        commoditySpuWrapper.eq(CommoditySpus::getDeleted, 0);
        commoditySpuWrapper.eq(CommoditySpus::getSaleRule, 0);
//        commoditySpusLambdaQueryWrapper.eq(CommoditySpus::getStoreStatus, 1); 门店上下架状态 1 上架 2 下架
        commoditySpuWrapper.eq(CommoditySpus::getWxStatus, 1);  //小程序上架状态 1 上架 2 下架
        List<CommoditySpus> commoditySpuses = commoditySpursService.list(commoditySpuWrapper);
        if (commoditySpuses == null || commoditySpuses.isEmpty()) {
            return Collections.emptyList();
        }
        List<CommodityActivityDO> commodityActivitys =
                activityMapper.selectList(new LambdaQueryWrapper<CommodityActivityDO>().eq(CommodityActivityDO::getIsActive, 1));

        if (commodityActivitys == null) {
            commodityActivitys = Collections.emptyList();
        }
        Set<Long> activeCommodityIds = commodityActivitys.stream()
                .map(CommodityActivityDO::getCommodityId)
                .collect(Collectors.toSet());
        for (CommoditySpus spus : commoditySpuses) {
            CommodityActivityRespVO commodyForActivityVO = new CommodityActivityRespVO();
            commodyForActivityVO.setCommodityName(spus.getCommodityName());
            commodyForActivityVO.setCommodityId(spus.getCommodityId());
            commodyForActivityVO.setIsShow(activeCommodityIds.contains(spus.getCommodityId()) ? "0" : "1");
            if (ObjectUtil.isNotEmpty(spus.getDictValue())) {
                String nameStr = spus.getCommodityName() + "(" + spus.getDictValue() + ")";
                commodyForActivityVO.setCommodityName(nameStr);
            }
            commodityForActivityVOList.add(commodyForActivityVO);
        }
        commodityForActivityVOList.sort(Comparator.comparing(CommodityActivityRespVO::getIsShow).reversed());
        return commodityForActivityVOList;
    }

    @Override
    public void updateCommodityName(Long commodityId, String commodityName) {
        LambdaUpdateWrapper<CommodityActivityDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CommodityActivityDO::getCommodityId, commodityId);
        updateWrapper.set(CommodityActivityDO::getCommodityName, commodityName);
        activityMapper.update(updateWrapper);
    }

    @Override
    public void updateCommodityNameAndImage(Long commodityId, String commodityName, String image) {
        LambdaUpdateWrapper<CommodityActivityDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CommodityActivityDO::getCommodityId, commodityId);
        updateWrapper.set(CommodityActivityDO::getCommodityName, commodityName);
        updateWrapper.set(CommodityActivityDO::getThumbnailUrl, image);
        activityMapper.update(updateWrapper);
    }

    @Override
    public List<CommodityActivityDTO> getCommodityActivityList(CommodityActivityVO body) {
        if (body == null || body.getCommodityIds() == null || body.getCommodityIds().isEmpty()) {
            throw new ServiceException(1212,"商品ID参数不能为空");
        }
        log.info("-----------------------------查询活动商品列表，body={}", body);
        LambdaQueryWrapper<CommodityActivityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommodityActivityDO::getIsActive, 1);
        wrapper.in(ObjectUtil.isNotEmpty(body.getCommodityIds()),
                CommodityActivityDO::getCommodityId, body.getCommodityIds());
        List<CommodityActivityDO> commodityActivityDOS = activityMapper.selectList(wrapper);
        return BeanUtils.toBean(commodityActivityDOS, CommodityActivityDTO.class);
    }
}