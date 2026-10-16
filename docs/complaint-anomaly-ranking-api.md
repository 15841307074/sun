# 经营预警投诉排行接口说明

## 1. 功能说明

经营预警投诉排行包含以下两个接口：

1. 投诉订单占比排行：按照门店投诉订单数占有效订单数的比例进行排名。
2. 食品卫生投诉排行：按照门店食品卫生类投诉订单数进行排名。

接口只统计当前登录人所在项目内有权限管理的门店，门店范围包含：

- 当前登录人所属组织及其下级组织的门店。
- 当前登录人额外绑定的门店。

没有门店权限或所选周期内没有相应异常数据时，接口正常返回空数组。

## 2. 公共请求规则

### 2.1 日期统计口径

- `startDate` 和 `endDate` 均可选择任意日期，不要求必须为周一或周日。
- 开始日期不能晚于结束日期。
- 日期范围包含开始和结束当天，最多允许选择 90 天。
- 实际统计范围为所选日期命中的全部 ISO 自然周。
- 开始日期所在周和结束日期所在周均按照完整的周一至周日统计。

例如：

```text
startDate=2026-10-07（周三）
endDate=2026-10-09（周五）
```

实际读取并统计 `2026-10-05` 至 `2026-10-11` 这一完整自然周的数据。

### 2.2 排名类型

`rankType` 支持以下值，不区分大小写：

| 参数值 | 含义 |
| --- | --- |
| `TOP` | 指标由高到低，返回前 10 家门店 |
| `BOTTOM` | 指标由低到高，返回后 10 家门店 |

每个接口最多返回 10 条记录。指标相同时，按照投诉订单数和门店 ID 保持稳定排序。

### 2.3 项目和用户权限

- 前端不传 `businessId` 和 `userId`。
- 项目 ID 从当前请求的 `BusinessContextHolder` 获取。
- 用户 ID 从当前登录信息获取。
- 服务端通过 Dubbo `StoreApi.listUserStoreRelations` 查询当前用户可管理的门店。
- 门店 ID 在响应中使用字符串，避免小程序处理 Long 类型时发生精度丢失。

## 3. 投诉订单占比排行

### 3.1 接口信息

```http
GET /analysis/app/anomaly-report/complaint-ratio-ranking
```

控制器方法：

```java
AnomalyAppController#getComplaintRatioRanking
```

### 3.2 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `startDate` | String | 是 | 开始日期，格式 `yyyy-MM-dd` |
| `endDate` | String | 是 | 结束日期，格式 `yyyy-MM-dd` |
| `rankType` | String | 是 | 排名类型：`TOP` 或 `BOTTOM` |

请求示例：

```http
GET /analysis/app/anomaly-report/complaint-ratio-ranking?startDate=2026-10-07&endDate=2026-10-16&rankType=TOP
```

### 3.3 统计规则

接口将范围内所有命中周的数据按门店合并：

```text
投诉订单占比 = 各周投诉订单数之和 ÷ 各周有效订单数之和 × 100%
```

- 投诉订单按照投诉订单号去重。
- 有效订单按照订单号去重，并使用系统现有有效订单状态口径。
- 占比保留两位小数。
- 只返回投诉订单数大于 0 且有效订单数大于 0 的门店。
- 某周存在投诉但有效订单数为 0 时，该周投诉数仍被保留；跨周汇总后有效订单总数大于 0 时可以正常计算占比。

### 3.4 响应字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `storeId` | String | 门店 ID |
| `storeName` | String | 门店名称 |
| `complaintOrderRatio` | Number | 投诉订单占比，单位 `%` |
| `complaintOrderCount` | Long | 去重后的投诉订单数 |

响应示例：

```json
{
  "code": 0,
  "data": [
    {
      "storeId": "2109515101383192577",
      "storeName": "沈阳航空航天大学店",
      "complaintOrderRatio": 70.34,
      "complaintOrderCount": 200
    }
  ],
  "msg": ""
}
```

## 4. 食品卫生投诉排行

### 4.1 接口信息

```http
GET /analysis/app/anomaly-report/food-hygiene-complaint-ranking
```

控制器方法：

```java
AnomalyAppController#getFoodHygieneComplaintRanking
```

### 4.2 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `startDate` | String | 是 | 开始日期，格式 `yyyy-MM-dd` |
| `endDate` | String | 是 | 结束日期，格式 `yyyy-MM-dd` |
| `rankType` | String | 是 | 排名类型：`TOP` 或 `BOTTOM` |

请求示例：

```http
GET /analysis/app/anomaly-report/food-hygiene-complaint-ranking?startDate=2026-10-01&endDate=2026-10-16&rankType=BOTTOM
```

### 4.3 统计规则

- 食品卫生投诉沿用 `business_type=3` 的业务口径。
- 投诉订单按照投诉订单号去重。
- 跨周查询时，按照门店汇总各周食品卫生投诉订单数和全部投诉订单数。
- 只返回食品卫生投诉订单数大于 0 的门店。

### 4.4 响应字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `storeId` | String | 门店 ID |
| `storeName` | String | 门店名称 |
| `foodHygieneComplaintOrderCount` | Long | 食品卫生类去重投诉订单数 |
| `complaintOrderCount` | Long | 全部去重投诉订单数 |

响应示例：

```json
{
  "code": 0,
  "data": [
    {
      "storeId": "2109515101383192577",
      "storeName": "沈阳航空航天大学店",
      "foodHygieneComplaintOrderCount": 20,
      "complaintOrderCount": 200
    }
  ],
  "msg": ""
}
```

## 5. 业务异常

| 错误码 | 提示信息 | 触发条件 |
| --- | --- | --- |
| `1008000024` | 开始日期和结束日期不能为空 | 任一日期未传 |
| `1008000025` | 开始日期不能晚于结束日期，且日期范围不能超过 90 天 | 日期顺序错误或超过 90 天 |
| `1008000026` | 排行类型仅支持 TOP 或 BOTTOM | `rankType` 不合法 |
| `1008000011` | 查询超时，请缩短查询范围后重试 | 经营异常数据查询失败 |

日期格式不是 `yyyy-MM-dd` 时，由接口参数解析校验统一返回参数格式错误。

## 6. 周度投诉指标任务

接口读取 MySQL 的 `store_anomaly_metrics_weekly` 周度指标表。该表由以下 XXL-Job 定期计算：

```text
Handler: analysisComplaintAnomalyWeekJobHandler
建议执行时间: 每周一 06:30
```

计算上一个完整自然周：

```json
{"businessId":10}
```

历史补跑：

```json
{"businessId":10,"periodStart":"2026-10-05"}
```

任务参数说明：

| 参数名 | 必填 | 说明 |
| --- | --- | --- |
| `businessId` | 是 | 项目 ID，可配置任意有效项目 |
| `periodStart` | 否 | 统计周的周一，格式 `yyyy-MM-dd`；未传时计算上一个完整自然周 |

任务数据来源：

- 投诉明细：`analytics.system_complaint_ch`。
- 有效订单：`analytics.bz_order_ch`。
- 周度指标结果：MySQL `store_anomaly_metrics_weekly`。

任务按项目和自然周覆盖投诉相关指标，不覆盖营业额、客单价、销量等其他异常指标。任务失败时不会写入不完整结果，并且会清理当前任务线程中的项目上下文。

## 7. 部署前置条件

1. 确认 MySQL `store_anomaly_metrics_weekly` 已包含四个投诉指标字段。
2. 确认 ClickHouse 已存在并持续同步 `analytics.system_complaint_ch` 和 `analytics.bz_order_ch`。
3. 先部署包含 `StoreApi.listUserStoreRelations` 的 system 服务，再部署 analysis 服务。
4. 在 XXL-Job 中按照项目分别配置周度投诉指标任务。
5. 周度投诉指标任务建议安排在 06:30，现有周度报告任务安排在 07:00。
