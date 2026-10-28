package io.github.sspanak.tt9.extensions;

import io.github.sspanak.tt9.db.entities.CustomWord;
import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.ModePinyin;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

/** The host contract implemented by the optional, Android-signed Chinese split. */
public interface ChineseExtension {
	ModePinyin createInputMode(SettingsStore settings, Language language, InputType inputType, TextField textField);
	BaseScreenFragment createScreen(PreferencesActivity activity, String screenName);
	CustomWord parseUserWord(String nativeWord, String reading);
	String exportUserWord(String rawStoredWord);
}
