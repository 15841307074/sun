package com.htyoudao.youdao.module.member.dal.mysql.crowd;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.MemberCrowdRefDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmembercard.WxMemberCardDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WxMemberCrowdRefMapper extends BaseMapperX<MemberCrowdRefDO> {

    /**
     * 执行TRUNCATE清空表
     */
    @Update("TRUNCATE TABLE crowd_member_ref") // 替换为你的表名
    void truncateTable();

}
