package com.htyoudao.youdao.module.system.controller.admin.user.vo.user;

import com.htyoudao.youdao.framework.excel.core.annotations.DictFormat;
import com.htyoudao.youdao.framework.excel.core.convert.DictConvert;
import com.htyoudao.youdao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 用户 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class UserImportExcelVO {

    @ExcelProperty(value = "手机号码", index = 0)
    @NotBlank(message = "手机号码不能为空")
    private String mobile;
    @ExcelProperty(value = "账号",index = 1)
    @NotBlank(message = "账号")
    private String username;
    @ExcelProperty(value = "显示名",index = 2)
    @NotBlank(message = "显示名")
    private String nickname;
//    @ExcelProperty(value = "账号状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

}
