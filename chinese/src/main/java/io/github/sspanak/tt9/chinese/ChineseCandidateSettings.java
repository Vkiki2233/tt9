package io.github.sspanak.tt9.chinese;

import androidx.preference.PreferenceManager;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

/** Chinese-only behavior settings; the host owns ordinary single-row scrolling. */
public final class ChineseCandidateSettings {
	public static final String EXPANDED_SCROLL_SPEED = "pref_chinese_expanded_scroll_speed";
	private ChineseCandidateSettings() {}

	public static boolean getCandidateCompact(SettingsStore settings) {
		return PreferenceManager.getDefaultSharedPreferences(settings.getContext()).getBoolean("pref_chinese_candidate_compact", true);
	}

	public static boolean getNumberSelect(SettingsStore settings) {
		return PreferenceManager.getDefaultSharedPreferences(settings.getContext()).getBoolean("pref_chinese_number_select", true);
	}

	public static String getExpandedScrollSpeed(SettingsStore settings) {
		String speed = PreferenceManager.getDefaultSharedPreferences(settings.getContext())
			.getString(EXPANDED_SCROLL_SPEED, "normal");
		return "slow".equals(speed) || "fast".equals(speed) ? speed : "normal";
	}

	public static int getExpandedCandidateRepeatInterval(SettingsStore settings) {
		return switch (getExpandedScrollSpeed(settings)) {
			case "slow" -> 240;
			case "fast" -> 60;
			default -> 120;
		};
	}
}
