# 家庭账本 MVP 验收清单（M9）

对照 PRD V1.1 第 10 章；P1-08 识别准确率专项按决议搁置。

| # | 项 | 标准 | 实现/自测 |
|---|----|------|-----------|
| 1 | 拍照识别 | 有 Key 可走通确认保存；无 Key 提示并转手动 | CaptureScreen / AiRecognitionService |
| 2 | 小票多条 | 多条勾选，逐条入库，共享 batchId | CaptureViewModel receipt 分支 |
| 3 | 首页展示 | 汇总 + 时间线，本地分页 20 | HomeScreen |
| 4 | 价格折线图 | 同 productKey ≥2 次显示 | ProductScreen + ProductMatcher |
| 5 | 分类预算 | 进度条；≥80% 横幅一次 | Budget + BudgetAlertBus |
| 6 | 总预算 | 独立 monthlyBudget | SettingsRepository |
| 7 | 手动补录 | 离线可保存 | ManualEntryScreen |
| 8 | 编辑/删除 | F9 + 回算 notified | Edit/Detail + CheckBudget |
| 9 | 搜索 | 名称/店铺/备注/分类/动态字段 | SearchViewModel.searchCombined |
| 10 | CSV | 全量、BOM、含 ID、两位金额 | ExportCsvUseCase |
| 11 | Widget | 桌面显示本月与分类进度 | SpendingWidget (Glance) |
| 12 | 深色模式 | 跟随系统 Compose 主题 | FamilyLedgerTheme |
| 13 | 动态字段 | 识别 extras 持久化 | Record.fields |
| 14 | 无网/失败 | 提示 + 重试 + 转手动 | Capture ERROR 阶段 |
| 15 | 离线优先 | 除识别 API 外无网络依赖 | 本地 Room/DataStore |

## 构建

- `testDebugUnitTest` 通过  
- `assembleDebug` 通过（M9 出包）  
- 版本：**0.9.0**（versionCode 9）

## 已知限制

- 未配置 `AI_API_KEY` 时识别不可用（符合「Key 本机」决议）  
- Widget 显示分类预算进度；总预算进度依赖 DataStore，小部件侧仅展示已花与分类  
- 折线图点击数据点未单独做命中，列表可进详情  
