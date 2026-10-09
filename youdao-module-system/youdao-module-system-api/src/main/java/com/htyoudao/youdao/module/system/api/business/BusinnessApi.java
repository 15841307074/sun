package com.htyoudao.youdao.module.system.api.business;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.business.dto.BusinessDTO;
import com.htyoudao.youdao.module.system.api.printer.dto.PrinterSettingDTO;
import com.htyoudao.youdao.module.system.api.printer.dto.PrinterTableVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author dht
 */
@Tag(name = "RPC 服务 - 项目")
public interface BusinnessApi {



    @Operation(summary = "项目列表", description = "远程调用")
    CommonResult<List<BusinessDTO>> listAll();



    @Operation(summary = "获取项目名", description = "远程调用")
    public CommonResult<String> selectByName(@RequestParam("id") Long id);



}
