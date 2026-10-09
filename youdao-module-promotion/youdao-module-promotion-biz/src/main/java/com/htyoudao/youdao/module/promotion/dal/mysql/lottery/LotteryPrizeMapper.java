package com.htyoudao.youdao.module.promotion.dal.mysql.lottery;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LotteryPrizeMapper extends BaseMapperX<LotteryPrizeDO> {

    int updateBatchByCode(@Param("list") List<LotteryPrizeDO> list);




}
