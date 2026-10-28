# Chinese input verification

This is the verification record and reproduction guide for the upstream Chinese
input contribution. See [the update description](chinese-update.md) for scope and
[the extension notes](chinese-extension.md) for the package layout.

## Current results

| Check | Result |
| --- | --- |
| Host and Chinese feature unit tests | 162 tests passing in Full; 162 in Lite |
| Debug APK and combined Chinese pack build | Passed |
| Full debug Android lint | 0 errors; warnings remain |
| Updated Full release with R8 and resource shrinking, including combined pack | Build passed |
| Updated Full release Android lint | 0 errors, 367 warnings |
| Mive, Android 12, debug operation combinations | 11/11 passed; reading-to-Han mapping also passed |
| Android 15 / API 35 debug operation combinations | 23/23 passed |
| API 35 minified release operation combinations | 6/6 passed |
| API 35 candidate-grid and repeat checks | 8/8 passed |
| API 35 installed-engine profiles | 24/24 passed |

Build results and runtime results are recorded separately. The minified runtime
coverage is the API 35 smoke described below; Mive used the debug artifacts.
Lint and Gradle warnings are present; this contribution does not claim a
warning-free build.

The current unit suite includes the repaired transitions: ordinary `XIAN` versus
an old editing preview, repeated edit/convert/reopen cycles, query/result/render
revision ordering, rapid second-Up queuing, stale short-reading rejection while
preserving enabled fuzzy alternatives, and finish/cancel/timeout behavior after
insertion or deletion. Full-spelling restoration is also checked in the device
operation matrices below.

## Reproduce the debug build

Use the repository's Gradle wrapper and its configured JDK/Android SDK. From the
repository root (`gradlew.bat` on Windows):

```sh
./gradlew :app:testLiteDebugUnitTest :chinese:testLiteDebugUnitTest \
  :app:packageLiteDebugChinesePack :app:lintLiteDebug
```

On Windows, a checkout made with `core.symlinks=false` can leave the tracked
`app/src/lite/java` symlink as a text file containing `../full/java`. The local
test environment maps the Lite Java source set to `app/src/full/java` through an
external test-only init script when that happens. This does not change the
repository's source layout. A normal Linux checkout/CI follows the tracked
symlink and needs no such mapping.

This exercises dictionary/parser and prediction behavior, candidate-row
numbering/navigation, Pinyin editing and selection tracking, custom-word/readings,
fuzzy matching, prefix indexing, common-character filtering, and staged-pack
rollback. Inspect the host and feature test reports under their respective
`build/reports/tests/` directories and the host lint report under
`app/build/reports/`.

Install the generated base APK from `app/build/outputs/apk/lite/debug/` on a test
device or emulator. Enable TT9 and Simplified Chinese, then import
`app/build/outputs/chinese-packs/liteDebug/zh-simplified-pinyin.zip` through
**Appearance → Chinese input → Import/update Chinese pack**. Confirm Android's
install-source and split-install prompts. The base and both splits must be from
the same build and use the same signing certificate. Split installation may
restart the input method.

## Mive Android 12 debug checks

Installed device APKs used an isolated test application ID and the Full source
set with lean test assets and Lite download behavior. The normal Full flavor's
complete bundled dictionary set was generated/validated but excluded from these
installed test APKs. The device results below validate Chinese input and optional
pack behavior; they are not an installation test of the stock all-dictionaries
Full APK. Lite unit tests were also compiled and executed separately.

The latest targeted run used ADB-injected Android `KeyEvent` input on the actual
Mive. This reaches Android's input method key handling, including repeat events;
it does not exercise the physical keypad scanner. Earlier human-operated keypad
checks and raw input-event capture establish separate physical-key evidence.
They are not a fresh physical-key retest of every latest change.

Verified targeted cases:

- Compose, commit, then delete the last character of 你好 (`64426`), 中国
  (`94664486`), 我们 (`96636`), 北京 (`2345464`), and 上海 (`74264424`).
- For ambiguous `943943`, accept 这些 or explicitly select 谢谢 with Right and
  commit it. Both spellings share the T9 sequence; 谢谢 is not required to be the
  first candidate.
- Expand candidates, move to another row, use that row's repeated number labels,
  and verify that an absent number leaves selection/composition unchanged.
- Rapid Up/Down and repeated arrows preserve the compact separators; the held
  arrow cases settle after release. A short Up from the first row collapses.
- Edit `XIAN` after the committed prefix 你好. Left/Backspace changes the
  composition to 你好XIN with the real selection inside the composing span;
  inserting the missing key restores 你好XIAN. Conversion produces 你好西安 while
  retaining the prefix. Editor selection/composition offsets were captured.
- Cancel Pinyin editing; enter numeric and password fields; confirm Chinese with
  Pound, insert the existing Chinese U+3000 space when idle, switch to numeric
  mode with Star, and switch between English and Chinese with held Star.
- Pass all 11 final operation combinations, including repeated spelling/edit/Han
  cycles. Explicitly selecting `XIAO` produces 小; selecting `XIAN` produces 先,
  with the corresponding distinct Han candidate lists.

Language switching above is the explicit key action. Changing an editor language
hint on a reused input connection was not verified as automatic language
switching and is not claimed here.

During fixture development, clearing a field and calling `restartInput` within
150 ms of a composing write could trigger TT9's existing automatic
composing-disable compatibility heuristic. The cached field could then show
candidates while the editor stayed blank. The final combination run used fresh
input sessions and passed; this contribution does not claim a product fix for
that pre-existing restart heuristic.

The final Mive preservation audit matched all 482,284 word/position pairs against
the pretest backup without reloading the dictionary. Only five identified
test-created frequency promotions were reverted; user learning and existing
preferences were preserved. Final installed APK hashes matched the tested set.

Local evidence is kept outside the repository under the QA workspace's
`global-qa-mive/` directory: `normal-typing-cases-verified.json`,
`thanks-committed.json`, `prefixed-native-caret-verification.json`,
`held-arrows.json`, `mixed-field-keypad-checks-verified.json`,
`reading-to-han-mapping.json`, `qa-final-report.json`, row-state captures, and
screenshots. Device backups and private preferences are not release assets.

## API 35 debug and minified runtime results

The final Android 15 emulator report records 23/23 debug operation combinations,
6/6 minified release combinations, 8/8 candidate-grid/repeat checks, and 24/24
installed-engine profiles passing. Checks included full spelling-list restoration,
rapid transitions, held phonetic arrows, second-row numeric selection, natural
candidate widths, and the absence of labels or reserved number space when numeric
selection is disabled. Normal multiword typing, backspace, numeric/password
fields, and mixed input also passed.

The final minified build loaded the reflected Chinese settings and word-manager
entry points, passed custom-word creation/reading-edit/deletion, and displayed
explicit Chinese and system UI language selections. Its installed APK hashes
matched the final immutable base/feature/UI set. No unresolved product failures
were reported. The emulator's original state was restored and shutdown verified.

Normal document-picker import and Android split-install consent were exercised
on the earlier debug build before the Pinyin-only transition fixes. The final
code upgrade used `adb install-multiple -r` with matching three-APK sets; the final
runtime results do not imply a fresh document-picker installation of that last
set.

Full personal-word import/export and invalid-import atomic rollback, plus
dictionary/profile/split deletion and offline reload with personal readings
preserved, passed in the earlier unchanged-path verification. Those complete
lifecycle cases were not freshly repeated in the final operation matrix.

Evidence is outside the repository in `global-qa-api35/qa-final-report.json` and
its referenced captures. Earlier lifecycle evidence is separately recorded in
`api35-split-feature-qa/qa-final-complete.json`. Injected Android key events do not
validate a manufacturer's physical keypad scanner.

## Reproduce a minified release smoke test

Use a disposable test environment. Keep any test keystore, passwords, and signing
init script outside the checkout. Configure the base release signing config with
a local key; the feature and generated UI split must use that same key. For
example, save this init script outside the repository and supply the named
environment variables through the local shell or CI secret store:

```groovy
gradle.beforeProject { project ->
    project.plugins.withId('com.android.application') {
        project.androidComponents.finalizeDsl { android ->
            def keyStore = System.getenv('TT9_SMOKE_KEYSTORE')
            def storePass = System.getenv('TT9_SMOKE_STORE_PASSWORD')
            def keyAlias = System.getenv('TT9_SMOKE_KEY_ALIAS')
            def keyPass = System.getenv('TT9_SMOKE_KEY_PASSWORD') ?: storePass
            if (!keyStore || !storePass || !keyAlias || !keyPass) {
                throw new GradleException('Missing local smoke signing configuration')
            }
            def signing = android.signingConfigs.create('localSmoke')
            signing.storeFile = new File(keyStore)
            signing.storePassword = storePass
            signing.keyAlias = keyAlias
            signing.keyPassword = keyPass
            android.buildTypes.release.signingConfig = signing
        }
    }
}
```

Build using that script's absolute path:

```sh
./gradlew -I /absolute/path/to/local-smoke.init.gradle \
  :app:packageLiteReleaseChinesePack :app:lintLiteRelease
```

The existing `release` build type enables R8 and resource shrinking. The Full
flavor equivalents (`packageFullReleaseChinesePack`, `lintFullRelease`) also
exercise the offline flavor. Keep test signing material and test-signed artifacts
out of upstream distribution; publishing uses the upstream signing process.

Install the matching release base and ZIP through the same user-facing import
path. Verify the following on the minified build:

1. Start the base without the feature installed, open the Chinese bootstrap import
   screen, and install both signed splits using Android's confirmation.
2. Reopen Chinese settings and word management, change the optional UI language,
   and confirm that the reflected feature entry point and feature resources load.
3. Repeat candidate expansion, current-row number selection, and held-arrow
   release checks. Cycle Han → spellings → Pinyin edit → Han → spellings, checking
   the whole alternative list after each cycle. Include rapid Up/Up, immediate
   insert/delete → Down/confirm → Up, no-match input, and Back cancellation.
   Restart input while editing and confirm that the old cursor override does not
   survive; separate fixture-triggered composing-disable behavior as noted above.
4. Add/edit/export/import a word with an explicit reading; test fuzzy and
   common-character switches; confirm ordinary English/numeric/password input.
5. Cancel a split update, resume pending installation from the same Chinese
   settings screen, and verify that a mismatched version/signature is rejected by
   Android. Remove the dictionary, inspect profile/data cleanup, confirm native
   split removal, and reimport while preserving personal words/readings.

Record APK/ZIP hashes, application ID/version, Android version, signer match,
test outcomes, and crash-log results. Preserve the exact base/split set used;
debug runtime results cannot substitute for this R8 runtime smoke test.
