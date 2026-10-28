# Simplified Chinese extension

The base APK keeps shared keyboard rendering, storage and a small extension contract.
`chinese/` contains the Pinyin editor, syllable rules, fuzzy matching, prefix lookup,
user-word manager and Chinese settings. Host UI translations are a resource-only
`config.zh_rCN` split, with IDs generated from that exact base build.

Build matching signed debug artifacts with:

```
./gradlew :app:testLiteDebugUnitTest :chinese:testLiteDebugUnitTest :app:packageLiteDebugChinesePack :app:lintLiteDebug
```

The main deliverables are the base APK and
`app/build/outputs/chinese-packs/liteDebug/zh-simplified-pinyin.zip`.
The ZIP starts with the normal dictionary entry, followed by the common-character
table and its Unicode license, the compact prefix index, `chinese.apk` and
`chinese-ui.apk`. Do not put this ZIP back into the base APK's assets.
Release packaging requires the same configured signing key for the base and both
splits. Android checks the package, version and signing certificate during install.

Enable Simplified Chinese, then use Appearance → 中文输入 → Import/Update
Chinese Pack to select the ZIP. Android may ask to allow installs from this source
and will ask for installation confirmation. Installing or removing splits can
restart the input method. No previous dictionary download is required to import
this ZIP. The full offline flavor keeps its original bundled plain dictionaries
and has no Internet permission; both flavors keep Chinese extension code, indexes
and translated host UI outside the base. The legacy online dictionary URL remains supported; a
data-only dictionary does not contain these extension features. Until a matching
signed pack is published, distribute the base APK and generated ZIP together and
use this local import path. Existing online dictionary files alone must not be
advertised as the new code pack.

Deleting the Chinese dictionary removes its settings and private pack data, then
asks Android to remove both optional splits. Declining that confirmation leaves
the code installed but the dictionary and Chinese profile are already removed.
If Chinese remains enabled, the same Appearance entry remains available to import
the pack again. Deselecting a language does not delete it. User words and their explicit
readings survive removal and return when the pack/dictionary is reloaded.

The original custom-word import/export also accepts Chinese after the code split
is installed. The format is `word<TAB>462106<TAB>reading`, with one unaccented
syllable per Han character, for example `重庆<TAB>462106<TAB>chong qing`.
Reading boundaries are stored separately, so editing `CHAN'GAN` into `CHANG'AN`
does not silently lose the chosen boundary. Other languages retain their existing
two-column format and the usual import limits.

The common-character switch is off by default and uses the 2013 national table's
3,500 level-one characters; explicitly saved user words bypass it. Fuzzy rules are
also off by default. Phrase completion and expanded-panel number selection are on
by default. Completion replaces the current composition; zero-key prediction
after a committed word and partial-syllable confirmation are not implemented.

Each visual row of the expanded candidates restarts its numeric labels at 1. A
digit selects that position in the highlighted candidate's row; an absent position
does nothing. Numbered rows contain at most nine naturally sized candidates.
Common exact single characters precede prefix completions, while less frequent
characters follow them. Learned leading candidates and explicit user words retain
their priority. The prefix index also carries a small per-reading frequency table;
older version-one indexes remain readable.

The second Up enters Pinyin editing with the application's actual composing-text
cursor. Left/Right, insertion and backspace move or modify that selection. Editors
that reject composing selection retain the visible inline cursor fallback.

The GitHub/local-install channel uses Android's package installer, not loose DEX
loading. A Play distribution would need its own Play Feature Delivery integration.
