package com.htyoudao.youdao.module.member.dal.mysql.wxmember;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface WxMemberMapper extends BaseMapperX<WxMemberDO> {


    Set<String> getSmsPhone(@Param("key") Integer key, @Param("value") List<Long> value);

    List<Long> getCommunityList(@Param("busId") String busId);

    List<Long> getNotInCommunityList(@Param("busId") String busId);

    @DS(DsNameConstants.MASTER)
    void resetCommunityFlag(@Param("key") int key);

    @DS(DsNameConstants.MASTER)
    void setCommunityFlag(@Param("key") int key);
}
