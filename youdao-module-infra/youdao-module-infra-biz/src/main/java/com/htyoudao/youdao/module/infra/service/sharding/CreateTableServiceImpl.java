package com.htyoudao.youdao.module.infra.service.sharding;

import com.htyoudao.youdao.framework.common.util.date.DateUtils;
import com.htyoudao.youdao.framework.redis.core.utils.RedissonUtils;
import com.htyoudao.youdao.module.infra.dal.mysql.sharding.CreateTableMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * details
 *
 * @author liuzhaowang
 */
@Service
public class CreateTableServiceImpl implements CreateTableService {

    @Resource
    private CreateTableMapper createTableMapper;

    @Override
    public void createShardingTable() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        String suffix = DateUtils.parseDateToStr("yyyyMM", calendar.getTime());
        List<String> tableNames = List.of("bz_order", "bz_order_condiments", "bz_order_log",
                "bz_order_product", "bz_order_product_son", "bz_order_purchase", "bz_order_pay",
                "scm_consumption_record", "scm_warehouse_history", "sys_pay_record", "scm_order",
                "scm_order_detail", "scm_order_status_his");
        tableNames.forEach(tableName -> {
            createTableMapper.createShardingTable(tableName, suffix);
            Set<String> tables = createTableMapper.selectTables(tableName + "_20____");
            RedissonUtils.setCacheSet("sharding:tables:" + tableName, tables);
        });
    }
}
