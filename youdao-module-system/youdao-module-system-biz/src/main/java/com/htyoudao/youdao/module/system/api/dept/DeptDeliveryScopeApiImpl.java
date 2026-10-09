package com.htyoudao.youdao.module.system.api.dept;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptDeliveryScopeDTO;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptRespDTO;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.delivery.SysDeptDeliveryScopeReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.DeptDeliveryScopeDO;
import com.htyoudao.youdao.module.system.service.dept.DeptDeliveryScopeService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@DubboService // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class DeptDeliveryScopeApiImpl implements DeptDeliveryScopeApi {

    @Resource
    private DeptDeliveryScopeService deptDeliveryScopeService;
    @Override
    public CommonResult<List<DeptDeliveryScopeDTO>> getDeptDeliveryScope(DeptDeliveryScopeDTO deptDeliveryScopeDTO) {
        List<DeptDeliveryScopeDO> deptDeliveryScopeDO = deptDeliveryScopeService.selectSysDeptDeliveryScopeList(deptDeliveryScopeDTO);
        return CommonResult.success(BeanUtils.toBean(deptDeliveryScopeDO, DeptDeliveryScopeDTO.class));
    }
}
