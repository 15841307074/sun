package com.htyoudao.youdao.module.system.dal.dataobject.wxstore;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@TableName(value = "store_wecom_config", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreWecomConfigDO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId
    private Long id;

    /** 门店id */
    private Long storeId;

    /** 企微二维码 */
    private String qrCode;

    /**
     * 门店经度
     */
    private double longitude;

    /**
     * 门店维度
     */
    private double latitude;
    /** 企微二维码类型 0 福利官 1 社群 */
    private Integer qrType;
    /** 企微社群二维码 */
    private String qrCommunityCode;

}
