package com.htyoudao.youdao.module.commodity.dal.dataobject.invertory;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("import_log_record")
public class ImportLogRecord extends BusinessBaseDO {

    @Schema(description = "日志记录唯一ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;

    @Schema(description = "日志类型：1配方找不到 2门店下原材料找不到 3门店分类下无对应原材料 4单位不匹配", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer logType;


    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityName;


    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;


}
