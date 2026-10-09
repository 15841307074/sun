package com.htyoudao.youdao.module.member.dal.dataobject.crowd;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("crowd_member_ref")
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberCrowdRefDO {

    @TableId(value = "id")
    private Long id;

    private Long memberId;

    private Long crowdId;


}
