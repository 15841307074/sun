package com.htyoudao.youdao.module.system.controller.admin.user.vo.user;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author dht
 * @description: 导入用户部门数据
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class UserDeptImportExcel {

    @ExcelProperty(index = 0)
    private String name;

    @ExcelProperty(index = 1)
    private String deptName;

    @ExcelProperty(index = 2)
    private String mobile;
}
