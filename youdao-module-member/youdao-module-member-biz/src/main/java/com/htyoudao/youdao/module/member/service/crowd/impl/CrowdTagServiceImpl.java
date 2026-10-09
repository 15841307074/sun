package com.htyoudao.youdao.module.member.service.crowd.impl;

import com.htyoudao.youdao.module.member.dal.dataobject.crowd.CrowdTagDO;
import com.htyoudao.youdao.module.member.dal.mysql.crowd.CrowdTagMapper;
import com.htyoudao.youdao.module.member.service.crowd.CrowdTagService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lbw
 */
@Service
public class CrowdTagServiceImpl implements CrowdTagService {

    @Resource
    private CrowdTagMapper crowdTagMapper;
    @Override
    public List<Long> getTagsByCrowdId(Long id) {
        List<CrowdTagDO> list = crowdTagMapper.selectList("crowd_id", id);
        return convertToTagIdList(list);
    }

    /**
     * 将List<CrowdTagDO>转换为包含tagId的List<Long>
     * @param crowdTagList 源列表（CrowdTagDO对象集合）
     * @return 提取出的tagId列表（Long类型）
     */
    private List<Long> convertToTagIdList(List<CrowdTagDO> crowdTagList) {
        // 处理空列表，避免空指针异常
        if (crowdTagList == null || crowdTagList.isEmpty()) {
            return List.of();
        }

        // 流式处理：提取每个CrowdTagDO的tagId，收集为List<Long>
        return crowdTagList.stream()
                .map(CrowdTagDO::getTagId) // 调用getter方法获取tagId
                .toList();
    }
}
