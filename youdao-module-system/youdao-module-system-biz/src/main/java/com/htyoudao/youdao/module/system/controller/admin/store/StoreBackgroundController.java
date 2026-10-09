package com.htyoudao.youdao.module.system.controller.admin.store;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.background.StoreBackgroundRespVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.background.StoreBackgroundSaveReqVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.background.StoreBackgroundStatusReqVO;
import com.htyoudao.youdao.module.system.service.storebackground.StoreBackgroundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * 门店背景模板管理接口。
 */
@Tag(name = "管理后台 - 门店背景")
@RestController
@RequestMapping("/system/store/background")
@Validated
public class StoreBackgroundController {

    @Resource
    private StoreBackgroundService storeBackgroundService;

    /**
     * 查询全部门店背景模板。
     */
    @GetMapping("/list")
    @Operation(summary = "查询门店背景模板列表")
    @PreAuthorize("@ss.hasPermission('system:store:query')")
    public CommonResult<List<StoreBackgroundRespVO>> list() {
        return success(storeBackgroundService.getList());
    }

    /**
     * 根据模板编号查询门店背景模板详情。
     *
     * @param id 模板编号
     * @return 模板详情
     */
    @GetMapping("/get")
    @Operation(summary = "查询门店背景模板详情")
    @PreAuthorize("@ss.hasPermission('system:store:query')")
    public CommonResult<StoreBackgroundRespVO> get(@RequestParam @NotNull Long id) {
        return success(storeBackgroundService.get(id));
    }

    /**
     * 创建自定义门店背景模板，新增模板默认为关闭状态。
     *
     * @param reqVO 模板保存参数
     * @return 新增模板编号
     */
    @PostMapping("/create")
    @Operation(summary = "新增门店背景模板")
    @PreAuthorize("@ss.hasPermission('system:store:add')")
    public CommonResult<Long> create(@Valid @RequestBody StoreBackgroundSaveReqVO reqVO) {
        return success(storeBackgroundService.create(reqVO));
    }

    /**
     * 修改关闭状态的自定义模板，或修改系统默认模板图片。
     *
     * @param reqVO 模板保存参数
     * @return 是否修改成功
     */
    @PutMapping("/update")
    @Operation(summary = "修改门店背景模板")
    @PreAuthorize("@ss.hasPermission('system:store:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody StoreBackgroundSaveReqVO reqVO) {
        storeBackgroundService.update(reqVO);
        return success(true);
    }

    /**
     * 修改自定义门店背景模板发布状态，并更新发布时间。
     *
     * @param reqVO 模板状态参数
     * @return 是否修改成功
     */
    @PutMapping("/update-status")
    @Operation(summary = "修改门店背景模板发布状态")
    @PreAuthorize("@ss.hasPermission('system:store:update')")
    public CommonResult<Boolean> updateStatus(@Valid @RequestBody StoreBackgroundStatusReqVO reqVO) {
        storeBackgroundService.updateStatus(reqVO);
        return success(true);
    }

    /**
     * 删除关闭状态的自定义门店背景模板。
     *
     * @param id 模板编号
     * @return 是否删除成功
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除关闭状态的门店背景模板")
    @PreAuthorize("@ss.hasPermission('system:store:delete')")
    public CommonResult<Boolean> delete(@RequestParam @NotNull Long id) {
        storeBackgroundService.delete(id);
        return success(true);
    }
}
