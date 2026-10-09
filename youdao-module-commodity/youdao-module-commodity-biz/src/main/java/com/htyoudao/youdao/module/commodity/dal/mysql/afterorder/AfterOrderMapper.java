package com.htyoudao.youdao.module.commodity.dal.mysql.afterorder;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.commodity.api.DTO.AfterInfoDTO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.afterorder.AfterOrderDO;
import org.apache.ibatis.annotations.Mapper;
import com.htyoudao.youdao.module.commodity.controller.admin.afterorder.vo.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 订单生成后加购商品 Mapper
 *
 * @author 零零玖零
 */
@Mapper
public interface AfterOrderMapper extends BaseMapperX<AfterOrderDO> {

    /**
     * 分页查询订单生成后加购商品
     * @param reqVO reqVO
     * @return PageResult
     */
    default PageResult<AfterOrderDO> selectPage(AfterOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AfterOrderDO>()
                .eqIfPresent(AfterOrderDO::getCommodityId, reqVO.getCommodityId())
                .eqIfPresent(AfterOrderDO::getAfterPrice, reqVO.getAfterPrice())
                .eqIfPresent(AfterOrderDO::getStrikeThroughPrice, reqVO.getStrikeThroughPrice())
                .eqIfPresent(AfterOrderDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(AfterOrderDO::getCreateTime, reqVO.getCreateTime())
                .likeIfPresent(AfterOrderDO::getCommodityName, reqVO.getCommodityName())
                .eqIfPresent(AfterOrderDO::getThumbnailUrl, reqVO.getThumbnailUrl())
                .orderByDesc(AfterOrderDO::getAfterId));
    }

    /**
     * 获取订单生成后加购商品列表
     *
     * @param afterIds
     * @return List
     */
    List<AfterInfoDTO> selectAfterListForRpc(@Param("afterIds") Set<Long> afterIds, @Param("storeId") Long storeId);

}