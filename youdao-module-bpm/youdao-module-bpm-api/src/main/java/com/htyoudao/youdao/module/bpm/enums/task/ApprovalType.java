package com.htyoudao.youdao.module.bpm.enums.task;

/**
 * 详细的审批类型枚举
 */
public enum ApprovalType {
    SINGLE,      // 单人审批
    SEQUENTIAL,  // 依次审批
    ALL,         // 会签（所有人都要审批）
    ANY          // 或签（任意一人审批即可）
}