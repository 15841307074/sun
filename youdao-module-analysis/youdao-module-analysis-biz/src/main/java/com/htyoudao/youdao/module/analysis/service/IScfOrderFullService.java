package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.module.analysis.dal.es.ScmOrderDetailDocument;
import java.time.LocalDateTime;
import java.util.List;

public interface IScfOrderFullService {


    /**
     * 获取供应链采购金额
     * @param storeIds
     * @param timeStart
     * @param timeEnd
     * @return
     */
     Double getGylOrderAmount(List<Long> storeIds, LocalDateTime timeStart, LocalDateTime timeEnd);

    /**
     * 批量获取供应链订单信息
     * @param storeIds
     * @param timeStart
     * @param timeEnd
     * @return
     */
    List<ScmOrderDetailDocument> gylOrderList(List<Long> storeIds, LocalDateTime timeStart, LocalDateTime timeEnd);


}
