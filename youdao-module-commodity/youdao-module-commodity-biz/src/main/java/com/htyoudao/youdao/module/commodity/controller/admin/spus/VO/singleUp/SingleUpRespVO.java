package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.singleUp;

import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu.CommoditySpusByIdRespVo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SingleUpRespVO {

    private  Long singleUpId;

    private Integer chooseView;

    private List<CommoditySpusByIdRespVo> commoditySpusList = new ArrayList<>();

}
