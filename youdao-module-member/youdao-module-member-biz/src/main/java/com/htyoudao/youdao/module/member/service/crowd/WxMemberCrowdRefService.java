package com.htyoudao.youdao.module.member.service.crowd;

import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.controller.app.address.vo.CheckWithinDeliveryRangeReqVO;
import com.htyoudao.youdao.module.member.controller.app.address.vo.WxMemberAddressReqMemberVO;
import com.htyoudao.youdao.module.member.controller.app.address.vo.WxMemberAddressReqVO;
import com.htyoudao.youdao.module.member.controller.app.address.vo.WxMemberAddressRespVO;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.MemberCrowdRefDO;

import java.util.List;
import java.util.Map;

/**
 * 会员人群
 *
 * @author lbw
 * @date 2025-08-7
 */
public interface WxMemberCrowdRefService {

    /**
     * 清空数据
     */
    void trunc();

    void batchWrite(List<MemberCrowdRefDO> memberCrowdRefList);

    /**
     * 判断会员是否在人群中
     * @param memberId memberId
     * @param crowdId crowdId
     * @return Boolean
     */
    Boolean memberExist(Long memberId, String crowdId);

    /**
     * 查询会员的人群ids
     * @param memberId memberId
     * @return Boolean
     */
    List<Long> selectCrowdIdsByMemberId(Long memberId);
}
