package com.htyoudao.youdao.module.bpm.controller.admin.oa;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectCreateReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectPageReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectPageRespVO;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectRelationshipReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectRespVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.project.OAProjectDO;
import com.htyoudao.youdao.module.bpm.service.project.OAProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "管理后台 - oa项目")
@RestController
@RequestMapping("/bpm/oa/project")
@Validated
public class OAProjectController {

    @Resource
    private OAProjectService oaProjectService;

    @PostMapping("/create")
    @Operation(summary = "创建OA项目")
    public CommonResult<Long> createOAProject(@Valid @RequestBody OAProjectCreateReqVO createReqVO) {
        return CommonResult.success(oaProjectService.createOAProject(createReqVO));
    }

    @PostMapping("/update")
    @Operation(summary = "更新OA项目")
    public CommonResult<Long> updateOAProject(@Valid @RequestBody OAProjectCreateReqVO createReqVO) {
        return CommonResult.success(oaProjectService.updateOAProject(createReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获取OA项目详情")
    @Parameter(name = "id", description = "oa项目ID", required = true, example = "1024")
    public CommonResult<OAProjectPageRespVO> detail(@RequestParam("id") Long id) {
        return success(oaProjectService.detailById(id));
    }


    @PostMapping("/add/relationship")
    @Operation(summary = "关联项目任务信息")
    public CommonResult<Boolean> addRelationship(@RequestBody @Valid OAProjectRelationshipReqVO reqVO) {
        oaProjectService.addRelationship(reqVO);
        return success(true);
    }


    @PostMapping("/remove/relationship")
    @Operation(summary = "移除项目任务信息")
    public CommonResult<Boolean> removeRelationship(@RequestBody @Valid OAProjectRelationshipReqVO reqVO) {
        oaProjectService.removeRelationship(reqVO);
        return success(true);
    }


    @DeleteMapping("/delete")
    @Operation(summary = "删除项目")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteProject(@RequestParam("id") Long id) {
        oaProjectService.deleteProject(id);
        return success(true);
    }


    @PostMapping("/page")
    @Operation(summary = "获得项目分页")
    public CommonResult<PageResult<OAProjectPageRespVO>> getProjectPage(@RequestBody OAProjectPageReqVO pageVO) {
        PageResult<OAProjectPageRespVO> pageResult = oaProjectService.getProjectPage(pageVO);
        return success(pageResult);
    }

}
