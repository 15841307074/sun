package com.htyoudao.youdao.module.commodity.controller.admin.afterorder;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.commodity.controller.admin.afterorder.vo.AfterOrderDeleteMoreReq;
import com.htyoudao.youdao.module.commodity.controller.admin.afterorder.vo.AfterOrderPageReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.afterorder.vo.AfterOrderRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.afterorder.vo.AfterOrderSaveReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.afterorder.AfterOrderDO;
import com.htyoudao.youdao.module.commodity.service.afterorder.AfterOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * @author dht
 */
@Tag(name = "管理后台 - 订单生成后加购商品")
@RestController
@RequestMapping("/commodity/after-order")
@Validated
public class AfterOrderController {

    @Resource
    private AfterOrderService afterOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建订单生成后加购商品")
    @PreAuthorize("@ss.hasPermission('commodity:after-order:create')")
    public CommonResult<Long> createAfterOrder(@Valid @RequestBody AfterOrderSaveReqVO createReqVO) {
        return success(afterOrderService.createAfterOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新订单生成后加购商品")
    @PreAuthorize("@ss.hasPermission('commodity:after-order:update')")
    public CommonResult<Boolean> updateAfterOrder(@Valid @RequestBody AfterOrderSaveReqVO updateReqVO) {
        afterOrderService.updateAfterOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除订单生成后加购商品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('commodity:after-order:delete')")
    public CommonResult<Boolean> deleteAfterOrder(@RequestParam("afterId") Long afterId) {
        afterOrderService.deleteAfterOrder(afterId);
        return success(true);
    }

    @PostMapping("/deleteMore")
    @Operation(summary = "多选删除订单生成后加购商品")
    @PreAuthorize("@ss.hasPermission('commodity:after-order:delete')")
    public CommonResult<Boolean> deleteMore(@RequestBody AfterOrderDeleteMoreReq afterOrderDeleteMoreReq) {
        afterOrderService.deleteMore(afterOrderDeleteMoreReq);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得订单生成后加购商品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    //@PreAuthorize("@ss.hasPermission('commodity:after-order:query')")
    public CommonResult<AfterOrderRespVO> getAfterOrder(@RequestParam("afterId") Long afterId) {
        AfterOrderDO afterOrder = afterOrderService.getAfterOrder(afterId);
        return success(BeanUtils.toBean(afterOrder, AfterOrderRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得订单生成后加购商品分页")
    //@PreAuthorize("@ss.hasPermission('commodity:after-order:query')")
    public CommonResult<PageResult<AfterOrderRespVO>> getAfterOrderPage(@Valid AfterOrderPageReqVO pageReqVO) {
        return success(afterOrderService.getAfterOrderPage(pageReqVO));
    }

}