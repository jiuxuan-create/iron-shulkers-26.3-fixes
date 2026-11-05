# Iron Shulker Boxes 26.3 — 非官方修复版 / Unofficial fix build

因为在和朋友玩的时候遇到了这些bug，就用deepseek修了一下，顺便做了QuickShulker和ShulkerBoxTooltip的适配

本仓库是 [Iron Shulker Boxes](https://github.com/astryxion/Iron-Shulkers) (id: ironshulkerbox, version 26.3-1.0.0)
的非官方补丁集，修复了 26.3 版本上的三个实际存在的问题，并额外为两个可选模组做了适配。
This repository is an unofficial patch set for Iron Shulker Boxes 26.3-1.0.0.

原模组作者（移植者）：Astryxion（模组最初原作者：ProgWML6）。本补丁与原模组作者无关，未获其背书。
Original mod by Astryxion (contributor ProgWML6). This patch is not affiliated with or endorsed by the original author.

## 修复内容 / What is fixed

| # | 问题 | 原因 | 补丁位置 |
|---|------|------|----------|
| 1 | 专用服务器上右键打开潜影盒导致服务器崩溃 | 六个 MenuType 在静态初始化块里注册，专用服务器上直到玩家第一次开箱才触发，此时注册表已冻结（Registry is already frozen） | patches/java/.../RegistryBootstrapFix.java |
| 2 | 拆掉潜影盒后里面的物品全部消失 | 26.3 把战利品表条目里的 functions 字段改名为 modifier，旧写法被静默忽略，copy_components 不执行 | patches/tools/fix_loot.py |
| 3 | QuickShulker 无法在背包/手持打开、无法收纳 | QuickShulker 按方块类名精确匹配，并只把 instanceof ShulkerBoxBlock 认作潜影盒 | patches/tools/PatchSuper.java + patches/java/.../QuickShulkerCompat.java |
| 4 | QuickShulker 收纳时潜影盒会被短暂拿起到鼠标上 | 客户端只对原版潜影盒做预测，模组潜影盒回退到原版拾取逻辑 | patches/java/.../InsertPrediction.java + IronShulkerBoxBlockItem.java |
| 5 | 潜影盒提示框只显示前 27 格 | ShulkerBoxTooltip 的通用容器提供器把容量硬编码为 27 | patches/java/.../IronBoxPreviewProvider.java + ScaledPreviewRenderer.java |

## 安装 / Install

1. 安装上游 Iron Shulker Boxes 26.3-1.0.0。
2. 用构建出来的补丁 jar 替换 mods 目录里的原 jar（同一 mod id，永远不要两个同时存在）。
3. 客户端和服务端都要装同一个 jar。

## 构建 / Build

见 build.md。补丁不修改原版游戏，也不修改任何其他模组文件，只重建 ironshulkerbox 这一个 jar。

## 已知限制 / Known limitations

- Iron Shulker Boxes 的方块现在继承原版 ShulkerBoxBlock，因此所有第三方模组的 instanceof ShulkerBoxBlock 判断都会把它们当作潜影盒（这正是修复 3 的目的）。
- 已经丢失的物品无法找回。
- RegistryBootstrapFix 只是把菜单类型注册提前到模组加载阶段。

## 许可 / License

GNU General Public License v3.0，与上游一致。详见 LICENSE 与 NOTICE。
