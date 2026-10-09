# 学习任务技术文档（历史草案）

> 本文中的“按门店复制完整进度、MySQL访问流水、外部fileId/fileVersion”设计已废弃。当前实现与接口以 `docs/learning-task-api.html`、`docs/learning-task-risk-notes.md` 和 `sql/bpm_learning_task.sql` 为准：用户主进度只保存一份，门店仅保存投影；访问统计写ES；附件使用任务侧SHA-256指纹和进度修订号。

## 1. 数据库表结构

完整 SQL：[bpm_learning_task.sql](../sql/bpm_learning_task.sql)。共 9 张表，采用逻辑删除，不建立数据库外键，由 Service 事务保证一致性。

### 1.1 公共字段

以下字段存在于全部 9 张表中：

| 字段 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `creator` | varchar(64) | `''` | 创建人账号或用户标识 |
| `create_time` | timestamp | 当前时间 | 记录创建时间 |
| `updater` | varchar(64) | `''` | 最后修改人账号或用户标识 |
| `update_time` | timestamp | 当前时间并自动更新 | 最后修改时间 |
| `deleted` | bit(1) | `0` | 逻辑删除：`0未删除、1已删除` |
| `tenant_id` | bigint | `0` | 租户编号，用于租户隔离 |
| `business_id` | bigint | `NULL` | 业务线或项目 ID，用于项目隔离 |

### 1.2 `bpm_learning_task` 学习任务主表

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
|---|---|---|---|---|
| `task_id` | bigint | 否 | 无 | 学习任务主键，雪花 ID |
| `task_name` | varchar(50) | 否 | 无 | 任务名称，最多 50 字 |
| `cover_image_url` | varchar(500) | 否 | 无 | 任务封面图地址 |
| `start_time` | datetime | 否 | 无 | 任务开始时间 |
| `end_time` | datetime | 否 | 无 | 任务结束时间，必须晚于开始时间 |
| `task_description` | varchar(500) | 是 | `NULL` | 任务描述，最多 500 字 |
| `apply_scope` | tinyint | 否 | 无 | 应用范围：`1按门店、2按标签` |
| `store_scope` | tinyint | 是 | `NULL` | 按门店时：`1全部门店、2部分门店`；按标签时为空 |
| `order_limit_type` | tinyint | 否 | `0` | `0不限制订货、1任务开始N天后未完成禁止订货` |
| `order_limit_days` | smallint | 是 | `NULL` | 限制订货等待天数，范围 3 至 365 |
| `online_status` | tinyint | 否 | `0` | `0下架、1上架` |

索引：主键 `task_id`；`online_status + start_time + end_time + deleted` 查询任务状态；`business_id + deleted + create_time` 支持项目内分页。

### 1.3 `bpm_learning_task_attachment` 任务详情附件表

保存任务介绍图片或视频，与学习资料的 `files` 不是同一类附件，每个任务最多 5 条。

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
|---|---|---|---|---|
| `attachment_id` | bigint | 否 | 无 | 任务附件主键 |
| `task_id` | bigint | 否 | 无 | 所属学习任务 ID |
| `attachment_url` | varchar(500) | 否 | 无 | 附件访问地址 |
| `attachment_type` | tinyint | 否 | 无 | `1视频、2图片` |
| `sort` | int | 否 | `0` | 排序，数值越小越靠前 |

索引：`task_id + deleted + sort`，用于按顺序查询任务附件。

### 1.4 `bpm_learning_task_store` 任务指定门店关系表

`apply_scope=1` 且 `store_scope=2` 时保存指定门店。

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint | 否 | 无 | 关系主键 |
| `task_id` | bigint | 否 | 无 | 学习任务 ID |
| `store_id` | bigint | 否 | 无 | 被投放的门店 ID |

索引：唯一索引 `task_id + store_id` 防止重复；`store_id + deleted + task_id` 反查门店任务。

### 1.5 `bpm_learning_task_tag` 任务门店标签关系表

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint | 否 | 无 | 关系主键 |
| `task_id` | bigint | 否 | 无 | 学习任务 ID |
| `tag_id` | bigint | 否 | 无 | 门店标签 ID，多个标签按并集匹配门店 |

索引：唯一索引 `task_id + tag_id` 防止重复；`tag_id + deleted + task_id` 按标签匹配任务。

### 1.6 `bpm_learning_task_material` 任务学习资料关系表

只保存学习资料 ID、任务内规则和排序。资料名称、分类及附件由资料 JSON 提供。

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
|---|---|---|---|---|
| `task_material_id` | bigint | 否 | 无 | 任务资料关系主键；删除重加时生成新 ID |
| `task_id` | bigint | 否 | 无 | 所属学习任务 ID |
| `material_id` | bigint | 否 | 无 | 资料 JSON 中的资料 ID |
| `document_browse_limit_type` | tinyint | 否 | `0` | `0不限制、1限制最少有效浏览时长` |
| `min_document_browse_seconds` | int | 是 | `NULL` | 文档至少有效浏览秒数；不限制时为空 |
| `video_speed_allowed` | bit(1) | 否 | `0` | `0不允许倍速、1允许倍速` |
| `required_flag` | bit(1) | 否 | `1` | `0非必学、1必学` |
| `sort` | int | 否 | `0` | 资料在任务中的排序 |

索引：`task_id + deleted + sort` 查询任务资料；`material_id + deleted` 按资料反查任务引用。

### 1.7 `bpm_learning_task_visit_log` 任务访问记录表

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
|---|---|---|---|---|
| `visit_id` | bigint | 否 | 无 | 访问记录主键 |
| `task_id` | bigint | 否 | 无 | 被访问的任务 ID |
| `store_id` | bigint | 否 | 无 | 本次访问计入的门店 ID |
| `learner_user_id` | bigint | 否 | 无 | 学习店长用户 ID |
| `visit_time` | datetime | 否 | 无 | 实际访问时间 |

索引：`task_id + deleted + visit_time` 统计任务访问；`task_id + store_id + learner_user_id + deleted` 查询个人访问。

### 1.8 `bpm_learning_task_learner_progress` 任务整体进度表

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
|---|---|---|---|---|
| `progress_id` | bigint | 否 | 无 | 任务进度主键 |
| `task_id` | bigint | 否 | 无 | 学习任务 ID |
| `store_id` | bigint | 否 | 无 | 进度所属门店 ID |
| `learner_user_id` | bigint | 否 | 无 | 学习店长用户 ID |
| `study_status` | tinyint | 否 | `0` | `0未开始、1进行中、2已完成` |
| `progress_rate` | decimal(5,2) | 否 | `0.00` | 任务学习进度百分比 |
| `required_material_count` | int | 否 | `0` | 当前有效必学资料总数 |
| `completed_material_count` | int | 否 | `0` | 已完成必学资料数量 |
| `valid_study_seconds` | bigint | 否 | `0` | 任务累计有效学习秒数 |
| `start_time` | datetime | 是 | `NULL` | 首次开始学习时间 |
| `last_study_time` | datetime | 是 | `NULL` | 最近有效学习时间 |
| `completed_time` | datetime | 是 | `NULL` | 首次完成任务时间 |

索引：唯一索引 `task_id + store_id + learner_user_id`；`task_id + study_status + deleted` 支持状态统计。

### 1.9 `bpm_learning_task_material_progress` 单资料进度表

资料进度按该资料当前有效附件的完成数量计算。

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
|---|---|---|---|---|
| `progress_id` | bigint | 否 | 无 | 资料进度主键 |
| `task_id` | bigint | 否 | 无 | 学习任务 ID |
| `task_material_id` | bigint | 否 | 无 | 任务资料关系 ID |
| `material_id` | bigint | 否 | 无 | 外部学习资料 ID |
| `store_id` | bigint | 否 | 无 | 进度所属门店 ID |
| `learner_user_id` | bigint | 否 | 无 | 学习店长用户 ID |
| `study_status` | tinyint | 否 | `0` | `0未开始、1进行中、2已完成` |
| `progress_rate` | decimal(5,2) | 否 | `0.00` | 已完成附件数占当前有效附件总数的百分比 |
| `valid_study_seconds` | bigint | 否 | `0` | 资料下全部当前有效附件的累计学习秒数 |
| `start_time` | datetime | 是 | `NULL` | 首次开始资料学习时间 |
| `last_study_time` | datetime | 是 | `NULL` | 最近有效学习时间 |
| `completed_time` | datetime | 是 | `NULL` | 首次完成资料时间 |

索引：唯一索引 `task_material_id + store_id + learner_user_id`；`task_id + store_id + learner_user_id + deleted` 支持任务汇总。

### 1.10 `bpm_learning_task_file_progress` 单附件进度表

资料 JSON 的 `files` 即附件列表，`external_file_id` 保存每个附件的 `fileId`。每个附件独立记录是否完成。

| 字段 | 类型 | 允许空 | 默认值 | 说明 |
|---|---|---|---|---|
| `file_progress_id` | bigint | 否 | 无 | 附件进度主键 |
| `task_id` | bigint | 否 | 无 | 学习任务 ID |
| `task_material_id` | bigint | 否 | 无 | 任务资料关系 ID |
| `material_id` | bigint | 否 | 无 | 外部学习资料 ID |
| `store_id` | bigint | 否 | 无 | 进度所属门店 ID |
| `learner_user_id` | bigint | 否 | 无 | 学习店长用户 ID |
| `external_file_id` | bigint | 否 | 无 | 资料 JSON 中附件的 `fileId` |
| `file_type` | tinyint | 否 | 无 | 附件类型编码，沿用资料系统定义 |
| `file_version` | varchar(64) | 否 | 无 | 附件内容版本，内容变化时必须更新 |
| `valid_study_seconds` | bigint | 否 | `0` | 服务端根据心跳累计的有效学习秒数 |
| `last_position_seconds` | bigint | 否 | `0` | 视频最后有效播放位置；非视频为 0 |
| `completed_flag` | bit(1) | 否 | `0` | 当前附件是否完成：`0未完成、1已完成` |
| `start_time` | datetime | 是 | `NULL` | 首次打开附件时间 |
| `last_study_time` | datetime | 是 | `NULL` | 最近有效学习时间 |
| `completed_time` | datetime | 是 | `NULL` | 首次完成附件时间 |

索引：唯一索引 `task_material_id + store_id + learner_user_id + external_file_id + file_version`，保证一个附件版本只有一条进度；`task_id + store_id + learner_user_id + deleted` 查询任务内附件进度。

## 2. PC 管理后台接口（Swagger 风格）

**Tag：** `管理后台 - 学习任务`  
**Base URL：** `/bpm/learning-task`  
**Content-Type：** GET 使用 Query 参数，POST/PUT 使用 `application/json`。

所有接口统一返回 `CommonResult<T>`：

| 字段 | 类型 | 必返 | 说明 |
|---|---|---|---|
| `code` | Integer | 是 | 业务状态码，成功为 `0` |
| `data` | T | 是 | 业务数据；无数据时可为 `null` |
| `msg` | String | 是 | 状态说明 |

分页数据统一为 `PageResult<T>`：`data.list` 为数据数组，`data.total` 为总记录数。

### 2.1 GET `/page` 分页查询学习任务

**OperationId：** `getLearningTaskPage`  
**Response：** `CommonResult<PageResult<LearningTaskPageRespVO>>`

Query 参数：

| 参数 | 类型 | 必填 | 示例 | 说明 |
|---|---|---|---|---|
| `pageNo` | Integer | 否 | `1` | 页码，从 1 开始 |
| `pageSize` | Integer | 否 | `10` | 每页条数 |
| `taskName` | String | 否 | `食品制作` | 任务名称模糊查询 |
| `storeId` | Long | 否 | `10001` | 学习门店 ID |
| `taskPeriod` | LocalDateTime[] | 否 | `2026-09-01,2026-09-30` | 任务周期起止范围，共 2 个时间 |
| `taskStatus` | Integer | 否 | `1` | 周期状态：`0未开始、1进行中、2已结束` |
| `onlineStatus` | Integer | 否 | `1` | 上下架：`0下架、1上架` |

`LearningTaskPageRespVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `taskId` | Long | 学习任务 ID |
| `taskName` | String | 任务名称 |
| `taskDescription` | String | 任务描述 |
| `startTime` | LocalDateTime | 开始时间 |
| `endTime` | LocalDateTime | 结束时间 |
| `taskStatus` | Integer | `0未开始、1进行中、2已结束` |
| `applyScope` | Integer | `1按门店、2按标签` |
| `storeScope` | Integer | `1全部门店、2部分门店` |
| `storeIds` | Long[] | 指定门店 ID 列表 |
| `storeNames` | String[] | 指定门店名称列表 |
| `tagIds` | Long[] | 标签 ID 列表 |
| `tagNames` | String[] | 标签名称列表 |
| `targetStoreCount` | Integer | 最终命中的门店数 |
| `onlineStatus` | Integer | `0下架、1上架` |

### 2.2 GET `/get` 查询学习任务详情

**OperationId：** `getLearningTask`  
**Response：** `CommonResult<LearningTaskDetailRespVO>`

| 参数位置 | 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| Query | `id` | Long | 是 | 学习任务 ID |

`LearningTaskDetailRespVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `taskId` | Long | 学习任务 ID |
| `taskName` | String | 任务名称 |
| `coverImageUrl` | String | 任务封面图 |
| `detailAttachments` | AttachmentRespVO[] | 任务介绍附件，最多 5 个 |
| `startTime` / `endTime` | LocalDateTime | 任务周期 |
| `taskDescription` | String | 任务描述 |
| `applyScope` | Integer | `1按门店、2按标签` |
| `storeScope` | Integer | `1全部门店、2部分门店` |
| `storeIds` | Long[] | 指定门店 ID |
| `tagIds` | Long[] | 指定标签 ID |
| `orderLimitType` | Integer | `0不限制、1限制订货` |
| `orderLimitDays` | Integer | 开始 N 天未完成后限制订货 |
| `taskStatus` | Integer | `0未开始、1进行中、2已结束` |
| `onlineStatus` | Integer | `0下架、1上架` |
| `materialGroups` | LearningTaskMaterialGroupRespVO[] | 按项目大类分组的资料 |

`AttachmentRespVO`：`attachmentId: Long`、`attachmentUrl: String`、`attachmentType: Integer（1视频、2图片）`、`sort: Integer`。

### 2.3 POST `/create` 创建学习任务

**OperationId：** `createLearningTask`  
**Request Body：** `LearningTaskSaveReqVO`  
**Response：** `CommonResult<Long>`，`data` 为新任务 ID。

### 2.4 PUT `/update` 修改学习任务

**OperationId：** `updateLearningTask`  
**Request Body：** `LearningTaskSaveReqVO`，修改时 `taskId` 必填。  
**Response：** `CommonResult<Boolean>`，成功时 `data=true`。

`LearningTaskSaveReqVO` 字段：

| 字段 | 类型 | 创建必填 | 修改必填 | 规则 |
|---|---|---|---|---|
| `taskId` | Long | 否 | 是 | 学习任务 ID |
| `taskName` | String | 是 | 是 | 最多 50 个字符 |
| `coverImageUrl` | String | 是 | 是 | 任务封面图地址 |
| `detailAttachments` | AttachmentReqVO[] | 否 | 否 | 最多 5 个 |
| `startTime` | LocalDateTime | 是 | 是 | 格式 `yyyy-MM-dd'T'HH:mm:ss` |
| `endTime` | LocalDateTime | 是 | 是 | 必须晚于开始时间 |
| `taskDescription` | String | 否 | 否 | 最多 500 个字符 |
| `applyScope` | Integer | 是 | 是 | `1按门店、2按标签` |
| `storeScope` | Integer | 条件必填 | 条件必填 | 按门店时必填：`1全部、2部分` |
| `storeIds` | Long[] | 条件必填 | 条件必填 | `applyScope=1 && storeScope=2` 时非空 |
| `tagIds` | Long[] | 条件必填 | 条件必填 | `applyScope=2` 时非空，多个标签按并集 |
| `orderLimitType` | Integer | 是 | 是 | `0不限制、1限制` |
| `orderLimitDays` | Integer | 条件必填 | 条件必填 | 限制时为 3 至 365 |
| `materialItems` | MaterialReqVO[] | 是 | 是 | 至少 1 条，只提交资料 ID、规则和排序 |

`AttachmentReqVO`：

| 字段 | 类型 | 必填 | 规则 |
|---|---|---|---|
| `attachmentUrl` | String | 是 | 任务详情附件地址 |
| `attachmentType` | Integer | 是 | `1视频、2图片` |
| `sort` | Integer | 是 | 大于等于 0 |

`MaterialReqVO`：

| 字段 | 类型 | 必填 | 规则 |
|---|---|---|---|
| `taskMaterialId` | Long | 否 | 修改已有关系时传；新增关系不传 |
| `materialId` | Long | 是 | 外部学习资料 ID |
| `documentBrowseLimitType` | Integer | 是 | `0不限制、1限制浏览时长` |
| `minDocumentBrowseSeconds` | Integer | 条件必填 | 限制浏览时必须大于 0 |
| `videoSpeedAllowed` | Boolean | 是 | 视频是否允许倍速 |
| `requiredFlag` | Boolean | 是 | 是否必学 |
| `sort` | Integer | 是 | 大于等于 0 |

请求示例：

```json
{
  "taskId": 208900000000000001,
  "taskName": "食品制作与卫生清洁",
  "coverImageUrl": "https://example.com/task-cover.png",
  "detailAttachments": [
    {"attachmentUrl": "https://example.com/intro.mp4", "attachmentType": 1, "sort": 0}
  ],
  "startTime": "2026-09-01T00:00:00",
  "endTime": "2026-09-30T23:59:59",
  "taskDescription": "学习任务说明",
  "applyScope": 1,
  "storeScope": 2,
  "storeIds": [10001, 10002],
  "tagIds": [],
  "orderLimitType": 1,
  "orderLimitDays": 7,
  "materialItems": [
    {
      "taskMaterialId": 208900000000000101,
      "materialId": 20001,
      "documentBrowseLimitType": 1,
      "minDocumentBrowseSeconds": 10,
      "videoSpeedAllowed": false,
      "requiredFlag": true,
      "sort": 0
    }
  ]
}
```

### 2.5 PUT `/update-online-status` 修改上下架状态

**OperationId：** `updateOnlineStatus`  
**Request Body：** `LearningTaskOnlineStatusReqVO`  
**Response：** `CommonResult<Boolean>`。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `taskId` | Long | 是 | 学习任务 ID |
| `onlineStatus` | Integer | 是 | `0下架、1上架` |

### 2.6 DELETE `/delete` 删除学习任务

**OperationId：** `deleteLearningTask`  
**Response：** `CommonResult<Boolean>`。

| 参数位置 | 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| Query | `id` | Long | 是 | 学习任务 ID，执行逻辑删除 |

### 2.7 GET `/material-page` 分页查询可添加资料

**OperationId：** `getMaterialPage`  
**Response：** `CommonResult<PageResult<ExternalLearningMaterialVO>>`。

| Query 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `pageNo` | Integer | 否 | 页码 |
| `pageSize` | Integer | 否 | 每页条数 |
| `materialName` | String | 否 | 资料名称模糊查询 |
| `projectCategoryId` | Long | 否 | 项目大类 ID |
| `secondaryCategoryId` | Long | 否 | 二级分类 ID |

`ExternalLearningMaterialVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `materialId` | Long | 外部资料 ID |
| `materialName` | String | 资料名称 |
| `materialType` / `materialTypeName` | Integer / String | 资料类型编码及名称 |
| `projectCategoryId` / `projectCategoryName` | Long / String | 项目大类 |
| `secondaryCategoryId` / `secondaryCategoryName` | Long / String | 二级分类 |
| `documentCount` | Integer | 文档及图片数量 |
| `videoCount` | Integer | 视频数量 |
| `documentBrowseLimitType` | Integer | 资料默认文档浏览限制：0 不限制，1 限制时长 |
| `minDocumentBrowseSeconds` | Integer | 资料默认最少浏览秒数；不限制时为空 |
| `enabledStatus` | Integer | 外部资料启用状态 |
| `files` | ExternalLearningMaterialFileVO[] | 资料附件 JSON |

创建任务选中资料时，可用这两个默认值回填 `materialItems`；任务详情仍返回任务自身保存的规则。

`ExternalLearningMaterialFileVO`：`fileId: Long`、`fileType: Integer`、`fileName: String`、`fileUrl: String`、`fileVersion: String`。

### 2.8 GET `/statistics/overview` 查询统计概览

**OperationId：** `getStatisticsOverview`  
**Response：** `CommonResult<LearningTaskStatisticsOverviewRespVO>`。

| 参数位置 | 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| Query | `taskId` | Long | 是 | 学习任务 ID |

| 响应字段 | 类型 | 说明 |
|---|---|---|
| `totalVisitCount` | Long | 总访问次数 |
| `visitorCount` | Long | 去重访问人数 |
| `totalLearnerCount` | Long | 应学习总人数 |
| `startedLearnerCount` | Long | 已开始人数 |
| `unstartedLearnerCount` | Long | 未开始人数 |
| `completedLearnerCount` | Long | 已完成人数 |
| `completionRate` | BigDecimal | 任务完成人数占比，百分比 |

### 2.9 GET `/statistics/store-page` 分页查询门店进度

**OperationId：** `getStatisticsStorePage`  
**Response：** `CommonResult<PageResult<LearningTaskStorePageRespVO>>`。

| Query 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `taskId` | Long | 是 | 学习任务 ID |
| `orgId` | Long | 否 | 上级组织 ID |
| `storeId` | Long | 否 | 门店 ID |
| `studyStatus` | Integer | 否 | `0未开始、1进行中、2已完成` |
| `pageNo` | Integer | 否 | 页码 |
| `pageSize` | Integer | 否 | 每页条数 |

`LearningTaskStorePageRespVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `storeId` / `storeName` | Long / String | 门店 ID、名称 |
| `managerUserId` | Long | 店长用户 ID |
| `managerName` / `managerMobile` | String / String | 店长姓名、手机号 |
| `orgId` / `orgName` | Long / String | 上级组织 ID、名称 |
| `progressRate` | BigDecimal | 任务学习进度百分比 |
| `studyStatus` | Integer | `0未开始、1进行中、2已完成` |

### 2.10 GET `/statistics/store-detail` 查询门店学习详情

**OperationId：** `getStatisticsStoreDetail`  
**Response：** `CommonResult<LearningTaskStoreDetailRespVO>`。

| Query 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `taskId` | Long | 是 | 学习任务 ID |
| `storeId` | Long | 是 | 门店 ID |
| `learnerUserId` | Long | 否 | 学习人 ID；不传时取当前门店店长 |

响应字段：`taskId`、门店信息、店长信息、组织信息、`progressRate`、`studyStatus`、`materialGroups`。

### 2.11 POST `/statistics/export` 导出门店进度

**OperationId：** `exportStatistics`  
**Request Body：** `LearningTaskStorePageReqVO`，字段同 2.9。  
**Response：** `CommonResult<Boolean>`；当前接口契约返回是否受理成功。

### 2.12 PC 资料分组响应模型

`LearningTaskMaterialGroupRespVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `projectCategoryId` | Long | 项目大类 ID |
| `projectCategoryName` | String | 项目大类名称 |
| `materialCount` | Integer | 组内资料数量 |
| `materials` | MaterialRespVO[] | 组内资料列表 |

`MaterialRespVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `taskMaterialId` | Long | 任务资料关系 ID |
| `materialId` | Long | 外部资料 ID |
| `materialName` | String | 资料名称 |
| `materialType` / `materialTypeName` | Integer / String | 资料类型 |
| `secondaryCategoryId` / `secondaryCategoryName` | Long / String | 二级分类 |
| `documentCount` / `videoCount` | Integer / Integer | 文档数、视频数 |
| `documentBrowseLimitType` | Integer | `0不限制、1限制` |
| `minDocumentBrowseSeconds` | Integer | 最少浏览秒数 |
| `videoSpeedAllowed` | Boolean | 是否允许倍速 |
| `requiredFlag` | Boolean | 是否必学 |
| `sort` | Integer | 资料排序 |
| `studyStatus` | Integer | `0未开始、1进行中、2已完成` |
| `validStudySeconds` | Long | 有效学习秒数 |
| `materialAvailable` | Boolean | 外部资料是否存在且有效 |
| `files` | ExternalLearningMaterialFileVO[] | 外部资料附件 JSON |

## 3. 小程序接口（Swagger 风格）

**Tag：** `小程序 - 学习中心`  
**Base URL：** `/bpm/learning-center`  
**身份参数：** `learnerUserId` 从登录态获取；前端不提交用户 ID 和门店 ID。  
**统一响应：** `CommonResult<T>`，分页结构同 PC 接口。

### 3.1 GET `/material/category-list` 查询资料分类树

**OperationId：** `getMaterialCategoryList`  
**Request：** 无  
**Response：** `CommonResult<List<AppLearningCategoryRespVO>>`

| 响应字段 | 类型 | 说明 |
|---|---|---|
| `categoryId` | Long | 分类 ID |
| `categoryName` | String | 分类名称 |
| `sort` | Integer | 分类排序 |
| `children` | AppLearningCategoryRespVO[] | 二级分类列表 |

### 3.2 GET `/material/page` 分页查询学习资料

**OperationId：** `getMaterialPage`  
**Response：** `CommonResult<PageResult<AppLearningMaterialPageRespVO>>`

| Query 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `keyword` | String | 否 | 资料标题、内容模糊搜索 |
| `projectCategoryId` | Long | 否 | 项目大类 ID |
| `secondaryCategoryId` | Long | 否 | 二级分类 ID |
| `pageNo` | Integer | 否 | 页码 |
| `pageSize` | Integer | 否 | 每页条数 |

`AppLearningMaterialPageRespVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `materialId` | Long | 外部学习资料 ID |
| `materialName` | String | 资料名称 |
| `summary` | String | 资料摘要 |
| `coverImageUrl` | String | 资料封面图 |
| `materialType` / `materialTypeName` | Integer / String | 外部资料类型编码及名称 |
| `projectCategoryId` / `projectCategoryName` | Long / String | 项目大类 |
| `secondaryCategoryId` / `secondaryCategoryName` | Long / String | 二级分类 |
| `viewerCount` | Long | 按用户去重的看过人数 |
| `createTime` | LocalDateTime | 资料创建时间 |

### 3.3 GET `/material/get` 查询学习资料详情

**OperationId：** `getMaterial`  
**Response：** `CommonResult<AppLearningMaterialDetailRespVO>`

| 参数位置 | 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| Query | `id` | Long | 是 | 外部学习资料 ID |

响应包含 3.2 的资料字段，并增加：

| 字段 | 类型 | 说明 |
|---|---|---|
| `content` | String | 资料富文本内容 |
| `downloadAllowed` | Boolean | 固定 `false`，禁止下载保存 |
| `files` | AppLearningMaterialFileRespVO[] | 资料附件 JSON；字段见 3.10 |

### 3.4 POST `/material/visit` 记录资料访问

**OperationId：** `recordMaterialVisit`  
**Request Body：** `AppLearningMaterialVisitReqVO`  
**Response：** `CommonResult<Boolean>`。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `materialId` | Long | 是 | 外部学习资料 ID；看过人数按用户去重 |

### 3.5 GET `/task/page` 分页查询我的学习任务

**OperationId：** `getTaskPage`  
**Response：** `CommonResult<PageResult<AppLearningTaskPageRespVO>>`

| Query 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `keyword` | String | 否 | 学习任务名称模糊搜索 |
| `studyStatus` | Integer | 否 | `0未开始、1进行中、2已完成`；不传查全部 |
| `pageNo` | Integer | 否 | 页码 |
| `pageSize` | Integer | 否 | 每页条数 |

`AppLearningTaskPageRespVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `taskId` | Long | 学习任务 ID |
| `taskName` | String | 任务名称 |
| `coverImageUrl` | String | 任务封面 |
| `taskDescription` | String | 任务描述 |
| `startTime` / `endTime` | LocalDateTime | 任务周期 |
| `taskStatus` | Integer | 周期状态：`0未开始、1进行中、2已结束` |
| `studyStatus` | Integer | 个人状态：`0未开始、1进行中、2已完成` |
| `progressRate` | BigDecimal | 个人任务进度百分比 |
| `actionType` | Integer | `1开始学习、2查看任务` |
| `learningAllowed` | Boolean | 是否允许学习；过期任务仍为 `true` |

### 3.6 GET `/task/get` 查询学习任务详情

**OperationId：** `getTask`  
**Response：** `CommonResult<AppLearningTaskDetailRespVO>`

| Query 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `taskId` | Long | 是 | 学习任务 ID |
| `studyStatus` | Integer | 否 | 筛选资料：`0未开始、1进行中、2已完成` |

`AppLearningTaskDetailRespVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `taskId` / `taskName` | Long / String | 任务 ID、名称 |
| `coverImageUrl` | String | 任务封面 |
| `detailAttachments` | AttachmentRespVO[] | 任务介绍图片或视频 |
| `startTime` / `endTime` | LocalDateTime | 任务周期 |
| `taskDescription` | String | 任务描述 |
| `orderLimitType` / `orderLimitDays` | Integer / Integer | 订货限制及天数 |
| `taskStatus` | Integer | `0未开始、1进行中、2已结束` |
| `studyStatus` | Integer | `0未开始、1进行中、2已完成` |
| `progressRate` | BigDecimal | 当前用户任务进度 |
| `learningAllowed` | Boolean | 是否允许继续学习 |
| `allCount` | Integer | 当前任务全部资料数 |
| `unstartedCount` | Integer | 未开始资料数 |
| `studyingCount` | Integer | 进行中资料数 |
| `completedCount` | Integer | 已完成资料数 |
| `materialGroups` | AppLearningTaskMaterialGroupRespVO[] | 按项目大类分组的资料 |

资料分组对象：

| 字段 | 类型 | 说明 |
|---|---|---|
| `projectCategoryId` / `projectCategoryName` | Long / String | 项目大类 |
| `materialCount` | Integer | 组内资料数 |
| `materials` | MaterialRespVO[] | 组内资料 |

组内 `MaterialRespVO`：`taskMaterialId`、`materialId`、`materialName`、`summary`、`coverImageUrl`、资料类型、二级分类、`requiredFlag`、`sort`、`studyStatus`、`progressRate`、`materialAvailable`。

### 3.7 GET `/task/material/get` 查询任务中的资料详情

**OperationId：** `getTaskMaterial`  
**Response：** `CommonResult<AppLearningTaskMaterialDetailRespVO>`

| 参数位置 | 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| Query | `taskMaterialId` | Long | 是 | 任务资料关系 ID，不是外部资料 ID |

`AppLearningTaskMaterialDetailRespVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `taskId` | Long | 学习任务 ID |
| `taskMaterialId` | Long | 任务资料关系 ID |
| `materialId` | Long | 外部资料 ID |
| `materialName` / `summary` | String / String | 资料名称、摘要 |
| `content` | String | 富文本内容 |
| `coverImageUrl` | String | 资料封面 |
| `materialType` / `materialTypeName` | Integer / String | 资料类型 |
| `projectCategoryId` / `projectCategoryName` | Long / String | 项目大类 |
| `secondaryCategoryId` / `secondaryCategoryName` | Long / String | 二级分类 |
| `requiredFlag` | Boolean | 是否必学 |
| `documentBrowseLimitType` | Integer | `0不限制、1限制时长` |
| `minDocumentBrowseSeconds` | Integer | 最少有效浏览秒数 |
| `videoSpeedAllowed` | Boolean | 是否允许视频倍速 |
| `studyStatus` | Integer | `0未开始、1进行中、2已完成` |
| `progressRate` | BigDecimal | 已完成附件数占当前有效附件数的百分比 |
| `downloadAllowed` | Boolean | 固定 `false` |
| `learningAllowed` | Boolean | 过期任务仍为 `true` |
| `materialAvailable` | Boolean | 外部资料是否有效；失效时仍保留资料 ID |
| `files` | AppLearningMaterialFileRespVO[] | 附件及每个附件的学习状态 |

服务端生成响应 JSON 的步骤：

1. 根据 `taskMaterialId` 查询任务资料关系和学习规则。
2. 使用关系中的 `materialId` 获取资料 JSON，其中 `files` 包含该资料的全部附件 ID。
3. 按当前登录店长、命中门店、`fileId + fileVersion` 查询每个附件的学习记录。
4. 合并资料 JSON、任务规则和附件进度，计算资料进度后返回。

完整 `data` 示例：

```json
{
  "taskId": 208900000000000001,
  "taskMaterialId": 208900000000000101,
  "materialId": 20001,
  "materialName": "鸡腿汉堡制作标准",
  "summary": "制作步骤和质量标准",
  "content": "<p>资料正文</p>",
  "coverImageUrl": "https://example.com/cover.jpg",
  "materialType": 1,
  "materialTypeName": "图文视频",
  "projectCategoryId": 100,
  "projectCategoryName": "食材制作",
  "secondaryCategoryId": 101,
  "secondaryCategoryName": "主食制作",
  "requiredFlag": true,
  "documentBrowseLimitType": 1,
  "minDocumentBrowseSeconds": 10,
  "videoSpeedAllowed": false,
  "studyStatus": 1,
  "progressRate": 50.00,
  "downloadAllowed": false,
  "learningAllowed": true,
  "materialAvailable": true,
  "files": [
    {
      "fileId": 30001,
      "fileType": 1,
      "fileTypeName": "视频",
      "fileName": "制作视频.mp4",
      "fileUrl": "https://example.com/a.mp4",
      "fileVersion": "v1",
      "sort": 0,
      "studyStatus": 2,
      "validStudySeconds": 180,
      "lastPositionSeconds": 180,
      "reachedBottom": false
    },
    {
      "fileId": 30002,
      "fileType": 2,
      "fileTypeName": "图片",
      "fileName": "成品标准.jpg",
      "fileUrl": "https://example.com/b.jpg",
      "fileVersion": "v1",
      "sort": 1,
      "studyStatus": 0,
      "validStudySeconds": 0,
      "lastPositionSeconds": 0,
      "reachedBottom": false
    }
  ]
}
```

### 3.8 POST `/task/visit` 记录任务访问

**OperationId：** `recordTaskVisit`  
**Request Body：** `AppLearningTaskVisitReqVO`  
**Response：** `CommonResult<Boolean>`。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `taskId` | Long | 是 | 学习任务 ID |

### 3.9 POST `/task/file-progress/report` 上报附件学习进度

**OperationId：** `reportFileProgress`  
**Request Body：** `AppLearningFileProgressReportReqVO`  
**Response：** `CommonResult<AppLearningProgressRespVO>`。

请求字段：

| 字段 | 类型 | 必填 | 规则与说明 |
|---|---|---|---|
| `taskMaterialId` | Long | 是 | 任务资料关系 ID |
| `fileUrl` | String | 有附件时必填 | 从任务资料详情 `text` 取得：视频/图片取 `url`，文档取 `file.url`；纯富文本不传 |
| `progressRevision` | Integer | 是 | 任务资料详情返回的修订号，原样带回 |
| `eventType` | Integer | 是 | `1打开、2心跳、3结束或完成申报` |
| `currentPositionSeconds` | Long | 否 | 视频当前播放位置，大于等于 0 |
| `reachedBottom` | Boolean | 否 | 文档、图片或富文本是否浏览到底部 |
| `fileKey` / `fileVersion` | String | 否 | 兼容旧客户端，新客户端无需传 |

```json
{
  "taskMaterialId": 208900000000000101,
  "fileUrl": "https://example.com/lesson.mp4",
  "progressRevision": 2,
  "eventType": 2,
  "currentPositionSeconds": 35,
  "reachedBottom": false
}
```

`AppLearningProgressRespVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `taskId` | Long | 学习任务 ID |
| `taskMaterialId` | Long | 任务资料关系 ID |
| `fileKey` | String | 本次上报附件的服务端稳定键 |
| `fileStudyStatus` | Integer | 附件状态：`0未学习、1学习中、2已学习` |
| `materialStudyStatus` | Integer | 资料状态：`0未开始、1进行中、2已完成` |
| `taskStudyStatus` | Integer | 任务状态：`0未开始、1进行中、2已完成` |
| `fileValidStudySeconds` | Long | 当前附件有效学习秒数 |
| `materialValidStudySeconds` | Long | 当前资料累计有效学习秒数 |
| `materialProgressRate` | BigDecimal | 当前资料进度百分比 |
| `taskProgressRate` | BigDecimal | 当前任务进度百分比 |

响应示例：

```json
{
  "code": 0,
  "data": {
    "taskId": 208900000000000001,
    "taskMaterialId": 208900000000000101,
    "fileKey": "e35d...完整SHA-256",
    "fileStudyStatus": 1,
    "materialStudyStatus": 1,
    "taskStudyStatus": 1,
    "fileValidStudySeconds": 35,
    "materialValidStudySeconds": 215,
    "materialProgressRate": 50.00,
    "taskProgressRate": 25.00
  },
  "msg": ""
}
```

### 3.10 小程序附件响应模型

`AppLearningMaterialFileRespVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `fileId` | Long | 外部附件 ID |
| `fileType` | Integer | 类型编码，沿用外部资料系统 |
| `fileTypeName` | String | 视频、图片、Word、PPT、PDF 等 |
| `fileName` | String | 附件名称 |
| `fileUrl` | String | 在线预览地址 |
| `fileVersion` | String | 内容版本；内容变化必须更新 |
| `sort` | Integer | 附件排序 |
| `coverImageUrl` | String | 附件封面或预览图 |
| `durationSeconds` | Long | 视频总时长，秒 |
| `fileSize` | Long | 文件大小，字节 |
| `fileSuffix` | String | 文件后缀 |
| `studyStatus` | Integer | `0未学习、1学习中、2已学习` |
| `validStudySeconds` | Long | 当前附件有效学习秒数 |
| `lastPositionSeconds` | Long | 视频最后有效播放位置 |
| `reachedBottom` | Boolean | 图片、文档是否浏览到底部 |

## 4. 附件与资料进度计算

### 4.1 单附件记录

- 每个资料可以有多个附件，每个附件 ID 分别保存一条学习进度。
- 唯一维度：`taskMaterialId + storeId + learnerUserId + fileId + fileVersion`。
- 视频根据心跳、播放位置和视频总时长判断完成。
- 图片、Word、PPT、PDF 根据有效浏览时长及 `reachedBottom` 判断完成。
- 附件完成后：`completed_flag=1`，接口返回 `studyStatus=2`。
- 文件内容变化必须更新 `fileVersion`；新版本重新学习，旧版本记录保留但不参与当前进度。

### 4.2 单资料进度

设当前资料 JSON 中有效附件总数为 `N`，其中当前版本已完成附件数为 `M`：

```text
资料进度 progressRate = M / N * 100
```

计算示例：资料包含 5 个附件，完成 3 个，资料进度为 `60.00%`。

- `N > 0 && M = 0`：资料状态为未开始或进行中，取决于是否已有打开/心跳记录。
- `N > 0 && 0 < M < N`：资料状态为进行中。
- `N > 0 && M = N`：资料状态为已完成。
- `N = 0`：按无附件富文本规则判断；未满足规则为 `0%`，满足规则为 `100%`。
- JSON 中新增附件：`N` 增加，新附件从 0 开始，资料进度重新计算。
- JSON 中移除附件：该附件历史进度保留，但不再计入 `N` 和 `M`。
- 只修改资料名称或分类、不改变 `fileId + fileVersion`：附件及资料进度保持不变。

### 4.3 任务进度

任务进度只统计当前有效的必学资料：

```text
任务进度 = 已完成必学资料数 / 当前必学资料总数 * 100
```

非必学资料保留个人学习状态，但不影响任务是否完成。同一店长管理多家命中门店时，附件、资料和任务三级进度同步写入全部命中门店。
