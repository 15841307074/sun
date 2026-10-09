package com.htyoudao.youdao.module.promotion.service.exchangelog;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.controller.admin.activityexchangelog.vo.ExchangeLogExcel;
import com.htyoudao.youdao.module.promotion.controller.admin.activityexchangelog.vo.ExchangeLogPageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponDateRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.exchangelog.ActivityExchangeLogDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.exchangelog.ExchangeLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * 活动的兑换记录 Service 实现类
 *
 * @author lzw
 */
@Service
@Validated
public class ExchangeLogServiceImpl extends ServiceImpl<ExchangeLogMapper, ActivityExchangeLogDO> implements ExchangeLogService {

    @Resource
    private ExchangeLogMapper exchangeLogMapper;

    @Resource
    private ExcelActionService excelActionService;

    @Override
    public ActivityExchangeLogDO getExchangeLog(Long id) {
        return exchangeLogMapper.selectById(id);
    }

    @Override
    public PageResult<ActivityExchangeLogDO> getExchangeLogPage(ExchangeLogPageReqVO reqVO) {
        LambdaQueryWrapperX<ActivityExchangeLogDO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eqIfPresent(ActivityExchangeLogDO::getActivityId, reqVO.getActivityId());
        queryWrapper.eqIfPresent(ActivityExchangeLogDO::getAwardType, reqVO.getAwardType());
        queryWrapper.betweenIfPresent(ActivityExchangeLogDO::getCreateTime, reqVO.getLotteryStartDate(), reqVO.getLotteryEndDate());
        if(ObjectUtil.isNotEmpty(reqVO.getNameOrMobile())){
            queryWrapper.and(query -> {
                query.eq(ActivityExchangeLogDO::getMemberMobile, reqVO.getNameOrMobile());
            });
        }
        queryWrapper.orderByDesc(ActivityExchangeLogDO::getCreateTime);
        Page<ActivityExchangeLogDO> page = exchangeLogMapper.selectPage(new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), queryWrapper);
        PageResult<ActivityExchangeLogDO> pageResult = PageResult.empty();
        pageResult.setList(page.getRecords());
        pageResult.setTotal(page.getTotal());
        return pageResult;
    }

    @Override
    public Integer getMemberNum(ExchangeLogPageReqVO pageReqVO) {
        QueryWrapper<ActivityExchangeLogDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("COUNT(DISTINCT member_id) as distinct_member_count ");
        queryWrapper.eq("activity_id", pageReqVO.getActivityId());
        List<Map<String, Object>> maps = exchangeLogMapper.selectMaps(queryWrapper);
        if (CollectionUtil.isNotEmpty(maps)) {
            return Integer.parseInt(maps.get(0).get("distinct_member_count").toString());
        }
        return 0;
    }

    @Override
    public void insert(ActivityExchangeLogDO activityExchangeLogDO) {
        exchangeLogMapper.insert(activityExchangeLogDO);
    }

    @Override
    public void export(ExchangeLogPageReqVO activityExchangeLogDO) {
        Page<GoodCouponDateRespVO> page = new Page<>(1, 5000);
        String fileName = "兑换记录--" + activityExchangeLogDO.getActivityId();
        excelActionService.exportAsyncExcel(ExchangeLogExcel.class, param -> this.exportList(activityExchangeLogDO), fileName);
    }

    @Override
    public List<ExchangeLogExcel> exportList(ExchangeLogPageReqVO reqVO) {
        LambdaQueryWrapperX<ActivityExchangeLogDO> activityExchangeLogDOLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
        activityExchangeLogDOLambdaQueryWrapperX.eqIfPresent(ActivityExchangeLogDO::getActivityId, reqVO.getActivityId());
        activityExchangeLogDOLambdaQueryWrapperX.eqIfPresent(ActivityExchangeLogDO::getAwardType, reqVO.getAwardType());
        activityExchangeLogDOLambdaQueryWrapperX.betweenIfPresent(ActivityExchangeLogDO::getCreateTime, reqVO.getLotteryStartDate(), reqVO.getLotteryEndDate());
        if(ObjectUtil.isNotEmpty(reqVO.getNameOrMobile())){
            activityExchangeLogDOLambdaQueryWrapperX.and(query -> {
                query.like(ActivityExchangeLogDO::getMemberMobile, reqVO.getNameOrMobile())
                        .or()
                        .like(ActivityExchangeLogDO::getMemberNickName, reqVO.getNameOrMobile());
            });
        }
        activityExchangeLogDOLambdaQueryWrapperX.orderByDesc(ActivityExchangeLogDO::getCreateTime);
        List<ActivityExchangeLogDO> list = exchangeLogMapper.selectList(activityExchangeLogDOLambdaQueryWrapperX);
        return BeanUtils.toBean(list, ExchangeLogExcel.class);
    }
}