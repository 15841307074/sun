package com.htyoudao.youdao.module.analysis.dal.es;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

@Mapper
public interface BzOrderMapper extends ElasticsearchRepository<BzOrder, Long> {

}
