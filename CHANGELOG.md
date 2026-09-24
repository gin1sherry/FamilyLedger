# 更新日志

## 0.7.0 — M6 + M7

日期：2026-09-24

### 新增

#### M6 拍照记账（F1）
- 图片压缩管线（EXIF / 缩放 / JPEG 质量阶梯），参数按 PRD 4.2.4
- 识别服务：仅调外部多模态 API；Key 读 `local.properties` → BuildConfig
- 识别流程：选图 → 压缩 → 识别 → 确认页（单品 / 小票多条勾选）
- 失败处理：无 Key / 超时 / HTTP / 非 JSON → 提示 + 重试 + 转手动
- 无网暂存：写入 `pending_images`，提示可重试
- 低置信度（&lt;60%）橙色提示；extras 展示为自定义字段
- 入口：首页相机按钮

#### M7 商品价格走势（F5）
- `ProductMatcher`：规范化 productKey（规格后缀、全半角、品牌别名）
- 详情页「价格走势」→ 商品详情
- ≥2 次购买显示折线图（Canvas），&lt;2 次仅列表
- 点击购买记录可回详情

### 测试
- 单测通过（含 ProductMatcher 品牌别名）
- `compileDebugKotlin` 通过（按约定本步不打安装包）

### 版本
- versionName 相对 0.5.0 → 本提交后为 0.7.0（M6+M7）
