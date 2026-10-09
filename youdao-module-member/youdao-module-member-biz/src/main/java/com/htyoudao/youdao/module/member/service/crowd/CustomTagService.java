package com.htyoudao.youdao.module.member.service.crowd;

import java.util.List;

/**
 * @author dht
 */
public interface CustomTagService {
    List<Long> getMembersByTags(List<Long> tags);
}
