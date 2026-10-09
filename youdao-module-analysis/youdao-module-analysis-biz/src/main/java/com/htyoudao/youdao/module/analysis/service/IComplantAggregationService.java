package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActicitytyNjnzPageRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActivityNjnzRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityNjnzStorePageVO;
import jakarta.validation.Valid;

import java.util.Map;

public interface IComplantAggregationService {


    Map<Integer, Map<String, Object>> query(Long storeId);

}
