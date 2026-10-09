package com.htyoudao.youdao.module.commodity.controller.app.scmCommodityStastics;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodityStastic.VO.ScmCommodityStasticRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodityStastic.VO.ScmStasticsReqVO;
import com.htyoudao.youdao.module.commodity.enums.DeviceType;
import com.htyoudao.youdao.module.commodity.service.scmCommodityStastics.ScmCommodityStasticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "点餐机 - 二级目录查询")
@RestController
@RequestMapping("/commodity/app/scmCommodityStastics")
public class AppScmCommodityStasticsController {

    @Resource
    private ScmCommodityStasticsService scmCommodityStasticsService;

    @PostMapping("/selectList")
    @Operation(summary = "查询二级目录列表")
    public CommonResult<List<ScmCommodityStasticRespVO>> selectList(@RequestBody ScmStasticsReqVO scmStasticsReqVO) {
        List<ScmCommodityStasticRespVO> list = scmCommodityStasticsService.selectList(scmStasticsReqVO, DeviceType.APP.getValue());
        return success(list);
    }
}
