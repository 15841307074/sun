package com.htyoudao.youdao.module.promotion.service.bzsplicing;

import com.htyoudao.youdao.module.promotion.controller.admin.order.vo.SplicingOrderConfigReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.order.vo.SplicingOrderConfigRespVO;

public interface BzSplicingOrderServcie {
    void configSaveOrUpdate(SplicingOrderConfigReqVO splicingOrderConfigReqVO);

    SplicingOrderConfigRespVO configInfo();
}
