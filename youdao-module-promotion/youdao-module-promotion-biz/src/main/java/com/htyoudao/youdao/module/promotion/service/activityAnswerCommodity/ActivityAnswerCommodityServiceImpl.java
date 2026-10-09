package com.htyoudao.youdao.module.promotion.service.activityAnswerCommodity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.ActivityAnswerCommodityMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RefreshScope
public class ActivityAnswerCommodityServiceImpl extends
    ServiceImpl<ActivityAnswerCommodityMapper, ActivityAnswerCommodityDO> implements ActivityAnswerCommodityService {

    @Resource
    private ActivityAnswerCommodityMapper activityAnswerCommodityMapper;

    @Resource
    private CommodityApi commodityApi;

    /**
     * 批量创建有奖问答指定商品关系。
     */
    @Override
    public void createBatch(List<Long> commodityIds, Long activityId) {
        List<ActivityAnswerCommodityDO> answerCommodityList = new ArrayList<>();
        for (Long commodityId : commodityIds) {
            ActivityAnswerCommodityDO answerCommodityDO = new ActivityAnswerCommodityDO();
            answerCommodityDO.setCommodityId(commodityId);
            answerCommodityDO.setActivityId(activityId);
            answerCommodityList.add(answerCommodityDO);
        }
        this.saveBatch(answerCommodityList);
    }

    /**
     * 删除活动关联的指定商品关系。
     */
    @Override
    public void deleteByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivityAnswerCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityAnswerCommodityDO::getActivityId, activityId);
        activityAnswerCommodityMapper.delete(queryWrapper);
    }

    /**
     * 查询活动关联的指定商品关系。
     */
    @Override
    public List<ActivityAnswerCommodityDO> selectByActivityId(Long id) {
        LambdaQueryWrapper<ActivityAnswerCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityAnswerCommodityDO::getActivityId, id);
        return activityAnswerCommodityMapper.selectList(queryWrapper);
    }

    /**
     * 查询活动关联的商品详情。
     */
    @Override
    public List<CommodityDTO> listByActivityId(Long id) {
        List<ActivityAnswerCommodityDO> commodityDOS = this.selectByActivityId(id);
        if (CollectionUtils.isEmpty(commodityDOS)) {
            return List.of();
        }
        List<Long> commIds = commodityDOS.stream().map(mm -> mm.getCommodityId())
            .collect(Collectors.toList());
        CommonResult<List<CommodityDTO>> commodityList = commodityApi.getCommodityList(commIds);
        return commodityList.getData();
    }
}
