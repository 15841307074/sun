package com.htyoudao.youdao.module.bpm.controller.admin.definition;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.model.BpmModelQueryVO;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionPageReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionRespVO;
import com.htyoudao.youdao.module.bpm.convert.definition.BpmProcessDefinitionConvert;
import com.htyoudao.youdao.module.bpm.dal.dataobject.definition.BpmCategoryDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.definition.BpmFormDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import com.htyoudao.youdao.module.bpm.service.definition.BpmCategoryService;
import com.htyoudao.youdao.module.bpm.service.definition.BpmFormService;
import com.htyoudao.youdao.module.bpm.service.definition.BpmProcessDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertList;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertSet;
import static com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 流程定义")
@RestController
@RequestMapping("/bpm/process-definition")
@Validated
@RefreshScope
public class BpmProcessDefinitionController {

    @Resource
    private BpmProcessDefinitionService processDefinitionService;
    @Resource
    private BpmFormService formService;
    @Resource
    private BpmCategoryService categoryService;

    @Value("${storeTaskProcessDefinitionIdList}")
    private String storeTaskProcessDefinitionIdStr;

    @GetMapping("/page")
    @Operation(summary = "获得流程定义分页")
    @PreAuthorize("@ss.hasPermission('bpm:process-definition:query')")
    public CommonResult<PageResult<BpmProcessDefinitionRespVO>> getProcessDefinitionPage(
            BpmProcessDefinitionPageReqVO pageReqVO) {
        PageResult<ProcessDefinition> pageResult = processDefinitionService.getProcessDefinitionPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 获得 Category Map
        Map<String, BpmCategoryDO> categoryMap = categoryService.getCategoryMap(
                convertSet(pageResult.getList(), ProcessDefinition::getCategory));
        // 获得 Deployment Map
        Map<String, Deployment> deploymentMap = processDefinitionService.getDeploymentMap(
                convertSet(pageResult.getList(), ProcessDefinition::getDeploymentId));
        // 获得 BpmProcessDefinitionInfoDO Map
        Map<String, BpmProcessDefinitionInfoDO> processDefinitionMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(pageResult.getList(), ProcessDefinition::getId));
        // 获得 Form Map
        Map<Long, BpmFormDO> formMap = formService.getFormMap(
               convertSet(processDefinitionMap.values(), BpmProcessDefinitionInfoDO::getFormId));
        return success(BpmProcessDefinitionConvert.INSTANCE.buildProcessDefinitionPage(
                pageResult, deploymentMap, processDefinitionMap, formMap, categoryMap));
    }

    @GetMapping ("/list")
    @Operation(summary = "获得流程定义列表")
    @Parameter(name = "suspensionState", description = "挂起状态", required = true, example = "1") // 参见 Flowable SuspensionState 枚举
    public CommonResult<List<BpmProcessDefinitionRespVO>> getProcessDefinitionList(
            @RequestParam("suspensionState") Integer suspensionState) {
        // 1.1 获得开启的流程定义
        List<ProcessDefinition> list = processDefinitionService.getProcessDefinitionListBySuspensionState(suspensionState);
        if (CollUtil.isEmpty(list)) {
            return success(Collections.emptyList());
        }
        // 1.2 移除不可见的流程定义
        Map<String, BpmProcessDefinitionInfoDO> processDefinitionMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(list, ProcessDefinition::getId));
        Long userId = getLoginUserId();
        list.removeIf(processDefinition -> {
            BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionMap.get(processDefinition.getId());
            return processDefinitionInfo == null // 不存在
                    || Boolean.FALSE.equals(processDefinitionInfo.getVisible()) // visible 不可见
                    || !processDefinitionService.canUserStartProcessDefinition(processDefinitionInfo, userId); // 无权限发起
        });

        List<BpmProcessDefinitionRespVO> bpmProcessDefinitionRespVOS = BpmProcessDefinitionConvert.INSTANCE.buildProcessDefinitionList(
                list, null, processDefinitionMap, null, null);

        if (StringUtils.isNotBlank(storeTaskProcessDefinitionIdStr)) {
            List<String> excludeIds = Arrays.asList(storeTaskProcessDefinitionIdStr.split(","));
            bpmProcessDefinitionRespVOS.removeIf(m -> excludeIds.contains(m.getModelId()));
        }

        // 2. 拼接 VO 返回
        return success(bpmProcessDefinitionRespVOS);
    }


    @GetMapping ("/list/withQuery")
    @Operation(summary = "获得流程定义列表 支持条件筛选")
    @Parameter(name = "suspensionState", description = "挂起状态", required = true, example = "1") // 参见 Flowable SuspensionState 枚举
    public CommonResult<List<BpmProcessDefinitionRespVO>> getProcessDefinitionListWithQuery(BpmModelQueryVO queryVO) {
        CommonResult<List<BpmProcessDefinitionRespVO>> result = this.getProcessDefinitionList(queryVO.getSuspensionState());
        List<BpmProcessDefinitionRespVO> data = result.getData();
        if (CollectionUtil.isEmpty(result.getData())){
            return result;
        }

        if (queryVO.getCategory() != null) {
            data.removeIf(m -> !Objects.equals(m.getCategory(), queryVO.getCategory()));
        }

        if (queryVO.getCategoryName() != null) {
            data.removeIf(m -> !m.getCategoryName().contains(queryVO.getCategoryName()));
        }

        if (queryVO.getName() != null) {
            data.removeIf(m -> !m.getName().contains(queryVO.getName()) && !m.getDescription().contains(queryVO.getName()));
        }

        if (queryVO.getDeptId() != null) {
            data.removeIf(m -> !Objects.equals(m.getDeptId(), queryVO.getDeptId()));
        }

        return CommonResult.success(data);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得流程定义精简列表", description = "只包含未挂起的流程，主要用于前端的下拉选项")
    public CommonResult<List<BpmProcessDefinitionRespVO>> getSimpleProcessDefinitionList() {
        // 只查询未挂起的流程
        List<ProcessDefinition> list = processDefinitionService.getProcessDefinitionListBySuspensionState(
                SuspensionState.ACTIVE.getStateCode());
        // 拼接 VO 返回，只返回 id、name、key
        return success(convertList(list, definition -> new BpmProcessDefinitionRespVO()
                .setId(definition.getId()).setName(definition.getName()).setKey(definition.getKey())));
    }

    @GetMapping ("/get")
    @Operation(summary = "获得流程定义")
    @Parameter(name = "id", description = "流程编号", required = true, example = "1024")
    @Parameter(name = "key", description = "流程定义标识", required = true, example = "1024")
    public CommonResult<BpmProcessDefinitionRespVO> getProcessDefinition(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "key", required = false) String key) {
        ProcessDefinition processDefinition = id != null ? processDefinitionService.getProcessDefinition(id)
                : processDefinitionService.getActiveProcessDefinition(key);
        if (processDefinition == null) {
            return success(null);
        }
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService.getProcessDefinitionInfo(processDefinition.getId());
        BpmnModel bpmnModel = processDefinitionService.getProcessDefinitionBpmnModel(processDefinition.getId());
        return success(BpmProcessDefinitionConvert.INSTANCE.buildProcessDefinition(
                processDefinition, null, processDefinitionInfo, null, null, bpmnModel));
    }

}
