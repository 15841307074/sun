package com.htyoudao.youdao.module.promotion.service.lottery;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.api.activityjk.DTO.ActivityJkOrderReqDTO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsNumVo;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryLogVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotterySettingsResVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryTaskReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryTaskVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryTypeVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;

import java.util.List;
import java.util.Map;

public interface LotteryMobileService   {
    /**
     * 获取活动类别列表
     */

    public List<LotteryTypeVO> getLotteryTypeList(Long storeId);
    /**
     * 获取活动详情
     */
    public LotterySettingsResVO getLotteryDetail(LotteryVO lotteryVO);

    /**
     * 用户抽奖记录
     */
    public List<LotteryLogDO> getLotteryLogByMemberId(LotteryLogVO lotteryLogVo);

    /**
     * 设置收获地址
     */
    public int updateReceivingAddress(LotteryLogVO lotteryLogVo);
    /**
     * 抽奖
     */
    public CommonResult lottery(LotteryVO lotteryLogVo);

    /**
     * 抽奖校验
     */
   public CommonResult verifyLottery(LotteryVO lotteryVO);

    /**
     * 抽奖次数
     */
    public LotterySettingsNumVo lotteryNum(LotteryVO lotteryVO);

    public void  delRedis(int type);


    public Integer  getRedis(int type,Long memberId,Long lotteryId);
    Map<Long, List<Long>> getLotteryList(Long storeId, Long memberId);
    List<LotteryTaskVO> getTaskList(LotteryTaskReqVO reqVO);
    Boolean shareCheck(LotteryTaskReqVO reqVO);
    Boolean shareTask(LotteryTaskReqVO reqVO);
    Boolean browseTask(LotteryTaskReqVO reqVO);
    void handleOrderTask(ActivityJkOrderReqDTO reqDTO);
}
