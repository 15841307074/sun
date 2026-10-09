package com.htyoudao.youdao.module.bpm.service.project;


import static com.htyoudao.youdao.module.bpm.enums.LogRecordConstants.*;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectCreateReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectPageReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectPageRespVO;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectRelationshipReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.BpmAppGetSumNumRespVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessQueryDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.project.OAProjectDO;
import com.htyoudao.youdao.module.bpm.dal.mysql.definition.BpmProcessBusinessMapper;
import com.htyoudao.youdao.module.bpm.dal.mysql.oa.OAProjectMapper;
import com.htyoudao.youdao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.htyoudao.youdao.module.bpm.service.definition.BpmProcessBusinessService;
import com.htyoudao.youdao.module.bpm.service.task.BpmProcessInstanceService;
import com.htyoudao.youdao.module.system.api.dept.DeptApi;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptRespDTO;
import com.htyoudao.youdao.module.system.api.permission.PermissionApi;
import com.htyoudao.youdao.module.system.api.user.AdminUserApi;
import com.htyoudao.youdao.module.system.api.user.dto.AdminUserRespDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * OA 项目 Service 实现类
 *
 * @author jason
 * @author 0090
 */
@Service
public class OAProjectServiceImpl extends ServiceImpl<OAProjectMapper, OAProjectDO> implements OAProjectService {


    @Resource
    private OAProjectMapper oaProjectMapper;

    @Resource
    private OAProjectUserService oaProjectUserService;

    @Resource
    private BpmProcessBusinessMapper bpmBusinessMapper;

    @Resource
    @Lazy
    private BpmProcessInstanceService bpmProcessInstanceService;

    @Lazy
    @Resource
    private BpmProcessBusinessService bpmProcessBusinessService;


    @DubboReference
    private AdminUserApi userApi;

    @DubboReference
    private DeptApi deptApi;

    @DubboReference
    private PermissionApi permissionApi;



    @Override
    public PageResult<OAProjectPageRespVO> getProjectPage(OAProjectPageReqVO pageVO) {

        LambdaQueryWrapperX<OAProjectDO> queryWrapperX = new LambdaQueryWrapperX();

        if (StringUtils.isNotBlank(pageVO.getKeyword())){
            queryWrapperX.and(wq -> {
                wq.like(OAProjectDO::getProjectName, pageVO.getKeyword().trim());
                wq.or().eq(OAProjectDO::getProjectDescription, pageVO.getKeyword().trim());
            });
        }

        queryWrapperX.likeIfPresent(OAProjectDO::getDeptIds, pageVO.getDeptId());
        queryWrapperX.inIfPresent(OAProjectDO::getProjectLeader, pageVO.getProjectLeader());
        queryWrapperX.likeIfPresent(OAProjectDO::getProjectMembers, pageVO.getProjectUser());

        queryWrapperX.geIfPresent(OAProjectDO::getEndDate, pageVO.getStartDate());
        queryWrapperX.leIfPresent(OAProjectDO::getStartDate, pageVO.getEndDate());

        Long userId = SecurityFrameworkUtils.getLoginUserId();

        if (!pageVO.getIsAll()) {
            queryWrapperX.and(wq -> {

                //项目成员
                wq.like(OAProjectDO::getProjectMembers, userId + "");

                //项目负责人
                wq.or().eq(OAProjectDO::getProjectLeader, userId);

                //判断当前用户是否为部门负责人
                List<Long> checkedData = deptApi.getManagerDeptByUserId(userId).getCheckedData();
                if (CollectionUtils.isEmpty(checkedData)) {
                    return;
                }
                for (Long deptId : checkedData) {
                    wq.or().like(OAProjectDO::getDeptIds, deptId);
                }
            });
        }


        queryWrapperX.orderByDesc(OAProjectDO::getCreateTime);
        PageResult<OAProjectDO> projectDOPageResult = oaProjectMapper.selectPage(pageVO, queryWrapperX);

        List<OAProjectDO> list = projectDOPageResult.getList();
        List<OAProjectPageRespVO> pageResponseVOS = BeanCopyUtils.copyBeanList(list, OAProjectPageRespVO.class);

        //设置用户信息
        setUserInfo(pageResponseVOS);

        //设置部门信息
        setDeptInfo(pageResponseVOS);

        //根据oaProjectId 设置完成情况
        setSumNum(pageResponseVOS);


        PageResult<OAProjectPageRespVO> pageResult = new PageResult();
        pageResult.setList(pageResponseVOS);
        pageResult.setTotal(projectDOPageResult.getTotal());
        return pageResult;
    }

    private void setSumNum(List<OAProjectPageRespVO> pageResponseVOS) {
        for (OAProjectPageRespVO pageResponseVO : pageResponseVOS) {
            Long oaProjectId = pageResponseVO.getId();
            List<BpmBusinessDO> bpmBusinessList = getBpmBusinessList(oaProjectId);
            BpmAppGetSumNumRespVO result = new BpmAppGetSumNumRespVO();

            if (!CollectionUtils.isEmpty(bpmBusinessList)){
                //总任务数：不含已拒绝、已撤销任务数
                List<Integer> totalStatus = List.of(
                    BpmProcessInstanceStatusEnum.REJECT.getStatus(),
                    BpmProcessInstanceStatusEnum.CANCEL.getStatus()
                );
                long totalTasks = bpmBusinessList.stream()
                    .filter(task -> task.getTaskState() != null
                        && !totalStatus.contains(task.getTaskState()))
                    .count();
                pageResponseVO.setTotalTasks(totalTasks);

                //已完成任务数
                long completedTasks = bpmBusinessList.stream()
                    .filter(task -> task.getTaskState() != null
                        && task.getTaskState().equals(BpmProcessInstanceStatusEnum.COMPLETED.getStatus()))
                    .count();
                pageResponseVO.setCompletedTasks(completedTasks);

                List<BpmBusinessQueryDO> bean = BeanUtils.toBean(bpmBusinessList, BpmBusinessQueryDO.class);
                bpmProcessBusinessService.calculateTaskStatistics(result, bean);
            }
            pageResponseVO.setSumNumRespVO(result);
        }
    }

    private List<BpmBusinessDO> getBpmBusinessList(Long oaProjectId) {
        LambdaQueryWrapperX<BpmBusinessDO> queryWrapperX = new LambdaQueryWrapperX();
        queryWrapperX.eq(BpmBusinessDO::getOaProjectId, oaProjectId);
        queryWrapperX.isNull(BpmBusinessDO::getParentProcInstId);
        return bpmBusinessMapper.selectList(queryWrapperX);
    }

    private void setDeptInfo(List<OAProjectPageRespVO> pageResponseVOS) {
        if (CollectionUtils.isEmpty(pageResponseVOS)) {
            return;
        }

        try {
            // 1. 收集所有部门ID
            Set<Long> deptIds = collectAllDeptIds(pageResponseVOS);

            if (CollectionUtils.isEmpty(deptIds)) {
                setEmptyDeptList(pageResponseVOS);
                return;
            }

            // 2. 批量获取部门信息
            Map<Long, DeptRespDTO> deptMap = fetchDeptMap(deptIds);

            // 3. 为每个VO设置部门列表
            pageResponseVOS.forEach(vo -> setDeptListForVO(vo, deptMap));

        } catch (Exception e) {
            log.error("设置部门信息时发生异常", e);
            // 发生异常时设置空列表，避免影响主流程
            setEmptyDeptList(pageResponseVOS);
        }
    }

    /**
     * 收集所有部门ID
     */
    private Set<Long> collectAllDeptIds(List<OAProjectPageRespVO> pageResponseVOS) {
        return pageResponseVOS.stream()
            .map(OAProjectPageRespVO::getDeptIds)
            .filter(StringUtils::isNotBlank)
            .flatMap(deptIdsStr -> Arrays.stream(deptIdsStr.split(",")))
            .filter(StringUtils::isNotBlank) // 过滤空字符串
            .map(Long::valueOf)
            .collect(Collectors.toSet());
    }



    /**
     * 获取部门信息映射
     */
    private Map<Long, DeptRespDTO> fetchDeptMap(Set<Long> deptIds) {
        try {
            CommonResult<List<DeptRespDTO>> deptResult = deptApi.getDeptList(deptIds);
            if (deptResult == null || !deptResult.isSuccess() || CollectionUtils.isEmpty(deptResult.getCheckedData())) {
                log.warn("获取部门信息失败或返回空数据");
                return Collections.emptyMap();
            }

            return deptResult.getCheckedData().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(DeptRespDTO::getId, Function.identity()));
        } catch (Exception e) {
            log.error("调用部门API异常", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 为单个VO设置部门列表
     */
    private void setDeptListForVO(OAProjectPageRespVO vo, Map<Long, DeptRespDTO> deptMap) {
        if (vo == null || StringUtils.isBlank(vo.getDeptIds())) {
            vo.setDeptList(Collections.emptyList());
            return;
        }

        List<DeptRespDTO> deptList = Arrays.stream(vo.getDeptIds().split(","))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .map(Long::valueOf)
            .map(deptId -> Optional.ofNullable(deptMap.get(deptId))
                .orElseGet(() -> createDefaultDept(deptId))) // 创建默认部门信息
            .collect(Collectors.toList());

        vo.setDeptList(deptList);
    }

    /**
     * 创建默认部门信息（当部门信息不存在时）
     */
    private DeptRespDTO createDefaultDept(Long deptId) {
        DeptRespDTO defaultDept = new DeptRespDTO();
        defaultDept.setId(deptId);
        defaultDept.setName("未知部门");
        // 设置其他必要的默认字段...
        return defaultDept;
    }

    /**
     * 设置空的部门列表
     */
    private void setEmptyDeptList(List<OAProjectPageRespVO> pageResponseVOS) {
        pageResponseVOS.forEach(vo -> vo.setDeptList(Collections.emptyList()));
    }


    private void setUserInfo(List<OAProjectPageRespVO> pageResponseVOS) {
        // 1. 收集所有用户ID
        Set<Long> userIds = new HashSet<>();
        pageResponseVOS.forEach(vo -> {
            userIds.add(vo.getProjectLeader());
            List<Long> projectUserIds = oaProjectUserService.getUserIdsByProjectId(vo.getId());
            vo.setProjectUsers(projectUserIds);
            userIds.addAll(projectUserIds);
        });

        // 2. 批量获取用户信息
        Map<Long, AdminUserRespDTO> userMap = userApi.getUserMap(userIds);

        // 3. 设置用户信息到VO
        pageResponseVOS.forEach(vo -> {
            // 设置项目负责人信息
            Optional.ofNullable(userMap.get(vo.getProjectLeader()))
                .ifPresent(vo::setProjectLeaderDTO);

            // 设置项目成员信息
            Optional.ofNullable(vo.getProjectUsers())
                .map(users -> users.stream()
                    .map(userMap::get)
                    .filter(Objects::nonNull) // 过滤掉null值
                    .collect(Collectors.toList()))
                .ifPresent(vo::setProjectUserDTOs);
        });
    }


    @Override
    @LogRecord(type = BPM_OA_BUSINESS, subType = BPM_OA_BUSINESS_CREATE_TYPE, bizNo = "{{#oaProject.id}}", success = BPM_OA_BUSINESS_CREATE_SUCCESS)
    public Long createOAProject(OAProjectCreateReqVO createReqVO) {
        OAProjectDO oaProjectDO = BeanCopyUtils.copyBean(createReqVO, OAProjectDO.class);
        oaProjectDO.setId(null);
        oaProjectDO.setProjectMembers(createReqVO.getProjectUsers().stream().map(String::valueOf).collect(Collectors.joining(",")));
        oaProjectDO.setDeptIds(createReqVO.getDeptIds().stream().map(String::valueOf) .collect(Collectors.joining(",")));
        oaProjectMapper.insert(oaProjectDO);

        Long oaProjectDOId = oaProjectDO.getId();

        List<Long> projectUsers = createReqVO.getProjectUsers();
        oaProjectUserService.batchSaveByProjectId(oaProjectDOId, projectUsers);

        LogRecordContext.putVariable("oaProject", oaProjectDO);
        return oaProjectDOId;
    }

    @Override
    @LogRecord(type = BPM_OA_BUSINESS, subType = BPM_OA_BUSINESS_UPDATE_TYPE, bizNo = "{{#oaProject.id}}", success = BPM_OA_BUSINESS_UPDATE_SUCCESS)
    public Long updateOAProject(OAProjectCreateReqVO createReqVO) {
        OAProjectDO oaProjectDO = BeanCopyUtils.copyBean(createReqVO, OAProjectDO.class);
        oaProjectDO.setProjectMembers(createReqVO.getProjectUsers().stream().map(String::valueOf).collect(Collectors.joining(",")));
        oaProjectDO.setDeptIds(createReqVO.getDeptIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
        oaProjectMapper.updateById(oaProjectDO);

        Long oaProjectDOId = oaProjectDO.getId();

        List<Long> projectUsers = createReqVO.getProjectUsers();
        oaProjectUserService.batchUpdateByProjectId(oaProjectDOId, projectUsers);

        LogRecordContext.putVariable("oaProject", oaProjectDO);
        return oaProjectDOId;
    }

    @Override
    @LogRecord(type = BPM_OA_BUSINESS, subType = BPM_OA_BUSINESS_DELETE_TYPE, bizNo = "{{#id}}", success = BPM_OA_BUSINESS_DELETE_SUCCESS)
    public void deleteProject(Long id) {

        List<BpmBusinessDO> bpmBusinessList = getBpmBusinessList(id);

        if (!CollectionUtils.isEmpty(bpmBusinessList)){
            throw new ServiceException(500, "项目下已有关联任务，如需删除项目，请移除关联任务");
        }

        oaProjectMapper.deleteById(id);
        oaProjectUserService.removeByOAProjectId(id);
    }

    @Override
    @LogRecord(type = BPM_OA_BUSINESS, subType = BPM_OA_BUSINESS_CREATE_RELATIONSHIP_TYPE, bizNo = "{{#reqVO.oaProjectId}}", success = BPM_OA_BUSINESS_CREATE_RELATIONSHIP_SUCCESS)
    public void addRelationship(OAProjectRelationshipReqVO reqVO) {

        // 1. 查询任务数据
        List<BpmBusinessDO> bpmBusinessDOS = bpmBusinessMapper.selectList(
            BpmBusinessDO::getTaskId, reqVO.getBusinessTaskIds());

        if (CollectionUtils.isEmpty(bpmBusinessDOS)) {
            log.warn("任务不存在: taskIds={}" + reqVO.getBusinessTaskIds());
            throw new ServiceException(500 , "任务不存在");
        }

        // 2. 检查是否已被关联到其他项目
        Optional<BpmBusinessDO> alreadyAssociatedTask = bpmBusinessDOS.stream()
            .filter(task -> task.getOaProjectId() != null)
            .findFirst();

        if (alreadyAssociatedTask.isPresent()) {
            BpmBusinessDO task = alreadyAssociatedTask.get();
            log.warn("任务已被关联至其他项目: " + task.getTaskId() +task.getOaProjectId());
            throw new ServiceException(500, "任务关联失败，所选任务已被关联至其他项目");
        }

        for (Long businessTaskId : reqVO.getBusinessTaskIds()) {
            BpmBusinessDO bpmBusinessDO = new BpmBusinessDO();
            bpmBusinessDO.setOaProjectId(reqVO.getOaProjectId());
            bpmBusinessDO.setTaskId(businessTaskId);
            bpmBusinessMapper.updateById(bpmBusinessDO);
        }
    }

    @Override
    @LogRecord(type = BPM_OA_BUSINESS, subType = BPM_OA_BUSINESS_DELETE_RELATIONSHIP_TYPE, bizNo = "{{#reqVO.oaProjectId}}", success = BPM_OA_BUSINESS_DELETE_RELATIONSHIP_SUCCESS)
    public void removeRelationship(OAProjectRelationshipReqVO reqVO) {
        bpmBusinessMapper.removeOABusiness(reqVO.getBusinessTaskIds());

    }

    @Override
    public OAProjectPageRespVO detailById(Long id) {
        OAProjectDO oaProjectDO = oaProjectMapper.selectById(id);
        if (oaProjectDO == null){
            return null;
        }
        OAProjectPageRespVO respVO = BeanUtils.toBean(oaProjectDO, OAProjectPageRespVO.class);
        List<OAProjectPageRespVO> respVOList = List.of(respVO);
        setDeptInfo(respVOList);
        setUserInfo(respVOList);
        setSumNum(respVOList);
        return respVOList.get(0);
    }

    @Override
    public OAProjectPageRespVO selectById(Long oaProjectId) {
        OAProjectDO oaProjectDO = oaProjectMapper.selectById(oaProjectId);
        return BeanUtils.toBean(oaProjectDO, OAProjectPageRespVO.class);
    }

    @Override
    public Map<Long, String> getAll() {
        List<OAProjectDO> oaProjectDOS = oaProjectMapper.selectList();
        Map<Long, String> projectMap = oaProjectDOS.stream()
                // 关键：将每个 OAProjectDO 映射为 Map 的 entry（key=id，value=projectName）
                .collect(Collectors.toMap(
                        OAProjectDO::getId,    // key 提取器：获取 id 作为 key
                        OAProjectDO::getProjectName // value 提取器：获取 projectName 作为 value
                ));
        return projectMap;
    }
}
