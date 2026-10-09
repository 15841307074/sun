package com.htyoudao.youdao.module.bpm.service.definition;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.*;
import com.htyoudao.youdao.module.bpm.controller.app.task.VO.getSumBydept.BpmAppGetSumNumByDeptRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.task.VO.getSumBydept.BpmBusinessQueryDTO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.*;
import com.htyoudao.youdao.module.bpm.dal.mysql.definition.*;
import com.htyoudao.youdao.module.bpm.service.task.BpmProcessInstanceCopyService;
import com.htyoudao.youdao.module.bpm.service.task.BpmProcessInstanceService;
import com.htyoudao.youdao.module.bpm.service.task.BpmTaskService;
import com.htyoudao.youdao.module.system.api.dept.DeptApi;
import com.htyoudao.youdao.module.system.api.dept.DeptOrgApi;
import com.htyoudao.youdao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptDTO;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.bpm.enums.BpmConstants.BPM_OA_BUSINESS_STORE_ID;
import static com.htyoudao.youdao.module.bpm.enums.ErrorCodeConstants.USER_DEPT_ERROR;


/**
 * 流程定义实现
 * 主要进行 Flowable {@link ProcessDefinition} 和 {@link Deployment} 的维护
 *
 * @author yunlongn
 * @author ZJQ
 * @author 0090
 */
@Service
@Validated
@Slf4j
public class BpmProcessBusinessServiceImpl implements BpmProcessBusinessService {

    @Resource
    private BpmProcessBusinessMapper processBusinessMapper;

    @Resource
    private BpmProcessBusinessQueryMapper processBusinessQueryMapper;

    @Resource
    private BpmBusinessCopyMapper bpmBusinessCopyMapper;

    @Resource
    private BussinessTaskStoreMapper storeInfoMapper;

    @Resource
    private BpmAllStoreInfoMapper allStoreInfoMapper;

    @Resource
    private BpmUserMapper userMapper;

    @DubboReference
    private DeptOrgApi deptOrgApi;

    @Resource
    private BpmTaskService taskService;

    @Resource
    @Lazy
    private BpmProcessInstanceService processInstanceService;

    @Resource
    @Lazy
    private BpmProcessInstanceCopyService processInstanceCopyService;

    @DubboReference
    private DeptApi deptApi;

    @Override
    public void createProcessBusiness(BpmBusinessDO bpmBusinessDO) {
        processBusinessMapper.insert(bpmBusinessDO);
    }

    @Override
    public void createProcessBusinessBatch(List<BpmBusinessDO> list) {
        processBusinessMapper.insertBatch(list);
    }

    @Override
    public void createProcessStoreInfo(List<BussinessTaskStoreDO> bpmStoreInfoDOS) {
        storeInfoMapper.insertBatch(bpmStoreInfoDOS);
    }

    @Override
    public BpmBusinessDO getBusinessTask(String procInstId) {
        LambdaQueryWrapper<BpmBusinessDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BpmBusinessDO::getProcInstId, procInstId);
        return processBusinessMapper.selectOne(wrapper);
    }

    @Override
    public List<BussinessTaskStoreDO> getStoreList(String procInstId, Long businessId) {
        LambdaQueryWrapper<BussinessTaskStoreDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BussinessTaskStoreDO::getProcInstId, procInstId);
        wrapper.eq(BussinessTaskStoreDO::getBusinessId, businessId);
        return storeInfoMapper.selectList(wrapper);
    }

    @Override
    public List<BpmAllStoreInfoDO> getAllStoreList(Long businessId) {
        LambdaQueryWrapper<BpmAllStoreInfoDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(BpmAllStoreInfoDO::getUserId);
        wrapper.eq(BpmAllStoreInfoDO::getBusinessId, businessId);

        //过滤闭店
        wrapper.eq(BpmAllStoreInfoDO::getStoreStatus, 0);

        // 全部门店
        return allStoreInfoMapper.selectList(wrapper);
    }

    @Override
    public void updateProcInstId(Long taskId, String bProcessInstanceId) {
        LambdaUpdateWrapper<BpmBusinessDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(BpmBusinessDO::getProcInstId, bProcessInstanceId);
        wrapper.eq(BpmBusinessDO::getTaskId, taskId);
        processBusinessMapper.update(wrapper);
    }

    @Override
    public PageResult<BpmBusinessQueryDO> getBusinessTaskList(BpmProcessInstanceQueryReqVO reqVO, PageParam pageParam) {

        PageResult<BpmBusinessQueryDO> result = new PageResult<>();
        // 数据可见范围 当前登录者能查看哪些流程
        CommonResult<List<Long>> commonResult = deptOrgApi.getUserIdsByDept();
        // 获取所有可见的userId
        List<Long> userIds = commonResult.getCheckedData();
        if (CollectionUtil.isEmpty(userIds)) {
            throw exception(USER_DEPT_ERROR);
        }

        List<String> userIdList = userIds.stream()
                // 将每个 Long 元素转为 String（使用 String.valueOf() 或 Long::toString）
                .map(String::valueOf) // 或 .map(l -> l.toString())
                // 收集为 List<String>
                .toList();
        List<String> proInstIdList = new ArrayList<>();
        extractedAllList(userIdList, proInstIdList, reqVO.getOaProjectFlag(), reqVO.getOaProjectId());
        if (CollectionUtil.isEmpty(proInstIdList)) {
            return PageResult.empty();
        }

        // 统计 涉及到执行人 执行部门 执行门店流程ID
        boolean childFlag = false;
        boolean parentFlag = false;
        int executorCount = 0;
        if(CollectionUtil.isNotEmpty(reqVO.getExecutorStoreId()) || CollectionUtil.isNotEmpty(reqVO.getExecutorUserId())){
            childFlag = true;
            List<String> procInstIdsChild = getProcInstIdsByTaskStore(reqVO);
            if (CollectionUtil.isNotEmpty(procInstIdsChild)){
                proInstIdList.retainAll(procInstIdsChild);
                executorCount++;
            }

        }
        if(reqVO.getExecutorDeptId() != null || CollectionUtil.isNotEmpty(reqVO.getExecutorUserId())){
            parentFlag = true;
            List<String> procInstIdsParent = getProcInstIdsByTask(reqVO);
            if (CollectionUtil.isNotEmpty(procInstIdsParent)){
                proInstIdList.retainAll(procInstIdsParent);
                executorCount++;
            }

        }

        if((childFlag || parentFlag) && executorCount == 0){
            return PageResult.empty();
        }

        if(CollectionUtil.isNotEmpty(proInstIdList)){

            reqVO.setProInstIdList(proInstIdList);
            List<BpmBusinessQueryDO> bpmBusinessQueryDOS = processBusinessQueryMapper.selectMainTaskListByPage(reqVO, pageParam);
            Long total = processBusinessQueryMapper.selectMainTaskListTotal(reqVO, pageParam);
            result.setList(bpmBusinessQueryDOS);
            result.setTotal(total);
            return result;
        }


        return PageResult.empty();
    }

    @Override
    public PageResult<BpmBusinessQueryDO> queryOABusinessTask(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {

        PageResult<BpmBusinessQueryDO> result = new PageResult<>();



        PageParam pageParam = new PageParam();
        pageParam.setPageNo(bpmProcessInstanceQueryReqVO.getPageNo());
        pageParam.setPageSize(bpmProcessInstanceQueryReqVO.getPageSize());

        boolean childFlag = false;
        boolean parentFlag = false;

        List<String> proInstIdList = new ArrayList<>();
        if(CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorStoreId()) || CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorUserId())){
            List<String> procInstIdsChild = getProcInstIdsByTaskStore(bpmProcessInstanceQueryReqVO);
            if (CollectionUtil.isNotEmpty(procInstIdsChild)) {
                proInstIdList.addAll(procInstIdsChild);
            }
            childFlag = true;

        }

        if(bpmProcessInstanceQueryReqVO.getExecutorDeptId() != null || CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorUserId())){
            List<String> procInstIdsParent = getProcInstIdsByTask(bpmProcessInstanceQueryReqVO);
            if (CollectionUtil.isNotEmpty(procInstIdsParent)) {
                if (CollectionUtil.isNotEmpty(proInstIdList)) {
                    proInstIdList.retainAll(procInstIdsParent);
                }else {
                    proInstIdList.addAll(procInstIdsParent);
                }
            }
            parentFlag = true;

        }

        if((childFlag || parentFlag) && CollectionUtils.isEmpty(proInstIdList)){
            return PageResult.empty();
        }

        bpmProcessInstanceQueryReqVO.setProInstIdList(proInstIdList);
        List<BpmBusinessQueryDO> bpmBusinessQueryDOS = processBusinessQueryMapper.selectMainTaskListByPage(bpmProcessInstanceQueryReqVO, pageParam);
        Long total = processBusinessQueryMapper.selectMainTaskListTotal(bpmProcessInstanceQueryReqVO, pageParam);
        result.setList(bpmBusinessQueryDOS);
        result.setTotal(total);
        return result;




    }

    @Override
    public PageResult<BpmBusinessQueryDO> queryNotRelatedBusinessTask(BpmNotRelatedReqVO bpmNotRelatedReqVO) {

        PageParam pageParam = new PageParam();
        pageParam.setPageNo(bpmNotRelatedReqVO.getPageNo());
        pageParam.setPageSize(bpmNotRelatedReqVO.getPageSize());


        LambdaQueryWrapper<BpmBusinessQueryDO> wrapper = new LambdaQueryWrapper<>();

        if (StringUtil.isNotBlank(bpmNotRelatedReqVO.getTaskContent())) {
            wrapper.and(orWrapper -> orWrapper
                    .like(BpmBusinessQueryDO::getTaskName, bpmNotRelatedReqVO.getTaskContent())
                    .or()
                    .like(BpmBusinessQueryDO::getTaskDesc, bpmNotRelatedReqVO.getTaskContent())
            );
        }

        if (StringUtil.isNotBlank(bpmNotRelatedReqVO.getProcessDefinitionId())) {
            wrapper.eq(BpmBusinessQueryDO::getFlowId, bpmNotRelatedReqVO.getProcessDefinitionId());
        }
        if (!Objects.isNull(bpmNotRelatedReqVO.getApplicantId())) {
            wrapper.eq(BpmBusinessQueryDO::getApplicantId, bpmNotRelatedReqVO.getApplicantId());
        }
        wrapper.isNull(BpmBusinessQueryDO::getParentProcInstId);
        wrapper.isNull(BpmBusinessQueryDO::getOaProjectId);

        return processBusinessQueryMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public void extractedAllList(List<String> userIdList, List<String> proInstIdList, Integer oaProjectFlag, Long oaProjectId) {

        List<String> pIds = processInstanceService.getProcessInstanceIdsByOaProjectId(oaProjectId, oaProjectFlag);


        Set<String> set = new HashSet<>();
        // 统计所有代办
        List<String> todoResult = todoAll(userIdList, pIds);
        if (CollectionUtil.isNotEmpty(todoResult)) {
            set.addAll(todoResult);
        }
        // 统计所有已办
        List<String> doneResult = doneAll(userIdList, pIds);
        if (CollectionUtil.isNotEmpty(doneResult)) {
            set.addAll(doneResult);
        }

        // 统计所有我的
        List<String> myResult = myAll(userIdList, pIds);
        if (CollectionUtil.isNotEmpty(myResult)) {
            set.addAll(myResult);
        }
        // 全部暂时不统计抄送? 又包含了
        // 统计所有抄送
        List<String> copyResult = copyAll(userIdList, pIds);
        if (CollectionUtil.isNotEmpty(copyResult)) {
            set.addAll(copyResult);
        }

        if (CollectionUtil.isNotEmpty(set)) {
            proInstIdList.addAll(set);
        }
    }

    private List<String> copyAll(List<String> userIdList, List<String> pIds) {
        return processInstanceCopyService.getBpmProcessInstanceCopyList(userIdList, pIds);
    }

    private List<String> myAll(List<String> userIdList, List<String> pIds) {

        LambdaQueryWrapper<BpmBusinessDO> wrapper = new LambdaQueryWrapper<>();

        List<String> procList = new ArrayList<>();
        ResultHandler<String> resultHandler = new ResultHandler<String>() {
            @Override
            public void handleResult (ResultContext<? extends String> resultContext) {
                // 获取当前行的结果（即 memberId）
                String procId = resultContext.getResultObject();
                // 过滤 null 值并添加到集合
                if (Objects.nonNull(procId)) {
                    procList.add(procId);
                }
            }
        };

        wrapper.select(BpmBusinessDO::getProcInstId);
        wrapper.in(BpmBusinessDO::getApplicantId, userIdList);

        if (CollectionUtil.isNotEmpty(pIds)){
            wrapper.in(BpmBusinessDO::getProcInstId, pIds);
        }

        wrapper.isNull(BpmBusinessDO::getParentProcInstId);
        processBusinessMapper.selectObjs(wrapper, resultHandler);

        return procList;
    }

    private List<String> doneAll(List<String> userIds, List<String> pIds) {
        return taskService.getBpmTaskDoneList(userIds, pIds);
    }

    private List<String> todoAll(List<String> userIds, List<String> pIds) {
        return taskService.getBpmTaskTodoList(userIds, pIds);
    }

    @Override
    public PageResult<BpmBusinessQueryDO> getBusinessTaskListNotAll(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO, PageParam pageParam) {
        //
        /*Long loginUserId = WebFrameworkUtils.getLoginUserId();
        if (!Objects.equals(loginUserId, bpmProcessInstanceQueryReqVO.getApplicantId())) {
            return PageResult.empty();
        }*/

        // 统计 涉及到执行人 执行部门 执行门店流程ID
        List<String> IdList = bpmProcessInstanceQueryReqVO.getProInstIdList();
        if (CollectionUtil.isEmpty(IdList)) {
            return PageResult.empty();
        }
        List<String> proInstIdList = new ArrayList<>();
        proInstIdList.addAll(IdList);
        boolean childFlag = false;
        boolean parentFlag = false;
        int executorCount = 0;
        PageResult<BpmBusinessQueryDO> result = new PageResult<>();
        if(CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorStoreId()) || CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorUserId())){
            childFlag = true;
            List<String> procInstIdsChild = getProcInstIdsByTaskStore(bpmProcessInstanceQueryReqVO);
            if (CollectionUtil.isNotEmpty(procInstIdsChild)){
                proInstIdList.retainAll(procInstIdsChild);
                executorCount++;
            }

        }
        if(bpmProcessInstanceQueryReqVO.getExecutorDeptId() != null || CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorUserId())){
            parentFlag = true;
            List<String> procInstIdsParent = getProcInstIdsByTask(bpmProcessInstanceQueryReqVO);
            if (CollectionUtil.isNotEmpty(procInstIdsParent)){
                proInstIdList.retainAll(procInstIdsParent);
                executorCount++;
            }

        }

        if ((childFlag || parentFlag) && executorCount == 0){
            return PageResult.empty();
        }

        if(CollectionUtil.isNotEmpty(proInstIdList)){

            bpmProcessInstanceQueryReqVO.setProInstIdList(proInstIdList);
            List<BpmBusinessQueryDO> bpmBusinessQueryDOS = processBusinessQueryMapper.selectTaskListByPage(bpmProcessInstanceQueryReqVO, pageParam);
            Long total = processBusinessQueryMapper.selectTaskListTotal(bpmProcessInstanceQueryReqVO, pageParam);
            result.setList(bpmBusinessQueryDOS);
            result.setTotal(total);
            return result;
        }

        return PageResult.empty();
    }


    @Override
    public Object getBusinessTaskListCountNotAll(BpmProcessInstanceQueryTaskCountReqVO bpmProcessInstanceQueryReqVO, PageParam pageParam) {
        // 统计 涉及到执行人 执行部门 执行门店流程ID
        List<String> IdList = bpmProcessInstanceQueryReqVO.getProInstIdList();
        List<String> proInstIdList = new ArrayList<>();
        proInstIdList.addAll(IdList);
        boolean childFlag = false;
        boolean parentFlag = false;
        int executorCount = 0;
        PageResult<BpmBusinessQueryDO> result = new PageResult<>();
        BpmProcessInstanceQueryReqVO bean = BeanUtil.toBean(bpmProcessInstanceQueryReqVO, BpmProcessInstanceQueryReqVO.class);
        if( CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorStoreId()) || CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorUserId())){
            childFlag = true;
            List<String> procInstIdsChild = getProcInstIdsByTaskStore(bean);
            if (CollectionUtil.isNotEmpty(procInstIdsChild)){
                proInstIdList.retainAll(procInstIdsChild);
                executorCount++;
            }

        }
        if(bpmProcessInstanceQueryReqVO.getExecutorDeptId() != null || CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorUserId())){
            parentFlag = true;
            List<String> procInstIdsParent = getProcInstIdsByTask(bean);
            if (CollectionUtil.isNotEmpty(procInstIdsParent)){
                proInstIdList.retainAll(procInstIdsParent);
                executorCount++;
            }

        }

        if((childFlag || parentFlag) && executorCount == 0){
            return 0L;
        }

        if(CollectionUtil.isNotEmpty(proInstIdList)){

            bpmProcessInstanceQueryReqVO.setProInstIdList(proInstIdList);
            return processBusinessQueryMapper.selectTaskListTotal(bean, pageParam);
        }

        return 0L;
    }

    @Override
    public Object getBusinessTaskListCountCopy(BpmProcessInstanceQueryTaskCountReqVO bpmProcessInstanceQueryReqVO, PageParam pageParam) {
        // 统计 涉及到执行人 执行部门 执行门店流程ID
        List<String> IdList = bpmProcessInstanceQueryReqVO.getProInstIdList();
        List<String> proInstIdList = new ArrayList<>();
        proInstIdList.addAll(IdList);
        boolean childFlag = false;
        boolean parentFlag = false;
        int executorCount = 0;
        PageResult<BpmBusinessQueryDO> result = new PageResult<>();
        BpmProcessInstanceQueryReqVO bean = BeanUtil.toBean(bpmProcessInstanceQueryReqVO, BpmProcessInstanceQueryReqVO.class);
        if( CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorStoreId()) || CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorUserId())){
            childFlag = true;
            List<String> procInstIdsChild = getProcInstIdsByTaskStore(bean);
            if (CollectionUtil.isNotEmpty(procInstIdsChild)){
                proInstIdList.retainAll(procInstIdsChild);
                executorCount++;
            }

        }
        if(bpmProcessInstanceQueryReqVO.getExecutorDeptId() != null || CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorUserId())){
            parentFlag = true;
            List<String> procInstIdsParent = getProcInstIdsByTask(bean);
            if (CollectionUtil.isNotEmpty(procInstIdsParent)){
                proInstIdList.retainAll(procInstIdsParent);
                executorCount++;
            }

        }

        if((childFlag || parentFlag) && executorCount == 0){
            return 0L;
        }

        if(CollectionUtil.isNotEmpty(proInstIdList)){

            bpmProcessInstanceQueryReqVO.setProInstIdList(proInstIdList);
            // 抄送我 总数
            Long sum = processBusinessQueryMapper.selectTaskListTotal(bean, pageParam);
            // 获取我已读的
            Long read_sum = processBusinessQueryMapper.selectTaskListCopyReadTotal(bean, SecurityFrameworkUtils.getLoginUserId());

            if (sum != null) {
                if (read_sum == null) {
                    return sum;
                }else {
                    return sum - read_sum;
                }
            }
        }

        return 0L;
    }

    private List<String> getProcInstIdsByTask(BpmProcessInstanceQueryReqVO queryReqVO) {
        LambdaQueryWrapper<BpmBusinessDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(BpmBusinessDO::getProcInstId);
        if (CollectionUtil.isNotEmpty(queryReqVO.getExecutorUserId())) {
            wrapper.in(BpmBusinessDO::getExecutorUserId, queryReqVO.getExecutorUserId());
        }
        if (queryReqVO.getExecutorDeptId() != null) {
            wrapper.eq(BpmBusinessDO::getExecutorDeptId, queryReqVO.getExecutorDeptId());
        }

        // 2. 创建自定义 ResultHandler 收集结果
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
        processBusinessMapper.selectObjs(wrapper, resultHandler);
        return proInstIdList;
    }

    private List<String> getProcInstIdsByTaskStore(BpmProcessInstanceQueryReqVO queryReqVO) {
        LambdaQueryWrapper<BussinessTaskStoreDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(BussinessTaskStoreDO::getProcInstId);
        if (CollectionUtil.isNotEmpty(queryReqVO.getExecutorStoreId())) {
            wrapper.in(BussinessTaskStoreDO::getExecutorStoreId, queryReqVO.getExecutorStoreId());
        }
        if (CollectionUtil.isNotEmpty(queryReqVO.getExecutorUserId())) {
            wrapper.in(BussinessTaskStoreDO::getUserId, queryReqVO.getExecutorUserId());
        }


        // 2. 创建自定义 ResultHandler 收集结果
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
        storeInfoMapper.selectObjs(wrapper, resultHandler);
        return proInstIdList;
    }

    @Override
    public BpmAppGetSumNumRespVO getSumNum(BpmProcessInstanceQueryNumReqVO bpmProcessInstanceQueryReqVO) {
        BpmAppGetSumNumRespVO result = new BpmAppGetSumNumRespVO();
        
        // 1. 处理时间参数，如果为空则设置为当前年的起始和结束时间
        bpmProcessInstanceQueryReqVO.processTimeParams();
        
        // 2. 设置数据可见范围（申请人权限）
        setApplicantPermissionScope(bpmProcessInstanceQueryReqVO);
        
        // 3. 获取符合条件的流程实例ID列表
        List<String> processInstanceIds = getProcessInstanceIdsByExecutorConditions(bpmProcessInstanceQueryReqVO);
        
        // 4. 设置流程ID列表并查询任务数据
        if (!CollectionUtil.isEmpty(processInstanceIds)) {
            if (!CollectionUtil.isEmpty(bpmProcessInstanceQueryReqVO.getProInstIdList())) {
                // processInstanceIds和bpmProcessInstanceQueryReqVO.getProInstIdList() 做交集
                List<String> intersection = processInstanceIds.stream()
                        .filter(bpmProcessInstanceQueryReqVO.getProInstIdList()::contains)
                        .collect(Collectors.toList());
                bpmProcessInstanceQueryReqVO.setProInstIdList(intersection);
            } else {
                bpmProcessInstanceQueryReqVO.setProInstIdList(processInstanceIds);
            }

        }else {
            // 检查是否有执行人相关条件
            boolean hasStoreCondition = CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorStoreId());
            boolean hasDeptCondition = bpmProcessInstanceQueryReqVO.getExecutorDeptId() != null;
            boolean hasUserCondition = CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorUserId());

            if (hasStoreCondition || hasDeptCondition || hasUserCondition) {
                return result;
            }
        }
        
        // 5. 使用 MyBatis Plus 查询任务列表
        LocalDateTime createTimeStart = bpmProcessInstanceQueryReqVO.getCreateTimeStart().toLocalDateTime();
        LocalDateTime createTimeEnd = bpmProcessInstanceQueryReqVO.getCreateTimeEnd().toLocalDateTime();
        
        LambdaQueryWrapperX<BpmBusinessQueryDO> wrapper = new LambdaQueryWrapperX<BpmBusinessQueryDO>();
        List<Integer> notIn = new ArrayList<>();
        //notIn.add(BpmProcessInstanceStatusEnum.REJECT.getStatus());
        notIn.add(BpmProcessInstanceStatusEnum.CANCEL.getStatus());
        wrapper.notIn(BpmBusinessQueryDO::getTaskState, notIn);

        wrapper.isNull(BpmBusinessQueryDO::getParentProcInstId);
        //发起人
        wrapper.eqIfPresent(BpmBusinessQueryDO::getApplicantId, bpmProcessInstanceQueryReqVO.getApplicantId())
                .eqIfPresent(BpmBusinessQueryDO::getOaProjectId, bpmProcessInstanceQueryReqVO.getOaProjectId())
                //发起部门
                .inIfPresent(BpmBusinessQueryDO::getDeptId, bpmProcessInstanceQueryReqVO.getApplicantDeptIdList())
                //发起门店
                .eqIfPresent(BpmBusinessQueryDO::getStoreId, bpmProcessInstanceQueryReqVO.getApplicantStoreId())
                //流程模板
                .eqIfPresent(BpmBusinessQueryDO::getFlowId, bpmProcessInstanceQueryReqVO.getProcessDefinitionId())
                //任务类型
                .eqIfPresent(BpmBusinessQueryDO::getTaskType, bpmProcessInstanceQueryReqVO.getTaskType())
                //时间范围
                .between(BpmBusinessQueryDO::getCreateTime, createTimeStart, createTimeEnd)
                ;


        // 添加 proc_inst_id IN 条件（如果值存在）
        if (CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getProInstIdList())) {
            wrapper.in(BpmBusinessQueryDO::getProcInstId, bpmProcessInstanceQueryReqVO.getProInstIdList());
        }else {
            //如果没有说明，没有可查数据
            return result;
        }
        
        List<BpmBusinessQueryDO> taskList = processBusinessQueryMapper.selectList(wrapper);
        
        // 6. 统计任务状态并计算完成率
        if (CollectionUtil.isNotEmpty(taskList)) {
            calculateTaskStatistics(result, taskList);
        }
        
        return result;
    }

    @Override
    public PageResult<BpmBusinessQueryDO> getBusinessTaskListCopy(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO, PageParam pageParam) {
        // 统计 涉及到执行人 执行部门 执行门店流程ID
        List<String> IdList = bpmProcessInstanceQueryReqVO.getProInstIdList();

        if (CollectionUtil.isEmpty(IdList)) {
            return PageResult.empty();
        }

        List<String> proInstIdList = new ArrayList<>();
        proInstIdList.addAll(IdList);
        boolean childFlag = false;
        boolean parentFlag = false;
        int executorCount = 0;
        PageResult<BpmBusinessQueryDO> result = new PageResult<>();
        if(CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorStoreId()) || CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorUserId())){
            childFlag = true;
            List<String> procInstIdsChild = getProcInstIdsByTaskStore(bpmProcessInstanceQueryReqVO);
            if (CollectionUtil.isNotEmpty(procInstIdsChild)){
                proInstIdList.retainAll(procInstIdsChild);
                executorCount++;
            }

        }
        if(bpmProcessInstanceQueryReqVO.getExecutorDeptId() != null || CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorUserId())){
            parentFlag = true;
            List<String> procInstIdsParent = getProcInstIdsByTask(bpmProcessInstanceQueryReqVO);
            if (CollectionUtil.isNotEmpty(procInstIdsParent)){
                proInstIdList.retainAll(procInstIdsParent);
                executorCount++;
            }

        }

        if((childFlag || parentFlag) && executorCount == 0){
            return PageResult.empty();
        }

        if(CollectionUtil.isNotEmpty(proInstIdList)){

            bpmProcessInstanceQueryReqVO.setProInstIdList(proInstIdList);
            Long loginUserId = SecurityFrameworkUtils.getLoginUserId();

            if (bpmProcessInstanceQueryReqVO.getCopyFlag() != null &&
                    bpmProcessInstanceQueryReqVO.getCopyFlag() == 0) {
                // 获取本人抄送未读
                // 总流程ID - 抄送已读的流程ID
                List<String> procList = new ArrayList<>();
                ResultHandler<String> resultHandler = new ResultHandler<String>() {
                    @Override
                    public void handleResult (ResultContext<? extends String> resultContext) {
                        // 获取当前行的结果（即 memberId）
                        String procId = resultContext.getResultObject();
                        // 过滤 null 值并添加到集合
                        if (Objects.nonNull(procId)) {
                            procList.add(procId);
                        }
                    }
                };
                LambdaQueryWrapper<BpmBusinessCopyDO> copyWrapper = new LambdaQueryWrapper<>();
                copyWrapper.select(BpmBusinessCopyDO::getProcInstId);
                copyWrapper.eq(BpmBusinessCopyDO::getCopyUserId, loginUserId);
                copyWrapper.eq(BpmBusinessCopyDO::getCopyFlag, 1);
                copyWrapper.in(BpmBusinessCopyDO::getProcInstId, bpmProcessInstanceQueryReqVO.getProInstIdList());
                bpmBusinessCopyMapper.selectObjs(copyWrapper, resultHandler);

                if (CollectionUtil.isNotEmpty(procList)) {
                    proInstIdList.removeAll(procList);
                    bpmProcessInstanceQueryReqVO.setProInstIdList(proInstIdList);
                }

            }
            if (CollectionUtil.isNotEmpty(proInstIdList)) {
                List<BpmBusinessQueryDO> bpmBusinessQueryDOS = processBusinessQueryMapper.selectTaskListCopyByPage(bpmProcessInstanceQueryReqVO, pageParam, loginUserId);

                Long total = processBusinessQueryMapper.selectTaskListCopyByPageTotal(bpmProcessInstanceQueryReqVO, pageParam, loginUserId);
                result.setList(bpmBusinessQueryDOS);
                result.setTotal(total);
                return result;
            }

        }

        result.setTotal(0L);
        result.setList(new ArrayList<>());
        return result;
    }

    /**
     * 为 getSumNum 方法专门创建的查询方法
     */
    private List<String> getProcInstIdsByTaskForNum(BpmProcessInstanceQueryNumReqVO queryReqVO) {
        LambdaQueryWrapper<BpmBusinessDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(BpmBusinessDO::getProcInstId);
        if (CollectionUtil.isNotEmpty(queryReqVO.getExecutorUserId())) {
            wrapper.in(BpmBusinessDO::getExecutorUserId, queryReqVO.getExecutorUserId());
        }
        if (queryReqVO.getExecutorDeptId() != null) {
            wrapper.eq(BpmBusinessDO::getExecutorDeptId, queryReqVO.getExecutorDeptId());
        }
        if (queryReqVO.getOaProjectId() != null) {
            wrapper.eq(BpmBusinessDO::getOaProjectId, queryReqVO.getOaProjectId());
        }

        // 2. 创建自定义 ResultHandler 收集结果
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
        processBusinessMapper.selectObjs(wrapper, resultHandler);
        return proInstIdList;
    }

    /**
     * 为 getSumNum 方法专门创建的查询方法
     */
    private List<String> getProcInstIdsByTaskStoreForNum(BpmProcessInstanceQueryNumReqVO queryReqVO) {
        LambdaQueryWrapper<BussinessTaskStoreDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(BussinessTaskStoreDO::getProcInstId);
        if (CollectionUtil.isNotEmpty(queryReqVO.getExecutorStoreId())) {
            wrapper.in(BussinessTaskStoreDO::getExecutorStoreId, queryReqVO.getExecutorStoreId());
        }
        if (CollectionUtil.isNotEmpty(queryReqVO.getExecutorUserId())) {
            wrapper.in(BussinessTaskStoreDO::getUserId, queryReqVO.getExecutorUserId());
        }

        // 2. 创建自定义 ResultHandler 收集结果
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
        storeInfoMapper.selectObjs(wrapper, resultHandler);
        return proInstIdList;
    }

    /**
     * 设置申请人权限范围
     * 根据当前登录用户的部门权限，设置可查看的申请人ID列表
     */
    private void setApplicantPermissionScope(BpmProcessInstanceQueryNumReqVO queryReqVO) {
        CommonResult<List<Long>> commonResult = deptOrgApi.getUserIdsByDept();
      /*  if (commonResult.getCheckedData() != null) {
            queryReqVO.setApplicantIdList(commonResult.getCheckedData());
        }
*/


        List<String> userIdList = commonResult.getCheckedData().stream()
                // 将每个 Long 元素转为 String（使用 String.valueOf() 或 Long::toString）
                .map(String::valueOf) // 或 .map(l -> l.toString())
                // 收集为 List<String>
                .toList();
        List<String> proInstIdList = new ArrayList<>();
        extractedAllList(userIdList, proInstIdList, queryReqVO.getOaProjectFlag(), queryReqVO.getOaProjectId());
        if(CollectionUtil.isNotEmpty(proInstIdList)) {
            queryReqVO.setProInstIdList(proInstIdList);
        }



    }

    /**
     * 根据执行人条件获取流程实例ID列表
     * 支持门店执行人、部门执行人、用户执行人三种条件
     */
    private List<String> getProcessInstanceIdsByExecutorConditions(BpmProcessInstanceQueryNumReqVO queryReqVO) {
        List<String> processInstanceIds = new ArrayList<>();
        
        // 检查是否有执行人相关条件
        boolean hasStoreCondition = CollectionUtil.isNotEmpty(queryReqVO.getExecutorStoreId());
        boolean hasDeptCondition = queryReqVO.getExecutorDeptId() != null;
        boolean hasUserCondition = CollectionUtil.isNotEmpty(queryReqVO.getExecutorUserId());
        
        if (!hasStoreCondition && !hasDeptCondition && !hasUserCondition) {
            // 没有执行人条件，返回空列表（表示查询所有）
            return processInstanceIds;
        }
        
        // 智能查询策略：根据条件组合选择最优查询方式
        if (hasStoreCondition && hasDeptCondition && hasUserCondition) {
            // 情况1：三种条件都有 → 分别查询门店和部门，用户条件会被包含在两次查询中，但通过去重处理
            List<String> storeProcessIds = getProcInstIdsByTaskStoreForNum(queryReqVO);
            if (CollectionUtil.isNotEmpty(storeProcessIds)) {
                processInstanceIds.addAll(storeProcessIds);
            }
            
            List<String> deptProcessIds = getProcInstIdsByTaskForNum(queryReqVO);
            if (CollectionUtil.isNotEmpty(deptProcessIds)) {
                processInstanceIds.addAll(deptProcessIds);
            }
        } else if (hasStoreCondition && hasDeptCondition) {
            // 情况2：只有门店和部门条件 → 分别查询
            List<String> storeProcessIds = getProcInstIdsByTaskStoreForNum(queryReqVO);
            if (CollectionUtil.isNotEmpty(storeProcessIds)) {
                processInstanceIds.addAll(storeProcessIds);
            }
            
            List<String> deptProcessIds = getProcInstIdsByTaskForNum(queryReqVO);
            if (CollectionUtil.isNotEmpty(deptProcessIds)) {
                processInstanceIds.addAll(deptProcessIds);
            }
        } else if (hasStoreCondition && hasUserCondition) {
            // 情况3：只有门店和用户条件 → 只查询门店表（包含用户条件）
            List<String> storeProcessIds = getProcInstIdsByTaskStoreForNum(queryReqVO);
            if (CollectionUtil.isNotEmpty(storeProcessIds)) {
                processInstanceIds.addAll(storeProcessIds);
            }
        } else if (hasDeptCondition && hasUserCondition) {
            // 情况4：只有部门和用户条件 → 只查询部门表（包含用户条件）
            List<String> deptProcessIds = getProcInstIdsByTaskForNum(queryReqVO);
            if (CollectionUtil.isNotEmpty(deptProcessIds)) {
                processInstanceIds.addAll(deptProcessIds);
            }
        } else if (hasStoreCondition) {
            // 情况5：只有门店条件 → 只查询门店表
            List<String> storeProcessIds = getProcInstIdsByTaskStoreForNum(queryReqVO);
            if (CollectionUtil.isNotEmpty(storeProcessIds)) {
                processInstanceIds.addAll(storeProcessIds);
            }
        } else if (hasDeptCondition) {
            // 情况6：只有部门条件 → 只查询部门表
            List<String> deptProcessIds = getProcInstIdsByTaskForNum(queryReqVO);
            if (CollectionUtil.isNotEmpty(deptProcessIds)) {
                processInstanceIds.addAll(deptProcessIds);
            }
        } else if (hasUserCondition) {
            // 情况7：只有用户条件 → 需要查询两个表（门店表和部门表都可能包含用户）
            List<String> storeProcessIds = getProcInstIdsByTaskStoreForNum(queryReqVO);
            if (CollectionUtil.isNotEmpty(storeProcessIds)) {
                processInstanceIds.addAll(storeProcessIds);
            }
            
            List<String> deptProcessIds = getProcInstIdsByTaskForNum(queryReqVO);
            if (CollectionUtil.isNotEmpty(deptProcessIds)) {
                processInstanceIds.addAll(deptProcessIds);
            }
        }
        
        // 去重处理
        if (CollectionUtil.isNotEmpty(processInstanceIds)) {
            processInstanceIds = processInstanceIds.stream().distinct().collect(Collectors.toList());
        }
        
        return processInstanceIds;
    }

    /**
     * 计算任务统计信息
     * 统计规则：
     * 1. 总任务数：taskList的size
     * 2. 待审核：PENDING状态
     * 3. 进行中：RUNNING状态
     * 4. 已拒绝：REJECT状态
     * 5. 已完成：COMPLETED状态
     * 6. 已逾期：OVERDUE状态 + PENDING中completionTime < 当前UTC+8时间 + RUNNING中completionTime < 当前UTC+8时间
     * 7. 完成率：已完成数 / 总任务数
     */
    @Override
    public void calculateTaskStatistics(BpmAppGetSumNumRespVO result, List<BpmBusinessQueryDO> taskList) {
        // 1. 任务总数


        
        // 获取当前UTC+8时间
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        
        // 2. 待审核任务数（PENDING状态）
        long pendingTasks = taskList.stream()
                .filter(task -> task.getTaskState() != null 
                        && task.getTaskState().equals(BpmProcessInstanceStatusEnum.PENDING.getStatus()))
                .count();
        result.setPendingTasks(pendingTasks);
        
        // 3. 进行中任务数（RUNNING状态）
        long inProgressTasks = taskList.stream()
                .filter(task -> task.getTaskState() != null 
                        && task.getTaskState().equals(BpmProcessInstanceStatusEnum.RUNNING.getStatus()))
                .count();
        result.setInProgressTasks(inProgressTasks);
        
        // 4. 已拒绝任务数（REJECT状态）
        long rejectedTasks = taskList.stream()
                .filter(task -> task.getTaskState() != null 
                        && task.getTaskState().equals(BpmProcessInstanceStatusEnum.REJECT.getStatus()))
                .count();
        result.setRejectedTasks(rejectedTasks);
        
        // 5. 已完成任务数（COMPLETED状态）
        long completedTasks = taskList.stream()
                .filter(task -> task.getTaskState() != null 
                        && task.getTaskState().equals(BpmProcessInstanceStatusEnum.COMPLETED.getStatus()))
                .count();
        result.setCompletedTasks(completedTasks);
        //任务总数 是 已完成 + 进行中+待审核
        result.setTotalTasks(inProgressTasks+pendingTasks+completedTasks);
        
        // 6. 已逾期任务数
        // OVERDUE状态 + PENDING中completionTime < 当前UTC+8时间 + RUNNING中completionTime < 当前UTC+8时间
        long overdueTasks = taskList.stream()
                .filter(task -> {
                    if (task.getTaskState() == null) {
                        return false;
                    }
                    Integer status = task.getTaskState();
                    
                    // OVERDUE状态
                    if (status.equals(BpmProcessInstanceStatusEnum.OVERDUE.getStatus())) {
                        return true;
                    }
                    
                    // PENDING状态且completionTime < 当前UTC+8时间
                    if (status.equals(BpmProcessInstanceStatusEnum.PENDING.getStatus())) {
                        if (task.getCompletionTime() != null) {
                            LocalDate completionDate = task.getCompletionTime().toLocalDate();
                            return completionDate.isBefore(today);
                        }
                        return false;
                    }
                    
                    // RUNNING状态且completionTime < 当前UTC+8时间
                    if (status.equals(BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
                        if (task.getCompletionTime() != null) {
                            LocalDate completionDate = task.getCompletionTime().toLocalDate();
                            return completionDate.isBefore(today);
                        }
                        return false;
                    }
                    
                    return false;
                })
                .count();
        result.setOverdueTasks(overdueTasks);
        
        // 7. 计算完成率
        calculateCompletionRate(result, completedTasks);
    }

    /**
     * 计算完成率
     */
    private void calculateCompletionRate(BpmAppGetSumNumRespVO result, long completedTasks) {
        if (result.getTotalTasks() > 0) {
            double completionRate = (double) completedTasks / result.getTotalTasks() * 100;
            // 保留小数点后两位
            result.setCompletionRate(Math.round(completionRate * 100.0) / 100.0);
        } else {
            result.setCompletionRate(0.0);
        }
    }

    @Override
    public BpmAppGetSumNumRespVO getBusinessTaskStatistics(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        BpmAppGetSumNumRespVO result = new BpmAppGetSumNumRespVO();

        List<BpmBusinessQueryDO> bpmBusinessQueryDOS = processBusinessQueryMapper.selectMainTaskList(bpmProcessInstanceQueryReqVO);





        // 统计任务状态并计算完成率
        if (CollectionUtil.isNotEmpty(bpmBusinessQueryDOS)) {
            calculateTaskStatistics(result, bpmBusinessQueryDOS);
        }

        return result;
    }


    @Override
    public List<BpmAppGetSumNumByDeptRespVO> getBusinessTaskStatisticsByDept(BpmProcessInstanceQueryReqVO queryReqVO) {
        List<BpmAppGetSumNumByDeptRespVO> result = new ArrayList<>();

        List<BpmBusinessQueryDO> bpmBusinessQueryDOS = processBusinessQueryMapper.selectMainTaskList(queryReqVO);
        if (ObjectUtil.isNotEmpty(bpmBusinessQueryDOS)) {
            List<Long> deptIds = new ArrayList<>();
            //门店的任务
            List<BpmBusinessQueryDO> storeBpmBusinessQueryDO = new ArrayList<>();
            List<BpmBusinessQueryDO> deptBpmBusinessQueryDO = new ArrayList<>();



            for (BpmBusinessQueryDO bpmBusinessQueryDO : bpmBusinessQueryDOS) {
                if (ObjectUtil.isNotEmpty(bpmBusinessQueryDO.getDeptId())){
                    if (!bpmBusinessQueryDO.getDeptId().equals(BPM_OA_BUSINESS_STORE_ID)) {
                        if (!deptIds.contains(bpmBusinessQueryDO.getDeptId())) {
                            deptIds.add(bpmBusinessQueryDO.getDeptId());
                        }
                        deptBpmBusinessQueryDO.add(bpmBusinessQueryDO);

                    }else {
                        storeBpmBusinessQueryDO.add(bpmBusinessQueryDO);
                    }
                }

            }
            //开始按照部门分
            if (ObjectUtil.isNotEmpty(deptIds)) {
                //部门 Map

                List<DeptRespDTO> deptRespDTOList = new ArrayList<>();


                //key 二级 id value 部门名称
                CommonResult<Map<Long, DeptDTO>> parentDeptList = deptApi.getParentDeptList(deptIds);
                Map<Long, DeptDTO> data = parentDeptList.getData();

                List<BpmBusinessQueryDTO> bpmBusinessQueryDTOS = new ArrayList<>();
                for (BpmBusinessQueryDO bpmBusinessQueryDO : deptBpmBusinessQueryDO) {
                    DeptDTO deptRespDTO = data.get(bpmBusinessQueryDO.getDeptId());
                    BpmBusinessQueryDTO queryDTO = new BpmBusinessQueryDTO();
                    BeanUtils.copyProperties(bpmBusinessQueryDO, queryDTO);
                    queryDTO.setSecondDeptId(deptRespDTO.getId());
                    // 核心逻辑：判断deptRespDTOList中是否已存在相同ID的对象，不存在则添加
                    if (CollectionUtil.isNotEmpty(deptRespDTOList)) {
                        // 检查列表中是否已有相同ID的部门
                        boolean isIdExisted = deptRespDTOList.stream()
                                .anyMatch(item -> item.getId().equals(deptRespDTO.getId()));
                        // 不存在则新建并添加
                        if (!isIdExisted) {
                            DeptRespDTO deptRespDTO1 = new DeptRespDTO();
                            deptRespDTO1.setId(deptRespDTO.getId());
                            deptRespDTO1.setName(deptRespDTO.getName());
                            deptRespDTOList.add(deptRespDTO1);
                        }
                    } else {
                        // 列表为空时，直接添加第一个对象
                        DeptRespDTO deptRespDTO1 = new DeptRespDTO();
                        deptRespDTO1.setId(deptRespDTO.getId());
                        deptRespDTO1.setName(deptRespDTO.getName());
                        deptRespDTOList.add(deptRespDTO1);
                    }

                    bpmBusinessQueryDTOS.add(queryDTO);
                }
                Map<Long, List<BpmBusinessQueryDTO>> collect = bpmBusinessQueryDTOS.stream().collect(Collectors.groupingBy(BpmBusinessQueryDTO::getSecondDeptId));


                for (DeptRespDTO deptRespDTO : deptRespDTOList) {
                    BpmAppGetSumNumByDeptRespVO bpmAppGetSumNumByDeptRespVO = new BpmAppGetSumNumByDeptRespVO();
                    BpmAppGetSumNumRespVO bpmAppGetSumNumRespVO = new BpmAppGetSumNumRespVO();
                    List<BpmBusinessQueryDTO> bpmBusinessQueryDTOS1 = collect.get(deptRespDTO.getId());
                    List<BpmBusinessQueryDO> bpmBusinessQueryDO = new ArrayList<>();
                    for (BpmBusinessQueryDTO bpmBusinessQueryDTO : bpmBusinessQueryDTOS1) {
                        BpmBusinessQueryDO bpmBusinessQueryDO1 = new BpmBusinessQueryDO();
                        BeanUtils.copyProperties(bpmBusinessQueryDTO, bpmBusinessQueryDO1);
                        bpmBusinessQueryDO.add(bpmBusinessQueryDO1);
                    }

                    calculateTaskStatistics(bpmAppGetSumNumRespVO, bpmBusinessQueryDO);
                    BeanUtils.copyProperties(bpmAppGetSumNumRespVO, bpmAppGetSumNumByDeptRespVO);
                    bpmAppGetSumNumByDeptRespVO.setDeptName(deptRespDTO.getName());
                    bpmAppGetSumNumByDeptRespVO.setSecondDeptId(deptRespDTO.getId());
                    result.add(bpmAppGetSumNumByDeptRespVO);
                }




            }



            if (ObjectUtil.isNotEmpty(storeBpmBusinessQueryDO)) {
                BpmAppGetSumNumByDeptRespVO bpmAppGetSumNumByDeptRespVO = new BpmAppGetSumNumByDeptRespVO();
                BpmAppGetSumNumRespVO resultByStore = new BpmAppGetSumNumRespVO();
                calculateTaskStatistics(resultByStore, storeBpmBusinessQueryDO);
                BeanUtils.copyProperties(resultByStore, bpmAppGetSumNumByDeptRespVO);
                bpmAppGetSumNumByDeptRespVO.setDeptName("门店任务");
                result.add(bpmAppGetSumNumByDeptRespVO);


            }
        }


        return result;
    }

    /**
     * 构建时间参数（针对 BpmProcessInstanceQueryReqVO）
     */
    private void buildCreateTimeForQueryReqVO(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        LocalDateTime createTimeStart = bpmProcessInstanceQueryReqVO.getCreateTimeStart();
        LocalDateTime createTimeEnd = bpmProcessInstanceQueryReqVO.getCreateTimeEnd();

        if (createTimeStart == null && createTimeEnd == null) {
            // 取最近一个月时间：开始时间 = 当前时间 - 1个月，结束时间 = 当前时间
            LocalDateTime now = LocalDateTime.now();
            createTimeStart = now.minusMonths(1);
            createTimeEnd = now;

            // 设置回查询参数中
            bpmProcessInstanceQueryReqVO.setCreateTimeStart(createTimeStart);
            bpmProcessInstanceQueryReqVO.setCreateTimeEnd(createTimeEnd);
        }
    }

    /**
     * 获取全部任务列表（用于统计，不分页）
     */
    private List<BpmBusinessQueryDO> getBusinessTaskListForStatistics(BpmProcessInstanceQueryReqVO createReqVO) {
        // 数据可见范围 当前登录者能查看哪些流程
        CommonResult<List<Long>> commonResult = deptOrgApi.getUserIdsByDept();
        // 获取所有可见的userId
        List<Long> userIds = commonResult.getCheckedData();
        if (CollectionUtil.isEmpty(userIds)) {
            throw exception(USER_DEPT_ERROR);
        }

        List<String> userIdList = userIds.stream()
                .map(String::valueOf)
                .toList();
        List<String> proInstIdList = new ArrayList<>();
        extractedAllListForStatistics(userIdList, proInstIdList);

        if (CollectionUtil.isEmpty(proInstIdList)) {
            return new ArrayList<>();
        }

        // 统计 涉及到执行人 执行部门 执行门店流程ID
        boolean childFlag = false;
        boolean parentFlag = false;
        int executorCount = 0;
        if (CollectionUtil.isNotEmpty(createReqVO.getExecutorStoreId()) || CollectionUtil.isNotEmpty(createReqVO.getExecutorUserId())) {
            childFlag = true;
            List<String> procInstIdsChild = getProcInstIdsByTaskStoreForStatistics(createReqVO);
            if (CollectionUtil.isNotEmpty(procInstIdsChild)) {
                proInstIdList.retainAll(procInstIdsChild);
                executorCount++;
            }
        }
        if (createReqVO.getExecutorDeptId() != null || CollectionUtil.isNotEmpty(createReqVO.getExecutorUserId())) {
            parentFlag = true;
            List<String> procInstIdsParent = getProcInstIdsByTaskForStatistics(createReqVO);
            if (CollectionUtil.isNotEmpty(procInstIdsParent)) {
                proInstIdList.retainAll(procInstIdsParent);
                executorCount++;
            }
        }

        if ((childFlag || parentFlag) && executorCount == 0) {
            return new ArrayList<>();
        }

        if (CollectionUtil.isNotEmpty(proInstIdList)) {
            createReqVO.setProInstIdList(proInstIdList);
            // 使用 MyBatis Plus 查询
            return selectTaskListAllForStatistics(createReqVO);
        }

        return new ArrayList<>();
    }

    /**
     * 获取非全部任务列表（用于统计，不分页）
     */
    private List<BpmBusinessQueryDO> getBusinessTaskListNotAllForStatistics(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        // 统计 涉及到执行人 执行部门 执行门店流程ID
        List<String> IdList = bpmProcessInstanceQueryReqVO.getProInstIdList();
        if (CollectionUtil.isEmpty(IdList)) {
            return new ArrayList<>();
        }
        List<String> proInstIdList = new ArrayList<>();
        proInstIdList.addAll(IdList);
        boolean childFlag = false;
        boolean parentFlag = false;
        int executorCount = 0;
        if (CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorStoreId()) || CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorUserId())) {
            childFlag = true;
            List<String> procInstIdsChild = getProcInstIdsByTaskStoreForStatistics(bpmProcessInstanceQueryReqVO);
            if (CollectionUtil.isNotEmpty(procInstIdsChild)) {
                proInstIdList.retainAll(procInstIdsChild);
                executorCount++;
            }
        }
        if (bpmProcessInstanceQueryReqVO.getExecutorDeptId() != null || CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorUserId())) {
            parentFlag = true;
            List<String> procInstIdsParent = getProcInstIdsByTaskForStatistics(bpmProcessInstanceQueryReqVO);
            if (CollectionUtil.isNotEmpty(procInstIdsParent)) {
                proInstIdList.retainAll(procInstIdsParent);
                executorCount++;
            }
        }

        if ((childFlag || parentFlag) && executorCount == 0) {
            return new ArrayList<>();
        }

        if (CollectionUtil.isNotEmpty(proInstIdList)) {
            bpmProcessInstanceQueryReqVO.setProInstIdList(proInstIdList);
            // 使用 MyBatis Plus 查询
            return selectTaskListAllForStatistics(bpmProcessInstanceQueryReqVO);
        }

        return new ArrayList<>();
    }

    /**
     * 获取抄送任务列表（用于统计，不分页）
     */
    private List<BpmBusinessQueryDO> getBusinessTaskListCopyForStatistics(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO) {
        // 统计 涉及到执行人 执行部门 执行门店流程ID
        List<String> IdList = bpmProcessInstanceQueryReqVO.getProInstIdList();
        if (CollectionUtil.isEmpty(IdList)) {
            return new ArrayList<>();
        }
        List<String> proInstIdList = new ArrayList<>();
        proInstIdList.addAll(IdList);
        boolean childFlag = false;
        boolean parentFlag = false;
        int executorCount = 0;
        BpmProcessInstanceQueryReqVO bean = BeanUtil.toBean(bpmProcessInstanceQueryReqVO, BpmProcessInstanceQueryReqVO.class);
        if (CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorStoreId()) || CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorUserId())) {
            childFlag = true;
            List<String> procInstIdsChild = getProcInstIdsByTaskStore(bean);
            if (CollectionUtil.isNotEmpty(procInstIdsChild)) {
                proInstIdList.retainAll(procInstIdsChild);
                executorCount++;
            }
        }
        if (bpmProcessInstanceQueryReqVO.getExecutorDeptId() != null || CollectionUtil.isNotEmpty(bpmProcessInstanceQueryReqVO.getExecutorUserId())) {
            parentFlag = true;
            List<String> procInstIdsParent = getProcInstIdsByTask(bean);
            if (CollectionUtil.isNotEmpty(procInstIdsParent)) {
                proInstIdList.retainAll(procInstIdsParent);
                executorCount++;
            }
        }

        if ((childFlag || parentFlag) && executorCount == 0) {
            return new ArrayList<>();
        }

        if (CollectionUtil.isNotEmpty(proInstIdList)) {
            Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
            bpmProcessInstanceQueryReqVO.setProInstIdList(proInstIdList);

            // 如果 copyFlag 为 0，则只查询未读的抄送
            if (bpmProcessInstanceQueryReqVO.getCopyFlag() != null &&
                    bpmProcessInstanceQueryReqVO.getCopyFlag() == 0) {
                // 获取本人抄送未读
                // 总流程ID - 抄送已读的流程ID
                List<String> procList = new ArrayList<>();
                ResultHandler<String> resultHandler = new ResultHandler<String>() {
                    @Override
                    public void handleResult(ResultContext<? extends String> resultContext) {
                        String procId = resultContext.getResultObject();
                        if (Objects.nonNull(procId)) {
                            procList.add(procId);
                        }
                    }
                };
                LambdaQueryWrapper<BpmBusinessCopyDO> copyWrapper = new LambdaQueryWrapper<>();
                copyWrapper.select(BpmBusinessCopyDO::getProcInstId);
                copyWrapper.eq(BpmBusinessCopyDO::getCopyUserId, loginUserId);
                copyWrapper.eq(BpmBusinessCopyDO::getCopyFlag, 1);
                copyWrapper.in(BpmBusinessCopyDO::getProcInstId, bpmProcessInstanceQueryReqVO.getProInstIdList());
                bpmBusinessCopyMapper.selectObjs(copyWrapper, resultHandler);

                if (CollectionUtil.isNotEmpty(procList)) {
                    proInstIdList.removeAll(procList);
                    bpmProcessInstanceQueryReqVO.setProInstIdList(proInstIdList);
                }
            }

            if (CollectionUtil.isNotEmpty(proInstIdList)) {
                // 使用 MyBatis Plus 查询
                return selectTaskListAllForStatistics(bpmProcessInstanceQueryReqVO);
            }
        }

        return new ArrayList<>();
    }

    /**
     * 提取所有流程实例ID列表（用于统计，独立方法）
     */
    private void extractedAllListForStatistics(List<String> userIdList, List<String> proInstIdList) {
        Set<String> set = new HashSet<>();
        // 统计所有代办
        List<String> todoResult = todoAllForStatistics(userIdList, proInstIdList);
        if (CollectionUtil.isNotEmpty(todoResult)) {
            set.addAll(todoResult);
        }
        // 统计所有已办
        List<String> doneResult = doneAllForStatistics(userIdList, proInstIdList);
        if (CollectionUtil.isNotEmpty(doneResult)) {
            set.addAll(doneResult);
        }
        // 统计所有我的
        List<String> myResult = myAllForStatistics(userIdList, proInstIdList);
        if (CollectionUtil.isNotEmpty(myResult)) {
            set.addAll(myResult);
        }
        // 统计所有抄送
        List<String> copyResult = copyAllForStatistics(userIdList, proInstIdList);
        if (CollectionUtil.isNotEmpty(copyResult)) {
            set.addAll(copyResult);
        }

        if (CollectionUtil.isNotEmpty(set)) {
            proInstIdList.addAll(set);
        }
    }

    /**
     * 获取抄送列表（用于统计，独立方法）
     */
    private List<String> copyAllForStatistics(List<String> userIdList, List<String> proInstIdList) {
        return processInstanceCopyService.getBpmProcessInstanceCopyList(userIdList, proInstIdList);
    }

    /**
     * 获取我的流程列表（用于统计，独立方法）
     */
    private List<String> myAllForStatistics(List<String> userIdList, List<String> proInstIdList) {
        LambdaQueryWrapper<BpmBusinessDO> wrapper = new LambdaQueryWrapper<>();
        List<String> procList = new ArrayList<>();
        ResultHandler<String> resultHandler = new ResultHandler<String>() {
            @Override
            public void handleResult(ResultContext<? extends String> resultContext) {
                String procId = resultContext.getResultObject();
                if (Objects.nonNull(procId)) {
                    procList.add(procId);
                }
            }
        };

        wrapper.select(BpmBusinessDO::getProcInstId);
        wrapper.in(BpmBusinessDO::getApplicantId, userIdList);

        if (CollectionUtil.isNotEmpty(proInstIdList)){
            wrapper.in(BpmBusinessDO::getProcInstId, proInstIdList);
        }

        wrapper.isNull(BpmBusinessDO::getParentProcInstId);
        processBusinessMapper.selectObjs(wrapper, resultHandler);

        return procList;
    }

    /**
     * 获取已办列表（用于统计，独立方法）
     */
    private List<String> doneAllForStatistics(List<String> userIds, List<String> proInstIdList) {
        return taskService.getBpmTaskDoneList(userIds, proInstIdList);
    }

    /**
     * 获取待办列表（用于统计，独立方法）
     */
    private List<String> todoAllForStatistics(List<String> userIds, List<String> proInstIdList) {
        return taskService.getBpmTaskTodoList(userIds, proInstIdList);
    }

    /**
     * 根据执行人条件获取流程实例ID列表（用于统计，独立方法）
     */
    private List<String> getProcInstIdsByTaskForStatistics(BpmProcessInstanceQueryReqVO queryReqVO) {
        LambdaQueryWrapper<BpmBusinessDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(BpmBusinessDO::getProcInstId);
        if (CollectionUtil.isNotEmpty(queryReqVO.getExecutorUserId())) {
            wrapper.in(BpmBusinessDO::getExecutorUserId, queryReqVO.getExecutorUserId());
        }
        if (queryReqVO.getExecutorDeptId() != null) {
            wrapper.eq(BpmBusinessDO::getExecutorDeptId, queryReqVO.getExecutorDeptId());
        }

        List<String> proInstIdList = new ArrayList<>();
        ResultHandler<String> resultHandler = new ResultHandler<String>() {
            @Override
            public void handleResult(ResultContext<? extends String> resultContext) {
                String proInstId = resultContext.getResultObject();
                if (Objects.nonNull(proInstId)) {
                    proInstIdList.add(proInstId);
                }
            }
        };
        processBusinessMapper.selectObjs(wrapper, resultHandler);
        return proInstIdList;
    }

    /**
     * 根据执行门店条件获取流程实例ID列表（用于统计，独立方法）
     */
    private List<String> getProcInstIdsByTaskStoreForStatistics(BpmProcessInstanceQueryReqVO queryReqVO) {
        LambdaQueryWrapper<BussinessTaskStoreDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(BussinessTaskStoreDO::getProcInstId);
        if (CollectionUtil.isNotEmpty(queryReqVO.getExecutorStoreId())) {
            wrapper.in(BussinessTaskStoreDO::getExecutorStoreId, queryReqVO.getExecutorStoreId());
        }
        if (CollectionUtil.isNotEmpty(queryReqVO.getExecutorUserId())) {
            wrapper.in(BussinessTaskStoreDO::getUserId, queryReqVO.getExecutorUserId());
        }

        List<String> proInstIdList = new ArrayList<>();
        ResultHandler<String> resultHandler = new ResultHandler<String>() {
            @Override
            public void handleResult(ResultContext<? extends String> resultContext) {
                String proInstId = resultContext.getResultObject();
                if (Objects.nonNull(proInstId)) {
                    proInstIdList.add(proInstId);
                }
            }
        };
        storeInfoMapper.selectObjs(wrapper, resultHandler);
        return proInstIdList;
    }

    /**
     * 使用 MyBatis Plus 查询任务列表（用于统计，不分页）
     * 注意：执行人条件（executorUserId、executorDeptId、executorStoreId）已经在 proInstIdList 中处理过了，不需要在这里再次过滤
     * 查询条件与 selectMainTaskListByPage 保持一致
     */
    private List<BpmBusinessQueryDO> selectTaskListAllForStatistics(BpmProcessInstanceQueryReqVO queryReqVO) {
        LambdaQueryWrapperX<BpmBusinessQueryDO> wrapper = new LambdaQueryWrapperX<>();
        
        // 基础条件：parent_proc_inst_id is null
        wrapper.isNull(BpmBusinessQueryDO::getParentProcInstId);
        
        // proc_inst_id IN 条件（这个已经包含了执行人条件的过滤结果）
        if (CollectionUtil.isNotEmpty(queryReqVO.getProInstIdList())) {
            wrapper.in(BpmBusinessQueryDO::getProcInstId, queryReqVO.getProInstIdList());
        }
        
        // 发起人条件
        wrapper.eqIfPresent(BpmBusinessQueryDO::getApplicantId, queryReqVO.getApplicantId());
        
        // 任务内容模糊查询（task_name 或 task_desc like）
        if (queryReqVO.getTaskContent() != null && !queryReqVO.getTaskContent().trim().isEmpty()) {
            wrapper.and(w -> w.like(BpmBusinessQueryDO::getTaskName, queryReqVO.getTaskContent())
                    .or()
                    .like(BpmBusinessQueryDO::getTaskDesc, queryReqVO.getTaskContent()));
        }
        
        // 发起人ID列表条件
        if (CollectionUtil.isNotEmpty(queryReqVO.getApplicantIdList())) {
            wrapper.in(BpmBusinessQueryDO::getApplicantId, queryReqVO.getApplicantIdList());
        }
        
        // 优先级条件
        if (queryReqVO.getPriority() != null && !queryReqVO.getPriority().trim().isEmpty()) {
            wrapper.eq(BpmBusinessQueryDO::getPriority, queryReqVO.getPriority());
        }
        
        // 发起部门条件
        wrapper.eqIfPresent(BpmBusinessQueryDO::getDeptId, queryReqVO.getApplicantDeptId());
        
        // 发起门店条件
        wrapper.eqIfPresent(BpmBusinessQueryDO::getStoreId, queryReqVO.getApplicantStoreId());
        
        // 流程定义ID条件
        wrapper.eqIfPresent(BpmBusinessQueryDO::getFlowId, queryReqVO.getProcessDefinitionId());
        
        // 任务状态条件（特殊处理：taskState == 5 表示逾期）
        if (queryReqVO.getTaskState() != null && !queryReqVO.getTaskState().trim().isEmpty()) {
            try {
                Integer taskStateInt = Integer.valueOf(queryReqVO.getTaskState());
                if (taskStateInt == 5) {
                    // 逾期：task_state != 6 且 task_state != 4 且 completion_time < 当前时间
                    wrapper.ne(BpmBusinessQueryDO::getTaskState, 6)
                            .ne(BpmBusinessQueryDO::getTaskState, 4)
                            .lt(BpmBusinessQueryDO::getCompletionTime, LocalDateTime.now());
                } else {
                    // 其他状态：直接等于
                    wrapper.eq(BpmBusinessQueryDO::getTaskState, taskStateInt);
                }
            } catch (NumberFormatException e) {
                // 如果转换失败，忽略这个条件
                log.warn("Invalid taskState value: {}", queryReqVO.getTaskState());
            }
        }
        
        // 任务类型条件
        if (queryReqVO.getTaskType() != null) {
            wrapper.eq(BpmBusinessQueryDO::getTaskType, queryReqVO.getTaskType());
        }
        
        // 抄送标识条件
        if (queryReqVO.getCopyFlag() != null) {
            wrapper.eq(BpmBusinessQueryDO::getCopyFlag, queryReqVO.getCopyFlag());
        }
        
        // 创建时间范围条件
        if (queryReqVO.getCreateTimeStart() != null && queryReqVO.getCreateTimeEnd() != null) {
            wrapper.between(BpmBusinessQueryDO::getCreateTime, queryReqVO.getCreateTimeStart(), queryReqVO.getCreateTimeEnd());
        }
        
        // 任务完成时间范围条件
        if (queryReqVO.getTaskCompletionTimeStart() != null && queryReqVO.getTaskCompletionTimeEnd() != null) {
            wrapper.between(BpmBusinessQueryDO::getCompletionTime, queryReqVO.getTaskCompletionTimeStart(), queryReqVO.getTaskCompletionTimeEnd());
        }
        
        // 去重查询
        return processBusinessQueryMapper.selectList(wrapper).stream()
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<BpmBusinessDO> selectListByOaProjectId(Long oaProjectId) {

        return processBusinessMapper.selectList(BpmBusinessDO::getOaProjectId, oaProjectId);
    }


}
