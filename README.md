# 家庭账本 Family Ledger

Android 原生消费记账应用（Kotlin + Jetpack Compose）。

## 文档

| 文档 | 说明 |
|------|------|
| `CHANGELOG.md` | 版本更新日志 |
| `docs/DEVELOPMENT_PLAN.md` | 开发计划 |
| `docs/ACCEPTANCE.md` | M9 验收清单 |
| `docs/PRD_ISSUES.md` | PRD 问题决议 |
| 桌面 `家庭账本PRD_V1.1.docx` | 产品需求 |

## 工程

- 路径：`android/`
- 架构：MVVM + Repository + Hilt + Room + Compose
- 网络：仅识别 API；Key 放 `android/local.properties`（勿提交）
- **当前版本**：`0.9.0`（versionCode `9`）

## 状态

- **M1–M9 完成**：手动记账、首页、编辑删除、预算横幅、拍照识别、价格走势、搜索、CSV、Widget、验收文档
- 单测与 `assembleDebug` 已通过
- 识别需在 `local.properties` 配置 `AI_API_KEY` / `AI_BASE_URL` / `AI_MODEL`

## Git

```bash
git status
git log --oneline
```
