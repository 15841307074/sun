# 抽奖手机号共享、券包与实物退回说明

## 上线顺序

1. 确认没有正在进行的抽奖活动。先执行 `sql/lottery_mobile_package_return_20261021.sql`：四张新表放在现有 `lottery_v2_*` 可访问的同一业务数据源；中奖记录索引段在中奖记录分片所在库执行。同库部署可以整份执行。
2. 部署 promotion 服务。不新增 Nacos 配置，不改会员余额、其他活动和红包回调。新建或修改上架活动，任务与次数使用手机号账本，不迁移旧次数、不清空旧库存或中奖记录。
3. 历史身份映射可手动运行 `lotteryMemberIdentityBackfillJobHandler`，参数 `{"businessId":10,"afterId":0,"maxRows":10000}`。只补映射，不修改中奖记录。日志的 `more=true` 时，将 `lastId` 作为下一次 `afterId`，直到 `more=false`。无手机号的历史记录不猜测归属，只允许原账号查看。
4. 保留原任务 `lotteryReturnRealJobHandler`，建议每5分钟执行：`0 0/5 * * * ?`，无需任务参数。已有配置直接更新，避免重复配置。

## 手机号共享

现有 `/app-api/promotion/lottery-mobile` 下任务、抽奖、次数、记录、地址接口路径和参数不变。本人操作必须使用当前登录账号的 `memberId`。手机号由服务端查询绑定资料，前端不传；未绑定时提示先绑定。

次数与任务按项目、活动、手机号、周期及来源隔离，会员 ID 不参与计数唯一键。微信、支付宝共享次数、任务进度、周期和总上限；积分余额和奖品归属仍属于实际参与账号。

`getLotteryLogByMemberId` 返回同项目、同手机号记录。`updateReceivingAddress` 可以由同手机号另一个渠道账号填写，仍更新原中奖会员分片。已退回、已发货、已有快递单号不能改地址。历史记录手机号为空时，仅原中奖账号可查。

## 券包中奖与人工补发

`prizeType=6` 为优惠券包，`awardId` 为券包 ID。单个券包最多1000张券，受理时保存本次明细快照。

中奖接口先正常返回，后台异步发放。`grantStatus` 为 `PENDING`、`SUCCESS` 或 `FAILED`。失败不切兜底、不退次数或积分，库存继续占用，停止自动重试。每张券使用固定 ID，部分成功后补发只补缺少的券。

相对有效期从首次实际发券尝试时间计算并持久化。人工补齐沿用该基准，不重置已有券；绝对日期券仍按其原日期。

复用 `POST /admin-api/promotion/lottery-log/reissue`，权限 `promotion:lottery:reissue`：

```json
{
  "lotteryId": "原活动主表ID或配置ID",
  "memberId": "原中奖会员ID",
  "storeId": "原中奖门店ID",
  "requestId": "原抽奖请求ID",
  "reason": "券包发放失败，人工补齐"
}
```

返回表示是否安排异步任务，不代表已到账。后台中奖记录增加 `requestId`、`grantStatus`、`failReason`、`canReissue`。必须使用原账号、门店和请求编号；成功、正在执行、已安排补发、已退奖记录不会重复发放。

## 后台中奖记录查询与导出

- `POST /admin-api/promotion/lottery-log/getLotteryLogList`：请求体中传 `prizeType=6` 筛选优惠券包；不传时包含所有奖品类型。券包记录返回 `prizeType=6`、中奖时保存的 `prizeName`，以及原有的 `requestId`、`grantStatus`、`failReason`、`canReissue`。
- `GET /admin-api/promotion/lottery-log/export`：查询参数传 `prizeType=6` 导出券包；不传时按原条件导出全部类型。“奖品类型”列为“优惠券包”，“奖品内容”列为中奖记录的奖品名称。
- `claimStatus` 仍仅用于红包领取状态，查询或导出券包时不要传该参数；券包发放状态查看 `grantStatus`。其他筛选条件、分页及异步下载方式保持原样。

## 支付宝红包与实物回收

服务端 `memberCategory=1` 为支付宝。支付宝命中红包时，在占库存和受理前替换为非红包兜底，正常消耗一次机会，不创建微信转账、不占红包库存。保存、上架和受理均禁止红包兜底。

支付宝查看微信原红包记录不会重新发放或转移收款账号。

实物中奖满北京时间48小时，仍未填地址且未发货才回收。只退原奖池、原批次库存，不退积分、不恢复次数。退奖和填地址使用条件更新，重复执行不会多退库存。旧版缓存恢复有持久化回执，失败后可恢复，不把库存退到新版批次。

## 验证范围

自动测试覆盖数据库事务、同手机号最后一次机会竞争、项目隔离、订单防重、共享地址权限及分片、券包部分成功补齐、失败停止、实物退回不退款和支付宝兜底。隔离测试不等同于真实数据库、Redis集群、Dubbo和微信的部署验收。
