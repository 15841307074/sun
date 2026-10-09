package com.htyoudao.youdao.module.member.service.crowd.impl;

import com.htyoudao.youdao.module.member.dal.dataobject.crowd.CrowdStoreDO;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.CrowdTagDO;
import com.htyoudao.youdao.module.member.dal.mysql.crowd.CrowdStoreMapper;
import com.htyoudao.youdao.module.member.service.crowd.CrowdStoreService;
import com.htyoudao.youdao.module.member.service.crowd.CrowdTagService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lbw
 */
@Service
public class CrowdStoreServiceImpl implements CrowdStoreService {
    @Resource
    private CrowdStoreMapper crowdStoreMapper;

    @Override
    public List<Long> getStoreIdsByCrowdId(Long id) {
        List<CrowdStoreDO> crowdStoreList = crowdStoreMapper.selectList("crowd_id", id);
        return convertToStoreIdList(crowdStoreList);
    }

    /**
     * 将List<CrowdStoreDO>转换为包含storeId的List<Long>
     * @param crowdStoreList 源列表（CrowdStoreDO对象集合）
     * @return 提取出的storeId列表（Long类型）
     */
    private List<Long> convertToStoreIdList(List<CrowdStoreDO> crowdStoreList) {
        // 处理空列表，避免空指针异常
        if (crowdStoreList == null || crowdStoreList.isEmpty()) {
            return List.of();
        }

        // 流式处理：提取每个CrowdTagDO的tagId，收集为List<Long>
        return crowdStoreList.stream()
                .map(CrowdStoreDO::getStoreId) // 调用getter方法获取tagId
                .toList();
    }
}
