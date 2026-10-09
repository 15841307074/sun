package com.htyoudao.youdao.module.system.dal.mysql.tagvalue;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.controller.admin.tag.vo.TagValuePageReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.tagvalue.TagValueDO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.ResultHandler;

/**
 * 标签 Mapper
 *
 * @author 零零玖零
 */
@Mapper
public interface TagValueMapper extends BaseMapperX<TagValueDO> {

    /**
     * 分页查询标签
     * @param reqVO 查询条件
     * @return PageResult
     */
    default PageResult<TagValueDO> selectPage(TagValuePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TagValueDO>()
                .likeIfPresent(TagValueDO::getName, reqVO.getName())
                .eqIfPresent(TagValueDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(TagValueDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(TagValueDO::getBusinessId, reqVO.getBusinessId())
                .eqIfPresent(TagValueDO::getTagGroupId, reqVO.getTagGroupId())
                .orderByDesc(TagValueDO::getId));
    }

    @Options(fetchSize = Integer.MIN_VALUE)
    @ResultType(Long.class)
    @Select("SELECT id FROM system_tag_value ${ew.customSqlSegment}")
    void selectTagIdList(@Param("ew") LambdaQueryWrapper<?> wrapper, ResultHandler<Long> handler);

}