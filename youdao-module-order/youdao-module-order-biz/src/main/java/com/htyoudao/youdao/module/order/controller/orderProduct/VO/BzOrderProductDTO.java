package com.htyoudao.youdao.module.order.controller.orderProduct.VO;


import com.htyoudao.youdao.module.order.controller.app.order.DTO.CommodityCondimentsDTO;
import com.htyoudao.youdao.module.order.controller.app.order.DTO.CommodityFlavorDTO;
import com.htyoudao.youdao.module.order.controller.app.order.DTO.CommoditySkusDTO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductSonDO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class BzOrderProductDTO {

    private String orderSn;

    private Long goodsId;

    private String goodsName;

    private String goodsImage;

    private String specValues;

    private BigDecimal goodsShowPrice;

    private String flavorName;

    private String flavorValue;

    private Integer goodsNum;

    private Integer isSingle;

    private String categoryName;

    private Long commodityId;

    private List<CommodityFlavorDTO> commodityFlavors = new ArrayList<>();
//
  private List<CommoditySkusDTO> commoditySkusList = new ArrayList<>();

    private List<String> describeList = new ArrayList<>();

    private List<String> condimentNameList = new ArrayList<>();

    private List<BzOrderProductSonDO> groupBzOrderProductList = new ArrayList<>();
//
    private List<CommodityCondimentsDTO> commodityCondiments = new ArrayList<>();

    private List<String> condimentStr = new ArrayList<>();

    private List<String> singleListStr = new ArrayList<>();

    private String skuStr;

    private Long    orderProductId ;
}
