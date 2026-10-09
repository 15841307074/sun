package com.htyoudao.youdao.module.promotion.service.activityVoteApp;

import com.htyoudao.youdao.module.promotion.controller.app.activityVote.vo.*;

import java.util.List;

public interface ActivityVoteAppService {

    AppVoteDetailVO getVoteDetail(Long activityId, Long storeId, String memberMobile);

    AppVoteResultVO doVote(AppVoteActionReqVO reqVO);

    AppVoteRankingVO getRanking(Long activityId);

    AppVoteShareVO getShareVO(Long activityId);

    List<AppVoteMyRewardRespVO> getMyRewards(Long activityId, Long memberMobile);

    void saveAddress(AppVoteSaveAddressReqVO reqVO, Long memberMobile);
}
