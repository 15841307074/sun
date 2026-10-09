package com.htyoudao.youdao.module.system.controller.admin.tag;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.controller.admin.tag.vo.*;
import com.htyoudao.youdao.module.system.service.taggroup.TagGroupService;
import com.htyoudao.youdao.module.system.service.tagvalue.TagValueService;
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
@Tag(name = "管理后台 - 标签")
@RestController
@RequestMapping("/system/tag")
@Validated
public class TagController {

    @Resource
    private TagGroupService tagGroupService;

    @Resource
    private TagValueService tagValueService;

    @GetMapping("/page")
    @Operation(summary = "获得标签组分页")
    @PreAuthorize("@ss.hasPermission('system:label:query')")
    public CommonResult<PageResult<TagGroupRespVO>> getTagGroupPage(@Valid TagGroupPageReqVO pageReqVO) {
        return success(tagGroupService.getTagGroupPage(pageReqVO));
    }

    @GetMapping("/value/page")
    @Operation(summary = "获得标签分页")
    @PreAuthorize("@ss.hasPermission('system:label:query')")
    public CommonResult<PageResult<TagValueRespVO>> getTagValuePage(@Valid TagValuePageReqVO pageReqVO) {
        return success(tagValueService.getTagValuePageResp(pageReqVO));
    }

    @PostMapping("/create")
    @Operation(summary = "创建标签组")
    @PreAuthorize("@ss.hasPermission('system:label:create')")
    public CommonResult<Long> createTagGroup(@Valid @RequestBody TagGroupSaveReqVO createReqVO) {
        return success(tagGroupService.createTagGroup(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新标签组")
    @PreAuthorize("@ss.hasPermission('system:label:update')")
    public CommonResult<Boolean> updateTagGroup(@Valid @RequestBody TagGroupUpdateReqVO updateReqVO) {
        tagGroupService.updateTagGroup(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除标签组")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:label:delete')")
    public CommonResult<Boolean> deleteTagGroup(@RequestParam("id") Long id) {
        tagGroupService.deleteTagGroup(id);
        return success(true);
    }

    @GetMapping("/getById")
    @Operation(summary = "获得标签组")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:label:query')")
    public CommonResult<TagGroupRespVO> getTagGroup(@RequestParam("id") Long id) {
        return success(tagGroupService.getTagGroup(id));
    }
    @GetMapping("/getByName")
    @Operation(summary = "获得标签组---门店")
    @Parameter(name = "tagName", description = "编号", required = false, example = "1024")
    public CommonResult<List<TagGroupRespVO>> getTagGroupByName(@RequestParam("tagName") String tagName) {
        return success(tagGroupService.getTagGroupByName(tagName));
    }
    @GetMapping("/getTagList")
    @Operation(summary = "获得标签---门店")
    @Parameter(name = "tagName", description = "编号", required = false, example = "1024")
    public CommonResult<List<TagValueRespVO>> getTagList() {
        return success(tagGroupService.getTagList());
    }


    @GetMapping("/tagList")
    @Operation(summary = "获得标签组列表")
    public CommonResult<List<TagGroupRespVO>> tagList() {
        return success(tagGroupService.tagList());
    }
}
