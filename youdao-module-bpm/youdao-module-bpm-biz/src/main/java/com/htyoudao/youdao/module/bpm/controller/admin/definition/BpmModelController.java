package com.htyoudao.youdao.module.bpm.controller.admin.definition;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertMap;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertSet;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;
import static com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

import cn.hutool.core.collection.CollUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.model.BpmModeUpdateBpmnReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.model.BpmModelQueryVO;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.model.BpmModelRespVO;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.model.BpmModelSaveReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.model.BpmModelUpdateStateReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelUpdateReqVO;
import com.htyoudao.youdao.module.bpm.convert.definition.BpmModelConvert;
import com.htyoudao.youdao.module.bpm.dal.dataobject.definition.BpmCategoryDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.definition.BpmFormDO;
import com.htyoudao.youdao.module.bpm.service.definition.BpmCategoryService;
import com.htyoudao.youdao.module.bpm.service.definition.BpmFormService;
import com.htyoudao.youdao.module.bpm.service.definition.BpmModelService;
import com.htyoudao.youdao.module.bpm.service.definition.BpmProcessDefinitionService;
import com.htyoudao.youdao.module.system.api.dept.DeptApi;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptRespDTO;
import com.htyoudao.youdao.module.system.api.user.AdminUserApi;
import com.htyoudao.youdao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理后台 - 流程模型")
@RestController
@RequestMapping("/bpm/model")
@Validated
public class BpmModelController {

    @Resource
    private BpmModelService modelService;
    @Resource
    private BpmFormService formService;
    @Resource
    private BpmCategoryService categoryService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;

    @DubboReference
    private AdminUserApi adminUserApi;
    @DubboReference
    private DeptApi deptApi;

    @GetMapping("/list")
    @Operation(summary = "获得模型分页")
    @Parameter(name = "name", description = "模型名称", example = "芋艿")
    public CommonResult<List<BpmModelRespVO>> getModelList(
        @RequestParam(value = "name", required = false) String name) {
        List<Model> list = modelService.getModelList(name);
        if (CollUtil.isEmpty(list)) {
            return success(Collections.emptyList());
        }

        // 获得 Form 表单
        Set<Long> formIds = convertSet(list, model -> {
            BpmModelMetaInfoVO metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model);
            return metaInfo != null ? metaInfo.getFormId() : null;
        });
        Map<Long, BpmFormDO> formMap = formService.getFormMap(formIds);
        // 获得 Category Map
        Map<String, BpmCategoryDO> categoryMap = categoryService.getCategoryMap(
            convertSet(list, Model::getCategory));
        // 获得 Deployment Map
        Map<String, Deployment> deploymentMap = processDefinitionService.getDeploymentMap(
            convertSet(list, Model::getDeploymentId));
        // 获得 ProcessDefinition Map
        List<ProcessDefinition> processDefinitions = processDefinitionService.getProcessDefinitionListByDeploymentIds(
            deploymentMap.keySet());
        Map<String, ProcessDefinition> processDefinitionMap = convertMap(processDefinitions,
            ProcessDefinition::getDeploymentId);
        // 获得 User Map、Dept Map
        Set<Long> userIds = convertSetByFlatMap(list, model -> {
            BpmModelMetaInfoVO metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model);
            return metaInfo != null ? metaInfo.getStartUserIds().stream() : Stream.empty();
        });
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Set<Long> deptIds = convertSetByFlatMap(list, model -> {
            BpmModelMetaInfoVO metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model);
            return metaInfo != null && metaInfo.getStartDeptIds() != null ? metaInfo.getStartDeptIds().stream()
                : Stream.empty();
        });

        Set<Long> collect = list.stream().map(BpmModelConvert.INSTANCE::parseMetaInfo)
            .map(BpmModelMetaInfoVO::getDeptId)
            .collect(Collectors.toSet());
        deptIds.addAll(collect);

        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(deptIds);
        return success(BpmModelConvert.INSTANCE.buildModelList(list,
            formMap, categoryMap, deploymentMap, processDefinitionMap, userMap, deptMap));
    }


    @GetMapping("/list/withQuery")
    @Operation(summary = "获得模型列表 支持条件筛选")
    public CommonResult<List<BpmModelRespVO>> getModelListV2(BpmModelQueryVO queryVO) {
        List<BpmModelRespVO> modelList = this.getModelList(null).getData();
        if (CollectionUtils.isEmpty(modelList) || queryVO == null) {
            return CommonResult.success(modelList);
        }

        if (StringUtils.isNotBlank(queryVO.getCategory())) {
            modelList.removeIf(m -> !Objects.equals(m.getCategory(), queryVO.getCategory()));
        }

        if (StringUtils.isNotBlank(queryVO.getCategoryName())) {
            modelList.removeIf(m -> !m.getCategoryName().contains(queryVO.getCategoryName()));
        }

        if (StringUtils.isNotBlank(queryVO.getName())) {
            modelList.removeIf(
                m -> !m.getName().contains(queryVO.getName()) && !m.getDescription().contains(queryVO.getName()));
        }

        if (queryVO.getDeptId() != null) {
            modelList.removeIf(m -> !Objects.equals(m.getDeptId(), queryVO.getDeptId()));
        }

        return CommonResult.success(modelList);
    }


    @GetMapping("/list/pageQuery")
    @Operation(summary = "获得模型列表 支持条件筛选 支持分页")
    public CommonResult<PageResult<BpmModelRespVO>> getModelListV3(BpmModelQueryVO pageReqVO) {
        CommonResult<List<BpmModelRespVO>> listResult = getModelListV2(pageReqVO);
        List<BpmModelRespVO> dataList = listResult.getData();

        // 手动分页逻辑
        int pageNo = pageReqVO.getPageNo() != null ? pageReqVO.getPageNo() : 1;
        int pageSize = pageReqVO.getPageSize() != null ? pageReqVO.getPageSize() : 10;

        // 计算分页参数
        int total = dataList.size();
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        // 确保索引不越界
        if (fromIndex >= total) {
            return CommonResult.success(new PageResult<BpmModelRespVO>(Collections.emptyList(), (long) total));
        }

        // 截取当前页的数据
        List<BpmModelRespVO> pageData = dataList.subList(fromIndex, toIndex);

        // 构建分页结果
        PageResult<BpmModelRespVO> pageResult = new PageResult<BpmModelRespVO>(pageData, (long) total);
        return CommonResult.success(pageResult);
    }


    @GetMapping("/get")
    @Operation(summary = "获得模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
//    @PreAuthorize("@ss.hasPermission('bpm:model:query')")
    public CommonResult<BpmModelRespVO> getModel(@RequestParam("id") String id) {
        Model model = modelService.getModel(id);
        if (model == null) {
            return null;
        }
        byte[] bpmnBytes = modelService.getModelBpmnXML(id);
        BpmSimpleModelNodeVO simpleModel = modelService.getSimpleModel(id);
        return success(BpmModelConvert.INSTANCE.buildModel(model, bpmnBytes, simpleModel));
    }

    @PostMapping("/create")
    @Operation(summary = "新建模型")
    public CommonResult<String> createModel(@Valid @RequestBody BpmModelSaveReqVO createRetVO) {
        setAssignStartUserHandler(createRetVO.getSimpleModel(), createRetVO.getAssignStartUserHandlerType());
        String model = modelService.createModel(createRetVO);
        return success(model);
    }


    /**
     * 递归设置node节点的 审批节点的审批人与发起人相同时，对应的处理类型
     * @param node
     * @param assignStartUserHandlerType
     */
    public void setAssignStartUserHandler(BpmSimpleModelNodeVO node, Integer assignStartUserHandlerType) {
        if (node == null) {
            return;
        }

        // 设置当前节点的值
        node.setAssignStartUserHandlerType(assignStartUserHandlerType);

        // 递归设置子节点
        BpmSimpleModelNodeVO childNode = node.getChildNode();
        setAssignStartUserHandler(childNode, assignStartUserHandlerType);
    }

    @PutMapping("/update")
    @Operation(summary = "修改模型")
    public CommonResult<Boolean> updateModel(@Valid @RequestBody BpmModelSaveReqVO modelVO) {
        setAssignStartUserHandler(modelVO.getSimpleModel(), modelVO.getAssignStartUserHandlerType());
        modelService.updateModel(getLoginUserId(), modelVO);
        return success(true);
    }

    @PutMapping("/update-sort-batch")
    @Operation(summary = "批量修改模型排序")
//    @Parameter(name = "ids", description = "编号数组", required = true, example = "1,2,3")
    public CommonResult<Boolean> updateModelSortBatch(@RequestParam("ids") List<String> ids) {
        modelService.updateModelSortBatch(getLoginUserId(), ids);
        return success(true);
    }

//    @PostMapping("/deploy")
//    @Operation(summary = "部署模型")
//    @Parameter(name = "id", description = "编号", required = true, example = "1024")

    /// /    @PreAuthorize("@ss.hasPermission('bpm:model:deploy')")
//    public CommonResult<Boolean> deployModel(@RequestParam("id") String id) {
//        modelService.deployModel(getLoginUserId(), id);
//        return success(true);
//    }
    @PutMapping("/update-state")
    @Operation(summary = "修改模型的状态", description = "实际更新的部署的流程定义的状态")
//    @PreAuthorize("@ss.hasPermission('bpm:model:update')")
    public CommonResult<Boolean> updateModelState(@Valid @RequestBody BpmModelUpdateStateReqVO reqVO) {
        modelService.updateModelState(getLoginUserId(), reqVO.getId(), reqVO.getState());
        return success(true);
    }

    @Deprecated
    @PutMapping("/update-bpmn")
    @Operation(summary = "修改模型的 BPMN")
//    @PreAuthorize("@ss.hasPermission('bpm:model:update')")
    public CommonResult<Boolean> updateModelBpmn(@Valid @RequestBody BpmModeUpdateBpmnReqVO reqVO) {
        modelService.updateModelBpmnXml(reqVO.getId(), reqVO.getBpmnXml());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
//    @PreAuthorize("@ss.hasPermission('bpm:model:delete')")
    public CommonResult<Boolean> deleteModel(@RequestParam("id") String id) {
        modelService.deleteModel(getLoginUserId(), id);
        return success(true);
    }

    @DeleteMapping("/clean")
    @Operation(summary = "清理模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
//    @PreAuthorize("@ss.hasPermission('bpm:model:clean')")
    public CommonResult<Boolean> cleanModel(@RequestParam("id") String id) {
        modelService.cleanModel(getLoginUserId(), id);
        return success(true);
    }

    // ========== 仿钉钉/飞书的精简模型 =========

    @GetMapping("/simple/get")
    @Operation(summary = "获得仿钉钉流程设计模型")
    @Parameter(name = "modelId", description = "流程模型编号", required = true, example = "a2c5eee0-eb6c-11ee-abf4-0c37967c420a")
    public CommonResult<BpmSimpleModelNodeVO> getSimpleModel(@RequestParam("id") String modelId) {
        return success(modelService.getSimpleModel(modelId));
    }

    @Deprecated
    @PostMapping("/simple/update")
    @Operation(summary = "保存仿钉钉流程设计模型")
//    @PreAuthorize("@ss.hasPermission('bpm:model:update')")
    public CommonResult<Boolean> updateSimpleModel(@Valid @RequestBody BpmSimpleModelUpdateReqVO reqVO) {
        modelService.updateSimpleModel(getLoginUserId(), reqVO);
        return success(Boolean.TRUE);
    }

}
