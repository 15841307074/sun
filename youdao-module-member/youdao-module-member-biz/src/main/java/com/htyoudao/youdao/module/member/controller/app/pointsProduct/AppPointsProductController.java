package com.htyoudao.youdao.module.member.controller.app.pointsProduct;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO.PointsProductDTO;
import com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO.PointsProductDetailDTO;
import com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO.PointsProductVO;
import com.htyoudao.youdao.module.member.service.pointsProduct.IPointsProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "app - 积分商品")
@RestController
@RequestMapping("/member/points-product")
@Validated
public class AppPointsProductController {

    @Autowired
    private IPointsProductService pointsProductService;
    @Operation(summary = "小程序请求积分商品列表")
    @GetMapping("/listDTO")
    public CommonResult<List<PointsProductDTO>> listDTO(
            @RequestParam(value = "productType", required = false)
            @Min(value = 1, message = "商品分类只能为1或2")
            @Max(value = 2, message = "商品分类只能为1或2") Integer productType) {
        List<PointsProductDTO> listDTO = pointsProductService.selectPointsProductListDTO(productType);
        return success(listDTO);
    }



    @Operation(summary = "小程序查询积分商品详情")
    @PostMapping("/pointProductDetail")
    public CommonResult<PointsProductDetailDTO> getpointProductDetail(@RequestBody PointsProductVO pointsProductVO) {
        PointsProductDetailDTO pointsProductDetailDTO = pointsProductService.getpointProductDetail(pointsProductVO);
        return CommonResult.success(pointsProductDetailDTO);
    }
}
