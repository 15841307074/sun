package com.htyoudao.youdao.module.promotion.dal.mysql.exchangelog;


import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.controller.admin.activityexchangelog.vo.ExchangeLogPageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.exchangelog.ActivityExchangeLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动的兑换记录 Mapper
 *
 * @author lzw
 */
@Mapper
public interface ExchangeLogMapper extends BaseMapperX<ActivityExchangeLogDO> {

//    /**
//     * 查询活动的兑换记录分页
//     * @param reqVO reqVO
//     * @return PageResult
//     */
//    default PageResult<ActivityExchangeLogDO> selectPage(ExchangeLogPageReqVO reqVO) {
//        return selectPage(reqVO, new LambdaQueryWrapperX<ActivityExchangeLogDO>()
//                .eqIfPresent(ActivityExchangeLogDO::getActivityId, reqVO.getActivityId())
//                .eqIfPresent(ActivityExchangeLogDO::getAwardType, reqVO.getAwardType())
//                .betweenIfPresent(ActivityExchangeLogDO::getCreateTime, reqVO.getCreateTime())
//                .and(query -> {
//                    query.like(ObjectUtil.isNotEmpty(reqVO.getNameOrMobile()), ActivityExchangeLogDO::getMemberMobile, reqVO.getNameOrMobile())
//                            .or()
//                            .like(ObjectUtil.isNotEmpty(reqVO.getNameOrMobile()), ActivityExchangeLogDO::getMemberNickName, reqVO.getNameOrMobile());
//                })
//                .orderByDesc(ActivityExchangeLogDO::getCreateTime));
//    }

}