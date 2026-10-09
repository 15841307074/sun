package com.htyoudao.youdao.module.bpm.service.storeinspectionrecord;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo.*;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecorditem.vo.StoreInspectionRecordItemRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo.InspectionItemStatRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo.InspectionOverviewRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo.InspectionReportReqVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo.StoreInspectionIntervalRespVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.template.TemplateChecklistDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecord.StoreInspectionRecordDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecorditem.StoreInspectionRecordItemDO;
import com.htyoudao.youdao.module.bpm.dal.mysql.storeInspection.template.TemplateChecklistMapper;
import com.htyoudao.youdao.module.bpm.dal.mysql.storeinspectionrecord.StoreInspectionRecordMapper;
import com.htyoudao.youdao.module.bpm.service.storeinspectionrecorditem.StoreInspectionRecordItemService;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.bpm.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.bpm.util.InspectionRecordItemRuleValidationUtil.*;

/**
 * 巡店记录主 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class StoreInspectionRecordServiceImpl extends ServiceImpl<StoreInspectionRecordMapper, StoreInspectionRecordDO> implements StoreInspectionRecordService {

    @Resource
    private StoreInspectionRecordMapper storeInspectionRecordMapper;

    @Resource
    @Lazy  // 避免循环依赖
    private StoreInspectionRecordItemService storeInspectionRecordItemService;

    @Resource
    private TemplateChecklistMapper templateChecklistMapper;

    @DubboReference
    private StoreApi storeApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStoreInspectionRecord(StoreInspectionRecordSaveReqVO createReqVO) {
        // 插入
        StoreInspectionRecordDO storeInspectionRecord = BeanUtils.toBean(createReqVO, StoreInspectionRecordDO.class);
        storeInspectionRecord.setStartTime(LocalDateTime.now());
        storeInspectionRecord.setEndTime(LocalDateTime.now().plusDays(1));
        storeInspectionRecordMapper.insert(storeInspectionRecord);

        // 2. 根据 templateId 查询模板点检项
        List<TemplateChecklistDO> templateChecklistList = templateChecklistMapper.selectList(
                new QueryWrapper<TemplateChecklistDO>()
                        .eq("template_id", createReqVO.getTemplateId())
                        .orderByAsc("sort")
        );
        Long id = storeInspectionRecord.getId();
        // 3. 生成巡店记录明细快照
        if (CollectionUtil.isNotEmpty(templateChecklistList)) {
            List<StoreInspectionRecordItemDO> recordItemList = templateChecklistList.stream()
                    .map(templateItem -> {
                        StoreInspectionRecordItemDO itemDO = new StoreInspectionRecordItemDO();
                        itemDO.setRecordId(id);
                        itemDO.setTypeId(templateItem.getTypeId());
                        itemDO.setTypeName(templateItem.getTypeName());
                        itemDO.setChecklistId(templateItem.getChecklistId());
                        itemDO.setTitleSnap(templateItem.getTitle());
                        itemDO.setPromptSnap(templateItem.getPrompt());
                        itemDO.setMaxScoreSnap(templateItem.getScore());
                        itemDO.setSort(templateItem.getSort());
                        itemDO.setTypeSort(templateItem.getTypeSort());

                        // 默认未检查
                        itemDO.setActualStatus(3); //  默认未检查
                        itemDO.setActualScore(0); // 默认分数为0
                        itemDO.setRewardAmount(BigDecimal.ZERO); // 默认奖金为0
                        // 模板规则快照
                        itemDO.setImgRule(templateItem.getImgRule());
                        itemDO.setDescriptionRule(templateItem.getDescriptionRule());
                        itemDO.setInapplicabilityRule(templateItem.getInapplicabilityRule());
                        itemDO.setRewardPunishmentRule(templateItem.getRewardPunishmentRule());

                        // 初始化结果字段
                        itemDO.setActualScore(0);
                        itemDO.setIsRectified(false);

                        return itemDO;
                    })
                    .collect(Collectors.toList());

            if (!recordItemList.isEmpty()) {
                storeInspectionRecordItemService.saveBatch(recordItemList);
            }
            int totalScore = templateChecklistList.stream()
                    .map(TemplateChecklistDO::getScore)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();
            storeInspectionRecord = new StoreInspectionRecordDO();
            storeInspectionRecord.setId(id);
            storeInspectionRecord.setTotalScore(totalScore);
            storeInspectionRecord.setActualScore(0);
            storeInspectionRecord.setScoreRate(BigDecimal.ZERO);
            storeInspectionRecord.setItemCount(templateChecklistList.size());
            storeInspectionRecord.setQualifiedCount(0);
            storeInspectionRecord.setUnqualifiedCount(0);
            storeInspectionRecord.setNotApplicableCount(0);
            storeInspectionRecordMapper.updateById(storeInspectionRecord);
        }

        // 返回
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStoreInspectionRecord(StoreInspectionRecordSaveReqVO updateReqVO) {
        // 校验存在
        //validateStoreInspectionRecordExists(updateReqVO.getId());
        StoreInspectionRecordDO storeInspectionRecord = storeInspectionRecordMapper.selectById(updateReqVO.getId());
        if (storeInspectionRecord == null) {
            throw exception(STORE_INSPECTION_RECORD_NOT_EXISTS);
        }
        // 是否过期

        if(Objects.equals(storeInspectionRecord.getStatus(), 1)){
            throw exception(STORE_INSPECTION_RECORD_ALREADY_FINISHED);
        }
        LocalDateTime endTime = storeInspectionRecord.getEndTime();
        if(endTime.isBefore(LocalDateTime.now())){
            throw exception(STORE_INSPECTION_RECORD_OVER_TIME);
        }


        Long id = updateReqVO.getId();
        if(Objects.equals(storeInspectionRecord.getTemplateId(), updateReqVO.getTemplateId())){
            storeInspectionRecord = BeanUtils.toBean(updateReqVO, StoreInspectionRecordDO.class);
            storeInspectionRecord.setId(id);
            storeInspectionRecordMapper.updateById(storeInspectionRecord);
            return;
        }

        storeInspectionRecordItemService.remove(new LambdaQueryWrapperX<StoreInspectionRecordItemDO>()
                .eq(StoreInspectionRecordItemDO::getRecordId, updateReqVO.getId()));

        // 2. 根据 templateId 查询模板点检项
        List<TemplateChecklistDO> templateChecklistList = templateChecklistMapper.selectList(
                new QueryWrapper<TemplateChecklistDO>()
                        .eq("template_id", updateReqVO.getTemplateId())
                        .orderByAsc("sort")
        );

        // 3. 生成巡店记录明细快照
        if (CollectionUtil.isNotEmpty(templateChecklistList)) {
            List<StoreInspectionRecordItemDO> recordItemList = templateChecklistList.stream()
                    .map(templateItem -> {
                        StoreInspectionRecordItemDO itemDO = new StoreInspectionRecordItemDO();
                        itemDO.setRecordId(id);
                        itemDO.setTypeId(templateItem.getTypeId());
                        itemDO.setTypeName(templateItem.getTypeName());
                        itemDO.setChecklistId(templateItem.getChecklistId());
                        itemDO.setTitleSnap(templateItem.getTitle());
                        itemDO.setPromptSnap(templateItem.getPrompt());
                        itemDO.setMaxScoreSnap(templateItem.getScore());
                        itemDO.setSort(templateItem.getSort());

                        // 默认未检查
                        itemDO.setActualStatus(3); //  默认未检查
                        itemDO.setActualScore(0); // 默认分数为0
                        itemDO.setRewardAmount(BigDecimal.ZERO); // 默认奖金为0
                        // 模板规则快照
                        itemDO.setImgRule(templateItem.getImgRule());
                        itemDO.setDescriptionRule(templateItem.getDescriptionRule());
                        itemDO.setInapplicabilityRule(templateItem.getInapplicabilityRule());
                        itemDO.setRewardPunishmentRule(templateItem.getRewardPunishmentRule());

                        // 初始化结果字段
                        itemDO.setActualScore(0);
                        itemDO.setIsRectified(false);

                        return itemDO;
                    })
                    .collect(Collectors.toList());

            if (!recordItemList.isEmpty()) {
                storeInspectionRecordItemService.saveBatch(recordItemList);
            }
            int totalScore = templateChecklistList.stream()
                    .map(TemplateChecklistDO::getScore)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();
            storeInspectionRecord = BeanUtils.toBean(updateReqVO, StoreInspectionRecordDO.class);
            storeInspectionRecord.setId(id);
            storeInspectionRecord.setTotalScore(totalScore);
            storeInspectionRecord.setActualScore(0);
            storeInspectionRecord.setScoreRate(BigDecimal.ZERO);
            storeInspectionRecord.setItemCount(templateChecklistList.size());
            storeInspectionRecord.setQualifiedCount(0);
            storeInspectionRecord.setUnqualifiedCount(0);
            storeInspectionRecord.setNotApplicableCount(0);
            storeInspectionRecordMapper.updateById(storeInspectionRecord);
        }

//        // 更新
//        StoreInspectionRecordDO updateObj = BeanUtils.toBean(updateReqVO, StoreInspectionRecordDO.class);
//        storeInspectionRecordMapper.updateById(updateObj);
    }

    @Override
    public void overInspectionRecord(OverInspectionRecordReqVO reqVO) {
        // 1. 校验主记录存在
        StoreInspectionRecordDO recordDO = storeInspectionRecordMapper.selectById(reqVO.getId());
        if (recordDO == null) {
            throw exception(STORE_INSPECTION_RECORD_NOT_EXISTS);
        }

        // 2. 已完成则直接返回（避免重复完成）
        if (Objects.equals(recordDO.getStatus(), 1)) {
            return;
        }

        // 是否过期
        LocalDateTime endTime = recordDO.getEndTime();
        if(endTime.isBefore(LocalDateTime.now())){
            throw exception(STORE_INSPECTION_RECORD_OVER_TIME);
        }

        String storePic = recordDO.getStorePic();
        if(StringUtils.isEmpty(storePic)){
            throw exception(STORE_INSPECTION_RECORD_NO_PIC_ERROR);
        }

        // 3. 查询该巡店记录下的所有明细
        List<StoreInspectionRecordItemDO> itemList = storeInspectionRecordItemService.lambdaQuery()
                .eq(StoreInspectionRecordItemDO::getRecordId, reqVO.getId())
                .list();

        // 4. 没有明细，不允许完成
        if (CollectionUtil.isEmpty(itemList)) {
            throw exception(STORE_INSPECTION_RECORD_ITEM_NULL_ERROR);
        }

        // 5. 只要存在未检查项（actualStatus 为 null 或 3），就不允许完成
        boolean hasUncheckedItem = itemList.stream()
                .anyMatch(item -> item.getActualStatus() == null || Objects.equals(item.getActualStatus(), 3));

        if (hasUncheckedItem) {
            throw exception(STORE_INSPECTION_RECORD_ITEM_NOT_FINISH);
        }

        // 最后判断是否在门店附近
        if(!storeApi.judgeDistance(reqVO.getLongitude(),reqVO.getLatitude(),recordDO.getStoreId(),1.0)){
            throw exception(STORE_INSPECTION_DISTANCE_ERROR);
        }

        // 6. 全部完成，更新主表状态
        StoreInspectionRecordDO updateObj = new StoreInspectionRecordDO();
        updateObj.setId(reqVO.getId());
        // 1 已完成
        updateObj.setStatus(1);
        updateObj.setOverTime(LocalDateTime.now());
        storeInspectionRecordMapper.updateById(updateObj);
    }

    @Override
    public void deleteStoreInspectionRecord(Long id) {
        // 校验存在
        validateStoreInspectionRecordExists(id);
        // 删除
        storeInspectionRecordMapper.deleteById(id);
        LambdaQueryWrapper<StoreInspectionRecordItemDO> lqw = new LambdaQueryWrapper<StoreInspectionRecordItemDO>()
                .eq(StoreInspectionRecordItemDO::getRecordId, id);
        storeInspectionRecordItemService.remove(lqw);
    }

    private void validateStoreInspectionRecordExists(Long id) {
        StoreInspectionRecordDO storeInspectionRecordDO = storeInspectionRecordMapper.selectById(id);
        if (storeInspectionRecordDO == null) {
            throw exception(STORE_INSPECTION_RECORD_NOT_EXISTS);
        }
        if(Objects.equals(storeInspectionRecordDO.getStatus(),1)){
            throw exception(STORE_INSPECTION_DELETE_ERROR);
        }
    }

    @Override
    public StoreInspectionRecordDetailRespVO getStoreInspectionRecord(StoreInspectionRecordDetailReqVO reqVO) {
        Long recordId = reqVO.getId();
        Integer actualStatus = reqVO.getActualStatus();

        // 1. 校验主记录存在
        StoreInspectionRecordDO recordDO = storeInspectionRecordMapper.selectById(recordId);
        if (recordDO == null) {
            throw exception(STORE_INSPECTION_RECORD_NOT_EXISTS);
        }

        // 2. 查询该巡店记录下的所有点检明细
        List<StoreInspectionRecordItemDO> allItemList = storeInspectionRecordItemService.lambdaQuery()
                .eq(StoreInspectionRecordItemDO::getRecordId, recordId)
                .orderByAsc(StoreInspectionRecordItemDO::getTypeSort)
                .orderByAsc(StoreInspectionRecordItemDO::getTypeId)
                .orderByAsc(StoreInspectionRecordItemDO::getSort)
                .orderByAsc(StoreInspectionRecordItemDO::getId)
                .list();

        StoreInspectionRecordDetailRespVO respVO = new StoreInspectionRecordDetailRespVO();
        BeanUtils.copyProperties(recordDO, respVO);
        respVO.setRecordId(recordId);

        // 3. 展示状态：未完成但已超时，前端显示已失效
        if (!Objects.equals(recordDO.getStatus(), 1)
                && recordDO.getEndTime() != null
                && recordDO.getEndTime().isBefore(LocalDateTime.now())) {
            respVO.setStatus(2);
        }

        // 4. 如果没有明细，直接返回主表汇总
        if (CollectionUtil.isEmpty(allItemList)) {
            respVO.setTotalScore(recordDO.getTotalScore() == null ? 0 : recordDO.getTotalScore());
            respVO.setActualScore(recordDO.getActualScore() == null ? 0 : recordDO.getActualScore());
            respVO.setScoreRate(recordDO.getScoreRate() == null ? BigDecimal.ZERO : recordDO.getScoreRate());
            respVO.setItemCount(recordDO.getItemCount() == null ? 0 : recordDO.getItemCount());

            // 注意：按你之前的口径，不适用也算合格
            Integer qualifiedCount = recordDO.getQualifiedCount();
            Integer notApplicableCount = recordDO.getNotApplicableCount();
            if (qualifiedCount == null) {
                qualifiedCount = 0;
            }
            if (notApplicableCount == null) {
                notApplicableCount = 0;
            }

            respVO.setQualifiedCount(qualifiedCount);
            respVO.setUnqualifiedCount(recordDO.getUnqualifiedCount() == null ? 0 : recordDO.getUnqualifiedCount());
            respVO.setNotApplicableCount(notApplicableCount);
            respVO.setRewardAmount(recordDO.getRewardAmount() == null ? BigDecimal.ZERO : recordDO.getRewardAmount());
            respVO.setTypeList(Collections.emptyList());
            return respVO;
        }

        // 5. 顶部统计：按全量明细计算（不是按 actualStatus 过滤后的结果）
        int totalScore = allItemList.stream()
                .map(StoreInspectionRecordItemDO::getMaxScoreSnap)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int actualScore = allItemList.stream()
                .map(StoreInspectionRecordItemDO::getActualScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int itemCount = allItemList.size();

        // 按你之前确认的口径：合格(1) + 不适用(2) 都算合格
        int qualifiedCount = (int) allItemList.stream()
                .filter(item -> Objects.equals(item.getActualStatus(), 1))
                .count();

        int checkCount = (int) allItemList.stream()
                .filter(item -> Objects.equals(item.getActualStatus(), 1)
                        || Objects.equals(item.getActualStatus(), 2))
                .count();

        int unqualifiedCount = (int) allItemList.stream()
                .filter(item -> Objects.equals(item.getActualStatus(), 0))
                .count();

        int notApplicableCount = (int) allItemList.stream()
                .filter(item -> Objects.equals(item.getActualStatus(), 2))
                .count();

        BigDecimal totalRewardAmount = allItemList.stream()
                .map(StoreInspectionRecordItemDO::getRewardAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        respVO.setTotalScore(totalScore);
        respVO.setActualScore(actualScore);
        respVO.setScoreRate(calculateRate(actualScore, totalScore));
        respVO.setItemCount(itemCount);
        respVO.setQualifiedCount(qualifiedCount);
        respVO.setUnqualifiedCount(unqualifiedCount);
        respVO.setNotApplicableCount(notApplicableCount);
        respVO.setRewardAmount(totalRewardAmount);

        // 6. 明细筛选：0 不合格、1 合格、2 不适用；不传或非法值则查全部
        boolean needFilter = Objects.equals(actualStatus, 0)
                || Objects.equals(actualStatus, 1)
                || Objects.equals(actualStatus, 2);

        // 7. 按分类分组，分类头部统计按“全量明细”算，detail 按筛选结果返回
        LinkedHashMap<String, List<StoreInspectionRecordItemDO>> groupedMap = allItemList.stream()
                .collect(Collectors.groupingBy(
                        this::buildTypeGroupKey,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<StoreInspectionRecordTypeGroupRespVO> typeList = groupedMap.values().stream()
                .sorted(Comparator
                        .comparing((List<StoreInspectionRecordItemDO> group) -> group.get(0).getTypeSort(),
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(group -> group.get(0).getTypeId(),
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(group -> group.get(0).getTypeName(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .map(groupAllItems -> {
                    StoreInspectionRecordItemDO first = groupAllItems.get(0);

                    List<StoreInspectionRecordItemDO> groupDetailList = needFilter
                            ? groupAllItems.stream()
                            .filter(item -> Objects.equals(item.getActualStatus(), actualStatus))
                            .collect(Collectors.toList())
                            : groupAllItems;

                    // 过滤后该分类没有数据，就不返回该分类
                    if (CollectionUtil.isEmpty(groupDetailList)) {
                        return null;
                    }

                    int groupTotalScore = groupAllItems.stream()
                            .map(StoreInspectionRecordItemDO::getMaxScoreSnap)
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .sum();

                    int groupActualScore = groupAllItems.stream()
                            .map(StoreInspectionRecordItemDO::getActualScore)
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .sum();

                    int checkedCount = (int) groupAllItems.stream()
                            .filter(item -> item.getActualStatus() != null
                                    && !Objects.equals(item.getActualStatus(), 3)
                                    && !Objects.equals(item.getActualStatus(), 0))
                            .count();

                    BigDecimal groupRewardAmount = groupAllItems.stream()
                            .map(StoreInspectionRecordItemDO::getRewardAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    StoreInspectionRecordTypeGroupRespVO groupVO = new StoreInspectionRecordTypeGroupRespVO();
                    groupVO.setTypeId(first.getTypeId());
                    groupVO.setTypeName(first.getTypeName());
                    groupVO.setCheckedCount(checkedCount);
                    groupVO.setTotalCount(groupAllItems.size());
                    groupVO.setActualScore(groupActualScore);
                    groupVO.setTotalScore(groupTotalScore);
                    groupVO.setScoreRate(calculateRate(groupActualScore, groupTotalScore));
                    groupVO.setRewardAmount(groupRewardAmount);
                    groupVO.setStorePatrolPosition(recordDO.getStorePatrolPosition());
                    groupVO.setDetail(BeanUtils.toBean(groupDetailList, StoreInspectionRecordItemRespVO.class));
                    return groupVO;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        respVO.setTypeList(typeList);

        List<InspectionTypeStatRespVO> statList = new ArrayList<>();

        InspectionTypeStatRespVO totalStat = new InspectionTypeStatRespVO();
        totalStat.setTypeName("总点检数");
        totalStat.setCheckedCount(checkCount);
        totalStat.setTotalCount(itemCount);
        totalStat.setActualScore(actualScore);
        totalStat.setTotalScore(totalScore);
        totalStat.setScoreRate(calculateRate(actualScore, totalScore));
        statList.add(totalStat);

        for (StoreInspectionRecordTypeGroupRespVO group : typeList) {
            InspectionTypeStatRespVO stat = new InspectionTypeStatRespVO();
            stat.setTypeName(group.getTypeName());
            stat.setCheckedCount(group.getCheckedCount());
            stat.setTotalCount(group.getTotalCount());
            stat.setActualScore(group.getActualScore());
            stat.setTotalScore(group.getTotalScore());
            stat.setScoreRate(group.getScoreRate());
            statList.add(stat);
        }

        respVO.setStatList(statList);

        return respVO;
    }

    private String buildTypeGroupKey(StoreInspectionRecordItemDO item) {
        if (item.getTypeId() != null) {
            return "ID_" + item.getTypeId();
        }
        return "NAME_" + Objects.toString(item.getTypeName(), "");
    }


    @Override
    public StoreInspectionRecordDetailRespVO getItemByRecordId(Long recordId) {

        // 1. 校验记录存在
        StoreInspectionRecordDO recordDO = storeInspectionRecordMapper.selectById(recordId);
        if(recordDO == null){
            throw exception(STORE_INSPECTION_RECORD_NOT_EXISTS);
        }

        // 2. 查询主记录
//        LocalDateTime endTime = recordDO.getEndTime();
//        if(endTime.isBefore(LocalDateTime.now())){
//            throw exception(STORE_INSPECTION_RECORD_OVER_TIME);
//        }

        // 3. 查询该记录下的点检快照明细
        List<StoreInspectionRecordItemDO> itemDOList = storeInspectionRecordItemService.lambdaQuery()
                .eq(StoreInspectionRecordItemDO::getRecordId, recordId)
                .orderByAsc(StoreInspectionRecordItemDO::getTypeSort)
                .orderByAsc(StoreInspectionRecordItemDO::getTypeId)
                .orderByAsc(StoreInspectionRecordItemDO::getSort)
                .orderByAsc(StoreInspectionRecordItemDO::getId)
                .list();

        StoreInspectionRecordDetailRespVO respVO = new StoreInspectionRecordDetailRespVO();
        BeanUtils.copyProperties(recordDO, respVO);
        respVO.setRecordId(recordId);

        // 没有明细时，直接返回主记录上的汇总数据
        if (CollectionUtil.isEmpty(itemDOList)) {
            respVO.setTotalScore(recordDO.getTotalScore() == null ? 0 : recordDO.getTotalScore());
            respVO.setActualScore(recordDO.getActualScore() == null ? 0 : recordDO.getActualScore());
            respVO.setScoreRate(recordDO.getScoreRate() == null ? BigDecimal.ZERO : recordDO.getScoreRate());
            respVO.setItemCount(recordDO.getItemCount() == null ? 0 : recordDO.getItemCount());
            respVO.setQualifiedCount(recordDO.getQualifiedCount() == null ? 0 : recordDO.getQualifiedCount());
            respVO.setUnqualifiedCount(recordDO.getUnqualifiedCount() == null ? 0 : recordDO.getUnqualifiedCount());
            respVO.setNotApplicableCount(recordDO.getNotApplicableCount() == null ? 0 : recordDO.getNotApplicableCount());
            respVO.setTypeList(Collections.emptyList());
            return respVO;
        }

        // 4. 顶部汇总：按明细实时计算
        int totalScore = itemDOList.stream()
                .map(StoreInspectionRecordItemDO::getMaxScoreSnap)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int actualScore = itemDOList.stream()
                .map(StoreInspectionRecordItemDO::getActualScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int itemCount = itemDOList.size();

        int qualifiedCount = (int) itemDOList.stream()
                .filter(item -> Objects.equals(item.getActualStatus(), 1))
                .count();

        int unqualifiedCount = (int) itemDOList.stream()
                .filter(item -> Objects.equals(item.getActualStatus(), 0))
                .count();

        int notApplicableCount = (int) itemDOList.stream()
                .filter(item -> Objects.equals(item.getActualStatus(), 2))
                .count();

        respVO.setTotalScore(totalScore);
        respVO.setActualScore(actualScore);
        respVO.setScoreRate(calculateRate(actualScore, totalScore));
        respVO.setItemCount(itemCount);
        respVO.setQualifiedCount(qualifiedCount);
        respVO.setUnqualifiedCount(unqualifiedCount);
        respVO.setNotApplicableCount(notApplicableCount);

        // 5. 按大类分组，左边那一列就是 typeName
        LinkedHashMap<Long, List<StoreInspectionRecordItemDO>> groupedMap = itemDOList.stream()
                .collect(Collectors.groupingBy(
                        StoreInspectionRecordItemDO::getTypeId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<StoreInspectionRecordTypeGroupRespVO> typeList = groupedMap.values().stream()
                .sorted(Comparator
                        .comparing((List<StoreInspectionRecordItemDO> group) -> group.get(0).getTypeSort(),
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(group -> group.get(0).getTypeId(),
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(group -> group.get(0).getTypeName(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .map(group -> {
                    StoreInspectionRecordItemDO first = group.get(0);

                    int groupTotalScore = group.stream()
                            .map(StoreInspectionRecordItemDO::getMaxScoreSnap)
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .sum();

                    int groupActualScore = group.stream()
                            .map(StoreInspectionRecordItemDO::getActualScore)
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .sum();

                    int checkedCount = (int) group.stream()
                            .filter(item -> item.getActualStatus() != null
                                    && item.getActualStatus() != 3)
                            .count();

                    StoreInspectionRecordTypeGroupRespVO groupVO = new StoreInspectionRecordTypeGroupRespVO();
                    groupVO.setTypeId(first.getTypeId());
                    groupVO.setTypeName(first.getTypeName());
                    groupVO.setCheckedCount(checkedCount);
                    groupVO.setTotalCount(group.size());
                    groupVO.setActualScore(groupActualScore);
                    groupVO.setTotalScore(groupTotalScore);
                    groupVO.setScoreRate(calculateRate(groupActualScore, groupTotalScore));
                    groupVO.setDetail(BeanUtils.toBean(group, StoreInspectionRecordItemRespVO.class));
                    return groupVO;
                })
                .collect(Collectors.toList());

        respVO.setTypeList(typeList);
        return respVO;
    }

    private BigDecimal calculateRate(int actualScore, int totalScore) {
        if (totalScore <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(actualScore)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalScore), 2, RoundingMode.HALF_UP);
    }

    @Override
    public PageResult<StoreInspectionRecordDO> getStoreInspectionRecordPage(StoreInspectionRecordPageReqVO pageReqVO) {
        LambdaQueryWrapperX<StoreInspectionRecordDO> storeInspectionRecordDOLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
        List<Long> storeIds = new ArrayList<>();
        if(CollectionUtil.isEmpty(pageReqVO.getStoreIds())){
            storeIds = storeApi.getStoreIdsByUser();
            if(CollectionUtil.isEmpty(storeIds)){
                return PageResult.empty();
            }
            storeInspectionRecordDOLambdaQueryWrapperX.in(StoreInspectionRecordDO::getStoreId, storeIds);
        }else {
            storeInspectionRecordDOLambdaQueryWrapperX.in(StoreInspectionRecordDO::getStoreId, pageReqVO.getStoreIds());
        }

        //Long userId = SecurityFrameworkUtils.getLoginUserId();
        if(CollectionUtil.isNotEmpty(pageReqVO.getInspectorIds())){
            storeInspectionRecordDOLambdaQueryWrapperX.in(StoreInspectionRecordDO::getInspectorId, pageReqVO.getInspectorIds());
        }

        Integer status = pageReqVO.getStatus();
        LocalDateTime now = LocalDateTime.now();

        if (Objects.equals(status, 0)) { // 进行中
            storeInspectionRecordDOLambdaQueryWrapperX.eq(StoreInspectionRecordDO::getStatus, 0);
            storeInspectionRecordDOLambdaQueryWrapperX.ge(StoreInspectionRecordDO::getEndTime, now);
        } else if (Objects.equals(status, 1)) { // 已完成
            storeInspectionRecordDOLambdaQueryWrapperX.eq(StoreInspectionRecordDO::getStatus, 1);
        } else if (Objects.equals(status, 2)) { // 已失效
            storeInspectionRecordDOLambdaQueryWrapperX.eq(StoreInspectionRecordDO::getStatus, 0);
            storeInspectionRecordDOLambdaQueryWrapperX.lt(StoreInspectionRecordDO::getEndTime, now);
        }


        storeInspectionRecordDOLambdaQueryWrapperX.likeIfPresent(StoreInspectionRecordDO::getStoreName, pageReqVO.getStoreName());
        storeInspectionRecordDOLambdaQueryWrapperX.eqIfPresent(StoreInspectionRecordDO::getTemplateId, pageReqVO.getTemplateId());
        storeInspectionRecordDOLambdaQueryWrapperX.betweenIfPresent(StoreInspectionRecordDO::getStartTime, pageReqVO.getStartTime(),pageReqVO.getEndTime());
        storeInspectionRecordDOLambdaQueryWrapperX.orderByDesc(StoreInspectionRecordDO::getStartTime);
        PageResult<StoreInspectionRecordDO> storeInspectionRecordDOPageResult = storeInspectionRecordMapper.selectPage2(pageReqVO,storeInspectionRecordDOLambdaQueryWrapperX);
        List<StoreInspectionRecordDO> list = storeInspectionRecordDOPageResult.getList();
        list.forEach(record -> {
            Integer recordStatus = record.getStatus();
            LocalDateTime endTime = record.getEndTime();
            if (!Objects.equals(recordStatus, 1) && endTime != null && endTime.isBefore(now)) {
                record.setStatus(2);
            }
        });

        return storeInspectionRecordDOPageResult;
    }

    @Override
    public PageResult<InspectionOverviewRespVO> getInspectionOverview(InspectionReportReqVO reqVO) {
        List<StoreInspectionRecordDO> recordList = listReportRecords(reqVO);
        if (CollectionUtil.isEmpty(recordList)) {
            return PageResult.empty();
        }

        LinkedHashMap<String, List<StoreInspectionRecordDO>> groupedMap = recordList.stream()
                .sorted(Comparator.comparing(this::resolveRecordTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(StoreInspectionRecordDO::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.groupingBy(
                        this::buildStoreGroupKey,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<InspectionOverviewRespVO> respList = groupedMap.values().stream()
                .map(group -> {
                    StoreInspectionRecordDO latest = group.get(0);
                    int inspectionCount = group.size();

                    int qualifiedCount = group.stream()
                            .map(StoreInspectionRecordDO::getQualifiedCount)
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .sum();

                    int unqualifiedCount = group.stream()
                            .map(StoreInspectionRecordDO::getUnqualifiedCount)
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .sum();

                    InspectionOverviewRespVO vo = new InspectionOverviewRespVO();
                    vo.setStoreId(latest.getStoreId());
                    vo.setStoreName(latest.getStoreName()); // 需要 InspectionOverviewRespVO 补 storeName 字段
                    vo.setInspectionCount(inspectionCount);
                    vo.setQualifiedCount(qualifiedCount);
                    vo.setUnqualifiedCount(unqualifiedCount);
                    vo.setAverageScore(averageInteger(group.stream()
                            .map(StoreInspectionRecordDO::getActualScore)
                            .collect(Collectors.toList())));
                    vo.setScoreRate(averageBigDecimal(group.stream()
                            .map(StoreInspectionRecordDO::getScoreRate)
                            .collect(Collectors.toList())));
                    return vo;
                })
                .sorted((o1, o2) -> {
                    // 准备主比较器
                    String sortBy = reqVO == null ? null : reqVO.getSortBy();
                    Integer sortOrder = reqVO == null ? null : reqVO.getSortOrder();
                    boolean desc = Objects.equals(sortOrder,1);

                    Comparator<InspectionOverviewRespVO> primary;

                    if (sortBy != null && !sortBy.trim().isEmpty()) {
                        String key = sortBy.trim();
                        switch (key) {
                            case "unqualifiedCount":
                                primary = Comparator.comparing(InspectionOverviewRespVO::getUnqualifiedCount,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "inspectionCount":
                                primary = Comparator.comparing(InspectionOverviewRespVO::getInspectionCount,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "qualifiedCount":
                                primary = Comparator.comparing(InspectionOverviewRespVO::getQualifiedCount,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "averageScore":
                                primary = Comparator.comparing(InspectionOverviewRespVO::getAverageScore,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "scoreRate":
                                primary = Comparator.comparing(InspectionOverviewRespVO::getScoreRate,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "storeName":
                                primary = Comparator.comparing(InspectionOverviewRespVO::getStoreName,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            default:
                                // 未识别的字段使用默认 comparator（与之前逻辑一致）
                                primary = Comparator.comparing(InspectionOverviewRespVO::getUnqualifiedCount,
                                        Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                                break;
                        }
                    } else {
                        // 默认排序：unqualifiedCount desc, inspectionCount desc, storeName asc
                        primary = Comparator.comparing(InspectionOverviewRespVO::getUnqualifiedCount,
                                Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                    }

                    if (sortBy != null && !sortBy.trim().isEmpty()) {
                        if (desc) {
                            primary = primary.reversed();
                        }
                    }

                    // 始终增加稳定的回退排序：inspectionCount desc, storeName asc
                    Comparator<InspectionOverviewRespVO> finalComp = primary
                            .thenComparing(InspectionOverviewRespVO::getInspectionCount,
                                    Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(InspectionOverviewRespVO::getStoreName,
                                    Comparator.nullsLast(Comparator.naturalOrder()));

                    return finalComp.compare(o1, o2);
                })
                .collect(Collectors.toList());

        return buildPageResult(respList, reqVO.getPageNo(), reqVO.getPageSize());
    }

    @Override
    public PageResult<InspectionItemStatRespVO> getInspectionItemStat(InspectionReportReqVO reqVO) {
        List<StoreInspectionRecordDO> recordList = listReportRecords(reqVO);
        if (CollectionUtil.isEmpty(recordList)) {
            return PageResult.empty();
        }

        List<Long> recordIds = recordList.stream()
                .map(StoreInspectionRecordDO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (CollectionUtil.isEmpty(recordIds)) {
            return PageResult.empty();
        }

        List<StoreInspectionRecordItemDO> itemList = storeInspectionRecordItemService.lambdaQuery()
                .in(StoreInspectionRecordItemDO::getRecordId, recordIds)
                .list();

        List<StoreInspectionRecordItemDO> checkedItemList = itemList.stream()
                .filter(item -> item.getActualStatus() != null && !Objects.equals(item.getActualStatus(), 3))// 过滤掉未检查和不合格的项
                .collect(Collectors.toList());
//        long count = checkedItemList.stream()
//                .filter(item -> Objects.equals(item.getActualStatus(), 0))
//                .count();
        if (CollectionUtil.isEmpty(checkedItemList)) {
            return PageResult.empty();
        }

        LinkedHashMap<String, List<StoreInspectionRecordItemDO>> groupedMap = checkedItemList.stream()
                .sorted(Comparator.comparing(StoreInspectionRecordItemDO::getSort, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(StoreInspectionRecordItemDO::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.groupingBy(
                        this::buildChecklistGroupKey,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<InspectionItemStatRespVO> respList = groupedMap.values().stream()
                .map(group -> {
                    StoreInspectionRecordItemDO first = group.get(0);
                    int checkCount = group.size();

                    int qualifiedCount = (int) group.stream()
                            .filter(item -> Objects.equals(item.getActualStatus(), 1)
                                    || Objects.equals(item.getActualStatus(), 2))
                            .count();

                    int unqualifiedCount = (int) group.stream()
                            .filter(item -> Objects.equals(item.getActualStatus(), 0))
                            .count();

                    InspectionItemStatRespVO vo = new InspectionItemStatRespVO();
                    vo.setChecklistTitle(first.getTitleSnap());
                    vo.setCheckCount(checkCount);
                    vo.setQualifiedCount(qualifiedCount);
                    vo.setUnqualifiedCount(unqualifiedCount);
                    vo.setUnqualifiedRate(calculateRate(unqualifiedCount, checkCount));
                    return vo;
                })
                // ----- 灵活排序改造位置 -----
                .sorted((o1, o2) -> {
                    String sortBy = reqVO == null ? null : reqVO.getSortBy();
                    Integer sortOrder = reqVO == null ? null : reqVO.getSortOrder();
                    boolean desc = Objects.equals(sortOrder,1); // 默认 0-升序，1-降序

                    Comparator<InspectionItemStatRespVO> primary;

                    if (sortBy != null && !sortBy.trim().isEmpty()) {
                        String key = sortBy.trim();
                        switch (key) {
                            case "unqualifiedCount":
                                primary = Comparator.comparing(InspectionItemStatRespVO::getUnqualifiedCount,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "checkCount":
                                primary = Comparator.comparing(InspectionItemStatRespVO::getCheckCount,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "qualifiedCount":
                                primary = Comparator.comparing(InspectionItemStatRespVO::getQualifiedCount,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "unqualifiedRate":
                                primary = Comparator.comparing(InspectionItemStatRespVO::getUnqualifiedRate,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "checklistTitle":
                                primary = Comparator.comparing(InspectionItemStatRespVO::getChecklistTitle,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            default:
                                // 默认未知字段
                                primary = Comparator.comparing(InspectionItemStatRespVO::getUnqualifiedCount,
                                        Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                                break;
                        }
                    } else {
                        // 没有传 sortBy 时的默认排序行为
                        primary = Comparator.comparing(InspectionItemStatRespVO::getUnqualifiedCount,
                                Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                    }

                    // 处理升降序
                    if (sortBy != null && !sortBy.trim().isEmpty()) {
                        if (desc) {
                            primary = primary.reversed();
                        }
                    }

                    // 增加稳定的后退比较（确保同样数据排序列一致）
                    Comparator<InspectionItemStatRespVO> finalComp = primary
                            .thenComparing(InspectionItemStatRespVO::getUnqualifiedRate,
                                    Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(InspectionItemStatRespVO::getCheckCount,
                                    Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(InspectionItemStatRespVO::getChecklistTitle,
                                    Comparator.nullsLast(Comparator.naturalOrder()));

                    return finalComp.compare(o1, o2);
                })
                // ---------------------------
                .collect(Collectors.toList());

        return buildPageResult(respList, reqVO.getPageNo(), reqVO.getPageSize());
    }

    @Override
    public PageResult<InspectionItemStatRespVO> getInspectionGroupStat(InspectionReportReqVO reqVO) {
        List<StoreInspectionRecordDO> recordList = listReportRecords(reqVO);
        if (CollectionUtil.isEmpty(recordList)) {
            return PageResult.empty();
        }

        List<Long> recordIds = recordList.stream()
                .map(StoreInspectionRecordDO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (CollectionUtil.isEmpty(recordIds)) {
            return PageResult.empty();
        }

        List<StoreInspectionRecordItemDO> itemList = storeInspectionRecordItemService.lambdaQuery()
                .in(StoreInspectionRecordItemDO::getRecordId, recordIds)
                .list();

        // 与 getInspectionItemStat 保持一致的“已检查项”筛选：过滤掉未检查（3）和不合格（0）
        List<StoreInspectionRecordItemDO> checkedItemList = itemList.stream()
                .filter(item -> item.getActualStatus() != null && !Objects.equals(item.getActualStatus(), 3))
                .collect(Collectors.toList());

        if (CollectionUtil.isEmpty(checkedItemList)) {
            return PageResult.empty();
        }

        // 按 typeName 分组，保持原有的排序（sort, id）
        LinkedHashMap<String, List<StoreInspectionRecordItemDO>> groupedMap = checkedItemList.stream()
                .sorted(Comparator.comparing(StoreInspectionRecordItemDO::getSort, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(StoreInspectionRecordItemDO::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.groupingBy(
                        item -> Objects.toString(item.getTypeName(), ""),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<InspectionItemStatRespVO> respList = groupedMap.values().stream()
                .map(group -> {
                    StoreInspectionRecordItemDO first = group.get(0);
                    int checkCount = group.size();

                    int qualifiedCount = (int) group.stream()
                            .filter(item -> Objects.equals(item.getActualStatus(), 1)
                                    || Objects.equals(item.getActualStatus(), 2))
                            .count();

                    int unqualifiedCount = (int) group.stream()
                            .filter(item -> Objects.equals(item.getActualStatus(), 0))
                            .count();

                    InspectionItemStatRespVO vo = new InspectionItemStatRespVO();
                    // 使用 checklistTitle 字段承载分组名称（保持和 getInspectionItemStat 返回结构一致）
                    vo.setChecklistTitle(first.getTypeName());
                    vo.setCheckCount(checkCount);
                    vo.setQualifiedCount(qualifiedCount);
                    vo.setUnqualifiedCount(unqualifiedCount);
                    vo.setUnqualifiedRate(calculateRate(unqualifiedCount, checkCount));
                    return vo;
                })
                // ----- 灵活排序改造位置 -----
                .sorted((o1, o2) -> {
                    String sortBy = reqVO == null ? null : reqVO.getSortBy();
                    Integer sortOrder = reqVO == null ? null : reqVO.getSortOrder();
                    boolean desc = Objects.equals(sortOrder,1); // 默认 0-升序，1-降序

                    Comparator<InspectionItemStatRespVO> primary;

                    if (sortBy != null && !sortBy.trim().isEmpty()) {
                        String key = sortBy.trim();
                        switch (key) {
                            case "unqualifiedCount":
                                primary = Comparator.comparing(InspectionItemStatRespVO::getUnqualifiedCount,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "checkCount":
                                primary = Comparator.comparing(InspectionItemStatRespVO::getCheckCount,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "qualifiedCount":
                                primary = Comparator.comparing(InspectionItemStatRespVO::getQualifiedCount,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "unqualifiedRate":
                                primary = Comparator.comparing(InspectionItemStatRespVO::getUnqualifiedRate,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "checklistTitle":
                                primary = Comparator.comparing(InspectionItemStatRespVO::getChecklistTitle,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            default:
                                // 默认未知字段
                                primary = Comparator.comparing(InspectionItemStatRespVO::getUnqualifiedCount,
                                        Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                                break;
                        }
                    } else {
                        // 没有传 sortBy 时的默认排序行为
                        primary = Comparator.comparing(InspectionItemStatRespVO::getUnqualifiedCount,
                                Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                    }

                    // 处理升降序
                    if (sortBy != null && !sortBy.trim().isEmpty()) {
                        if (desc) {
                            primary = primary.reversed();
                        }
                    }

                    // 增加稳定的后退比较（确保同样数据排序列一致）
                    Comparator<InspectionItemStatRespVO> finalComp = primary
                            .thenComparing(InspectionItemStatRespVO::getUnqualifiedRate,
                                    Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(InspectionItemStatRespVO::getCheckCount,
                                    Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(InspectionItemStatRespVO::getChecklistTitle,
                                    Comparator.nullsLast(Comparator.naturalOrder()));

                    return finalComp.compare(o1, o2);
                })
                // ---------------------------
                .collect(Collectors.toList());

        return buildPageResult(respList, reqVO.getPageNo(), reqVO.getPageSize());
    }


    @Override
    public PageResult<StoreInspectionIntervalRespVO> getStoreInspectionInterval(InspectionReportReqVO reqVO) {
        List<StoreInspectionRecordDO> recordList = listReportRecords(reqVO);
        if (CollectionUtil.isEmpty(recordList)) {
            return PageResult.empty();
        }

        LinkedHashMap<String, List<StoreInspectionRecordDO>> groupedMap = recordList.stream()
                .sorted(Comparator.comparing(this::resolveRecordTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(StoreInspectionRecordDO::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.groupingBy(
                        this::buildStoreGroupKey,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<StoreInspectionIntervalRespVO> respList = groupedMap.values().stream()
                .map(group -> {
                    StoreInspectionRecordDO latest = group.get(0);
                    LocalDateTime latestTime = resolveRecordTime(latest);

                    StoreInspectionIntervalRespVO vo = new StoreInspectionIntervalRespVO();
                    vo.setStoreId(latest.getStoreId());
                    vo.setStoreName(latest.getStoreName());
                    vo.setLastInspectorName(latest.getInspectorName());
                    vo.setLastScoreRate(latest.getScoreRate() == null ? BigDecimal.ZERO : latest.getScoreRate());
                    vo.setLastActualScore(latest.getActualScore() == null ? 0 : latest.getActualScore());
                    vo.setDaysSinceLastInspection(calculateDaysSinceLastInspection(latestTime));
                    return vo;
                })
                .sorted((o1, o2) -> {
                    String sortBy = reqVO == null ? null : reqVO.getSortBy();
                    Integer sortOrder = reqVO == null ? null : reqVO.getSortOrder();
                    boolean desc = Objects.equals(sortOrder, 1); // 0-升序，1-降序

                    Comparator<StoreInspectionIntervalRespVO> primary;

                    if (sortBy != null && !sortBy.trim().isEmpty()) {
                        String key = sortBy.trim();
                        switch (key) {
                            case "lastScoreRate":
                                primary = Comparator.comparing(StoreInspectionIntervalRespVO::getLastScoreRate,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "lastActualScore":
                                primary = Comparator.comparing(StoreInspectionIntervalRespVO::getLastActualScore,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "daysSinceLastInspection":
                                primary = Comparator.comparing(StoreInspectionIntervalRespVO::getDaysSinceLastInspection,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "storeName":
                                primary = Comparator.comparing(StoreInspectionIntervalRespVO::getStoreName,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            case "lastInspectorName":
                                primary = Comparator.comparing(StoreInspectionIntervalRespVO::getLastInspectorName,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                                break;
                            default:
                                // 默认排序：距离上次巡店天数倒序
                                primary = Comparator.comparing(StoreInspectionIntervalRespVO::getDaysSinceLastInspection,
                                        Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                                break;
                        }
                    } else {
                        // 未传排序字段时，保持当前默认行为
                        primary = Comparator.comparing(StoreInspectionIntervalRespVO::getDaysSinceLastInspection,
                                Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                    }

                    if (sortBy != null && !sortBy.trim().isEmpty()) {
                        if (desc) {
                            primary = primary.reversed();
                        }
                    }

                    Comparator<StoreInspectionIntervalRespVO> finalComp = primary
                            .thenComparing(StoreInspectionIntervalRespVO::getLastScoreRate,
                                    Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(StoreInspectionIntervalRespVO::getLastActualScore,
                                    Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(StoreInspectionIntervalRespVO::getDaysSinceLastInspection,
                                    Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(StoreInspectionIntervalRespVO::getStoreName,
                                    Comparator.nullsLast(Comparator.naturalOrder()));

                    return finalComp.compare(o1, o2);
                })
                .collect(Collectors.toList());

        return buildPageResult(respList, reqVO.getPageNo(), reqVO.getPageSize());
    }


    @Override
    public StoreInspectionRecordRespVO getRecordByStoreId(StoreInspectionRecordReqVO reqVO) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAll() {
        // 1. 固定一个模板，只用于演示
        Long templateId = 2058797620593930241L;
        String templateName = "演示巡店模板";

        // 2. 查模板点检项
        List<TemplateChecklistDO> templateChecklistList = templateChecklistMapper.selectList(
                new QueryWrapper<TemplateChecklistDO>()
                        .eq("template_id", templateId)
                        .orderByAsc("type_sort")
                        .orderByAsc("sort")
                        .orderByAsc("template_checklist_id")
        );
        if (CollectionUtil.isEmpty(templateChecklistList)) {
            return 0L;
        }

        // 3. 取所有门店，不是当前人可见门店
        List<StoreInfoDTO> storeList = storeApi.getAllStoreList().getCheckedData();
        if (CollectionUtil.isEmpty(storeList)) {
            return 0L;
        }

        Random random = new Random();
        Long inspectorId = ObjectUtil.defaultIfNull(SecurityFrameworkUtils.getLoginUserId(), 1L);

        int insertedCount = 0;

        // 4. 每个门店造 1 条已完成巡店记录
        for (StoreInfoDTO store : storeList) {
            LocalDateTime startTime = LocalDateTime.now()
                    .minusDays(random.nextInt(30))
                    .minusHours(random.nextInt(24))
                    .minusMinutes(random.nextInt(60));
            LocalDateTime endTime = startTime.plusDays(1);
            LocalDateTime overTime = startTime.plusMinutes(30 + random.nextInt(180));

            int totalScore = 0;
            int actualScore = 0;
            int qualifiedCount = 0;
            int unqualifiedCount = 0;
            int notApplicableCount = 0;
            BigDecimal totalRewardAmount = BigDecimal.ZERO;

            List<StoreInspectionRecordItemDO> itemList = new ArrayList<>();

            for (TemplateChecklistDO templateItem : templateChecklistList) {
                int actualStatus = random.nextInt(3); // 0/1/2 随机

                int maxScore = templateItem.getScore() == null ? 10 : Math.max(templateItem.getScore(), 0);
                int itemActualScore;
                if (actualStatus == 1) {
                    itemActualScore = maxScore;
                    qualifiedCount++;
                } else if (actualStatus == 0) {
                    itemActualScore = maxScore <= 0 ? 0 : random.nextInt(maxScore); // 小于满分
                    unqualifiedCount++;
                } else {
                    itemActualScore = 0;
                    notApplicableCount++;
                }

                BigDecimal rewardAmount = BigDecimal.valueOf(random.nextInt(21) - 10); // -10 ~ 10

                totalScore += maxScore;
                actualScore += itemActualScore;
                totalRewardAmount = totalRewardAmount.add(rewardAmount);

                StoreInspectionRecordItemDO itemDO = new StoreInspectionRecordItemDO();
                itemDO.setTypeId(templateItem.getTypeId());
                itemDO.setTypeName(templateItem.getTypeName());
                itemDO.setChecklistId(templateItem.getChecklistId());
                itemDO.setTitleSnap(templateItem.getTitle());
                itemDO.setPromptSnap(templateItem.getPrompt());
                itemDO.setStandardSnap("演示标准");
                itemDO.setMaxScoreSnap(maxScore);

                itemDO.setActualStatus(actualStatus);
                itemDO.setActualScore(itemActualScore);
                itemDO.setRewardAmount(rewardAmount);
                itemDO.setActualComment("演示数据-" + templateItem.getTitle());
                itemDO.setActualImages("[\"https://example.com/demo-item.jpg\"]");

                boolean rectified = actualStatus == 0 && random.nextBoolean();
                itemDO.setIsRectified(rectified);
                itemDO.setRectifyTime(rectified ? overTime.plusHours(2) : null);

                itemDO.setImgRule(templateItem.getImgRule());
                itemDO.setDescriptionRule(templateItem.getDescriptionRule());
                itemDO.setInapplicabilityRule(templateItem.getInapplicabilityRule());
                itemDO.setRewardPunishmentRule(templateItem.getRewardPunishmentRule());

                itemDO.setSort(templateItem.getSort());
                itemDO.setTypeSort(templateItem.getTypeSort());

                itemList.add(itemDO);
            }

            StoreInspectionRecordDO recordDO = new StoreInspectionRecordDO();
            recordDO.setStoreId(store.getStoreId());
            recordDO.setStoreName(store.getStoreName());
            recordDO.setTemplateId(templateId);
            recordDO.setTemplateName(templateName);

            // 你要求主表全部已完成
            recordDO.setStatus(1);

            recordDO.setInspectorId(inspectorId);
            recordDO.setInspectorName("演示巡店员");
            recordDO.setInspectorPhone("13800000000");
            recordDO.setInScope(Boolean.TRUE);

            recordDO.setStartTime(startTime);
            recordDO.setEndTime(endTime);
            recordDO.setOverTime(overTime);

            recordDO.setTotalScore(totalScore);
            recordDO.setActualScore(actualScore);
            recordDO.setScoreRate(calculateRate(actualScore, totalScore));

            recordDO.setItemCount(itemList.size());
            recordDO.setQualifiedCount(qualifiedCount);
            recordDO.setUnqualifiedCount(unqualifiedCount);
            recordDO.setNotApplicableCount(notApplicableCount);
            recordDO.setRewardAmount(totalRewardAmount);

            recordDO.setStorePatrolPosition("演示巡店位置");
            recordDO.setStorePic("[\"https://example.com/demo-store.jpg\"]");

            // 先插主表，拿到 recordId
            storeInspectionRecordMapper.insert(recordDO);

            Long recordId = recordDO.getId();
            for (StoreInspectionRecordItemDO itemDO : itemList) {
                itemDO.setRecordId(recordId);
            }

            // 再插明细
            storeInspectionRecordItemService.saveBatch(itemList);
            insertedCount++;
        }

        return (long) insertedCount;
    }


    /**
     * 查询报表统计范围内的巡店记录
     * 口径：只统计已完成的巡店记录
     */
    private List<StoreInspectionRecordDO> listReportRecords(InspectionReportReqVO reqVO) {

        List<Long> storeIds = storeApi.getStoreIdsByUser();
        if (CollectionUtil.isEmpty(storeIds)) {
            return Collections.emptyList();
        }
        return storeInspectionRecordMapper.selectList(
                new QueryWrapper<StoreInspectionRecordDO>()
                        .eq("status", 1)
                        .eq(reqVO.getInspectorId() != null, "inspector_id", reqVO.getInspectorId())
                        .eq(reqVO.getStoreId() != null, "store_id", reqVO.getStoreId())
                        .in("store_id", storeIds)
                        .eq(reqVO.getTemplateId() != null, "template_id", reqVO.getTemplateId())
                        .ge(reqVO.getBeginTime() != null, "start_time", reqVO.getBeginTime())
                        .le(reqVO.getEndTime() != null, "start_time", reqVO.getEndTime())
                        .orderByDesc("start_time")
                        .orderByDesc("id")
        );
    }

    /**
     * 内存分页
     */
    private <T> PageResult<T> buildPageResult(List<T> fullList, Integer pageNo, Integer pageSize) {
        if (CollectionUtil.isEmpty(fullList)) {
            return PageResult.empty();
        }

        if (pageSize == null || pageSize <= 0) {
            return new PageResult<>(fullList, (long) fullList.size());
        }

        int currentPageNo = (pageNo == null || pageNo < 1) ? 1 : pageNo;
        int fromIndex = (currentPageNo - 1) * pageSize;

        if (fromIndex >= fullList.size()) {
            return new PageResult<>(Collections.emptyList(), (long) fullList.size());
        }

        int toIndex = Math.min(fromIndex + pageSize, fullList.size());
        return new PageResult<>(new ArrayList<>(fullList.subList(fromIndex, toIndex)), (long) fullList.size());
    }


    /**
     * 门店分组 key
     */
    private String buildStoreGroupKey(StoreInspectionRecordDO record) {
        if (record.getStoreId() != null) {
            return "ID_" + record.getStoreId();
        }
        return "NAME_" + Objects.toString(record.getStoreName(), "");
    }

    /**
     * 是否为合格巡店
     * 口径：只要该次巡店没有不合格项，就算合格
     */
    private boolean isQualifiedRecord(StoreInspectionRecordDO record) {
        Integer unqualifiedCount = record.getUnqualifiedCount();
        return unqualifiedCount == null || unqualifiedCount == 0;
    }


    /**
     * 取巡店记录的统计时间
     * 优先取完成时间，没有则退化到开始时间/创建时间
     */
    private LocalDateTime resolveRecordTime(StoreInspectionRecordDO record) {
        if (record.getOverTime() != null) {
            return record.getOverTime();
        }
        if (record.getStartTime() != null) {
            return record.getStartTime();
        }
        return record.getCreateTime();
    }

    /**
     * 点检项分组 key
     * 优先按 checklistId 聚合，避免同一项多次巡店无法汇总
     */
    private String buildChecklistGroupKey(StoreInspectionRecordItemDO item) {
        if (item.getChecklistId() != null) {
            return "ID_" + item.getChecklistId();
        }
        return "TITLE_" + Objects.toString(item.getTitleSnap(), "");
    }
}