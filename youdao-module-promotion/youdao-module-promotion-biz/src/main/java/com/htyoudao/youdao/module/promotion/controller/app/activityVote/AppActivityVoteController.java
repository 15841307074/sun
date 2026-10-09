package com.htyoudao.youdao.module.promotion.controller.app.activityVote;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.util.concurrent.RateLimiter;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.promotion.controller.app.activityVote.vo.*;
import com.htyoudao.youdao.module.promotion.service.activityVoteApp.ActivityVoteAppService;
import com.htyoudao.youdao.module.promotion.util.redis.RedisForMember;
import com.htyoudao.youdao.module.promotion.util.string.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.CLAIM_COUPON_LIMITER;

@RestController
@RequestMapping("/promotion/activity-vote")
@Tag(name = "投票活动小程序", description = "投票活动小程序接口")
public class AppActivityVoteController {

    private static final String HEAD_NAME = "Authorization";

    private static final String TOKEN_PARAMETER = "token";

    @Resource
    private ActivityVoteAppService activityVoteAppService;

    // 投票限流：每秒 10 个令牌，参考有奖问答
    private static final RateLimiter rateLimiter = RateLimiter.create(10);

    @GetMapping("/detail")
    @Operation(summary = "获取投票活动详情")
    public CommonResult<AppVoteDetailVO> getVoteDetail(@RequestParam("activityId") Long activityId,
                                                       @RequestParam(value = "storeId", required = false) Long storeId) {
        String mobile = SecurityFrameworkUtils.getLoginMobile();
        return success(activityVoteAppService.getVoteDetail(activityId, storeId, mobile));
    }

    @PostMapping("/doVote")
    @Operation(summary = "执行投票")
    public CommonResult<AppVoteResultVO> doVote(@Valid @RequestBody AppVoteActionReqVO reqVO, HttpServletRequest request) {
        boolean acquire = rateLimiter.tryAcquire();
        if (!acquire) {
            throw exception(CLAIM_COUPON_LIMITER);
        }
        String mobile = SecurityFrameworkUtils.getLoginMobile();
        String token = SecurityFrameworkUtils.obtainAuthorization(request, HEAD_NAME, TOKEN_PARAMETER);
        Map<String, String> memberFromRedis = RedisForMember.getMemberFromRedis(token);

        if (StringUtils.isNotEmpty(mobile)){
            reqVO.setMemberMobile(Long.parseLong(mobile));
            reqVO.setMemberId(SecurityFrameworkUtils.getLoginUserId());
            return success(activityVoteAppService.doVote(reqVO));
        }

        if (ObjectUtil.isNotEmpty(memberFromRedis)){
            mobile = memberFromRedis.get("mobile");
            if (StringUtils.isNotEmpty(mobile)){
                reqVO.setMemberMobile(Long.parseLong(mobile));
                reqVO.setMemberId(SecurityFrameworkUtils.getLoginUserId());
                return success(activityVoteAppService.doVote(reqVO));
            }
        }

        return CommonResult.error(400, "请先登录");
    }

    @GetMapping("/ranking")
    @Operation(summary = "获取投票排行榜")
    public CommonResult<AppVoteRankingVO> getRanking(@RequestParam("activityId") Long activityId) {
        return success(activityVoteAppService.getRanking(activityId));
    }

    @GetMapping("/getShareVO")
    @Operation(summary = "获取分享信息")
    public CommonResult<AppVoteShareVO> getShareVO(@RequestParam("activityId") Long activityId) {
        return success(activityVoteAppService.getShareVO(activityId));
    }

    @GetMapping("/myRewards")
    @Operation(summary = "我的奖品列表")
    public CommonResult<java.util.List<AppVoteMyRewardRespVO>> getMyRewards(@RequestParam("activityId") Long activityId, HttpServletRequest request) {

        String mobile = SecurityFrameworkUtils.getLoginMobile();
        String token = SecurityFrameworkUtils.obtainAuthorization(request, HEAD_NAME, TOKEN_PARAMETER);
        Map<String, String> memberFromRedis = RedisForMember.getMemberFromRedis(token);

        if (StringUtils.isNotEmpty(mobile)){
            return success(activityVoteAppService.getMyRewards(activityId, Long.parseLong(mobile)));
        }

        if (ObjectUtil.isNotEmpty(memberFromRedis)){
            mobile = memberFromRedis.get("mobile");
            if (StringUtils.isNotEmpty(mobile)){
                return success(activityVoteAppService.getMyRewards(activityId, Long.parseLong(mobile)));
            }
        }

        return CommonResult.error(400, "请先登录");


    }

    @PostMapping("/saveAddress")
    @Operation(summary = "实物奖品保存收货地址")
    public CommonResult<Boolean> saveAddress(@Valid @RequestBody AppVoteSaveAddressReqVO reqVO) {
        String mobile = SecurityFrameworkUtils.getLoginMobile();
        if (mobile == null) {
            return CommonResult.error(400, "请先登录");
        }
        activityVoteAppService.saveAddress(reqVO, Long.parseLong(mobile));
        return success(true);
    }
}
