package com.htyoudao.youdao.module.system.api.dept;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.service.dept.DeptService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class DeptOrgApiImpl implements DeptOrgApi {

    @Resource
    private DeptService deptService;

    @Override
    public CommonResult<List<Long>> getUserIdsByDept() {
        return success(deptService.getUserIdsByDept());
    }
}
