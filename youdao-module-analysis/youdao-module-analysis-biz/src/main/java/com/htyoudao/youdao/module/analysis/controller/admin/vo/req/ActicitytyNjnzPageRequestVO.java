package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.htyoudao.youdao.framework.common.enums.UserTypeEnum;
import com.htyoudao.youdao.framework.common.validation.InEnum;
import com.htyoudao.youdao.module.analysis.service.dto.EsAggDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class ActicitytyNjnzPageRequestVO{

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "所属组织")
    private Long orgId;

    public List<Long> storeIds;


    public List<Integer> orderS;


    public Long storeId;

    @Schema(description = "活动ID")
    private Long activityId;


    @NotNull
    @Schema(description = "页码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer pageNo;

    @NotNull
    @Schema(description = "每页条数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer pageSize;


    @Schema(description = "开始时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class) // 序列化
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 反序列化
    private LocalDateTime beforeTimeStart;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class) // 序列化
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 反序列化
    private LocalDateTime beforeTimeEnd;

    @Schema(description = "需要导出的title")
    @InEnum(UserTypeEnum.class)
    private Set<String> fields;



    public EsAggDTO buildCurrentRequest(){
        EsAggDTO esAggDTO = new EsAggDTO();
        if(ObjectUtil.isNotEmpty(beforeTimeStart)){
            esAggDTO.setTimes(new LocalDateTime[]{beforeTimeStart,beforeTimeEnd});
        }
        return esAggDTO;
    }
}
