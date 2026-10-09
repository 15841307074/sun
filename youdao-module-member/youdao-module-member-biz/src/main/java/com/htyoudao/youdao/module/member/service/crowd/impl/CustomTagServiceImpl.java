package com.htyoudao.youdao.module.member.service.crowd.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.CrowdTagDO;
import com.htyoudao.youdao.module.member.dal.dataobject.tag.MemberTagDO;
import com.htyoudao.youdao.module.member.dal.mysql.tag.MemberTagMapper;
import com.htyoudao.youdao.module.member.service.crowd.CustomTagService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author dht
 */
@Service
public class CustomTagServiceImpl implements CustomTagService {

    @Resource
    private MemberTagMapper memberTagMapper;
    @Override
    public List<Long> getMembersByTags(List<Long> tags) {
        LambdaQueryWrapper<MemberTagDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(MemberTagDO::getTagId, tags);
        List<MemberTagDO> memberTagList = memberTagMapper.selectList(wrapper);
        return convertToMemberIdList(memberTagList);
    }

    /**
     * 将List<MemberTagDO>转换为包含memberId的List<Long>
     * @param memberTagList 源列表（MemberTagDO对象集合）
     * @return 提取出的memberId列表（Long类型）
     */
    private List<Long> convertToMemberIdList(List<MemberTagDO> memberTagList) {
        // 处理空列表，避免空指针异常
        if (memberTagList == null || memberTagList.isEmpty()) {
            return List.of();
        }

        // 流式处理：提取每个MemberTagDO的MemberId，收集为List<Long>
        return memberTagList.stream()
                .map(MemberTagDO::getMemberId) // 调用getter方法获取tagId
                .toList();
    }
}
