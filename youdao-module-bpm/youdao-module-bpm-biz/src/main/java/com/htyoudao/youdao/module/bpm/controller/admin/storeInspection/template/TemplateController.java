package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.checklist.vo.CheckListsSortReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo.*;
import com.htyoudao.youdao.module.bpm.service.storeInspection.template.StoreInspectionTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 点检模板")
@RestController
@RequestMapping("/bpm/inspectionTemplate")
@Validated
public class TemplateController {

    @Resource
    private StoreInspectionTemplateService templateService;

    @PostMapping("/createTemplate")
    @Operation(summary = "创建模板")
    public CommonResult<Integer> createTemplate(@Valid @RequestBody TemplateReqVO templateReqVO) {
        return success(templateService.create(templateReqVO));
    }

    @GetMapping("/getTemplateById")
    @Operation(summary = "查询模板详情")
    public CommonResult<TemplateRespVO> getTemplateById(@Valid @NotNull Long templateId) {
        return success(templateService.getTemplateById(templateId));
    }

    @PostMapping("/queryTemplate")
    @Operation(summary = "查询模板")
    public CommonResult<PageResult<TemplateRespVO>> queryTemplate(@Valid @RequestBody TemplateQueryReqVO templateQueryReqVO) {
        return success(templateService.query(templateQueryReqVO));
    }

    @PostMapping("/updateTemplate")
    @Operation(summary = "更新模板点检项")
    public CommonResult<Integer> updateTemplate(@Valid @RequestBody TemplateReqVO templateReqVO) {
        return success(templateService.updateTemplate(templateReqVO));
    }

/*    @PostMapping("/delChecklistIds")
    @Operation(summary = "删除模板点检项")
    public CommonResult<Boolean> delChecklistIds(@Valid @RequestBody TemplateChecklistDelReqVO templateChecklistDelReqVO) {
        return success(null);
    }*/

    @GetMapping("/delTemplate")
    @Operation(summary = "删除模板")
    public CommonResult<Integer> delTemplate(@Valid @NotNull Long templateId) {
        return success(templateService.delTemplate(templateId));
    }

/*    @PostMapping("/copyTemplate")
    @Operation(summary = "复制模板")
    public CommonResult<Boolean> copyTemplate(@Valid @NotNull Long templateId) {
        return success(null);
    }*/

/*    @PostMapping("/sortTemplateChecklists")
    @Operation(summary = "模板点检项排序")
    public CommonResult<Integer> sortTemplateChecklists(@Valid @RequestBody List<TemplateChecklistsSortReqVO> list) {
//        return success(templateService.sortChecklists(list));
        return success(null);
    }*/
}
