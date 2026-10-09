package com.htyoudao.youdao.module.infra.dal.mysql.group;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.infra.controller.admin.group.vo.GroupPageReqVO;
import com.htyoudao.youdao.module.infra.dal.dataobject.group.GroupDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 群 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface GroupMapper extends BaseMapperX<GroupDO> {

    default PageResult<GroupDO> selectPage(GroupPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<GroupDO>()
                .likeIfPresent(GroupDO::getName, reqVO.getName())
                .eqIfPresent(GroupDO::getOwnerId, reqVO.getOwnerId())
                .eqIfPresent(GroupDO::getHeadImage, reqVO.getHeadImage())
                .eqIfPresent(GroupDO::getHeadImageThumb, reqVO.getHeadImageThumb())
                .eqIfPresent(GroupDO::getNotice, reqVO.getNotice())
                .eqIfPresent(GroupDO::getIsBanned, reqVO.getIsBanned())
                .eqIfPresent(GroupDO::getReason, reqVO.getReason())
                .eqIfPresent(GroupDO::getDissolve, reqVO.getDissolve())
                .betweenIfPresent(GroupDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(GroupDO::getId));
    }

}
