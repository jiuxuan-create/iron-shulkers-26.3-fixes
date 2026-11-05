# 构建 / Build

本仓库只包含补丁源码，不含上游模组源码，也不含构建好的 jar。

## 依赖

- JDK 25
- 上游 Iron Shulker Boxes 26.3-1.0.0 的 jar（作为重建基础）
- Minecraft 26.3 的客户端 jar 与 Fabric 库（编译时 classpath）
- 可选：QuickShulker 3.1.1-26.3 与 ShulkerBoxTooltip 5.4.2+26.3 的 jar（只有编译兼容层时需要）
- ASM 9.x（只有运行 PatchSuper 时需要）

## 步骤

1. 复制一份上游 jar，例如 cp ironshulkerboxfabric-26.3-1.0.0.jar work.jar

2. 把父类改成 ShulkerBoxBlock：

    java -cp asm-9.11.jar;asm-tree-9.11.jar PatchSuper.java work.jar
    # 输出应为 PATCHED super=net/minecraft/world/level/block/ShulkerBoxBlock

3. 修正 102 个战利品表（就地改写 work.jar）：

    python patches/tools/fix_loot.py work.jar

4. 编译补丁类（把 <mc>、<libs>、<qs>、<sbt> 换成实际路径）：

    javac -encoding UTF-8 -proc:none \
      -cp "work.jar;<mc-26.3.jar>;<qs.jar>;<sbt.jar>;<所有 Fabric 库>" \
      -d out patches/java/com/astryxion/ironshulkerbox/RegistryBootstrapFix.java \
          patches/java/com/astryxion/ironshulkerbox/common/item/IronShulkerBoxBlockItem.java \
          patches/java/com/astryxion/ironshulkerbox/compat/*.java

5. 把 out 下的 class 与 patches/fabric.mod.json 写回 work.jar：

    jar uf work.jar -C out .
    jar uf work.jar fabric.mod.json      # 注意：需要先在 jar 根放好新的 fabric.mod.json

   或者直接用 java.util.zip / Python zipfile 覆盖对应条目。

6. 校验：

    jar tf work.jar | findstr /C:"compat/" 
    # 应看到 BoxContainer, InsertPrediction, IronBoxPreviewProvider,
    #        QuickShulkerCompat, SbtCompat, ScaledPreviewRenderer
    # 且没有 META-INF/*.SF / *.RSA / *.DSA

## fabric.mod.json

补丁版在原有 entrypoints 基础上只增加两项：

    "main": [ "com.astryxion.ironshulkerbox.IronShulkerBoxes",
              "com.astryxion.ironshulkerbox.RegistryBootstrapFix" ],
    "quickshulker":     [ "com.astryxion.ironshulkerbox.compat.QuickShulkerCompat" ],
    "shulkerboxtooltip": [ "com.astryxion.ironshulkerbox.compat.SbtCompat" ]

Fabric 只会实例化被别的模组查询过的入口点，所以在没装 QuickShulker / ShulkerBoxTooltip 的
环境下，这两个兼容类永远不会被加载，也不会有 NoClassDefFoundError。

如果不想发布与原 jar 相同版本号，建议把 version 改成 26.3-1.0.0+fix.2。本仓库的构建保持
26.3-1.0.0，以免客户端与服务端因版本号不同而互相警告。
