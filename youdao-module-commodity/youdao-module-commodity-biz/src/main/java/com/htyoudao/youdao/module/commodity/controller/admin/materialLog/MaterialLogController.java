package com.htyoudao.youdao.module.commodity.controller.admin.materialLog;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLog.VO.MaterialLogReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLog.VO.MaterialLogRespVo;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialLossRecordPageReq;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialLossRecordPageVO;
import com.htyoudao.youdao.module.commodity.service.materialLog.RawMaterialLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理后台 - 销售日志记录表")
@RestController
@RequestMapping("/commodity/log")
public class MaterialLogController {

    @Autowired
    private RawMaterialLogService rawMaterialLogService;



    @PostMapping("/logList")
    @Operation(summary = "日志记录列表")
    public CommonResult<PageResult<MaterialLogRespVo>> logList(@RequestBody MaterialLogReqVo pageReq){
        PageResult<MaterialLogRespVo> pageResult = rawMaterialLogService.logList(pageReq);
        return CommonResult.success(pageResult);
    }
}
