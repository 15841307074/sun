package com.htyoudao.youdao.module.bpm.service.task;

import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertList;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertMap;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertSet;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.filterList;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.newArrayList;
import static com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO.ActivityNode;
import static com.htyoudao.youdao.module.bpm.enums.BpmConstants.BPM_OA_BUSINESS_STORE_ID;
import static com.htyoudao.youdao.module.bpm.enums.BpmConstants.BPM_OA_BUSINESS_STORE_NAME;
import static com.htyoudao.youdao.module.bpm.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.START_USER_NODE_ID;
import static com.htyoudao.youdao.module.bpm.framework.flowable.core.util.BpmnModelUtils.parseNodeType;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.flowable.bpmn.constants.BpmnXMLConstants.ELEMENT_CALL_ACTIVITY;
import static org.flowable.bpmn.constants.BpmnXMLConstants.ELEMENT_EVENT_END;
import static org.flowable.bpmn.constants.BpmnXMLConstants.ELEMENT_EVENT_START;
import static org.flowable.bpmn.constants.BpmnXMLConstants.ELEMENT_TASK_USER;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.CollectionUtils;
import com.htyoudao.youdao.framework.common.util.date.DateUtils;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.common.util.number.NumberUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.object.ObjectUtils;
import com.htyoudao.youdao.framework.common.util.object.PageUtils;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.security.core.LoginUser;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import com.htyoudao.youdao.module.bpm.controller.admin.base.user.UserSimpleBaseVO;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionRespVO;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectPageReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectPageRespVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.BpmAppGetSumNumRespVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.*;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO.ActivityNodeTask;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.task.BpmBusinessRespVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.task.BpmDeptRespVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.task.BpmTaskRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.task.VO.getSum.BpmProcessInstanceSumReqVO;
import com.htyoudao.youdao.module.bpm.controller.app.task.VO.getSumBydept.BpmAppGetSumNumByDeptRespVO;
import com.htyoudao.youdao.module.bpm.convert.definition.BpmProcessDefinitionConvert;
import com.htyoudao.youdao.module.bpm.convert.task.BpmProcessInstanceConvert;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmAllStoreInfoDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessCopyDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessQueryDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BussinessTaskStoreDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.task.BpmProcessInstanceCopyDO;
import com.htyoudao.youdao.module.bpm.dal.mysql.definition.BpmBusinessCopyMapper;
import com.htyoudao.youdao.module.bpm.dal.mysql.definition.BpmProcessBusinessMapper;
import com.htyoudao.youdao.module.bpm.dal.mysql.task.BpmProcessInstanceCopyMapper;
import com.htyoudao.youdao.module.bpm.dal.redis.BpmProcessIdRedisDAO;
import com.htyoudao.youdao.module.bpm.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.bpm.enums.definition.BpmModelTypeEnum;
import com.htyoudao.youdao.module.bpm.enums.definition.BpmSimpleModelNodeTypeEnum;
import com.htyoudao.youdao.module.bpm.enums.task.BpmButtonTypeEnum;
import com.htyoudao.youdao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.htyoudao.youdao.module.bpm.enums.task.BpmReasonEnum;
import com.htyoudao.youdao.module.bpm.enums.task.BpmTaskStatusEnum;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.event.BpmProcessInstanceEventPublisher;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.util.BpmHttpRequestUtils;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.util.FlowableUtils;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import com.htyoudao.youdao.module.bpm.service.definition.BpmProcessBusinessService;
import com.htyoudao.youdao.module.bpm.service.definition.BpmProcessDefinitionService;
import com.htyoudao.youdao.module.bpm.service.message.BpmMessageService;
import com.htyoudao.youdao.module.bpm.service.project.OAProjectService;
import com.htyoudao.youdao.module.system.api.dept.DeptApi;
import com.htyoudao.youdao.module.system.api.dept.DeptOrgApi;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptRespDTO;
import com.htyoudao.youdao.module.system.api.user.AdminUserApi;
import com.htyoudao.youdao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.MultiInstanceLoopCharacteristics;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceBuilder;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

/**
 * 流程实例 Service 实现类
 * <p>
 * ProcessDefinition & ProcessInstance & Execution & Task 的关系： 1. <a
 * href="https://blog.csdn.net/bobozai86/article/details/105210414" />
 * <p>
 * HistoricProcessInstance & ProcessInstance 的关系： 1. <a href=" https://my.oschina.net/843294669/blog/71902" />
 * <p>
 * 简单来说，前者 = 历史 + 运行中的流程实例，后者仅是运行中的流程实例
 *
 * @author 0090
 */
@Service
@Validated
@Slf4j
@RefreshScope
public class BpmProcessInstanceServiceImpl implements BpmProcessInstanceService {

    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;

    @Resource
    private BpmProcessDefinitionService processDefinitionService;

    @Resource
    private BpmProcessBusinessService processBusinessService;

    @Resource
    private BpmProcessDefinitionService bpmProcessDefinitionService;

    @Resource
    @Lazy // 避免循环依赖
    private BpmTaskService taskService;
    @Resource
    private BpmMessageService messageService;

    @DubboReference
    private AdminUserApi adminUserApi;
    @DubboReference
    private DeptApi deptApi;

    @Value("${storeTaskNoApprove}")
    private String storeTaskNoApprove;

    @Value("${storeTaskApprove}")
    private String storeTaskApprove;

    @Value("${storeTaskFeedBackActivityIDForNoApprove}")
    private String storeTaskFeedBackActivityIDForNoApprove;

    @Value("${storeTaskFeedBackActivityIDForApprove}")
    private String storeTaskFeedBackActivityIDForApprove;

    @Value("${storeTaskReApproveActivityIDForApprove}")
    private String storeTaskReApproveActivityIDForApprove;

    @DubboReference
    private DeptOrgApi deptOrgApi;

    @Resource
    private BpmProcessInstanceEventPublisher processInstanceEventPublisher;

    @Resource
    private BpmTaskCandidateInvoker taskCandidateInvoker;

    @Resource
    private BpmProcessIdRedisDAO processIdRedisDAO;

    @Resource
    private BpmProcessBusinessMapper bpmProcessBusinessMapper;

    @Resource
    private BpmBusinessCopyMapper bpmBusinessCopyMapper;

    @Resource
    private BpmProcessInstanceCopyMapper bpmProcessInstanceCopyMapper;

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private BpmProcessBusinessService bpmProcessBusinessService;

    @Resource
    private OAProjectService oaProjectService;

    // ========== Query 查询相关方法 ==========

    @Override
    public ProcessInstance getProcessInstance(String id) {
        return runtimeService.createProcessInstanceQuery()
            .includeProcessVariables()
            .processInstanceId(id)
            .singleResult();
    }

    @Override
    public List<ProcessInstance> getProcessInstances(Set<String> ids) {
        return runtimeService.createProcessInstanceQuery().processInstanceIds(ids).includeProcessVariables().list();
    }

    @Override
    public HistoricProcessInstance getHistoricProcessInstance(String id) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceId(id).includeProcessVariables()
            .singleResult();
    }

    @Override
    public List<HistoricProcessInstance> getHistoricProcessInstances(Set<String> ids) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceIds(ids).includeProcessVariables()
            .list();
    }

    private Map<String, String> getFormFieldsPermission(BpmnModel bpmnModel,
        String activityId, String taskId) {
        // 1. 获取流程活动编号。流程活动 Id 为空事，从流程任务中获取流程活动 Id
        if (StrUtil.isEmpty(activityId) && StrUtil.isNotEmpty(taskId)) {
            activityId = Optional.ofNullable(taskService.getHistoricTask(taskId))
                .map(HistoricTaskInstance::getTaskDefinitionKey).orElse(null);
        }
        if (StrUtil.isEmpty(activityId)) {
            return null;
        }

        // 2. 从 BpmnModel 中解析表单字段权限
        return BpmnModelUtils.parseFormFieldsPermission(bpmnModel, activityId);
    }

    @Override
    public BpmApprovalDetailRespVO getApprovalDetail(Long loginUserId, BpmApprovalDetailReqVO reqVO) {
        // 1.1 从 reqVO 中，读取公共变量
        Long startUserId = loginUserId; // 流程发起人
        log.info("bpmn-log-------流程发起人{}", startUserId);
        HistoricProcessInstance historicProcessInstance = null; // 流程实例
        Integer processInstanceStatus = BpmProcessInstanceStatusEnum.NOT_START.getStatus(); // 流程状态
        Map<String, Object> processVariables = new HashMap<>(); // 流程变量
        // 1.2 如果是流程已发起的场景，则使用流程实例的数据
        if (reqVO.getProcessInstanceId() != null) {
            historicProcessInstance = getHistoricProcessInstance(reqVO.getProcessInstanceId());
            log.info("bpmn-log-------historicProcessInstance{}", historicProcessInstance);
            if (historicProcessInstance == null) {
                throw exception(ErrorCodeConstants.PROCESS_INSTANCE_NOT_EXISTS);
            }
            startUserId = Long.valueOf(historicProcessInstance.getStartUserId());
            processInstanceStatus = FlowableUtils.getProcessInstanceStatus(historicProcessInstance);
            // 合并 DB 和前端传递的流量变量，以前端的为主
            if (CollUtil.isNotEmpty(historicProcessInstance.getProcessVariables())) {
                processVariables.putAll(historicProcessInstance.getProcessVariables());
            }
        }
        if (CollUtil.isNotEmpty(reqVO.getProcessVariables())) {
            processVariables.putAll(reqVO.getProcessVariables());
        }
        // 1.3 读取其它相关数据
        ProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(
            historicProcessInstance != null ? historicProcessInstance.getProcessDefinitionId()
                : reqVO.getProcessDefinitionId());
        log.info("bpmn-log-------读取其它相关数据{}", processDefinition);
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService
            .getProcessDefinitionInfo(processDefinition.getId());
        BpmnModel bpmnModel = processDefinitionService.getProcessDefinitionBpmnModel(processDefinition.getId());
        log.info("bpmn-log-------bpmnModel{}", bpmnModel);

        // 2.1 已结束 + 进行中的活动节点
        List<ActivityNode> endActivityNodes = null; // 已结束的审批信息
        List<ActivityNode> runActivityNodes = null; // 进行中的审批信息
        List<HistoricActivityInstance> activities = null; // 流程实例列表
        if (reqVO.getProcessInstanceId() != null) {
            activities = taskService.getActivityListByProcessInstanceId(reqVO.getProcessInstanceId());
            List<HistoricTaskInstance> tasks = taskService.getTaskListByProcessInstanceId(reqVO.getProcessInstanceId(),
                true);
            endActivityNodes = getEndActivityNodeList(startUserId, bpmnModel, processDefinitionInfo,
                historicProcessInstance, processInstanceStatus, activities, tasks);
            runActivityNodes = getRunApproveNodeList(startUserId, bpmnModel, processDefinition, processVariables,
                activities, tasks);
            log.info("bpmn-log-------已结束 + 进行中的活动节点{},{}", endActivityNodes, runActivityNodes);
        }

        // 2.2 流程已经结束，直接 return，无需预测
        if (BpmProcessInstanceStatusEnum.isProcessEndStatus(processInstanceStatus)) {
            BpmApprovalDetailRespVO bpmApprovalDetailRespVO = buildApprovalDetail(reqVO, bpmnModel, processDefinition,
                processDefinitionInfo,
                historicProcessInstance,
                processInstanceStatus, endActivityNodes, runActivityNodes, null, null);

            setCopyInfo(bpmApprovalDetailRespVO, reqVO.getProcessInstanceId());
            return bpmApprovalDetailRespVO;
        }

        // 3.1 计算当前登录用户的待办任务
        BpmTaskRespVO todoTask = taskService.getTodoTask(loginUserId, reqVO.getTaskId(), reqVO.getProcessInstanceId());
        log.info("bpmn-log-------计算当前登录用户的待办任务{}", todoTask);
        // 3.2 预测未运行节点的审批信息
        List<ActivityNode> simulateActivityNodes = getSimulateApproveNodeList(startUserId, bpmnModel,
            processDefinitionInfo,
            processVariables, activities);
        log.info("bpmn-log-------预测未运行节点的审批信息{}", simulateActivityNodes);
        // 4. 拼接最终数据
        BpmApprovalDetailRespVO bpmApprovalDetailRespVO = buildApprovalDetail(reqVO, bpmnModel, processDefinition,
            processDefinitionInfo, historicProcessInstance,
            processInstanceStatus, endActivityNodes, runActivityNodes, simulateActivityNodes, todoTask);
        log.info("bpmn-log-------拼接最终数据{}", bpmApprovalDetailRespVO);
        //4.1 设置抄送人信息
        setCopyInfo(bpmApprovalDetailRespVO, reqVO.getProcessInstanceId());
        log.info("bpmn-log-------设置抄送人信息{}", bpmApprovalDetailRespVO);
        LambdaQueryWrapper<BpmBusinessDO> query = new LambdaQueryWrapper<>();
        query.eq(BpmBusinessDO::getProcInstId, reqVO.getProcessInstanceId());
        query.last(" LIMIT 1");
        BpmBusinessDO bpmBusinessDO = bpmProcessBusinessMapper.selectOne(query);
        if (ObjectUtil.isNotEmpty(bpmBusinessDO)) {
            BpmBusinessRespVO bean = BeanUtils.toBean(bpmBusinessDO, BpmBusinessRespVO.class);
            Long deptId = bpmBusinessDO.getDeptId();
            if (ObjectUtil.isNotEmpty(deptId) && ObjectUtil.isNotEmpty(bean)) {
                CommonResult<DeptRespDTO> result = deptApi.getDept(deptId);
                DeptRespDTO data = result.getData();
                if (ObjectUtil.isNotEmpty(data)) {
                    BpmDeptRespVO deptRespVO = new BpmDeptRespVO();
                    deptRespVO.setId(data.getId());
                    deptRespVO.setName(data.getName());
                    deptRespVO.setParentId(data.getParentId());
                    bean.setDeptRespVO(deptRespVO);
                }
            }

            String parentProcInstId = bpmBusinessDO.getParentProcInstId();

            // 如果没有父流程实例ID，设置按钮类型为2
            if (ObjectUtil.isEmpty(parentProcInstId)) {
                bpmApprovalDetailRespVO.setButtonType(2);
            } else {
                BpmTaskRespVO todoTask1 = todoTask;
                if (ObjectUtil.isNotEmpty(todoTask1)) {
                    String taskId = todoTask.getId();
                    BpmProcessDefinitionInfoDO processDefinitionInfo2 = bpmProcessDefinitionService
                        .getProcessDefinitionInfo(processDefinitionInfo.getProcessDefinitionId());
                    Boolean completionApprovalEnabled = processDefinitionInfo2.getCompletionApprovalEnabled();
                    // 根据是否启用完成审批，检查不同的节点状态
                    boolean isTargetNode = checkTargetNode(taskId, completionApprovalEnabled);
                    if (isTargetNode) {
                        // 设置按钮类型：根据拒绝标志判断是审批还是重新审批
                        Integer rejectFlag = bpmBusinessDO.getRejectFlag();
                        BpmButtonTypeEnum buttonType = (rejectFlag == null || rejectFlag == 0)
                            ? BpmButtonTypeEnum.APPROVE
                            : BpmButtonTypeEnum.REAPPROVE;
                        bpmApprovalDetailRespVO.setButtonType(buttonType.getStatus());
                    }
                }
            }

//            String parentProcInstId = bpmBusinessDO.getParentProcInstId();
//            if(ObjectUtil.isEmpty(parentProcInstId)){
//                bpmApprovalDetailRespVO.setButtonType(2);
//            }else {
//                BpmTaskRespVO todoTask1 = bpmApprovalDetailRespVO.getTodoTask();
//                if(ObjectUtil.isNotEmpty(todoTask1)){
//                    String id = todoTask1.getId();
//                    BpmProcessDefinitionInfoDO processDefinitionInfo2 = bpmProcessDefinitionService.
//                            getProcessDefinitionInfo(processDefinitionInfo.getProcessDefinitionId());
//                    Boolean completionApprovalEnabled = processDefinitionInfo2.getCompletionApprovalEnabled();
//                    if(completionApprovalEnabled != null && completionApprovalEnabled){
//                        boolean secondLastNode = isSecondLastNode(taskService.getTask(id));
//                        if(secondLastNode){
//                            Integer rejectFlag = bpmBusinessDO.getRejectFlag();
//                            if(rejectFlag == null || rejectFlag == 0){
//                                bpmApprovalDetailRespVO.setButtonType(BpmButtonTypeEnum.APPROVE.getStatus());
//                            }else {
//                                bpmApprovalDetailRespVO.setButtonType(BpmButtonTypeEnum.REAPPROVE.getStatus());
//                            }
//                        }
//                    }
//
//                    if(ObjectUtil.isEmpty(completionApprovalEnabled) || !completionApprovalEnabled){
//                        boolean secondLastNode = isLastNode(taskService.getTask(id));
//                        if(secondLastNode){
//                            Integer rejectFlag = bpmBusinessDO.getRejectFlag();
//                            if(rejectFlag == null && rejectFlag == 0){
//                                bpmApprovalDetailRespVO.setButtonType(BpmButtonTypeEnum.APPROVE.getStatus());
//                            }else {
//                                bpmApprovalDetailRespVO.setButtonType(BpmButtonTypeEnum.REAPPROVE.getStatus());
//                            }
//                        }
//                    }
//                }
//            }
        }
        return bpmApprovalDetailRespVO;
    }


    /**
     * 检查目标节点状态
     *
     * @param taskId                    任务ID
     * @param completionApprovalEnabled 是否启用完成审批
     * @return 是否是目标节点
     */
    private boolean checkTargetNode(String taskId, Boolean completionApprovalEnabled) {
        if (Boolean.TRUE.equals(completionApprovalEnabled)) {
            // 启用完成审批时，检查是否是倒数第二个节点
            return isSecondLastNode(taskService.getTask(taskId));
        } else {
            // 未启用完成审批时，检查是否是最后一个节点
            return isLastNode(taskService.getTask(taskId));
        }
    }


    private void setCopyInfo(BpmApprovalDetailRespVO bpmApprovalDetailRespVO, String processInstanceId) {
        if (processInstanceId == null) {
            return;
        }

        List<BpmProcessInstanceCopyDO> bpmProcessInstanceCopyDOS = bpmProcessInstanceCopyMapper.selectList(
            BpmProcessInstanceCopyDO::getProcessInstanceId, processInstanceId);

        if (CollUtil.isEmpty(bpmProcessInstanceCopyDOS)) {
            return;
        }

        List<Long> copyUserIds = bpmProcessInstanceCopyDOS.stream()
            .map(BpmProcessInstanceCopyDO::getUserId).distinct()
            .toList();

        bpmApprovalDetailRespVO.setCopyUsers(adminUserApi.getUserList(copyUserIds).getData().stream()
            .map(u -> BeanUtils.toBean(u, UserSimpleBaseVO.class))
            .toList());
    }

    @Override
    public List<ActivityNode> getNextApprovalNodes(Long loginUserId, BpmApprovalDetailReqVO reqVO) {
        // 1.1 校验任务存在，且是当前用户的
        Task task = taskService.validateTask(loginUserId, reqVO.getTaskId());
        // 1.2 校验流程实例存在
        ProcessInstance instance = getProcessInstance(task.getProcessInstanceId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_NOT_EXISTS);
        }
        HistoricProcessInstance historicProcessInstance = getHistoricProcessInstance(task.getProcessInstanceId());
        if (historicProcessInstance == null) {
            throw exception(ErrorCodeConstants.PROCESS_INSTANCE_NOT_EXISTS);
        }
        // 1.3 校验BpmnModel
        BpmnModel bpmnModel = processDefinitionService.getProcessDefinitionBpmnModel(task.getProcessDefinitionId());
        if (bpmnModel == null) {
            return null;
        }

        // 2. 设置流程变量
        Map<String, Object> processVariables = new HashMap<>();
        // 2.1 获取历史中流程变量
        if (CollUtil.isNotEmpty(historicProcessInstance.getProcessVariables())) {
            processVariables.putAll(historicProcessInstance.getProcessVariables());
        }
        // 2.2 合并前端传递的流程变量，以前端为准
        if (CollUtil.isNotEmpty(reqVO.getProcessVariables())) {
            processVariables.putAll(reqVO.getProcessVariables());
        }

        // 3. 获取下一个将要执行的节点集合
        FlowElement flowElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        List<FlowNode> nextFlowNodes = BpmnModelUtils.getNextFlowNodes(flowElement, bpmnModel, processVariables);
        // 仅仅获取 UserTask 节点  TODO add from jason：如果网关节点和网关节点相连，获取下个 UserTask. 貌似有点不准。
        List<FlowNode> nextUserTaskList = CollectionUtils.filterList(nextFlowNodes, node -> node instanceof UserTask);
        List<ActivityNode> nextActivityNodes = convertList(nextUserTaskList,
            node -> new ActivityNode().setId(node.getId())
                .setName(node.getName()).setNodeType(BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType())
                .setStatus(BpmTaskStatusEnum.RUNNING.getStatus())
                .setCandidateStrategy(BpmnModelUtils.parseCandidateStrategy(node))
                .setCandidateUserIds(getTaskCandidateUserList(bpmnModel, node.getId(),
                    loginUserId, historicProcessInstance.getProcessDefinitionId(), processVariables)));
        if (CollUtil.isEmpty(nextActivityNodes)) {
            return nextActivityNodes;
        }

        // 4. 拼接基础信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
            convertSetByFlatMap(nextActivityNodes, ActivityNode::getCandidateUserIds, Collection::stream));
        Map<Long, DeptRespDTO> deptMap = new HashMap<>(8);
//        if  (CollectionUtil.isNotEmpty(convertSet(userMap.values(), AdminUserRespDTO::getDeptId))) {
//            Set<Long> longs = convertSet(userMap.values(), AdminUserRespDTO::getDeptId);
//            deptMap = deptApi.getDeptMap(longs);
//        }
        Map<Long, DeptRespDTO> finalDeptMap = deptMap;
        nextActivityNodes.forEach(node -> node.setCandidateUsers(convertList(node.getCandidateUserIds(), userId -> {
            AdminUserRespDTO user = userMap.get(userId);
            if (user != null) {
                return BpmProcessInstanceConvert.INSTANCE.buildUser(userId, userMap, finalDeptMap);
            }
            return null;
        })));
        return nextActivityNodes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PageResult<HistoricProcessInstance> getProcessInstancePage(Long userId,
        BpmProcessInstancePageReqVO pageReqVO) {
        // 1. 构建查询条件
        HistoricProcessInstanceQuery processInstanceQuery = historyService.createHistoricProcessInstanceQuery()
            .includeProcessVariables()
            .processInstanceTenantId(FlowableUtils.getTenantId())
            .orderByProcessInstanceStartTime().desc();
        if (userId != null) { // 【我的流程】菜单时，需要传递该字段
            processInstanceQuery.startedBy(String.valueOf(userId));
        } else if (pageReqVO.getStartUserId() != null) { // 【管理流程】菜单时，才会传递该字段
            processInstanceQuery.startedBy(String.valueOf(pageReqVO.getStartUserId()));
        }
        if (StrUtil.isNotEmpty(pageReqVO.getName())) {
            processInstanceQuery.processInstanceNameLike("%" + pageReqVO.getName() + "%");
        }
        if (StrUtil.isNotEmpty(pageReqVO.getProcessDefinitionKey())) {
            processInstanceQuery.processDefinitionKey(pageReqVO.getProcessDefinitionKey());
        }
        if (StrUtil.isNotEmpty(pageReqVO.getCategory())) {
            processInstanceQuery.processDefinitionCategory(pageReqVO.getCategory());
        }
        if (pageReqVO.getStatus() != null) {
            processInstanceQuery.variableValueEquals(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS,
                pageReqVO.getStatus());
        }
        if (ArrayUtil.isNotEmpty(pageReqVO.getCreateTime())) {
            processInstanceQuery.startedAfter(DateUtils.of(pageReqVO.getCreateTime()[0]));
            processInstanceQuery.startedBefore(DateUtils.of(pageReqVO.getCreateTime()[1]));
        }
        if (ArrayUtil.isNotEmpty(pageReqVO.getEndTime())) {
            processInstanceQuery.finishedAfter(DateUtils.of(pageReqVO.getEndTime()[0]));
            processInstanceQuery.finishedBefore(DateUtils.of(pageReqVO.getEndTime()[1]));
        }
        // 表单字段查询
        Map<String, Object> formFieldsParams = JsonUtils.parseObject(pageReqVO.getFormFieldsParams(), Map.class);
        if (CollUtil.isNotEmpty(formFieldsParams)) {
            formFieldsParams.forEach((key, value) -> {
                if (StrUtil.isEmpty(String.valueOf(value))) {
                    return;
                }
                // TODO @lesan：应支持多种类型的查询方式，目前只有字符串全等
                processInstanceQuery.variableValueEquals(key, value);
            });
        }

        // 2.1 查询数量
        long processInstanceCount = processInstanceQuery.count();
        if (processInstanceCount == 0) {
            return PageResult.empty(processInstanceCount);
        }
        // 2.2 查询列表
        List<HistoricProcessInstance> processInstanceList = processInstanceQuery.listPage(PageUtils.getStart(pageReqVO),
            pageReqVO.getPageSize());
        return new PageResult<>(processInstanceList, processInstanceCount);
    }

    @Override
    public PageResult<BpmBusinessQueryDO> getBpmProcessInstancePage(
        BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO, List<String> pIds) {
        Long loginUserId = WebFrameworkUtils.getLoginUserId();
        // 1. 构建查询条件
        HistoricProcessInstanceQuery processInstanceQuery = historyService.createHistoricProcessInstanceQuery()
            .includeProcessVariables()
            .processInstanceTenantId(FlowableUtils.getTenantId())
            .orderByProcessInstanceStartTime().desc();
        processInstanceQuery.startedBy(String.valueOf(loginUserId));

        if (CollectionUtil.isNotEmpty(pIds)) {
            processInstanceQuery.processInstanceIds(new HashSet<>(pIds));
        }

        List<HistoricProcessInstance> list = processInstanceQuery.list();
        if (CollectionUtil.isEmpty(list)) {
            return PageResult.empty();
        }
        // 取出所有的流程唯一标识
        List<String> procInstIds = list.stream()
            // 提取每个 Task 的 processInstanceId
            .map(HistoricProcessInstance::getId)
            // 过滤可能的 null 值（可选，根据业务场景）
            .filter(StrUtil::isNotEmpty)
            // 收集为 List<String>
            .toList();
        bpmProcessInstanceQueryReqVO.setIsAll(2);
        bpmProcessInstanceQueryReqVO.setProInstIdList(procInstIds);
        return queryBusinessTask(bpmProcessInstanceQueryReqVO);
    }

    @Override
    public Object queryBusinessTaskCount(BpmProcessInstanceQueryTaskCountReqVO bpmProcessInstanceQueryReqVO) {

        PageParam pageParam = new PageParam();
        pageParam.setPageNo(bpmProcessInstanceQueryReqVO.getPageNo());
        pageParam.setPageSize(bpmProcessInstanceQueryReqVO.getPageSize());

        // 待处理、已处理、我申请的
        return processBusinessService.getBusinessTaskListCountNotAll(bpmProcessInstanceQueryReqVO, pageParam);

    }

    @Override
    public Object queryBusinessTaskCopyCount(BpmProcessInstanceQueryTaskCountReqVO bpmProcessInstanceQueryReqVO) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(bpmProcessInstanceQueryReqVO.getPageNo());
        pageParam.setPageSize(bpmProcessInstanceQueryReqVO.getPageSize());

        // 抄送我的
        return processBusinessService.getBusinessTaskListCountCopy(bpmProcessInstanceQueryReqVO, pageParam);
    }


    private List<BpmBusinessDO> getBpmBusinessList(Long oaProjectId) {
        return    bpmProcessBusinessService.selectListByOaProjectId(oaProjectId);
    }

    @Override
    public List<String> getProcessInstanceIdsByOaProjectId(Long oaProjectId, Integer oaProjectFlag) {

        if (Objects.isNull(oaProjectFlag) || oaProjectFlag == 2){
            return null;
        }

        if (oaProjectFlag == 1 && Objects.isNull(oaProjectId)){
            return null;
        }

        if (!(oaProjectFlag == 0 || oaProjectFlag == 1)){
            return null;
        }

        LambdaQueryWrapper<BpmBusinessDO> wrapper = new LambdaQueryWrapper<>();

        wrapper.select(BpmBusinessDO::getProcInstId);
        if (oaProjectFlag == 1){
            wrapper.eq(BpmBusinessDO::getOaProjectId, oaProjectId);
        }else {
            wrapper.ne(BpmBusinessDO::getOaProjectId, oaProjectId);
        }

        wrapper.isNull(BpmBusinessDO::getParentProcInstId);
        wrapper.eq(BpmBusinessDO::getDeleted, Boolean.FALSE);

        List<String> proInstIdList = new ArrayList<>();

        ResultHandler<String> resultHandler = new ResultHandler<String>() {
            @Override
            public void handleResult (ResultContext<? extends String> resultContext) {
                // 获取当前行的结果（即 memberId）
                String proInstId = resultContext.getResultObject ();
                // 过滤 null 值并添加到集合
                if (Objects.nonNull (proInstId)) {
                    proInstIdList.add (proInstId);
                }
            }
        };
        bpmProcessBusinessMapper.selectObjs(wrapper, resultHandler);
        return proInstIdList;
    }

    @Override
    public Object getBpmProcessInstanceCountPage(BpmProcessInstanceQueryTaskCountReqVO bpmProcessInstanceQueryReqVO) {
        Long loginUserId = WebFrameworkUtils.getLoginUserId();
        // 1. 构建查询条件
        HistoricProcessInstanceQuery processInstanceQuery = historyService.createHistoricProcessInstanceQuery()
            .includeProcessVariables()
            .processInstanceTenantId(FlowableUtils.getTenantId())
            .orderByProcessInstanceStartTime().desc();
        processInstanceQuery.startedBy(String.valueOf(loginUserId));
        List<HistoricProcessInstance> list = processInstanceQuery.list();
        if (CollectionUtil.isEmpty(list)) {
            return 0;
        }
        // 取出所有的流程唯一标识
        List<String> procInstIds = list.stream()
            // 提取每个 Task 的 processInstanceId
            .map(HistoricProcessInstance::getId)
            // 过滤可能的 null 值（可选，根据业务场景）
            .filter(StrUtil::isNotEmpty)
            // 收集为 List<String>
            .toList();
        bpmProcessInstanceQueryReqVO.setProInstIdList(procInstIds);
        return queryBusinessTaskCount(bpmProcessInstanceQueryReqVO);
    }

    @Override
    public PageResult<BpmBusinessDO> queryFeedbackList(BpmProcessInstanceFeedBackReqVO feedBackReqVO) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        LambdaQueryWrapper<BpmBusinessDO> bpmBusinessDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        bpmBusinessDOLambdaQueryWrapper.eq(BpmBusinessDO::getProcInstId, feedBackReqVO.getProcInstId());
        // 主流程业务对象
        BpmBusinessDO bpmBusinessDO = bpmProcessBusinessMapper.selectOne(bpmBusinessDOLambdaQueryWrapper);

        boolean childIn = true;
        String childProcInstId = null;
        if (bpmBusinessDO != null && StringUtils.isNotBlank(bpmBusinessDO.getParentProcInstId())) {
            // procInstId为子流程ID
            LambdaQueryWrapper<BpmBusinessDO> mainWrapper = new LambdaQueryWrapper<>();
            mainWrapper.eq(BpmBusinessDO::getProcInstId, bpmBusinessDO.getParentProcInstId());
            // 重置主流程对象
            bpmBusinessDO = bpmProcessBusinessMapper.selectOne(mainWrapper);
            childProcInstId = feedBackReqVO.getProcInstId();
            feedBackReqVO.setProcInstId(bpmBusinessDO.getProcInstId());


        } else if (bpmBusinessDO != null && StringUtils.isBlank(bpmBusinessDO.getParentProcInstId())) {
            // procInstId为主流程ID
            childIn = false;
        } else {
            return PageResult.empty();
        }

//        List<BpmBusinessDO> bpmBusinessDOS = bpmProcessBusinessMapper.selectList(bpmBusinessDOLambdaQueryWrapper);

        if (bpmBusinessDO.getTaskType() == 0) {
            // 内部任务
            return getBpmBusinessDOPageResult(feedBackReqVO);
        } else {
            // 门店任务 1 主任务进来看子任务反馈 2 子任务进来看任务反馈
            if (childIn) {
                LambdaQueryWrapper<BpmBusinessDO> childWrapper = new LambdaQueryWrapper<>();
                childWrapper.eq(BpmBusinessDO::getProcInstId, childProcInstId);
                childWrapper.isNotNull(BpmBusinessDO::getFeedBackMsg);
                bpmBusinessDO = bpmProcessBusinessMapper.selectOne(childWrapper);

                if (bpmBusinessDO == null) {
                    return PageResult.empty();
                }

                PageResult<BpmBusinessDO> pageResult = new PageResult();
                List<BpmBusinessDO> list = new ArrayList<>();
                list.add(bpmBusinessDO);
                pageResult.setList(list);
                pageResult.setTotal(1L);
                return pageResult;
            }
            BpmApprovalDetailReqVO bpmApprovalDetailReqVO = new BpmApprovalDetailReqVO();
            bpmApprovalDetailReqVO.setProcessInstanceId(feedBackReqVO.getProcInstId());
            List<Long> approvalPerson = getApprovalPerson(loginUser.getId(), bpmApprovalDetailReqVO);

            // 数据可见范围 当前登录者能查看哪些流程
            CommonResult<List<Long>> commonResult = deptOrgApi.getUserIdsByDept();
            // 获取所有可见的userId
            List<Long> userIds = commonResult.getCheckedData();
            if (CollectionUtil.isEmpty(userIds)) {
                throw exception(USER_DEPT_ERROR);
            }
            //
            if (CollectionUtil.isNotEmpty(userIds) && userIds.size() == 1 ) {
                return getBpmBusinessDOSelfPageResult(feedBackReqVO, loginUser.getId());
            }

            approvalPerson.addAll(userIds);
            // 判断当前用户是否为主流程审核人 废弃
            if (approvalPerson.contains(loginUser.getId())) {
                // 查看所有子任务
                return getBpmBusinessDOChildPageResult(feedBackReqVO);
            } else {
                return PageResult.empty();

            }
        }
    }

    private PageResult<BpmBusinessDO> getBpmBusinessDOPageResult(BpmProcessInstanceFeedBackReqVO feedBackReqVO) {
        LambdaQueryWrapper<BpmBusinessDO> wrapper = new LambdaQueryWrapper<>();
        if (CollectionUtil.isNotEmpty(feedBackReqVO.getExecutorStoreId())) {
            wrapper.in(BpmBusinessDO::getStoreId, feedBackReqVO.getExecutorStoreId());
        }
        if (feedBackReqVO.getTaskState() != null) {
            // 已逾期
            if (feedBackReqVO.getTaskState() == 5) {
                wrapper.ne(BpmBusinessDO::getTaskState, 4).ne(BpmBusinessDO::getTaskState, 6);
                wrapper.lt(BpmBusinessDO::getCompletionTime, LocalDateTime.now());
            }
            wrapper.eq(BpmBusinessDO::getTaskState, feedBackReqVO.getTaskState());


        }
        wrapper.eq(BpmBusinessDO::getProcInstId, feedBackReqVO.getProcInstId());
        wrapper.isNotNull(BpmBusinessDO::getFeedBackMsg);

        PageParam pageParam = new PageParam();
        pageParam.setPageNo(feedBackReqVO.getPageNo());
        pageParam.setPageSize(feedBackReqVO.getPageSize());

        return bpmProcessBusinessMapper.selectPage(pageParam, wrapper);
    }

    private PageResult<BpmBusinessDO> getBpmBusinessDOChildPageResult(BpmProcessInstanceFeedBackReqVO feedBackReqVO) {
        LambdaQueryWrapper<BpmBusinessDO> wrapper = new LambdaQueryWrapper<>();
        if (CollectionUtil.isNotEmpty(feedBackReqVO.getExecutorStoreId())) {
            wrapper.in(BpmBusinessDO::getStoreId, feedBackReqVO.getExecutorStoreId());
        }
        if (feedBackReqVO.getTaskState() != null) {
            // 已逾期
            if (feedBackReqVO.getTaskState() == 5) {
                wrapper.ne(BpmBusinessDO::getTaskState, 4).ne(BpmBusinessDO::getTaskState, 6);
                wrapper.lt(BpmBusinessDO::getCompletionTime, LocalDateTime.now());
            }
            wrapper.eq(BpmBusinessDO::getTaskState, feedBackReqVO.getTaskState());


        }
        wrapper.eq(BpmBusinessDO::getParentProcInstId, feedBackReqVO.getProcInstId());
        wrapper.isNotNull(BpmBusinessDO::getFeedBackMsg);

        PageParam pageParam = new PageParam();
        pageParam.setPageNo(feedBackReqVO.getPageNo());
        pageParam.setPageSize(feedBackReqVO.getPageSize());

        return bpmProcessBusinessMapper.selectPage(pageParam, wrapper);
    }

    private PageResult<BpmBusinessDO> getBpmBusinessDOSelfPageResult(BpmProcessInstanceFeedBackReqVO feedBackReqVO,
        Long userId) {
        LambdaQueryWrapper<BpmBusinessDO> wrapper = new LambdaQueryWrapper<>();
        if (CollectionUtil.isNotEmpty(feedBackReqVO.getExecutorStoreId())) {
            wrapper.in(BpmBusinessDO::getStoreId, feedBackReqVO.getExecutorStoreId());
        }
        if (feedBackReqVO.getTaskState() != null) {
            // 已逾期
            if (feedBackReqVO.getTaskState() == 5) {
                wrapper.ne(BpmBusinessDO::getTaskState, 4).ne(BpmBusinessDO::getTaskState, 6);
                wrapper.lt(BpmBusinessDO::getCompletionTime, LocalDateTime.now());
            }
            wrapper.eq(BpmBusinessDO::getTaskState, feedBackReqVO.getTaskState());


        }
        wrapper.eq(BpmBusinessDO::getParentProcInstId, feedBackReqVO.getProcInstId());
        wrapper.eq(BpmBusinessDO::getExecutorUserId, userId);
        wrapper.isNotNull(BpmBusinessDO::getFeedBackMsg);

        PageParam pageParam = new PageParam();
        pageParam.setPageNo(feedBackReqVO.getPageNo());
        pageParam.setPageSize(feedBackReqVO.getPageSize());

        return bpmProcessBusinessMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public void buildFeedbackMap(BpmProcessInstanceFeedBackReqVO feedBackReqVO, Map<String, Object> map) {

        List<BpmBusinessDO> list = queryFeedbackListSum(feedBackReqVO);

        if (CollectionUtil.isNotEmpty(list)) {
            int completed = 0;       // 已完成（taskState = 6）
            int uncompleted = 0;     // 未完成（taskState != 6 且 completionTime > 当前时间）
            int overdue = 0;         // 已逾期（taskState != 6 且 completionTime < 当前时间）
            // 获取当前时间（用于判断是否逾期）
            LocalDateTime now = LocalDateTime.now();

            for (BpmBusinessDO business : list) {
                Integer taskState = business.getTaskState();
                LocalDateTime completionTime = business.getCompletionTime().withHour(23)    // 小时设为23
                        .withMinute(59)  // 分钟设为59
                        .withSecond(59)  // 秒设为59
                        .withNano(0);    // 纳秒设为0（可选，避免毫秒级干扰）

                // 1. 判断是否已完成（taskState = 6）
                if (6 == taskState) {
                    completed++;
                    continue; // 已完成，无需判断其他条件
                }

                // 2. 未完成（taskState != 6），进一步判断是否逾期
                // 注意：completionTime可能为null，需先判断非空
                if (completionTime == null) {
                    // 若期望完成时间为null，按业务需求处理（这里暂归为未完成）
                    uncompleted++;
                    continue;
                }

                // 比较期望完成时间与当前时间
                if (completionTime.isAfter(now)) {
                    // 期望时间在当前时间之后 → 未逾期（未完成）
                    uncompleted++;
                } else if (completionTime.isBefore(now)) {
                    // 期望时间在当前时间之前 → 已逾期
                    overdue++;
                } else {
                    // 极少数情况：期望时间与当前时间完全相同 → 按未逾期处理
                    uncompleted++;
                }
            }

            map.put("done", completed);
            map.put("doing", uncompleted);
            map.put("overdue", overdue);
        }
    }

    public List<BpmBusinessDO> queryFeedbackListSum(BpmProcessInstanceFeedBackReqVO feedBackReqVO) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        LambdaQueryWrapper<BpmBusinessDO> bpmBusinessDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        bpmBusinessDOLambdaQueryWrapper.eq(BpmBusinessDO::getParentProcInstId, feedBackReqVO.getProcInstId());
        List<BpmBusinessDO> bpmBusinessDOS = bpmProcessBusinessMapper.selectList(bpmBusinessDOLambdaQueryWrapper);

        if (CollectionUtil.isEmpty(bpmBusinessDOS)) {
            // 内部任务
            return getBpmBusinessDOList(feedBackReqVO);
        } else {
            // 子任务
            BpmApprovalDetailReqVO bpmApprovalDetailReqVO = new BpmApprovalDetailReqVO();
            bpmApprovalDetailReqVO.setProcessInstanceId(feedBackReqVO.getProcInstId());
            List<Long> approvalPerson = getApprovalPerson(loginUser.getId(), bpmApprovalDetailReqVO);
            // 判断当前用户是否为主流程审核人
            if (approvalPerson == null) {
                return null;
            }
            if (approvalPerson.contains(loginUser.getId())) {
                // 查看所有子任务
                return getBpmBusinessDOChildPageList(feedBackReqVO);
            } else {
                // 查看自己
                return getBpmBusinessDOSelfPageList(feedBackReqVO, loginUser.getId());
            }
        }
    }

    private List<BpmBusinessDO> getBpmBusinessDOSelfPageList(BpmProcessInstanceFeedBackReqVO feedBackReqVO,
        Long userId) {
        LambdaQueryWrapper<BpmBusinessDO> wrapper = new LambdaQueryWrapper<>();
        if (CollectionUtil.isNotEmpty(feedBackReqVO.getExecutorStoreId())) {
            wrapper.in(BpmBusinessDO::getStoreId, feedBackReqVO.getExecutorStoreId());
        }
        if (feedBackReqVO.getTaskState() != null) {
            // 已逾期
            if (feedBackReqVO.getTaskState() == 5) {
                wrapper.ne(BpmBusinessDO::getTaskState, 4).ne(BpmBusinessDO::getTaskState, 6);
                wrapper.lt(BpmBusinessDO::getCompletionTime, LocalDateTime.now());
            }
            wrapper.eq(BpmBusinessDO::getTaskState, feedBackReqVO.getTaskState());
        }
        wrapper.eq(BpmBusinessDO::getParentProcInstId, feedBackReqVO.getProcInstId());
        wrapper.eq(BpmBusinessDO::getExecutorUserId, userId);
        wrapper.isNotNull(BpmBusinessDO::getFeedBackMsg);

        return bpmProcessBusinessMapper.selectList(wrapper);
    }

    private List<BpmBusinessDO> getBpmBusinessDOChildPageList(BpmProcessInstanceFeedBackReqVO feedBackReqVO) {
        LambdaQueryWrapper<BpmBusinessDO> wrapper = new LambdaQueryWrapper<>();
        if (CollectionUtil.isNotEmpty(feedBackReqVO.getExecutorStoreId())) {
            wrapper.in(BpmBusinessDO::getStoreId, feedBackReqVO.getExecutorStoreId());
        }
        if (feedBackReqVO.getTaskState() != null) {
            // 已逾期
            if (feedBackReqVO.getTaskState() == 5) {
                wrapper.ne(BpmBusinessDO::getTaskState, 4).ne(BpmBusinessDO::getTaskState, 6);
                wrapper.lt(BpmBusinessDO::getCompletionTime, LocalDateTime.now());
            }
            wrapper.eq(BpmBusinessDO::getTaskState, feedBackReqVO.getTaskState());


        }
        wrapper.eq(BpmBusinessDO::getParentProcInstId, feedBackReqVO.getProcInstId());
        wrapper.isNotNull(BpmBusinessDO::getFeedBackMsg);

        return bpmProcessBusinessMapper.selectList(wrapper);
    }

    private List<BpmBusinessDO> getBpmBusinessDOList(BpmProcessInstanceFeedBackReqVO feedBackReqVO) {
        LambdaQueryWrapper<BpmBusinessDO> wrapper = new LambdaQueryWrapper<>();
        if (CollectionUtil.isNotEmpty(feedBackReqVO.getExecutorStoreId())) {
            wrapper.in(BpmBusinessDO::getStoreId, feedBackReqVO.getExecutorStoreId());
        }
        if (feedBackReqVO.getTaskState() != null) {
            // 已逾期
            if (feedBackReqVO.getTaskState() == 5) {
                wrapper.ne(BpmBusinessDO::getTaskState, 4).ne(BpmBusinessDO::getTaskState, 6);
                wrapper.lt(BpmBusinessDO::getCompletionTime, LocalDateTime.now());
            }
            wrapper.eq(BpmBusinessDO::getTaskState, feedBackReqVO.getTaskState());


        }
        wrapper.eq(BpmBusinessDO::getProcInstId, feedBackReqVO.getProcInstId());
        wrapper.isNotNull(BpmBusinessDO::getFeedBackMsg);
        return bpmProcessBusinessMapper.selectList(wrapper);
    }


    @Override
    public void writeCopyPage(String procInstId, String taskInstId) {

        LambdaQueryWrapper<BpmBusinessCopyDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BpmBusinessCopyDO::getProcInstId, procInstId);
        wrapper.eq(BpmBusinessCopyDO::getCopyUserId, SecurityFrameworkUtils.getLoginUserId());
        wrapper.eq(BpmBusinessCopyDO::getCopyFlag, 1);
        BpmBusinessCopyDO bpmBusinessDO = bpmBusinessCopyMapper.selectOne(wrapper);
        if (bpmBusinessDO == null) {
            BpmBusinessCopyDO bpmBusinessCopyDO = new BpmBusinessCopyDO();
            bpmBusinessCopyDO.setCopyFlag(1);
            bpmBusinessCopyDO.setCopyUserId(SecurityFrameworkUtils.getLoginUserId());
            bpmBusinessCopyDO.setProcInstId(procInstId);
            bpmBusinessCopyMapper.insert(bpmBusinessCopyDO);
        }


    }

    @Override
    public BpmBusinessDO queryFeed(String procInstId, String taskId) {
        LambdaQueryWrapper<BpmBusinessDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(procInstId) && StringUtils.isNotBlank(taskId)) {
//            wrapper.eq(BpmBusinessDO::getTaskId, taskId);
            wrapper.eq(BpmBusinessDO::getProcInstId, procInstId);
        } else {
            return null;
        }

        return bpmProcessBusinessMapper.selectOne(wrapper);
    }

    @Override
    public BpmBusinessOAProjectQueryRespVO queryBpmBusinessTaskDetail(String procInstId, String taskId) {
        LambdaQueryWrapper<BpmBusinessDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(procInstId) && StringUtils.isNotBlank(taskId)) {
//            wrapper.eq(BpmBusinessDO::getTaskId, taskId);
            wrapper.eq(BpmBusinessDO::getProcInstId, procInstId);
        } else {
            return null;
        }
        BpmBusinessDO businessDO = bpmProcessBusinessMapper.selectOne(wrapper);
        BpmBusinessOAProjectQueryRespVO respVO = BeanUtils.toBean(businessDO, BpmBusinessOAProjectQueryRespVO.class);
        Long oaProjectId = businessDO.getOaProjectId();

        if (Objects.nonNull(oaProjectId) ) {
            OAProjectPageRespVO oaProjectPageRespVO = oaProjectService.selectById(oaProjectId);
            if (Objects.nonNull(oaProjectPageRespVO)) {
                respVO.setOaProjectName(oaProjectPageRespVO.getProjectName());
            }
        }
        return respVO;
    }


    /**
     * 拼接审批详情的最终数据
     * <p>
     * 主要是，拼接审批人的用户信息、部门信息
     */
    private BpmApprovalDetailRespVO buildApprovalDetail(BpmApprovalDetailReqVO reqVO,
        BpmnModel bpmnModel,
        ProcessDefinition processDefinition,
        BpmProcessDefinitionInfoDO processDefinitionInfo,
        HistoricProcessInstance processInstance,
        Integer processInstanceStatus,
        List<ActivityNode> endApprovalNodeInfos,
        List<ActivityNode> runningApprovalNodeInfos,
        List<ActivityNode> simulateApprovalNodeInfos,
        BpmTaskRespVO todoTask) {
        // 1. 获取所有需要读取用户信息的 userIds
        List<ActivityNode> approveNodes = newArrayList(
            asList(endApprovalNodeInfos, runningApprovalNodeInfos, simulateApprovalNodeInfos));
        Set<Long> userIds = BpmProcessInstanceConvert.INSTANCE.parseUserIds(processInstance, approveNodes, todoTask);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, DeptRespDTO> deptMap = new HashMap<>(8);
//        if  (CollectionUtil.isNotEmpty(convertSet(userMap.values(), AdminUserRespDTO::getDeptId))) {
//            Set<Long> longs = convertSet(userMap.values(), AdminUserRespDTO::getDeptId);
//            deptMap = deptApi.getDeptMap(longs);
//        }
        // 2. 表单权限
        String taskId = reqVO.getTaskId() == null && todoTask != null ? todoTask.getId() : reqVO.getTaskId();
        Map<String, String> formFieldsPermission = getFormFieldsPermission(bpmnModel, reqVO.getActivityId(), taskId);

        // 3. 拼接数据
        return buildApprovalDetail(bpmnModel, processDefinition,
            processDefinitionInfo, processInstance,
            processInstanceStatus, approveNodes, todoTask, formFieldsPermission, userMap, deptMap);
    }

    private BpmApprovalDetailRespVO buildApprovalDetail(BpmnModel bpmnModel,
        ProcessDefinition processDefinition,
        BpmProcessDefinitionInfoDO processDefinitionInfo,
        HistoricProcessInstance processInstance,
        Integer processInstanceStatus,
        List<BpmApprovalDetailRespVO.ActivityNode> activityNodes,
        BpmTaskRespVO todoTask,
        Map<String, String> formFieldsPermission,
        Map<Long, AdminUserRespDTO> userMap,
        Map<Long, DeptRespDTO> deptMap) {
        // 1.1 流程实例
        BpmProcessInstanceRespVO processInstanceResp;
        if (processInstance != null) {
            AdminUserRespDTO startUser = userMap.get(NumberUtils.parseLong(processInstance.getStartUserId()));
            DeptRespDTO dept = startUser != null ? deptMap.get(startUser.getDeptId()) : null;
            processInstanceResp = BpmProcessInstanceConvert.INSTANCE.buildProcessInstance(processInstance, null, null,
                startUser, dept);
        } else {
            processInstanceResp = null;
        }

        // 1.2 流程定义
        BpmProcessDefinitionRespVO definitionResp = BpmProcessDefinitionConvert.INSTANCE.buildProcessDefinition(
            processDefinition, null, processDefinitionInfo, null, null, bpmnModel);

        // 1.3 流程节点
//        activityNodes.forEach(approveNode -> {
//            List<UserSimpleBaseVO> candidateUsers  = new ArrayList<>();
//            if (approveNode.getTasks() != null) {
//                approveNode.getTasks().forEach(task -> {
//                    UserSimpleBaseVO userSimpleBaseVO = BpmProcessInstanceConvert.INSTANCE.buildUser(task.getAssignee(), userMap, deptMap);
//                    if(ObjectUtil.isEmpty(approveNode.getEndTime()) && isSequentialMultiInstance(processInstanceResp.getProcessDefinitionId(),approveNode.getId())){
//                        approveNode.setIsSequentialMultiInstance(Boolean.TRUE);
//                        candidateUsers.add(userSimpleBaseVO);
//                    }
//                    //task.setAssigneeUser(userSimpleBaseVO);
//                    //task.setOwnerUser(BpmProcessInstanceConvert.INSTANCE.buildUser(task.getOwner(), userMap, deptMap));
//                });
//            }
//            List<UserSimpleBaseVO> userSimpleBase = convertList(approveNode.getCandidateUserIds(), userId -> BpmProcessInstanceConvert.INSTANCE.buildUser(userId, userMap, deptMap));
//
//            candidateUsers.addAll(userSimpleBase);
//            approveNode.setCandidateUsers(candidateUsers);
//        });

        activityNodes.forEach(approveNode -> {
            if (approveNode.getTasks() != null) {
                approveNode.getTasks().forEach(task -> {
                    if (ObjectUtil.isEmpty(approveNode.getEndTime()) && isSequentialMultiInstance(
                        processInstanceResp.getProcessDefinitionId(), approveNode.getId())) {
                        approveNode.setIsSequentialMultiInstance(Boolean.TRUE);
                    }
                    Integer approveMethod = getApproveMethod(approveNode.getId(), definitionResp.getSimpleModel());
                    log.info("approveType----------------------:{}", approveMethod);
                    approveNode.setApproveMethod(approveMethod);
                    task.setAssigneeUser(
                        BpmProcessInstanceConvert.INSTANCE.buildUser(task.getAssignee(), userMap, deptMap));
                    task.setOwnerUser(BpmProcessInstanceConvert.INSTANCE.buildUser(task.getOwner(), userMap, deptMap));
                });
            } else {
                Integer approveMethod = getApproveMethod(approveNode.getId(), definitionResp.getSimpleModel());
                log.info("approveType----------------------:{}", approveMethod);
                approveNode.setApproveMethod(approveMethod);
            }
            approveNode.setCandidateUsers(convertList(approveNode.getCandidateUserIds(),
                userId -> BpmProcessInstanceConvert.INSTANCE.buildUser(userId, userMap, deptMap)));
        });

        // 1.4 待办任务
        if (todoTask != null) {
            todoTask.setAssigneeUser(
                BpmProcessInstanceConvert.INSTANCE.buildUser(todoTask.getAssignee(), userMap, deptMap));
            todoTask.setOwnerUser(BpmProcessInstanceConvert.INSTANCE.buildUser(todoTask.getOwner(), userMap, deptMap));
            if (CollUtil.isNotEmpty(todoTask.getChildren())) {
                todoTask.getChildren().forEach(childTask -> {
                    childTask.setAssigneeUser(
                        BpmProcessInstanceConvert.INSTANCE.buildUser(childTask.getAssignee(), userMap, deptMap));
                    childTask.setOwnerUser(
                        BpmProcessInstanceConvert.INSTANCE.buildUser(childTask.getOwner(), userMap, deptMap));
                });
            }
        }

        // 2. 拼接起来
        return new BpmApprovalDetailRespVO().setStatus(processInstanceStatus)
            .setProcessDefinition(definitionResp)
            .setProcessInstance(processInstanceResp)
            .setFormFieldsPermission(formFieldsPermission)
            .setTodoTask(todoTask)
            .setActivityNodes(activityNodes);
    }

    private boolean isSequentialMultiInstance(String processDefinitionId, String activityId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        if (bpmnModel == null) {
            return false;
        }

        FlowElement flowElement = bpmnModel.getFlowElement(activityId);
        if (!(flowElement instanceof UserTask)) {
            return false;
        }

        UserTask userTask = (UserTask) flowElement;
        MultiInstanceLoopCharacteristics multiInstance = userTask.getLoopCharacteristics();

        // 如果是多实例且是顺序执行，就是依次审批
        return multiInstance != null && multiInstance.isSequential();
    }

    /**
     * 获得【已结束】的活动节点们
     */
    private List<ActivityNode> getEndActivityNodeList(Long startUserId, BpmnModel bpmnModel,
        BpmProcessDefinitionInfoDO processDefinitionInfo,
        HistoricProcessInstance historicProcessInstance, Integer processInstanceStatus,
        List<HistoricActivityInstance> activities, List<HistoricTaskInstance> tasks) {
        // 遍历 tasks 列表，只处理已结束的 UserTask
        // 为什么不通过 activities 呢？因为，加签场景下，它只存在于 tasks，没有 activities，导致如果遍历 activities 的话，它无法成为一个节点
        List<HistoricTaskInstance> endTasks = filterList(tasks, task -> task.getEndTime() != null);
        List<ActivityNode> approvalNodes = convertList(endTasks, task -> {
            FlowElement flowNode = BpmnModelUtils.getFlowElementById(bpmnModel, task.getTaskDefinitionKey());
            ActivityNode activityNode = new ActivityNode().setId(task.getTaskDefinitionKey()).setName(task.getName())
                .setNodeType(START_USER_NODE_ID.equals(task.getTaskDefinitionKey())
                    ? BpmSimpleModelNodeTypeEnum.START_USER_NODE.getType()
                    : ObjUtil.defaultIfNull(parseNodeType(flowNode), // 目的：解决“办理节点”的识别
                        BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType()))
                .setStatus(getEndActivityNodeStatus(task))
                .setCandidateStrategy(BpmnModelUtils.parseCandidateStrategy(flowNode))
                .setStartTime(DateUtils.of(task.getCreateTime())).setEndTime(DateUtils.of(task.getEndTime()))
                .setTasks(singletonList(BpmProcessInstanceConvert.INSTANCE.buildApprovalTaskInfo(task)));
            // 如果是取消状态，则跳过
            if (BpmTaskStatusEnum.isCancelStatus(activityNode.getStatus())) {
                return null;
            }
            return activityNode;
        });

        // 遍历 activities，只处理已结束的 StartEvent、EndEvent
        List<HistoricActivityInstance> endActivities = filterList(activities, activity -> activity.getEndTime() != null
            && (StrUtil.equalsAny(activity.getActivityType(), ELEMENT_EVENT_START, ELEMENT_CALL_ACTIVITY,
            ELEMENT_EVENT_END)));
        endActivities.forEach(activity -> {
            // StartEvent：只处理 BPMN 的场景。因为，SIMPLE 情况下，已经有 START_USER_NODE 节点
            if (ELEMENT_EVENT_START.equals(activity.getActivityType())
                && BpmModelTypeEnum.BPMN.getType().equals(processDefinitionInfo.getModelType())
                && !CollUtil.contains(activities, // 特殊：如果已经存在用户手动创建的 START_USER_NODE_ID 节点，则忽略 StartEvent
                historicActivity -> historicActivity.getActivityId().equals(START_USER_NODE_ID))) {
                ActivityNodeTask startTask = new ActivityNodeTask().setId(BpmnModelConstants.START_USER_NODE_ID)
                    .setAssignee(startUserId).setStatus(BpmTaskStatusEnum.APPROVE.getStatus());
                ActivityNode startNode = new ActivityNode().setId(startTask.getId())
                    .setName(BpmSimpleModelNodeTypeEnum.START_USER_NODE.getName())
                    .setNodeType(BpmSimpleModelNodeTypeEnum.START_USER_NODE.getType())
                    .setStatus(startTask.getStatus()).setTasks(ListUtil.of(startTask))
                    .setStartTime(DateUtils.of(activity.getStartTime()))
                    .setEndTime(DateUtils.of(activity.getEndTime()));
                approvalNodes.add(0, startNode);
                return;
            }
            // EndEvent
            if (ELEMENT_EVENT_END.equals(activity.getActivityType())) {
                if (BpmProcessInstanceStatusEnum.isRejectStatus(processInstanceStatus)) {
                    // 拒绝情况下，不需要展示 EndEvent 结束节点。原因是：前端已经展示 x 效果，无需重复展示
                    return;
                }
                ActivityNode endNode = new ActivityNode().setId(activity.getId())
                    .setName(BpmSimpleModelNodeTypeEnum.END_NODE.getName())
                    .setNodeType(BpmSimpleModelNodeTypeEnum.END_NODE.getType()).setStatus(processInstanceStatus)
                    .setStartTime(DateUtils.of(activity.getStartTime()))
                    .setEndTime(DateUtils.of(activity.getEndTime()));
                String reason = FlowableUtils.getProcessInstanceReason(historicProcessInstance);
                if (StrUtil.isNotEmpty(reason)) {
                    endNode.setTasks(singletonList(new ActivityNodeTask().setId(endNode.getId())
                        .setStatus(endNode.getStatus()).setReason(reason)));
                }
                approvalNodes.add(endNode);
            }
            // CallActivity
            if (ELEMENT_CALL_ACTIVITY.equals(activity.getActivityType())) {
                ActivityNode callActivity = new ActivityNode().setId(activity.getId())
                    .setName(BpmSimpleModelNodeTypeEnum.CHILD_PROCESS.getName())
                    .setNodeType(BpmSimpleModelNodeTypeEnum.CHILD_PROCESS.getType()).setStatus(processInstanceStatus)
                    .setStartTime(DateUtils.of(activity.getStartTime()))
                    .setEndTime(DateUtils.of(activity.getEndTime()))
                    .setProcessInstanceId(activity.getCalledProcessInstanceId());
                approvalNodes.add(callActivity);
            }
        });

        // 按照时间排序
        approvalNodes.sort(Comparator.comparing(ActivityNode::getStartTime));
        return approvalNodes;
    }

    /**
     * 获取结束节点的状态
     */
    private Integer getEndActivityNodeStatus(HistoricTaskInstance task) {
        Integer status = FlowableUtils.getTaskStatus(task);
        if (status != null) {
            return status;
        }
        // 结束节点未获取到状态，为跳过状态。可见 bpmn 或者 simple 的 skipExpression
        return BpmTaskStatusEnum.SKIP.getStatus();
    }

    /**
     * 获得【进行中】的活动节点们
     */
    private List<ActivityNode> getRunApproveNodeList(Long startUserId,
        BpmnModel bpmnModel,
        ProcessDefinition processDefinition,
        Map<String, Object> processVariables,
        List<HistoricActivityInstance> activities,
        List<HistoricTaskInstance> tasks) {
        // 构建运行中的任务、子流程，基于 activityId 分组
        List<HistoricActivityInstance> runActivities = filterList(activities, activity -> activity.getEndTime() == null
            && (StrUtil.equalsAny(activity.getActivityType(), ELEMENT_TASK_USER, ELEMENT_CALL_ACTIVITY)));
        Map<String, List<HistoricActivityInstance>> runningTaskMap = convertMultiMap(runActivities,
            HistoricActivityInstance::getActivityId);

        // 按照 activityId 分组，构建 ApprovalNodeInfo 节点
        Map<String, HistoricTaskInstance> taskMap = convertMap(tasks, HistoricTaskInstance::getId);
        return convertList(runningTaskMap.entrySet(), entry -> {
            String activityId = entry.getKey();
            List<HistoricActivityInstance> taskActivities = entry.getValue();
            // 构建活动节点
            FlowElement flowNode = BpmnModelUtils.getFlowElementById(bpmnModel, activityId);
            HistoricActivityInstance firstActivity = CollUtil.getFirst(taskActivities); // 取第一个任务，会签/或签的任务，开始时间相同
            ActivityNode activityNode = new ActivityNode().setId(firstActivity.getActivityId())
                .setName(firstActivity.getActivityName())
                .setNodeType(ObjUtil.defaultIfNull(parseNodeType(flowNode), // 目的：解决“办理节点”和"子流程"的识别
                    BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType()))
                .setStatus(BpmTaskStatusEnum.RUNNING.getStatus())
                .setCandidateStrategy(BpmnModelUtils.parseCandidateStrategy(flowNode))
                .setStartTime(DateUtils.of(CollUtil.getFirst(taskActivities).getStartTime()))
                .setTasks(new ArrayList<>());
            // 处理每个任务的 tasks 属性
            for (HistoricActivityInstance activity : taskActivities) {
                HistoricTaskInstance task = taskMap.get(activity.getTaskId());
                // 特殊情况：子流程节点 ChildProcess 仅存在于 activity 中，并且没有自身的 task，需要跳过执行
                // TODO @芋艿：后续看看怎么优化！
                if (task == null) {
                    continue;
                }
                activityNode.getTasks().add(BpmProcessInstanceConvert.INSTANCE.buildApprovalTaskInfo(task));
                // 加签子任务，需要过滤掉已经完成的加签子任务
                List<HistoricTaskInstance> childrenTasks = filterList(
                    taskService.getAllChildrenTaskListByParentTaskId(activity.getTaskId(), tasks),
                    childTask -> childTask.getEndTime() == null);
                if (CollUtil.isNotEmpty(childrenTasks)) {
                    activityNode.getTasks().addAll(
                        convertList(childrenTasks, BpmProcessInstanceConvert.INSTANCE::buildApprovalTaskInfo));
                }
            }
            // 处理每个任务的 candidateUsers 属性：如果是依次审批，需要预测它的后续审批人。因为 Task 是审批完一个，创建一个新的 Task
            if (BpmnModelUtils.isSequentialUserTask(flowNode)) {
                List<Long> candidateUserIds = getTaskCandidateUserList(bpmnModel, flowNode.getId(),
                    startUserId, processDefinition.getId(), processVariables);
                // 截取当前审批人位置后面的候选人，不包含当前审批人
                ActivityNodeTask approvalTaskInfo = CollUtil.getFirst(activityNode.getTasks());
                Assert.notNull(approvalTaskInfo, "任务不能为空");
                int index = CollUtil.indexOf(candidateUserIds,
                    userId -> ObjectUtils.equalsAny(userId, approvalTaskInfo.getOwner(),
                        approvalTaskInfo.getAssignee())); // 委派或者向前加签情况，需要先比较 owner
                activityNode.setCandidateUserIds(CollUtil.sub(candidateUserIds, index + 1, candidateUserIds.size()));
            }
            if (BpmSimpleModelNodeTypeEnum.CHILD_PROCESS.getType().equals(activityNode.getNodeType())) {
                activityNode.setProcessInstanceId(firstActivity.getCalledProcessInstanceId());
            }
            return activityNode;
        });
    }

    /**
     * 获得【预测（未来）】的活动节点们
     */
    private List<ActivityNode> getSimulateApproveNodeList(Long startUserId, BpmnModel bpmnModel,
        BpmProcessDefinitionInfoDO processDefinitionInfo,
        Map<String, Object> processVariables,
        List<HistoricActivityInstance> activities) {
        // TODO @芋艿：【可优化】在驳回场景下，未来的预测准确性不高。原因是，驳回后，HistoricActivityInstance
        // 包括了历史的操作，不是只有 startEvent 到当前节点的记录
        Set<String> runActivityIds = convertSet(activities, HistoricActivityInstance::getActivityId);
        // 情况一：BPMN 设计器
        if (Objects.equals(BpmModelTypeEnum.BPMN.getType(), processDefinitionInfo.getModelType())) {
            List<FlowElement> flowElements = BpmnModelUtils.simulateProcess(bpmnModel, processVariables);
            return convertList(flowElements, flowElement -> buildNotRunApproveNodeForBpmn(
                startUserId, bpmnModel, flowElements,
                processDefinitionInfo, processVariables, flowElement, runActivityIds));
        }
        // 情况二：SIMPLE 设计器
        if (Objects.equals(BpmModelTypeEnum.SIMPLE.getType(), processDefinitionInfo.getModelType())) {
            BpmSimpleModelNodeVO simpleModel = JsonUtils.parseObject(processDefinitionInfo.getSimpleModel(),
                BpmSimpleModelNodeVO.class);
            List<BpmSimpleModelNodeVO> simpleNodes = SimpleModelUtils.simulateProcess(simpleModel, processVariables);
            return convertList(simpleNodes, simpleNode -> buildNotRunApproveNodeForSimple(
                startUserId, bpmnModel,
                processDefinitionInfo, processVariables, simpleNode, runActivityIds));
        }
        throw new IllegalArgumentException("未知设计器类型：" + processDefinitionInfo.getModelType());
    }

    private ActivityNode buildNotRunApproveNodeForSimple(Long startUserId, BpmnModel bpmnModel,
        BpmProcessDefinitionInfoDO processDefinitionInfo, Map<String, Object> processVariables,
        BpmSimpleModelNodeVO node, Set<String> runActivityIds) {
        // TODO @芋艿：【可优化】在驳回场景下，未来的预测准确性不高。原因是，驳回后，HistoricActivityInstance
        // 包括了历史的操作，不是只有 startEvent 到当前节点的记录
        if (runActivityIds.contains(node.getId())) {
            return null;
        }
        Integer status = BpmTaskStatusEnum.NOT_START.getStatus();
        // 如果节点被跳过。设置状态为跳过
        if (SimpleModelUtils.isSkipNode(node, processVariables)) {
            status = BpmTaskStatusEnum.SKIP.getStatus();
        }
        ActivityNode activityNode = new ActivityNode().setId(node.getId()).setName(node.getName())
            .setNodeType(node.getType()).setCandidateStrategy(node.getCandidateStrategy())
            .setStatus(status);

        // 1. 开始节点/审批节点
        if (ObjectUtils.equalsAny(node.getType(),
            BpmSimpleModelNodeTypeEnum.START_USER_NODE.getType(),
            BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType(),
            BpmSimpleModelNodeTypeEnum.TRANSACTOR_NODE.getType())) {
            List<Long> candidateUserIds = getTaskCandidateUserList(bpmnModel, node.getId(),
                startUserId, processDefinitionInfo.getProcessDefinitionId(), processVariables);
            activityNode.setCandidateUserIds(candidateUserIds);
            return activityNode;
        }

        // 2. 结束节点
        if (BpmSimpleModelNodeTypeEnum.END_NODE.getType().equals(node.getType())) {
            return activityNode;
        }

        // 3. 抄送节点
        if (CollUtil.isEmpty(runActivityIds) && // 流程发起时：需要展示抄送节点，用于选择抄送人
            BpmSimpleModelNodeTypeEnum.COPY_NODE.getType().equals(node.getType())) {
            List<Long> candidateUserIds = getTaskCandidateUserList(bpmnModel, node.getId(),
                startUserId, processDefinitionInfo.getProcessDefinitionId(), processVariables);
            activityNode.setCandidateUserIds(candidateUserIds);
            return activityNode;
        }

        // 4. 子流程节点
        if (BpmSimpleModelNodeTypeEnum.CHILD_PROCESS.getType().equals(node.getType())) {
            return activityNode;
        }
        return null;
    }

    private ActivityNode buildNotRunApproveNodeForBpmn(Long startUserId, BpmnModel bpmnModel,
        List<FlowElement> flowElements,
        BpmProcessDefinitionInfoDO processDefinitionInfo,
        Map<String, Object> processVariables,
        FlowElement node, Set<String> runActivityIds) {
        if (runActivityIds.contains(node.getId())) {
            return null;
        }
        Integer status = BpmTaskStatusEnum.NOT_START.getStatus();
        // 如果节点被跳过，状态设置为跳过
        if (BpmnModelUtils.isSkipNode(node, processVariables)) {
            status = BpmTaskStatusEnum.SKIP.getStatus();
        }
        ActivityNode activityNode = new ActivityNode().setId(node.getId())
            .setStatus(status);

        // 1. 开始节点
        if (node instanceof StartEvent) {
            if (CollUtil.contains(flowElements, // 特殊：如果已经存在用户手动创建的 START_USER_NODE_ID 节点，则忽略 StartEvent
                flowElement -> flowElement.getId().equals(START_USER_NODE_ID))) {
                return null;
            }
            return activityNode.setName(BpmSimpleModelNodeTypeEnum.START_USER_NODE.getName())
                .setNodeType(BpmSimpleModelNodeTypeEnum.START_USER_NODE.getType());
        }

        // 2. 审批节点
        if (node instanceof UserTask) {
            List<Long> candidateUserIds = getTaskCandidateUserList(bpmnModel, node.getId(),
                startUserId, processDefinitionInfo.getProcessDefinitionId(), processVariables);
            return activityNode.setName(node.getName()).setNodeType(BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType())
                .setCandidateStrategy(BpmnModelUtils.parseCandidateStrategy(node))
                .setCandidateUserIds(candidateUserIds);
        }

        // 3. 结束节点
        if (node instanceof EndEvent) {
            return activityNode.setName(BpmSimpleModelNodeTypeEnum.END_NODE.getName())
                .setNodeType(BpmSimpleModelNodeTypeEnum.END_NODE.getType());
        }
        return null;
    }

    private List<Long> getTaskCandidateUserList(BpmnModel bpmnModel, String activityId,
        Long startUserId, String processDefinitionId, Map<String, Object> processVariables) {
        Set<Long> userIds = taskCandidateInvoker.calculateUsersByActivity(bpmnModel, activityId,
            startUserId, processDefinitionId, processVariables);
        return new ArrayList<>(userIds);
    }

    @Override
    public BpmProcessInstanceBpmnModelViewRespVO getProcessInstanceBpmnModelView(String id) {
        // 1.1 获得流程实例
        HistoricProcessInstance processInstance = getHistoricProcessInstance(id);
        if (processInstance == null) {
            return null;
        }
        // 1.2 获得流程定义
        BpmnModel bpmnModel = processDefinitionService
            .getProcessDefinitionBpmnModel(processInstance.getProcessDefinitionId());
        if (bpmnModel == null) {
            return null;
        }
        BpmSimpleModelNodeVO simpleModel = null;
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService.getProcessDefinitionInfo(
            processInstance.getProcessDefinitionId());
        if (processDefinitionInfo != null
            && BpmModelTypeEnum.SIMPLE.getType().equals(processDefinitionInfo.getModelType())) {
            simpleModel = JsonUtils.parseObject(processDefinitionInfo.getSimpleModel(), BpmSimpleModelNodeVO.class);
        }
        // 1.3 获得流程实例对应的活动实例列表 + 任务列表
        List<HistoricActivityInstance> activities = taskService.getActivityListByProcessInstanceId(id);
        List<HistoricTaskInstance> tasks = taskService.getTaskListByProcessInstanceId(id, true);

        // 2.1 拼接进度信息
        Set<String> unfinishedTaskActivityIds = convertSet(activities, HistoricActivityInstance::getActivityId,
            activityInstance -> activityInstance.getEndTime() == null);
        Set<String> finishedTaskActivityIds = convertSet(activities, HistoricActivityInstance::getActivityId,
            activityInstance -> activityInstance.getEndTime() != null
                && ObjectUtil.notEqual(activityInstance.getActivityType(),
                BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW));
        Set<String> finishedSequenceFlowActivityIds = convertSet(activities, HistoricActivityInstance::getActivityId,
            activityInstance -> activityInstance.getEndTime() != null
                && ObjectUtil.equals(activityInstance.getActivityType(),
                BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW));
        // 特殊：会签情况下，会有部分已完成（审批）、部分未完成（待审批），此时需要 finishedTaskActivityIds 移除掉
        finishedTaskActivityIds.removeAll(unfinishedTaskActivityIds);
        // 特殊：如果流程实例被拒绝，则需要计算是哪个活动节点。
        // 注意，只取最后一个。因为会存在多次拒绝的情况，拒绝驳回到指定节点
        Set<String> rejectTaskActivityIds = CollUtil.newHashSet();
        if (BpmProcessInstanceStatusEnum.isRejectStatus(FlowableUtils.getProcessInstanceStatus(processInstance))) {
            tasks.stream()
                .filter(task -> BpmTaskStatusEnum.isRejectStatus(FlowableUtils.getTaskStatus(task)))
                .max(Comparator.comparing(HistoricTaskInstance::getEndTime))
                .ifPresent(reject -> rejectTaskActivityIds.add(reject.getTaskDefinitionKey()));
            finishedTaskActivityIds.removeAll(rejectTaskActivityIds);
        }

        // 2.2 拼接基础信息
        Set<Long> userIds = BpmProcessInstanceConvert.INSTANCE.parseUserIds02(processInstance, tasks);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, DeptRespDTO> deptMap = new HashMap<>(8);
//        if  (CollectionUtil.isNotEmpty(convertSet(userMap.values(), AdminUserRespDTO::getDeptId))) {
//            Set<Long> longs = convertSet(userMap.values(), AdminUserRespDTO::getDeptId);
//            deptMap = deptApi.getDeptMap(longs);
//        }
        return BpmProcessInstanceConvert.INSTANCE.buildProcessInstanceBpmnModelView(processInstance, tasks, bpmnModel,
            simpleModel,
            unfinishedTaskActivityIds, finishedTaskActivityIds, finishedSequenceFlowActivityIds,
            rejectTaskActivityIds,
            userMap, deptMap);
    }

    // ========== Update 写入相关方法 ==========
    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成指定长度的随机字符（数字+字母）
     *
     * @param length 长度（此处固定为20）
     * @return 随机字符串
     */
    public static String generateRandomString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于0");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // 从字符集中随机选取一个字符
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @DataPermission(enable = false)
    public String createProcessInstance(@Valid BpmProcessInstanceCreateReqVO createReqVO) {

        log.info("bpmn-log-------创建流程实例{}", createReqVO);

        Long businessId = createReqVO.getBusinessId();
        Long userId = createReqVO.getApplicantId();
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        log.info("bpmn-log-------创建流程实例user{},{}", userId, loginUserId);
        // 登录拦截
        if (!Objects.equals(loginUserId, userId) || loginUserId == null) {
            throw exception(UNAUTHORIZED);
        }
        // 测试
//        String processInstanceId = generateRandomString(20);
        // 获得流程定义
        ProcessDefinition definition = processDefinitionService
            .getProcessDefinition(createReqVO.getProcessDefinitionId());
        log.info("bpmn-log-------获得流程定义{}", definition);

        // 发起流程
        String processInstanceId = createProcessInstance0(userId, definition, createReqVO.getVariables(), null,
            createReqVO.getStartUserSelectAssignees());
        log.info("bpmn-log-------发起流程{}", processInstanceId);
        // 保存流程业务信息
        BpmBusinessDO bpmBusinessDO = new BpmBusinessDO();
        bpmBusinessDO.setOaProjectId(createReqVO.getOaProjectId());
        bpmBusinessDO.setBusinessId(businessId);

        // 封装流程唯一ID
        bpmBusinessDO.setProcInstId(processInstanceId);
        // 封装发起门店
        bpmBusinessDO.setStoreId(createReqVO.getStoreId());
        bpmBusinessDO.setStoreName(createReqVO.getStoreName());
        // 封装发起部门
        // 若角色填写了所属门店 无所属部门 默认为线下门店
        if (Objects.isNull(createReqVO.getDeptId()) && Objects.nonNull(createReqVO.getStoreId())){
            bpmBusinessDO.setDeptId(BPM_OA_BUSINESS_STORE_ID);
            bpmBusinessDO.setDeptName(BPM_OA_BUSINESS_STORE_NAME);
        } else {
            bpmBusinessDO.setDeptId(createReqVO.getDeptId());
            bpmBusinessDO.setDeptName(createReqVO.getDeptName());
        }


        // 封装任务标题与任务描述

        /*Long l = bpmProcessBusinessMapper.selectCount(
            new LambdaQueryWrapper<BpmBusinessDO>().eq(BpmBusinessDO::getTaskName, createReqVO.getTaskName()));
        if (l == 0) {
            bpmBusinessDO.setTaskName(createReqVO.getTaskName());
        } else {
            throw exception(TASK_NAME_NOT_UNIQUE);
        }*/
        bpmBusinessDO.setTaskName(createReqVO.getTaskName());
        bpmBusinessDO.setTaskDesc(createReqVO.getTaskDesc());

        // 获取发起人信息 保存到流程业务表
//        Long loginUserId = WebFrameworkUtils.getLoginUserId();
        // 封装申请人信息
        Long applicantId = createReqVO.getApplicantId();

        // 申请人权限验证
        if (!Objects.equals(applicantId, loginUserId)) {
            throw exception(UNAUTHORIZED);
        }

        bpmBusinessDO.setApplicant(createReqVO.getApplicant());
        bpmBusinessDO.setApplicantId(createReqVO.getApplicantId());
        bpmBusinessDO.setApplicantName(createReqVO.getApplicantName());
        // 封装流程
        bpmBusinessDO.setFlowId(createReqVO.getProcessDefinitionId());
        bpmBusinessDO.setFlowName(createReqVO.getProcessDefinitionName());
        // 封装优先级
        bpmBusinessDO.setPriority(createReqVO.getPriority());
        // 封装任务类型
        Integer taskType = createReqVO.getTaskType();
        // 任务类型
        bpmBusinessDO.setTaskType(createReqVO.getTaskType());
        // 期望完成时间
        bpmBusinessDO.setCompletionTime(createReqVO.getCompletionTime());

        bpmBusinessDO.setTaskState(0);

        bpmBusinessDO.setHandleDeptId(createReqVO.getHandleDeptId());
        bpmBusinessDO.setHandleDeptName(createReqVO.getHandleDeptName());

        bpmBusinessDO.setFormMsg(createReqVO.getFormMsg());

        bpmBusinessDO.setIconUrl(createReqVO.getIconUrl());
        // 内部任务
        if (taskType == 0) {
            // 封装处理部门
            bpmBusinessDO.setHandleDeptId(createReqVO.getHandleDeptId());
            bpmBusinessDO.setHandleDeptName(createReqVO.getHandleDeptName());
            if (CollectionUtil.isNotEmpty(createReqVO.getExecutorUserId())) {
                bpmBusinessDO.setExecutorUserId(String.join(",", createReqVO.getExecutorUserId()));
            }
            if (CollectionUtil.isNotEmpty(createReqVO.getExecutorUserName())) {
                bpmBusinessDO.setExecutorUserName(String.join(",", createReqVO.getExecutorUserName()));
            }
            if (CollectionUtil.isNotEmpty(createReqVO.getExecutorDeptId())) {
                bpmBusinessDO.setExecutorDeptId(String.join(",", createReqVO.getExecutorDeptId()));
            }
            if (CollectionUtil.isNotEmpty(createReqVO.getExecutorDeptName())) {
                bpmBusinessDO.setExecutorDeptName(String.join(",", createReqVO.getExecutorDeptName()));
            }


        } else if (taskType == 1) { // 门店任务
            // 如果是门店任务 执行部门ID 1   部门名称 线下门店    执行人1  执行人名称 门店
            bpmBusinessDO.setExecutorDeptName("线下门店");
            bpmBusinessDO.setExecutorUserName("门店");
            Integer storeType = createReqVO.getStoreType();
            // 封装执行门店
            bpmBusinessDO.setStoreType(createReqVO.getStoreType());
            // 选择部分门店
            if (storeType != null && storeType == 2) {
                // 当为部分门店时，保存部门门店扩展信息
                List<StoreInfoReqVO> storeInfos = createReqVO.getStoreInfos();
                List<BussinessTaskStoreDO> storeInfoDOList = BeanUtils.toBean(storeInfos, BussinessTaskStoreDO.class);
                bpmBusinessDO.setStoreCount(storeInfos.size());

                // 补全流程唯一标识
                storeInfoDOList.forEach(doObj ->
                    {
                        doObj.setProcInstId(processInstanceId);
                        doObj.setBusinessId(createReqVO.getBusinessId());
                    }
                );
                if (storeInfoDOList.size() == 1) {
                    bpmBusinessDO.setExecutorUserId(String.valueOf(storeInfoDOList.get(0).getUserId()));
                    bpmBusinessDO.setExecutorUserName(storeInfoDOList.get(0).getStoreLeader());
                    bpmBusinessDO.setExecutorDeptName(storeInfoDOList.get(0).getExecutorStoreName());
                    bpmBusinessDO.setExecutorDeptId(String.valueOf(storeInfoDOList.get(0).getExecutorStoreId()));
                }
                // 批量插入部分门店信息
                processBusinessService.createProcessStoreInfo(storeInfoDOList);
            }


        } else {
            throw exception(TASK_NAME_NOT_EXIST);
        }
        // 封装附件地址
        bpmBusinessDO.setFileUrls(createReqVO.getFileUrls());

        processBusinessService.createProcessBusiness(bpmBusinessDO);
        log.info("bpmn-log-------保存流程业务信息{}", bpmBusinessDO);
        return processInstanceId;

    }

    @DataPermission(enable = false)
    @Transactional(rollbackFor = Exception.class)
    @Async("applicationTaskExecutor")
    public void createProcessStoreChildInstance(BpmProcessStoreChildReqVO childReqVO) {

        // 根据流程唯一标识 读取业务表信息
        BpmBusinessDO businessTask = processBusinessService.getBusinessTask(childReqVO.getProcInstId());
        Long businessId = businessTask.getBusinessId();

        // 选定门店任务子流程模板
        Integer taskType = businessTask.getTaskType();
        if (taskType != 1) {
            // 内部任务
            throw exception(STORE_BUSINESS_NOT_EXISTS);
        }

        //获取需要执行的门店列表
        List<BussinessTaskStoreDO> storeList = getBussinessTaskStoreDOS(childReqVO, businessTask, businessId);

        // 判断是否执行复批 选定子流程模板
        BpmProcessDefinitionInfoDO processDefinitionInfo = bpmProcessDefinitionService.
            getProcessDefinitionInfo(businessTask.getFlowId());
        if (processDefinitionInfo == null) {
            throw exception(STORE_TASK_HAS_NOT_FLOW_ID);
        }


        //是否需要复批
        Boolean isRepeat = processDefinitionInfo.getCompletionApprovalEnabled();
        if (isRepeat == null) {
            isRepeat = false;
        }

        // 模板ID
        String storeTaskId = isRepeat ? storeTaskApprove : storeTaskNoApprove;
        // 反馈ID
        String feedBackId = isRepeat ? storeTaskFeedBackActivityIDForApprove : storeTaskFeedBackActivityIDForNoApprove;


        // 设计门店任务反馈人 与复批人
        Map<String, List<Long>> startUserSelectAssignees = new HashMap<>();

        if (isRepeat) {
            List<Long> repeatMembers = getRepeatMembers(processDefinitionInfo);
            //只要复批指定人员为空 ，就门店子任务就走 不需要复批的流程
            if (repeatMembers == null || repeatMembers.isEmpty()){
                isRepeat = false;
                storeTaskId = storeTaskNoApprove;
                feedBackId = storeTaskFeedBackActivityIDForNoApprove;
            }else {
                startUserSelectAssignees.put(storeTaskReApproveActivityIDForApprove, repeatMembers);
            }
        }


        List<BpmBusinessDO> bpmBusinessDOList = new ArrayList<>(storeList.size());

        //获取子流程需要执行的定义
        ProcessDefinition definition = processDefinitionService.getProcessDefinition(storeTaskId);

        // 获取选中门店信息
        for (int i = 0; i < storeList.size(); i++) {
            try {
                BussinessTaskStoreDO bpmStoreInfoDO = storeList.get(i);
                Long storeUserId = bpmStoreInfoDO.getUserId(); // 门店店长ID
                if (storeUserId == null) {
                    continue;
                }
                //设置 反馈ID
                Map<String, List<Long>> assignees = new HashMap<>();
                assignees.put(feedBackId, Collections.singletonList(storeUserId));

                if (isRepeat) {
                    // 复批
                    assignees.put(storeTaskReApproveActivityIDForApprove, startUserSelectAssignees.get(storeTaskReApproveActivityIDForApprove));
                }

                // 1. 先构建子流程参数，创建子流程实例
                Map<String, Object> variables = new HashMap<>();
                variables.put("mainProcessInstanceId", childReqVO.getProcInstId());
                variables.put("batchNo", i + 1);
                variables.put("taskName", businessTask.getTaskName());
                variables.put("priority", businessTask.getPriority());
                variables.put("completionTime", businessTask.getCompletionTime());
                variables.put("taskDesc", businessTask.getTaskDesc());
                variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_ID, storeUserId); // 设置流程变量，发起人 ID

                String businessKey = "SUB_" + childReqVO.getProcInstId() + "_" + i;

                // 发起子流程
                BpmProcessDefinitionInfoDO sonProcessDefinitionInfo = processDefinitionService
                    .getProcessDefinitionInfo(definition.getId());

                String bProcessInstanceId = createProcessInstanceByStoreTask(
                        storeUserId,
                        sonProcessDefinitionInfo,
                        variables,
                        definition.getName(),
                        assignees);

                // 3. 构建子流程业务数据（关联主流程和子流程）
                BpmBusinessDO businessDO = getBpmBusinessDO(childReqVO, businessTask, bpmStoreInfoDO, storeUserId, bProcessInstanceId, businessKey);
                bpmBusinessDOList.add(businessDO);
            } catch (Exception ex) {
                log.error("流程创建子流程异常：主流程ID={}, 门店名称={},店长={} 异常原因",
                    businessTask.getProcInstId(),
                    (storeList.size() > i ? storeList.get(i).getExecutorStoreName() : "未知门店"),
                    (storeList.size() > i ? storeList.get(i).getUserId() : "未知店长"),
                    ex);
            }

        }
        if (CollectionUtil.isNotEmpty(bpmBusinessDOList)) {
            processBusinessService.createProcessBusinessBatch(bpmBusinessDOList);
        }

    }

    private List<Long> getRepeatMembers(BpmProcessDefinitionInfoDO processDefinitionInfo) {
        List<Long> specifiedMembers = processDefinitionInfo.getSpecifiedMembers();
        Long deptId = processDefinitionInfo.getDeptId();
        //指定成员列表  （为空时由发起人直属主管审批)

        List<Long> repeatMembers = new ArrayList<>();

        if (CollectionUtil.isNotEmpty(specifiedMembers)) {
            repeatMembers = specifiedMembers;
        } else {
            // 获取部门负责人
            Long deptManager = deptApi.getDeptManager(deptId).getCheckedData();
            if (deptManager != null){
                repeatMembers = List.of(deptManager);
            }else {
                Integer strategy = processDefinitionInfo.getNoDepartmentHeadStrategy();
                //无部门负责人时的处理策略 1-自动通过 2-自动拒绝 3-指定人员审批
                if (strategy == 3 ){
                    repeatMembers = processDefinitionInfo.getAssigneeList();
                }

                //todo 12策略如何实现
            }
        }
        return repeatMembers;
    }

    /**
     * 获取需要执行的门店列表
     * @param childReqVO
     * @param businessTask
     * @param businessId
     * @return
     */
    private List<BussinessTaskStoreDO> getBussinessTaskStoreDOS(BpmProcessStoreChildReqVO childReqVO,
        BpmBusinessDO businessTask, Long businessId) {
        List<BussinessTaskStoreDO> storeList = new ArrayList<>();
        Integer storeType = businessTask.getStoreType();
        if (storeType == 2) {
            // 部分门店
            storeList = processBusinessService.getStoreList(childReqVO.getProcInstId(), businessId);
        } else {
            // 全部门店 todo 后面换成执行一次system api 调用
            List<BpmAllStoreInfoDO> list = processBusinessService.getAllStoreList(businessId);
            for (BpmAllStoreInfoDO storeInfo : list) {
                BussinessTaskStoreDO taskStore = new BussinessTaskStoreDO();
                // 复制同名字段（若有），再单独处理不同名字段
                BeanUtils.copyProperties(storeInfo, taskStore);
                // 处理字段名不同的映射
                taskStore.setExecutorStoreId(storeInfo.getStoreId());
                taskStore.setExecutorStoreName(storeInfo.getStoreName());
                // 设置固定值
                taskStore.setProcInstId(childReqVO.getProcInstId());
                storeList.add(taskStore);
            }
        }
        return storeList;
    }

    private static BpmBusinessDO getBpmBusinessDO(BpmProcessStoreChildReqVO childReqVO, BpmBusinessDO businessTask,
        BussinessTaskStoreDO bpmStoreInfoDO, Long storeUserId, String bProcessInstanceId, String businessKey) {
        BpmBusinessDO businessDO = new BpmBusinessDO();
        BeanUtils.copyProperties(businessTask, businessDO);
        // 重置主流程相关字段，设置子流程专属字段
        businessDO.setTaskId(null); // 自增主键重置，避免冲突
        businessDO.setTaskName(businessTask.getTaskName() + "_" + bpmStoreInfoDO.getExecutorStoreName());
        businessDO.setApplicantId(storeUserId);
        businessDO.setApplicantName(bpmStoreInfoDO.getStoreLeader());
        businessDO.setApplicant(bpmStoreInfoDO.getStoreLeaderPhone());
        businessDO.setProcInstId(bProcessInstanceId); // 子流程实例ID
        businessDO.setParentProcInstId(childReqVO.getProcInstId()); // 关联主流程
        businessDO.setStoreId(bpmStoreInfoDO.getExecutorStoreId()); // 绑定当前门店
        businessDO.setStoreName(bpmStoreInfoDO.getExecutorStoreName());
        businessDO.setTaskState(0); // 子流程初始状态
        businessDO.setBusinessKey(businessKey);
        businessDO.setExecutorUserId(String.valueOf(bpmStoreInfoDO.getUserId()));
        businessDO.setExecutorUserName(bpmStoreInfoDO.getStoreLeader());
        businessDO.setExecutorDeptId(String.valueOf(bpmStoreInfoDO.getExecutorStoreId()));
        businessDO.setExecutorDeptName(bpmStoreInfoDO.getExecutorStoreName());
        businessDO.setTaskType(1);
        businessDO.setCompletionTime(businessTask.getCompletionTime());
        businessDO.setFormMsg(businessTask.getFormMsg());
        businessDO.setIconUrl(businessTask.getIconUrl());
        businessDO.setOaProjectId(null);
        return businessDO;
    }

    @Override
    public String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqDTO createReqDTO) {
        return FlowableUtils.executeAuthenticatedUserId(userId, () -> {
            // 获得流程定义
            ProcessDefinition definition = processDefinitionService
                .getActiveProcessDefinition(createReqDTO.getProcessDefinitionKey());
            // 发起流程
            return createProcessInstance0(userId, definition, createReqDTO.getVariables(),
                createReqDTO.getBusinessKey(),
                createReqDTO.getStartUserSelectAssignees());
        });
    }

    private String createProcessInstanceByStoreTask(Long storeUserId, BpmProcessDefinitionInfoDO processDefinitionInfo,
        Map<String, Object> variables, String title,
        Map<String, List<Long>> startUserSelectAssignees){

        // 流程实例状态：审批中
        variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS, BpmProcessInstanceStatusEnum.RUNNING.getStatus());
        // 跳过表达式需要添加此变量为 true，不影响没配置 skipExpression 的节点
        variables.put(BpmnVariableConstants.PROCESS_INSTANCE_SKIP_EXPRESSION_ENABLED, true);
        if (CollUtil.isNotEmpty(startUserSelectAssignees)) {
            // 设置流程变量，发起人自选审批人
            variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_SELECT_ASSIGNEES,
                startUserSelectAssignees);
        }

        String originalUserId = Authentication.getAuthenticatedUserId();

        try {
            // 将当前 userId 绑定到 Flowable 上下文
            Authentication.setAuthenticatedUserId(String.valueOf(storeUserId));
            // 3. 创建流程
            ProcessInstanceBuilder processInstanceBuilder = runtimeService.createProcessInstanceBuilder()
                    .processDefinitionId(processDefinitionInfo.getProcessDefinitionId())
                    .businessKey(null)
                    .variables(variables);
            // 3.1 创建流程 ID
            BpmModelMetaInfoVO.ProcessIdRule processIdRule = processDefinitionInfo.getProcessIdRule();
            if (processIdRule != null && Boolean.TRUE.equals(processIdRule.getEnable())) {
                processInstanceBuilder.predefineProcessInstanceId(processIdRedisDAO.generate(processIdRule));
            }
            // 3.2 流程名称
            processInstanceBuilder.name(title);
            // 3.3 发起流程实例
            ProcessInstance instance = processInstanceBuilder.start();
            return instance.getId();
        }finally {
            // 恢复原有上下文，避免线程复用污染
            Authentication.setAuthenticatedUserId(originalUserId);
        }



    }

    private String createProcessInstance0(Long userId, ProcessDefinition definition,
        Map<String, Object> variables, String businessKey,
        Map<String, List<Long>> startUserSelectAssignees) {
        // 1.1 校验流程定义
        if (definition == null) {
            throw exception(PROCESS_DEFINITION_NOT_EXISTS);
        }
        if (definition.isSuspended()) {
            throw exception(PROCESS_DEFINITION_IS_SUSPENDED);
        }
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService
            .getProcessDefinitionInfo(definition.getId());
        if (processDefinitionInfo == null) {
            throw exception(PROCESS_DEFINITION_NOT_EXISTS);
        }
        // 1.2 校验是否能够发起
        if (!processDefinitionService.canUserStartProcessDefinition(processDefinitionInfo, userId)) {
            throw exception(PROCESS_INSTANCE_START_USER_CAN_START);
        }
        // 1.3 校验发起人自选审批人
        validateStartUserSelectAssignees(userId, definition, startUserSelectAssignees, variables);

        // 2. 创建流程实例
        if (variables == null) {
            variables = new HashMap<>();
        }
        FlowableUtils.filterProcessInstanceFormVariable(variables); // 过滤一下，避免 ProcessInstance 系统级的变量被占用
        variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_ID, userId); // 设置流程变量，发起人 ID
        variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS, // 流程实例状态：审批中
            BpmProcessInstanceStatusEnum.RUNNING.getStatus());
        variables.put(BpmnVariableConstants.PROCESS_INSTANCE_SKIP_EXPRESSION_ENABLED,
            true); // 跳过表达式需要添加此变量为 true，不影响没配置 skipExpression 的节点
        if (CollUtil.isNotEmpty(startUserSelectAssignees)) {
            // 设置流程变量，发起人自选审批人
            variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_SELECT_ASSIGNEES,
                startUserSelectAssignees);
        }

        // 3. 创建流程
        ProcessInstanceBuilder processInstanceBuilder = runtimeService.createProcessInstanceBuilder()
            .processDefinitionId(definition.getId())
            .businessKey(businessKey)
            .variables(variables);
        // 3.1 创建流程 ID
        BpmModelMetaInfoVO.ProcessIdRule processIdRule = processDefinitionInfo.getProcessIdRule();
        if (processIdRule != null && Boolean.TRUE.equals(processIdRule.getEnable())) {
            processInstanceBuilder.predefineProcessInstanceId(processIdRedisDAO.generate(processIdRule));
        }
        // 3.2 流程名称
        processInstanceBuilder.name(generateProcessInstanceName(userId, definition, processDefinitionInfo, variables));
        // 3.3 发起流程实例
        ProcessInstance instance = processInstanceBuilder.start();
        return instance.getId();
    }

    private void validateStartUserSelectAssignees(Long userId, ProcessDefinition definition,
        Map<String, List<Long>> startUserSelectAssignees,
        Map<String, Object> variables) {
        // 1. 获取预测的节点信息
        BpmApprovalDetailRespVO detailRespVO = getApprovalDetail(userId, new BpmApprovalDetailReqVO()
            .setProcessDefinitionId(definition.getId())
            .setProcessVariables(variables));
        List<ActivityNode> activityNodes = detailRespVO.getActivityNodes();
        if (CollUtil.isEmpty(activityNodes)) {
            return;
        }

        // 2.1 移除掉不是发起人自选审批人节点
        activityNodes.removeIf(task ->
            ObjectUtil.notEqual(BpmTaskCandidateStrategyEnum.START_USER_SELECT.getStrategy(),
                task.getCandidateStrategy()));
        // 2.2 流程发起时要先获取当前流程的预测走向节点，发起时只校验预测的节点发起人自选审批人的审批人和抄送人是否都配置了
        activityNodes.forEach(task -> {
            List<Long> assignees = startUserSelectAssignees != null ? startUserSelectAssignees.get(task.getId()) : null;
            if (CollUtil.isEmpty(assignees)) {
                throw exception(PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_CONFIG, task.getName());
            }
            Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(assignees);
            assignees.forEach(assignee -> {
                if (userMap.get(assignee) == null) {
                    throw exception(PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_EXISTS, task.getName(), assignee);
                }
            });
        });
    }

    private String generateProcessInstanceName(Long userId,
        ProcessDefinition definition,
        BpmProcessDefinitionInfoDO definitionInfo,
        Map<String, Object> variables) {
        if (definition == null || definitionInfo == null) {
            return null;
        }
        BpmModelMetaInfoVO.TitleSetting titleSetting = definitionInfo.getTitleSetting();
        if (titleSetting == null || !BooleanUtil.isTrue(titleSetting.getEnable())) {
            return definition.getName();
        }
//        AdminUserRespDTO user = adminUserApi.getUser(userId).getCheckedData();
        Map<String, Object> cloneVariables = new HashMap<>(variables);
//        cloneVariables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_ID, user.getNickname());
        cloneVariables.put(BpmnVariableConstants.PROCESS_START_TIME, DateUtil.now());
        cloneVariables.put(BpmnVariableConstants.PROCESS_DEFINITION_NAME, definition.getName().trim());
        cloneVariables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_ID, userId);
        return StrUtil.format(definitionInfo.getTitleSetting().getTitle(), cloneVariables);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProcessInstanceByStartUser(Long userId, @Valid BpmProcessInstanceCancelReqVO cancelReqVO) {
        // 1.1 校验流程实例存在
        ProcessInstance instance = getProcessInstance(cancelReqVO.getId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_CANCEL_FAIL_NOT_EXISTS);
        }
        // 1.2 只能取消自己的
        if (!Objects.equals(instance.getStartUserId(), String.valueOf(userId))) {
            throw exception(PROCESS_INSTANCE_CANCEL_FAIL_NOT_SELF);
        }
        // 1.3 校验允许撤销审批中的申请
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService
            .getProcessDefinitionInfo(instance.getProcessDefinitionId());
        Assert.notNull(processDefinitionInfo, "流程定义({})不存在", processDefinitionInfo);
        if (processDefinitionInfo.getAllowCancelRunningProcess() != null // 防止未配置 AllowCancelRunningProcess , 默认为可取消
            && BooleanUtil.isFalse(processDefinitionInfo.getAllowCancelRunningProcess())) {
            throw exception(PROCESS_INSTANCE_CANCEL_FAIL_NOT_ALLOW);
        }
        // 1.4 子流程不允许取消
        if (StrUtil.isNotBlank(instance.getSuperExecutionId())) {
            throw exception(PROCESS_INSTANCE_CANCEL_CHILD_FAIL_NOT_ALLOW);
        }

        LambdaUpdateWrapper<BpmBusinessDO> update = new LambdaUpdateWrapper<>();
        update.eq(BpmBusinessDO::getProcInstId, cancelReqVO.getId());
        update.set(BpmBusinessDO::getTaskState, BpmProcessInstanceStatusEnum.CANCEL.getStatus());
        bpmProcessBusinessMapper.update(update);
        // 2. 取消流程
        updateProcessInstanceCancel(cancelReqVO.getId(),
            BpmReasonEnum.CANCEL_PROCESS_INSTANCE_BY_START_USER.format(cancelReqVO.getReason()));
    }

    @Override
    public void cancelProcessInstanceByAdmin(Long userId, BpmProcessInstanceCancelReqVO cancelReqVO) {
        // 1.1 校验流程实例存在
        ProcessInstance instance = getProcessInstance(cancelReqVO.getId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_CANCEL_FAIL_NOT_EXISTS);
        }

        // 2. 取消流程
        AdminUserRespDTO user = adminUserApi.getUser(userId).getCheckedData();
        updateProcessInstanceCancel(cancelReqVO.getId(),
            BpmReasonEnum.CANCEL_PROCESS_INSTANCE_BY_ADMIN.format(user.getNickname(), cancelReqVO.getReason()));
    }

    private void updateProcessInstanceCancel(String id, String reason) {
        // 1. 更新流程实例 status
        runtimeService.setVariable(id, BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS,
            BpmProcessInstanceStatusEnum.CANCEL.getStatus());
        runtimeService.setVariable(id, BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_REASON, reason);

        // 2. 取消所有子流程
        List<ProcessInstance> childProcessInstances = runtimeService.createProcessInstanceQuery()
            .superProcessInstanceId(id).list();
        childProcessInstances.forEach(processInstance -> updateProcessInstanceCancel(
            processInstance.getProcessInstanceId(),
            BpmReasonEnum.CANCEL_CHILD_PROCESS_INSTANCE_BY_MAIN_PROCESS.getReason()));

        // 3. 结束流程
        taskService.moveTaskToEnd(id, reason);
    }

    @Override
    public void updateProcessInstanceReject(ProcessInstance processInstance, String reason) {
        runtimeService.setVariable(processInstance.getProcessInstanceId(),
            BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS,
            BpmProcessInstanceStatusEnum.REJECT.getStatus());
        runtimeService.setVariable(processInstance.getProcessInstanceId(),
            BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_REASON,
            BpmReasonEnum.REJECT_TASK.format(reason));
    }

    @Override
    public void updateProcessInstanceVariables(String id, Map<String, Object> variables) {
        runtimeService.setVariables(id, variables);
    }

    @Override
    public void removeProcessInstanceVariables(String id, Collection<String> variableNames) {
        runtimeService.removeVariables(id, variableNames);
    }

    // ========== Event 事件相关方法 ==========

    @Override
    public void processProcessInstanceCompleted(ProcessInstance instance) {
        // 1.1 获取当前状态
        Integer status = (Integer) instance.getProcessVariables()
            .get(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS);
        String reason = (String) instance.getProcessVariables()
            .get(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_REASON);
        // 1.2 当流程状态还是审批状态中，说明审批通过了，则变更下它的状态
        // 为什么这么处理？因为流程完成，并且完成了，说明审批通过了
        if (Objects.equals(status, BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            status = BpmProcessInstanceStatusEnum.APPROVE.getStatus();
            runtimeService.setVariable(instance.getId(), BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS,
                status);
        }

        // 2. 发送对应的消息通知
//        if (Objects.equals(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
//            messageService.sendMessageWhenProcessInstanceApprove(BpmProcessInstanceConvert.INSTANCE.buildProcessInstanceApproveMessage(instance));
//        } else if (Objects.equals(status, BpmProcessInstanceStatusEnum.REJECT.getStatus())) {
//            messageService.sendMessageWhenProcessInstanceReject(BpmProcessInstanceConvert.INSTANCE.buildProcessInstanceRejectMessage(instance, reason));
//        }

        // 3. 发送流程实例的状态事件
        processInstanceEventPublisher.sendProcessInstanceResultEvent(
            BpmProcessInstanceConvert.INSTANCE.buildProcessInstanceStatusEvent(this, instance, status, reason));

        // 4. 流程后置通知
        if (Objects.equals(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
            BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService.
                getProcessDefinitionInfo(instance.getProcessDefinitionId());
            if (ObjUtil.isNotNull(processDefinitionInfo) &&
                ObjUtil.isNotNull(processDefinitionInfo.getProcessAfterTriggerSetting())) {
                BpmModelMetaInfoVO.HttpRequestSetting setting = processDefinitionInfo.getProcessAfterTriggerSetting();

                BpmHttpRequestUtils.executeBpmHttpRequest(instance,
                    setting.getUrl(), setting.getHeader(), setting.getBody(), true, setting.getResponse());
            }
        }
    }

    @Override
    public void processProcessInstanceCreated(ProcessInstance instance) {
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService.
            getProcessDefinitionInfo(instance.getProcessDefinitionId());
        ProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(
            instance.getProcessDefinitionId());
        if (processDefinition == null || processDefinitionInfo == null) {
            return;
        }

        // 自定义标题。目的：主要处理子流程的标题无法处理
        // 注意：必须使用 TransactionSynchronizationManager 事务提交后，否则不生效！！！
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                String name = generateProcessInstanceName(Long.valueOf(instance.getStartUserId()),
                    processDefinition, processDefinitionInfo, instance.getProcessVariables());
                if (ObjUtil.notEqual(instance.getName(), name)) {
                    runtimeService.setProcessInstanceName(instance.getProcessInstanceId(), name);
                }
            }

        });

        // 流程前置通知
        if (ObjUtil.isNull(processDefinitionInfo.getProcessBeforeTriggerSetting())) {
            return;
        }
        BpmModelMetaInfoVO.HttpRequestSetting setting = processDefinitionInfo.getProcessBeforeTriggerSetting();
        BpmHttpRequestUtils.executeBpmHttpRequest(instance,
            setting.getUrl(), setting.getHeader(), setting.getBody(), true, setting.getResponse());
    }

    @Override
    public PageResult<BpmBusinessQueryDO> queryBusinessTask(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {

        Integer isAll = bpmProcessInstanceQueryReqVO.getIsAll();
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(bpmProcessInstanceQueryReqVO.getPageNo());
        pageParam.setPageSize(bpmProcessInstanceQueryReqVO.getPageSize());
        if (isAll == 1) {
            // 用于甘特图于全部
            return processBusinessService.getBusinessTaskList(bpmProcessInstanceQueryReqVO, pageParam);
        } else if (isAll == 5) {
            // 抄送我的
            return processBusinessService.getBusinessTaskListCopy(bpmProcessInstanceQueryReqVO, pageParam);
        } else {
            // 待处理、已处理、我申请的
            return processBusinessService.getBusinessTaskListNotAll(bpmProcessInstanceQueryReqVO, pageParam);
        }
    }

    @Override
    public PageResult<BpmBusinessQueryDO> queryOABusinessTask(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {

        return processBusinessService.queryOABusinessTask(bpmProcessInstanceQueryReqVO);
    }

    @Override
    public List<BpmAppGetSumNumByDeptRespVO> getBusinessTaskStatisticsByDept(BpmProcessInstanceSumReqVO bpmProcessInstanceQueryReqVO) {
        BpmProcessInstanceQueryReqVO queryReqVO = new BpmProcessInstanceQueryReqVO();


        queryReqVO.setOaProjectId(bpmProcessInstanceQueryReqVO.getOaProjectId());


        return processBusinessService.getBusinessTaskStatisticsByDept(queryReqVO);
    }

    @Override
    public PageResult<BpmBusinessQueryDO> queryNotRelatedBusinessTask(BpmNotRelatedReqVO bpmNotRelatedReqVO) {

        return processBusinessService.queryNotRelatedBusinessTask(bpmNotRelatedReqVO);
    }

    @Override
    public void batchBpmProcessInstanceCopyPageFlag(List<BpmBusinessQueryDO> list) {
        Long id = SecurityFrameworkUtils.getLoginUserId();
        List<BpmBusinessCopyDO> copylist = BeanUtils.toBean(list, BpmBusinessCopyDO.class);
        if (CollectionUtil.isEmpty(copylist)) {
            return;
        }
        bpmBusinessCopyMapper.batchInsertCopyFlag(copylist, id);

    }

    @Override
    public List<Long> getApprovalPerson(Long loginUserId, BpmApprovalDetailReqVO reqVO) {
        BpmApprovalDetailRespVO approvalDetail = getApprovalDetail(loginUserId, reqVO);
        List<ActivityNode> activityNodes = approvalDetail.getActivityNodes();
        List<Long> assigneeIds = new ArrayList<>();
        if (activityNodes == null || activityNodes.isEmpty()) {
            return assigneeIds; // 空集合直接返回
        }

        for (ActivityNode node : activityNodes) {
            List<ActivityNodeTask> tasks = node.getTasks();
            if (tasks == null || tasks.isEmpty()) {
                continue; // 无任务则跳过当前节点
            }

            for (ActivityNodeTask task : tasks) {
                UserSimpleBaseVO assigneeUser = task.getAssigneeUser();
                if (assigneeUser == null) {
                    continue; // 无办理人则跳过当前任务
                }

                Long id = assigneeUser.getId();
                if (id == null) {
                    continue; // id 为空则跳过
                }

                try {
                    assigneeIds.add(id);
                } catch (NumberFormatException e) {

                }
            }
        }
        return assigneeIds;
    }

    @Override
    public PageResult<BpmBusinessGanttRespVO> queryGantt(BpmProcessInstanceQueryReqVO createReqVO) {

        PageParam pageParam = new PageParam();
        pageParam.setPageNo(createReqVO.getPageNo());
        pageParam.setPageSize(createReqVO.getPageSize());
        // 查询任务表 获取流程实例ID集合
        PageResult<BpmBusinessQueryDO> businessTaskList = processBusinessService.getBusinessTaskList(createReqVO,
            pageParam);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<BpmBusinessGanttRespVO> result = businessTaskList.getList().stream().map(queryDO -> {
            BpmBusinessGanttRespVO ganttVO = new BpmBusinessGanttRespVO();

            // 复制同名同类型字段（如 taskName、taskState 等）
            BeanUtils.copyProperties(queryDO, ganttVO);

            // 处理字段名不同的映射
            // 1. createTime（LocalDateTime）→ startDate（String）
            LocalDateTime createTime = queryDO.getCreateTime();
            if (createTime != null) {
                ganttVO.setStartDate(createTime.format(formatter));
            }

            // 2. taskCompletionTime（String）→ endDate（String）
            ganttVO.setEndDate(queryDO.getCompletionTime().format(formatter));

            ganttVO.setCreateTimeStr(queryDO.getCreateTime().format(dateFormatter));

            ganttVO.setCompletionTimeStr(queryDO.getCompletionTime().format(dateFormatter));

            ganttVO.setHandleUserName(queryDO.getExecutorUserName());

            ganttVO.setHandleUserId(queryDO.getExecutorUserId());

            ganttVO.setHandleDeptName(queryDO.getExecutorDeptName());

            ganttVO.setHandleDeptId(queryDO.getExecutorDeptId());

            // 处理类型不同的字段（如 priority：BpmBusinessDO 是 String，BpmBusinessGanttRespVO 是 Integer）
            if (queryDO.getPriority() != null) {
                ganttVO.setPriority(queryDO.getPriority());
            }
            ganttVO.setTaskId(queryDO.getTaskId() != null ? queryDO.getTaskId().toString() : null);
            ganttVO.setHandleDeptId(queryDO.getHandleDeptId() != null ? queryDO.getHandleDeptId().toString() : null);

            return ganttVO;
        }).toList();
        PageResult<BpmBusinessGanttRespVO> pageResult = new PageResult<>();
        pageResult.setList(result);
        pageResult.setTotal(businessTaskList.getTotal());

        return pageResult;
    }


    @Override
    public BpmAppGetSumNumRespVO getSumNum(BpmProcessInstanceQueryNumReqVO bpmProcessInstanceQueryReqVO) {

        BpmAppGetSumNumRespVO bpmAppGetSumNumRespVO = processBusinessService.getSumNum(bpmProcessInstanceQueryReqVO);

        return bpmAppGetSumNumRespVO;
    }

    @Override
    public BpmAppGetSumNumRespVO getBusinessTaskStatistics(BpmProcessInstanceSumReqVO bpmProcessInstanceQueryReqVO) {
        BpmProcessInstanceQueryReqVO queryReqVO = new BpmProcessInstanceQueryReqVO();


        queryReqVO.setOaProjectId(bpmProcessInstanceQueryReqVO.getOaProjectId());


        return processBusinessService.getBusinessTaskStatistics(queryReqVO);
    }


    /**
     * 判断当前任务节点是否是最后一个节点
     *
     * @param task 任务
     * @return true表示是最后一个节点，false表示不是
     */
    public boolean isLastNode(Task task) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        if (bpmnModel == null) {
            return false;
        }

        FlowNode currentNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        if (currentNode == null) {
            return false;
        }

        // 检查所有后续路径是否有审批任务
        return currentNode.getOutgoingFlows().stream()
            .map(flow -> bpmnModel.getFlowElement(flow.getTargetRef()))
            .noneMatch(target -> hasApprovalTask(target, bpmnModel));
    }

    private boolean hasApprovalTask(FlowElement element, BpmnModel bpmnModel) {
        if (element instanceof UserTask) {
            return !isCcTask((UserTask) element);
        }

        if (element instanceof FlowNode) {
            FlowNode node = (FlowNode) element;
            return node.getOutgoingFlows().stream()
                .map(flow -> bpmnModel.getFlowElement(flow.getTargetRef()))
                .anyMatch(target -> hasApprovalTask(target, bpmnModel));
        }

        return false;
    }

    private boolean isCcTask(UserTask userTask) {
        String name = userTask.getName();
        return name != null && (name.contains("抄送") || name.contains("CC") || name.contains("通知"));
    }

    /**
     * 判断当前任务节点是否是倒数第二个节点
     *
     * @param task 任务
     * @return true表示是倒数第二个节点，false表示不是
     */
    public boolean isSecondLastNode(Task task) {
        // 1. 获取当前任务

        // 2. 获取流程定义ID和当前节点ID
        String processDefinitionId = task.getProcessDefinitionId();
        String taskDefinitionKey = task.getTaskDefinitionKey();

        // 3. 获取Bpmn模型
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        if (bpmnModel == null) {
            return false;
        }

        // 4. 获取当前流程节点
        FlowNode currentNode = (FlowNode) bpmnModel.getFlowElement(taskDefinitionKey);
        if (currentNode == null) {
            return false;
        }

        // 5. 检查所有出线的目标节点
        List<SequenceFlow> outgoingFlows = currentNode.getOutgoingFlows();
        if (outgoingFlows.isEmpty()) {
            // 没有出线，不是倒数第二个
            return false;
        }

        // 6. 检查所有后续节点是否都是结束事件
        for (SequenceFlow sequenceFlow : outgoingFlows) {
            FlowElement targetElement = bpmnModel.getFlowElement(sequenceFlow.getTargetRef());

            // 如果目标节点不是结束事件，检查目标节点的出线
            if (!(targetElement instanceof EndEvent)) {
                FlowNode targetNode = (FlowNode) targetElement;
                List<SequenceFlow> targetOutgoingFlows = targetNode.getOutgoingFlows();

                // 如果目标节点的所有出线都指向结束事件，则当前节点是倒数第二个
                boolean allTargetsAreEndEvents = true;
                for (SequenceFlow targetOutgoingFlow : targetOutgoingFlows) {
                    FlowElement nextTargetElement = bpmnModel.getFlowElement(targetOutgoingFlow.getTargetRef());
                    if (!(nextTargetElement instanceof EndEvent)) {
                        allTargetsAreEndEvents = false;
                        break;
                    }
                }

                if (allTargetsAreEndEvents) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 循环childNode字段获取approveMethod
     */
    public Integer getApproveMethod(String activityNodeId, Object simpleModel) {
        try {
            String jsonString;
            if (simpleModel instanceof String) {
                jsonString = (String) simpleModel;
            } else {
                jsonString = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(simpleModel);
            }

            // 解析为Map
            Map<String, Object> map = new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(jsonString, Map.class);

            // 循环查找
            return findApproveMethodInChildNodes(map, activityNodeId);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 递归循环childNode查找approveMethod
     */
    private Integer findApproveMethodInChildNodes(Map<String, Object> currentNode, String targetId) {
        // 检查当前节点
        if (targetId.equals(currentNode.get("id"))) {
            return (Integer) currentNode.get("approveMethod");
        }

        // 循环childNode
        if (currentNode.containsKey("childNode") && currentNode.get("childNode") != null) {
            Map<String, Object> childNode = (Map<String, Object>) currentNode.get("childNode");
            Integer result = findApproveMethodInChildNodes(childNode, targetId);
            if (result != null) {
                return result;
            }
        }

        return null;
    }




}
