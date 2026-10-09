package com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 会员卡权益分页 Request VO")
public class WxMemberCardBenefitPageReqVO extends PageParam {
}
