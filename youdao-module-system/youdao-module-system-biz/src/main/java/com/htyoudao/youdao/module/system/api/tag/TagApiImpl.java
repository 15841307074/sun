package com.htyoudao.youdao.module.system.api.tag;

import com.htyoudao.youdao.module.system.service.tagvalue.TagValueService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class TagApiImpl implements TagApi {

    @Resource
    private TagValueService tagValueService;

    @Override
    public java.util.Map<Long, String> getNamesByIds(java.util.List<Long> ids) {
        return tagValueService.getNamesByIds(ids);
    }

}
