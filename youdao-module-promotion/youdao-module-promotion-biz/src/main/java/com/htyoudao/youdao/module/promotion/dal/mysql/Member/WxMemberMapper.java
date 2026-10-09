package com.htyoudao.youdao.module.promotion.dal.mysql.Member;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.wxmember.WxMemberDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface WxMemberMapper extends BaseMapperX<WxMemberDO> {

}
