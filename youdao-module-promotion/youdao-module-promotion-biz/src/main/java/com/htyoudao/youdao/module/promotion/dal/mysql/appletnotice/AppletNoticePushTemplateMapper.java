package com.htyoudao.youdao.module.promotion.dal.mysql.appletnotice;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.appletNoticePushTemplate.AppletNoticePushTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AppletNoticePushTemplateMapper extends BaseMapper<AppletNoticePushTemplate> {

    @Select("select * from applet_notice_push_template where deleted = 0 and template_type = #{templateType}")
    List<AppletNoticePushTemplate> getListByTemplateType(@Param("templateType") Integer templateType);
}
