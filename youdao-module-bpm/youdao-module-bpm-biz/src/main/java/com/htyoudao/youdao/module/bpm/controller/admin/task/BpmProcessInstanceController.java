package com.htyoudao.youdao.module.bpm.controller.admin.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.common.util.number.NumberUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectPageRespVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.BpmFeedbackRespVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.*;
import com.htyoudao.youdao.module.bpm.controller.app.task.VO.getSum.BpmProcessInstanceSumReqVO;
import com.htyoudao.youdao.module.bpm.controller.app.task.VO.getSumBydept.BpmAppGetSumNumByDeptRespVO;
import com.htyoudao.youdao.module.bpm.convert.task.BpmProcessInstanceConvert;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessQueryDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.definition.BpmCategoryDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import com.htyoudao.youdao.module.bpm.service.definition.BpmCategoryService;
import com.htyoudao.youdao.module.bpm.service.definition.BpmProcessDefinitionService;
import com.htyoudao.youdao.module.bpm.service.project.OAProjectService;
import com.htyoudao.youdao.module.bpm.service.task.BpmProcessInstanceCopyService;
import com.htyoudao.youdao.module.bpm.service.task.BpmProcessInstanceService;
import com.htyoudao.youdao.module.bpm.service.task.BpmTaskService;
import com.htyoudao.youdao.module.system.api.dept.DeptApi;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptRespDTO;
import com.htyoudao.youdao.module.system.api.user.AdminUserApi;
import com.htyoudao.youdao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.error;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.*;
import static com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.htyoudao.youdao.module.bpm.enums.ErrorCodeConstants.OA_PROJECT_ID_QUERY_ERROR;

@Tag(name = "管理后台 - 流程实例") // 流程实例，通过流程定义创建的一次“申请”
@RestController
@RequestMapping("/bpm/process-instance")
@Validated
public class BpmProcessInstanceController {

    @Resource
    private BpmProcessInstanceService processInstanceService;
    @Resource
    private BpmTaskService taskService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;
    @Resource
    private BpmProcessInstanceCopyService processInstanceCopyService;
    @Resource
    private BpmCategoryService categoryService;

    @Resource
    private OAProjectService oaProjectService;
    @DubboReference
    private AdminUserApi adminUserApi;
    @DubboReference
    private DeptApi deptApi;

    @GetMapping("/my-page")
    @Operation(summary = "获得我的实例分页列表", description = "在【我的流程】菜单中，进行调用")
    @PreAuthorize("@ss.hasPermission('bpm:process-instance:query')")
    public CommonResult<PageResult<BpmProcessInstanceRespVO>> getProcessInstanceMyPage(
            @Valid BpmProcessInstancePageReqVO pageReqVO) {
        PageResult<HistoricProcessInstance> pageResult = processInstanceService.getProcessInstancePage(
                getLoginUserId(), pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接返回
        Map<String, List<Task>> taskMap = taskService.getTaskMapByProcessInstanceIds(
                convertList(pageResult.getList(), HistoricProcessInstance::getId));
        Map<String, ProcessDefinition> processDefinitionMap = processDefinitionService.getProcessDefinitionMap(
                convertSet(pageResult.getList(), HistoricProcessInstance::getProcessDefinitionId));
        Map<String, BpmCategoryDO> categoryMap = categoryService.getCategoryMap(
                convertSet(processDefinitionMap.values(), ProcessDefinition::getCategory));
        Map<String, BpmProcessDefinitionInfoDO> processDefinitionInfoMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(pageResult.getList(), HistoricProcessInstance::getProcessDefinitionId));
        Set<Long> userIds = convertSet(pageResult.getList(), processInstance -> NumberUtils.parseLong(processInstance.getStartUserId()));
        userIds.addAll(convertSetByFlatMap(taskMap.values(),
                tasks -> tasks.stream().map(Task::getAssignee).filter(StrUtil::isNotBlank).map(Long::parseLong)));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        return success(BpmProcessInstanceConvert.INSTANCE.buildProcessInstancePage(pageResult,
                processDefinitionMap, categoryMap, taskMap, userMap, deptMap, processDefinitionInfoMap));
    }

    @GetMapping("/manager-page")
    @Operation(summary = "获得管理流程实例的分页列表", description = "在【流程实例】菜单中，进行调用")
    @PreAuthorize("@ss.hasPermission('bpm:process-instance:manager-query')")
    public CommonResult<PageResult<BpmProcessInstanceRespVO>> getProcessInstanceManagerPage(
            @Valid BpmProcessInstancePageReqVO pageReqVO) {
        PageResult<HistoricProcessInstance> pageResult = processInstanceService.getProcessInstancePage(
                null, pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接返回
        Map<String, List<Task>> taskMap = taskService.getTaskMapByProcessInstanceIds(
                convertList(pageResult.getList(), HistoricProcessInstance::getId));
        Map<String, ProcessDefinition> processDefinitionMap = processDefinitionService.getProcessDefinitionMap(
                convertSet(pageResult.getList(), HistoricProcessInstance::getProcessDefinitionId));
        Map<String, BpmCategoryDO> categoryMap = categoryService.getCategoryMap(
                convertSet(processDefinitionMap.values(), ProcessDefinition::getCategory));
        // 发起人信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), processInstance -> NumberUtils.parseLong(processInstance.getStartUserId())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        Map<String, BpmProcessDefinitionInfoDO> processDefinitionInfoMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(pageResult.getList(), HistoricProcessInstance::getProcessDefinitionId));
        return success(BpmProcessInstanceConvert.INSTANCE.buildProcessInstancePage(pageResult,
                processDefinitionMap, categoryMap, taskMap, userMap, deptMap, processDefinitionInfoMap));
    }

    @PostMapping("/create")
    @Operation(summary = "新建流程实例")
//    @PreAuthorize("@ss.hasPermission('bpm:process-instance:query')")
    public CommonResult<String> createProcessInstance(@Valid @RequestBody BpmProcessInstanceCreateReqVO createReqVO) {
        return success(processInstanceService.createProcessInstance(createReqVO));
    }

//    @PostMapping("/testCreateProcessStoreChildInstance")
    @Operation(summary = "新建流程实例")
    public CommonResult<String> testCreateProcessStoreChildInstance(@Valid @RequestBody BpmProcessStoreChildReqVO createReqVO) {
        processInstanceService.createProcessStoreChildInstance(createReqVO);
        return success(null);
    }

    @PostMapping("/queryBusinessTask")
    @Operation(summary = "查询任务列表")
    public CommonResult<PageResult<BpmBusinessOAProjectQueryRespVO>> queryBusinessTask(@Valid @RequestBody BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        buildCreateTime(bpmProcessInstanceQueryReqVO);

        Integer choose = bpmProcessInstanceQueryReqVO.getIsAll();

        Map<Long, String> oaProjectMap =  oaProjectService.getAll();

        buildFullDeptList(bpmProcessInstanceQueryReqVO);
        if (choose == 1) {
            // 全部
            PageResult<BpmBusinessQueryDO> pageResult = processInstanceService.queryBusinessTask(bpmProcessInstanceQueryReqVO);
            PageResult<BpmBusinessOAProjectQueryRespVO> result = BeanUtils.toBean(pageResult, BpmBusinessOAProjectQueryRespVO.class);
            buildOaProjectName(result, oaProjectMap);
            return success(result);
        } else if (choose == 2) {
            // 代办
            PageResult<BpmBusinessQueryDO> taskTodoPage = getTaskTodoPage(bpmProcessInstanceQueryReqVO);
            PageResult<BpmBusinessOAProjectQueryRespVO> result = BeanUtils.toBean(taskTodoPage, BpmBusinessOAProjectQueryRespVO.class);
            buildOaProjectName(result, oaProjectMap);
            return success(result);
        } else if (choose == 3) {
            // 已办
            PageResult<BpmBusinessQueryDO> taskDonePage = getTaskDonePage(bpmProcessInstanceQueryReqVO);
            PageResult<BpmBusinessOAProjectQueryRespVO> result = BeanUtils.toBean(taskDonePage, BpmBusinessOAProjectQueryRespVO.class);
            buildOaProjectName(result, oaProjectMap);
            return success(result);
        } else if (choose == 4) {
            // 我的
            PageResult<BpmBusinessQueryDO> processInstanceMyPage = getProcessInstanceMyPage(bpmProcessInstanceQueryReqVO);
            PageResult<BpmBusinessOAProjectQueryRespVO> result = BeanUtils.toBean(processInstanceMyPage, BpmBusinessOAProjectQueryRespVO.class);
            buildOaProjectName(result, oaProjectMap);
            return success(result);
        } else if (choose == 5) {
            // 抄送
            PageResult<BpmBusinessQueryDO> processInstanceCopyPage = getProcessInstanceCopyPage(bpmProcessInstanceQueryReqVO);
            PageResult<BpmBusinessOAProjectQueryRespVO> result = BeanUtils.toBean(processInstanceCopyPage, BpmBusinessOAProjectQueryRespVO.class);
            buildOaProjectName(result, oaProjectMap);
            return success(result);
        } else {
            return success(PageResult.empty());
        }

    }

    private void buildFullDeptList(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        if (Objects.nonNull(bpmProcessInstanceQueryReqVO.getApplicantDeptId())) {
            CommonResult<List<Long>> sonDeptList = deptApi.getSonDeptList(bpmProcessInstanceQueryReqVO.getApplicantDeptId());
            if (Objects.nonNull(sonDeptList) && CollectionUtil.isNotEmpty(sonDeptList.getCheckedData())) {
                bpmProcessInstanceQueryReqVO.setApplicantDeptIdList(sonDeptList.getCheckedData());
            }

        }
    }

    private void buildOaProjectName(PageResult<BpmBusinessOAProjectQueryRespVO> pageResult, Map<Long, String> oaProjectMap) {

        if (Objects.nonNull(pageResult) && CollectionUtils.isNotEmpty(pageResult.getList())) {
            List<BpmBusinessOAProjectQueryRespVO> list = pageResult.getList();
            for (BpmBusinessOAProjectQueryRespVO ele : list) {
                ele.setOaProjectName(oaProjectMap.get(ele.getOaProjectId()));
            }
        }
    }

    @PostMapping("/queryNotRelatedBusinessTask")
    @Operation(summary = "查询未关联任务列表")
    public CommonResult<PageResult<BpmBusinessQueryDO>> queryNotRelatedBusinessTask(@Valid @RequestBody BpmNotRelatedReqVO bpmNotRelatedReqVO) {
        //暂时不做时间限制
        //buildOaCreateTime(bpmNotRelatedReqVO);
        return success(processInstanceService.queryNotRelatedBusinessTask(bpmNotRelatedReqVO));
    }

    @PostMapping("/queryOABusinessTask")
    @Operation(summary = "查询OA项目任务列表")
    public CommonResult<PageResult<BpmBusinessOAProjectQueryRespVO>> queryOABusinessTask(@Valid @RequestBody BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        //暂时不做时间限制
        //buildOaCreateTime(bpmNotRelatedReqVO);
        if (Objects.isNull(bpmProcessInstanceQueryReqVO.getOaProjectId())) {
            return error(OA_PROJECT_ID_QUERY_ERROR);
        }
        OAProjectPageRespVO oaProjectPageRespVO = oaProjectService.selectById(bpmProcessInstanceQueryReqVO.getOaProjectId());

        buildFullDeptList(bpmProcessInstanceQueryReqVO);

        PageResult<BpmBusinessQueryDO> pageResult = processInstanceService.queryOABusinessTask(bpmProcessInstanceQueryReqVO);
        PageResult<BpmBusinessOAProjectQueryRespVO> respVOPageResult = BeanUtils.toBean(pageResult, BpmBusinessOAProjectQueryRespVO.class);

        if (Objects.nonNull(respVOPageResult) && CollectionUtils.isNotEmpty(respVOPageResult.getList())) {
            List<BpmBusinessOAProjectQueryRespVO> list = respVOPageResult.getList();
            for (BpmBusinessOAProjectQueryRespVO ele : list) {
                ele.setOaProjectName(oaProjectPageRespVO.getProjectName());
            }
        }
        return success(respVOPageResult);
    }

    @PostMapping("/getBusinessTaskStatistics")
    @Operation(summary = "查询任务列表并统计（不带分页）")
    /*不封装*/
    public CommonResult<BpmAppGetSumNumRespVO> getBusinessTaskStatistics(@Valid @RequestBody BpmProcessInstanceSumReqVO bpmProcessInstanceQueryReqVO) {
        BpmAppGetSumNumRespVO result = processInstanceService.getBusinessTaskStatistics(bpmProcessInstanceQueryReqVO);
        return success(result);
    }

    private void buildCreateTime(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        LocalDateTime createTimeStart = bpmProcessInstanceQueryReqVO.getCreateTimeStart();
        LocalDateTime createTimeEnd = bpmProcessInstanceQueryReqVO.getCreateTimeEnd();

        if (createTimeStart == null && createTimeEnd == null) {
            // 取最近一年时间：开始时间 = 当前时间 - 1年，结束时间 = 当前时间
            LocalDateTime now = LocalDateTime.now();
            createTimeStart = now.minusYears(1); // 一年前的此刻
            createTimeEnd = now; // 当前时间

            // 设置回查询参数中
            bpmProcessInstanceQueryReqVO.setCreateTimeStart(createTimeStart);
            bpmProcessInstanceQueryReqVO.setCreateTimeEnd(createTimeEnd);
        }

    }



    @Operation(summary = "获取 Todo 待办任务分页")
    public PageResult<BpmBusinessQueryDO> getTaskTodoPage(@Valid BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {

        List<String> pIds = processInstanceService.getProcessInstanceIdsByOaProjectId(bpmProcessInstanceQueryReqVO.getOaProjectId(), bpmProcessInstanceQueryReqVO.getOaProjectFlag());
        buildFullDeptList(bpmProcessInstanceQueryReqVO);
        PageResult<BpmBusinessQueryDO> pageResult = taskService.getBpmTaskTodoPage(bpmProcessInstanceQueryReqVO, pIds);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty();
        }
        return pageResult;
    }

    @Operation(summary = "获取 Done 已办任务分页")
    public PageResult<BpmBusinessQueryDO> getTaskDonePage(@Valid BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {

        List<String> pIds = processInstanceService.getProcessInstanceIdsByOaProjectId(bpmProcessInstanceQueryReqVO.getOaProjectId(), bpmProcessInstanceQueryReqVO.getOaProjectFlag());
        buildFullDeptList(bpmProcessInstanceQueryReqVO);
        PageResult<BpmBusinessQueryDO> pageResult = taskService.getBpmTaskDonePage(bpmProcessInstanceQueryReqVO, pIds);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty();
        }
        return pageResult;
    }

    @Operation(summary = "获得抄送流程分页列表")
    public PageResult<BpmBusinessQueryDO> getProcessInstanceCopyPage(
            @Valid BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {

        List<String> pIds = processInstanceService.getProcessInstanceIdsByOaProjectId(bpmProcessInstanceQueryReqVO.getOaProjectId(), bpmProcessInstanceQueryReqVO.getOaProjectFlag());
        buildFullDeptList(bpmProcessInstanceQueryReqVO);
        PageResult<BpmBusinessQueryDO> pageResult = processInstanceCopyService.getBpmProcessInstanceCopyPage(
                bpmProcessInstanceQueryReqVO, pIds);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty();
        }

        return pageResult;
    }

    @Operation(summary = "获得我的实例分页列表", description = "在【我的流程】菜单中，进行调用")
    public PageResult<BpmBusinessQueryDO> getProcessInstanceMyPage(
            @Valid BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        Long loginUserId = WebFrameworkUtils.getLoginUserId();
        if (bpmProcessInstanceQueryReqVO.getApplicantId() !=null &&
                !Objects.equals(loginUserId, bpmProcessInstanceQueryReqVO.getApplicantId())) {
            return PageResult.empty();
        }

        List<String> pIds = processInstanceService.getProcessInstanceIdsByOaProjectId(bpmProcessInstanceQueryReqVO.getOaProjectId(), bpmProcessInstanceQueryReqVO.getOaProjectFlag());
        buildFullDeptList(bpmProcessInstanceQueryReqVO);
        PageResult<BpmBusinessQueryDO> pageResult = processInstanceService.getBpmProcessInstancePage(
                bpmProcessInstanceQueryReqVO, pIds);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }

        return pageResult;
    }

    @PostMapping("/queryGantt")
    @Operation(summary = "查询甘特图")
    public CommonResult<PageResult<BpmBusinessGanttRespVO>> queryGantt(@Valid @RequestBody BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        buildFullDeptList(bpmProcessInstanceQueryReqVO);
        PageResult<BpmBusinessGanttRespVO> listPageResult = processInstanceService.queryGantt(bpmProcessInstanceQueryReqVO);
        return success(listPageResult);
    }

    @GetMapping("/get")
    @Operation(summary = "获得指定流程实例", description = "在【流程详细】界面中，进行调用")
    @Parameter(name = "id", description = "流程实例的编号", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:process-instance:query')")
    public CommonResult<BpmProcessInstanceRespVO> getProcessInstance(@RequestParam("id") String id) {
        HistoricProcessInstance processInstance = processInstanceService.getHistoricProcessInstance(id);
        if (processInstance == null) {
            return success(null);
        }

        // 拼接返回
        ProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(
                processInstance.getProcessDefinitionId());
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService.getProcessDefinitionInfo(
                processInstance.getProcessDefinitionId());
        AdminUserRespDTO startUser = adminUserApi.getUser(NumberUtils.parseLong(processInstance.getStartUserId())).getCheckedData();
        DeptRespDTO dept = null;
        if (startUser != null && startUser.getDeptId() != null) {
            dept = deptApi.getDept(startUser.getDeptId()).getCheckedData();
        }
        return success(BpmProcessInstanceConvert.INSTANCE.buildProcessInstance(processInstance,
                processDefinition, processDefinitionInfo, startUser, dept));
    }

    @DeleteMapping("/cancel-by-start-user")
    @Operation(summary = "用户取消流程实例", description = "取消发起的流程")
    //@PreAuthorize("@ss.hasPermission('bpm:process-instance:cancel')")
    public CommonResult<Boolean> cancelProcessInstanceByStartUser(
            @Valid @RequestBody BpmProcessInstanceCancelReqVO cancelReqVO) {
        processInstanceService.cancelProcessInstanceByStartUser(getLoginUserId(), cancelReqVO);
        return success(true);
    }

    @DeleteMapping("/cancel-by-admin")
    @Operation(summary = "管理员取消流程实例", description = "管理员撤回流程")
    @PreAuthorize("@ss.hasPermission('bpm:process-instance:cancel-by-admin')")
    public CommonResult<Boolean> cancelProcessInstanceByManager(
            @Valid @RequestBody BpmProcessInstanceCancelReqVO cancelReqVO) {
        processInstanceService.cancelProcessInstanceByAdmin(getLoginUserId(), cancelReqVO);
        return success(true);
    }

    @GetMapping("/get-approval-detail")
    @Operation(summary = "获得审批详情")
    @Parameter(name = "id", description = "流程实例的编号", required = true)
//    @PreAuthorize("@ss.hasPermission('bpm:process-instance:query')")
    @SuppressWarnings("unchecked")
    public CommonResult<BpmApprovalDetailRespVO> getApprovalDetail(@Valid BpmApprovalDetailReqVO reqVO) {
        if (StrUtil.isNotEmpty(reqVO.getProcessVariablesStr())) {
            reqVO.setProcessVariables(JsonUtils.parseObject(reqVO.getProcessVariablesStr(), Map.class));
        }
        return success(processInstanceService.getApprovalDetail(getLoginUserId(), reqVO));
    }

    @GetMapping("/get-approval-person")
    @Operation(summary = "获得流程审批人列表")
    @Parameter(name = "id", description = "流程实例的编号", required = true)
//    @PreAuthorize("@ss.hasPermission('bpm:process-instance:query')")
    @SuppressWarnings("unchecked")
    public CommonResult<List<Long>> getApprovalPerson(@Valid BpmApprovalDetailReqVO reqVO) {
        if (StrUtil.isNotEmpty(reqVO.getProcessVariablesStr())) {
            reqVO.setProcessVariables(JsonUtils.parseObject(reqVO.getProcessVariablesStr(), Map.class));
        }
        return success(processInstanceService.getApprovalPerson(getLoginUserId(), reqVO));
    }


    @GetMapping("/get-next-approval-nodes")
    @Operation(summary = "获取下一个执行的流程节点")
    @PreAuthorize("@ss.hasPermission('bpm:process-instance:query')")
    @SuppressWarnings("unchecked")
    public CommonResult<List<BpmApprovalDetailRespVO.ActivityNode>> getNextApprovalNodes(@Valid BpmApprovalDetailReqVO reqVO) {
        if (StrUtil.isNotEmpty(reqVO.getProcessVariablesStr())) {
            reqVO.setProcessVariables(JsonUtils.parseObject(reqVO.getProcessVariablesStr(), Map.class));
        }
        return success(processInstanceService.getNextApprovalNodes(getLoginUserId(), reqVO));
    }



    @PostMapping("/get-approval-detail/post")
    @Operation(summary = "获得审批详情")
    @Parameter(name = "id", description = "流程实例的编号", required = true)
    public CommonResult<BpmApprovalDetailRespVO> getApprovalDetailPost(@RequestBody @Valid BpmApprovalDetailReqVO reqVO) {
        if (StrUtil.isNotEmpty(reqVO.getProcessVariablesStr())) {
            reqVO.setProcessVariables(JsonUtils.parseObject(reqVO.getProcessVariablesStr(), Map.class));
        }
        return success(processInstanceService.getApprovalDetail(getLoginUserId(), reqVO));
    }

    @PostMapping("/get-approval-person/post")
    @Operation(summary = "获得流程审批人列表")
    @Parameter(name = "id", description = "流程实例的编号", required = true)
    public CommonResult<List<Long>> getApprovalPersonPost(@RequestBody @Valid BpmApprovalDetailReqVO reqVO) {
        if (StrUtil.isNotEmpty(reqVO.getProcessVariablesStr())) {
            reqVO.setProcessVariables(JsonUtils.parseObject(reqVO.getProcessVariablesStr(), Map.class));
        }
        return success(processInstanceService.getApprovalPerson(getLoginUserId(), reqVO));
    }


    @PostMapping("/get-next-approval-nodes/post")
    @Operation(summary = "获取下一个执行的流程节点")
    public CommonResult<List<BpmApprovalDetailRespVO.ActivityNode>> getNextApprovalNodesPost(@RequestBody @Valid BpmApprovalDetailReqVO reqVO) {
        if (StrUtil.isNotEmpty(reqVO.getProcessVariablesStr())) {
            reqVO.setProcessVariables(JsonUtils.parseObject(reqVO.getProcessVariablesStr(), Map.class));
        }
        return success(processInstanceService.getNextApprovalNodes(getLoginUserId(), reqVO));
    }


    @GetMapping("/get-bpmn-model-view")
    @Operation(summary = "获取流程实例的 BPMN 模型视图", description = "在【流程详细】界面中，进行调用")
    @Parameter(name = "id", description = "流程实例的编号", required = true)
    public CommonResult<BpmProcessInstanceBpmnModelViewRespVO> getProcessInstanceBpmnModelView(@RequestParam(value = "id") String id) {
        return success(processInstanceService.getProcessInstanceBpmnModelView(id));
    }

    @PostMapping("/queryTaskCount")
    @Operation(summary = "查询任务列表待办与抄送数量")
    public CommonResult<Map<String, Object>> queryTaskCount(@Valid @RequestBody BpmProcessInstanceQueryTaskCountReqVO bpmProcessInstanceQueryReqVO) {
        Map<String, Object> map = new HashMap<>();
        List<String> pIds = processInstanceService.getProcessInstanceIdsByOaProjectId(bpmProcessInstanceQueryReqVO.getOaProjectId(), bpmProcessInstanceQueryReqVO.getOaProjectFlag());

        if (Objects.nonNull(bpmProcessInstanceQueryReqVO.getApplicantDeptId())) {
            CommonResult<List<Long>> sonDeptList = deptApi.getSonDeptList(bpmProcessInstanceQueryReqVO.getApplicantDeptId());
            if (Objects.nonNull(sonDeptList) && CollectionUtil.isNotEmpty(sonDeptList.getCheckedData())) {
                bpmProcessInstanceQueryReqVO.setApplicantDeptIdList(sonDeptList.getCheckedData());
            }

        }
        map.put("todo", getTaskCountTodoPage(bpmProcessInstanceQueryReqVO, pIds));
/*        map.put("done", getTaskCountDonePage(bpmProcessInstanceQueryReqVO));
        map.put("myPage", getProcessInstanceCountMyPage(bpmProcessInstanceQueryReqVO));*/
        map.put("copyPage", getProcessInstanceCountCopyPage(bpmProcessInstanceQueryReqVO, pIds));

        return success(map);

    }

    @GetMapping("/writeCopyPage")
    @Operation(summary = "记录抄送已读")
    public CommonResult<Boolean> writeCopyPage(@RequestParam("procInstId") String procInstId,
                                               @RequestParam("taskId") String taskId) {
        processInstanceService.writeCopyPage(procInstId, taskId);

        return success(true);

    }
    @PostMapping("/writeAllCopyPage")
    @Operation(summary = "全部抄送标记为已读")
    public CommonResult<Boolean> writeAllCopyPage(@Valid @RequestBody BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        Integer choose = bpmProcessInstanceQueryReqVO.getIsAll();
        if (choose == 5) {
            // 抄送
            List<String> pIds = processInstanceService.getProcessInstanceIdsByOaProjectId(bpmProcessInstanceQueryReqVO.getOaProjectId(), bpmProcessInstanceQueryReqVO.getOaProjectFlag());

            setProcessInstanceCopyPageFlag(bpmProcessInstanceQueryReqVO, pIds);
            return success(true);
        } else {
            return success(false);
        }

    }

    private void setProcessInstanceCopyPageFlag(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO, List<String> pIds) {
        Long loginUserId = WebFrameworkUtils.getLoginUserId();
        if (Objects.equals(loginUserId, bpmProcessInstanceQueryReqVO.getApplicantId())) {
            return ;
        }

        buildFullDeptList(bpmProcessInstanceQueryReqVO);

        PageResult<BpmBusinessQueryDO> bpmProcessInstanceCopyPage = processInstanceCopyService.getBpmProcessInstanceCopyPage(bpmProcessInstanceQueryReqVO, pIds);

        List<BpmBusinessQueryDO> list = bpmProcessInstanceCopyPage.getList();


        processInstanceCopyService.updateBpmProcessInstanceCopyPageFlag(
                list);

    }

    private Object getProcessInstanceCountCopyPage(BpmProcessInstanceQueryTaskCountReqVO bpmProcessInstanceQueryReqVO, List<String> pIds) {
        return processInstanceCopyService.getBpmProcessInstanceCountCopyPage(bpmProcessInstanceQueryReqVO, pIds);
    }

    private Object getProcessInstanceCountMyPage(BpmProcessInstanceQueryTaskCountReqVO bpmProcessInstanceQueryReqVO) {

        Long loginUserId = WebFrameworkUtils.getLoginUserId();
        if (Objects.equals(loginUserId, bpmProcessInstanceQueryReqVO.getApplicantId())) {
            return 0L;
        }

        return processInstanceService.getBpmProcessInstanceCountPage(bpmProcessInstanceQueryReqVO);
    }

    private Object getTaskCountDonePage(BpmProcessInstanceQueryTaskCountReqVO bpmProcessInstanceQueryReqVO) {
        return taskService.getBpmTaskCountDonePage(bpmProcessInstanceQueryReqVO);
    }

    private Object getTaskCountTodoPage(BpmProcessInstanceQueryTaskCountReqVO bpmProcessInstanceQueryReqVO, List<String> pIds) {
        return taskService.getBpmTaskCountTodoPage(bpmProcessInstanceQueryReqVO, pIds);
    }

    @PostMapping("/queryFeedbackList")
    @Operation(summary = "查询反馈表")
    public CommonResult<PageResult<BpmFeedbackRespVO>> queryFeedbackList(@Valid @RequestBody BpmProcessInstanceFeedBackReqVO feedBackReqVO) {
        PageResult<BpmBusinessDO> pageResult = processInstanceService.queryFeedbackList(feedBackReqVO);


        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        PageResult<BpmFeedbackRespVO> bean = BeanUtils.toBean(pageResult, BpmFeedbackRespVO.class);
        return success(bean);

    }

    @PostMapping("/queryFeedbackCount")
    @Operation(summary = "查询任务反馈统计")
    public CommonResult<Map<String, Object>> queryFeedbackCount(@Valid @RequestBody BpmProcessInstanceFeedBackReqVO feedBackReqVO) {
        Map<String, Object> map = new HashMap<>();
        map.put("done", 0);
        map.put("doing", 0);
        map.put("overdue", 0);
        processInstanceService.buildFeedbackMap(feedBackReqVO, map);
        return success(map);

    }

    @GetMapping("/queryFeedback")
    @Operation(summary = "查询反馈详情")
    public CommonResult<BpmBusinessDO> queryFeedback(@RequestParam("procInstId") String procInstId,
                                                     @RequestParam("taskId") String taskId
    ) {
        BpmBusinessDO result = processInstanceService.queryFeed(procInstId, taskId);

        return success(result);

    }

    @GetMapping("/queryBpmBusinessTaskDetail")
    @Operation(summary = "查询业务详情")
    public CommonResult<BpmBusinessOAProjectQueryRespVO> queryBpmBusinessTaskDetail(@RequestParam("procInstId") String procInstId,
                                                                  @RequestParam("taskId") String taskId) {
        BpmBusinessOAProjectQueryRespVO result = processInstanceService.queryBpmBusinessTaskDetail(procInstId, taskId);

        return success(result);

    }

    @PostMapping("/getSumNumPassProjectId")
    @Operation(summary = "app获取任务数量")
    public CommonResult<BpmAppGetSumNumRespVO> getSumNumPassProjectId(@RequestBody BpmProcessInstanceQueryNumReqVO bpmProcessInstanceQueryReqVO) {
        if (Objects.nonNull(bpmProcessInstanceQueryReqVO.getApplicantDeptId())) {
            CommonResult<List<Long>> sonDeptList = deptApi.getSonDeptList(bpmProcessInstanceQueryReqVO.getApplicantDeptId());
            if (Objects.nonNull(sonDeptList) && CollectionUtil.isNotEmpty(sonDeptList.getCheckedData())) {
                bpmProcessInstanceQueryReqVO.setApplicantDeptIdList(sonDeptList.getCheckedData());
            }

        }
        BpmAppGetSumNumRespVO bpmAppGetSumNumRespVO = processInstanceService.getSumNum(bpmProcessInstanceQueryReqVO);
        return success(bpmAppGetSumNumRespVO);
    }

    @PostMapping("/getBusinessTaskStatisticsByDept")
    @Operation(summary = "查询任务列表统计数值")
    public CommonResult<List<BpmAppGetSumNumByDeptRespVO>> getBusinessTaskStatisticsByDept(@Valid @RequestBody BpmProcessInstanceSumReqVO bpmProcessInstanceQueryReqVO) {
        List<BpmAppGetSumNumByDeptRespVO> result = processInstanceService.getBusinessTaskStatisticsByDept(bpmProcessInstanceQueryReqVO);
        return success(result);
    }
}
