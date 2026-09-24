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
- **当前版本**：`0.5.0`（versionCode `5`，对应里程碑 M5）

## 状态

- **M1–M5 完成**（应用版本 **0.5.0**）：骨架、数据层、记账首页、编辑删除、预算与 80% 横幅
- 单测与 `assembleDebug` 已通过
- **本阶段编码已停止**，待确认后进入 M6（识别 F1）

## Git

```bash
git status
git log --oneline
```
