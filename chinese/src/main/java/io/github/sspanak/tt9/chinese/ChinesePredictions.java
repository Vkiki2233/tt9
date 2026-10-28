package io.github.sspanak.tt9.chinese;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.extensions.ChinesePack;
import io.github.sspanak.tt9.ime.modes.predictions.IdeogramPredictions;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.TextTools;

/** Optional Chinese-only queries and ranking; all dictionary work stays off the input thread. */
public class ChinesePredictions extends IdeogramPredictions {
	private ChinesePrefixIndex prefixes;
	private CommonHanCharacters commonCharacters;
	private boolean prefixesAttempted;
	private boolean commonCharactersAttempted;
	private ArrayList<String> phoneticWords = new ArrayList<>();
	private final AtomicLong generation = new AtomicLong();
	private final Handler mainHandler = new Handler(Looper.getMainLooper());
	private long cachedPackGeneration = -1;
	private boolean completionsAllowed = true;

	public ChinesePredictions(SettingsStore settings) { super(settings); }
	public ChinesePredictions setCompletionsAllowed(boolean allowed) { completionsAllowed = allowed; return this; }

	@Override public void reset() { generation.incrementAndGet(); phoneticWords.clear(); super.reset(); }

	@Override public void load() {
		containsGeneratedWords = false;
		if (digitSequence.isEmpty() || !digitSequence.matches("[2-9]+")) { super.load(); return; }
		final long request = generation.incrementAndGet();
		final String digits = digitSequence, selectedReading = stem;
		final int fuzzy = settings.getChineseFuzzyOptions();
		List<String> sequences = PinyinFuzzy.sequences(digits, selectedReading, fuzzy);
		// Fuzzy filtering uses its own compact spelling check; the ordinary stem SQL only matches exact reading prefixes.
		DataStore.getWordVariants(raw -> {
			if (request != generation.get()) return;
			ArrayList<String> exact = new ArrayList<>(), alternatives = new ArrayList<>();
			for (String word : raw) {
				String reading = reading(word);
				if (PinyinSyllables.getSequence(reading).equals(digits)) exact.add(word); else alternatives.add(word);
			}
			int insertion = Math.min(8, exact.size());
			exact.addAll(insertion, alternatives);
			publish(exact, false, request);
		}, language, sequences, fuzzy == 0 ? selectedReading.replace("'", "") : "", word -> {
			String reading = reading(word);
			return !reading.isEmpty() && PinyinFuzzy.matches(digits, selectedReading, reading, fuzzy)
				&& PinyinSyllables.matchesSelectedReading(word, selectedReading) && allowed(word);
		}, word -> {
			String reading = reading(word);
			return !reading.isEmpty() && PinyinFuzzy.matchesPrefix(digits, selectedReading, reading)
				&& PinyinSyllables.matchesSelectedReading(word, selectedReading) && allowed(word);
		});
	}

	@Override protected void onDbWords(ArrayList<String> dbWords, boolean retry) {
		publish(dbWords, retry, generation.get());
	}

	private void publish(ArrayList<String> dbWords, boolean retry, long request) {
		refreshCaches();
		long packGeneration = ChinesePack.getGeneration();
		String digits = digitSequence;
		ArrayList<String> phoneticSnapshot = new ArrayList<>(dbWords);
		ArrayList<String> completed = new ArrayList<>(dbWords);
		if (completionsAllowed && settings.getChinesePrefixCompletion() && !dbWords.isEmpty()) {
			ChinesePrefixIndex index = prefixes();
			if (index != null) {
				String first = nativeWord(dbWords.get(0));
				LinkedHashSet<String> unique = new LinkedHashSet<>(dbWords);
				ArrayList<String> additions = new ArrayList<>();
				for (ChinesePrefixIndex.Entry entry : index.lookup(first, 12)) {
					String raw = entry.transcription + entry.word;
					if (allowed(raw) && unique.add(raw)) additions.add(raw);
				}
				CommonHanCharacters common = commonCharacters();
				completed = ChineseCandidateRanking.merge(dbWords, additions,
					raw -> PinyinSyllables.getSequence(reading(raw)).equals(digits) ? index.singleCharacterFrequency(raw) : 0,
					raw -> {
						String word = nativeWord(raw);
						boolean exact = PinyinSyllables.getSequence(reading(raw)).equals(digits);
						if (!exact) return false;
						if (word.codePointCount(0, word.length()) > 1) return true;
						boolean commonCharacter = common == null || common.containsOnlyCommonHan(word);
						return commonCharacter && (!index.hasSingleCharacterFrequencies() || index.singleCharacterFrequency(raw) > 0)
							|| DataStore.isCustomWord(language, raw);
					});
			}
		}
		ArrayList<String> rankedSnapshot = completed;
		mainHandler.post(() -> {
			if (request != generation.get() || packGeneration != ChinesePack.getGeneration()) return;
			phoneticWords = phoneticSnapshot;
			super.onDbWords(rankedSnapshot, retry);
		});
	}

	@Override public void stripNativeWords() {
		if (!areThereDbWords) return;
		TreeSet<String> choices = new TreeSet<>();
		for (String raw : phoneticWords) {
			List<String> syllables = PinyinSyllables.choicesForStoredWord(raw);
			if (syllables.isEmpty() && nativeWord(raw).isEmpty()) choices.add(reading(raw));
			else choices.addAll(syllables);
		}
		words = new ArrayList<>(choices);
	}

	@Override public void onAcceptIdeogram(String word) throws InvalidLanguageCharactersException {
		String reading = getTranscription(word).replace("'", "");
		if (!reading.isEmpty()) super.onAccept(reading + word, PinyinSyllables.getSequence(reading));
	}

	private boolean allowed(String raw) {
		if (!settings.getChineseCommonOnly()) return true;
		refreshCaches();
		CommonHanCharacters common = commonCharacters();
		return common == null || common.containsOnlyCommonHan(nativeWord(raw)) || DataStore.isCustomWord(language, raw);
	}

	private ChinesePrefixIndex prefixes() {
		refreshCaches();
		if (!prefixesAttempted) {
			prefixesAttempted = true;
			try (InputStream stream = ChinesePack.open(settings.getContext(), ChinesePrefixIndex.FILE_NAME)) { prefixes = ChinesePrefixIndex.load(stream); }
			catch (IOException e) { Logger.w("ChinesePredictions", "Prefix index unavailable: " + e.getMessage()); }
		}
		return prefixes;
	}

	private CommonHanCharacters commonCharacters() {
		if (!commonCharactersAttempted) {
			commonCharactersAttempted = true;
			try (InputStream stream = ChinesePack.open(settings.getContext(), CommonHanCharacters.FILE_NAME)) { commonCharacters = CommonHanCharacters.load(stream); }
			catch (IOException e) { Logger.w("ChinesePredictions", "Common-character table unavailable: " + e.getMessage()); }
		}
		return commonCharacters;
	}

	private void refreshCaches() {
		long current = ChinesePack.getGeneration();
		if (cachedPackGeneration != current) {
			prefixes = null;
			commonCharacters = null;
			prefixesAttempted = false;
			commonCharactersAttempted = false;
			cachedPackGeneration = current;
		}
	}

	private static String reading(String raw) { return raw.substring(0, TextTools.lastIndexOfLatin(raw) + 1); }
	private static String nativeWord(String raw) { return raw.substring(TextTools.lastIndexOfLatin(raw) + 1); }
}
