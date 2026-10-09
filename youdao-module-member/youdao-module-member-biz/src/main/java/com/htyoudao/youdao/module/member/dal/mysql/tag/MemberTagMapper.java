package com.htyoudao.youdao.module.member.dal.mysql.tag;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.member.dal.dataobject.tag.MemberTagDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员标签关系表 Mapper
 *
 */
@Mapper
public interface MemberTagMapper extends BaseMapperX<MemberTagDO> {
}