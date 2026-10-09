package com.htyoudao.youdao.module.commodity.service.inventory.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.ImportLogRecord;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialLogDO;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.ImportLogRecordMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.RawMaterialLogMapper;
import com.htyoudao.youdao.module.commodity.service.inventory.ImportLogRecordService;
import com.htyoudao.youdao.module.commodity.service.materialLog.RawMaterialLogService;
import org.springframework.stereotype.Service;

@Service
public class ImportLogRecordServiceImpl extends ServiceImpl<ImportLogRecordMapper, ImportLogRecord> implements ImportLogRecordService {
}
