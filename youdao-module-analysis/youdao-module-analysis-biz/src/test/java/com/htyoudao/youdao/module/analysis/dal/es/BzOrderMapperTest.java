package com.htyoudao.youdao.module.analysis.dal.es;

import com.htyoudao.youdao.module.analysis.AnalysisServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;


@SpringBootTest(classes = AnalysisServerApplication.class)
public class BzOrderMapperTest {

    @Autowired
    private BzOrderMapper orderMapper;

    @Test // 根据 ID 编号数组，查询多条记录
    public void testPageQuery() {
        Iterable<BzOrder> all = orderMapper.findAll(Pageable.ofSize(10));
        all.forEach(System.out::println);
    }

}