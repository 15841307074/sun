package com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.analysis.enums.PeriodTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 经营异常提醒 - 人×门店快照 DO
 *
 * <p>对应表 {@code analysis_user_store_snapshot}。
 *
 * <p>语义：跑批当时，这个人管辖了哪些门店。
 * 为什么要快照而不是实时查？因为需求要的是「<b>当时</b>这个人管了多少门店」，
 * 事后门店划分调整了，不应该影响历史报告的展示。
 *
 * <p>分周、月两套：
 * <ul>
 *   <li>周快照（{@code periodType=1}）：每周一跑，period_start 存周一</li>
 *   <li>月快照（{@code periodType=2}）：每月 1 号跑，period_start 存该月 1 号</li>
 * </ul>
 * 月快照是后来加的——之前只做周度，现在月度也要自己做。
 *
 * <p>这张表有两个消费方：
 * <ol>
 *   <li>给报告表提供 {@code store_count}（统计门店）数字</li>
 *   <li>给其他同事提供批量查询函数：传 userIds 返回 Map&lt;userId, List&lt;storeId&gt;&gt;</li>
 * </ol>
 *
 * @see PeriodTypeEnum
 */
@TableName("analysis_user_store_snapshot")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserStoreSnapshotDO extends BusinessBaseDO {

    /**
     * 主键，MyBatis-Plus ASSIGN_ID 雪花算法生成
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 周期类型：1=周 2=月
     *
     * @see PeriodTypeEnum
     */
    private Integer periodType;

    /**
     * 周期开始日。周快照存该周周一，月快照存该月 1 号
     */
    private LocalDate periodStart;

    /**
     * 用户ID，对应 system_users.id
     */
    private Long userId;

    /**
     * 门店ID，对应 system_store_info.store_id
     */
    private Long storeId;

    /**
     * 门店名称（快照）。冗余存一份是为了避免每次展示都回查门店表。
     * 长度 255、排序规则 utf8mb4_bin，对齐 system_store_info.store_name
     */
    private String storeName;
}
