package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotteryLogExportRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryLogQueryService;
import com.htyoudao.youdao.module.promotion.service.lottery.impl.LotteryLogServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LotteryLogExportPaginationTest {

    private static final Pattern PARAMETER = Pattern.compile("#\\{ew\\.paramNameValuePairs\\.([A-Z0-9_]+)\\}");

    @Test
    void keepsPagingByTimeAndIdAfterAnEarlierRecordBecomesEligible() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), LotteryLogDO.class);
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE lottery_log (id BIGINT PRIMARY KEY, member_id BIGINT,"
                + " create_time TIMESTAMP NOT NULL, tracking_number VARCHAR(32))");
        LocalDateTime drawTime = LocalDateTime.of(2026, 11, 20, 10, 0);
        addLog(jdbc, 10, 10L, drawTime.minusSeconds(1), null);
        addLog(jdbc, 11, 11L, drawTime, "shipped");
        addLog(jdbc, 12, 12L, drawTime, "shipped");
        addLog(jdbc, 13, null, drawTime, "shipped");
        addLog(jdbc, 14, 14L, drawTime.plusSeconds(1), "shipped");
        addLog(jdbc, 15, 15L, drawTime.plusMinutes(2), "shipped");

        LotteryLogServiceImpl service = new LotteryLogServiceImpl();
        LotteryLogQueryService queryService = mock(LotteryLogQueryService.class);
        ReflectionTestUtils.setField(service, "lotteryLogQueryService", queryService);
        when(queryService.selectPage(any(), any())).thenAnswer(call -> {
            Page<LotteryLogDO> page = call.getArgument(0);
            return page.setRecords(selectPage(jdbc, page, call.getArgument(1)));
        });
        LambdaQueryWrapper<LotteryLogDO> filter = new LambdaQueryWrapper<LotteryLogDO>()
                .isNotNull(LotteryLogDO::getTrackingNumber)
                .le(LotteryLogDO::getCreateTime, drawTime.plusMinutes(1))
                .orderByAsc(LotteryLogDO::getCreateTime, LotteryLogDO::getId);
        Function<Page<LotteryLogExportRespVO>, List<LotteryLogExportRespVO>> exportPage =
                ReflectionTestUtils.invokeMethod(service, "createExportPageSupplier", filter);

        assertEquals(List.of("11", "12"), memberIds(exportPage.apply(new Page<>(1, 2))));

        jdbc.update("UPDATE lottery_log SET tracking_number = ? WHERE id = ?", "shipped", 10L);
        assertThrows(NullPointerException.class, () -> exportPage.apply(new Page<>(2, 2)));
        jdbc.update("UPDATE lottery_log SET member_id = ? WHERE id = ?", 13L, 13L);

        assertEquals(List.of("13", "14"), memberIds(exportPage.apply(new Page<>(2, 2))));
        assertEquals(List.of(), exportPage.apply(new Page<>(3, 2)));
    }

    private static List<LotteryLogDO> selectPage(JdbcTemplate jdbc, Page<LotteryLogDO> page,
            LambdaQueryWrapper<LotteryLogDO> filter) {
        var placeholders = PARAMETER.matcher(filter.getSqlSegment());
        List<Object> args = new ArrayList<>();
        String sql = placeholders.replaceAll(parameter -> {
            args.add(filter.getParamNameValuePairs().get(parameter.group(1)));
            return "?";
        });
        args.add(page.getSize());
        args.add(page.offset());
        return jdbc.query("SELECT id, member_id, create_time FROM lottery_log WHERE " + sql + " LIMIT ? OFFSET ?",
                (rs, rowNum) -> {
                    LotteryLogDO row = LotteryLogDO.builder().id(rs.getLong("id"))
                            .memberId(rs.getObject("member_id", Long.class)).prizeType(3).build();
                    row.setCreateTime(rs.getObject("create_time", LocalDateTime.class));
                    return row;
                }, args.toArray());
    }

    private static List<String> memberIds(List<LotteryLogExportRespVO> rows) {
        return rows.stream().map(LotteryLogExportRespVO::getMemberId).toList();
    }

    private static void addLog(JdbcTemplate jdbc, long id, Long memberId, LocalDateTime time, String trackingNumber) {
        jdbc.update("INSERT INTO lottery_log (id, member_id, create_time, tracking_number) VALUES (?, ?, ?, ?)",
                id, memberId, time, trackingNumber);
    }
}
