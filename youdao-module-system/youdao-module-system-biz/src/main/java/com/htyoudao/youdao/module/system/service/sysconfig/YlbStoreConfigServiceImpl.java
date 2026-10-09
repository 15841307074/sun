package com.htyoudao.youdao.module.system.service.sysconfig;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.module.system.dal.dataobject.sysconfig.YlbStoreConfigDO;
import com.htyoudao.youdao.module.system.dal.mysql.sysconfig.YlbStoreConfigMapper;
import org.springframework.stereotype.Service;

/**
 * 云喇叭门店配置 服务实现类
 */
@Service
public class YlbStoreConfigServiceImpl extends ServiceImpl<YlbStoreConfigMapper, YlbStoreConfigDO>
        implements YlbStoreConfigService {

}

