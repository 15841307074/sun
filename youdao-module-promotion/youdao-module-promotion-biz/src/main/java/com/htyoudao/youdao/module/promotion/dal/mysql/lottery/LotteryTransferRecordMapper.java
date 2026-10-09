package com.htyoudao.youdao.module.promotion.dal.mysql.lottery;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket.LotteryTransferRecordDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LotteryTransferRecordMapper extends BaseMapperX<LotteryTransferRecordDO> {
}
