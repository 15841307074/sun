package com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO;

import lombok.Data;

import java.util.List;

@Data
public class MaterialOwnDataRespVo {


    private List<MaterialListRespVo> respVoList;


    private List<MaterialErrorVo> errorVoList;
}
