package com.htyoudao.youdao.module.order.controller.print.VO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 该类代表整个打印配置，包含了不同角色（如店铺、会员、厨房、配送）的打印信息。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrintConfigVO {
    @Schema(description = "店铺的打印信息")
    private PrintInfoStoreVO store = new PrintInfoStoreVO();
    @Schema(description = "会员的打印信息")
    private PrintInfoMemberVO member= new PrintInfoMemberVO();;
    @Schema(description = "厨房的打印信息")
    private PrintInfoKitchenVO kitchen= new PrintInfoKitchenVO();;
    @Schema(description = "配送的打印信息")
    private PrintInfoDeliveryVO delivery= new PrintInfoDeliveryVO();;
}