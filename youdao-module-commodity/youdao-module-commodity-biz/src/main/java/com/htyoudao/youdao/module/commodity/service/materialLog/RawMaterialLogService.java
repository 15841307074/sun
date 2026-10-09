package com.htyoudao.youdao.module.commodity.service.materialLog;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.api.VO.CommoditySplitMaterialReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLog.VO.MaterialLogReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLog.VO.MaterialLogRespVo;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialLogDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialLossRecordDO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 日志记录Service
 */
public interface RawMaterialLogService extends IService<RawMaterialLogDO> {


    public void asyncSaveLog(RawMaterialLogDO log);

    PageResult<MaterialLogRespVo> logList(MaterialLogReqVo pageReq);



}
