package com.htyoudao.youdao.module.commodity.api.DTO;

import com.htyoudao.youdao.module.commodity.enums.StockChangeEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 库存变动参数
 */
@Data
public class StockChangeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1126903510178688595L;

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
    public static class ChangeInfo implements Serializable {


        @Serial
        private static final long serialVersionUID = 1126903510178688598L;
        
        /**
         * 原材料ID
         */
        private Long rawMaterialId;

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