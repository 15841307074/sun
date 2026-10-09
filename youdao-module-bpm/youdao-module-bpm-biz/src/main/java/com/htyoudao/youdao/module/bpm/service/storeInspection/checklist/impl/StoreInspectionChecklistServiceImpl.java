package com.htyoudao.youdao.module.bpm.service.storeInspection.checklist.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.checklist.vo.*;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.checklist.StoreInspectionChecklistDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.checklist.StoreInspectionTypeDO;
import com.htyoudao.youdao.module.bpm.dal.mysql.storeInspection.checklist.StoreInspectionChecklistMapper;
import com.htyoudao.youdao.module.bpm.dal.mysql.storeInspection.checklist.StoreInspectionTypeMapper;
import com.htyoudao.youdao.module.bpm.service.storeInspection.checklist.StoreInspectionChecklistService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.bpm.enums.ErrorCodeConstants.*;

/**
 * 点检项 Service 接口
 *
 * @lbw
 */
@Service
public class StoreInspectionChecklistServiceImpl implements StoreInspectionChecklistService {

    @Resource
    private StoreInspectionChecklistMapper checklistMapper;

    @Resource
    private StoreInspectionTypeMapper typeMapper;

    @Override
    public Integer createType(StoreInspectionTypeReqVO storeInspectionTypeReqVO) {

        LambdaQueryWrapper<StoreInspectionTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StoreInspectionTypeDO::getTypeName, storeInspectionTypeReqVO.getTypeName());
        List<StoreInspectionTypeDO> storeInspectionTypeDOS = typeMapper.selectList(queryWrapper);

        if (CollectionUtil.isNotEmpty(storeInspectionTypeDOS)){
            throw exception(TYPE_NAME_UNIQUE_ERROR);
        }

        if (StringUtils.length(storeInspectionTypeReqVO.getTypeName()) > 20) {
            throw exception(TYPE_NAME_TOO_LARGE_ERROR);
        }

        StoreInspectionTypeDO bean = BeanUtils.toBean(storeInspectionTypeReqVO, StoreInspectionTypeDO.class);

        return typeMapper.insert(bean);
    }

    @Override
    public List<StoreInspectionTypeRespVO> getAllType(String typeName) {

        LambdaQueryWrapper<StoreInspectionTypeDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(typeName)){
            wrapper.like(StoreInspectionTypeDO::getTypeName, typeName);
        }
        List<StoreInspectionTypeDO> storeInspectionTypeDOS = typeMapper.selectList(wrapper);

        List<StoreInspectionTypeRespVO> result = BeanUtils.toBean(storeInspectionTypeDOS, StoreInspectionTypeRespVO.class);

        // 获取点检项分数综合
        if (CollectionUtil.isEmpty(result)){
            return List.of();
        }
        // 1. 提取所有 typeId
        List<Long> typeIds = result.stream()
                .map(StoreInspectionTypeRespVO::getTypeId)
                .toList();

        // 2. 一次性批量查询所有 typeId 的总分（核心优化：只查一次库）
        Map<Long, Integer> typeIdSumScoreMap = checklistMapper.selectList(
                        Wrappers.lambdaQuery(StoreInspectionChecklistDO.class)
                                .in(StoreInspectionChecklistDO::getTypeId, typeIds)
                                .select(StoreInspectionChecklistDO::getTypeId, StoreInspectionChecklistDO::getScore)
                ).stream()
                .collect(Collectors.groupingBy(
                        StoreInspectionChecklistDO::getTypeId,
                        Collectors.summingInt(StoreInspectionChecklistDO::getScore)
                ));

        // 3. 赋值总分
        result.forEach(ele -> {
            Integer sumScore = typeIdSumScoreMap.getOrDefault(ele.getTypeId(), 0);
            ele.setTypeScore(sumScore);
        });

        return result;
    }

    @Override
    public Integer deleteTypeById(Long typeId) {

        LambdaQueryWrapper<StoreInspectionChecklistDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StoreInspectionChecklistDO::getTypeId, typeId);
        List<StoreInspectionChecklistDO> storeInspectionChecklistDOS = checklistMapper.selectList(queryWrapper);

        if(CollectionUtil.isNotEmpty(storeInspectionChecklistDOS)){
            throw exception(TYPE_DELETE_ERROR);
        }

        LambdaQueryWrapper<StoreInspectionTypeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreInspectionTypeDO::getTypeId, typeId);
        return typeMapper.delete(wrapper);
    }

    @Override
    public Integer updateType(StoreInspectionTypeReqVO storeInspectionTypeReqVO) {

        if (StringUtils.isEmpty(storeInspectionTypeReqVO.getTypeId()) ||
                StringUtils.isEmpty(storeInspectionTypeReqVO.getTypeName())
        ){
            throw exception(INSPECTION_TYPE_ERROR);
        }

        LambdaUpdateWrapper<StoreInspectionTypeDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(StoreInspectionTypeDO::getTypeId, storeInspectionTypeReqVO.getTypeId());
        wrapper.set(StoreInspectionTypeDO::getTypeName, storeInspectionTypeReqVO.getTypeName());
        return typeMapper.update(wrapper);
    }

    @Override
    public Integer create(CheckListReqVO checkListReqVO) {

        checkCreateParams(checkListReqVO);

        LambdaQueryWrapper<StoreInspectionChecklistDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StoreInspectionChecklistDO::getTypeId, checkListReqVO.getTypeId());
        queryWrapper.eq(StoreInspectionChecklistDO::getTitle, checkListReqVO.getTitle());
        List<StoreInspectionChecklistDO> storeInspectionChecklistDOS = checklistMapper.selectList(queryWrapper);

        if (CollectionUtil.isNotEmpty(storeInspectionChecklistDOS)){
            throw exception(CHECKLIST_NAME_UNIQUE_ERROR);
        }

        Integer maxSort = Optional.ofNullable(
                        checklistMapper.selectOne(
                                new LambdaQueryWrapper<StoreInspectionChecklistDO>()
                                        .eq(StoreInspectionChecklistDO::getTypeId, checkListReqVO.getTypeId())
                                        .orderByDesc(StoreInspectionChecklistDO::getSort)
                                        .last("LIMIT 1")
                        )
                ).map(StoreInspectionChecklistDO::getSort)
                .orElse(0);

        StoreInspectionChecklistDO checklist = BeanUtils.toBean(checkListReqVO, StoreInspectionChecklistDO.class);
        checklist.setSort(maxSort + 1);

        return checklistMapper.insert(checklist);
    }

    @Override
    public Integer deleteChecklistById(Long checklistId) {
        LambdaQueryWrapper<StoreInspectionChecklistDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreInspectionChecklistDO::getChecklistId, checklistId);
        return checklistMapper.delete(wrapper);
    }

    @Override
    public Integer update(CheckListReqVO checkListReqVO) {
        checkCreateParams(checkListReqVO);

        StoreInspectionChecklistDO checklist = BeanUtils.toBean(checkListReqVO, StoreInspectionChecklistDO.class);

        return checklistMapper.updateById(checklist);
    }

    @Override
    public List<CheckListRespVO> getChecklistsByTypeId(Long typeId) {

        LambdaQueryWrapper<StoreInspectionChecklistDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StoreInspectionChecklistDO::getTypeId, typeId);
        queryWrapper.orderByAsc(StoreInspectionChecklistDO::getSort);
        List<StoreInspectionChecklistDO> storeInspectionChecklistDOS = checklistMapper.selectList(queryWrapper);

        return BeanUtils.toBean(storeInspectionChecklistDOS, CheckListRespVO.class);
    }

    @Override
    public PageResult<CheckListRespVO> getAllChecklists(CheckListsQueryReqVO checkListsQueryReqVO) {
        LambdaQueryWrapper<StoreInspectionChecklistDO> queryWrapper = new LambdaQueryWrapper<>();
        if (checkListsQueryReqVO.getTypeId() != null){
            queryWrapper.eq(StoreInspectionChecklistDO::getTypeId, checkListsQueryReqVO.getTypeId());
        }
        if (StringUtils.isNotBlank(checkListsQueryReqVO.getTitle())){
            queryWrapper.like(StoreInspectionChecklistDO::getTitle, checkListsQueryReqVO.getTitle());
        }

        queryWrapper.orderByDesc(StoreInspectionChecklistDO::getCreateTime);

        PageResult<StoreInspectionChecklistDO> storeInspectionChecklistDOPageResult = checklistMapper.selectPage(checkListsQueryReqVO, queryWrapper);

        return BeanUtils.toBean(storeInspectionChecklistDOPageResult, CheckListRespVO.class);
    }

    @Override
    public Integer sortChecklists(List<CheckListsSortReqVO> list) {
        if (CollectionUtil.isEmpty(list)){
            return 0;
        }

        int updateCount = 0;
        // 2. 循环批量更新每条数据的排序值
        for (CheckListsSortReqVO req : list) {
            // 构建更新条件：根据主键id更新
            LambdaUpdateWrapper<StoreInspectionChecklistDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(StoreInspectionChecklistDO::getTypeId, req.getTypeId())
                    .eq(StoreInspectionChecklistDO::getChecklistId, req.getChecklistId())
                    .set(StoreInspectionChecklistDO::getSort, req.getSort());

            // 执行更新
            checklistMapper.update(wrapper);
            updateCount++;
        }

        // 返回成功更新的条数
        return updateCount;
    }

    private void checkCreateParams(CheckListReqVO checkListReqVO) {
        // 必填
        if (checkListReqVO.getTypeId() == null || StringUtils.isBlank(checkListReqVO.getTypeName())) {
            throw exception(INSPECTION_TYPE_ERROR);
        }
        // 点检项标题
        if (StringUtils.length(checkListReqVO.getTitle()) > 200) {
            throw exception(CHECKLIST_TITLE_LENGTH_TOO_LARGE_ERROR);
        }
        // 提示长度
        if (StringUtils.length(checkListReqVO.getPrompt()) > 400) {
            throw exception(CHECKLIST_PROMPT_LENGTH_TOO_LARGE_ERROR);
        }
        if (StringUtils.isNotBlank(checkListReqVO.getImgState())) {
            checkImgState(checkListReqVO.getImgState());
        }

        // 点检标准最多3项
        if (StringUtils.isNotBlank(checkListReqVO.getInspectionStandard())
                && checkListReqVO.getInspectionStandard().split(",").length > 3) {
            throw exception(INSPECTION_STANDARD_TOO_LARGE_ERROR);
        }
    }

    private void checkImgState(String imgState) {
        if (StringUtils.isBlank(imgState)) {
            return;
        }

        List<String> list = Arrays.stream(imgState.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toList();

        // 核心规则
        if (list.contains("0") && list.size() > 1) {
            throw exception(CHECKLIST_IMG_STATE_ERROR);
        }
    }
}