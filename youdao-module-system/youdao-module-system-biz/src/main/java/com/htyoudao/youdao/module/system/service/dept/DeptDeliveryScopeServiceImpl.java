package com.htyoudao.youdao.module.system.service.dept;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptDeliveryScopeDTO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.DeptDeliveryScopeDO;
import com.htyoudao.youdao.module.system.dal.mysql.dept.DeptDeliveryScopeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeptDeliveryScopeServiceImpl implements DeptDeliveryScopeService{

    @Resource
    private DeptDeliveryScopeMapper deptDeliveryScopeMapper;

    @Override
    public List<DeptDeliveryScopeDO> selectSysDeptDeliveryScopeList(DeptDeliveryScopeDTO deptDeliveryScopeDTO) {
        LambdaQueryWrapper<DeptDeliveryScopeDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DeptDeliveryScopeDO::getStoreId, deptDeliveryScopeDTO.getStoreId());
        return deptDeliveryScopeMapper.selectList(lambdaQueryWrapper);
    }
}
