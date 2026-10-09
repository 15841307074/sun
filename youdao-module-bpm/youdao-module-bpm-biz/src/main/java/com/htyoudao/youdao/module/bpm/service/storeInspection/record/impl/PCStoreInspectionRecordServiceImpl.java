package com.htyoudao.youdao.module.bpm.service.storeInspection.record.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.record.vo.StoreInspectionRecordQueryReqVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecord.StoreInspectionRecordDO;
import com.htyoudao.youdao.module.bpm.dal.mysql.storeInspection.record.PCStoreInspectionRecordMapper;
import com.htyoudao.youdao.module.bpm.service.storeInspection.record.PCStoreInspectionRecordService;
import com.htyoudao.youdao.module.system.api.dept.DeptOrgApi;
import com.htyoudao.youdao.module.system.api.orgstore.OrgStoreApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.bpm.enums.ErrorCodeConstants.STORE_INSPECTION_USER_DEPT_ERROR;
import static com.htyoudao.youdao.module.bpm.enums.ErrorCodeConstants.USER_DEPT_ERROR;
import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Service
public class PCStoreInspectionRecordServiceImpl implements PCStoreInspectionRecordService {

    @Resource
    private PCStoreInspectionRecordMapper pcStoreInspectionRecordMapper;

    @DubboReference
    private OrgStoreApi orgStoreApi;

    @DubboReference
    private StoreApi storeApi;

    @DubboReference
    private DeptOrgApi deptOrgApi;

    // 排序列表1升序 2降序
    public static final List<String> SORT_VALUES = List.of("asc", "desc");
    private static final List<String> ALLOW_SORT_COLUMNS = List.of(
            "actualScore",    // 实际得分
            "totalScore",     // 总分
            "scoreRate",      // 得分率
            "rewardAmount"    // 奖励金额
    );

    private static final String ASC = "asc";
    private static final String DESC = "desc";


    @Override
    public PageResult<StoreInspectionRecordDO> getStoreInspectionRecordPage(StoreInspectionRecordQueryReqVO pageReqVO) {

        // 获取用户可见的模板ID
        Long userId = SecurityFrameworkUtils.getLoginUserId();

        // 获取用户权限
//        CommonResult<List<Long>> commonResult = deptOrgApi.getUserIdsByDept();
        // 获取用户权限的门店集合
        List<Long> storeIdsByUser = storeApi.getStoreIdsByUser();

        // 获取所有可见的userId
        if (CollectionUtil.isEmpty(storeIdsByUser)) {
            throw exception(STORE_INSPECTION_USER_DEPT_ERROR);
        }
        LambdaQueryWrapper<StoreInspectionRecordDO> wrapper = new LambdaQueryWrapper<>();
        Long storeId = pageReqVO.getStoreId();
        Long orgId = pageReqVO.getOrgId();

        if (storeId != null){
            // 门店
            if (!storeIdsByUser.contains(storeId)){
                return PageResult.empty();
            }
            wrapper.eq(StoreInspectionRecordDO::getStoreId, storeId);
        }else if (orgId != null){
            // 组织
            CommonResult<List<Long>> result = orgStoreApi.selectByOrgStoreList(orgId);
            List<Long> storeIds = new ArrayList<>();
            if (result != null && CollectionUtil.isNotEmpty(result.getCheckedData())) {
                storeIds = result.getCheckedData();
                List<Long> intersectStoreIds = storeIdsByUser.stream()
                        .filter(storeIds::contains)
                        .toList();
                if (CollectionUtil.isEmpty(intersectStoreIds)){
                    return PageResult.empty();
                }
                wrapper.in(StoreInspectionRecordDO::getStoreId, intersectStoreIds);
            }else {
                return PageResult.empty();
            }
        }else {
            wrapper.in(StoreInspectionRecordDO::getStoreId, storeIdsByUser);
        }

        if (pageReqVO.getInspectorId() != null){
            wrapper.eq(StoreInspectionRecordDO::getInspectorId, pageReqVO.getInspectorId());
        }

        if (pageReqVO.getTemplateId() != null){
            wrapper.eq(StoreInspectionRecordDO::getTemplateId, pageReqVO.getTemplateId());
        }

        LocalDateTime start = null;
        LocalDateTime end = null;

        if (StringUtils.isNotBlank(pageReqVO.getStartTime())) {
            start = LocalDateTime.parse(pageReqVO.getStartTime(), DateTimeFormatter.ofPattern(FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND));
        }
        if (StringUtils.isNotBlank(pageReqVO.getEndTime())) {
            end = LocalDateTime.parse(pageReqVO.getEndTime(), DateTimeFormatter.ofPattern(FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND));
        }
        // 巡店时间

        if (start != null && end != null) {
            // 前后都传：区间查询
            wrapper.between(StoreInspectionRecordDO::getStartTime, start, end);
        } else if (start != null) {
            // 只传开始时间：>= 开始时间
            wrapper.ge(StoreInspectionRecordDO::getStartTime, start);
        } else if (end != null) {
            // 只传结束时间：<= 结束时间
            wrapper.le(StoreInspectionRecordDO::getStartTime, end);
        } else {
            // 都不传：默认查询【近半年】
            LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(12);
            wrapper.ge(StoreInspectionRecordDO::getStartTime, sixMonthsAgo);
        }

        // 巡店状态
        Integer status = pageReqVO.getStatus();
        LocalDateTime now = LocalDateTime.now();
        if (status != null){
            switch (status){
                case 1:
                    wrapper.eq(StoreInspectionRecordDO::getStatus, pageReqVO.getStatus());
                    break;
                case 0:
                    wrapper.eq(StoreInspectionRecordDO::getStatus, pageReqVO.getStatus());
                    wrapper.gt(StoreInspectionRecordDO::getEndTime, now);
                    break;
                case 2:
                    // endTime小于now status =1  或者status=2
                    wrapper.and(w -> {
                        w.eq(StoreInspectionRecordDO::getStatus, 2)
                                .or()
                                .eq(StoreInspectionRecordDO::getStatus, 0)
                                .lt(StoreInspectionRecordDO::getEndTime, now);
                    });
                    break;
                default:
                    break;
            }

        }

        String sortBy = pageReqVO.getSortBy();
        String sortOrder = pageReqVO.getSortOrder();
        if (StringUtils.isNotBlank(sortBy) && ALLOW_SORT_COLUMNS.contains(sortBy)) {
            boolean isAsc = ASC.equalsIgnoreCase(sortOrder);

            switch (sortBy) {
                case "actualScore":
                    wrapper.orderBy(true, isAsc, StoreInspectionRecordDO::getActualScore);
                    break;
                case "totalScore":
                    wrapper.orderBy(true, isAsc, StoreInspectionRecordDO::getTotalScore);
                    break;
                case "scoreRate":
                    wrapper.orderBy(true, isAsc, StoreInspectionRecordDO::getScoreRate);
                    break;
                case "rewardAmount":
                    wrapper.orderBy(true, isAsc, StoreInspectionRecordDO::getRewardAmount);
                    break;
            }
        }

        // 默认排序（必须放最后）
        wrapper.orderByDesc(StoreInspectionRecordDO::getCreateTime);
        PageResult<StoreInspectionRecordDO> storeInspectionRecordDOPageResult = pcStoreInspectionRecordMapper.selectPage(pageReqVO, wrapper);

        // 2. DO 转 VO
        List<StoreInspectionRecordDO> respList = new ArrayList<>();
        for (StoreInspectionRecordDO recordDO : storeInspectionRecordDOPageResult.getList()) {
            StoreInspectionRecordDO respVO = new StoreInspectionRecordDO();
            // 拷贝基础属性（你自己用 BeanUtils 或 mapstruct 都行）
            BeanUtils.copyProperties(recordDO, respVO);

            // 3. 核心：展示状态处理 —— 未完成但已超时 → 前端显示已失效（2）
            if (!Objects.equals(recordDO.getStatus(), 1)  // 原状态是进行中
                    && recordDO.getEndTime() != null       // 结束时间不为空
                    && recordDO.getEndTime().isBefore(LocalDateTime.now())) { // 已超时
                respVO.setStatus(2); // 前端展示：已失效/已结束
            }

            respList.add(respVO);
        }

        // 4. 封装返回分页结果
        PageResult<StoreInspectionRecordDO> resultPage = new PageResult<>();
        resultPage.setList(respList);
        resultPage.setTotal(storeInspectionRecordDOPageResult.getTotal());

        return resultPage;
    }


}
