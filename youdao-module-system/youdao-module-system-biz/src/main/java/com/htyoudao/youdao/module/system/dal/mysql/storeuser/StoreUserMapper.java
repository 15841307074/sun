package com.htyoudao.youdao.module.system.dal.mysql.storeuser;

import java.util.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.storeuser.StoreUserDO;
import org.apache.ibatis.annotations.Mapper;
import com.htyoudao.youdao.module.system.controller.admin.storeuser.vo.*;
import org.apache.ibatis.annotations.Param;

/**
 * 门店和用户关联 Mapper
 *
 * @author 零零玖零
 */
@Mapper
public interface StoreUserMapper extends BaseMapperX<StoreUserDO> {

    default PageResult<StoreUserDO> selectPage(StoreUserPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StoreUserDO>()
                .eqIfPresent(StoreUserDO::getUserId, reqVO.getUserId())
                .eqIfPresent(StoreUserDO::getStoreId, reqVO.getStoreId())
                .betweenIfPresent(StoreUserDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(StoreUserDO::getType, reqVO.getType())
                .eqIfPresent(StoreUserDO::getBusinessId, reqVO.getBusinessId())
                .orderByDesc(StoreUserDO::getId));
    }

    /**
     * 根据storeId
     * @param objectPage page
     * @param pageReqVO pageReqVO
     * @return OrgUserPageRespVO
     */
    Page<OrgUserPageRespVO> getUserListByStoreId(@Param("page") Page<OrgUserPageRespVO> objectPage,@Param("pageReqVO") com.htyoudao.youdao.module.system.controller.admin.org.vo.StoreUserPageReqVO pageReqVO);
}