package com.htyoudao.youdao.module.commodity.controller.admin.afterorder.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class AfterOrderDeleteMoreReq {
    List<Long> afterIds = new ArrayList<>();
}
