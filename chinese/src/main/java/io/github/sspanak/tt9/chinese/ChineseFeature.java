package io.github.sspanak.tt9.chinese;

import io.github.sspanak.tt9.db.entities.CustomWord;
import io.github.sspanak.tt9.extensions.ChineseExtension;
import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.ModePinyin;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.preferences.screens.chinese.ChineseSettingsScreen;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

/** Stable entry point retained by the base app's shrinker rule. */
public final class ChineseFeature implements ChineseExtension {
	public ChineseFeature() {}

	@Override public ModePinyin createInputMode(SettingsStore settings, Language language, InputType inputType, TextField textField) {
		return new ModeChinesePinyin(settings, language, inputType, textField);
	}

	@Override public BaseScreenFragment createScreen(PreferencesActivity activity, String screenName) {
		return switch (screenName) {
			case "ChineseSettings" -> new ChineseSettingsScreen(activity);
			case ChineseUserWordsScreen.NAME -> new ChineseUserWordsScreen(activity);
			default -> null;
		};
	}

	@Override public CustomWord parseUserWord(String word, String reading) {
		ChineseUserWord entry = ChineseUserWord.create(word, reading);
		return new CustomWord(entry.storedWord(), entry.sequence(), ChineseUserWord.LANGUAGE_ID, entry.reading());
	}

	@Override public String exportUserWord(String rawStoredWord) {
		return ChineseUserWord.fromStored(rawStoredWord).exportLine();
	}
}
