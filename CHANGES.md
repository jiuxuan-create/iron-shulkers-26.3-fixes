# 修改说明 / Changes

针对 Iron Shulker Boxes 26.3-1.0.0 的全部改动。所有改动都发生在重建的 ironshulkerbox jar 内部，
没有修改原版游戏文件，也没有修改任何其他模组。

## 1. 服务器开箱崩溃 / dedicated server crash on open

IronShulkerBoxesMenuTypes 的静态初始化块调用 Registry.register(BuiltInRegistries.MENU, ...)。
单机时客户端入口点会提前初始化这个类，注册发生在注册表冻结之前；专用服务器上没有任何东西在加载阶段
引用它，于是第一次开箱时才触发，抛出：

    java.lang.ExceptionInInitializerError
      at ...IronShulkerBoxMenu.createObsidianContainer(IronShulkerBoxMenu.java:72)
    Caused by: java.lang.IllegalStateException: Registry is already frozen
      (trying to add key ResourceKey[minecraft:menu / ironshulkerbox:iron_shulker_box])
      at net.minecraft.core.MappedRegistry.validateWrite(MappedRegistry.java:90)
      at ...IronShulkerBoxesMenuTypes.<clinit>(IronShulkerBoxesMenuTypes.java:13)

修复：新增一个 Fabric main 入口点 RegistryBootstrapFix，在 onInitialize() 里用
Class.forName(..., true, loader) 强制提前初始化该类。

## 2. 拆箱丢物品 / items lost when breaking a box

26.3 的战利品表条目把 functions 数组改名成 modifier 对象：

    26.2: "functions": [ { "function": "minecraft:copy_components", ... } ]
    26.3: "modifier":  { "type": "minecraft:copy_components", ... }

字段名对不上时 DFU 的 MapCodec 会静默忽略，箱子照常掉落，但没有 minecraft:container 组件，
于是内容为空。修复：patches/tools/fix_loot.py 把 102 个战利品表重写成 26.3 的形状：

    {
      "type": "minecraft:block",
      "pools": [
        {
          "rolls": 1,
          "entries": [
            {
              "type": "minecraft:item",
              "modifier": {
                "type": "minecraft:copy_components",
                "include": [
                  "minecraft:custom_name",
                  "minecraft:container",
                  "minecraft:lock",
                  "minecraft:container_loot"
                ],
                "source": "block_entity"
              },
              "name": "ironshulkerbox:iron_shulker_box"
            }
          ]
        }
      ],
      "random_sequence": "ironshulkerbox:blocks/iron_shulker_box"
    }

## 3. 让第三方模组认得出这些潜影盒 / instanceof ShulkerBoxBlock

QuickShulker 用 ShulkerUtils.isShulkerItem(stack) = Block.byItem(item) instanceof ShulkerBoxBlock
判断"这是不是一个潜影盒"。Iron Shulker Boxes 的方块继承 BaseEntityBlock，永远返回 false，
因此既不能被收纳，也不会被禁止嵌套，而且两个模组潜影盒右键会交换位置。

修复：patches/tools/PatchSuper.java 用 ASM 把 AbstractIronShulkerBoxBlock 的父类改成
net.minecraft.world.level.block.ShulkerBoxBlock，并把构造器里的 super 调用改成
ShulkerBoxBlock.<init>(DyeColor, Properties)（插入 ALOAD 2）。原模组已经重写了
ShulkerBoxBlock 的每一个方法，且 FACING 字段与 DirectionalBlock.FACING 是同一个对象，
所以行为不变。

## 4. QuickShulker 兼容层 / QuickShulker integration

patches/java/.../QuickShulkerCompat.java 通过 QuickShulker 自己的 quickshulker 入口点注册六个方块类：
背包/手持打开、开关音效、收纳与取出、以及"潜影盒不能放进潜影盒"的限制。
容器使用 SimpleContainer 子类，读写物品的 minecraft:container 组件。

## 5. 收纳闪烁 / insert flicker

QuickShulker 只在客户端对原版潜影盒预测整合点击（isShulkerItem 为真时），否则回退到原版拾取，
于是模组潜影盒会短暂被拿到鼠标上再弹回去。
修复：重写 IronShulkerBoxBlockItem，加入 overrideOtherStackedOnMe / overrideStackedOnOther，
先调用 super（服务端仍由 QuickShulker 处理），若未处理且处于客户端、潜影盒收纳方向，
则用 InsertPrediction 做同样的预测并返回 true。

## 6. 潜影盒提示框 / Shulker Box Tooltip

SBT 的通用容器提供器容量硬编码 27，导致 45/54/81/108 格的箱子只显示前 27 格。
修复：通过 shulkerboxtooltip 入口点注册 IronBoxPreviewProvider，容量取该等级真实大小，
列数取该等级自己 GUI 的列数（9 或 12）；并用 ScaledPreviewRenderer 包住 SBT 自带渲染器，
在面板超出屏幕时按比例整体缩放（同时按宽和高取比例，最小 0.25）。
缩略预览的合并列表也使用真实容量，因此所有物品与总数都能显示。
