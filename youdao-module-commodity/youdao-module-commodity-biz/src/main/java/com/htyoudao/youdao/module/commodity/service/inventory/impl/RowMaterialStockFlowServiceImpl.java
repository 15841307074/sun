package com.htyoudao.youdao.module.commodity.service.inventory.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.QueryWrapperX;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterial;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialStockFlow;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.FlowAmountDTO;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.StockChangeDTO;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.StockChangeDTO.ChangeInfo;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.RawMaterialMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.RawMaterialStockFlowMapper;
import com.htyoudao.youdao.module.commodity.enums.StockChangeEnum;
import com.htyoudao.youdao.module.commodity.service.inventory.RowMaterialStockFlowService;
import de.codecentric.boot.admin.server.eventstore.OptimisticLockingException;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class RowMaterialStockFlowServiceImpl implements RowMaterialStockFlowService {

    @Resource
    private RawMaterialStockFlowMapper flowMapper;

    @Resource
    private RawMaterialMapper rawMaterialMapper;

    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;
    // 重试间隔(毫秒)
    private static final long RETRY_INTERVAL = 100L;


    @Override
    @Transactional
    public Boolean setStock(StockChangeDTO param) {
        if (param == null || CollectionUtils.isEmpty(param.getChangeList())) {
            log.warn("增加库存参数为空");
            return false;
        }

        for (ChangeInfo material : param.getChangeList()) {
            // 查询当前物料信息
            RawMaterial rawMaterial = null;
            if (material.getRawMaterialId() != null) {
                rawMaterial = rawMaterialMapper.selectById(material.getRawMaterialId());
            } else if (material.getCommodityCode() != null) {
                rawMaterial = rawMaterialMapper.selectMaterial(param.getStoreId(), material.getCommodityCode());
            }

            if (rawMaterial == null) {
                log.warn("原材料不存在，ID: {},Code:{}", material.getRawMaterialId(), material.getCommodityCode());
                continue;
            }
            material.setRawMaterialId(rawMaterial.getId());

            // 盘点时，传入的数量为 设置的库存
            BigDecimal targetStock = material.getQuantity();

            // 创建并保存库存流水记录
            RawMaterialStockFlow stockFlow = createStockFlowRecord(param, material, rawMaterial, targetStock);
            flowMapper.insert(stockFlow);

            // 使用乐观锁更新
            LambdaUpdateWrapper<RawMaterial> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(RawMaterial::getId, material.getRawMaterialId())
                .eq(RawMaterial::getVersion, rawMaterial.getVersion());
            updateWrapper.set(RawMaterial::getStock, targetStock);
            updateWrapper.set(RawMaterial::getVersion, rawMaterial.getVersion() + 1);

            int updatedRows = rawMaterialMapper.update(null, updateWrapper);

            if (updatedRows == 0) {
                // 版本号不匹配，抛出乐观锁异常
                throw new OptimisticLockingException("库存版本冲突，物料ID: " + material.getRawMaterialId());
            }

            log.debug("同步设置库存成功，物料ID: {}, 新库存: {}", material.getRawMaterialId(), targetStock);
        }

        return true;
    }

    @Override
    public BigDecimal getFlowAmount(FlowAmountDTO flowAmountDTO) {
        QueryWrapperX<RawMaterialStockFlow> queryWrapperX = new QueryWrapperX();
        queryWrapperX.select("COALESCE(SUM(change_quantity * unit_price), 0)");
        queryWrapperX.eqIfPresent("store_id", flowAmountDTO.getStoreId());
        queryWrapperX.inIfPresent("change_type", flowAmountDTO.getChangeTypes());
        queryWrapperX.betweenIfPresent("create_time",
            flowAmountDTO.getStartTime(),
            flowAmountDTO.getEndTime()
        );

        List<Object> results = flowMapper.selectObjs(queryWrapperX);
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (!CollectionUtils.isEmpty(results)) {
            Object result = results.get(0);
            if (result != null) {
                totalAmount = (BigDecimal) result;
            }
        }
        return totalAmount;
    }


    @Transactional
    @Override
    public Boolean changeStock(StockChangeDTO param) {
        if (param == null || CollectionUtils.isEmpty(param.getChangeList())) {
            log.warn("库存参数为空");
            return false;
        }


        for (ChangeInfo changeInfo : param.getChangeList()) {
            // 查询当前物料信息
            RawMaterial rawMaterial = null;
            if (changeInfo.getRawMaterialId() != null) {
                rawMaterial = rawMaterialMapper.selectById(changeInfo.getRawMaterialId());
            } else if (changeInfo.getCommodityCode() != null) {
                rawMaterial = rawMaterialMapper.selectMaterial(param.getStoreId(), changeInfo.getCommodityCode());
            }
            if (rawMaterial == null) {
                log.warn("{}:{}:原材料不存在,ID:{},code:{}",
                    param.getType().getType(),
                    param.getReferenceId(), changeInfo.getRawMaterialId(),
                    changeInfo.getCommodityCode());
                continue;
            }
            changeInfo.setRawMaterialId(rawMaterial.getId());

            // 计算变动后的库存
            BigDecimal newStock = rawMaterial.getStock().add(changeInfo.getQuantity());

            // 创建并保存库存流水记录
            RawMaterialStockFlow stockFlow = createStockFlowRecord(param, changeInfo, rawMaterial, newStock);
            flowMapper.insert(stockFlow);

            // 异步更新库存
            asyncUpdateStock(stockFlow.getId(), changeInfo.getRawMaterialId(), changeInfo.getQuantity());
        }

        return true;
    }


    /**
     * 创建库存流水记录
     */
    private RawMaterialStockFlow createStockFlowRecord(StockChangeDTO param, ChangeInfo material,
        RawMaterial rawMaterial, BigDecimal newStock) {
        RawMaterialStockFlow stockFlow = new RawMaterialStockFlow();
        stockFlow.setStoreId(param.getStoreId());
        stockFlow.setRawMaterialId(material.getRawMaterialId());
        stockFlow.setChangeType(param.getType().getType());
//        stockFlow.setSpecifications(rawMaterial.getSpecifications());
//        stockFlow.setSpecificationsRules(rawMaterial.getSpecificationsRules());
        stockFlow.setMaterialSnapshot(JSON.toJSONString(rawMaterial));
        stockFlow.setStockBefore(rawMaterial.getStock());
        stockFlow.setStockAfter(newStock);
        stockFlow.setChangeQuantity(newStock.subtract(rawMaterial.getStock()));
        stockFlow.setUnitPrice(rawMaterial.getUnitPrice());
        stockFlow.setMinUnit(rawMaterial.getMinUnit());
        stockFlow.setCommodityName(rawMaterial.getCommodityName());
        stockFlow.setChooseUnit(material.getChooseUnit());
        stockFlow.setReferenceId(param.getReferenceId());
        stockFlow.setNotes(material.getNotes());
        return stockFlow;
    }

    /**
     * 异步更新库存
     */
    @Async
    public void asyncUpdateStock(Long flowId, Long materialId, BigDecimal changeQuantity) {
        try {
            // 使用乐观锁更新库存，如果失败则重试
            boolean success = updateStockWithRetry(flowId, materialId, changeQuantity);

            if (!success) {
                log.error("库存更新失败，原材料ID: {}", materialId);
            }
        } catch (Exception e) {
            log.error("异步更新库存失败", e);
        }
    }

    /**
     * 使用重试机制更新库存
     */
    private boolean updateStockWithRetry(Long flowId, Long materialId, BigDecimal changeQuantity) {
        for (int retry = 0; retry < MAX_RETRY_COUNT; retry++) {
            try {
                // 查询当前最新的物料信息
                RawMaterial currentMaterial = rawMaterialMapper.selectById(materialId);
                if (currentMaterial == null) {
                    log.warn("原材料不存在，ID: {}", materialId);
                    return false;
                }

                // 计算新的库存值
                BigDecimal oldStock = currentMaterial.getStock();
                BigDecimal newStock = oldStock.add(changeQuantity);

                // 使用乐观锁更新
                LambdaUpdateWrapper<RawMaterial> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(RawMaterial::getId, materialId)
                    .eq(RawMaterial::getVersion, currentMaterial.getVersion());
                updateWrapper.set(RawMaterial::getStock, newStock);
                //如果变动后的库存 为负数 设置为在售
                if (newStock.compareTo(BigDecimal.ZERO) < 0) {
                    updateWrapper.set(RawMaterial::getIsEnable, 1);
                }
                updateWrapper.set(RawMaterial::getVersion, currentMaterial.getVersion() + 1);

                int updatedRows = rawMaterialMapper.update(null, updateWrapper);

                if (updatedRows > 0) {
                    log.info("库存更新成功，原材料ID: {}, 新库存: {}, 旧库存:{}", materialId, newStock, oldStock);

                    LambdaUpdateWrapper<RawMaterialStockFlow> flow = new LambdaUpdateWrapper();
                    flow.set(RawMaterialStockFlow::getStockBefore, oldStock);
                    flow.set(RawMaterialStockFlow::getStockAfter, newStock);
                    flow.eq(RawMaterialStockFlow::getId, flowId);
                    flowMapper.update(flow);

                    return true;
                }

                // 更新失败，等待后重试
                if (retry < MAX_RETRY_COUNT - 1) {
                    Thread.sleep(RETRY_INTERVAL * (retry + 1));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("库存更新重试被中断", e);
                return false;
            } catch (Exception e) {
                log.error("第{}次,{},库存更新重试异常", retry, materialId, e);
                if (retry == MAX_RETRY_COUNT - 1) {
                    return false;
                }
            }
        }

        return false;
    }


    @Override
    public List<RawMaterialStockFlow> flowByReferenceInfo(String referenceId, StockChangeEnum changeEnum) {
        LambdaQueryWrapperX<RawMaterialStockFlow> queryWrapperX = new LambdaQueryWrapperX();
        queryWrapperX.eq(RawMaterialStockFlow::getReferenceId, referenceId);
        queryWrapperX.eq(RawMaterialStockFlow::getChangeType, changeEnum.getType());
        return flowMapper.selectList(queryWrapperX);
    }

}