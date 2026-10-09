package com.htyoudao.youdao.module.commodity.controller.app.scmCommodity;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO.ScmCommodityInfoReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO.ScmCommodityInfoRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO.ScmCommodityPageReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO.ScmCommodityRespVO;
import com.htyoudao.youdao.module.commodity.enums.DeviceType;
import com.htyoudao.youdao.module.commodity.service.scmCommodity.ScmCommodityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "点餐机 - 供应链商品")
@RestController
@RequestMapping("/commodity/app/scmCommodity")
public class AppScmCommodityController {
    @Resource
    private ScmCommodityService scmCommodityService;


    @Operation(summary = "供应链商品分页查询")
    @PostMapping("/getPage")
    public CommonResult<PageResult<ScmCommodityRespVO>> selectPage(@Valid @RequestBody ScmCommodityPageReqVO scmCommodityPageReqVO) {
        PageResult<ScmCommodityRespVO> pageResult = scmCommodityService.selectPage(scmCommodityPageReqVO, DeviceType.APP.getValue());

        return success(pageResult);
    }


    @Operation(summary = "供应链获取商品详情")
    @PostMapping("/selectCommodityInfo")
    public CommonResult<ScmCommodityInfoRespVO> selectCommodityInfo(@Valid @RequestBody ScmCommodityInfoReqVO scmCommodityInfoReqVO) {
        ScmCommodityInfoRespVO scmCommodityRespVO = scmCommodityService.selectCommodityInfo(scmCommodityInfoReqVO, DeviceType.APP.getValue());

        return success(scmCommodityRespVO);
    }
}
