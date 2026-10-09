package com.htyoudao.youdao.module.promotion.service.lotteryRedPacket;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket.LotteryTransferRecordDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.market.SmsMarketDeptDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryTransferRecordMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.market.SmsMarketDeptMapper;
import com.htyoudao.youdao.module.promotion.service.market.SmsMarketDeptService;
import org.springframework.stereotype.Service;

@Service
public class LotteryTransferRecordServiceImpl extends ServiceImpl<LotteryTransferRecordMapper, LotteryTransferRecordDO> implements LotteryTransferRecordService {
}
