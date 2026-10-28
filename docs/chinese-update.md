# 简体中文输入更新 / Simplified Chinese input update

## 中文

本文供 Traditional T9 上游贡献、代码审查和后续版本说明使用。本次更新面向有实体数字键和方向键的 Android 翻盖机、按键机，正常使用保留候选区即可，不要求显示屏幕数字键盘。

### 输入与选词

- 修复简体中文无法切换、关闭全局英文预测后无法输入中文，以及输入不完整拼音时过早提交候选的问题。
- 增加紧凑多行候选，保留自然词宽和竖线分隔。下键展开；首行再短按上键收回单行；返回键也可收回。
- 展开候选和拼音选择支持长按方向键滚动。修复快速连按导致的分隔错位、松开后多走一步，以及选择拼音后无法正常展开的问题。
- 展开候选每行独立重复标注 1–9，数字选择当前高亮框所在行的词。每行最多九项；不存在的数字不会选到其他行。数字直选可在中文设置中关闭。
- 一次上键选择拼音，两次上键编辑拼音。左右键移动光标，数字键插入，退格删除光标前一位；支持的编辑器使用系统组词光标，拒绝组词选区的编辑器保留可见的兼容光标。下键或确认返回汉字候选，返回键取消本轮修改。
- 修复编辑后重新选拼音时列表被旧读音筛选锁住的问题，重新打开会恢复整串按键对应的拼音候选；正常的 `XIAN` 不再与编辑预览混淆。快速连按两次上键会等当前拼音列表显示后再编辑，不会用旧候选截短输入。插入或删除后立即确认会等待读音解析；无匹配或超时后仍可修改，并可按返回恢复本轮编辑前的输入。
- 重新整理默认词频，保留全部 482,284 组词语与读音，修正部分多音字的默认读音，并去除候选栏里的重复汉字。
- 增加离线汉字前缀补全，例如输入“你”的读音时可以找到“你好”“你好吗”。常用的精确单字排在补全词组前，次级单字排在后；已学习的首选候选和个人词条保留优先级。

### 中文设置与个人词库

- 中文设置统一放在 **外观 → 中文输入**，包含紧凑候选、数字直选、界面语言、前缀补全、模糊音、生僻字过滤和个人词语管理。
- 中文包提供可选中文界面资源。安装或启用中文输入不会自动切换界面语言，可自行选择中文、英文或跟随系统。
- 增加中文用户词与读音管理：添加、搜索、修改和删除单个读音，并接入现有导入／导出流程。同一个词可保存多个读音，手动选择的音节边界会被保留。
- UTF-8 个人词导入格式为 `词语<TAB>462106<TAB>拼音`，例如 `重庆<TAB>462106<TAB>chong qing`。其他语言保留原有两列格式。导入中文词条需要已安装中文代码扩展。
- 增加六组可独立开启的模糊音：n/l、z/zh、c/ch、s/sh、en/eng、in/ing，默认关闭。
- 增加可选生僻字过滤，依据 2013 年《通用规范汉字表》的 3,500 个一级字，默认关闭；明确保存的用户词仍可显示。
- 删除中文字典会清除中文设置和私有包数据，并请求 Android 移除功能和翻译分包。个人词与读音保留，重新导入后可继续使用。若取消系统移除确认，代码分包仍在，但中文配置已清除。中文语言仍启用时，外观中的同一个入口可继续用于重新导入中文包。仅取消勾选语言会保留数据。
- 保留全局星号／井号互换选项，方便适配不同实体键盘布局。

### 包体与上游发布集成

中文专用转换增强、拼音编辑、音节规则、模糊音、前缀索引、用户词管理和中文设置位于可选 `chinese` 功能模块。主 APK 保留共用键盘、存储和安装接口；中文界面翻译由资源分包提供。未安装中文包的用户无需安装这些中文功能代码和索引。Full 离线版本继续保留原有普通字典，不增加联网要求。

此实现使用 Android 签名分包和系统安装确认。包任务会生成字典、数据、功能 APK 与翻译 APK 的组合 ZIP。代码分包必须与主 APK 的包名、版本和签名匹配，翻译分包的资源 ID 必须来自同一次主 APK 构建。构建和本地导入方法见 [中文扩展开发说明](chinese-extension.md)。

**上游仍需接入中文功能包的构建、签名、托管与发布流程。** 现有在线中文字典下载地址仍可能只提供普通字典，不能据此宣称下载字典就会自动安装新功能。后续上游发布需一同提供匹配的主 APK 与组合 ZIP，或将匹配包接入其下载渠道。本地导入路径已提供在 **外观 → 中文输入 → 导入／更新中文扩展包**；Android 会要求安装来源权限和安装确认，安装分包可能重启输入法。应用商店分发还需适配相应的动态功能交付流程。

### 当前范围

联想目前是本轮输入中的离线前缀补全。提交后无需按键的下一词预测、整句语言模型和逐段确认仍是后续功能；已支持音节分隔和读音修改。不同厂商的实体按键扫描、编辑器和安装限制仍需设备验证，问题报告应包含型号、Android 版本及复现按键顺序。

## English

This document describes a contribution to upstream Traditional T9 and can be used for code review and future release notes. The changes target Android flip phones and feature phones with physical number keys and a D-pad. Normal keypad use needs only the candidate area; an on-screen number pad is unnecessary.

### Input and candidate selection

- Fixes Simplified Chinese activation, Chinese input when global alphabetic prediction is disabled, and premature commits while typing incomplete Pinyin.
- Adds compact, naturally sized multirow candidates with vertical separators. Down expands; a short Up on the first row or Back returns to the single row.
- Supports held-arrow scrolling in expanded candidates and Pinyin choices. Fixes separator corruption during rapid navigation, extra movement on release, and expansion after selecting a reading.
- Repeats numeric labels 1–9 on every expanded row. Number keys select within the highlighted candidate's row. Rows contain at most nine candidates; absent positions never select from another row. This selection mode can be disabled in Chinese settings.
- One Up selects a reading; a second Up edits Pinyin. Left/Right move the caret, number keys insert, and Backspace deletes before it. Supported editors use their actual system composing caret; editors that reject composing selection retain a visible compatibility cursor. Down/confirm returns to Han candidates; Back cancels the current edit.
- Restores the full spelling list when reopening it after an edit, removes the collision between an ordinary `XIAN` candidate and its old editing preview, and queues rapid Up/Up until the current spelling list is shown so stale candidates cannot truncate the input. Confirmation immediately after insertion/deletion waits for reading resolution. A no-match or timeout leaves the text editable, and Back restores the input from before the edit.
- Revises default frequencies while retaining all 482,284 word/reading pairs, corrects several polyphonic defaults, and deduplicates displayed Han candidates.
- Adds offline Han-prefix completion, including 你好 and 你好吗 while composing the reading for 你. Frequent exact single characters precede completions; secondary characters follow them. Learned leading candidates and explicit custom words retain priority.

### Chinese settings and personal words

- Consolidates Chinese settings under **Appearance → Chinese input**, including compact candidates, numeric selection, UI language, prefix completion, fuzzy matching, the rare-character filter, and personal-word management.
- Provides optional Chinese UI resources in the pack. Installing or enabling Chinese input does not automatically change the UI language; choose Chinese, English, or the system language explicitly.
- Adds Chinese word/reading management with add, search, edit, per-reading deletion, and existing import/export integration. A word can have multiple readings, and explicitly chosen syllable boundaries survive round trips.
- UTF-8 custom-word records use `word<TAB>462106<TAB>reading`, for example `重庆<TAB>462106<TAB>chong qing`. Other languages retain their two-column format. Importing Chinese entries requires the Chinese code extension.
- Adds six independently optional fuzzy rules: n/l, z/zh, c/ch, s/sh, en/eng, and in/ing. All are off by default.
- Adds an optional rare-character filter based on the 3,500 level-one characters in the 2013 national Table of General Standard Chinese Characters. It is off by default and preserves explicitly saved user words.
- Removing the Chinese dictionary clears its profile and private pack data and asks Android to remove both feature and translation splits. Personal words/readings survive and return after reimport. Declining Android's removal confirmation leaves the code splits installed while the Chinese profile is cleared. If Chinese remains enabled, the same Appearance entry remains available to reimport the pack. Merely disabling the language preserves its data.
- Retains the global star/pound swap setting for different physical keypad layouts.

### Package size and upstream publishing

Chinese-specific conversion enhancements, Pinyin editing, syllable rules, fuzzy matching, the prefix index, word management, and Chinese settings live in the optional `chinese` feature module. The base retains shared keyboard, storage, and installation interfaces. Chinese UI translations ship in a resource split. Users without the Chinese pack do not install this feature code or its indexes. The Full offline flavor retains its original plain dictionaries without adding an Internet requirement.

The implementation uses Android-signed splits and the system installation prompt. Its packaging task produces a combined ZIP containing the dictionary, data, feature APK, and translation APK. Code splits must match the base package name, version, and signing certificate. Translation resource IDs must come from that exact base build. See the [Chinese extension development notes](chinese-extension.md) for building and local import.

**Upstream still needs to integrate building, signing, hosting, and publishing the Chinese feature pack.** Existing online Chinese dictionary URLs may still provide only plain dictionary data; downloading those files does not deliver the new code. A future upstream release must provide its matching base APK and combined ZIP together, or integrate the matching pack into its download channel. Local import is available through **Appearance → Chinese input → Import/update Chinese pack**. Android requests install-source permission and confirmation; installing splits can restart the input method. App-store distribution also needs the applicable dynamic-feature delivery integration.

### Current scope

Completion operates on the current composition using an offline prefix index. Zero-key prediction after commit, a sentence language model, and incremental segment confirmation remain future work. Syllable boundaries and reading editing are supported. Vendor key scanning, editor behavior, and installation restrictions still require device validation; reports should include the phone model, Android version, and exact key sequence.
