package com.htyoudao.youdao.module.member.dal.mysql.tagvalue;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.member.controller.admin.tag.vo.MemberTagVO;
import com.htyoudao.youdao.module.member.controller.admin.tag.vo.TagValuePageReqVO;
import com.htyoudao.youdao.module.member.dal.dataobject.tagvalue.TagValueDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签 Mapper
 *
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

    List<MemberTagVO> getMemberTageListByMemberId(@Param("memberIds") List<Long> memberIds);
}