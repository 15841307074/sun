package com.htyoudao.youdao.module.bpm.controller.app.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectPageReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.*;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.task.BpmTaskTransferReqVO;
import com.htyoudao.youdao.module.bpm.controller.app.task.VO.getSum.BpmProcessInstanceSumReqVO;
import com.htyoudao.youdao.module.bpm.controller.app.task.VO.getSumBydept.BpmAppGetSumNumByDeptRespVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessQueryDO;
import com.htyoudao.youdao.module.bpm.service.task.BpmProcessInstanceCopyService;
import com.htyoudao.youdao.module.bpm.service.task.BpmProcessInstanceService;
import com.htyoudao.youdao.module.bpm.service.task.BpmTaskService;
import com.htyoudao.youdao.module.system.api.dept.DeptApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.error;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static com.htyoudao.youdao.module.bpm.enums.ErrorCodeConstants.OA_PROJECT_ID_QUERY_ERROR;

@Tag(name = "app - 流程实例") // 流程实例，通过流程定义创建的一次“申请”
@RestController
@RequestMapping("/bpm/process-instance")
@Validated
public class AppBpmProcessInstanceController {

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Resource
    private BpmProcessInstanceCopyService processInstanceCopyService;

    @Resource
    private BpmTaskService taskService;

    @DubboReference
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "新建流程实例")
    public CommonResult<String> createProcessInstance(@Valid @RequestBody BpmProcessInstanceCreateReqVO createReqVO) {
        Integer taskType = createReqVO.getTaskType();
        Integer storeType = createReqVO.getStoreType();
        if (taskType != null && taskType == 1 ) {
            if ( storeType == null) {
                createReqVO.setStoreType(1);
            }
        }
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
    public CommonResult<PageResult<BpmBusinessQueryDO>> queryBusinessTask(@Valid @RequestBody BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        buildCreateTime(bpmProcessInstanceQueryReqVO);

        Integer choose = bpmProcessInstanceQueryReqVO.getIsAll();

        buildFullDeptList(bpmProcessInstanceQueryReqVO);
        if (choose == 1) {
            // 全部
            return success(processInstanceService.queryBusinessTask(bpmProcessInstanceQueryReqVO));
        } else if (choose == 2) {
            // 代办
            return getTaskTodoPage(bpmProcessInstanceQueryReqVO);
        } else if (choose == 3) {
            // 已办
            return getTaskDonePage(bpmProcessInstanceQueryReqVO);
        } else if (choose == 4) {
            // 我的
            return getProcessInstanceMyPage(bpmProcessInstanceQueryReqVO);
        } else if (choose == 5) {
            // 抄送
            return getProcessInstanceCopyPage(bpmProcessInstanceQueryReqVO);
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

    @PostMapping("/queryNotRelatedBusinessTask")
    @Operation(summary = "查询未关联任务列表")
    public CommonResult<PageResult<BpmBusinessQueryDO>> queryNotRelatedBusinessTask(@Valid @RequestBody BpmNotRelatedReqVO bpmNotRelatedReqVO) {
        //暂时不做时间限制
        //buildOaCreateTime(bpmNotRelatedReqVO);
        return success(processInstanceService.queryNotRelatedBusinessTask(bpmNotRelatedReqVO));
    }

    @PostMapping("/queryOABusinessTask")
    @Operation(summary = "查询OA项目任务列表")
    public CommonResult<PageResult<BpmBusinessQueryDO>> queryOABusinessTask(@Valid @RequestBody BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        //暂时不做时间限制
        //buildOaCreateTime(bpmNotRelatedReqVO);
        if (Objects.isNull(bpmProcessInstanceQueryReqVO.getOaProjectId())) {
            return error(OA_PROJECT_ID_QUERY_ERROR);
        }
        buildFullDeptList(bpmProcessInstanceQueryReqVO);
        return success(processInstanceService.queryOABusinessTask(bpmProcessInstanceQueryReqVO));
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

    @PostMapping("/queryGantt")
    @Operation(summary = "查询甘特图")
    public CommonResult<PageResult<BpmBusinessGanttRespVO>> queryGantt(@Valid @RequestBody BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        buildFullDeptList(bpmProcessInstanceQueryReqVO);
        PageResult<BpmBusinessGanttRespVO> listPageResult = processInstanceService.queryGantt(bpmProcessInstanceQueryReqVO);
        return success(listPageResult);
    }

    @Operation(summary = "获取 Todo 待办任务分页")
    public CommonResult<PageResult<BpmBusinessQueryDO>> getTaskTodoPage(@Valid BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        Long oaProjectId = bpmProcessInstanceQueryReqVO.getOaProjectId();
        Integer oaProjectFlag = bpmProcessInstanceQueryReqVO.getOaProjectFlag();
        List<String> pIds = processInstanceService.getProcessInstanceIdsByOaProjectId(oaProjectId, oaProjectFlag);
        buildFullDeptList(bpmProcessInstanceQueryReqVO);
        PageResult<BpmBusinessQueryDO> pageResult = taskService.getBpmTaskTodoPage(bpmProcessInstanceQueryReqVO, pIds);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }
        return success(pageResult);
    }

    @Operation(summary = "获取 Done 已办任务分页")
    public CommonResult<PageResult<BpmBusinessQueryDO>> getTaskDonePage(@Valid BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {

        List<String> pIds = processInstanceService.getProcessInstanceIdsByOaProjectId(bpmProcessInstanceQueryReqVO.getOaProjectId(), bpmProcessInstanceQueryReqVO.getOaProjectFlag());
        buildFullDeptList(bpmProcessInstanceQueryReqVO);
        PageResult<BpmBusinessQueryDO> pageResult = taskService.getBpmTaskDonePage(bpmProcessInstanceQueryReqVO, pIds);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }
        return success(pageResult);
    }

    @Operation(summary = "获得抄送流程分页列表")
    public CommonResult<PageResult<BpmBusinessQueryDO>> getProcessInstanceCopyPage(
            @Valid BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {

        List<String> pIds = processInstanceService.getProcessInstanceIdsByOaProjectId(bpmProcessInstanceQueryReqVO.getOaProjectId(), bpmProcessInstanceQueryReqVO.getOaProjectFlag());

        buildFullDeptList(bpmProcessInstanceQueryReqVO);
        PageResult<BpmBusinessQueryDO> pageResult = processInstanceCopyService.getBpmProcessInstanceCopyPage(
                bpmProcessInstanceQueryReqVO, pIds);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }

        return success(pageResult);
    }

    @Operation(summary = "获得我的实例分页列表", description = "在【我的流程】菜单中，进行调用")
    public CommonResult<PageResult<BpmBusinessQueryDO>> getProcessInstanceMyPage(
            @Valid BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        Long loginUserId = WebFrameworkUtils.getLoginUserId();
        if (Objects.equals(loginUserId, bpmProcessInstanceQueryReqVO.getApplicantId())) {
            return success(PageResult.empty());
        }

        buildFullDeptList(bpmProcessInstanceQueryReqVO);
        List<String> pIds = processInstanceService.getProcessInstanceIdsByOaProjectId(bpmProcessInstanceQueryReqVO.getOaProjectId(), bpmProcessInstanceQueryReqVO.getOaProjectFlag());


        PageResult<BpmBusinessQueryDO> pageResult = processInstanceService.getBpmProcessInstancePage(
                bpmProcessInstanceQueryReqVO, pIds);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        return success(pageResult);
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

        Object taskCountTodoPage = getTaskCountTodoPage(bpmProcessInstanceQueryReqVO, pIds);
        Object processInstanceCountCopyPage = getProcessInstanceCountCopyPage(bpmProcessInstanceQueryReqVO, pIds);

        Long todoCount = 0L;
        Long copyCount = 0L;

        map.put("todo", taskCountTodoPage);
//        map.put("done", getTaskCountDonePage(bpmProcessInstanceQueryReqVO));
//        map.put("myPage", getProcessInstanceCountMyPage(bpmProcessInstanceQueryReqVO));
        map.put("copyPage", processInstanceCountCopyPage);

        if (Objects.nonNull(taskCountTodoPage) && taskCountTodoPage instanceof Long){
            todoCount = (Long) taskCountTodoPage;
        }

        if (Objects.nonNull(processInstanceCountCopyPage) && processInstanceCountCopyPage instanceof Long){
            copyCount = (Long) processInstanceCountCopyPage;
        }

        map.put("sum", todoCount + copyCount);
        return success(map);

    }

    private Object getProcessInstanceCountCopyPage(BpmProcessInstanceQueryTaskCountReqVO bpmProcessInstanceQueryReqVO, List<String> pIds) {
        return processInstanceCopyService.getBpmProcessInstanceCountCopyPage(bpmProcessInstanceQueryReqVO, pIds);
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
    public CommonResult<PageResult<BpmBusinessDO>> queryFeedbackList(@Valid @RequestBody BpmProcessInstanceFeedBackReqVO feedBackReqVO) {
        PageResult<BpmBusinessDO> pageResult = processInstanceService.queryFeedbackList(feedBackReqVO);

        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        return success(pageResult);

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

    @GetMapping("/isSecondLastNode")
    @Operation(summary = "app-是否是倒数第二个节点")
    //@PreAuthorize("@ss.hasPermission('bpm:task:query')")
    public CommonResult<Boolean> isSecondLastNode(@Valid @RequestBody BpmTaskApproveReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        if (taskService.completionApprovalEnabled(userId,reqVO)) {
            return success(taskService.isSecondLastNode(userId,reqVO));
        }
        return success(Boolean.FALSE);

    }


    @PutMapping("/approve")
    @Operation(summary = "通过任务")
    //@PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> approveTask(@Valid @RequestBody BpmTaskApproveReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        taskService.approveTask(userId, reqVO);
        return success(true);
    }

    @GetMapping("/buttonDisplay")
    @Operation(summary = "pc审批时候按钮显示 true代表只有完成任务按钮")
    //@PreAuthorize("@ss.hasPermission('bpm:task:query')")
    public CommonResult<Boolean> buttonDisplay(@Valid @RequestBody BpmTaskApproveReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        return success(taskService.buttonDisplay(userId,reqVO));
    }

    @PutMapping("/reject")
    @Operation(summary = "不通过任务")
    //@PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> rejectTask(@Valid @RequestBody BpmTaskRejectReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        taskService.rejectTask(userId, reqVO);
        return success(true);
    }

    @DeleteMapping("/cancel-by-start-user")
    @Operation(summary = "用户取消流程实例", description = "取消发起的流程")
    //@PreAuthorize("@ss.hasPermission('bpm:process-instance:cancel')")
    public CommonResult<Boolean> cancelProcessInstanceByStartUser(
            @Valid @RequestBody BpmProcessInstanceCancelReqVO cancelReqVO) {
        processInstanceService.cancelProcessInstanceByStartUser(SecurityFrameworkUtils.getLoginUserId(), cancelReqVO);
        return success(true);
    }

    @PutMapping("/transfer")
    @Operation(summary = "转派任务", description = "用于【流程详情】的【转派】按钮")
    //@PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> transferTask(@Valid @RequestBody BpmTaskTransferReqVO reqVO) {
        taskService.transferTask(getLoginUserId(), reqVO);
        return success(true);
    }


    @PutMapping("/withdraw")
    @Operation(summary = "撤回任务")
    //@PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> withdrawTask(@RequestParam("taskId") String taskId) {
        taskService.withdrawTask(getLoginUserId(), taskId);
        return success(true);
    }



    @PostMapping("/getSumNum")
    @Operation(summary = "app获取任务数量")
    public CommonResult<BpmAppGetSumNumRespVO> getSumNum(@RequestBody BpmProcessInstanceQueryNumReqVO bpmProcessInstanceQueryReqVO) {
        if (Objects.nonNull(bpmProcessInstanceQueryReqVO.getApplicantDeptId())) {
            CommonResult<List<Long>> sonDeptList = deptApi.getSonDeptList(bpmProcessInstanceQueryReqVO.getApplicantDeptId());
            if (Objects.nonNull(sonDeptList) && CollectionUtil.isNotEmpty(sonDeptList.getCheckedData())) {
                bpmProcessInstanceQueryReqVO.setApplicantDeptIdList(sonDeptList.getCheckedData());
            }

        }
        BpmAppGetSumNumRespVO bpmAppGetSumNumRespVO = processInstanceService.getSumNum(bpmProcessInstanceQueryReqVO);
        return success(bpmAppGetSumNumRespVO);
    }


    @PostMapping("/getBusinessTaskStatistics")
    /*不封装*/
    @Operation(summary = "查询任务列表统计数值")
    public CommonResult<BpmAppGetSumNumRespVO> getBusinessTaskStatistics(@Valid @RequestBody BpmProcessInstanceSumReqVO bpmProcessInstanceQueryReqVO) {
        BpmAppGetSumNumRespVO result = processInstanceService.getBusinessTaskStatistics(bpmProcessInstanceQueryReqVO);
        return success(result);
    }

    @PostMapping("/getBusinessTaskStatisticsByDept")
    @Operation(summary = "查询任务列表统计数值")
    public CommonResult<List<BpmAppGetSumNumByDeptRespVO>> getBusinessTaskStatisticsByDept(@Valid @RequestBody BpmProcessInstanceSumReqVO bpmProcessInstanceQueryReqVO) {
        List<BpmAppGetSumNumByDeptRespVO> result = processInstanceService.getBusinessTaskStatisticsByDept(bpmProcessInstanceQueryReqVO);
        return success(result);
    }


}
