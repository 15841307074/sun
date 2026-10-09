package com.htyoudao.youdao.module.system.service.business;

import com.htyoudao.youdao.module.system.dal.dataobject.businessuser.BusinessUserDO;

import java.util.List;

public interface BusinessUserService {

    List<BusinessUserDO> getBusinessUserDOS(Long businessId, Long userId);
}
