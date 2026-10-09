package com.htyoudao.youdao.module.member.service.crowd;

import java.util.List;

/**
 * @author lbw
 *
 * 人群门店接口
 */
public interface CrowdStoreService {
    List<Long> getStoreIdsByCrowdId(Long id);
}
