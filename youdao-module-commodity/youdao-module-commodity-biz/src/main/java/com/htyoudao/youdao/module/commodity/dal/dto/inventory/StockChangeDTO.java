package com.htyoudao.youdao.module.commodity.dal.dto.inventory;

import com.htyoudao.youdao.module.commodity.enums.StockChangeEnum;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 库存变动参数
 */
@Data
public class StockChangeDTO {

    private Long storeId;
    /**
     * 变动类型
     */
    private StockChangeEnum type;

    /**
     * 关联业务单号
     */
    private String referenceId;

    /**
     * 原材料
     */
    private List<ChangeInfo> changeList;


    @Data
    public static class ChangeInfo {
        /**
         * 原材料ID
         */
        private Long rawMaterialId;

        /**
         * 商品编号
         */
        private String commodityCode;

        /**
         * 最小单位 变动数量 （正数表示增加，负数表示减少）'
         */
        private BigDecimal quantity;

        /**
         * 选择单位
         */
        private String chooseUnit;

        /**
         * 备注
         */
        private String notes;

    }


}