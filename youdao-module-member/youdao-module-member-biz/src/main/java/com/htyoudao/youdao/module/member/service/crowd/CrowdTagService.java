package com.htyoudao.youdao.module.member.service.crowd;

import java.util.List;

/**
 * @author lbw
 *
 * 人群标签接口
 */
public interface CrowdTagService {
    List<Long> getTagsByCrowdId(Long id);
}
