package com.htyoudao.youdao.module.bpm.dal.dataobject.business;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.time.LocalDateTime;


/**
 * OA 请假申请 DO
 *
 * @author jason
 * @author 0090
 */
@TableName("business_task_copy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BpmBusinessCopyDO extends BusinessBaseDO {

    /**
     * 业务主键
     */
    @TableId
    private Long id;

    /**
     * 抄送人Id
     */
    private Long copyUserId;
    /**
     * 抄送人已读标识 0 未读 1已读
     */
    private Integer copyFlag;


    /**
     * 唯一流程ID
     */
    private String procInstId;
}
