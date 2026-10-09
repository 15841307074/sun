package com.htyoudao.youdao.module.system.dal.mysql.business;


import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.business.BusinessDO;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import com.htyoudao.youdao.module.system.controller.admin.business.vo.*;

/**
 * 项目 Mapper
 *
 * @author 零零玖零
 */
@Mapper
public interface BusinessMapper extends BaseMapperX<BusinessDO> {

    default PageResult<BusinessDO> selectPage(BusinessPageReqVO reqVO) {

        LambdaQueryWrapperX<BusinessDO> queryWrapper = new LambdaQueryWrapperX<BusinessDO>()
            .eqIfPresent(BusinessDO::getCode, reqVO.getCode())
            .likeIfPresent(BusinessDO::getName, reqVO.getName())
            .eqIfPresent(BusinessDO::getManageType, reqVO.getManageType())
            .eqIfPresent(BusinessDO::getStatus, reqVO.getStatus())
            .betweenIfPresent(BusinessDO::getValidityStartTime, reqVO.getValidityTime());

        if (StringUtils.isNotBlank(reqVO.getKeyword())){
            queryWrapper.and(
                c -> c.like(BusinessDO::getCode, reqVO.getKeyword())
                    .or().like(BusinessDO::getName, reqVO.getKeyword())
            );
        }
        queryWrapper.orderByDesc(BusinessDO::getId);

        return selectPage(reqVO, queryWrapper);
    }


    default BusinessDO selectByName(String name){
        return selectOne(BusinessDO::getName, name);
    }
}