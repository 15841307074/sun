package com.htyoudao.youdao.module.member.service.crowd.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.MemberCrowdRefDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.member.dal.mysql.crowd.WxMemberCrowdRefMapper;
import com.htyoudao.youdao.module.member.service.crowd.WxMemberCrowdRefService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WxMemberCrowdRefServiceImpl implements WxMemberCrowdRefService {

    @Resource
    private WxMemberCrowdRefMapper wxMemberCrowdRefMapper;


    @Override
    public void trunc() {
        wxMemberCrowdRefMapper.truncateTable();
    }

    @Override
    public void batchWrite(List<MemberCrowdRefDO> memberCrowdRefList) {
        wxMemberCrowdRefMapper.insertBatch(memberCrowdRefList);
    }

    @Override
    public Boolean memberExist(Long memberId, String crowdId) {
        List<Long> longList = Arrays.stream(crowdId.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();

        QueryWrapper<MemberCrowdRefDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId);
        queryWrapper.in("crowd_id", longList);
        return wxMemberCrowdRefMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public List<Long> selectCrowdIdsByMemberId(Long memberId) {
        MPJLambdaWrapper<MemberCrowdRefDO> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(MemberCrowdRefDO::getCrowdId);
        queryWrapper.eq(MemberCrowdRefDO::getMemberId, memberId);
        return wxMemberCrowdRefMapper.selectJoinList(Long.class, queryWrapper);
    }
}
