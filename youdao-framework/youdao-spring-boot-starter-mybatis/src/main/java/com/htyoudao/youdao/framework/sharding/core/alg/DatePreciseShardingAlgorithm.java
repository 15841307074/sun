package com.htyoudao.youdao.framework.sharding.core.alg;

import lombok.Getter;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;

/**
 * 根据时间分片
 *
 * @author liuzhaowang
 */
@Getter
public class DatePreciseShardingAlgorithm implements StandardShardingAlgorithm<LocalDateTime> {

    private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");

    @Override
    public Collection<String> doSharding(Collection<String> tables, RangeShardingValue<LocalDateTime> rangeValue) {
        LocalDateTime start = rangeValue.getValueRange().lowerEndpoint();
        LocalDateTime end = rangeValue.getValueRange().upperEndpoint();

       Collection<String> result = new LinkedHashSet<>();
        while (!start.isAfter(end)) {
            String tableSuffix = start.format(YEAR_MONTH_FORMAT);
            tables.stream()
                .filter(t -> t.endsWith(tableSuffix))
                .findFirst()
                .ifPresent(result::add);
            start = start.plusMonths(1).withDayOfMonth(1);
        }
        return result;
    }

    @Override
    public String doSharding(Collection<String> tables, PreciseShardingValue<LocalDateTime> preciseValue) {
       return preciseValue.getLogicTableName() + "_" + preciseValue.getValue().format(YEAR_MONTH_FORMAT);
    }


    @Override
    public void init(Properties properties) {

    }

    @Override
    public String getType() {
        return "HIS_DATE_BASED";
    }
}
