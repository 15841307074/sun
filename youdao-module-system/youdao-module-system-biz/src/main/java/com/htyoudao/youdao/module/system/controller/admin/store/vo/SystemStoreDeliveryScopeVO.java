package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 门店配送范围关系表 DO
 *
 * @author ssz
 */
@Schema(description = "门店配送范围关系表 Request VO")
@Data
public class SystemStoreDeliveryScopeVO extends BaseDO {


    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

}
