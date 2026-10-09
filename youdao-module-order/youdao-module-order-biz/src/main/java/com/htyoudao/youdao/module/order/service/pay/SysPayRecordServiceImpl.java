package com.htyoudao.youdao.module.order.service.pay;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.order.dal.dataobject.order.SysPayRecordDO;
import com.htyoudao.youdao.module.order.dal.mysql.SysPayRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-03
 */
@DS(DsNameConstants.SHARDING)
@Slf4j
@Service
public class SysPayRecordServiceImpl extends ServiceImpl<SysPayRecordMapper, SysPayRecordDO> implements SysPayRecordService {
}
