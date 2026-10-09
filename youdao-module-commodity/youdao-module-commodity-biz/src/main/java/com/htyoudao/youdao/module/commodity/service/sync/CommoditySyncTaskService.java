package com.htyoudao.youdao.module.commodity.service.sync;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommodityBaseToStoreSyncReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommoditySyncListRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommoditySyncPageReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommoditySyncStoreReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommodityTemplateToStoreSyncReqVO;

import com.htyoudao.youdao.module.commodity.service.sync.dto.CommoditySyncCategoryDTO;
import java.util.List;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface CommoditySyncTaskService {

    /**
     * 同步连锁商品到门店接口
     *
     * @param commodityStoreSpuVO
     * @return
     */
    String baseToStore(CommodityBaseToStoreSyncReqVO commodityStoreSpuVO);

    /**
     * 同步模板商品到门店
     *
     * @param syncVO
     * @return
     */
    String templateToStore(CommodityTemplateToStoreSyncReqVO syncVO);


    /**
     * 同步记录
     *
     * @return
     */
    PageResult<CommoditySyncListRespVO> syncPageList(CommoditySyncPageReqVO reqVO);


    /**
     * 根据批次号查询门店记录列表
     *
     * @param batchNo
     * @return
     */
    List<CommoditySyncStoreReqVO> listByBatchNo(String batchNo);

}
