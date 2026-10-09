package com.htyoudao.youdao.module.commodity.service.inventory;


import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialStockFlow;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.FlowAmountDTO;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.StockChangeDTO;
import com.htyoudao.youdao.module.commodity.enums.StockChangeEnum;
import java.math.BigDecimal;
import java.util.List;

/**
 * 原材料变动service
 */
public interface RowMaterialStockFlowService {

    /**
     * 扣减/增加 库存接口
     */
    Boolean changeStock(StockChangeDTO param);

    /**
     * 库存变动 库存接口
     * @param param 变动参数
     * @return 是否成功
     */
    Boolean setStock(StockChangeDTO param);


    /**
     * 根据流水信息 获取合计金额
     */
    BigDecimal getFlowAmount(FlowAmountDTO flowAmountDTO);


    /**
     * 根据关联业务信息 获取对应库存变动流水
     * 1.取消订单时获取应该退多少库存
     * 2.盘点,损耗之类的 查询详情获取变动了多少库存
     * @param referenceId 关联业务单号
     * @param changeEnum 变动类型
     * @return 流水列表
     */
    List<RawMaterialStockFlow> flowByReferenceInfo(String referenceId, StockChangeEnum changeEnum);
}
