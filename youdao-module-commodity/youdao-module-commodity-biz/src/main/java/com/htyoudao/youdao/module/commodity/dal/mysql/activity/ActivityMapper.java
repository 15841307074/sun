package com.htyoudao.youdao.module.commodity.dal.mysql.activity;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.commodity.dal.dataobject.activity.CommodityActivityDO;
import org.apache.ibatis.annotations.Mapper;
import com.htyoudao.youdao.module.commodity.controller.admin.activity.vo.*;

/**
 * 商品活动 Mapper
 *
 * @author hhhh
 */
@Mapper
public interface ActivityMapper extends BaseMapperX<CommodityActivityDO> {

    /**
     * 查询商品活动分页
     * @param reqVO reqVO
     * @return PageResult
     */
    default PageResult<CommodityActivityDO> selectPage(CommodityActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CommodityActivityDO>()
                .eqIfPresent(CommodityActivityDO::getCommodityId, reqVO.getCommodityId())
                .eqIfPresent(CommodityActivityDO::getCategoryId, reqVO.getCategoryId())
                .likeIfPresent(CommodityActivityDO::getCommodityName, reqVO.getCommodityName())
                .eqIfPresent(CommodityActivityDO::getThumbnailUrl, reqVO.getThumbnailUrl())
                .eqIfPresent(CommodityActivityDO::getIsSingle, reqVO.getIsSingle())
                .eqIfPresent(CommodityActivityDO::getSkuIds, reqVO.getSkuIds())
                .eqIfPresent(CommodityActivityDO::getActivityType, reqVO.getActivityType())
                .betweenIfPresent(CommodityActivityDO::getActivityStartDate, reqVO.getActivityStartDate())
                .betweenIfPresent(CommodityActivityDO::getActivityEndDate, reqVO.getActivityEndDate())
                .eqIfPresent(CommodityActivityDO::getActivityDescription, reqVO.getActivityDescription())
                .eqIfPresent(CommodityActivityDO::getIsActive, reqVO.getIsActive())
                .betweenIfPresent(CommodityActivityDO::getCreateTime, reqVO.getCreatedTime())
                .betweenIfPresent(CommodityActivityDO::getUpdateTime, reqVO.getUpdatedTime())
                .eqIfPresent(CommodityActivityDO::getBusinessId, reqVO.getBusinessId())
                .orderByDesc(CommodityActivityDO::getActivityId));
    }

}