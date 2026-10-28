package io.github.sspanak.tt9.ime.helpers;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.NullLanguage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InputModePolicyTest {
	private static final int PREDICTIVE = InputMode.MODE_PREDICTIVE;
	private static final int ABC = InputMode.MODE_ABC;
	private static final int NUMERIC = InputMode.MODE_123;
	private static final int HIRAGANA = InputMode.MODE_HIRAGANA;
	private static final int KATAKANA = InputMode.MODE_KATAKANA;
	private static final List<Integer> TEXT_MODES = Arrays.asList(PREDICTIVE, NUMERIC, ABC);
	private static final Language PINYIN = new TestLanguage(462106, false);
	private static final Language BOPOMOFO = new TestLanguage(774426, true);
	private static final Language JAPANESE = new TestLanguage(534570, false);
	private static final Language KOREAN = new TestLanguage(601579, false);
	private static final Language LATIN = new TestLanguage(453, true);


	@Test
	public void chineseConversionRemainsAvailableWithoutOptionalPredictions() {
		assertEquals(Arrays.asList(PREDICTIVE, NUMERIC), InputModePolicy.getAllowedModes(TEXT_MODES, PINYIN, false));
		assertEquals(TEXT_MODES, InputModePolicy.getAllowedModes(TEXT_MODES, BOPOMOFO, false));
		assertTrue(InputModePolicy.isDictionaryEnabled(PINYIN, false));
		assertTrue(InputModePolicy.isDictionaryEnabled(BOPOMOFO, false));
	}


	@Test
	public void disablingLatinPredictionsKeepsAbcAndNumbers() {
		assertEquals(Arrays.asList(NUMERIC, ABC), InputModePolicy.getAllowedModes(TEXT_MODES, LATIN, false));
		assertFalse(InputModePolicy.isDictionaryEnabled(LATIN, false));
	}


	@Test
	public void enabledPredictionsKeepExistingModes() {
		assertEquals(TEXT_MODES, InputModePolicy.getAllowedModes(TEXT_MODES, LATIN, true));
		assertEquals(Arrays.asList(PREDICTIVE, NUMERIC), InputModePolicy.getAllowedModes(TEXT_MODES, PINYIN, true));
	}


	@Test
	public void japaneseKeepsKanaWhenOptionalPredictionsAreDisabled() {
		assertEquals(Arrays.asList(NUMERIC, HIRAGANA, KATAKANA), InputModePolicy.getAllowedModes(TEXT_MODES, JAPANESE, false));
		assertFalse(InputModePolicy.isDictionaryEnabled(JAPANESE, false));
	}


	@Test
	public void japaneseKeepsKanjiAndKanaWhenPredictionsAreEnabled() {
		assertEquals(Arrays.asList(PREDICTIVE, NUMERIC, HIRAGANA, KATAKANA), InputModePolicy.getAllowedModes(TEXT_MODES, JAPANESE, true));
	}


	@Test
	public void koreanRetainsItsExistingPredictionPreference() {
		assertEquals(Arrays.asList(PREDICTIVE, NUMERIC), InputModePolicy.getAllowedModes(TEXT_MODES, KOREAN, true));
		assertEquals(List.of(NUMERIC), InputModePolicy.getAllowedModes(TEXT_MODES, KOREAN, false));
	}


	@Test
	public void numericEditorsNeverAcquireChineseConversion() {
		List<Integer> editorModes = List.of(NUMERIC);
		assertEquals(editorModes, InputModePolicy.getAllowedModes(editorModes, PINYIN, false));
		assertEquals(editorModes, InputModePolicy.getAllowedModes(editorModes, PINYIN, true));
		assertEquals(editorModes, InputModePolicy.getAllowedModes(editorModes, JAPANESE, true));
	}


	@Test
	public void passwordEditorsNeverAcquireChineseConversion() {
		List<Integer> editorModes = Arrays.asList(NUMERIC, ABC);
		assertEquals(List.of(NUMERIC), InputModePolicy.getAllowedModes(editorModes, PINYIN, false));
		assertEquals(List.of(NUMERIC), InputModePolicy.getAllowedModes(editorModes, PINYIN, true));
		assertEquals(editorModes, InputModePolicy.getAllowedModes(editorModes, LATIN, true));
	}


	@Test
	public void passthroughEditorsNeverAcquireTextModes() {
		List<Integer> editorModes = List.of(InputMode.MODE_PASSTHROUGH);
		assertEquals(editorModes, InputModePolicy.getAllowedModes(editorModes, PINYIN, false));
	}


	@Test
	public void filteringDoesNotMutateEditorModes() {
		ArrayList<Integer> editorModes = new ArrayList<>(TEXT_MODES);
		InputModePolicy.getAllowedModes(editorModes, JAPANESE, false);
		assertEquals(TEXT_MODES, editorModes);
	}


	@Test
	public void manuallySelectingNumbersDoesNotPreventChangingTextLanguage() {
		assertTrue(InputModePolicy.canSwitchLanguage(true, false, Arrays.asList(PREDICTIVE, NUMERIC)));
		assertTrue(InputModePolicy.canSwitchLanguage(true, false, Arrays.asList(NUMERIC, ABC)));
		assertTrue(InputModePolicy.canSwitchLanguage(true, false, Arrays.asList(NUMERIC, HIRAGANA, KATAKANA)));
	}


	@Test
	public void languageSwitchUsesEditorCapabilitiesWhenCurrentLanguageHasNoTextMode() {
		assertEquals(List.of(NUMERIC), InputModePolicy.getAllowedModes(TEXT_MODES, KOREAN, false));
		assertTrue(InputModePolicy.canSwitchLanguage(true, false, TEXT_MODES));
	}


	@Test
	public void numericOnlyAndPasswordModesKeepLanguageSwitchRestricted() {
		assertFalse(InputModePolicy.canSwitchLanguage(true, false, List.of(NUMERIC)));
		assertFalse(InputModePolicy.canSwitchLanguage(true, false, List.of(InputMode.MODE_PASSTHROUGH)));
		assertFalse(InputModePolicy.canSwitchLanguage(true, true, Arrays.asList(NUMERIC, ABC)));
		assertTrue(InputModePolicy.canSwitchLanguage(false, true, Arrays.asList(NUMERIC, ABC)));
	}


	private static class TestLanguage extends NullLanguage {
		TestLanguage(int languageId, boolean supportsAbc) {
			id = languageId;
			hasABC = supportsAbc;
		}
	}
}
