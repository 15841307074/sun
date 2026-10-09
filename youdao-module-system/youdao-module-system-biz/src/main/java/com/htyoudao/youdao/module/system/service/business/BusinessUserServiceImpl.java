package com.htyoudao.youdao.module.system.service.business;

import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.businessuser.BusinessUserDO;
import com.htyoudao.youdao.module.system.dal.mysql.businessuser.BusinessUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessUserServiceImpl implements BusinessUserService{

    @Autowired
    private BusinessUserMapper businessUserMapper;

    @Override
    @DataPermission(enable = false)
    public List<BusinessUserDO> getBusinessUserDOS(Long businessId, Long userId) {
        return businessUserMapper.selectList(
                new LambdaQueryWrapperX<BusinessUserDO>()
                        .eq(BusinessUserDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                        .ne(BusinessUserDO::getBusinessId, businessId)
                        .eq(BusinessUserDO::getUserId, userId)
                        .eq(BusinessUserDO::getDeleted, 0)
        );
    }
}
