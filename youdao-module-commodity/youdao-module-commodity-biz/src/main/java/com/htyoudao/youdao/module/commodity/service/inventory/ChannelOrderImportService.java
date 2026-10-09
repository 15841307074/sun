package com.htyoudao.youdao.module.commodity.service.inventory;

import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.QueryWrapperX;
import com.htyoudao.youdao.module.commodity.controller.admin.inventory.vo.CheckChannelOrderDTO;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLog.VO.SplitCommodityMaterialReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.triRecipe.VO.TriRecipeReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TriFormulationDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TriRecipeDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialStockFlow;
import com.htyoudao.youdao.module.commodity.dal.dto.otherorder.ChannelElemeOrder;
import com.htyoudao.youdao.module.commodity.dal.dto.otherorder.ChannelSankuaiOrder;
import com.htyoudao.youdao.module.commodity.dal.dto.otherorder.ProductQuantity;
import com.htyoudao.youdao.module.commodity.dal.mysql.triInventory.TriFormulationMapper;
import com.htyoudao.youdao.module.commodity.enums.ChannelType;
import com.htyoudao.youdao.module.commodity.enums.StockChangeEnum;
import com.htyoudao.youdao.module.commodity.service.inventory.ImportTaskService.ErrorRecord;
import com.htyoudao.youdao.module.commodity.service.materialLoss.RawMaterialLossRecordService;
import com.htyoudao.youdao.module.commodity.service.triInventory.ITriRecipeService;
import com.htyoudao.youdao.module.commodity.util.ProductParserUtil;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreSimpleResDto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChannelOrderImportService {

    private static final List<String> INVALID_ORDER_STATUS = List.of("订单无效", "已取消");

    private final ExcelActionService excelActionService;
    private final ImportTaskService importTaskService;
    private final RowMaterialStockFlowService flowService;
    private final RawMaterialLossRecordService lossRecordService;
    @DubboReference
    private StoreApi storeApi;
    private final TriFormulationMapper triFormulationMapper;


    /**
     * 通用的异步导入处理方法
     */
    @Async
    public <T> void processImportAsync(byte[] fileBytes, String filename, Class<T> orderClass, String taskId, ChannelType channelType) {
        List<ErrorRecord> errorRecords = new ArrayList<>();

        List<T> orders;
        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            orders = importOrdersFromExcel(inputStream, orderClass);
        } catch (Exception e) {
            log.error("异步导入任务解析Excel失败", e);
            importTaskService.updateTaskFailed(taskId, "文件解析异常");
            return;
        }

        // 批量处理订单
        List<CheckChannelOrderDTO> successList = processOrdersBatch(orders, errorRecords);

        // 保存错误记录到数据库
        importTaskService.saveErrorRecords(taskId, errorRecords);

        // 更新任务状态
        importTaskService.updateTaskSuccess(taskId, orders.size(), successList.size(), errorRecords.size());

        // 成功订单后置处理：扣库存
        List<SplitCommodityMaterialReqVO> reqVOS = new ArrayList<>();
        for (CheckChannelOrderDTO orderDTO : successList) {
            SplitCommodityMaterialReqVO reqVO = new SplitCommodityMaterialReqVO();
            reqVO.setStoreId(orderDTO.getStoreId());
            reqVO.setOrderNo(orderDTO.getOrderNumber());
            reqVO.setProductQuantities(ProductParserUtil.parseProducts(orderDTO.getProductInfo(), channelType));
            reqVO.setChannelType(channelType);
            reqVOS.add(reqVO);
        }
        lossRecordService.splitCommodityMaterial(reqVOS);
    }


    /**
     * 批量处理订单
     */
    private <T> List<CheckChannelOrderDTO> processOrdersBatch(List<T> orders,
        List<ErrorRecord> errorRecords) {

        if (orders.isEmpty()) {
            return List.of();
        }

        // 获取渠道类型（从第一个订单获取，假设所有订单都是同一渠道）
        ChannelType channelType = getChannelType(orders.get(0));

        // 批量准备数据
        BatchCheckContext context = prepareBatchCheckContext(orders, channelType);

        // 批量校验
        Map<Integer, String> checkResults = doBatchCheck(context, channelType);

        // 处理校验结果
        for (int i = 0; i < orders.size(); i++) {
            T order = orders.get(i);
            String errorMessage = checkResults.get(i);

            if (StringUtils.isNotBlank(errorMessage)) {
                errorRecords.add(new ErrorRecord(order, errorMessage));
            }
        }

        return context.successList;
    }

    /**
     * 批量准备校验上下文
     */
    private <T> BatchCheckContext prepareBatchCheckContext(List<T> orders, ChannelType channelType) {
        BatchCheckContext context = new BatchCheckContext();

        // 收集所有需要的数据
        Set<String> storeCodes = new HashSet<>();
        List<ProductQuantity> quantities = new ArrayList<>();

        for (int i = 0; i < orders.size(); i++) {
            T order = orders.get(i);
            CheckChannelOrderDTO checkDTO = BeanCopyUtils.copyBean(order, CheckChannelOrderDTO.class);
            context.addOrder(i, checkDTO);

            // 收集门店编码
            if (StringUtils.isNotBlank(checkDTO.getStoreCode())) {
                storeCodes.add(checkDTO.getStoreCode());
            }

            List<ProductQuantity> productQuantities = ProductParserUtil.parseProducts(checkDTO.getProductInfo(), channelType);

            //收集商品信息
            quantities.addAll(productQuantities);
        }

        // 批量查询门店映射
        Map<String, Long> storeMappings = batchGetStoreIds(storeCodes, channelType);
        context.storeMappings.putAll(storeMappings);

        // 批量查询配方映射
        List<ProductQuantity> productQuantities = ProductParserUtil.aggregateSameProducts(quantities);
        Set<String> productNames = productQuantities.stream().map(ProductQuantity::getProductName)
            .collect(Collectors.toSet());
        Map<String, TriFormulationDO> productRecipeMappings = batchGetProductRecipeMappings(productNames, channelType);

        context.productRecipeMappings.putAll(productRecipeMappings);
        return context;
    }


    /**
     * 批量校验逻辑
     */
    private Map<Integer, String> doBatchCheck(BatchCheckContext context, ChannelType channelType) {
        Map<Integer, String> results = new HashMap<>();

        for (Entry<Integer, CheckChannelOrderDTO> entry : context.orders.entrySet()) {
            Integer index = entry.getKey();
            CheckChannelOrderDTO orderDTO = entry.getValue();

            //批量校验单个订单
            String errorMessage = validateOrderBatch(orderDTO, context, channelType);
            results.put(index, errorMessage);

            if (StringUtils.isBlank(errorMessage)){
                context.successList.add(orderDTO);
            }
        }

        return results;
    }

    /**
     * 批量校验单个订单
     */
    private String validateOrderBatch(CheckChannelOrderDTO orderDTO, BatchCheckContext context, ChannelType channelType) {
        // 1. 校验门店映射
        Long storeId = context.storeMappings.get(orderDTO.getStoreCode());
        if (storeId == null) {
            return "找不到门店映射id，请联系运营在门店管理板块设置";
        }
        orderDTO.setStoreId(storeId);

        // 2. 校验订单状态
        if (INVALID_ORDER_STATUS.contains(orderDTO.getOrderStatus())) {
            return String.format("%s订单已失效，无需导入", orderDTO.getOrderNumber());
        }

        // 3. 解析商品信息
        String productInfo = orderDTO.getProductInfo();
        List<ProductQuantity> products = ProductParserUtil.parseProducts(productInfo, channelType);
        String productDetails = products.stream()
            .map(ProductQuantity::toString)
            .collect(Collectors.joining(","));
        log.info("订单号: {}, 商品详情: {}", orderDTO.getOrderNumber(), productDetails);

        //3.5 配方校验
        List<String> missingProducts = new ArrayList<>();
        for (ProductQuantity product : products) {
            if (!context.productRecipeMappings.containsKey(product.getProductName())) {
                missingProducts.add(product.getProductName());
            }
        }
        if (!missingProducts.isEmpty()) {
            return String.format("找不到订单内%s的配方，请设置配方后，重新导入该订单",
                String.join("、", missingProducts));
        }

        // 4.订单号重复导入
        StockChangeEnum changeEnum = null;
        switch (channelType) {
            case ELE_ME -> changeEnum = StockChangeEnum.ELE_ME_OUT;
            case SAN_KUAI -> changeEnum = StockChangeEnum.SAN_KUAI_OUT;
            case SCHOOL_PROGRAM -> changeEnum = StockChangeEnum.SCHOOL_OUT;
        }
        List<RawMaterialStockFlow> rawMaterialStockFlows = flowService.flowByReferenceInfo(orderDTO.getOrderNumber(), changeEnum);
        if (!CollectionUtils.isEmpty(rawMaterialStockFlows)){
            return "订单号重复导入";
        }

        return null;
    }



    /**
     * 实现批量根据门店编码和渠道类型获取门店ID的逻辑
     */
    private Map<String, Long> batchGetStoreIds(Set<String> storeCodes, ChannelType channelType) {

        List<StoreSimpleResDto> checkedData = storeApi.getStoreListByChannelId(
            channelType.getCode(), storeCodes.stream().toList()).getCheckedData();

        if (channelType == ChannelType.ELE_ME){
            return checkedData.stream()
                .collect(Collectors.toMap(StoreSimpleResDto::getHungryId, StoreSimpleResDto::getStoreId));
        }


        if (channelType == ChannelType.SAN_KUAI){
            return checkedData.stream()
                .collect(Collectors.toMap(StoreSimpleResDto::getMeituanId, StoreSimpleResDto::getStoreId));
        }

        return Map.of();
    }


    /**
     * 实现批量根据商品名称和渠道类型获取配方信息的逻辑
     */
    private Map<String, TriFormulationDO> batchGetProductRecipeMappings(Set<String> productNames, ChannelType channelType) {

        LambdaQueryWrapperX<TriFormulationDO> queryWrapperX = new LambdaQueryWrapperX<>();
        queryWrapperX.in(TriFormulationDO::getTriCommodityName, productNames);
        queryWrapperX.eq(TriFormulationDO::getTriType, channelType.getCode());
        List<TriFormulationDO> triFormulationDOS = triFormulationMapper.selectList(queryWrapperX);

        return triFormulationDOS.stream().collect(Collectors.toMap(
            TriFormulationDO::getTriCommodityName,
            Function.identity(),
            (existing, replacement) -> existing  // 保留第一个值，忽略后续重复..因为只是校验用
        ));
    }



    /**
     * 获取订单的渠道类型
     */
    private <T> ChannelType getChannelType(T order) {
        if (order instanceof ChannelSankuaiOrder) {
            return ChannelType.SAN_KUAI;
        } else if (order instanceof ChannelElemeOrder) {
            return ChannelType.ELE_ME;
        }
        throw new IllegalArgumentException("不支持的订单类型");
    }

    // 以下原有方法保持不变
    public <T> List<T> importOrdersFromExcel(InputStream inputStream, Class<T> orderClass) {
        try {
            return excelActionService.importExcel(inputStream, orderClass);
        } catch (Exception e) {
            log.error("导入Excel文件失败", e);
            throw e;
        }
    }

    private <T> String getOrderNumber(T order) {
        if (order instanceof ChannelSankuaiOrder) {
            return ((ChannelSankuaiOrder) order).getOrderNumber();
        } else if (order instanceof ChannelElemeOrder) {
            return ((ChannelElemeOrder) order).getOrderNumber();
        }
        return "未知订单";
    }



    /**
     * 批量校验上下文类
     */
    private static class BatchCheckContext {
        Map<Integer, CheckChannelOrderDTO> orders = new HashMap<>();
        Map<String, Long> storeMappings = new HashMap<>();
        Map<String, Object> productRecipeMappings = new HashMap<>();//商品配方
        List<CheckChannelOrderDTO> successList = new ArrayList<>();

        void addOrder(Integer index, CheckChannelOrderDTO orderDTO) {
            orders.put(index, orderDTO);
        }
    }
}