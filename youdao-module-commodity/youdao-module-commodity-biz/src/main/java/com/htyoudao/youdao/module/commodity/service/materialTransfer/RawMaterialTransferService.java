package com.htyoudao.youdao.module.commodity.service.materialTransfer;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.*;
import com.htyoudao.youdao.module.commodity.controller.app.materialTransfer.VO.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialLossRecordDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialTransferRecordDO;
import com.htyoudao.youdao.module.commodity.enums.TransferStatus;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 损耗记录Service
 */
public interface RawMaterialTransferService extends IService<RawMaterialTransferRecordDO> {

    PageResult<MaterialTransferRecordPageVO> transferRecordList(MaterialTransferRecordPageReq pageReq);

    List<MaterialTypeVo> transferStatusList();


    Boolean isReceive(MaterialTransferReceiveReq receiveReq);

    Boolean saveTransferRecord(@Valid MaterialTransferSaveReq saveReq);

    Boolean deleteTransferRecord(MaterialTransferDeleteReq transferDeleteReq);

    MaterialTransferRecordPageVO selectInfo(Long id);
}
