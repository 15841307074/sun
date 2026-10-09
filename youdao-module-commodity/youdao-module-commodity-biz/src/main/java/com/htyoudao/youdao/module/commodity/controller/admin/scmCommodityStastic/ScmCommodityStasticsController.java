package com.htyoudao.youdao.module.commodity.controller.admin.scmCommodityStastic;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodityStastic.VO.ScmCommodityStasticRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodityStastic.VO.ScmStasticsReqVO;
import com.htyoudao.youdao.module.commodity.enums.DeviceType;
import com.htyoudao.youdao.module.commodity.service.scmCommodityStastics.ScmCommodityStasticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.dubbo.remoting.http12.rest.Schema;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 二级目录查询")
@RestController
@RequestMapping("/commodity/scmCommodityStastics")
public class ScmCommodityStasticsController {

    @Resource
    private ScmCommodityStasticsService scmCommodityStasticsService;

    @PostMapping("/selectList")
    @Operation(summary = "查询二级目录列表")
    public CommonResult<List<ScmCommodityStasticRespVO>> selectList(@RequestBody ScmStasticsReqVO  scmStasticsReqVO) {
        List<ScmCommodityStasticRespVO> list = scmCommodityStasticsService.selectList(scmStasticsReqVO, DeviceType.PC.getValue());
        return success(list);
    }
}
