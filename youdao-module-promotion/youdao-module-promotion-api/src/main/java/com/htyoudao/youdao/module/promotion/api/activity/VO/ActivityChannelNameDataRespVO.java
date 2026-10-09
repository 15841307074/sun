package com.htyoudao.youdao.module.promotion.api.activity.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityChannelNameDataRespVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3487118204004321176L;

    @Schema(description = "渠道名称")
    private String name;

    private Long id;
}
