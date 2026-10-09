package com.htyoudao.youdao.module.order.controller.app.sq.VO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProcessCodesWithAppidReq {

    @NotEmpty(message = "codes 不能为空")
    private List<String> codes;

    @NotBlank(message = "appid 不能为空")
    private String appid;
}
