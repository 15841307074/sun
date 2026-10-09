package com.htyoudao.youdao.module.commodity.controller.admin.materialLog.VO;

import com.htyoudao.youdao.module.commodity.dal.dto.otherorder.ProductQuantity;
import com.htyoudao.youdao.module.commodity.enums.ChannelType;
import lombok.Data;

import java.util.List;

@Data
public class SplitCommodityMaterialReqVO {

    //必传
    private Long storeId;

    private String orderNo;

    private List<ProductQuantity> productQuantities;

    private ChannelType channelType;

}
