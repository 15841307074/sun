# 抽签 C 端接口文档

## 1. 说明

- 接口前缀：`/promotion/activity-cq`
- 返回包装：`CommonResult<T>`
- 分页返回：`PageResult<T>`
- 当前登录用户 ID 统一由后端从 token 获取，C 端不要再传 `memberId`；即使传入也不会作为当前用户使用。
- 分页入参公共字段：
  - `pageNo`：页码，从 `1` 开始
  - `pageSize`：每页条数，最大 `100`

当前抽签玩法为“任务直接发签码”，不再保留手动抽签发码流程。以下接口已废弃，不再给 C 端使用：

- `draw/verify`
- `draw/count`
- `draw`
- `getActivityCqList`

---

## 2. 活动详情

### 2.1 获取抽签活动详情

- 方法：`GET`
- 路径：`/getActivityCqDetail`

#### 请求参数

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| activityId | Long | 是 | 活动 ID |

#### 返回字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| activityId | Long | 活动 ID |
| activityTitle | String | 活动标题 |
| activityCoverImage | String | 活动封面图 |
| activityBackgroundImage | String | 活动背景图 |
| activityDetailsImage | String | 活动详情图 |
| buttonBackgroundColor | String | 按钮背景色 |
| activityStartTime | Date | 活动开始时间 |
| activityEndTime | Date | 活动结束时间 |
| activityRule | String | 活动规则 |
| shareTitle | String | 分享标题 |
| shareNote | String | 分享内容 |
| shareImgUrl | String | 分享图片 |
| dailyAttendance | Integer | 签到开关，`1` 开启 |
| freeEvent | Integer | 免费集签开关，`1` 开启 |
| freeCount | Integer | 免费抽签次数 |
| placeOrderStatus | Integer | 下单开关，`1` 开启 |
| shareEvent | Integer | 分享开关，`1` 开启 |
| browseHomeEvent | Integer | 浏览首页集签开关，`1` 开启 |
| browseHomeCount | Integer | 浏览首页每人每天上限获得次数 |
| activityStatus | Integer | 活动状态，`0未开始 1已开始 2已开奖` |
| paymentThreshold | BigDecimal | 下单金额门槛 |
| placeOrderCodeNumber | Integer | 下单得码次数 |
| shareCount | Integer | 分享得码次数 |
| resultPublishTime | LocalDateTime | 开奖公布时间 |
| drawStatus | Integer | 开奖状态，`0未开奖 1开奖中 2已开奖` |
| joined | Boolean | 是否已参与 |
| prizeList | List\<PrizeVO\> | 奖品列表 |

#### PrizeVO

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 奖品 ID |
| prizeType | Integer | 奖品类型 |
| prizeName | String | 奖品名称 |
| prizeImgUrl | String | 奖品图片 |
| prizeValue | BigDecimal | 奖品价值 |
| totalStock | Integer | 总库存 |
| usedStock | Integer | 已发放库存 |
| remainStock | Integer | 剩余库存 |

---

## 3. 任务相关

### 3.1 获取任务列表

- 方法：`POST`
- 路径：`/task/getTaskList`

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| activityId | Long | 是 | 活动 ID |
| storeId | Long | 否 | 门店 ID |

#### 返回字段

返回 `List<TaskVO>`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| taskType | Integer | 任务类型，`1签到 2下单 3分享 4免费集签 5浏览首页` |
| amount | BigDecimal | 下单金额门槛 |
| taskLimit | Integer | 完成上限 |
| finishCount | Integer | 已完成数量 |

### 3.2 完成签到任务

- 方法：`POST`
- 路径：`/task/sign`

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| activityId | Long | 是 | 活动 ID |
| storeId | Long | 否 | 门店 ID |

#### 说明

- 签到成功后直接生成签码
- 会校验签到任务开关和签到次数上限
- 已到开奖时间或活动已开奖时不可再签到得码

### 3.2.1 完成普通分享任务

- 方法：`POST`
- 路径：`/task/share/simple`

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| activityId | Long | 是 | 活动 ID |
| storeId | Long | 否 | 门店 ID |

#### 说明

- 当前抽签活动推荐使用该接口完成分享任务
- 用户只要完成一次普通分享，即可累计一次分享任务并获得签码
- 不依赖 `activity_help` 助力关系表
- 会校验用户是否已参与活动
- 会校验分享任务开关和分享次数上限

### 3.2.2 完成浏览首页任务

- 方法：`POST`
- 路径：`/task/browse/home`

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| activityId | Long | 是 | 活动 ID |
| storeId | Long | 否 | 门店 ID |

#### 返回字段

返回 `List<String>`，表示本次新增的抽签码列表。

#### 说明

- 浏览首页成功后直接生成签码
- 会校验用户是否已参与活动
- 会校验浏览首页任务开关和当日次数上限
- 已到开奖时间或活动已开奖时不可再获得签码

### 3.1.1 参与抽签活动

- 方法：`POST`
- 路径：`/join`

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| activityId | Long | 是 | 活动 ID |
| storeId | Long | 否 | 门店 ID |

#### 说明

- 用户首次点击“立即参与抽签”时调用
- 会复用活动缓存做活动校验
- 已参与过时直接返回成功，接口幂等
- 参与成功后，后续进入详情页可通过 `joined=true` 直接展示任务列表

### 3.3 分享助力预校验

- 方法：`POST`
- 路径：`/task/shareCheck`

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| activityId | Long | 是 | 活动 ID |
| inviterMemberId | Long | 是 | 被助力人用户 ID |
| storeId | Long | 否 | 门店 ID |

#### 说明

- 校验是否允许助力
- 校验是否重复助力
- 校验单日助力上限
- 该接口为历史助力模型保留，不作为当前抽签玩法主入口

### 3.4 完成分享助力任务

- 方法：`POST`
- 路径：`/task/share`

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| activityId | Long | 是 | 活动 ID |
| inviterMemberId | Long | 是 | 被助力人用户 ID |
| storeId | Long | 否 | 门店 ID |

#### 说明

- 分享助力成功后，给 `inviterMemberId` 发签码
- 新会员助力时当前实现会额外奖励签码
- 已到开奖时间或活动已开奖时不可再分享得码
- 该接口为历史助力模型保留，不作为当前抽签玩法主入口

---

## 4. 奖品相关

### 4.1 获取奖品列表

- 方法：`POST`
- 路径：`/prize/list`

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| activityId | Long | 是 | 活动 ID |

#### 返回字段

返回 `ActivityCqPrizeListVO`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| prizeList | List\<PrizeVO\> | 奖品列表 |

### 4.2 填写收货地址

- 方法：`POST`
- 路径：`/prize/address/save`

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | Long | 是 | 中奖记录 ID |
| receiveUser | String | 否 | 收件人 |
| receiveMobile | String | 否 | 收件联系方式 |
| receiveAddress | String | 否 | 收件地址 |

#### 说明

- 仅更新当前用户自己的中奖记录地址信息

---

## 5. 签码与结果查询

### 5.1 获取抽签记录

- 方法：`POST`
- 路径：`/record/draw`

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| activityId | Long | 是 | 活动 ID |
| pageNo | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页数量 |

#### 返回字段

返回 `PageResult<DrawRecordVO>`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 记录 ID |
| signCode | String | 签码（固定 10 位） |
| obtainType | Integer | 获取方式，`1签到 2下单 3分享` |
| resultStatus | Integer | 结果状态，`0待开奖 1未中签 2已中签` |
| prizeId | Long | 奖品 ID |
| prizeType | Integer | 奖品类型 |
| prizeContent | String | 奖品内容 |
| prizeImgUrl | String | 奖品图片 |
| prizeState | Integer | 奖品状态 |
| drawTime | LocalDateTime | 签码生成时间 |

### 5.2 获取我的抽签码汇总

- 方法：`POST`
- 路径：`/code/my`

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| activityId | Long | 是 | 活动 ID |

#### 返回字段

返回 `MyCodeVO`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| totalCount | Integer | 总签码数 |
| pendingCount | Integer | 待开奖数 |
| winningCount | Integer | 中奖数 |
| loseCount | Integer | 未中奖数 |
| signCount | Integer | 签到获得数 |
| orderCount | Integer | 下单获得数 |
| shareCount | Integer | 分享获得数 |

### 5.3 获取我的抽签结果

- 方法：`POST`
- 路径：`/record/myResult`

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| activityId | Long | 是 | 活动 ID |
| pageNo | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页数量 |

#### 返回字段

返回 `PageResult<MyResultVO>`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 记录 ID |
| signCode | String | 中奖签码（固定 10 位） |
| resultStatus | Integer | 结果状态，固定为 `2` |
| prizeId | Long | 奖品 ID |
| prizeType | Integer | 奖品类型 |
| prizeContent | String | 奖品内容 |
| prizeImgUrl | String | 奖品图片 |
| prizeState | Integer | 奖品状态 |
| trackingNumber | String | 快递单号 |
| drawTime | LocalDateTime | 开奖时间 |

### 5.4 获取中奖公示列表

- 方法：`POST`
- 路径：`/record/winning`

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| activityId | Long | 是 | 活动 ID |
| pageNo | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页数量 |

#### 返回字段

返回 `PageResult<WinningRecordVO>`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| memberMobile | String | 用户手机号 |
| prizeContent | String | 奖品内容 |
| signCode | String | 签码（固定 10 位） |

---

## 6. 状态口径

### 6.1 活动状态 `activityStatus`

- `0`：未开始
- `1`：已开始
- `2`：已开奖

判定口径：

- `drawStatus = 2` 时，`activityStatus = 2`
- 未到活动开始时间时，`activityStatus = 0`
- 其余为 `activityStatus = 1`

### 6.2 开奖状态 `drawStatus`

- `0`：未开奖
- `1`：开奖中
- `2`：已开奖

### 6.3 结果状态 `resultStatus`

- `0`：待开奖
- `1`：未中签
- `2`：已中签

### 6.4 奖品类型 `prizeType`

当前抽签活动奖品表使用口径：

- `1`：优惠券
- `2`：积分
- `3`：实物
- `4`：无奖品
- `5`：现金红包
- `6`：优惠券包

### 6.5 奖品状态 `prizeState`

- `0`：已发放
- `1`：未填写收货地址
- `2`：已填写地址待发货
- `3`：已发货
- `9`：已退回

---

## 7. 业务补充

- 用户签码来源只有三类：签到、分享助力、下单
- 不存在 C 端用户主动“抽一次生成签码”的动作
- `activity_cq_log` 一条记录代表一张签码，同时承载最终开奖结果
- 开奖为统一批处理，不由 C 端触发
