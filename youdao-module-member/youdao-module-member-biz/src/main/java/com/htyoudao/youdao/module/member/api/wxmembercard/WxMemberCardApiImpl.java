package com.htyoudao.youdao.module.member.api.wxmembercard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.member.api.wxmembercard.dto.WxMemberCardBenefitJobDTO;
import com.htyoudao.youdao.module.member.api.wxmembercard.dto.WxMemberCardJobDTO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmembercard.WxMemberCardDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmembercardbenefit.WxMemberCardBenefitRefDO;
import com.htyoudao.youdao.module.member.dal.mysql.wxmembercard.WxMemberCardMapper;
import com.htyoudao.youdao.module.member.dal.mysql.wxmembercardbenefit.WxMemberCardBenefitRefMapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collections;
import java.util.List;

@DubboService
public class WxMemberCardApiImpl implements WxMemberCardApi {

    @Resource
    private WxMemberCardMapper wxMemberCardMapper;

    @Resource
    private WxMemberCardBenefitRefMapper wxMemberCardBenefitRefMapper;

    @Override
    @DataPermission(enable = false)
    public List<WxMemberCardJobDTO> listMemberCardJobCards(Long businessId) {
        List<WxMemberCardDO> cards = wxMemberCardMapper.selectList(new LambdaQueryWrapper<WxMemberCardDO>()
                .eq(WxMemberCardDO::getCardStatus, 1)
                .eq(WxMemberCardDO::getBusinessId, businessId)
                .eq(WxMemberCardDO::getDeleted, false)
                .orderByAsc(WxMemberCardDO::getMinPointsThreshold)
                .orderByAsc(WxMemberCardDO::getMemberLevel));
        return BeanCopyUtils.copyBeanList(cards, WxMemberCardJobDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<WxMemberCardBenefitJobDTO> listMemberCardJobBenefits(Long businessId, List<Long> memberCardIds) {
        if (memberCardIds == null || memberCardIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<WxMemberCardBenefitRefDO> benefits = wxMemberCardBenefitRefMapper.selectList(new LambdaQueryWrapper<WxMemberCardBenefitRefDO>()
                .in(WxMemberCardBenefitRefDO::getMemberCardId, memberCardIds)
                .eq(WxMemberCardBenefitRefDO::getBusinessId, businessId)
                .eq(WxMemberCardBenefitRefDO::getDeleted, false)
                .orderByAsc(WxMemberCardBenefitRefDO::getMemberCardId)
                .orderByAsc(WxMemberCardBenefitRefDO::getBenefitScene)
                .orderByAsc(WxMemberCardBenefitRefDO::getId));
        return BeanCopyUtils.copyBeanList(benefits, WxMemberCardBenefitJobDTO.class);
    }
}
