package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 会员exl
 * @author dht
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class MemberExlVo   {

    @ExcelProperty(value = "手机号", index = 0)
    private String memberMobile;
}