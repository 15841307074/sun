package com.htyoudao.youdao.module.order.controller.app.sq.service;


import com.htyoudao.youdao.module.order.controller.app.sq.VO.ProcessCodesResp;

import java.util.List;

public interface SqProcessService {

    ProcessCodesResp processCodes(List<String> codes);

    ProcessCodesResp processCodesWithAppid(List<String> codes, String appid);
}
