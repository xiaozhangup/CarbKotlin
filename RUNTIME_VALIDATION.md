# 去 TabooLib 远端验证

时间：2026-09-26 00:51–01:04（Asia/Shanghai）。
主机：s1.dimc.cloud；服务根目录：`/home/xiaozhangup/Minecraft`。

## 结果

| 节点 | tmux | 结果 |
| --- | --- | --- |
| SlimeCargo-Master | ma:0.0 | CarbKotlin、Cubozoa、SlimeMasterNext 启动成功；数据库、API、传输服务初始化成功；节点和 MSPT 查询成功 |
| SlimeCargo-Lobby | lo:0.0 | 9 个迁移插件及 Pipes、Crab 启动成功；本轮日志无 ERROR、类加载异常或插件禁用 |
| SlimeCargo-Worker-1 | wo1:0.0 | 共享插件启动成功；时装缺失及其抽奖模块连带错误按用户要求跳过；原有 CraftEngine 工作流警告保留 |
| SlimeCargo-Worker-2 | wo2:0.0 | 同 Worker-1 |

Worker-3 未操作会话或单独测试。用户指定的 `./U.sh sc update q` 自身会同步 update 目录到全部 Worker，包括 Worker-3。

以上为首轮检查范围，当时未覆盖所有 WARN。后续排查发现 Lobby 的 CreativeSubset 顺序问题，以及 Worker-3 的待更新目录被后一次同步覆盖，详见文末补充。

没有发现重复类、LinkageError、ClassCastException 或其他 classloader 冲突；没有实施任何 classloader 冲突修复。

## 修复

1. **Crab Paper Placeholder 自动注册**
   - 首轮异常：`NoSuchMethodException: WhaleVisitor$activeModules$2.<init>()`。
   - 原因：Whale 的匿名 Placeholder 适配器捕获了模块实例，需手动创建；自动扫描却再次尝试无参构造。
   - 修复：自动注册排除匿名适配器，保留 Whale 已有的显式注册。
   - 验证：Whale 成功启动，其依赖插件缺少 Whale 类的连带异常消失；三个 Paper 节点均成功解析 `network-online=%network_online%`，返回 `network-online=0`。
2. **Pipes FlexibleItem 接口**
   - 首轮异常：`NoClassDefFoundError: me/xiaozhangup/slimecargo/utils/flexible/FlexibleItemHandler`。
   - 修复：注册表和 Handler 导入改为 Crab 的 `flexible` 包；编译依赖改为 `me.xiaozhangup.crab:CarbKotlin:2.3.20:paper`，关闭传递依赖。
   - 验证：构建、本地 Maven 发布成功；三个 Paper 节点加载 Pipes 并成功执行 `pipes info`。
   - Pipes 已列于 PROJECTS.md，不重复添加。

## 实际执行的功能验证

- `velocity plugins`：包含 CarbKotlin、Cubozoa、SlimeMasterNext。
- `slimemaster node` / `node worker` / `mspt`：Lobby、Worker-1、Worker-2 在线；两个 Worker 有实时 MSPT 返回。
- `plugins`：所有本次更新的 Paper 插件均完成启用，第二轮无插件禁用记录。
- `whale module Cosmetics`：命令和模块查询正常；仅证明模块已注册，不代表缺少资源的 Worker 时装初始化成功。
- `papi list` / `papi parse --null network-online=%network_online%`：扩展注册及实际解析成功。
- `tardigrade events`：三个节点均返回 73 个事件；`tardigrade instances` 返回 0 个当前实例。
- `opossum reload`：Lobby 返回配置重载成功。
- `dolphinsync` / `poly list`：命令解析和输出正常。
- `pipes info`：各节点查询成功，无孤立显示实体。
- `tps`：三个 Paper 节点当前 1 分钟 TPS 为 20。
- `save-all flush`：三个节点均完成保存，没有事件分发异常。
- Whale、DolphinSync、Raven、SlimeMaster 的 Hikari 数据源成功启动；Raven 加载 178 个任务并挂钩 SlimeCargo。

无参数 `tardigrade` / `opossum` 返回命令不完整，随后有效子命令验证成功。`slimecargo_platform` 的空玩家请求返回未实现，与原 TabooLib Placeholder 的 OfflinePlayer 默认实现一致，未为这次验证改动原有语义。

## 未修改的问题与验证边界

- `Unknown resource pack: default` 已出现在 Worker 的 2026-09-24 启动日志，属于迁移前已有的 CraftEngine 配置警告。
- Worker 缺少 `craftengine:backpack_plus:bastion_backpack` 等资源，导致 Cosmetics 初始化失败以及 CosmeticLottery 连带报错。用户确认资源尚未同步，明确要求不处理。
- 未进行玩家登录后的菜单点击、物品交互、跨服背包同步等客户端验收。远端原有维护状态保持不变。

## 部署与保留

- Master 原有三个 jar 直接替换；Lobby 独占的 OrangDomain、SharkChest 直接替换。
- 共享插件仅放入 Lobby 的 `plugins/update`，通过 `./U.sh sc update q` 同步，再由正常重启应用。未直接改写 Worker 的插件目录。
- 保留原安装文件名 `CarbKotlin-2.3.20.jar`，分别使用对应平台运行包，避免同一目录出现两份 Crab。
- SHA-256 验证：Master 3 份、Lobby 11 份、Worker-1 9 份、Worker-2 9 份，均与本地构建一致。
- 旧 jar 备份位于远端 `migration-backup-20260926-005101`；未删除任何非 jar 文件。初轮启动日志已由服务器正常日志轮转保留。
- 本轮重新发布了 Crab API 和 Pipes 到本地 Maven，源码改动未提交，保留原工作区已有改动。

## 01:14 启动日志复查

对照各节点 `logs/2026-09-24-3.log.gz` 与本次最新启动日志，确认以下新增问题：

1. **Lobby：CreativeSubset 缺少 69 个书本条目**
   - Crab 扫描按类名排序，CreativeSubset 在 CustomRecipe 注册配方条目前执行，分组时找不到这些条目。
   - 初次修复采用 `afterInit()`，随后按用户要求撤销，最终改为 `@WhaleDepend` 声明 Unit 加载依赖，见下节。
   - 修复涉及 WhaleMechanism 1 个插件、4 个源码文件：WhaleModule.kt、WhaleVisitor.kt、CreativeSubset.kt、CreativeGuide.kt。
   - Whale 构建与本地 Maven 发布成功，运行包已上传到约定的 Minecraft 目录。
2. **Worker-3：Pipes 新旧注册表不一致**
   - Pipes 和 Crab 已更新，但包括 Whale、SlimeCargo 在内的其他共享插件仍是旧 jar；Whale 日志仍有旧 TabooLib 入口。
   - `U.sh sc update q` 对目标 update 目录执行整体替换。第二轮只同步 Crab、Pipes，覆盖了 Worker-3 尚未应用的其他更新包。这是前一轮部署遗漏。
   - 重新在 Lobby 的 update 目录准备完整 9 个共享插件，经 U.sh 同步；四个节点的待更新 jar 均通过 SHA-256 校验。Worker-3 旧 jar 备份于原备份目录的 `Worker-3-before-full-update/`。
   - Worker-3 实际位于另一台托管服务器，本机为 SSHFS 文件挂载；用户负责通过面板重启。

NBTAPI、PlugManX、SLF4J、旧 MySQL 驱动、spark 统计超时等对应提示在 9 月 24 日已有，未作为本次迁移回归修改。Worker 的时装资源缺失继续按用户要求跳过。

### 补充验证结果

- Lobby 于 01:29:27 完成启动；原有 69 条 CreativeSubset 警告归零，无配方缺失、模块初始化失败或新增 WARN/ERROR。`customrecipe list` 返回 125 个配方，`pipes info` 查询成功。
- Worker-1、Worker-2 分别于 01:31:25、01:31:26 完成启动；没有 CreativeSubset、Pipes 配方、类加载异常，仅保留未同步时装资源导致的 Cosmetics / CosmeticLottery 错误。
- 上述三个节点各 9 个共享插件的已安装 jar 均与最新本地产物 SHA-256 一致，update 目录已消费完毕。
- Worker-3 最初于 01:27:16 的启动早于同步完成。用户再次从面板重启后，于 01:34:27 完成启动，9 个已安装 jar 均与最新构建 SHA-256 一致，待更新目录已消费完毕。
- Worker-3 的 Pipes 配方缺失、CreativeSubset 文件处理异常及旧 TabooLib 调用栈均消失，无类加载异常。时装资源缺失继续忽略；spark 统计超时在 9 月 23 日已有，BiliBili 消息查询超时在 9 月 21 日已有。
- 本轮同步前确认所有目标 update 目录中没有非 jar 文件。没有直接改写 Worker 的 plugins 文件，没有删除配置或其他非 jar 文件。

## WhaleDepend 替换 afterInit

- 新增 `WhaleMechanism/src/main/kotlin/me/xiaozhangup/whale/tag/visitor/WhaleDepend.kt`，支持 `@WhaleDepend("Unit名称", "另一Unit名称")`，以 `@WhaleUnit.name` 匹配。
- `WhaleVisitor.kt` 在实例化前按依赖关系排序；注册顺序与 `init()` 顺序保持一致，仍然先注册全部 Unit，再初始化全部 Unit。依赖缺失、被禁用或成环时报告相关名称并跳过受影响的 Unit；重复 Unit 名称明确报错。
- 已删除 `WhaleModule.afterInit()` 及调用，CreativeSubset 恢复通过 `init()` 初始化，手动重载仍调用 `init()`。
- 扫描 150 个 Unit，为其中 10 个 Unit 声明 14 条依赖关系：

| Unit | 依赖 | 源码位置（相对 WhaleMechanism/src/main/kotlin/me/xiaozhangup/whale） |
| --- | --- | --- |
| CreativeSubset | CreativeGuide、CreativeDisplay、CreativeRecipe、CustomRecipe | module/CreativeSubset.kt |
| CreativeDisplay | CreativeGuide | module/CreativeDisplay.kt |
| CreativeRecipe | CreativeGuide | module/CreativeRecipe.kt |
| CosmeticLottery | Cosmetics | module/shop/CosmeticLottery.kt |
| ActionGuide | ActionbarOverlay | module/ActionGuide.kt |
| KauCim | Sign | module/KauCim.kt |
| Villager | Sign、Coins | module/shop/Villager.kt |
| Payment | HandMap | module/payment/Payment.kt |
| EnchantmentRender | EnchantmentMenu | module/enchantment/ui/EnchantmentRender.kt |
| MiniBlock | HeadDatabase | module/MiniBlock.kt |

CosmeticLottery 同时移除原先用于等待 Cosmetics 的一 tick 启动任务及其取消状态，改为按依赖顺序直接加载卡池。玩家交互和抽奖动画自身的调度保持原有行为。

CreativeGuide 的手动重载反向引用，以及 ChatCommand、IdleDetector、Cosmetics、RareMobs 等已有的可选联动不声明为硬依赖；CustomRecipe 在没有 CreativeGuide 时仍可只注册普通配方。背包类型识别等静态工具调用不依赖对应 Unit 的加载，BiomeInquirer 属于物品实现，也不是 WhaleUnit。

Whale 构建与本地 Maven 发布成功，产物上传至约定目录；完整共享更新包通过 Lobby 的 update 目录及 `U.sh sc update q` 同步。01:51:00 在 ma 执行一次 `slimemaster execute * stop`，四个服务端均正常停止并自动重启，包括 Worker-3。

### 本轮实际验证

- 启动完成时间：Worker-3 01:51:40；Worker-1、Worker-2 01:52:26；Lobby 01:52:33。
- 四个节点各 9 个已安装共享 jar 均通过最新本地产物 SHA-256 校验，待更新目录已消费完毕。
- 没有 Unit 依赖缺失、循环依赖、CreativeSubset 条目缺失、配方缺失或 classloader 异常。Lobby 对照旧启动日志没有新增 WARN/ERROR；Worker 仅有已确认忽略的时装资源缺失及 CosmeticLottery 连带错误。
- 通过 ma 广播 `customrecipe list`、`pipes info`，四个节点均返回 125 个自定义配方，Pipes 查询成功且没有孤立显示实体。
- SlimeMaster 确认 lobby、worker-1、worker-2、remote-1（Worker-3）在线；三个工作节点 MSPT 分别约 0.65、0.66、0.36。
- MiniBlock 仍按原配置禁用，其依赖声明已构建，但没有进行对应运行功能验证。没有新增测试单元，没有删除非 jar 文件。

## CE 同步后的重启复查（2026-09-26 01:59–02:04）

用户完成 CE 同步后，于 01:59:29 从 ma 执行一次 `slimemaster execute * stop`。Master 保持运行作为控制端，Lobby 和三个 Worker 均自动重启；本轮没有更新 jar 或删除文件。

| 节点 | 启动完成时间 | WARN 行数 | ERROR 行数 |
| --- | --- | --- | --- |
| Lobby | 02:01:00 | 21 | 0 |
| Worker-1 | 02:00:55 | 19 | 0 |
| Worker-2 | 02:00:56 | 19 | 0 |
| Worker-3 | 02:00:13 | 20 | 0 |

- 四端均加载 CE cosmetics 包并注册 cosmetics 占位符。先前的时装物品缺失、Cosmetics / CosmeticLottery 初始化失败均未再出现；没有 CreativeSubset 条目缺失、Unit 依赖错误或 classloader 异常。
- 02:03:24 SlimeMaster 确认 lobby、worker-1、worker-2、remote-1 全部在线。02:03:26 四端 `customrecipe list` 均返回 125 个配方，随后 `pipes info` 全部成功且没有孤立显示实体。
- Worker 的 `Failed to load resource pack workflow: default` 按用户要求忽略，表内 WARN 总数仍包含该警告。
- 仍有此前已见的 NBTAPI 2.15.7 不支持 26.2 / 更新提示、PlugManX 对 Paper 插件的限制、离线代理模式提示、spark 世界统计超时、服务端版本查询提示，以及 Worker-3 的默认语言回退；Lobby 的 DecentHolograms 内置 NBTAPI 同样报告兼容警告。
- **本轮另有一条需关注的 Lobby 警告**：02:01:07 `[Payment] QR payment account is not ready`。检索 9 月 24–26 日此前归档日志未找到同一提示。源码中它表示启动时 `paymentClient()` 失败，该调用包括读取凭据、请求爱发电账号资料和保存账号标记；当前日志没有保留具体异常，不能据此断定凭据失效或迁移回归。没有更改凭据或触发付款。
- 本轮验证覆盖启动日志、节点联通和上述查询命令；未验证客户端资源包显示、时装穿戴或实际支付。

## 补发 RealisticSeasons（2026-09-26 02:12–02:16）

清理上传包时发现遗漏：四端安装的 RealisticSeasons 仍内置并 relocate adventure-kt，根目录上传包已改为引用 Crab 共享版本。两份 plugin.yml 均标记 10.7.4，文件名仍为 RealisticSeasons-6.12.jar。

- 旧包备份：远端 `Minecraft/migration-backup-20260926-021143/RealisticSeasons-before-shared-adventure.jar`。
- 新包经 Lobby/plugins/update 和 `U.sh sc update q` 同步，02:12:10 从 ma 执行 `slimemaster execute * stop`。
- 四端安装包 SHA-256 均为 `d82409d219a30ea704a2a889befec0e9b164d44fe8c1dec823bde05c00194557`，待更新目录已消费。Worker-3 02:12:53、Worker-1 02:13:40、Worker-2 02:13:42、Lobby 02:13:48 完成启动；四端在线。
- RealisticSeasons 四端均正常启用，注册 401 个生物群系，帮助命令全部返回；启动阶段无 ERROR、无类加载冲突。资源包工作流警告继续按要求忽略。Payment 账号未就绪警告本轮出现在 Worker-1/2。
- 执行只读 `season world`：四端均成功经过 `plainMini` 调用共享 adventure-kt 并输出中文文本，Lobby 返回世界未启用季节，三个 Worker 返回季节信息。
- 查询同时暴露一个旧缺陷：Worker-1 在输出时间时抛出 `PatternSyntaxException: Illegal octal escape sequence`；Worker-2/3 的时间占位符未替换。`TimeManager.kt:175` 等代码把实际秒/分钟/小时数插入正则，而非匹配字面的 `$seconds$`、`$minutes$`、`$hours$`。更新前后 `TimeManager.class` SHA-256 完全相同（`b1f3989a0302b9ffcb4ea9fde6485fcecd8dbdff5ea452c80f7d67f1366d66da`），确认缺陷已存在于旧包。本轮没有扩大修改该业务代码；Worker-1 因本次查询新增 1 条 ERROR，其余三端 ERROR 为 0。
- 验证仅覆盖启动、依赖调用及查询；没有改变季节、时间、配置或玩家数据，未进行客户端季节视觉验收。

## 统一源码包名为 me.xiaozhangup.carb（2026-09-26 02:38–02:41）

- Crab 的 common、paper、velocity 源码及入口统一到 `me.xiaozhangup.carb`；同步更新插件描述文件、API 打包路径和文档。Maven 坐标继续使用已约定的 `me.xiaozhangup.crab:CarbKotlin:2.3.20:paper/velocity`。
- 同步修改 12 个调用方：SlimeCargoNext、SlimeMasterNext、WhaleMechanism、Cubozoa、Opossum、Raven、Tardigrade、DolphinSync、OrangDomain、Pipes、SharkChest、Spectator。共 13 个项目、704 个文本文件更新，Crab 253 个源码文件迁移目录。
- 全部项目构建成功，相关 API 已发布本地 Maven；14 份运行 jar 均上传。当前源码及产物未检出旧源码包名前缀。扫描已安装插件的字节码未发现额外调用方，运行配置和脚本未发现旧包名引用。
- 9 个共享插件通过 Lobby/plugins/update 和 U.sh 同步；Lobby 的 OrangDomain、SharkChest 直接替换。02:38:17 从 ma 广播 stop，02:38:19 停止 Master，替换 Master 的 Crab、Cubozoa、SlimeMaster 后于 02:38:25 重新运行 Start.sh。本轮未额外创建备份。
- 启动完成：Master 02:38:42、Worker-3 02:39:01、Worker-1 02:39:47、Worker-2 02:39:48、Lobby 02:39:56。Master 3 个、Lobby 11 个、每个 Worker 9 个安装 jar 均与本地产物 SHA-256 一致，待更新目录已消费。
- 截至复查，五端 ERROR 均为 0，无旧包名类缺失、方法缺失、类加载冲突或 Unit 初始化错误。四个 Paper 节点全部在线；四端 customrecipe list 返回 125 个配方，pipes info 返回成功且无孤立显示实体。仅保留已有兼容/更新提示，资源包工作流警告按用户要求忽略。
- 已部署的根目录上传 jar 随后清理；没有删除非 jar 文件，没有新增测试单元。验证覆盖构建、包名引用、启动及上述查询，不代表所有游戏内交互已逐项验收。
