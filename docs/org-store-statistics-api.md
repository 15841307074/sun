# 实时组织门店统计接口

```http
GET /app-api/analysis/app/anomaly-report/org-store-statistics
```

需要登录，无业务参数。用户、项目和默认组织均由服务端当前登录上下文确定，不传 `month`、`orgId` 或 `businessId`。旧调用携带额外的 `month` 参数时仍可调用，但该参数不参与统计。

每次请求通过 system Dubbo API 查询当前组织权限、完整组织链和正常营业门店，不读取历史快照。门店须满足 `deleted=0、storeStatus=0`；无所属有效组织的门店不进入组织统计。

响应示例：

```json
{
  "code": 0,
  "data": {
    "orgId": "1001",
    "orgName": "东北区域",
    "totalStoreCount": 3,
    "directStoreCount": 1,
    "items": [
      {
        "orgId": "2001",
        "orgName": "某小组",
        "level": 2,
        "storeCount": 2,
        "directStoreCount": 2,
        "children": []
      }
    ]
  },
  "msg": ""
}
```

`totalStoreCount` 为本级及全部下级的去重门店总数，`directStoreCount` 为直属门店数；`items` 按真实组织层级返回，下级节点继续放在 `children`。组织按排序值、层级和 ID 排序，组织 ID 始终为字符串。无正常营业门店时返回数量 0 和空 `items`；此时没有门店组织链提供名称，`orgName` 可以为空。接口不再返回 `month`。

模块内可调用 `StoreOrgStatisticsService.getOrgStores(userId, orgId)`，校验当前项目中的组织权限后返回实时门店资料列表，按门店 ID 去重排序。旧的 `getMonthOrgStores` 随月度组织快照功能取消。

## 下线月度组织快照

1. 在 XXL-Job 停用或删除 `analysisStoreOrgSnapshotMonthJobHandler`。
2. 部署新版 analysis 服务。组织快照任务、DO、Mapper、读写服务和建表脚本已移除。
3. 已建旧表且不再需要其中数据时，执行 `sql/经营异常提醒_261014/retire_analysis_store_org_snapshot.sql` 清理 `analysis_store_org_snapshot`。该脚本未由开发工具自动执行。

`analysis_user_store_snapshot` 人店关系快照及其周/月任务继续服务原经营报告；system 实时门店组织 Dubbo API 也继续供投诉任务使用。
