# 简体中文输入更新 / Simplified Chinese input update

## 中文

本文供 Traditional T9 上游贡献、代码审查和后续版本说明使用。本次更新面向有实体数字键和方向键的 Android 翻盖机、按键机，正常使用保留候选区即可，不要求显示屏幕数字键盘。

### 输入与选词

- 修复简体中文无法切换、关闭全局英文预测后无法输入中文，以及输入不完整拼音时过早提交候选的问题。
- 增加紧凑多行候选，保留自然词宽和竖线分隔。下键展开；首行再短按上键收回单行；返回键也可收回。
- 默认展开行与普通单行候选条等高。“宽松候选区”默认关闭，开启后仅适度增加行高和左右留白；不再提供旧的“紧凑”开关。快速反复展开、收起会复用原始单行尺寸，避免行高累积变化。
- 展开候选和拼音选择支持长按方向键滚动。修复快速连按导致的分隔错位、松开后多走一步，以及选择拼音后无法正常展开的问题。
- 普通单行候选也支持长按左右键连续选择，中英文共用主包中的“候选条滚动速度”（慢速／标准／快速）。中文展开面板单独提供滚动速度设置。长按单行候选到达首尾后停止，短按保留原有循环切换。
- 展开候选每行独立重复标注 1–9，数字选择当前高亮框所在行的词。每行最多九项；不存在的数字不会选到其他行。数字直选可在中文设置中关闭。
- 一次上键选择拼音，两次上键编辑拼音。左右键移动光标，数字键插入，退格删除光标前一位；支持的编辑器使用系统组词光标，拒绝组词选区的编辑器保留可见的兼容光标。下键或确认返回汉字候选，返回键取消本轮修改。
- 修复编辑后重新选拼音时列表被旧读音筛选锁住的问题，重新打开会恢复整串按键对应的拼音候选；正常的 `XIAN` 不再与编辑预览混淆。快速连按两次上键会等当前拼音列表显示后再编辑，不会用旧候选截短输入。插入或删除后立即确认会等待读音解析；无匹配或超时后仍可修改，并可按返回恢复本轮编辑前的输入。
- 重新整理默认词频，保留全部 482,284 组词语与读音，修正部分多音字的默认读音，并去除候选栏里的重复汉字。
- 不完整输入也能进入拼音编辑：例如 `745` 的拼音候选显示 `RI'LE` 时，编辑的是实际输入的 `RIL`。未修改就返回会保留原读音边界；修改后也保留解析到的音节分隔，避免把 `XI'AN` 误当作 `XIAN`。
- 增加离线汉字前缀补全，例如输入“你”的读音时可以找到“你好”“你好吗”。常用的精确单字排在补全词组前，次级单字排在后；已学习的首选候选和个人词条保留优先级。

### 中文设置与个人词库

- 按功能归类中文设置：**语言 → 中文输入** 管理中文包、用户词与读音、数字直选、前缀补全、模糊音和生僻字过滤；**外观 → 中文显示** 只保留候选区呈现选项。
- 全局 **界面语言** 位于主应用的 **外观** 中，始终提供跟随系统和英文；安装中文翻译分包后才提供中文。安装或启用中文输入不会自动切换界面语言。
- 增加中文用户词与读音管理：添加、搜索、修改和删除单个读音，并接入现有导入／导出流程。同一个词可保存多个读音，手动选择的音节边界会被保留。
- UTF-8 个人词导入格式为 `词语<TAB>462106<TAB>拼音`，例如 `重庆<TAB>462106<TAB>chong qing`。其他语言保留原有两列格式。导入中文词条需要已安装中文代码扩展。
- 个人词导入／导出统一使用 **语言 → 自定义字词** 的原有入口，中文词语管理页专注于添加、搜索、修改和删除读音，不再重复放置同一组导入／导出按钮。
- 增加六组可独立开启的模糊音：n/l、z/zh、c/ch、s/sh、en/eng、in/ing，默认关闭。
- 增加可选生僻字过滤，依据 2013 年《通用规范汉字表》的 3,500 个一级字，默认关闭；明确保存的用户词仍可显示。
- 删除中文字典会清除中文设置和私有包数据，并请求 Android 移除功能和翻译分包。个人词与读音保留，重新导入后可继续使用。若界面语言选了中文，会回到跟随系统；英文偏好保留。若取消系统移除确认，代码分包仍在，但中文配置已清除。中文仍启用时，语言设置中的同一个入口可继续用于重新导入中文包。仅取消勾选语言会保留数据。
- 保留全局星号／井号互换选项，方便适配不同实体键盘布局。
- 增加默认关闭、可分别启用的七类领域词包：游戏／ACG、医学、金融、心理、生物化学、工程、计算机与技术。共 2,980 条分类记录，合并为 2,961 组词语／读音；其中 103 组是主词库尚未收录的原创词条，其余用于领域排序。保留首选、常用单字和用户词的优先级；停用后恢复普通排序，领域学习独立保存在本机。[词源、授权与维护说明](chinese-domain-vocabulary.md)。

### 包体与上游发布集成

中文专用转换增强、拼音编辑、音节规则、模糊音、前缀索引、领域词库及其学习、用户词管理、展开候选面板的布局与导航、中文设置位于可选 `chinese` 功能模块。主 APK 保留共用键盘、普通候选条、存储和安装接口；中文界面翻译由资源分包提供。未安装中文包的用户无需安装这些中文功能代码和索引，也不会看到展开面板的设置。Full 保留其他语言的内置词典和离线加载，同时允许联网下载可选中文包；也保留离线文件导入。

此实现使用 Android 签名分包和系统安装确认。包任务会生成字典、数据、功能 APK 与翻译 APK 的组合 ZIP。代码分包必须与主 APK 的包名、版本和签名匹配，翻译分包的资源 ID 必须来自同一次主 APK 构建。构建和本地导入方法见 [中文扩展开发说明](chinese-extension.md)。

**软件内下载已接入配套组合包：语言 → 中文输入 → 下载中文包。** 常规词典加载入口在缺少配套中文功能时也使用同一条下载链路。下载一次即包含词库、离线数据、功能和界面分包，随后进入 Android 安装确认。下载按主应用包名、版本、类型与签名匹配，并核对当前主 APK 和分包的文件哈希；下载失败、包不匹配或损坏时保留原数据。发布流程会一起构建、验证主 APK 和配套中文包，支持先预演、再明确发布；维护者配置发布签名后即可按 [发布说明](chinese-release.md) 操作，无需另外设计下载机制。当前 PR 本身不代表已发布官方新版。应用商店分发仍需相应的交付集成。

### 当前范围

联想目前是本轮输入中的离线前缀补全。提交后无需按键的下一词预测、整句语言模型和逐段确认仍是后续功能；已支持音节分隔和读音修改。不同厂商的实体按键扫描、编辑器和安装限制仍需设备验证，问题报告应包含型号、Android 版本及复现按键顺序。

## English

This document describes a contribution to upstream Traditional T9 and can be used for code review and future release notes. The changes target Android flip phones and feature phones with physical number keys and a D-pad. Normal keypad use needs only the candidate area; an on-screen number pad is unnecessary.

### Input and candidate selection

- Fixes Simplified Chinese activation, Chinese input when global alphabetic prediction is disabled, and premature commits while typing incomplete Pinyin.
- Adds compact, naturally sized multirow candidates with vertical separators. Down expands; a short Up on the first row or Back returns to the single row.
- Expanded rows default to the ordinary candidate row's measured height. A default-off Roomy candidates option modestly increases height and side spacing, replacing the old Compact switch. Rapid collapse/reopen retains the original single-row baseline rather than compounding expanded sizes.
- Supports held-arrow scrolling in expanded candidates and Pinyin choices. Fixes separator corruption during rapid navigation, extra movement on release, and expansion after selecting a reading.
- Adds held Left/Right navigation to ordinary single-row candidates in all languages. The host Keypad screen offers Slow/Normal/Fast speed; Chinese expanded candidates have an independent speed setting in the optional feature. Held single-row navigation stops at either end; short taps retain cyclic selection.
- Repeats numeric labels 1–9 on every expanded row. Number keys select within the highlighted candidate's row. Rows contain at most nine candidates; absent positions never select from another row. This selection mode can be disabled in Chinese settings.
- One Up selects a reading; a second Up edits Pinyin. Left/Right move the caret, number keys insert, and Backspace deletes before it. Supported editors use their actual system composing caret; editors that reject composing selection retain a visible compatibility cursor. Down/confirm returns to Han candidates; Back cancels the current edit.
- Restores the full spelling list when reopening it after an edit, removes the collision between an ordinary `XIAN` candidate and its old editing preview, and queues rapid Up/Up until the current spelling list is shown so stale candidates cannot truncate the input. Confirmation immediately after insertion/deletion waits for reading resolution. A no-match or timeout leaves the text editable, and Back restores the input from before the edit.
- Revises default frequencies while retaining all 482,284 word/reading pairs, corrects several polyphonic defaults, and deduplicates displayed Han candidates.
- Allows editing an incomplete reading: when `745` shows `RI'LE`, the editor opens only the typed `RIL`. Returning unchanged retains the selected syllable boundaries; edited readings also retain their resolved separators so `XI'AN` cannot silently become `XIAN`.
- Adds offline Han-prefix completion, including 你好 and 你好吗 while composing the reading for 你. Frequent exact single characters precede completions; secondary characters follow them. Learned leading candidates and explicit custom words retain priority.

### Chinese settings and personal words

- Classifies Chinese settings by function: **Languages → Chinese Input** contains pack management, personal words/readings, numeric candidate selection, prefix completion, fuzzy matching and the rare-character filter. **Appearance → Chinese Appearance** contains candidate presentation only.
- Keeps one global **Interface Language** selector in the host **Appearance** screen. System and English are always available; Chinese appears only while its translation split is installed. Installing or enabling Chinese input does not automatically change this preference.
- Adds Chinese word/reading management with add, search, edit, per-reading deletion, and existing import/export integration. A word can have multiple readings, and explicitly chosen syllable boundaries survive round trips.
- UTF-8 custom-word records use `word<TAB>462106<TAB>reading`, for example `重庆<TAB>462106<TAB>chong qing`. Other languages retain their two-column format. Importing Chinese entries requires the Chinese code extension.
- Uses the existing **Languages → Added Words** import/export actions once. The Chinese word manager focuses on add, search, edit and per-reading deletion without duplicating those buttons.
- Adds six independently optional fuzzy rules: n/l, z/zh, c/ch, s/sh, en/eng, and in/ing. All are off by default.
- Adds an optional rare-character filter based on the 3,500 level-one characters in the 2013 national Table of General Standard Chinese Characters. It is off by default and preserves explicitly saved user words.
- Removing the Chinese dictionary clears its profile and private pack data and asks Android to remove both feature and translation splits. Personal words/readings survive and return after reimport. A selected Chinese UI language returns to System; an English preference is retained. Declining Android's removal confirmation leaves the code splits installed while the Chinese profile is cleared. If Chinese remains enabled, the same Languages entry remains available to reimport the pack. Merely disabling the language preserves its data.
- Retains the global star/pound swap setting for different physical keypad layouts.
- Adds seven independently enabled domain vocabularies, all off by default: games/ACG, medicine, finance, psychology, biochemistry, engineering, and computing/technology. Their 2,980 category rows merge into 2,961 word/reading pairs, including 103 new original pairs; existing factory pairs provide opt-in domain priority. Leading candidates, frequent single characters and personal words retain priority. Disabling restores ordinary ranking; domain learning is local and separate. See [sources, licenses and maintenance](chinese-domain-vocabulary.md).

### Package size and upstream publishing

Chinese-specific conversion enhancements, Pinyin editing, syllable rules, fuzzy matching, the prefix index, domain data and learning, word management, expanded-panel layout/navigation, and Chinese settings live in the optional `chinese` feature module. The base retains shared keyboard, ordinary candidate rows, storage, and installation interfaces. Chinese UI translations ship in a resource split. Users without the Chinese pack do not install this feature code or its indexes and do not see expanded-panel settings. Full retains bundled/offline dictionaries for other languages while allowing the optional Chinese download; matching local ZIP import remains available offline.

The implementation uses Android-signed splits and the system installation prompt. Its packaging task produces a combined ZIP containing the dictionary, data, feature APK, and translation APK. Code splits must match the base package name, version, and signing certificate. Translation resource IDs must come from that exact base build. See the [Chinese extension development notes](chinese-extension.md) for building and local import.

**In-app delivery uses Languages → Chinese Input → Download Chinese pack.** Ordinary Chinese dictionary loading uses the same combined download when its matching feature is missing. One download contains the dictionary, offline data, feature and translation splits, then continues to Android installation consent. Resolution includes package, version, flavor/build type and signer; metadata additionally verifies the installed base APK and split hashes before changing data. Failed, incompatible or damaged downloads preserve the preceding dictionary and pack. The release workflow builds/verifies matching base APKs and packs together, supports a dry run and requires explicit publishing. Maintainers configure their release signing credentials and follow the [release guide](chinese-release.md); no separate download mechanism remains to design. This PR itself does not publish an official release. Store distributions still need their corresponding delivery integration.

### Current scope

Completion operates on the current composition using an offline prefix index. Zero-key prediction after commit, a sentence language model, and incremental segment confirmation remain future work. Syllable boundaries and reading editing are supported. Vendor key scanning, editor behavior, and installation restrictions still require device validation; reports should include the phone model, Android version, and exact key sequence.
