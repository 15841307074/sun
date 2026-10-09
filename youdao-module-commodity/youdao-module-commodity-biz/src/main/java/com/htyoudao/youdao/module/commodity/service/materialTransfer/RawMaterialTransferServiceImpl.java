package com.htyoudao.youdao.module.commodity.service.materialTransfer;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialDataReqVo;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialDataRespVo;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialTypeVo;
import com.htyoudao.youdao.module.commodity.controller.app.materialTransfer.VO.*;
import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.CommodityConversion;
import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.SaveMaterialReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.*;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.StockChangeDTO;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.*;
import com.htyoudao.youdao.module.commodity.enums.StockChangeEnum;
import com.htyoudao.youdao.module.commodity.enums.TransferStatus;
import com.htyoudao.youdao.module.commodity.service.inventory.IRawMaterialService;
import com.htyoudao.youdao.module.commodity.service.inventory.RowMaterialStockFlowService;
import com.htyoudao.youdao.module.system.api.business.BusinnessApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;


@Service
public class RawMaterialTransferServiceImpl extends ServiceImpl<RawMaterialTransferRecordMapper, RawMaterialTransferRecordDO> implements RawMaterialTransferService {

    @Autowired
    private RawMaterialLossRecordMapper materialLossRecordMapper;

    @Autowired
    private RawMaterialTransferRecordMapper materialTransferRecordMapper;

    @Autowired
    private RawMaterialStockFlowMapper rawMaterialStockFlowMapper;

    @Autowired
    private RowMaterialStockFlowService rowMaterialStockFlowService;

    @Autowired
    private RawMaterialMapper rawMaterialMapper;


    @Resource
    private IRawMaterialService rawMaterialService;

    @Autowired
    private RawMaterialTransferTemporaryMapper transferTemporaryMapper;


    @DubboReference
    private StoreApi storeApi;


    @DubboReference
    private BusinnessApi businnessApi;


    @Override
    public PageResult<MaterialTransferRecordPageVO> transferRecordList(MaterialTransferRecordPageReq pageReq) {
        PageResult<MaterialTransferRecordPageVO> pageResult= new PageResult<>();
        List<MaterialTransferRecordPageVO> transferRecordPageVOS= new ArrayList<>();
        LambdaQueryWrapper<RawMaterialTransferRecordDO> wrapper = new LambdaQueryWrapper<>();

//        if(ObjectUtil.isNotEmpty(pageReq.getStoreId())){
//            wrapper.eq(RawMaterialTransferRecordDO::getFromStoreId, pageReq.getStoreId())
//                    .or()
//                    .eq(RawMaterialTransferRecordDO::getToStoreId, pageReq.getStoreId());
//        }
        if(ObjectUtil.isNotEmpty(pageReq.getStoreId())){
            wrapper.and(w -> w.eq(RawMaterialTransferRecordDO::getFromStoreId, pageReq.getStoreId())
                    .or()
                    .eq(RawMaterialTransferRecordDO::getToStoreId, pageReq.getStoreId()));
        }

        if(ObjectUtil.isNotEmpty(pageReq.getFromStoreId())){
            wrapper.eq(RawMaterialTransferRecordDO::getFromStoreId,pageReq.getFromStoreId());
        }


        if(ObjectUtil.isNotEmpty(pageReq.getToStoreId())){
            wrapper.eq(RawMaterialTransferRecordDO::getToStoreId,pageReq.getToStoreId());
        }

        if(ObjectUtil.isNotEmpty(pageReq.getTransferType())){
            wrapper.eq(RawMaterialTransferRecordDO::getTransferType,pageReq.getTransferType());
        }
        if(ObjectUtil.isNotEmpty(pageReq.getMaterialCode())){
            if(ObjectUtil.isNotEmpty(pageReq.getMaterialCode())){
                LambdaQueryWrapperX<RawMaterial> rawMaterialLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
                rawMaterialLambdaQueryWrapperX.eqIfPresent(RawMaterial::getCommodityCode,pageReq.getMaterialCode());
                List<RawMaterial> rawMaterials = rawMaterialMapper.selectList(rawMaterialLambdaQueryWrapperX);
                if(ObjectUtil.isNotEmpty(rawMaterials)){
                    List<Long> materialIds = rawMaterials.stream().map(mm -> mm.getId()).collect(Collectors.toList());
                    LambdaQueryWrapperX<RawMaterialTransferTemporaryDO> wrapperX = new LambdaQueryWrapperX<>();
                    wrapperX.inIfPresent(RawMaterialTransferTemporaryDO::getRawMaterialId,materialIds);
                    List<RawMaterialTransferTemporaryDO> rawMaterialTransferTemporaryDOS = transferTemporaryMapper.selectList(wrapperX);
                    if(ObjectUtil.isNotEmpty(rawMaterialTransferTemporaryDOS)){
                        Set<Long> collect = rawMaterialTransferTemporaryDOS.stream().map(mm -> mm.getTransferId()).collect(Collectors.toSet());
                        wrapper.in(RawMaterialTransferRecordDO::getId,collect);
                    }else{
                        pageResult.setTotal(0L);
                        pageResult.setList(new ArrayList<>());
                        return pageResult;
                    }
                }else{
                    pageResult.setTotal(0L);
                    pageResult.setList(new ArrayList<>());
                    return pageResult;
                }



            }

        }

        wrapper.orderByDesc(RawMaterialTransferRecordDO::getCreateTime);

        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageReq.getPageNo());
        pageParam.setPageSize(pageReq.getPageSize());
        PageResult<RawMaterialTransferRecordDO> rawMaterialTransferRecordDOPageResult = materialTransferRecordMapper.selectPage(pageParam, wrapper);
        List<RawMaterialTransferRecordDO> list = rawMaterialTransferRecordDOPageResult.getList();

        if(ObjectUtil.isNotEmpty(list)){
            for (RawMaterialTransferRecordDO materialTransferRecordDO : list) {
                MaterialTransferRecordPageVO materialTransferRecordPageVO = new MaterialTransferRecordPageVO();
                BeanUtils.copyProperties(materialTransferRecordDO,materialTransferRecordPageVO);
                if(ObjectUtil.isNotEmpty(pageReq.getStoreId())){
                    if(materialTransferRecordDO.getTransferType().equals(0)){
                        if(pageReq.getStoreId().equals(materialTransferRecordDO.getFromStoreId())){
                            materialTransferRecordPageVO.setTransferType(1);
                        }else if (pageReq.getStoreId().equals(materialTransferRecordDO.getToStoreId())){
                            materialTransferRecordPageVO.setTransferType(2);
                        }
                    }

                }
                CommonResult<StoreDTO> store = storeApi.getStoreByStoreId(materialTransferRecordDO.getFromStoreId());
                StoreDTO data = store.getData();
                if(ObjectUtil.isNotEmpty(data)){
                    String storeName = data.getStoreName();
                    materialTransferRecordPageVO.setFromStoreName(storeName);
                }
                CommonResult<StoreDTO> storeToName = storeApi.getStoreByStoreId(materialTransferRecordDO.getToStoreId());
                StoreDTO storeDTO = storeToName.getData();
                if(ObjectUtil.isNotEmpty(storeDTO)){
                    String storeName = storeDTO.getStoreName();
                    materialTransferRecordPageVO.setToStoreName(storeName);
                }
                List<MaterialDataRespVo> materialList = new ArrayList<>();
                LambdaQueryWrapperX<RawMaterialTransferTemporaryDO> transferTemporaryDOLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
                transferTemporaryDOLambdaQueryWrapperX.eq(RawMaterialTransferTemporaryDO::getTransferId,materialTransferRecordDO.getId());
                List<RawMaterialTransferTemporaryDO> rawMaterialTransferTemporaryDOS = transferTemporaryMapper.selectList(transferTemporaryDOLambdaQueryWrapperX);

                if(ObjectUtil.isNotEmpty(rawMaterialTransferTemporaryDOS)){
                    for (RawMaterialTransferTemporaryDO rawMaterialTransferTemporaryDO : rawMaterialTransferTemporaryDOS) {
                        MaterialDataRespVo materialDataRespVo = new MaterialDataRespVo();
                        Long rawMaterialId = rawMaterialTransferTemporaryDO.getRawMaterialId();
                        materialDataRespVo.setRawMaterialId(rawMaterialId);
                        LambdaQueryWrapper<RawMaterial> rawMaterialLambdaQueryWrapper = new LambdaQueryWrapper<>();
                        rawMaterialLambdaQueryWrapper.eq(RawMaterial::getId,rawMaterialId);
                        materialDataRespVo.setBrandName(rawMaterialTransferTemporaryDO.getBrandName());
                        materialDataRespVo.setCommodityCode(rawMaterialTransferTemporaryDO.getMaterialCode());
                        materialDataRespVo.setCommodityName(rawMaterialTransferTemporaryDO.getMaterialName());
                        materialDataRespVo.setUnit(rawMaterialTransferTemporaryDO.getUnit());
                        materialDataRespVo.setPrice(rawMaterialTransferTemporaryDO.getPrice());
                        materialDataRespVo.setQuantity(rawMaterialTransferTemporaryDO.getQuantity());
                        materialDataRespVo.setTotalPrice(rawMaterialTransferTemporaryDO.getTotalPrice());
                        materialList.add(materialDataRespVo);

                    }
                    materialTransferRecordPageVO.setMaterialList(materialList);

                }
                transferRecordPageVOS.add(materialTransferRecordPageVO);
            }
            pageResult.setTotal(rawMaterialTransferRecordDOPageResult.getTotal());
            pageResult.setList(transferRecordPageVOS);
            return pageResult;
        }else{
            pageResult.setTotal(0L);
            pageResult.setList(new ArrayList<>());
            return pageResult;
        }



    }

    @Override
    public List<MaterialTypeVo> transferStatusList() {
        List<MaterialTypeVo> list = new ArrayList<>();
        for (TransferStatus type : TransferStatus.values()){
            MaterialTypeVo materialTypeVo = new MaterialTypeVo();
            Integer code = type.getCode();
            String description = type.getDescription();
            materialTypeVo.setCode(code);
            materialTypeVo.setDescription(description);
            list.add(materialTypeVo);
        }

        return list;
    }

    @Override
    @Transactional
    public Boolean isReceive(MaterialTransferReceiveReq receiveReq) {
        if(receiveReq!=null){
            if(ObjectUtil.isNotEmpty(receiveReq.getStoreId())){
                CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(receiveReq.getStoreId());
                StoreDTO data = storeByStoreId.getData();
                if(data!=null){
                    Integer storeStatus = data.getStoreStatus();
                    if(storeStatus.equals(1)){
                        throw exception(1_003_011_022,"门店已闭店,不可操作");
                    }
                }

            }
            if(receiveReq.getTransferType().equals(TransferStatus.COMPLETED.getCode())){
                LambdaQueryWrapperX<RawMaterialTransferRecordDO> rawMaterialTransferRecordDOLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
                rawMaterialTransferRecordDOLambdaQueryWrapperX.eq(RawMaterialTransferRecordDO::getId,receiveReq.getId());
                RawMaterialTransferRecordDO recordDO = materialTransferRecordMapper.selectOne(rawMaterialTransferRecordDOLambdaQueryWrapperX);
                BigDecimal totalAmount = BigDecimal.ZERO;
                if(recordDO!=null){

                    recordDO.setTransferType(receiveReq.getTransferType());
                    recordDO.setReceiveDate(new Date());
                    int i = materialTransferRecordMapper.updateById(recordDO);

                    List<StockChangeDTO.ChangeInfo> changeInfoList = new ArrayList<>();
                    List<StockChangeDTO.ChangeInfo> changeInfoArrayList = new ArrayList<>();
                    //转出店数据
                    StockChangeDTO stockChangeDTO = new StockChangeDTO();
                    stockChangeDTO.setStoreId(recordDO.getFromStoreId());
                    stockChangeDTO.setType(StockChangeEnum.TRANSFER_OUT);

                    //转入店数据
                    StockChangeDTO stockChangeDTOTwo = new StockChangeDTO();
                    stockChangeDTOTwo.setType(StockChangeEnum.TRANSFER_IN);
                    stockChangeDTOTwo.setStoreId(recordDO.getToStoreId());

                    LocalDateTime now = LocalDateTime.now();
                    //原材料列表
//                    List<SaveMaterialReqVO> saveMaterialReqVO = new ArrayList<>();
                    LambdaQueryWrapperX<RawMaterialTransferTemporaryDO> doLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
                    doLambdaQueryWrapperX.eq(RawMaterialTransferTemporaryDO::getTransferId,recordDO.getId());
                    List<RawMaterialTransferTemporaryDO> rawMaterialTransferTemporaryDOS = transferTemporaryMapper.selectList(doLambdaQueryWrapperX);
                    if(ObjectUtil.isNotEmpty(rawMaterialTransferTemporaryDOS)){
                        for (RawMaterialTransferTemporaryDO rawMaterialTransferTemporaryDO : rawMaterialTransferTemporaryDOS) {
                            //选择的单位
                            String unit = rawMaterialTransferTemporaryDO.getUnit();
                            Long fromMaterialId = rawMaterialTransferTemporaryDO.getRawMaterialId();
                            LambdaQueryWrapperX<RawMaterial> wrapperX = new LambdaQueryWrapperX<>();
                            wrapperX.eq(RawMaterial::getId,fromMaterialId);
                            RawMaterial rawMaterial = rawMaterialMapper.selectOne(wrapperX);
                            if(rawMaterial!=null){
                                //如果转入门店没有 择新增原材料
                                RawMaterial selectMaterial = new RawMaterial();
                                LambdaQueryWrapperX<RawMaterial> materialLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
                                materialLambdaQueryWrapperX.eq(RawMaterial::getStoreId,recordDO.getToStoreId());
                                materialLambdaQueryWrapperX.eq(RawMaterial::getCommodityCode,rawMaterial.getCommodityCode());
                                List<RawMaterial> rawMaterials = rawMaterialMapper.selectList(materialLambdaQueryWrapperX);
                                if(ObjectUtil.isEmpty(rawMaterials)){
                                    List<SaveMaterialReqVO> saveMaterialReqVOs = getSaveMaterialReqVOS(rawMaterial, now, recordDO);
                                    rawMaterialService.batchSaveOrUpdate(saveMaterialReqVOs);
                                    selectMaterial = rawMaterialMapper.selectMaterial(recordDO.getToStoreId(), rawMaterial.getCommodityCode());
                                }


                                StockChangeDTO.ChangeInfo retrievedOut = new StockChangeDTO.ChangeInfo();
                                StockChangeDTO.ChangeInfo transferredIn = new StockChangeDTO.ChangeInfo();
                                if(rawMaterial.getMinUnit().equals(unit)){
                                    BigDecimal quantity = rawMaterialTransferTemporaryDO.getQuantity();
                                    BigDecimal negativeBigDecimal = quantity != null ? quantity.negate() : BigDecimal.ZERO;
                                    retrievedOut.setQuantity(negativeBigDecimal);
                                    retrievedOut.setRawMaterialId(rawMaterialTransferTemporaryDO.getRawMaterialId());
                                    retrievedOut.setChooseUnit(rawMaterialTransferTemporaryDO.getUnit());
                                    retrievedOut.setNotes(recordDO.getRemark());
                                    BigDecimal qu = getSafeBigDecimal(rawMaterialTransferTemporaryDO.getQuantity());
                                    BigDecimal price = getSafeBigDecimal(rawMaterialTransferTemporaryDO.getPrice());
                                    totalAmount = calculateTotalAmount(totalAmount, qu, price);
                                    changeInfoList.add(retrievedOut);

                                    BigDecimal voQuantity = rawMaterialTransferTemporaryDO.getQuantity();
                                    BigDecimal nn = voQuantity != null ? voQuantity : BigDecimal.ZERO;
                                    transferredIn.setQuantity(nn);
                                    if(ObjectUtil.isNotEmpty(selectMaterial.getId())){
                                        transferredIn.setRawMaterialId(selectMaterial.getId());
                                    }else {
                                        RawMaterial rawMaterial1 = rawMaterials.get(0);
                                        if(rawMaterial1!=null){
                                            transferredIn.setRawMaterialId(rawMaterial1.getId());
                                        }
                                    }

                                    transferredIn.setChooseUnit(rawMaterialTransferTemporaryDO.getUnit());
                                    transferredIn.setNotes(recordDO.getRemark());

                                    changeInfoArrayList.add(transferredIn);
                                }else{
                                    //选择的不是最小单位，需要转化
                                    BigDecimal aa = BigDecimal.ZERO;

                                    try {
                                        String specificationsRules = rawMaterial.getSpecificationsRules();
                                        if (ObjectUtil.isNotEmpty(rawMaterial.getSpecificationsRules())&& !rawMaterial.getSpecificationsRules().equals("[]")) {
                                            ObjectMapper objectMapper = new ObjectMapper();

                                            List<CommodityConversion> conversionList = objectMapper.readValue(
                                                    specificationsRules,
                                                    new TypeReference<List<CommodityConversion>>() {}
                                            );
                                            for (CommodityConversion commodityConversion : conversionList) {
                                                if(commodityConversion.getBeforeUnit().equals(unit)){
                                                    aa = commodityConversion.getAfterNumber();
                                                }
                                            }
                                            BigDecimal quantity = rawMaterialTransferTemporaryDO.getQuantity().multiply(aa);
                                            BigDecimal negativeBigDecimal = quantity != null ? quantity.negate() : BigDecimal.ZERO;
                                            retrievedOut.setQuantity(negativeBigDecimal);
                                            retrievedOut.setRawMaterialId(rawMaterialTransferTemporaryDO.getRawMaterialId());
                                            retrievedOut.setNotes(recordDO.getRemark());


                                            transferredIn.setQuantity(quantity);
                                            if(selectMaterial!=null){
                                                transferredIn.setRawMaterialId(selectMaterial.getId());
                                            }else {
                                                RawMaterial rawMaterial1 = rawMaterials.get(0);
                                                if(rawMaterial1!=null){
                                                    transferredIn.setRawMaterialId(rawMaterial1.getId());
                                                }
                                            }
                                            transferredIn.setChooseUnit(rawMaterialTransferTemporaryDO.getUnit());
                                            transferredIn.setNotes(recordDO.getRemark());
                                            changeInfoArrayList.add(transferredIn);
                                        }else{
                                            retrievedOut.setQuantity(rawMaterialTransferTemporaryDO.getQuantity());
                                            retrievedOut.setRawMaterialId(rawMaterialTransferTemporaryDO.getRawMaterialId());
//                                            retrievedOut.setChooseUnit(rawMaterialTransferTemporaryDO.getUnit());
                                            retrievedOut.setNotes(recordDO.getRemark());
//                                            changeInfoList.add(retrievedOut);


                                            transferredIn.setQuantity(rawMaterialTransferTemporaryDO.getQuantity());
                                            if(selectMaterial!=null){
                                                transferredIn.setRawMaterialId(selectMaterial.getId());
                                            }else {
                                                RawMaterial rawMaterial1 = rawMaterials.get(0);
                                                if(rawMaterial1!=null){
                                                    transferredIn.setRawMaterialId(rawMaterial1.getId());
                                                }
                                            }
//                                            transferredIn.setChooseUnit(rawMaterialTransferTemporaryDO.getUnit());
                                            transferredIn.setNotes(recordDO.getRemark());
                                            changeInfoArrayList.add(transferredIn);

                                        }



                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    retrievedOut.setChooseUnit(rawMaterialTransferTemporaryDO.getUnit());

                                    BigDecimal qu = getSafeBigDecimal(rawMaterialTransferTemporaryDO.getQuantity());
                                    BigDecimal price = getSafeBigDecimal(rawMaterialTransferTemporaryDO.getPrice());
                                    totalAmount = calculateTotalAmount(totalAmount, qu, price);
                                    changeInfoList.add(retrievedOut);
                                }
                            }


                        }

                        stockChangeDTO.setReferenceId(recordDO.getId().toString());
                        stockChangeDTOTwo.setReferenceId(recordDO.getId().toString());
                        stockChangeDTO.setChangeList(changeInfoList);
                        stockChangeDTOTwo.setChangeList(changeInfoArrayList);
                        Boolean b = rowMaterialStockFlowService.changeStock(stockChangeDTO);
                        Boolean c = rowMaterialStockFlowService.changeStock(stockChangeDTOTwo);


                        return i>0;
                    }
                }
            }else if(receiveReq.getTransferType().equals(TransferStatus.CANCELLED.getCode())){
                RawMaterialTransferRecordDO recordDO = new RawMaterialTransferRecordDO();
                recordDO.setId(receiveReq.getId());
                recordDO.setTransferType(receiveReq.getTransferType());
                int i = materialTransferRecordMapper.updateById(recordDO);
                return i>0;
            }

        }

        return false;
    }

    private List<SaveMaterialReqVO> getSaveMaterialReqVOS(RawMaterial rawMaterial, LocalDateTime now, RawMaterialTransferRecordDO recordDO) {
        List<SaveMaterialReqVO> saveMaterialReqVO = new ArrayList<>();
        SaveMaterialReqVO materialReqVO = new SaveMaterialReqVO();
        materialReqVO.setBrandName(rawMaterial.getBrandName());
        materialReqVO.setCommodityCode(rawMaterial.getCommodityCode());
        materialReqVO.setCommodityName(rawMaterial.getCommodityName());
        materialReqVO.setInTime(now);
        materialReqVO.setStoreId(recordDO.getToStoreId());
        materialReqVO.setBrandId(rawMaterial.getBrandId());
        materialReqVO.setCategoryId(rawMaterial.getCategoryId());
        materialReqVO.setCategoryName(rawMaterial.getCategoryName());
        materialReqVO.setCommodityId(rawMaterial.getCommodityId());
        materialReqVO.setCommodityType(rawMaterial.getCommodityType());
        materialReqVO.setMinUnit(rawMaterial.getMinUnit());
        materialReqVO.setOutUnit(rawMaterial.getOutUnit());
        materialReqVO.setOutPrice(rawMaterial.getOutPrice());
        materialReqVO.setUnitPrice(rawMaterial.getUnitPrice());
        materialReqVO.setSpecifications(rawMaterial.getSpecifications());
        materialReqVO.setSpecificationsRules(rawMaterial.getSpecificationsRules());
        materialReqVO.setStasticsCommodityName(rawMaterial.getStasticsCommodityName());
        saveMaterialReqVO.add(materialReqVO);
        return saveMaterialReqVO;
    }

    @Override
    public Boolean saveTransferRecord(MaterialTransferSaveReq saveReq) {

        BigDecimal totalAmount = BigDecimal.ZERO;
        RawMaterialTransferRecordDO recordDO = new RawMaterialTransferRecordDO();
        BeanUtils.copyProperties(saveReq,recordDO);
        recordDO.setTransferType(0);

        // 1. 获取当前详细时间
        LocalDateTime now = LocalDateTime.now();
        String dateTime = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // 2. 将当前ID格式化为固定长度
        String formattedId = String.format(dateTime);
        recordDO.setTransferCode("db"+formattedId);
        List<MaterialDataReqVo> materialList = saveReq.getMaterialList();

        List<RawMaterialTransferTemporaryDO> transferTemporaryDOS = new ArrayList<>();
        if(ObjectUtil.isNotEmpty(materialList)){
            for (MaterialDataReqVo materialDataReqVo : materialList) {
                BigDecimal qu = materialDataReqVo.getQuantity() != null ?
                        materialDataReqVo.getQuantity() : BigDecimal.ZERO;
                BigDecimal price = materialDataReqVo.getPrice() != null ?
                        materialDataReqVo.getPrice() : BigDecimal.ZERO;

                totalAmount = totalAmount.add(qu.multiply(price).setScale(4, RoundingMode.HALF_UP));
            }
            recordDO.setTotalAmount(totalAmount);
            materialTransferRecordMapper.insert(recordDO);


            for (MaterialDataReqVo materialDataReqVo : materialList) {
                RawMaterialTransferTemporaryDO rawMaterialTransferTemporaryDO = new RawMaterialTransferTemporaryDO();
                if(ObjectUtil.isNotEmpty(recordDO.getId())){
                    rawMaterialTransferTemporaryDO.setTransferId(recordDO.getId());
                }
                rawMaterialTransferTemporaryDO.setUnit(materialDataReqVo.getUnit());
                rawMaterialTransferTemporaryDO.setPrice(materialDataReqVo.getPrice());
                BigDecimal qu = materialDataReqVo.getQuantity() != null ?
                        materialDataReqVo.getQuantity() : BigDecimal.ZERO;
                BigDecimal price = materialDataReqVo.getPrice() != null ?
                        materialDataReqVo.getPrice() : BigDecimal.ZERO;
                rawMaterialTransferTemporaryDO.setTotalPrice(qu.multiply(price).setScale(4, RoundingMode.HALF_UP));
                rawMaterialTransferTemporaryDO.setQuantity(materialDataReqVo.getQuantity());
                rawMaterialTransferTemporaryDO.setRawMaterialId(materialDataReqVo.getRawMaterialId());
                LambdaQueryWrapperX<RawMaterial> wrapperX = new LambdaQueryWrapperX<>();
                wrapperX.eq(RawMaterial::getId,materialDataReqVo.getRawMaterialId());
                List<RawMaterial> rawMaterials = rawMaterialMapper.selectList(wrapperX);
                if(ObjectUtil.isNotEmpty(rawMaterials)){
                    RawMaterial rawMaterial = rawMaterials.get(0);
                    rawMaterialTransferTemporaryDO.setMaterialCode(rawMaterial.getCommodityCode());
                    rawMaterialTransferTemporaryDO.setMaterialName(rawMaterial.getCommodityName());
                    rawMaterialTransferTemporaryDO.setBrandName(rawMaterial.getBrandName());
                }

                transferTemporaryDOS.add(rawMaterialTransferTemporaryDO);
            }
            transferTemporaryMapper.insertBatch(transferTemporaryDOS);
        }
        return true;
    }

    @Override
    public Boolean deleteTransferRecord(MaterialTransferDeleteReq transferDeleteReq) {
        Long id = transferDeleteReq.getId();
        if(ObjectUtil.isNotEmpty(id)){
            int i = materialTransferRecordMapper.deleteById(id);
            return i>0;
        }
        return false;
    }

    @Override
    public MaterialTransferRecordPageVO selectInfo(Long id) {
        RawMaterialTransferRecordDO materialTransferRecordDO = materialTransferRecordMapper.selectById(id);
        if(materialTransferRecordDO!=null){
            MaterialTransferRecordPageVO materialTransferRecordPageVO = new MaterialTransferRecordPageVO();
            BeanUtils.copyProperties(materialTransferRecordDO,materialTransferRecordPageVO);

            CommonResult<StoreDTO> store = storeApi.getStoreByStoreId(materialTransferRecordDO.getFromStoreId());
            StoreDTO data = store.getData();
            if(ObjectUtil.isNotEmpty(data)){
                String storeName = data.getStoreName();
                materialTransferRecordPageVO.setFromStoreName(storeName);
            }
            CommonResult<String> stringCommonResult = businnessApi.selectByName(materialTransferRecordDO.getBusinessId());
            String resultData = stringCommonResult.getData();
            if(ObjectUtil.isNotEmpty(resultData)){
                materialTransferRecordPageVO.setBusinessName(resultData);
            }
            CommonResult<StoreDTO> storeToName = storeApi.getStoreByStoreId(materialTransferRecordDO.getToStoreId());
            StoreDTO storeDTO = storeToName.getData();
            if(ObjectUtil.isNotEmpty(storeDTO)){
                String storeName = storeDTO.getStoreName();
                materialTransferRecordPageVO.setToStoreName(storeName);
            }
            List<MaterialDataRespVo> materialList = new ArrayList<>();
            LambdaQueryWrapperX<RawMaterialTransferTemporaryDO> transferTemporaryDOLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
            transferTemporaryDOLambdaQueryWrapperX.eq(RawMaterialTransferTemporaryDO::getTransferId,materialTransferRecordDO.getId());
            List<RawMaterialTransferTemporaryDO> rawMaterialTransferTemporaryDOS = transferTemporaryMapper.selectList(transferTemporaryDOLambdaQueryWrapperX);

            if(ObjectUtil.isNotEmpty(rawMaterialTransferTemporaryDOS)){
                for (RawMaterialTransferTemporaryDO rawMaterialTransferTemporaryDO : rawMaterialTransferTemporaryDOS) {
                    MaterialDataRespVo materialDataRespVo = new MaterialDataRespVo();
                    Long rawMaterialId = rawMaterialTransferTemporaryDO.getRawMaterialId();
                    materialDataRespVo.setRawMaterialId(rawMaterialId);
                    LambdaQueryWrapper<RawMaterial> rawMaterialLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    rawMaterialLambdaQueryWrapper.eq(RawMaterial::getId,rawMaterialId);
                    RawMaterial rawMaterial = rawMaterialMapper.selectOne(rawMaterialLambdaQueryWrapper);
                    if(rawMaterial!=null){
                        materialDataRespVo.setBrandName(rawMaterial.getBrandName());
                        materialDataRespVo.setCommodityCode(rawMaterial.getCommodityCode());
                    }
                    materialDataRespVo.setCommodityName(rawMaterialTransferTemporaryDO.getMaterialName());
                    materialDataRespVo.setUnit(rawMaterialTransferTemporaryDO.getUnit());
                    materialDataRespVo.setPrice(rawMaterialTransferTemporaryDO.getPrice());
                    materialDataRespVo.setQuantity(rawMaterialTransferTemporaryDO.getQuantity());
                    materialDataRespVo.setTotalPrice(rawMaterialTransferTemporaryDO.getTotalPrice());
                    materialList.add(materialDataRespVo);

                }
                materialTransferRecordPageVO.setMaterialList(materialList);
                return materialTransferRecordPageVO;
            }
        }
        return null;
    }

    /**
     * 计算总金额
     * @param totalAmount 当前总金额
     * @param quantity 数量
     * @param price 单价
     * @return 累加后的总金额
     */
    private BigDecimal calculateTotalAmount(BigDecimal totalAmount, BigDecimal quantity, BigDecimal price) {
        BigDecimal safeQuantity = getSafeBigDecimal(quantity);
        BigDecimal safePrice = getSafeBigDecimal(price);
        return totalAmount.add(safeQuantity.multiply(safePrice));
    }
    private BigDecimal getSafeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
