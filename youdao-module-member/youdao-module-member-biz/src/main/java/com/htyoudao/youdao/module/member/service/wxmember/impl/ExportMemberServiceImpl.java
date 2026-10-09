package com.htyoudao.youdao.module.member.service.wxmember.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.util.concurrent.RateLimiter;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.constant.WxMemberConstant;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberExcelRespVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberReqVO;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.MemberCrowdRefDO;
import com.htyoudao.youdao.module.member.dal.dataobject.membertag.WxMemberTagDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmembercard.WxMemberCardDO;
import com.htyoudao.youdao.module.member.dal.mysql.wxmember.WxMemberMapper;
import com.htyoudao.youdao.module.member.dal.mysql.wxmembercard.WxMemberCardMapper;
import com.htyoudao.youdao.module.member.service.wxmember.ExportMemberService;
import com.htyoudao.youdao.module.member.service.wxmembercard.IWxMemberCardService;
import com.htyoudao.youdao.module.member.util.DateUtils;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.*;

/**
 * @author dht
 */
@Service
@DS(DsNameConstants.SHARDING)
public class ExportMemberServiceImpl implements ExportMemberService {

    @Resource
    private WxMemberMapper wxMemberMapper;

    private static final RateLimiter memberListLimiter = RateLimiter.create(5);

    @Resource
    private IWxMemberCardService wxMemberCardService;

    @DubboReference
    private StoreApi storeApi;



    /**
     * 获取导出列表
     *
     * @param param         param
     * @param wxMemberReqVO wxMemberReqVO
     * @param storeMap      storeMap
     * @return WxMemberExcelRespVO
     */
    @Override
    public List<WxMemberExcelRespVO> getMemberExportList(Page<WxMemberExcelRespVO> param, WxMemberReqVO wxMemberReqVO, Map<Long, String> storeMap,Map<Long, String> storeIdToOrgNameMap,Map<Long, String> memberTagMap,Map<Long, BigDecimal> runnerBalanceMap) {

        boolean acquire = memberListLimiter.tryAcquire();
        if (!acquire) {
            throw exception(WX_MEMBER_GET_LIMITER);
        }
        // 会员卡等级
        List<WxMemberCardDO> list = wxMemberCardService.list();
        Map<Integer, WxMemberCardDO> collect = list.stream().collect(Collectors.toMap(WxMemberCardDO::getMemberLevel, c -> c));
        MPJLambdaWrapper<WxMemberDO> queryWrapper = new MPJLambdaWrapper<>();
        // 会员手机号
        if (StringUtils.isNotEmpty(wxMemberReqVO.getMemberMobile())) {
            queryWrapper.and(wrapper ->
                    wrapper.eq(WxMemberDO::getMemberMobile, wxMemberReqVO.getMemberMobile())
                            .or()
                            .eq(WxMemberDO::getMemberNickName, wxMemberReqVO.getMemberMobile())
            );
        }
        //性别
        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getGender()),WxMemberDO::getGender, wxMemberReqVO.getGender());
        //首次下单门店
        if (ObjectUtil.isNotEmpty(wxMemberReqVO.getDeptId())) {
            if(ObjectUtil.isEmpty(wxMemberReqVO.getIsStore())){
                throw exception(WX_MEMBER_IS_STORE_NOT_NULL);
            }
            if(Objects.equals(wxMemberReqVO.getIsStore(), WxMemberConstant.IS_STORE_0)){
                CommonResult<List<Long>> commonResult = storeApi.getStoreIdsByDeptId(wxMemberReqVO.getDeptId());
                List<Long> longs = JSON.parseArray(JSON.toJSONString(commonResult.getData()), Long.class);
                if (CollectionUtil.isEmpty(longs)) {
                    return List.of();
                }
                queryWrapper.in(WxMemberDO::getFirstOrderStoreId, longs);
            }else {
                queryWrapper.eq(WxMemberDO::getFirstOrderStoreId, wxMemberReqVO.getDeptId());
            }

        }

        // 会员状态
        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getMemberStatus()),WxMemberDO::getMemberStatus, wxMemberReqVO.getMemberStatus());
        // 会员类型
        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getMemberType()),WxMemberDO::getUserIdentity, wxMemberReqVO.getMemberType());
        // 会员来源
        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getMemberCategory()),WxMemberDO::getMemberCategory, wxMemberReqVO.getMemberCategory());
        // 注册时间
        queryWrapper.between(ObjectUtils.isNotEmpty(wxMemberReqVO.getRegisterStartTime()) && ObjectUtils.isNotEmpty(wxMemberReqVO.getRegisterEndTime()),WxMemberDO::getRegisterTime, wxMemberReqVO.getRegisterStartTime(), wxMemberReqVO.getRegisterEndTime());
        // 最后登录时间
        queryWrapper.between(ObjectUtils.isNotEmpty(wxMemberReqVO.getLastLoginStartTime()) && ObjectUtils.isNotEmpty(wxMemberReqVO.getLastLoginEndTime()),WxMemberDO::getLastLoginTime, wxMemberReqVO.getLastLoginStartTime(), wxMemberReqVO.getLastLoginEndTime());
        // 最后下单时间
        queryWrapper.between(ObjectUtils.isNotEmpty(wxMemberReqVO.getLastOrderStartTime()) && ObjectUtils.isNotEmpty(wxMemberReqVO.getLastOrderEndTime()),WxMemberDO::getFinalOrderFinishTime, wxMemberReqVO.getLastOrderStartTime(), wxMemberReqVO.getLastOrderEndTime());

        //活跃度
        if(ObjectUtil.isNotEmpty(wxMemberReqVO.getLiveness())){
            // 活跃用户：7天内，uv访问次数＞3
            if(Objects.equals(wxMemberReqVO.getLiveness(), WxMemberConstant.LIVENESS_0)){
                queryWrapper.ge(WxMemberDO::getFourthLoginTime, DateUtils.getDaysBeforeStart(7));
            }
            // 有效用户：7天内，均单数＞2
            if(Objects.equals(wxMemberReqVO.getLiveness(), WxMemberConstant.LIVENESS_1)){
                queryWrapper.ge(WxMemberDO::getThirdOrderFinishTime, DateUtils.getDaysBeforeStart(7));
            }
            // 待促活用户：7天内未下单，14天内下过单
            if(Objects.equals(wxMemberReqVO.getLiveness(), WxMemberConstant.LIVENESS_2)){
                queryWrapper.between(WxMemberDO::getFinalOrderFinishTime, DateUtils.getDaysBeforeStart(7), DateUtils.getDaysBeforeStart(14));
            }
            // 半休眠用户：14天内未下单，30天内下过单
            if(Objects.equals(wxMemberReqVO.getLiveness(), WxMemberConstant.LIVENESS_3)){
                queryWrapper.between(WxMemberDO::getFinalOrderFinishTime, DateUtils.getDaysBeforeStart(14), DateUtils.getDaysBeforeStart(30));
            }
            // 休眠用户：30天内未下单，90天内下过单
            if(Objects.equals(wxMemberReqVO.getLiveness(), WxMemberConstant.LIVENESS_4)){
                queryWrapper.between(WxMemberDO::getFinalOrderFinishTime, DateUtils.getDaysBeforeStart(30), DateUtils.getDaysBeforeStart(90));
            }
            // 无效用户：＞90天未下单
            if(Objects.equals(wxMemberReqVO.getLiveness(), WxMemberConstant.LIVENESS_5)){
                queryWrapper.le(WxMemberDO::getThirdOrderFinishTime, DateUtils.getDaysBeforeStart(90));
            }
        }


        // 会员等级
        Integer grade = wxMemberReqVO.getGrade();
        if (ObjectUtils.isNotEmpty(grade)) {
            // 会员等级

            if (Objects.equals(grade, WxMemberConstant.GRADE_1)) {
                WxMemberCardDO wxMemberCard = collect.get(grade);
                queryWrapper.le(WxMemberDO::getIntegralFrozen, wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(grade, WxMemberConstant.GRADE_2)) {
                WxMemberCardDO wxMemberCard = collect.get(grade);
                queryWrapper.between(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold(), wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(grade, WxMemberConstant.GRADE_3)) {
                WxMemberCardDO wxMemberCard = collect.get(grade);
                queryWrapper.between(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold(), wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(grade, WxMemberConstant.GRADE_4)) {
                WxMemberCardDO wxMemberCard = collect.get(grade);
                queryWrapper.between(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold(), wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(grade, WxMemberConstant.GRADE_5)) {
                WxMemberCardDO wxMemberCard = collect.get(grade);
                queryWrapper.ge(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold());
            }
        }

        extracted(wxMemberReqVO, queryWrapper);
        // 倒序
        queryWrapper.orderByDesc("register_time");
        // 会员标签
        Integer memberLabel = wxMemberReqVO.getMemberLabel();
        if (ObjectUtil.isNotEmpty(memberLabel)) {
            queryWrapper.eq(WxMemberDO::getMemberLabel, memberLabel);
        }
        if(ObjectUtil.isNotEmpty(wxMemberReqVO.getCrowdId())){
            queryWrapper.leftJoin(MemberCrowdRefDO.class, MemberCrowdRefDO::getMemberId, WxMemberDO::getMemberId);
            queryWrapper.eq(MemberCrowdRefDO::getCrowdId, wxMemberReqVO.getCrowdId());
        }

        if(ObjectUtil.isNotEmpty(wxMemberReqVO.getTagId())){
            queryWrapper.leftJoin(WxMemberTagDO.class, WxMemberTagDO::getMemberId, WxMemberDO::getMemberId);
            queryWrapper.eq(WxMemberTagDO::getTagId, wxMemberReqVO.getTagId());
            queryWrapper.eq(WxMemberTagDO::getDeleted, false);
        }

        queryWrapper.select(WxMemberDO::getMemberId,
                WxMemberDO::getMemberId,
                WxMemberDO::getMemberNickName,
                WxMemberDO::getMemberMobile,
                WxMemberDO::getGrade,
                WxMemberDO::getUserIdentity,
                WxMemberDO::getMemberStatus,
                WxMemberDO::getGender,
                WxMemberDO::getMemberBirthday,
                WxMemberDO::getMemberIntegral,
                WxMemberDO::getFreezePoints,
                WxMemberDO::getTotalOrderNum,
                WxMemberDO::getFinalOrderFinishTime,
                WxMemberDO::getMemberCategory,
                WxMemberDO::getRegisterTime,
                WxMemberDO::getLastLoginTime,
                WxMemberDO::getFirstOrderStoreId,
                WxMemberDO::getIntegralFrozen);
        Page<WxMemberDO> page = wxMemberMapper.selectPage(new Page<>(param.getCurrent(), param.getSize()), queryWrapper);

        List<WxMemberDO> records = page.getRecords();
        List<WxMemberExcelRespVO> voList = BeanUtils.toBean(records, WxMemberExcelRespVO.class);

        if (CollectionUtil.isNotEmpty(voList)) {
            for (WxMemberExcelRespVO vo : voList) {
                Long firstOrderStoreId = vo.getFirstOrderStoreId();
                if (storeMap.containsKey(firstOrderStoreId)) {
                    vo.setFirstOrderStoreName(storeMap.get(firstOrderStoreId));
                    vo.setOrgName(storeIdToOrgNameMap.get(firstOrderStoreId));
                }
                if (memberTagMap.containsKey(vo.getMemberId())) {
                    vo.setMemberTag(memberTagMap.get(vo.getMemberId()));
                }
                if (runnerBalanceMap.containsKey(vo.getMemberId())) {
                    vo.setBalance(runnerBalanceMap.get(vo.getMemberId()));
                }
                Long integralFrozen = vo.getIntegralFrozen();
                vo.setGrade(5);
                for (WxMemberCardDO wxMemberCard : list) {
                    if (integralFrozen <= wxMemberCard.getMaxPointsThreshold()) {
                        vo.setGrade(wxMemberCard.getMemberLevel());
                        break;
                    }
                }
            }
        }
        return voList;
    }

    private static void extracted(WxMemberReqVO wxMemberReqVO, MPJLambdaWrapper<WxMemberDO> queryWrapper) {
        Integer orderFrequencyTime = wxMemberReqVO.getOrderFrequencyTime();
        Integer orderFrequency = wxMemberReqVO.getOrderFrequency();
        if ((ObjectUtil.isEmpty(orderFrequencyTime) && ObjectUtil.isNotEmpty(orderFrequency)) || (ObjectUtil.isNotEmpty(orderFrequencyTime) && ObjectUtil.isEmpty(orderFrequency))) {
            throw exception(WX_MEMBER_ORDER_FREQUENCY);
        } else {
            // 下单频次时间
            if (Objects.equals(orderFrequencyTime, WxMemberConstant.MEMBER_TIME_0)) {
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_0)) {
                    queryWrapper.gt(WxMemberDO::getThirtyDayOrderCount, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_1)) {
                    queryWrapper.between(WxMemberDO::getThirtyDayOrderCount, 2, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_2)) {
                    queryWrapper.eq(WxMemberDO::getThirtyDayOrderCount, 1);
                }
            }

            if (Objects.equals(orderFrequencyTime, WxMemberConstant.MEMBER_TIME_1)) {
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_0)) {
                    queryWrapper.gt(WxMemberDO::getSevenDayOrderCount, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_1)) {
                    queryWrapper.between(WxMemberDO::getSevenDayOrderCount, 2, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_2)) {
                    queryWrapper.eq(WxMemberDO::getSevenDayOrderCount, 1);
                }
            }

            if (Objects.equals(orderFrequencyTime, WxMemberConstant.MEMBER_TIME_2)) {
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_0)) {
                    queryWrapper.gt(WxMemberDO::getHalfYearOrderCount, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_1)) {
                    queryWrapper.between(WxMemberDO::getHalfYearOrderCount, 2, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_2)) {
                    queryWrapper.eq(WxMemberDO::getHalfYearOrderCount, 1);
                }
            }

            if (Objects.equals(orderFrequencyTime, WxMemberConstant.MEMBER_TIME_3)) {
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_0)) {
                    queryWrapper.gt(WxMemberDO::getOneYearOrderCount, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_1)) {
                    queryWrapper.between(WxMemberDO::getOneYearOrderCount, 2, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_2)) {
                    queryWrapper.eq(WxMemberDO::getOneYearOrderCount, 1);
                }
            }
        }

        Integer orderAvg = wxMemberReqVO.getOrderAvg();
        Integer orderAvgTime = wxMemberReqVO.getOrderAvgTime();
        if ((ObjectUtil.isEmpty(orderAvg) && ObjectUtil.isNotEmpty(orderAvgTime)) || (ObjectUtil.isNotEmpty(orderAvg) && ObjectUtil.isEmpty(orderAvgTime))) {
            throw exception(WX_MEMBER_ORDER_AVG_TIME);
        } else {
            // 下单时间
            if (Objects.equals(orderAvgTime, WxMemberConstant.MEMBER_TIME_0)) {
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_0)) {
                    queryWrapper.between(WxMemberDO::getThirtyDayOrderAvg, 0, 8);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_1)) {
                    queryWrapper.between(WxMemberDO::getThirtyDayOrderAvg, 8, 11);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_2)) {
                    queryWrapper.between(WxMemberDO::getThirtyDayOrderAvg, 11, 14);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_3)) {
                    queryWrapper.between(WxMemberDO::getThirtyDayOrderAvg, 14, 17);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_4)) {
                    queryWrapper.gt(WxMemberDO::getThirtyDayOrderAvg, 17);
                }
            }

            if (Objects.equals(orderAvgTime, WxMemberConstant.MEMBER_TIME_1)) {
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_0)) {
                    queryWrapper.between(WxMemberDO::getSevenDayOrderAvg, 0, 8);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_1)) {
                    queryWrapper.between(WxMemberDO::getSevenDayOrderAvg, 8, 11);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_2)) {
                    queryWrapper.between(WxMemberDO::getSevenDayOrderAvg, 11, 14);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_3)) {
                    queryWrapper.between(WxMemberDO::getSevenDayOrderAvg, 14, 17);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_4)) {
                    queryWrapper.gt(WxMemberDO::getSevenDayOrderAvg, 17);
                }
            }

            if (Objects.equals(orderAvgTime, WxMemberConstant.MEMBER_TIME_2)) {
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_0)) {
                    queryWrapper.between(WxMemberDO::getHalfYearOrderAvg, 0, 8);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_1)) {
                    queryWrapper.between(WxMemberDO::getHalfYearOrderAvg, 8, 11);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_2)) {
                    queryWrapper.between(WxMemberDO::getHalfYearOrderAvg, 11, 14);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_3)) {
                    queryWrapper.between(WxMemberDO::getHalfYearOrderAvg, 14, 17);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_4)) {
                    queryWrapper.gt(WxMemberDO::getHalfYearOrderAvg, 17);
                }
            }

            if (Objects.equals(orderAvgTime, WxMemberConstant.MEMBER_TIME_3)) {
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_0)) {
                    queryWrapper.lt(WxMemberDO::getOneYearOrderAvg, 3);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_1)) {
                    queryWrapper.between(WxMemberDO::getOneYearOrderAvg, 2, 3);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_2)) {
                    queryWrapper.eq(WxMemberDO::getOneYearOrderAvg, 1);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_3)) {
                    queryWrapper.between(WxMemberDO::getOneYearOrderAvg, 14, 17);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_4)) {
                    queryWrapper.gt(WxMemberDO::getOneYearOrderAvg, 17);
                }
            }
        }
        Integer orderInterval = wxMemberReqVO.getOrderInterval();
        if (ObjectUtil.isNotEmpty(orderInterval)) {
            switch (orderInterval) {
                case 0:
                    queryWrapper.apply(" DATEDIFF(final_order_finish_time,second_order_finish_time) < 10");
                    break;
                case 1:
                    queryWrapper.apply(" DATEDIFF(final_order_finish_time,second_order_finish_time) >= 10 and DATEDIFF(final_order_finish_time,second_order_finish_time) < 20");
                    break;
                case 2:
                    queryWrapper.apply(" DATEDIFF(final_order_finish_time,second_order_finish_time) >= 21 and DATEDIFF(final_order_finish_time,second_order_finish_time) < 30");
                    break;
                default:
                    break;
            }
        }
    }

}
