package com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO.ScmCommodityDataReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO.ScmCommodityPageReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO.ScmCommodityRespVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.scmCommodiy.ScmCommodity;
import com.htyoudao.youdao.module.commodity.enums.DeviceType;
import com.htyoudao.youdao.module.commodity.service.scmCommodity.ScmCommodityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 供应链商品")
@RestController
@RequestMapping("/commodity/scmCommodity")
public class ScmCommodityController {

    @Resource
    private ScmCommodityService scmCommodityService;


    @Operation(summary = "供应链商品分页查询")
    @PostMapping("/getPage")
    public CommonResult<PageResult<ScmCommodityRespVO>> selectPage(@Valid @RequestBody ScmCommodityPageReqVO scmCommodityPageReqVO) {
        PageResult<ScmCommodityRespVO> pageResult = scmCommodityService.selectPage(scmCommodityPageReqVO, DeviceType.PC.getValue());

        return success(pageResult);
    }



    @Operation(summary = "获取所有商品列表")
    @PostMapping("/selectByList")
    public CommonResult<List<ScmCommodityRespVO>> selectByList(@RequestBody(required = false) ScmCommodityDataReqVO scmCommodityDataReqVO) {
        List<ScmCommodityRespVO> list = scmCommodityService.selectByList(scmCommodityDataReqVO);

        return success(list);
    }



}
