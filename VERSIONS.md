# 版本演进 / Version history

三个版本共用一个 mod id（`ironshulkerbox`），**同一时间只能装一个**。

| 版本 | 内容 | 产物（字节 / 条目） | SHA256 |
|---|---|---|---|
| **fix1.0** | 最初的三个修复：专用服务器开箱崩溃、拆箱物品消失、QuickShulker 便携打开/收纳 | 1,059,369 / 1088 | `2555E3E8B74D79CFB6FA00CFFBB567B8C1254D5E3A7EBEDBB863B5B3F63C2D6D` |
| **fix1.1** | + 收纳时箱子被短暂拿起（客户端预测）、Shulker Box Tooltip 预览适配（全量格位 + 整体缩放） | 1,066,750 / 1093 | `7FD5F1057A9D96250F3F7AA031FE47FF212B5536644CA81E54A76D5A90FEA81C` |
| **fix1.2**（Latest） | + 方块改为继承原版 `ShulkerBoxBlock`（修掉与原版箱子的互相嵌套/交换）、预览网格按各档位真实列数（108 格为 12 列）、面板横纵双轴缩放 | 1,067,461 / 1093 | `7B7445857605D332023ADB0965EA7566116BD0D2430CD2A44FABEEA38D420C56` |

## 每个版本修了什么

### fix1.0
- `RegistryBootstrapFix`：把六个 `MenuType` 的注册提前到模组加载阶段，修掉专用服务器上 `Registry is already frozen` 导致的崩溃。
- `fix_loot.py`：把 102 张战利品表里的 `"functions":[...]`（26.3 已失效）改写为 `"modifier":{...}`，拆箱子不再丢物品。
- `QuickShulkerCompat`：向 QuickShulker 注册六个方块类，使箱子能在背包/手持打开并互相收纳。

### fix1.1
- `InsertPrediction` + 重写的 `IronShulkerBoxBlockItem`：在客户端做与原版一致的点击预测，收纳时箱子不再被拿起到鼠标上。
- `IronBoxPreviewProvider` + `ScaledPreviewRenderer`：提示框显示全部格位，并按屏幕高度缩放。
- 已知遗留（1.2 修复）：9 列网格对 108 格档位偏高；与原版箱子可互相嵌套/交换。

### fix1.2
- `PatchSuper`：把 `AbstractIronShulkerBoxBlock` 的父类由 `BaseEntityBlock` 改为 `ShulkerBoxBlock`（原模组已重写全部相关方法，构造器语义等价），于是 QuickShulker 的 `instanceof ShulkerBoxBlock` 判定对模组箱子成立。
- 预览网格改用 `IronShulkerBoxesTypes.rowLength`（铜/铁/金 9 列，钻石/水晶/黑曜石 12 列），与开箱界面一致。
- 缩放改为横纵双轴取最小值。

## 未公开的中间产物

- `-serverfix.jar`：只含服务器崩溃修复，是公开前的内部版本。

## 许可

这些构建都是上游 GPLv3 作品的修改版，随仓库一并以 GPLv3 提供；修改人：zXav1er（2026-11-06）。
