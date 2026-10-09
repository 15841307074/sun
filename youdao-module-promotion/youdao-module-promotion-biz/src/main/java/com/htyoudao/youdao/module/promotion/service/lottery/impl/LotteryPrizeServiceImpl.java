package com.htyoudao.youdao.module.promotion.service.lottery.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket.LotteryTransferRecordDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryPrizeMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryTransferRecordMapper;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryPrizeService;
import com.htyoudao.youdao.module.promotion.service.lotteryRedPacket.LotteryTransferRecordService;
import org.springframework.stereotype.Service;

@Service
public class LotteryPrizeServiceImpl extends ServiceImpl<LotteryPrizeMapper, LotteryPrizeDO> implements LotteryPrizeService {
}
