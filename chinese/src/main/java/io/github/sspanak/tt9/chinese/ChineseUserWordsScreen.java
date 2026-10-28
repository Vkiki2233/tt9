package io.github.sspanak.tt9.chinese;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import android.content.Intent;

import io.github.sspanak.tt9.db.entities.CustomWord;
import io.github.sspanak.tt9.db.words.WordStore;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.preferences.screens.languages.ItemExportCustomWords;
import io.github.sspanak.tt9.preferences.screens.languages.ItemImportCustomWords;
import io.github.sspanak.tt9.util.SupremeExecutor;

/** Reuses the existing custom-word importer/exporter and database ownership rules. */
public class ChineseUserWordsScreen extends BaseScreenFragment {
	public static final String NAME = "ChineseUserWords";
	private String search = "";
	private long queryVersion;
	private ItemImportCustomWords importer;
	private ItemExportCustomWords exporter;

	public ChineseUserWordsScreen() { super(); }
	public ChineseUserWordsScreen(@Nullable PreferencesActivity activity) { super(activity); }
	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.chinese_user_words_title; }
	@Override protected int getXml() { return R.xml.chinese_user_words; }

	@Override protected void onCreate() {
		if (activity == null) return;
		Preference add = findPreference("chinese_user_add");
		if (add != null) add.setOnPreferenceClickListener(preference -> { ChineseUserWordDialog.show(activity, null, this::refresh); return true; });
		EditTextPreference filter = findPreference("chinese_user_search");
		if (filter != null) filter.setOnPreferenceChangeListener((preference, value) -> {
			search = value.toString().trim(); filter.setSummary(search); refresh(); return true;
		});
		importer = new ItemImportCustomWords(findPreference("dictionary_import_custom"), activity, this::disableEditing, this::onProcessingFinished);
		ActivityResultLauncher<Intent> browser = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), importer::onFileSelected);
		importer.setBrowseFilesLauncher(browser);
		importer.enableClickHandler();
		exporter = new ItemExportCustomWords(findPreference("dictionary_export_custom"), activity, this::disableEditing, this::onProcessingFinished);
		exporter.enableClickHandler();
		resetFontSize(false);
	}

	@Override public void onResume() { super.onResume(); refresh(); }
	@Override public void onDestroyView() { queryVersion++; super.onDestroyView(); }

	private void disableEditing() {
		for (String key : new String[]{"chinese_user_add", "chinese_user_search", "chinese_user_list"}) {
			Preference item = findPreference(key);
			if (item != null) item.setEnabled(false);
		}
	}

	private void refresh() {
		refresh(true);
	}

	private void onProcessingFinished() {
		if (!isAdded()) return;
		if (importer != null) importer.enable();
		if (exporter != null) exporter.enable();
		// The worker can still be unwinding when its completion callback reaches the UI.
		refresh(false);
	}

	private void refresh(boolean refreshProcessingStatus) {
		if (activity == null || getPreferenceScreen() == null) return;
		for (String key : new String[]{"chinese_user_add", "chinese_user_search", "chinese_user_list"}) {
			Preference item = findPreference(key);
			if (item != null) item.setEnabled(true);
		}
		if (refreshProcessingStatus && importer != null) importer.refreshStatus();
		if (refreshProcessingStatus && exporter != null) exporter.refreshStatus();
		Preference export = findPreference("dictionary_export_custom");
		if (export != null) export.setTitle(R.string.chinese_user_export_all);
		Preference format = findPreference("chinese_user_format");
		if (format != null) format.setSummary(R.string.chinese_user_format);
		long version = ++queryVersion;
		String filter = search;
		PreferencesActivity screen = activity;
		SupremeExecutor.execute(() -> {
			var words = new WordStore(screen.getApplicationContext()).getChineseCustomWords(filter, 200);
			SupremeExecutor.executeOnMainThread(() -> {
				if (version != queryVersion || !isAdded()) return;
				PreferenceCategory list = findPreference("chinese_user_list");
				if (list == null) return;
				list.removeAll();
				for (CustomWord word : words) {
					Preference item = new Preference(screen);
					item.setTitle(word.getDisplayWord());
					item.setSummary(word.getReading().replace('\'', ' '));
					item.setOnPreferenceClickListener(preference -> { ChineseUserWordDialog.show(screen, word, this::refresh); return true; });
					list.addPreference(item);
				}
				if (words.isEmpty()) {
					Preference empty = new Preference(screen);
					empty.setSummary(R.string.chinese_user_empty);
					empty.setEnabled(false);
					list.addPreference(empty);
				}
				resetFontSize(true);
			});
		});
	}
}
