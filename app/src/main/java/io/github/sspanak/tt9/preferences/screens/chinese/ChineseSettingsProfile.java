package io.github.sspanak.tt9.preferences.screens.chinese;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.function.Consumer;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public final class ChineseSettingsProfile {
	private ChineseSettingsProfile() {}

	/** An enabled but not yet loaded language needs the local pack importer, including offline. */
	public static void checkAvailable(Context context, SettingsStore settings, Consumer<Boolean> onResult) {
		LanguageCollection.init(context);
		Language chinese = LanguageCollection.getByLocale("zh_CN");
		if (chinese != null && settings.getEnabledLanguageIds().contains(chinese.getId())) onResult.accept(true);
		else checkInstalled(context, onResult);
	}

	public static void checkInstalled(Context context, Consumer<Boolean> onResult) {
		Language language = LanguageCollection.getByLocale("zh_CN");
		if (language == null) {
			onResult.accept(false);
			return;
		}
		ArrayList<Language> languages = new ArrayList<>();
		languages.add(language);
		DataStore.init(context.getApplicationContext());
		DataStore.exists(installed -> new Handler(Looper.getMainLooper()).post(
			() -> onResult.accept(!installed.isEmpty())
		), languages);
	}
}
