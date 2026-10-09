package com.htyoudao.youdao.module.system.api.business;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.business.dto.BusinessDTO;
import com.htyoudao.youdao.module.system.api.complaint.ComplaintApi;
import com.htyoudao.youdao.module.system.api.printer.dto.PrinterTableVO;
import com.htyoudao.youdao.module.system.service.business.BusinessService;
import com.htyoudao.youdao.module.system.service.complant.ComplaintAppServiceImpl;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@DubboService
@Validated
public class BusinnessImplApi implements BusinnessApi {

    @Resource
    private BusinessService businessService;


    @Override
    public CommonResult<List<BusinessDTO>> listAll() {
        return CommonResult.success(businessService.listAll());
    }

    @Override
    public CommonResult<String> selectByName(Long id) {
        return CommonResult.success(businessService.getBusinessName(id));
    }
}
