package com.htyoudao.youdao.module.system.service.dept;

import com.htyoudao.youdao.module.system.api.dept.dto.DeptDeliveryScopeDTO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.DeptDeliveryScopeDO;

import java.util.List;

public interface DeptDeliveryScopeService {
    List<DeptDeliveryScopeDO> selectSysDeptDeliveryScopeList(DeptDeliveryScopeDTO deptDeliveryScopeDTO);
}
