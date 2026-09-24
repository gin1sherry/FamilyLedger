# 家庭账本 Family Ledger

Android 原生消费记账应用（Kotlin + Jetpack Compose）。

## 文档

| 文档 | 说明 |
|------|------|
| `docs/DEVELOPMENT_PLAN.md` | 开发计划与决议遵守项 |
| `docs/PRD_ISSUES.md` | PRD 问题清单与决议 |
| 桌面 `家庭账本PRD_V1.1.docx` | 产品需求（已合并决议） |
| 桌面 `家庭账本PRD问题清单与决议_V1.docx` | 问题评审稿 |

## 工程

- 路径：`android/`
- 架构：MVVM + Repository + Hilt + Room + Compose
- 网络：仅 AI 识别；Key 放 `android/local.properties`（勿提交）

## 状态

- **M1 工程骨架 + M2 数据层完成**
- 单测（金额/预算/校验/月份）与 `assembleDebug` 均已通过
- **本阶段编码已停止**，待确认后进入 M3（手动记账 + 首页）

## Git

```bash
git status
git log --oneline
```
