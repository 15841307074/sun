package com.htyoudao.youdao.module.member.dal.mysql.crowd;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.member.controller.admin.customcrowd.vo.CustomCrowdPageReqVO;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.CustomCrowdDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 自定义人群 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CustomCrowdMapper extends BaseMapperX<CustomCrowdDO> {

    default PageResult<CustomCrowdDO> selectPage(CustomCrowdPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CustomCrowdDO>()
                .likeIfPresent(CustomCrowdDO::getCrowdName, reqVO.getCrowdName())
                .orderByDesc(CustomCrowdDO::getId));
    }

}