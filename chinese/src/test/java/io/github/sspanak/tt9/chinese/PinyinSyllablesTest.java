package io.github.sspanak.tt9.chinese;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PinyinSyllablesTest {
	@Test public void separatesXianFromXiAnByHanLength() {
		assertEquals(List.of("XIAN"), PinyinSyllables.segment("xian", 1));
		assertEquals(List.of("XI'AN"), PinyinSyllables.segment("xian", 2));
		assertEquals(List.of("XIAN"), PinyinSyllables.choicesForStoredWord("XIAN先"));
		assertEquals(List.of("XI'AN"), PinyinSyllables.choicesForStoredWord("XIAN西安"));
	}

	@Test public void selectedBoundarySeparatesNativeCandidateSets() {
		assertTrue(PinyinSyllables.matchesSelectedReading("XIAN先", "XIAN"));
		assertFalse(PinyinSyllables.matchesSelectedReading("XIAN西安", "XIAN"));
		assertTrue(PinyinSyllables.matchesSelectedReading("XIAN西安", "XI'AN"));
		assertFalse(PinyinSyllables.matchesSelectedReading("XIAN先", "XI'AN"));
	}

	@Test public void doesNotGuessEqualLengthBoundaries() {
		assertTrue(PinyinSyllables.segment("changan", 2).contains("CHANG'AN"));
		assertTrue(PinyinSyllables.segment("changan", 2).contains("CHAN'GAN"));
		assertEquals(List.of("CHANGAN"), PinyinSyllables.choicesForStoredWord("CHANGAN长安"));
		assertTrue(PinyinSyllables.matchesSelectedReading("CHANGAN长安", "CHANGAN"));
	}

	@Test public void acceptsExplicitUserSyllablesAndUmlautSpellings() {
		assertEquals("NV'ER", PinyinSyllables.parseUserReading("nü er", 2));
		assertEquals("LV", PinyinSyllables.parseUserReading("lu:", 1));
		assertEquals("XI'AN", PinyinSyllables.parseUserReading("xi’an", 2));
		assertEquals("NI'HAO", PinyinSyllables.parseUserReading("nihao", 2));
	}

	@Test public void mapsOnlyLettersToDigits() {
		assertEquals("9426", PinyinSyllables.getSequence("xi'an"));
		assertEquals("6442662", PinyinSyllables.getSequence("ni hao ma"));
		assertEquals("NV", PinyinSyllables.compact("nü"));
	}

	@Test(expected = IllegalArgumentException.class) public void ambiguousUserReadingNeedsExplicitBoundaries() { PinyinSyllables.parseUserReading("changan", 2); }
	@Test(expected = IllegalArgumentException.class) public void rejectsWrongSyllableCount() { PinyinSyllables.parseUserReading("ni hao", 1); }
	@Test(expected = IllegalArgumentException.class) public void rejectsUnknownSyllable() { PinyinSyllables.parseUserReading("xyz", 1); }
	@Test(expected = IllegalArgumentException.class) public void rejectsToneDigitsRatherThanTreatingThemAsKeys() { PinyinSyllables.parseUserReading("ni3 hao3", 2); }
	@Test(expected = IllegalArgumentException.class) public void rejectsEmptySyllables() { PinyinSyllables.parseUserReading("xi''an", 2); }

	@Test public void segmentationIsBoundedAndDeterministic() {
		String reading = "XIAN".repeat(12);
		List<String> result = PinyinSyllables.segment(reading);
		assertFalse(result.isEmpty());
		assertTrue(result.size() <= 32);
		assertEquals(result, PinyinSyllables.segment(reading));
		assertEquals(List.of(), PinyinSyllables.segment("X".repeat(193)));
	}

	@Test public void allSyllablesAreSortedAndImmutable() {
		List<String> all = PinyinSyllables.all();
		ArrayList<String> sorted = new ArrayList<>(all);
		sorted.sort(String::compareTo);
		assertEquals(sorted, all);
		assertTrue(all.containsAll(List.of("ZHONG", "XIAN", "XI", "AN", "NV", "LVE")));
		try { all.add("INVALID"); fail("List must be immutable"); } catch (UnsupportedOperationException expected) {}
	}
}
