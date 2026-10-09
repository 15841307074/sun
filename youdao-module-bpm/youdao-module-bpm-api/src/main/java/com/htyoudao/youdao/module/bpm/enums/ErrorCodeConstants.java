package com.htyoudao.youdao.module.bpm.enums;


import com.htyoudao.youdao.framework.common.exception.ErrorCode;

/**
 * Bpm 错误码枚举类
 * <p>
 * bpm 系统，使用 1-009-000-000 段
 */
public interface ErrorCodeConstants {

    // ==========  通用流程处理 模块 1-009-000-000 ==========

    // ========== OA 流程模块 1-009-001-000 ==========
    ErrorCode OA_LEAVE_NOT_EXISTS = new ErrorCode(1_009_001_001, "请假申请不存在");

    // ========== 流程模型 1-009-002-000 ==========
    ErrorCode MODEL_KEY_EXISTS = new ErrorCode(1_009_002_000, "已经存在流程标识为【{}】的流程");
    ErrorCode MODEL_NOT_EXISTS = new ErrorCode(1_009_002_001, "流程模型不存在");
    ErrorCode MODEL_KEY_VALID = new ErrorCode(1_009_002_002, "流程标识格式不正确，需要以字母或下划线开头，后接任意字母、数字、中划线、下划线、句点！");
    ErrorCode MODEL_DEPLOY_FAIL_FORM_NOT_CONFIG = new ErrorCode(1_009_002_003, "部署流程失败，原因：流程表单未配置，请点击【修改流程】按钮进行配置");
    ErrorCode MODEL_DEPLOY_FAIL_TASK_CANDIDATE_NOT_CONFIG = new ErrorCode(1_009_002_004, "部署流程失败，" +
            "原因：用户任务({})未配置审批人，请点击【流程设计】按钮，选择该它的【任务（审批人）】进行配置");
    ErrorCode MODEL_DEPLOY_FAIL_BPMN_START_EVENT_NOT_EXISTS = new ErrorCode(1_009_002_005, "部署流程失败，原因：BPMN 流程图中，没有开始事件");
    ErrorCode MODEL_DEPLOY_FAIL_BPMN_USER_TASK_NAME_NOT_EXISTS = new ErrorCode(1_009_002_006, "部署流程失败，原因：BPMN 流程图中，用户任务({})的名字不存在");
    ErrorCode MODEL_UPDATE_FAIL_NOT_MANAGER = new ErrorCode(1_009_002_007, "操作流程失败，原因：你不是该流程({})的管理员");
    ErrorCode MODEL_DEPLOY_FAIL_FIRST_USER_TASK_CANDIDATE_STRATEGY_ERROR = new ErrorCode(1_009_002_008, "部署流程失败，原因：首个任务({})的审批人不能是【审批人自选】");

    // ========== 流程定义 1-009-003-000 ==========
    ErrorCode PROCESS_DEFINITION_KEY_NOT_MATCH = new ErrorCode(1_009_003_000, "流程定义的标识期望是({})，当前是({})，请修改 BPMN 流程图");
    ErrorCode PROCESS_DEFINITION_NAME_NOT_MATCH = new ErrorCode(1_009_003_001, "流程定义的名字期望是({})，当前是({})，请修改 BPMN 流程图");
    ErrorCode PROCESS_DEFINITION_NOT_EXISTS = new ErrorCode(1_009_003_002, "流程定义不存在");
    ErrorCode PROCESS_DEFINITION_IS_SUSPENDED = new ErrorCode(1_009_003_003, "流程定义处于挂起状态");

    // ========== 流程实例 1-009-004-000 ==========
    ErrorCode PROCESS_INSTANCE_NOT_EXISTS = new ErrorCode(1_009_004_000, "流程实例不存在");
    ErrorCode PROCESS_INSTANCE_CANCEL_FAIL_NOT_EXISTS = new ErrorCode(1_009_004_001, "流程取消失败，流程不处于运行中");
    ErrorCode PROCESS_INSTANCE_CANCEL_FAIL_NOT_SELF = new ErrorCode(1_009_004_002, "流程取消失败，该流程不是你发起的");
    ErrorCode PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_CONFIG = new ErrorCode(1_009_004_003, "任务({})的候选人未配置");
    ErrorCode PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_EXISTS = new ErrorCode(1_009_004_004, "任务({})的候选人({})不存在");
    ErrorCode PROCESS_INSTANCE_START_USER_CAN_START = new ErrorCode(1_009_004_005, "发起流程失败，你没有权限发起该流程");
    ErrorCode PROCESS_INSTANCE_CANCEL_FAIL_NOT_ALLOW = new ErrorCode(1_009_004_005, "流程取消失败，该流程不允许取消");
    ErrorCode PROCESS_INSTANCE_HTTP_CALL_ERROR = new ErrorCode(1_009_004_006, "流程 Http 请求调用失败");
    ErrorCode PROCESS_INSTANCE_APPROVE_USER_SELECT_ASSIGNEES_NOT_CONFIG = new ErrorCode(1_009_004_007, "下一个任务({})的审批人未配置");
    ErrorCode PROCESS_INSTANCE_CANCEL_CHILD_FAIL_NOT_ALLOW = new ErrorCode(1_009_004_008, "子流程取消失败，子流程不允许取消");

    // ========== 流程任务 1-009-005-000 ==========
    ErrorCode TASK_OPERATE_FAIL_ASSIGN_NOT_SELF = new ErrorCode(1_009_005_001, "操作失败，原因：该任务的审批人不是你");
    ErrorCode TASK_NOT_EXISTS = new ErrorCode(1_009_005_002, "流程任务不存在");
    ErrorCode TASK_IS_PENDING = new ErrorCode(1_009_005_003, "当前任务处于挂起状态，不能操作");
    ErrorCode TASK_TARGET_NODE_NOT_EXISTS = new ErrorCode(1_009_005_004, " 目标节点不存在");
    ErrorCode TASK_RETURN_FAIL_SOURCE_TARGET_ERROR = new ErrorCode(1_009_005_006, "退回任务失败，目标节点是在并行网关上或非同一路线上，不可跳转");
    ErrorCode TASK_DELEGATE_FAIL_USER_REPEAT = new ErrorCode(1_009_005_007, "任务委派失败，委派人和当前审批人为同一人");
    ErrorCode TASK_DELEGATE_FAIL_USER_NOT_EXISTS = new ErrorCode(1_009_005_008, "任务委派失败，被委派人不存在");
    ErrorCode TASK_SIGN_CREATE_USER_NOT_EXIST = new ErrorCode(1_009_005_009, "任务加签：选择的用户不存在");
    ErrorCode TASK_SIGN_CREATE_TYPE_ERROR = new ErrorCode(1_009_005_010, "任务加签：当前任务已经{}，不能{}");
    ErrorCode TASK_SIGN_CREATE_USER_REPEAT = new ErrorCode(1_009_005_011, "任务加签失败，加签人与现有审批人[{}]重复");
    ErrorCode TASK_SIGN_DELETE_NO_PARENT = new ErrorCode(1_009_005_012, "任务减签失败，被减签的任务必须是通过加签生成的任务");
    ErrorCode TASK_TRANSFER_FAIL_USER_REPEAT = new ErrorCode(1_009_005_013, "任务转办失败，转办人和当前审批人为同一人");
    ErrorCode TASK_TRANSFER_FAIL_USER_NOT_EXISTS = new ErrorCode(1_009_005_014, "任务转办失败，转办人不存在");
    ErrorCode TASK_SIGNATURE_NOT_EXISTS = new ErrorCode(1_009_005_015, "签名不能为空！");
    ErrorCode TASK_REASON_REQUIRE = new ErrorCode(1_009_005_016, "审批意见不能为空！");
    ErrorCode TASK_WITHDRAW_FAIL_PROCESS_NOT_RUNNING = new ErrorCode(1_009_005_017, "撤回失败，流程实例未运行！");
    ErrorCode TASK_WITHDRAW_FAIL_TASK_NOT_EXISTS = new ErrorCode(1_009_005_018, "撤回失败，未查询到用户已办任务！");
    ErrorCode TASK_WITHDRAW_FAIL_NOT_ALLOW = new ErrorCode(1_009_005_019, "撤回失败，此流程不允许撤回操作！");
    ErrorCode TASK_WITHDRAW_FAIL_NEXT_TASK_NOT_ALLOW = new ErrorCode(1_009_005_019, "撤回失败，下一节点不满足撤回条件！");

    // ========== 动态表单模块 1-009-010-000 ==========
    ErrorCode FORM_NOT_EXISTS = new ErrorCode(1_009_010_000, "动态表单不存在");
    ErrorCode FORM_FIELD_REPEAT = new ErrorCode(1_009_010_001, "表单项({}) 和 ({}) 使用了相同的字段名({})");

    // ========== 用户组模块 1-009-011-000 ==========
    ErrorCode USER_GROUP_NOT_EXISTS = new ErrorCode(1_009_011_000, "用户分组不存在");
    ErrorCode USER_GROUP_IS_DISABLE = new ErrorCode(1_009_011_001, "名字为【{}】的用户分组已被禁用");

    // ========== 用户组模块 1-009-012-000 ==========
    ErrorCode CATEGORY_NOT_EXISTS = new ErrorCode(1_009_012_000, "流程分类不存在");
    ErrorCode CATEGORY_NAME_DUPLICATE = new ErrorCode(1_009_012_001, "流程分类名字【{}】重复");
    ErrorCode CATEGORY_CODE_DUPLICATE = new ErrorCode(1_009_012_002, "流程分类编码【{}】重复");
    ErrorCode CATEGORY_DELETE_FAIL_MODEL_USED = new ErrorCode(1_009_012_003, "删除失败，流程分类【{}】已被流程模型使用，请先删除对应的流程模型");

    // ========== BPM 流程监听器 1-009-013-000 ==========
    ErrorCode PROCESS_LISTENER_NOT_EXISTS = new ErrorCode(1_009_013_000, "流程监听器不存在");
    ErrorCode PROCESS_LISTENER_CLASS_NOT_FOUND = new ErrorCode(1_009_013_001, "流程监听器类({})不存在");
    ErrorCode PROCESS_LISTENER_CLASS_IMPLEMENTS_ERROR = new ErrorCode(1_009_013_002, "流程监听器类({})没有实现接口({})");
    ErrorCode PROCESS_LISTENER_EXPRESSION_INVALID = new ErrorCode(1_009_013_003, "流程监听器表达式({})不合法");

    // ========== BPM 流程表达式 1-009-014-000 ==========
    ErrorCode PROCESS_EXPRESSION_NOT_EXISTS = new ErrorCode(1_009_014_000, "流程表达式不存在");

    // ========== BPM 流程表达式 1-009-015-000 ==========
    ErrorCode STORE_BUSINESS_NOT_EXISTS = new ErrorCode(1_009_014_000, "门店业务不存在");
    ErrorCode STORE_BUSINESS_NOT_HAVE_LEADER = new ErrorCode(1_009_014_001, "未配置店长，无法分配任务");
    ErrorCode IS_ALL_NOT_EXIST = new ErrorCode(1_009_014_002, "任务列表可见性有误");

    ErrorCode TASK_NAME_NOT_UNIQUE = new ErrorCode(1_009_014_003, "任务名称有重复");

    ErrorCode LOGIN_APPLICANT_NOT_EQUALS = new ErrorCode(1_009_014_004, "发起人与登录人不一致");

    ErrorCode TASK_NAME_NOT_EXIST = new ErrorCode(1_009_014_005, "任务类型不存在");

    ErrorCode STORE_TASK_HAS_NOT_FLOW_ID = new ErrorCode(1_009_014_006, "门店子任务没有主流程ID");

    ErrorCode USER_DEPT_ERROR = new ErrorCode(1_009_014_007, "BPM角色信息有误");

    ErrorCode OA_PROJECT_ID_QUERY_ERROR = new ErrorCode(1_009_014_008, "缺少OA项目ID");



    // ========== 巡店 1-009-016-000 ==========
    ErrorCode STORE_INSPECTION_ITEM_LOG_NOT_EXISTS = new ErrorCode(1_010_016_001, "巡店项操作变更日志不存在");
    ErrorCode STORE_INSPECTION_RECORD_NOT_EXISTS = new ErrorCode(1_010_016_002, "巡检任务不存在");
    ErrorCode STORE_INSPECTION_RECORD_ITEM_NOT_EXISTS = new ErrorCode(1_010_016_003, "巡店记录明细快照不存在");
    ErrorCode STORE_INSPECTION_RECORD_ITEM_CONFIGURATION_ERROR = new ErrorCode(1_010_016_004, "【{}】配置异常");
    ErrorCode STORE_INSPECTION_RECORD_ITEM_RESULT_ERROR = new ErrorCode(1_010_016_005, "检查结果仅支持：0-不合格、1-合格、2-不适用");
    ErrorCode STORE_INSPECTION_RECORD_ITEM_NULL_ERROR = new ErrorCode(1_010_016_006, "当前巡店记录没有点检项，无法完成巡检");
    ErrorCode STORE_INSPECTION_RECORD_ITEM_NOT_FINISH = new ErrorCode(1_010_016_007, "还有未完成的点检项，无法完成巡检");
    ErrorCode STORE_INSPECTION_RECORD_OVER_TIME = new ErrorCode(1_010_016_008, "当前任务已经过期，无法修改");
    ErrorCode STORE_INSPECTION_DISTANCE_ERROR = new ErrorCode(1_010_016_009, "系统检测到您不在门店范围内，无法提交巡店记录");
    ErrorCode STORE_INSPECTION_RECORD_ALREADY_FINISHED = new ErrorCode(1_010_016_010, "当前任务已经完成，无法修改");
    ErrorCode STORE_INSPECTION_RECORD_NO_PIC_ERROR = new ErrorCode(1_010_016_011, "请上传门头照片");
    ErrorCode STORE_INSPECTION_RECORD_AWARD_ERROR = new ErrorCode(1_010_016_012, "奖惩金额最大6位数");
    ErrorCode STORE_INSPECTION_RECORD_AWARD_ERROR_2 = new ErrorCode(1_010_016_012, "奖惩金额小数点后最多2位数");
    ErrorCode STORE_INSPECTION_RECORD_PIC_LITTLE_ERROR = new ErrorCode(1_010_016_013, "当前点检项至少上传【{}】张图片");
    ErrorCode STORE_INSPECTION_RECORD_PIC_MOST_ERROR = new ErrorCode(1_010_016_014, "至多上传12张图片");
    ErrorCode STORE_INSPECTION_DELETE_ERROR = new ErrorCode(1_010_016_015, "已完成的任务无法删除");



    // ========== 巡店 1-009-017-000 ==========
    ErrorCode INSPECTION_TYPE_ERROR = new ErrorCode(1_010_017_001, "删除大类有误");

    ErrorCode CHECKLIST_PROMPT_LENGTH_TOO_LARGE_ERROR = new ErrorCode(1_010_017_002, "提示语超长");

    ErrorCode INSPECTION_STANDARD_TOO_LARGE_ERROR = new ErrorCode(1_010_017_003, "标准图片数量超过规定数目");

    ErrorCode TYPE_NAME_UNIQUE_ERROR = new ErrorCode(1_010_017_004, "大类不能重复");

    ErrorCode CHECKLIST_NAME_UNIQUE_ERROR = new ErrorCode(1_010_017_005, "点检项不能重复");

    ErrorCode TYPE_NAME_TOO_LARGE_ERROR = new ErrorCode(1_010_017_006, "大类名称过长");

    ErrorCode TYPE_DELETE_ERROR = new ErrorCode(1_010_017_007, "大类不允许删除");

    ErrorCode VISIBILITY_TYPE_ERROR = new ErrorCode(1_010_017_008, "模板指定可见范围有误");

    ErrorCode USER_VISIBILITY_ERROR = new ErrorCode(1_010_017_009, "用户无查看权限");

    ErrorCode TEMPLATE_NAME_BLANK_ERROR = new ErrorCode(1_010_017_019, "模板名不为空");

    ErrorCode TEMPLATE_NAME_TOO_LARGE_ERROR = new ErrorCode(1_010_017_020, "模板名称过长");

    ErrorCode TEMPLATE_NAME_UNIQUE_ERROR = new ErrorCode(1_010_017_021, "模板名称重复");

    ErrorCode VISIBILITY_TYPE_NULL_ERROR = new ErrorCode(1_010_017_029, "选择用户不能为空");

    ErrorCode VISIBILITY_TYPE_USERID_NULL_ERROR = new ErrorCode(1_010_017_030, "指定人员不能为空");

    ErrorCode STORE_INSPECTION_USER_DEPT_ERROR = new ErrorCode(1_010_017_033, "巡店角色信息有误");

    ErrorCode CHECKLIST_TITLE_LENGTH_TOO_LARGE_ERROR = new ErrorCode(1_010_017_034, "标题超长");

    ErrorCode CHECKLIST_IMG_STATE_ERROR = new ErrorCode(1_010_017_035, "图片规则状态选择【不必传】时，不能同时选择其他");


}
