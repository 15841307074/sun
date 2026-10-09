# 云端源码与本地同步

本仓库 `main` 是 `feature/store_tags_cache_20260921` 在 2026-10-09 的独立脱敏源码快照。原始提交号见 `cloud-source-manifest.json`。快照包含当时未提交的业务文档、SQL 迁移和 Java 测试，不携带原仓库提交历史。

支付证书、私钥、依赖目录、编译产物、本地工具与备份，以及历史 SQL 数据导出已排除。配置中的密码与密钥使用 `${CLOUD_...:}` 环境变量占位；源码里的硬编码凭据替换为 `CLOUD_SECRET_REQUIRED`。配置和排除清单见 `cloud-source-manifest.json`，其中不包含凭据值。

项目使用 Java 17 与 Maven。云端按任务选择模块构建；运行真实业务服务还需要数据库、Redis、Nacos、第三方服务配置和由使用者安全提供的凭据。源码中的占位值应通过环境变量或配置注入接入，不能将真实值提交到 Git。本次同步验证针对文件安全、配置语法和 Git 内容完整性，不代表业务服务已经部署或完整构建通过。

## 云端编译与测试

云环境的安装脚本准备 Java 17、Maven、代理证书和依赖缓存。云端任务使用现有的 `/workspace/sun` 工作区，不需要额外创建 Git worktree。

```bash
cd /workspace/sun
source /workspace/.sun-cloud/env.sh
/workspace/.sun-cloud/mvn -T 2 -pl youdao-module-system/youdao-module-system-biz -am \
  -Dtest=CollectionUtilsTest,StoreCityListCacheServiceTest,SystemStoreInfoCityCacheTest,BossStoreSimpleQueryTest,AppVersionConfigServiceTest,StoreBackgroundCacheServiceTest \
  -Dsurefire.failIfNoSpecifiedTests=false package
```

此命令运行公共模块 1 项、system 模块 20 项隔离单元测试并打包 system 服务。`surefire.failIfNoSpecifiedTests=false` 允许上游模块没有所选测试类，不会忽略测试失败。`OrgTreeServiceImplTest` 会启动完整 Spring 应用并读取真实测试环境，不属于上述隔离测试范围；没有服务连接条件时不能把它报告为通过。

## 接入现有测试环境

确认使用的测试环境是：

- MySQL：`192.168.31.141:3307`，数据库 `cloud_0090_test` 和 `cloud_0090_test_shadow`。
- Redis 集群：`192.168.31.141`、`192.168.31.142`、`192.168.31.143`，每台的 `6379`、`6380` 端口；集群返回的节点地址也必须能从云端访问。
- Nacos：上述三台的 `8848` 端口，客户端 gRPC 通常还需要 `9848`；命名空间使用 `dev-local` Maven 配置中的 `test-cloud-0090-id`。

这些是内网地址。网络域名放行不能建立内网路由；需要在云环境设置中配置受支持的 VPN，或由环境管理员提供同一测试环境的可达入口。不要公开暴露数据库或 Redis，也不要在代理无法到达时通过关闭验证来绕过限制。

密码在云环境设置中安全填写，不写入仓库或聊天：

| 环境变量 | 用途 |
| --- | --- |
| `CLOUD_NACOS_DISCOVERY_PASSWORD` | Nacos 服务注册密码 |
| `CLOUD_NACOS_CONFIG_PASSWORD` | Nacos 配置中心密码 |
| `CLOUD_DATASOURCE_LOCAL_PASSWORD_L40` | 主数据库密码，配置模板中的变量 |
| `CLOUD_DATASOURCE_LOCAL_PASSWORD_L44` | 影子数据库密码，配置模板中的变量 |
| `CLOUD_DATASOURCE_LOCAL_PASSWORD_L59` | Redis 集群密码，配置模板中的变量 |

system 服务优先读取新的 Nacos 变量，并保留旧的 `CLOUD_APPLICATION_PASSWORD_L31`、`CLOUD_APPLICATION_PASSWORD_L37` 作为兼容回退。根 POM 的各环境配置不再保存 Nacos 明文密码。旧提交可能仍含此前的密码；有效凭据应由管理员轮换，删除当前文件中的明文并不会删除历史版本。

实际 Nacos 配置里的其他密码占位、RabbitMQ、Dubbo 下游服务及第三方配置也需要按测试环境的真实设置接入。原始 SQL 文件不是完整的空库初始化方案，不要对现有测试数据库自动执行迁移或数据导入。

网络连通且运行配置齐全后，再启动服务并执行 `OrgTreeServiceImplTest` 和功能请求。云端日志应写入 `/workspace/.sun-cloud/runtime/logs`；Nacos 使用 JVM 参数 `-DJM.LOG.PATH=/workspace/.sun-cloud/runtime/logs`，Spring 日志使用 `-Dlogging.file.name=/workspace/.sun-cloud/runtime/logs/system-server.log`。完整服务启动尚未验证，以上参数仅用于指定可写日志目录。

## 本地拉取

原工作区为 `E:\cloud-0090`，其 GitLab `origin`、原分支与未提交文件继续保留。新增 `cloud` 远端指向本 GitHub 仓库，本地 `codex/cloud-main` 分支跟踪 `cloud/main`。

独立云端源码目录为 `E:\cloud-0090\.storage-maintenance\sun-cloud`，其中 `main` 跟踪本 GitHub 仓库。该目录在原仓库的本地排除规则中忽略。

在 PowerShell 中从原工作区执行：

```powershell
Set-Location E:\cloud-0090
git cloud-fetch
git cloud-pull
```

`cloud-fetch` 只更新原仓库的 `cloud/*` 远端引用。`cloud-pull` 先核对独立目录的分支与工作区状态，再对其 `main` 执行 `pull --ff-only`；有未提交修改、处于其他分支或分支分叉时停止。它不会把云端代码直接写入原业务分支。

需要查看云端更新，也可直接执行：

```powershell
git -C E:\cloud-0090\.storage-maintenance\sun-cloud log -5 --oneline
git -C E:\cloud-0090\.storage-maintenance\sun-cloud status --short --branch
```

## 把云端修改纳入原业务分支

快照与原业务仓库历史独立。不要直接合并整个 `cloud/main`，也不要把原分支直接推送到 GitHub，因为原分支仍含本地凭据。

确认云端新提交后，可在原业务分支逐个 `git cherry-pick <云端新提交号>`（不要选初始快照提交）。若凭据占位或原有未跟踪文件导致冲突，应逐文件检查和合并，保留本地真实配置与未提交内容。操作前先提交或备份准备整合的本地业务文件。
