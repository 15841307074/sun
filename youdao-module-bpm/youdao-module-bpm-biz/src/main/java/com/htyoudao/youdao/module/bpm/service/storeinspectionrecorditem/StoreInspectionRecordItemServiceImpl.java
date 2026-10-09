package com.htyoudao.youdao.module.bpm.service.storeinspectionrecorditem;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo.StoreInspectionRecordDetailRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecorditem.vo.StoreInspectionRecordItemPageReqVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecorditem.vo.StoreInspectionRecordItemSaveReqVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionitemlog.StoreInspectionItemLogDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecord.StoreInspectionRecordDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecorditem.StoreInspectionRecordItemDO;
import com.htyoudao.youdao.module.bpm.dal.mysql.storeinspectionitemlog.StoreInspectionItemLogMapper;
import com.htyoudao.youdao.module.bpm.dal.mysql.storeinspectionrecorditem.StoreInspectionRecordItemMapper;
import com.htyoudao.youdao.module.bpm.service.storeinspectionrecord.StoreInspectionRecordService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.bpm.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.bpm.util.InspectionRecordItemRuleValidationUtil.*;

/**
 * 巡店记录明细快照 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class StoreInspectionRecordItemServiceImpl extends ServiceImpl<StoreInspectionRecordItemMapper,StoreInspectionRecordItemDO> implements StoreInspectionRecordItemService {

    @Resource
    private StoreInspectionRecordItemMapper storeInspectionRecordItemMapper;

    @Resource
    private StoreInspectionItemLogMapper storeInspectionItemLogMapper;

    @Resource
    @Lazy  // 避免循环依赖
    private StoreInspectionRecordService storeInspectionRecordService;


    @Override
    public Long createStoreInspectionRecordItem(StoreInspectionRecordItemSaveReqVO createReqVO) {
        // 插入
        StoreInspectionRecordItemDO storeInspectionRecordItem = BeanUtils.toBean(createReqVO, StoreInspectionRecordItemDO.class);
        storeInspectionRecordItemMapper.insert(storeInspectionRecordItem);
        // 返回
        return storeInspectionRecordItem.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoreInspectionRecordDetailRespVO updateStoreInspectionRecordItem(StoreInspectionRecordItemSaveReqVO updateReqVO) {
        StoreInspectionRecordItemDO itemDO = storeInspectionRecordItemMapper.selectById(updateReqVO.getId());
        if (itemDO == null) {
            throw exception(STORE_INSPECTION_RECORD_ITEM_NOT_EXISTS);
        }

        StoreInspectionRecordDO storeInspectionRecordDO = storeInspectionRecordService.getById(itemDO.getRecordId());
        if (storeInspectionRecordDO == null) {
            throw exception(STORE_INSPECTION_RECORD_NOT_EXISTS);
        }
//        if(LocalDateTime.now().isAfter(storeInspectionRecordDO.getEndTime())) {
//            throw exception(STORE_INSPECTION_RECORD_OVER_TIME);
//        }

//        if(Objects.equals(storeInspectionRecordDO.getStatus(), 1)) {
//            throw exception(STORE_INSPECTION_RECORD_ALREADY_FINISHED);
//
//        }

        Integer actualStatus = updateReqVO.getActualStatus();
        if (!Objects.equals(actualStatus, 0)
                && !Objects.equals(actualStatus, 1)
                && !Objects.equals(actualStatus, 2)) {
            throw exception(STORE_INSPECTION_RECORD_ITEM_RESULT_ERROR);
        }

        // 金额校验：非空时，必须为数字，且整数位最多6位，小数位最多2位
        if(ObjectUtil.isNotEmpty(updateReqVO.getRewardAmount())){
            validateDigits(updateReqVO.getRewardAmount(),6,2);
        }

        // 1. 校验规则
        validateInapplicabilityRule(itemDO, actualStatus);
        validateDescriptionRule(itemDO, actualStatus, updateReqVO.getActualComment());
        validateImageRule(itemDO, actualStatus, updateReqVO.getActualImages());
        validateRewardRule(itemDO, actualStatus, updateReqVO.getRewardAmount());

        // 2. 计算最终得分
        Integer finalActualScore = resolveActualScore(itemDO.getMaxScoreSnap(), actualStatus, updateReqVO.getActualScore());
        BigDecimal finalRewardAmount = normalizeRewardAmount(actualStatus, itemDO.getRewardPunishmentRule(), updateReqVO.getRewardAmount());
        String finalComment = normalizeText(updateReqVO.getActualComment());
        String finalImages = normalizeText(updateReqVO.getActualImages());

        if(Objects.equals(storeInspectionRecordDO.getStatus(), 1)) {
            // 3. 先组装变更前快照
            Integer beforeActualStatus = itemDO.getActualStatus();
            Map<String, Object> beforeSnap = buildItemLogSnap(
                    itemDO.getTitleSnap(),
                    itemDO.getMaxScoreSnap(),
                    itemDO.getActualStatus(),
                    itemDO.getActualScore(),
                    itemDO.getRewardAmount(),
                    itemDO.getActualComment(),
                    itemDO.getActualImages()
            );

            // 5. 组装变更后快照：原状态是 0/1/2，且快照字段有变化时，记录操作日志
            Map<String, Object> afterSnap = buildItemLogSnap(
                    itemDO.getTitleSnap(),
                    itemDO.getMaxScoreSnap(),
                    actualStatus,
                    finalActualScore,
                    finalRewardAmount,
                    finalComment,
                    finalImages
            );
            if (isFinishedStatus(beforeActualStatus) && hasLogSnapChanged(beforeSnap, afterSnap)) {
                StoreInspectionItemLogDO logDO = new StoreInspectionItemLogDO();
                logDO.setRecordId(itemDO.getRecordId());
                logDO.setRecordItemId(itemDO.getId());
                logDO.setOperatorId(SecurityFrameworkUtils.getLoginUserId());
                logDO.setOperatorName(SecurityFrameworkUtils.getLoginUserNickname());
                logDO.setOperateTime(LocalDateTime.now());
                logDO.setBeforeSnap(beforeSnap);
                logDO.setAfterSnap(afterSnap);
                storeInspectionItemLogMapper.insert(logDO);
            }
        }



        // 3. 更新明细表
        StoreInspectionRecordItemDO updateObj = new StoreInspectionRecordItemDO();
        updateObj.setId(itemDO.getId());
        updateObj.setActualStatus(actualStatus);
        updateObj.setActualScore(finalActualScore);
        updateObj.setRewardAmount(finalRewardAmount);
        updateObj.setActualComment(finalComment);
        updateObj.setActualImages(finalImages);
        storeInspectionRecordItemMapper.updateById(updateObj);


        // 4. 回刷主表汇总
        refreshRecordSummary(storeInspectionRecordDO);

        return storeInspectionRecordService.getItemByRecordId(itemDO.getRecordId());
    }

    /**
     * 回刷巡店记录的汇总数据
     * @param recordDO recordDO
     */
    private void refreshRecordSummary(StoreInspectionRecordDO recordDO) {
        if (recordDO == null) {
            throw exception(STORE_INSPECTION_RECORD_NOT_EXISTS);
        }

        List<StoreInspectionRecordItemDO> itemList = lambdaQuery()
                .eq(StoreInspectionRecordItemDO::getRecordId, recordDO.getId())
                .list();

        int actualScore = itemList.stream()
                .map(StoreInspectionRecordItemDO::getActualScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        BigDecimal totalAmount = itemList.stream()
                .map(StoreInspectionRecordItemDO::getRewardAmount) // 提取金额字段
                .filter(Objects::nonNull)                         // 过滤掉 null 值
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int qualifiedCount = (int) itemList.stream()
                .filter(item -> Objects.equals(item.getActualStatus(), 1))
                .count();

        int unqualifiedCount = (int) itemList.stream()
                .filter(item -> Objects.equals(item.getActualStatus(), 0))
                .count();

        int notApplicableCount = (int) itemList.stream()
                .filter(item -> Objects.equals(item.getActualStatus(), 2))
                .count();

        int totalScore = recordDO.getTotalScore() != null
                ? recordDO.getTotalScore()
                : itemList.stream()
                .map(StoreInspectionRecordItemDO::getMaxScoreSnap)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        StoreInspectionRecordDO updateRecord = new StoreInspectionRecordDO();
        updateRecord.setId(recordDO.getId());
        updateRecord.setActualScore(actualScore);
        updateRecord.setScoreRate(calculateScoreRate(actualScore, totalScore));
        updateRecord.setQualifiedCount(qualifiedCount);
        updateRecord.setRewardAmount(totalAmount);
        updateRecord.setUnqualifiedCount(unqualifiedCount);
        updateRecord.setNotApplicableCount(notApplicableCount);
        storeInspectionRecordService.updateById(updateRecord);
    }

    @Override
    public void deleteStoreInspectionRecordItem(Long id) {
        // 校验存在
        validateStoreInspectionRecordItemExists(id);
        // 删除
        storeInspectionRecordItemMapper.deleteById(id);
    }

    private void validateStoreInspectionRecordItemExists(Long id) {
        if (storeInspectionRecordItemMapper.selectById(id) == null) {
            throw exception(STORE_INSPECTION_RECORD_ITEM_NOT_EXISTS);
        }
    }

    @Override
    public StoreInspectionRecordItemDO getStoreInspectionRecordItem(Long id) {
        return storeInspectionRecordItemMapper.selectById(id);
    }

    @Override
    public PageResult<StoreInspectionRecordItemDO> getStoreInspectionRecordItemPage(StoreInspectionRecordItemPageReqVO pageReqVO) {
        return storeInspectionRecordItemMapper.selectPage(pageReqVO);
    }

}