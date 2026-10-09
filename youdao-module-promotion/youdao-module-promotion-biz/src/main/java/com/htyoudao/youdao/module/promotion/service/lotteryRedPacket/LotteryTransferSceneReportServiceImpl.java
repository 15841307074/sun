package com.htyoudao.youdao.module.promotion.service.lotteryRedPacket;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket.LotteryTransferRecordDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket.LotteryTransferSceneReportDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryTransferRecordMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryTransferSceneReportMapper;
import org.springframework.stereotype.Service;

@Service
public class LotteryTransferSceneReportServiceImpl extends ServiceImpl<LotteryTransferSceneReportMapper, LotteryTransferSceneReportDO> implements LotteryTransferSceneReportService{
}
