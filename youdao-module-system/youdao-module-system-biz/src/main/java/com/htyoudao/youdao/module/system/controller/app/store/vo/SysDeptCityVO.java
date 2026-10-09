package com.htyoudao.youdao.module.system.controller.app.store.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class SysDeptCityVO {
    private String letter;
    private Set<String> data;

}
