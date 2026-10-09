package com.htyoudao.youdao.module.member.service.appletnotice;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.htyoudao.youdao.module.member.dal.dataobject.appletNoticePushTemplate.AppletNoticePushTemplate;
import com.htyoudao.youdao.module.member.dal.mysql.appletnotice.AppletNoticePushTemplateMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppletNoticePushTemplateServiceImpl extends ServiceImpl<AppletNoticePushTemplateMapper, AppletNoticePushTemplate> implements AppletNoticePushTemplateService {


    @Resource
    private AppletNoticePushTemplateMapper appletNoticePushTemplateMapper;

    @Override
    public List<AppletNoticePushTemplate> getListByTemplateType(Integer templateType) {
        return appletNoticePushTemplateMapper.getListByTemplateType(templateType);
    }
}