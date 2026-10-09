package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 会员 聚合请求参数对象 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggMemberRequestVO {

    @NotNull
    @Schema(description = "时间类型 1: 昨日 2: 近七日 3: 近三十日")
    private Integer type;

    @Schema(description = "用户类型 1: 新客 2: 老客 3: 会员  -1:全部")
    private Integer userType;

    @Schema(description = "门店 ID 预留字段")
    private List<Long> storeIds;


    public AggregationRequestVO buildBaseRequest() {
        AggregationRequestVO requestVO = new AggregationRequestVO();
        requestVO.setStoreIds(storeIds);
        if (!Objects.equals(userType, -1)) {
            requestVO.setUserType(userType);
        }
        LocalDateTime[] beforeTimeRange = beforeTimeRange(type);
        requestVO.setBeforeTimeStart(beforeTimeRange[0]);
        requestVO.setBeforeTimeEnd(beforeTimeRange[1]);

        LocalDateTime[] currentTimeRange = currentTimeRange(type);
        requestVO.setCurrentTimeStart(currentTimeRange[0]);
        requestVO.setCurrentTimeEnd(currentTimeRange[1]);
        return requestVO;
    }


    public static LocalDateTime[] currentTimeRange(int type) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate startDate = switch (type) {
            case 1 -> yesterday;
            case 2 -> today.minusDays(7);
            case 3 -> today.minusDays(30);
            default -> throw new IllegalArgumentException("Unsupported time type: " + type);
        };

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = yesterday.atTime(LocalTime.MAX); // 23:59:59.999999999
        return new LocalDateTime[]{startDateTime, endDateTime};
    }

    public static void main(String[] args) {
        LocalDateTime[] currentDateTimes = currentTimeRange(3);
        LocalDateTime[] beforeDateTimes = beforeTimeRange(3);

        System.out.println(currentDateTimes[0] + "" + currentDateTimes[1]);
        System.out.println(beforeDateTimes[0] + "" + beforeDateTimes[1]);

    }


    /**
     * 获取环比时间段
     */
    public static LocalDateTime[] beforeTimeRange(int type) {
        LocalDate today = LocalDate.now();

        switch (type) {
            case 1: // 昨天 => 前天
                LocalDate frontDay = today.minusDays(2);
                return new LocalDateTime[]{frontDay.atStartOfDay(), frontDay.atTime(LocalTime.MAX)};

            case 2: // 近七日 => 前七日
                LocalDate endDay7 = today.minusDays(8);
                LocalDate startDay7 = today.minusDays(14);
                return new LocalDateTime[]{startDay7.atStartOfDay(), endDay7.atTime(LocalTime.MAX)};

            case 3: // 近三十日 => 前三十日
                LocalDate endDay30 = today.minusDays(31);
                LocalDate startDay30 = today.minusDays(60);
                return new LocalDateTime[]{startDay30.atStartOfDay(), endDay30.atTime(LocalTime.MAX)};
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

}
