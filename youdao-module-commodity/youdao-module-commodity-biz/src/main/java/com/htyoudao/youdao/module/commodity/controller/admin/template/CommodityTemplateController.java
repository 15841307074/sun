package com.htyoudao.youdao.module.commodity.controller.admin.template;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.TEMP_NAME_EMPTY;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.TemplateCategoryListRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.template.TemplateCommodityDetailReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.template.TemplateCommodityDetailRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.template.TemplateCommodityUpdateReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.template.VO.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateCategory;
import com.htyoudao.youdao.module.commodity.service.template.ICommodityTemplateService;
import com.htyoudao.youdao.module.commodity.service.templateCategory.ICommodityTemplateCategoryService;
import com.htyoudao.youdao.module.commodity.service.templateSpu.ICommodityTemplateSpusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品模板Controller
 *
 * @author Qizhongnan
 * @date 2024-03-18
 */
@Tag(name = "管理后台 - 模板商品")
@RestController
@RequestMapping("/commodity/template")
public class CommodityTemplateController {

    @Resource
    private ICommodityTemplateCategoryService templateCategoryService;

    @Resource
    private ICommodityTemplateService commodityTemplateService;

    @Resource
    private ICommodityTemplateSpusService commodityTemplateSpusService;


    @GetMapping("/list")
    @Operation(summary = "查询商品模板列表")
    @PreAuthorize("@ss.hasPermission('commodity:temp:query')")
    public CommonResult<List<CommodityTemplateListRespVo>> templateList() {
        List<CommodityTemplateListRespVo> commodityTemplates = commodityTemplateService.selectCommodityTemplateListNew();
        return success(commodityTemplates);
    }



    @Operation(summary = "模板商品详情")
    @PostMapping("/template/getSpuInfo")
    @PreAuthorize("@ss.hasPermission('commodity:temp:query')")
    public CommonResult<TemplateCommodityDetailRespVO> getTemplateSpuInfo(@Valid @RequestBody TemplateCommodityDetailReqVO detailReqVO) {
        return success(commodityTemplateSpusService.getTemplateSpuInfo(detailReqVO));
    }

    @Operation(summary = "模板商品编辑")
    @PostMapping("/template/updateCommodity")
    @PreAuthorize("@ss.hasPermission('commodity:temp:update')")
    public CommonResult<Boolean> updateTemplateCommodity(@Valid @RequestBody TemplateCommodityUpdateReqVO updateReqVO) {
        commodityTemplateSpusService.updateTemplateCommodity(updateReqVO);
        return success(true);
    }

    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermission('commodity:temp:create')")
    @Operation(summary = "新增商品模板")
    public CommonResult<Boolean> add(@Valid @RequestBody CommodityTemplateSaveReqVo saveReqVo) {
        if (ObjectUtil.isEmpty(saveReqVo.getCommodityTemplateName())) {
            throw exception(TEMP_NAME_EMPTY);
        }
        commodityTemplateService.add(saveReqVo);
        return success(true);
    }


    @PostMapping("/base2template")
    @Operation(summary = "配置模板商品")
    @PreAuthorize("@ss.hasPermission('commodity:temp:base2template')")
    public CommonResult<Boolean> base2template(@RequestBody @Valid CommodityBaseToTemplateReqVO baseToTemplateVO) {
        List<Long> commodityIds = baseToTemplateVO.getCommodityIds();
        Long commodityTemplateId = baseToTemplateVO.getCommodityTemplateId();
        commodityTemplateService.base2template(commodityIds, commodityTemplateId);
        return success(true);
    }


    @PostMapping("/copy")
    @Operation(summary = "复制模板")
    public CommonResult<Boolean> copy(@RequestBody @Valid CommodityCopyTemplateReqVO templateVO) {
        commodityTemplateService.copy(templateVO.getCommodityTemplateId());
        return success(true);
    }


    @PostMapping("/update")
    @Operation(summary = "更新模板")
    @PreAuthorize("@ss.hasPermission('commodity:temp:updateTemp')")
    public CommonResult<Boolean> update(@RequestBody @Valid CommodityUpdateTemplateReqVO commodityTemplate) {
        commodityTemplateService.update(commodityTemplate);
        return success(true);
    }


    @Operation(summary = "模板分类拖动排序")
    @PostMapping("/sort/category")
    @PreAuthorize("@ss.hasPermission('commodity:temp:sortCategory')")
    public CommonResult<Boolean> sortTemplateCategory(@Valid @RequestBody List<CommodityTemplateSortReqVO> sortReqVOS) {
        templateCategoryService.sortCategory(sortReqVOS);
        return success(true);
    }


    @Operation(summary = "查询模板分类列表")
    @GetMapping("/listCategory")
    @PreAuthorize("@ss.hasPermission('commodity:temp:query')")
    public CommonResult<List<TemplateCategoryListRespVO>> listCategory(@RequestParam(value = "templateId") Long templateId) {
        List<CommodityTemplateCategory> categories = templateCategoryService.selectByTemplateId(templateId);
        return success(BeanUtils.toBean(categories, TemplateCategoryListRespVO.class));
    }


    @Operation(summary = "模板商品拖动排序")
    @PostMapping("/sort/spu")
    @PreAuthorize("@ss.hasPermission('commodity:temp:update')")
    public CommonResult<Boolean> sortTemplateSpu(@Valid @RequestBody List<CommodityTemplateSortSpuReqVO> sortReqVOS) {
        commodityTemplateSpusService.sortSpu(sortReqVOS);
        return success(true);
    }



}

