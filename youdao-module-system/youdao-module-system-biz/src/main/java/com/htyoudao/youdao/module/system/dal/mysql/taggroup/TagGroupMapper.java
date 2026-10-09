package com.htyoudao.youdao.module.system.dal.mysql.taggroup;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.controller.admin.tag.vo.TagGroupPageReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.taggroup.TagGroupDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签组 Mapper
 *
 * @author dht
 */
@Mapper
public interface TagGroupMapper extends BaseMapperX<TagGroupDO> {

    /**
     * 分页查询标签组
     * @param reqVO 查询条件
     * @return PageResult
     */
    default PageResult<TagGroupDO> selectPage(TagGroupPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TagGroupDO>()
                .likeIfPresent(TagGroupDO::getName, reqVO.getName())
                .eqIfPresent(TagGroupDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(TagGroupDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(TagGroupDO::getBusinessId, reqVO.getBusinessId())
                .orderByDesc(TagGroupDO::getId));
    }

}