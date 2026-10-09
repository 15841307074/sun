package com.htyoudao.youdao.module.promotion.service.appletnotice;


import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.promotion.dal.dataobject.appletNoticePushTemplate.AppletNoticePushTemplate;

import java.util.List;

/**
 * @author dht
 */
public interface AppletNoticePushTemplateService extends IService<AppletNoticePushTemplate> {

    List<AppletNoticePushTemplate> getListByTemplateType(Integer templateType);

}
