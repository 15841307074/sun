package com.htyoudao.youdao.module.commodity.controller.admin.materialLog.VO;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import lombok.Data;

@Data
public class MaterialLogReqVo extends PageParam {


    private Long storeId;

    private Long orgId;

}
