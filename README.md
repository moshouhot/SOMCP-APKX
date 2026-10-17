# SOMCP-APKX

SOMCP-APKX 是 [SOMCP](https://github.com/bilieebiliee1-design/SOMCP) 的二次开发版本，**以 SOMCP `1.0.22` 源码树为基线**，在其上移植了 APK 全链路工具链。

上游能力完整保留：ELF 结构分析、Rizin 反汇编/分析、LIEF ELF 修复/重写、补丁会话、构建导出、Unidbg 模拟执行、AI 深度分析、子代理、JNI/壳/混淆/字符串解密/反调试/导入溯源、Cloudflare Tunnel、APK MCP 桥接、Flutter AOT（Blutter）分析。

本版新增一条**纯 APK 工作流**：DEX → Java 反编译、DEX ↔ smali 往返、资源解包、APK 回编/合并拆分包、v1/v2/v3 签名、加固壳内存脱壳、反混淆查找、字符串/密钥扫描、签名证书核对、两版 APK 差异、资源提取、内置 frida-server 起停。

当前版本：`1.0.22`（沿用基线 `versionName` / `versionCode` 23；本仓库按源码树分发，不发布 GitHub Release）

包名：`com.soreverse.mcp`

最低系统：Android 8.0 / API 26

许可证：`AGPL-3.0-only`。本项目是 SOMCP 的衍生作品，继续以 [GNU Affero General Public License v3.0](LICENSE) 授权，并保留上游 [NOTICE](NOTICE) 与运行时溯源信息。

> **再分发义务（重要）**：本软件受《中华人民共和国著作权法》与《计算机软件保护条例》保护。任何再分发（含修改、改名、二次打包版本）必须：保留版权与许可声明及 [NOTICE](NOTICE)、继续以 AGPL-3.0 授权、向每一位接收者提供完整对应源代码、并标注所做修改。上游 1.0.22 已把许可正文嵌进 DEX 与三个自建 `.so`（`Provenance.LICENSE_TEXT` / `cpp/license_notice.h`），该机制在本项目中完整保留。闭源分发、抹除署名或改名冒充原创即构成侵权——依 AGPL-3.0 第 8 条授权自动终止。本软件在 MCP `initialize`/健康检查返回与"关于"页固定输出许可证、上游仓库与版权，作为运行时溯源证据。

## 相对上游 1.0.22 的改动

只做加法，没有删除或回退任何上游能力、测试或构建校验。

| 改动 | 位置 |
|---|---|
| 新增 13 个 APK 全链路工具并注册进精简可见列表 | `app/src/main/java/com/soreverse/mcp/mcp/{JadxTool,StaticTools,ApkBuildTool,ApkEditorTool,DexKitTool,StringScanTool,ApkSignInfoTool,ApkDiffTool,ApkExtractTool,UnpackTool,FridaTool}.kt`、`mcp/ToolCatalog.kt` |
| 新增 root shell / frida-server / 内存脱壳实现 | `core/{RootShell,FridaManager,UnpackManager}.kt` |
| 新增移植工具的自产物防护、输入体积与堆闸门 | `mcp/ApkToolGuards.kt` |
| 新增 `decompile` / `dynamic` 两个工具分类并扩充 `build` 描述 | `mcp/ToolCatalogPresentation.kt` |
| 错误目录补齐移植工具的错误码 | `mcp/McpHttpServer.kt` |
| 新增 9 条 JVM 依赖（jadx / ARSCLib+APKEditor / smali / apksig / bouncycastle / dexkit / slf4j） | `app/build.gradle.kts` |
| 新增加载期保类规则 | `app/proguard-rules.pro` |
| 新增 6 条回归测试 | `app/src/test/java/com/soreverse/mcp/mcp/ApkToolchainTest.kt` |

### 与衍生版本 XuanXingNieHe 的差异

`XuanXingNieHe` 同样移植了这批工具，但代码基线停在 SOMCP `1.0.9` 并删除了 9 个上游工具网关与 `Provenance` / `MemoryGuard` / `SelfArtifactGuard` / `AppListGuard` / `ShellAssets` / 备份加密 / 子代理。本项目以 `1.0.22` 为基线，因此额外获得：

- 1.0.10 → 1.0.22 的全部修复：Rizin/LIEF 原生调用进程级串行化（修 `rz_core_free` 双释放 SIGSEGV）、APK 扫描与 SO 提取流式化、Flutter 解析 mmap 化（`-Xmx64m` 下整包分析不再 OOM）、更新包 SHA-256 强制校验、事务批处理精确回滚、Blutter 作业取消与分页。
- 仍可源码重建：`build-rizin*.ps1` / `build-lief*.ps1` / `build-unidbg-native.*` 与 `third_party` 子模块声明完整保留。
- Unidbg 依赖完整（`com.github.zhkl0228:unicorn:1.0.15` 未被裁掉），`BackendFactory` 不会因缺 `unicorn.*` 类回落到不可用后端。
- 安全默认值保持上游取向：`authEnabled` 默认开启、`IntegrityGuard` 与自产物防护启用、GitHub 更新检测可用。
- 许可链完整：`NOTICE`、SPDX 头、DEX 与三个 `.so` 内的 AGPL 正文均在。

## APK 工具链（新增）

| 工具 | 作用 | 依赖 | 需要 root |
|---|---|---|---|
| `jadx_decompile` | DEX/APK/JAR → Java（`action=save/class/list`） | jadx | 否 |
| `baksmali_decode` | DEX → smali 源码目录 | smali | 否 |
| `smali_assemble` | smali 目录 → DEX | smali | 否 |
| `apk_decode` | 清单/权限/资源清单/二进制 XML 解码 | ARSCLib | 否 |
| `apk_rebuild` | 完整回编、合并拆分包、去混淆重命名 | APKEditor | 否 |
| `apk_sign` | v1/v2/v3 签名，首次自动生成内置自签密钥 | apksig + BouncyCastle | 否 |
| `apk_sign_info` | 签名方案与证书指纹核对 | apksig | 否 |
| `dex_search` | 按字符串/方法特征反查被混淆的类与方法 | DexKit | 否 |
| `string_scan` | URL/邮箱/IP/JWT/AK-SK/私钥 扫描 | 内置 | 否 |
| `apk_diff` | 两版 APK 的清单/权限/SO/资源差异 | ZIP + ARSCLib | 否 |
| `apk_extract` | 列出并提取 APK 内条目 | 内置 | 否 |
| `dex_unpack` | root 内存 dump 绕过磁盘加固壳 | root shell | **是** |
| `frida_control` | 内置 frida-server 起停与进程枚举 | root shell | **是** |

典型闭环：

```text
apk_decode(path=...)                        # 先看清单/权限
jadx_decompile(path=..., action=save)       # DEX → Java 速读
baksmali_decode(path=classes.dex)           # 需要改字节码时 DEX → smali
smali_assemble(smaliDir=...)                # 改完 smali → 新 DEX
apk_rebuild(action=decode|build|merge, ...) # 替换 dex / 回编成 APK
apk_sign(inputApk=...)                      # 签名后才能安装
```

加固样本：`dex_unpack(action=pslist)` 找包名 → 先手动打开目标 App 让壳解密 dex 进内存 → `dex_unpack(action=dump, package=...)` → 把 dump 出的 dex 交给 `jadx_decompile` / `baksmali_decode`。三代指令抽取壳的方法体可能是 nop，本工具不做指令修复。

### 移植时补的三类防护

移植工具接受任意本地路径，因此必须继承上游对自产物的保护，否则 `jadx_decompile` / `apk_extract` 会变成导出本 App 自身 dex、资源与内置 `.so` 的口子：

1. **自产物防护**：所有带路径参数的移植工具在进入库调用前先过 `SelfArtifactGuard`，命中则返回 `SELF_ANALYSIS_FORBIDDEN`。
2. **输入体积闸门**：512 MiB 上限（`INPUT_TOO_LARGE`），jadx/DexKit 这类会把输入放大数倍建对象树的库靠它提前失败。
3. **堆闸门**：`jadx_decompile` / `dex_search` / `apk_rebuild` 调用前走 `MemoryGuard.ensureAnalysisMemory`，内存不足映射为 `INSUFFICIENT_MEMORY` 而不是 `OutOfMemoryError`。

### 可见性

上游默认 `leanTools=true`，只广告 `CORE`/`META` 与网关类。13 个新工具因此注册为 `CORE`：若注册为 `EXTRA`、又落在既无热度也无网关豁免的分类里，新客户端将永远看不到它们（衍生版本 XuanXingNieHe 正是踩了这个坑）。`ApkToolchainTest` 用测试钉死这一点。

## APK 位置

Release 构建输出目录：

```text
app/build/outputs/apk/release/
```

普通 arm64 手机优先安装：

```text
app/build/outputs/apk/release/app-arm64-v8a-release.apk
```

不确定 ABI 时安装：

```text
app/build/outputs/apk/release/app-universal-release.apk
```

## GitHub 更新

应用只通过 `bilieebiliee1-design/SOMCP` 的 GitHub 正式 Releases 检测更新。普通提交、分支、构建产物和未发布 tag 都不会被视为更新，draft 与 prerelease 也不会进入自动更新通道。

推荐 Release tag 使用 `v<versionName>`，并上传按 ABI 命名的 APK（`.github/workflows/release.yml` 在打包阶段把 `app-<abi>-release.apk` 统一重命名为下面的名字、顺带生成 `SHA256SUMS`，所以资产名与 tag 里的版本号始终一致）：

```text
SOMCP-1.0.22-arm64-v8a.apk
SOMCP-1.0.22-armeabi-v7a.apk
SOMCP-1.0.22-x86.apk
SOMCP-1.0.22-x86_64.apk
SOMCP-1.0.22-universal.apk
```

可同时上传同名 `<apk>.sha256` 或统一的 `SHA256SUMS`。检测器会优先选择当前设备 ABI；只要 release 存在校验资产，安装前就会强制验证 SHA-256，校验文件仅从 GitHub 官方域获取（与 APK 的第三方镜像下载严格分离），获取失败或不匹配都会直接中止安装。只有完全未发布校验资产的版本才允许以未验证状态安装。

v1.0.17 / v1.0.19 / v1.0.20 的资产是早年手工上传的 `SOMCP-main-<abi>.apk`，名字里没有版本号；更新器仍能按 ABI 子串选中并正常安装，只是人工核对资产与 tag 时会对不上。这几个 release 不再回溯改名（`SHA256SUMS` 里写的就是旧文件名，改名会让校验资产与文件对不上）。

Release 输出体积随原生后端更新变化，以 GitHub Release 资产页面为准。

## 核心能力

- Android 原生前台服务，不依赖 Python 运行环境。
- Compose + Material 3 界面。
- Ktor CIO MCP HTTP 服务，默认端口 `8000`。
- MCP token 访问控制（出厂默认开启），可绑定 `127.0.0.1` 或 `0.0.0.0`。
- 通过系统文件选择器授权工作目录。
- 支持独立 `.so` 与 APK 内 `lib/<abi>/*.so` 工作流。
- Rizin 原生后端：函数分析、CFG、xref、crypto 扫描、ESIL、字节搜索、反汇编、汇编。
- LIEF 原生后端：ELF 解析、section 内容读写、section header 重建、符号修改、地址 patch。
- 编辑会话：snapshot、rollback、undo、redo、check、audit、persist。
- 构建导出：自动改名/覆盖、patch report、多输出变体、镜像到工作目录。
- Cloudflare Tunnel：quick/named 隧道、keepalive、状态统计。
- 可选 Unidbg：`emulate_call`、`emulate_dump`。**注意**：Unidbg 的 Android 原生库（`libcapstone.so` / `libkeystone.so` / `libunicorn.so` / `libjnidispatch.so`）不随 Debug APK 内置，需自行交叉编译 unidbg 上游 native 并放入 `app/src/main/jniLibs/<abi>/` 后重新构建，或在设备上额外安装；其中 `libunicorn.so` 必须是 unidbg unicorn2 的 JNI 桥（导出 `Java_com_github_unidbg_arm_backend_unicorn_Unicorn_*`），只编译原始 unicorn 引擎不行，可用 `tools/verify_unicorn_jni.py` 校验。**官方 Release APK（v1.0.18+）已内置 Unidbg 原生库，但只有 64 位 ABI 完整可用**：arm64-v8a / x86_64 四个库齐全，装完就能用 `emulate_call` / `emulate_dump`；armeabi-v7a / x86 只有 `libcapstone.so` / `libkeystone.so` / `libjnidispatch.so`，**不含 `libunicorn.so`**（unicorn/QEMU 需要 `__uint128_t`，Android NDK 的 32 位工具链不提供，`app/build.gradle.kts` 在打包阶段就对 32 位 ABI 剔除该库），所以 `emulate_*` 在 32 位设备上不可用，要用模拟执行得装 64 位 ABI 的包。库缺失时 `system_control(action=status)` 的 `emulation.setup` 会明确标注 `requires-extra-install`，`emulate_*` 调用返回 `EMULATOR_UNAVAILABLE` 并说明缺失原因，不会伪装成可用。
- 完全离线 Flutter AOT 分析：内置 Flutter 3.44.2–3.44.7 / Dart 3.12.2 arm64 Blutter Runner；其他版本返回明确的不支持信息。
- JNI 动态注册静态还原（`jni_api`）：扫描 `JNINativeMethod` 表，把 Java 方法名/签名映射到真实原生地址，并解析回符号。
- 加固/壳静态指纹识别（`packer_api`）：厂商标记、逐段熵、入口点异常、段名异常、反分析字符串，输出带证据的排序判定。
- 控制流混淆检测（`obfusc_api`）：基于真实 basic block 图的 dispatcher 识别、扁平化因子、bogus edge 度量，替代高误报的符号名启发式；`action=deflatten` 进一步重建候选真实边。
- 加密字符串辅助解密（`strdecrypt_api`）：定位密文块、全量扫描 256 个单字节 XOR/add/sub/rol/ror 密钥并按可打印率排序，支持传入多字节 XOR / RC4 密钥试解。
- 反调试/反 hook 扫描（`antidebug_api`）：按 import / string / code_pattern 三级证据分类，输出风险等级。
- 导入符号溯源（`import_api`）：经 `.gnu.version_r` 把导入符号精确归属到源库，标注 Bionic 库用途。
- Cloudflare 永久隧道支持配置要展示的 HTTPS 公网地址；认证失败会停止重连并提示更新 token。
- APK 内 SO 使用流式扫描与按需提取，分析页可一键释放工作区、索引缓存和已结束的 Blutter 数据。
- 精简工具列表：默认暴露核心 + 网关（`lowlevel` 与 `agent`）+ meta 工具，完整能力通过 `meta_info(action=describe)` 发现。网关类不参与热度晋级——它们是入口而非证据调用，藏起来等于让对应能力不可达。
- **APK 全链路（本版新增）**：jadx DEX→Java、baksmali/smali 往返、ARSCLib 清单与资源解包、APKEditor 回编与合并拆分包、apksig v1/v2/v3 签名与证书核对、DexKit 反混淆查找、字符串/密钥扫描、两版 APK 差异、条目提取；root 侧提供内存 DEX 脱壳与内置 frida-server 起停。详见上文「APK 工具链」。

## MCP 工具体系

当前共 60 个内置工具（上游 47 + 本版新增 13），准确数量以 `tools/list` 返回的 `_meta.fullToolCount` 为准；默认 lean 模式会广告核心、底层网关和 meta 工具，降低 LLM 初始化上下文成本。

推荐工作流：

```text
so_open(action=list)
so_open(path=...)
analyze_elf(view=stats)
analyze_functions
read_disasm(locator=...)
session_open
edit_asm(dryRun=true)
session_history(action=check)
build_so
```

搜索语法：

```text
search_bytes(pattern="5F2403D5")          # Rizin 原生 byte pattern，紧凑 hex
search_bytes(pattern="5F24..D5")          # Rizin nibble 通配符，. 表示任意半字节
search_bytes(pattern="5F2403D5:FFFF00FF") # Rizin bytes:mask
search_bytes(pattern="5F 24 ?? D5")       # MCP 兼容写法，会规范化为 5F24..D5 后交给 Rizin
search_strings(prefix="JNI.*Load", regex=true)
```

AI 调用注意事项：

- `so_open`、`search_bytes`、`analyze_*`、`read_disasm` 等重型工具受全局并发闸门保护；自动化客户端应串行调用，遇到 `SERVER_BUSY` 按 `retryAfterMillis` 重试同一请求。
- `search_bytes` 优先使用 Rizin 搜索；返回的 hit 会包含 `addr`、`hexAddr`、`fileOffset`、`section`、`sectionOffset`，可直接衔接 `read_hexdump` 或 patch 工具。
- `search_strings(regex=true)` 使用 Kotlin 正则；非法正则会返回结构化错误，不会被当作空结果。
- 电脑侧直接调用手机 MCP 时可先执行 `adb forward tcp:8000 tcp:8000`，然后使用 `http://127.0.0.1:8000/mcp`。

工具发现：

```text
meta_info(action=help)
meta_info(action=tools)
meta_info(action=describe, tools=["edit_asm", "build_so"])
meta_info(action=workflows)
meta_info(action=suggest, workspaceId="...")
meta_info(action=errors)
meta_info(action=report, workspaceId="...", writeToFile=true)
meta_info(action=capabilities)
meta_info(action=health)
```

`meta_info(action=report)` 会生成完整分析报告，包含 ELF 统计、节区、动态符号、字符串、Rizin 函数、crypto 扫描和建议，并可落盘到 app 私有 reports 目录。

底层 API 通过 capability registry + 4 个聚合网关暴露，避免 MCP 工具数量爆炸。`meta_info(action=capabilities)` 是真实能力面来源，会明确列出 supported / partial / missing，避免把未实现的底层 API 误报为已覆盖：

```text
rizin_api(action=capabilities|command|analyze|functions|cfg|xrefs|search_bytes|crypto|esil|diff|asm|disasm|decompile)
lief_api(action=capabilities|parse|list|patch_address|add_export|remove_symbol|build|fix_sections|report)
unidbg_api(action=capabilities|status|call|dump)
xanso_api(action=capabilities|status|fix_sections)
flutter_blutter(action=inspect|analyze|status|result|cancel|packages|prune)
dynamic_api(action=capabilities|dispatch|status|analyze)
dynamic_analyze_ai(evidence=...|function=...|request=...)
jni_api(action=capabilities|scan|resolve|method_table, query=...)
packer_api(action=capabilities|scan|fingerprint)
obfusc_api(action=capabilities|scan|detect|deflatten|deflatten_capabilities, limit=40, locator=...)
strdecrypt_api(action=capabilities|scan|decrypt|hints, locator=..., length=32, keyHex=...)
antidebug_api(action=capabilities|scan|detect)
import_api(action=capabilities|trace|scan)
agent_api(action=roles|run|capabilities, role=..., task=..., workspaceId=...)
```

`jni_api` 静态还原 JNI 动态注册：扫描只读数据段里的 `JNINativeMethod` 三指针表（name / signature / fnPtr），把「Java 方法名 → 真实原生地址」直接映射出来，并解析 fnPtr 回 `.symtab` / `.dynsym` 符号。Android 上多数 native 方法不导出 `Java_*` 符号，而是在 `JNI_OnLoad` 里用 `RegisterNatives` 动态注册，因此普通符号表看不到这层映射。`packer_api` 做加固/壳静态指纹：厂商标记串（360 / 梆梆 / 爱加密 / 阿里 / 腾讯乐固 / UPX）、逐段 Shannon 熵、`e_entry` 落在 `.text` 之外的壳 stub 特征、非标准/空段名、反分析字符串。两者都是纯字节检查，不执行目标文件，对不可信输入安全；结论带证据与置信度，不会把「混淆构建」直接断言成「商业加固」。

`obfusc_api` 从**真实 basic block 图**判定控制流混淆，而不是靠符号名猜：识别 dispatcher 块（高 fan-in + 薄块体，即只做比较跳转的 switch）、算扁平化因子、量 bogus edge 候选比例，逐函数给 `flattened` / `suspected` / `clean` 与置信度。它是 `analyze_elf` 里 `hasOllvm` 标签的结构化替代——后者匹配的是 `.cold.`、`__clang_call_terminate` 这类**标准 clang 产物**，在正常 NDK Release 上也会命中。

`action=deflatten` 在此基础上重建去扁平化后的候选控制流：dispatcher 集合与检测器**共用同一判据和阈值**（避免两个 pass 对同一函数给出不同 dispatcher），另外重建被检测器丢弃的前驱表，并区分 trampoline（薄块且所有后继都是 dispatcher）。它输出的是**候选真实边**而非「已还原的 CFG`：Rizin 每个基本块只给 `addr/size/ninstr/jump/fail`，编码真实后继的**状态变量和 case 表并不在输入里**，所以任何单一「还原边」都是猜测。该 action 因此固定返回 `verdict=candidates_only` 与 `confidence=low`，并在 `limitations` 里写明下一步（用 `read_disasm` 读每个源块的状态赋值，或用 `emulate_call` 观测真实路径）——与检测器「宁可承认不知道，也不误判」的标准一致。

`strdecrypt_api` 处理编译期字符串加密（OLLVM `struc`/`encloud-cc`、商业加固壳、手写变体把密文留在 `.rodata`、运行时经桩函数解密）。它只声称做两件事里的一半：**从字节反推变换**。`action=scan` 在已分配非可执行段里定位既非明文文本也非零填充的字节段作为密文候选；`action=decrypt` 把 256 个单字节 XOR / add / sub / rol / ror **全量**扫一遍并按可打印 ASCII 占比排序，多字节 XOR 与 RC4 则只试调用方给的密钥（密钥空间无界，无法枚举）。判定门槛是可打印率 ≥ 0.75 且该比例**始终回传**，因为随机数据撞出可打印输出的概率不低，把未经验证的猜测当作「已还原字符串」比不报更糟。**不覆盖** AES/DES/ChaCha 与运行时派生密钥，`action=hints` 给出手工路线（找调用方 → 读解密循环 → 定位 RC4 的 256 字节 KSA / AES 的多轮循环），以及更可靠的动态路线（`dynamic_api` / `unidbg_api` 直接从内存读明文）。

`antidebug_api` 按证据强度分级扫描反调试/反 hook：`import`（加载器必须解析的符号，近乎确证）、`string`（`/proc/self/*` 等字面量，弱）、`code_pattern`（x86-64 inline detour 跳板序列）。`import_api` 解析 `.gnu.version_r`（DT_VERNEED）把每个导入符号**精确归属**到源库，并标注 Android Bionic 各库用途；缺版本表时降级为 `DT_NEEDED` 启发式，并在 `evidence` 字段明说「库名不是来源证明」。这三项同样不执行目标文件。

## 动态分析（手动加载到内存 → AI 分析）

SOMCP 提供一套**独立**的动态分析工具流程，与静态 deep-analysis 循环彼此分离，把「把 `.so` 手动加载到内存 → 执行 → 采集证据 → 交给 AI 分析」串成一条可被 MCP 调用的链路。核心入口是两个工具：

```text
dynamic_api(action=capabilities|dispatch|status|analyze, op=unidbg_session_open|unidbg_session_call|unidbg_session_registers|frida_hook|frida_call|analyze, backend=unidbg|frida, targetFunction=...)
dynamic_analyze_ai(function=... | evidence=...)
```

两个后端共用同一套编排层 `dynamicDispatch`：

- **unidbg（模拟）**：`unidbg_session_open` 把选中的 `.so` 映射进模拟内存，`unidbg_session_call` 解析指定导出函数并执行，随后采集寄存器和目标地址的内存 dump。无需真机，适合快速验证目标函数行为。
- **frida（真机 hook）**：`FridaBridge` 以纯 Kotlin 实现 `frida-server`/`frida-gadget` 的 TCP 协议客户端，`frida_hook` 通过 JS agent（Interceptor/Module）挂钩目标函数捕获入参与返回，`frida_call` 触发调用并回读 hook 结果。**注意**：`frida-server`/`frida-gadget` 不随 APK 内置，需在已 root 设备上单独安装并在推开时先对目标进程 `ptrace`/注入；未就绪时 `dynamic_api(action=capabilities)` 的 `status.fridaAvailable` 会如实标注 `false`，不会伪装成可用。

`dynamic_analyze_ai` 复用 `RikkaAgentEngine`，把 `dynamicRun` 证据（寄存器现场、内存 dump、hook 记录、调用返回）喂给 AI 生成结构化动态分析报告：

```text
dynamic_api(action=capabilities)        # 查看 backends.dynamic，确认 unidbg/frida 可用性
dynamic_api(action=dispatch, backend=unidbg, op=unidbg_session_open, targetFunction="JNI_OnLoad")
dynamic_api(action=dispatch, backend=unidbg, op=unidbg_session_call, targetFunction="JNI_OnLoad")
dynamic_analyze_ai(function="JNI_OnLoad")  # 或直接传入上一步 evidence
```

`meta_info(action=capabilities)` 的 `backends.dynamic` 是动态分析的能力真实面来源，明确列出 unidbg/frida 的支持状态与限制。

`rizin_api(action=command)` 提供受控 Rizin raw command 通道，用于覆盖大量 Rizin 命令式底层能力；为安全起见，写入、文件、shell 等危险命令会被阻止。

批量流水线支持事务模式：

```text
meta_info(action=batch, transactional=true, steps=[...])
```

事务模式会在执行前为涉及 `workspaceId + editSessionId` 的编辑会话自动创建 snapshot；后续步骤失败时会自动 rollback 到事务前快照。

分页继续：

```text
meta_info(action=continue, cursor="page:...")
```

系统状态：

```text
system_control(action=status)
```

`health/status` 会返回运行时信息，包括 APK 版本、设备 ABI、nativeLibraryDir、`librz_native.so` 大小、mtime 和 SHA-256 短指纹，用于确认设备上实际运行的是不是最新 APK。

## 子代理（scoped sub-agents）

前提是设置 → AI 里已填好 **API 地址、API Key、模型 ID**——子代理不另设一套凭据，全部复用这三个值（以及 provider、temperature、自定义 headers/body）。

一个子代理 = 一次**独立上下文**的取证运行：它只拿到自己角色白名单内的工具、只有一个被指派的问题、有自己的迭代上限，返回紧凑证据块而不是整体报告。两侧入口共用同一个 `SubAgentRunner`：

- **手机内置 agent**：AI 深度分析与动态分析的工具表里多出 `spawn_subagent(role, task)`，主模型自己决定何时派人手；子代理的工具结果不再回灌到主上下文，只回传结论，这正是省 token 的地方。
- **电脑侧 MCP 客户端**：`agent_api` 网关。它属于网关类，默认 lean 模式就会出现在 `tools/list` 里（早期版本把它按普通 EXTRA 处理，导致客户端根本看不到子代理入口）。

```text
agent_api(action=roles)                  # 角色、每个角色的工具白名单、当前上限与是否已配置
agent_api(action=run, role="xref_tracer", task="libfoo.so 里 JNI_OnLoad 注册了哪些方法，逐个给地址", workspaceId="ws_...")
agent_api(action=capabilities)           # supported / partial / not_covered，按实说
```

内置 5 个角色（白名单只读取证类工具，永不包含 `edit_*` / `build_so` / `session_*` / `app_config`）：

| 角色 | 干什么 | 迭代建议 |
| --- | --- | --- |
| `xref_tracer` | 调用链与交叉引用，含 `RegisterNatives` 动态注册入口 | 14 |
| `crypto_locator` | 算法常量 / S-box / 密钥调度定位与调用点 | 14 |
| `packer_profiler` | 壳、控制流混淆、反调试、导入来源画像 | 10 |
| `dyn_probe` | unidbg 模拟或 Frida 真机跑目标函数采证 | 12 |
| `generalist` | 上述都不匹配时的通用静态取证 | 12 |

`workspaceId` 由调用方继承：子代理拿得到同一个工作区，不会重复 `so_open` 再解析一遍目标。角色白名单里若出现目录里不存在的工具名，`action=roles` 会同时列 `declared_tools` 与 `unresolved_tools`——漂移可见，不会静默缩水。

三道闸门（设置 → AI → 子代理，`agent_api(action=roles)` 的 `limits` 原样回报）：

- **嵌套深度上限**（默认 1，最大 2）：=1 时子代理不再拿到 `spawn_subagent`，杜绝自我复制。
- **单次会话子代理配额**（默认 4）：**这一项才是约束 API 花销的**；用完时 `spawn_subagent` 回 `sub_agent_budget_exhausted`，主模型继续自己做完，而不是整轮失败。
- **单子代理迭代上限**（默认 12）与**并发子代理数**（默认 2）：迭代数按 `min(角色建议, 该上限)` 生效；并发是全进程槽位池，改设置只影响之后的派生。

失败与边界的处理是明确的：子代理抛错回 `sub_agent_failed`（父代理改用直接工具），槽位被占满等 5 分钟后回 `sub_agent_queue_timeout`，跑满迭代上限时**保留已取到的部分证据**并标注在何处停止。子代理之间、子代理与父代理之间不共享对话历史，也没有跨会话记忆。

## 设置项

- MCP 访问控制：token、绑定地址、访问 URL。
- 返回数量：默认 limit、字符串 limit、反汇编窗口、hexdump、请求体上限。
- 导出：冲突策略、patch report、镜像到工作目录、patch 字节上限、构建产物列表上限。
- 编辑校验与审计：自动快照、审计日志、审计持久化、最大快照数、最大审计数、diff 范围数。
- 工具暴露：lean tools、自适应 lean、禁用工具列表、工具结果字符上限、工具调用频率限制。
- 性能保护：重型工具并发上限、请求超时、工具统计持久化。
- 原生执行：Unidbg 模拟执行开关。
- AI 深度分析：provider、API 地址、API Key、模型 ID（可从 `action=roles` 旁的端点拉取模型列表）、temperature、最大迭代、历史压缩阈值、自定义 headers/body、系统提示词。
- 子代理：启用开关、嵌套深度上限、单次会话派生配额、单子代理迭代上限、并发子代理数（详见「子代理」一节；凭据复用上面的 AI 配置，不单独设置）。
- Blutter：查看内置 Flutter 3.44 / Dart 3.12.2 Runner、离线执行方式和精确兼容性规则。
- Cloudflare Tunnel：quick/named、目标端口、协议、IP 版本、日志等级、keepalive、重连退避。
- APK MCP 桥接：APK MCP URL、自动探测、工具合并、转发超时。

## 从零构建

准备环境：

- Windows 10/11
- JDK 17
- Android SDK 36
- Android NDK `29.0.14206865`
- CMake `3.22.1`
- Gradle `9.6.1`

获取 Rizin 源码（Rizin 反汇编/分析后端，非 git 子模块，需手动 clone 一次）：

```powershell
git clone https://github.com/rizinorg/rizin.git third_party/rizin-src
```

构建 Rizin / LIEF 原生依赖：

```powershell
.\build-rizin-all.ps1
.\build-lief-all.ps1
```

构建脚本不依赖任何固定机器路径：工具链通过环境变量解析（`ANDROID_NDK_HOME` / `ANDROID_HOME` 定位 NDK，`MINGW_HOME` / PATH 定位 MinGW 宿主编译器，`ANDROID_HOME` 定位 CMake/Ninja），也支持显式参数覆盖。例如：

```powershell
# Rizin：指定源码树 / NDK / 宿主 MinGW（任一均可省略，缺省自动探测）
.\build-rizin.ps1 -Abi arm64-v8a -RizinSrc D:\rizin-src -NdkRoot D:\Android\Sdk\ndk\29.0.14206865 -HostTools D:\mingw64
.\build-rizin-all.ps1 -RizinSrc D:\rizin-src   # 一次构建全部 4 个 ABI（含 arm64-v8a）
# LIEF：指定 CMake / Ninja / NDK
.\build-lief.ps1 -Abi arm64-v8a -NdkRoot D:\Android\Sdk\ndk\29.0.14206865
```

**关于 Rizin 的 host 编译器（build machine）**：Rizin 构建时需要用它生成少量 host 工具，Windows 下官方支持 MSVC，本构建脚本默认使用 MinGW-w64 作为 host（自动探测 `MINGW_HOME` / PATH，`-HostTools` 可覆盖），并会在 meson setup 前**自动对 Rizin 源码树应用幂等补丁**（`librz/util/` 下的 `file.c` / `time.c` / `sys.c` / `subprocess.c`：放宽 `_MSC_VER` 条件使其同时覆盖 MinGW 的 `__WINDOWS__`，并为 MSVC secure-CRT 的 `_wdupenv_s` / `_wputenv_s` / `_chsize_s` / `_snwprintf_s` 插入 MinGW shim）。这些补丁只改本地的 Rizin checkout，已打过则跳过，用 `-SkipPatch` 可关闭。若改用 MSVC host（需自行准备 VsDevCmd 环境并把 `rizin-native.ini` 指向 `cl.exe`），则无需这些补丁。

（可选）构建 Unidbg 模拟后端所需的 Android 原生库（capstone/keystone/unicorn，默认不内置，详见“核心能力”中 Unidbg 说明）：

```powershell
git submodule update --init --recursive
.\build-unidbg-native.ps1
```

构建 release：

```powershell
D:\Gradle\gradle-9.6.1\bin\gradle.bat :app:assembleRelease
```

验证原生库是否还有会导致 `dlopen` 失败的非系统未解析符号：

```powershell
.\verify-native-libs.ps1
```

如果输出全部为 `OK`，说明 Rizin/ZSTD/LIEF 等非系统符号已经在 APK 自带库内解析完毕。

## 安装与验证

建议先卸载旧包再安装，避免 Android 继续加载旧 native lib：

```powershell
adb uninstall com.soreverse.mcp
adb install -r app\build\outputs\apk\release\app-arm64-v8a-release.apk
```

启动 MCP 后调用：

```text
meta_info(action=health)
```

预期：

```json
{
  "nativeBackends": {
    "rizin": { "available": true, "loadStatus": "loaded" },
    "lief": { "available": true, "loadStatus": "loaded" }
  }
}
```

如果仍然报 `dlopen failed: cannot locate symbol ...`，请检查 `runtime.librzNative.sha256_16` 是否等于你本地最新 APK 中的 `librz_native.so` 指纹。

## Release 优化

Release 构建启用：

- R8 代码压缩和混淆。
- Android 资源压缩。
- ABI 分包 + universal 包。
- v1/v2/v3/v4 APK 签名。
- 严格 native 链接：禁止 Rizin/LIEF/zstd 未解析符号混入 `.so`。
- JNI keep 规则：保留 `RizinNativeEngine` / `LiefEngine` 的类名和 native 方法名。

## 注意事项

- 本项目采用 AGPL-3.0 许可证。任何再分发（含修改、改名、二次打包版本）必须：
  - 保留版权与许可声明；
  - 继续以 AGPL-3.0 授权；
  - 向每一位接收者提供完整对应源代码；
  - 标注所做修改。
  - 闭源分发、抹除署名或改名冒充原创即构成侵权，依 AGPL-3.0 第 8 条授权自动终止，SOMCP 项目组将依法提起诉讼以维护自身权益。
- 本工具只适合分析自己有权处理的文件。第三方二进制的逆向、修改和分发可能受法律、协议或平台规则限制。
- 汇编补丁更适合等长覆盖或明确边界内 patch，不会自动搬移后续代码。
- 手机无法单独证明自己从公网可访问；公网可达性需要远端客户端、Cloudflare Tunnel 或其他外部探测配合。
- APK MCP 桥接依赖 MT 管理器侧边栏 APK MCP 功能在线，并需要 MT 管理器保持后台运行。
