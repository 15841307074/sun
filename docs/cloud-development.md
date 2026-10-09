# 云端源码与本地同步

本仓库 `main` 是 `feature/store_tags_cache_20260921` 在 2026-10-09 的独立脱敏源码快照。原始提交号见 `cloud-source-manifest.json`。快照包含当时未提交的业务文档、SQL 迁移和 Java 测试，不携带原仓库提交历史。

支付证书、私钥、依赖目录、编译产物、本地工具与备份，以及历史 SQL 数据导出已排除。配置中的密码与密钥使用 `${CLOUD_...:}` 环境变量占位；源码里的硬编码凭据替换为 `CLOUD_SECRET_REQUIRED`。配置和排除清单见 `cloud-source-manifest.json`，其中不包含凭据值。

项目使用 Java 17 与 Maven。云端按任务选择模块构建；运行真实业务服务还需要数据库、Redis、Nacos、第三方服务配置和由使用者安全提供的凭据。源码中的占位值应通过环境变量或配置注入接入，不能将真实值提交到 Git。本次同步验证针对文件安全、配置语法和 Git 内容完整性，不代表业务服务已经部署或完整构建通过。

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
