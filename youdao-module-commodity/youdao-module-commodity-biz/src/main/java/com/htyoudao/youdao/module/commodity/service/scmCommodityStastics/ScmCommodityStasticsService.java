package com.htyoudao.youdao.module.commodity.service.scmCommodityStastics;

import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodityStastic.VO.ScmCommodityStasticRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodityStastic.VO.ScmStasticsReqVO;

import java.util.List;

public interface ScmCommodityStasticsService {

    List<ScmCommodityStasticRespVO> selectList(ScmStasticsReqVO scmStasticsReqVO, int value);
}
