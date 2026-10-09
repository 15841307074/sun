package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.singleUp;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SingleUpConfirmReqVO {
    private  Long singleUpId;

    private Integer chooseView;

    private List<Long>  packageIds = new ArrayList<>();
}
