package io.github.sspanak.tt9.languages;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SimplifiedChineseDictionaryTest {
	private static final Set<String> TARGETS = Set.of("说", "行", "重", "绿", "女", "吕", "略", "虐", "你", "米", "谧", "你好", "米糕", "输入法");
	private static final Map<String, Integer> FREQUENCIES = new HashMap<>();


	@BeforeClass
	public static void readDictionary() throws IOException {
		String path = System.getProperty("tt9.chineseDictionary");
		assertNotNull("The test task must provide the Chinese dictionary path", path);
		try (Stream<String> lines = Files.lines(Paths.get(path))) {
			lines.forEach(line -> {
				String[] fields = line.split("\t", 3);
				if (fields.length >= 2 && TARGETS.contains(fields[0])) {
					FREQUENCIES.put(fields[0] + "\t" + fields[1], fields.length == 3 ? Integer.parseInt(fields[2]) : 0);
				}
			});
		}
	}


	@Test
	public void commonPolyphonicReadingsAreAvailable() {
		assertTrue(FREQUENCIES.containsKey("说\tSHUO"));
		assertTrue(FREQUENCIES.containsKey("行\tXING"));
		assertTrue(FREQUENCIES.containsKey("重\tZHONG"));
		assertTrue(FREQUENCIES.containsKey("绿\tLV"));
	}


	@Test
	public void existingAlternativeReadingsRemainAvailable() {
		assertTrue(FREQUENCIES.containsKey("说\tSHUI"));
		assertTrue(FREQUENCIES.containsKey("行\tHANG"));
		assertTrue(FREQUENCIES.containsKey("重\tCHONG"));
		assertTrue(FREQUENCIES.containsKey("绿\tLU"));
	}


	@Test
	public void umlautUsesTheExistingVSpelling() {
		assertTrue(FREQUENCIES.containsKey("女\tNV"));
		assertTrue(FREQUENCIES.containsKey("吕\tLV"));
		assertTrue(FREQUENCIES.containsKey("绿\tLV"));
		assertTrue(FREQUENCIES.containsKey("略\tLVE"));
		assertTrue(FREQUENCIES.containsKey("虐\tNVE"));
	}


	@Test
	public void defaultGreetingOutranksItsLessCommonTextonym() {
		assertTrue(frequency("你好", "NIHAO") > frequency("米糕", "MIGAO"));
	}


	@Test
	public void commonCharactersOutrankRareTextonymsAcrossReadings() {
		assertTrue(frequency("你", "NI") > frequency("米", "MI"));
		assertTrue(frequency("米", "MI") > frequency("谧", "MI"));
	}


	@Test
	public void commonPhrasesHaveFactoryWeightsWithoutLearning() {
		assertTrue(frequency("输入法", "SHURUFA") > 0);
	}


	private int frequency(String word, String pinyin) {
		Integer weight = FREQUENCIES.get(word + "\t" + pinyin);
		assertNotNull("Missing reading: " + word + " [" + pinyin + "]", weight);
		return weight;
	}
}
