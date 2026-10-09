package com.htyoudao.youdao.module.system.dal.dataobject.printer;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.system.controller.app.printer.vo.PrintCommodityTempVO;
import com.htyoudao.youdao.module.system.controller.app.printer.vo.PrintCommodityVO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class PrinterTable extends BusinessBaseDO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 打印机表主键
     */
    @TableId
    private Long printerTableId;

    /**
     * 门店 ID
     */

    private Long storeId;

    /**
     * 打印机名称
     */
    @NotBlank(message = "打印机名称不能为空")
    private String printerName;

    /**
     * 打印机类型（1 USB，2 云打印机）
     */
    @NotNull(message = "打印机类型不能为空")
    private Integer printerType;

    /**
     * 打印机品牌
     */
    @NotBlank(message = "打印机品牌不能为空")
    private String printerBrand;

    /**
     * USB 端口
     */
    private String usbPort;

    /**
     * 纸张宽度
     */
    private Integer paperWidth;

    /**
     * 打印机序列号
     */
    private String printerSerialNumber;

    /**
     * 打印机设置 IDs
     */
    private String printerSettingIds;

    /**
     * 打印机 key
     */
    private String printerSerialKey;


    private String bindDeviceNumber;

    /**
     * 打印位置
     */
    private String printerLocation;

    /**
     * 打印状态
     */
    private Integer status;

    /**
     * 打印机设置里的所有数据
     */
    @TableField(exist = false)
    List<PrinterSetting> printerSettingList = new ArrayList<>();

    @TableField(exist = false)
    private String PrinterSettingString;
    /**
     * 打印小票模板
     */
    @TableField(exist = false)
    private List<PrintCommodityTempVO> printerTemplateList = new ArrayList<>();
    /**
     * 商品模板标签json
     */
    @TableField(exist = false)
    private List<PrintCommodityVO>  commodityTempTags = new ArrayList<>();


}
