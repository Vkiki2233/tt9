package io.github.sspanak.tt9.extensions;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.preferences.screens.chinese.ChineseSettingsProfile;

/** Keeps Languages' Chinese input entry useful before its optional split is installed. */
public class ChinesePackScreen extends BaseScreenFragment {
	public ChinesePackScreen() { super(); }
	public ChinesePackScreen(PreferencesActivity activity) { super(activity); }
	@Override public String getName() { return "ChineseSettings"; }
	@Override protected int getTitle() { return R.string.pref_chinese_settings; }
	@Override protected int getXml() { return R.xml.prefs_screen_chinese_pack; }

	@Override protected void onCreate() {
		resetFontSize(true);
		Preference file = findPreference("import_chinese_pack");
		if (file != null) file.setOnPreferenceClickListener(preference -> {
			if (activity != null) ChinesePackInstaller.importPack(activity);
			return true;
		});
		Preference install = findPreference("install_chinese_pack");
		if (install != null) {
			install.setVisible(activity != null && ChinesePackInstaller.needsInstall(activity));
			install.setOnPreferenceClickListener(preference -> {
				if (activity != null) ChinesePackInstaller.launch(activity);
				return true;
			});
		}
	}

	@Override public void onResume() {
		super.onResume();
		Preference install = findPreference("install_chinese_pack");
		if (install != null) install.setVisible(activity != null && ChinesePackInstaller.needsInstall(activity));
		if (activity != null) ChineseSettingsProfile.checkAvailable(activity, activity.getSettings(), installed -> {
			if (isAdded() && !installed) activity.getOnBackPressedDispatcher().onBackPressed();
		});
	}
}
