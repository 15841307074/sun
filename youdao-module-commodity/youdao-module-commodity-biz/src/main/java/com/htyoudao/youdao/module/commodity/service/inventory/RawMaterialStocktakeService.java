package com.htyoudao.youdao.module.commodity.service.inventory;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo.MaterialStocktakePageReq;
import com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo.MaterialStocktakePageVO;
import com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo.MaterialStocktakeSaveReq;
import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.RawMaterialRespVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialStocktake;
import jakarta.servlet.ServletOutputStream;
import jakarta.validation.Valid;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * 盘点ervice
 */
public interface RawMaterialStocktakeService extends IService<RawMaterialStocktake> {

    Boolean preserve(@Valid MaterialStocktakeSaveReq saveReq);

    Long generate(@Valid MaterialStocktakeSaveReq saveReq);

    BigDecimal selectOutboundAmount(Long storeId);

    BigDecimal selectChannelOutmount(Long storeId);

    MaterialStocktakeSaveReq getPrepareList(Long storeId);

    Boolean setRemark(Long id, String remark);

    PageResult<MaterialStocktakePageVO> stocktakeList(@Valid MaterialStocktakePageReq pageReq);

    RawMaterialRespVO stocktakeDetail(Long id);

    void export(RawMaterialRespVO id, OutputStream outputStream);
}
