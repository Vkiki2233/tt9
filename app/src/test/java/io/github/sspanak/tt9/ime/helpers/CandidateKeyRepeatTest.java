package io.github.sspanak.tt9.ime.helpers;

import org.junit.Test;
import static org.junit.Assert.*;

public class CandidateKeyRepeatTest {
	@Test public void rapidNativeEventsRespectSelectedInterval() {
		CandidateKeyRepeat gate = new CandidateKeyRepeat();
		assertTrue(gate.dispatch(22, 1000, 240));
		assertFalse(gate.dispatch(22, 1030, 240));
		assertFalse(gate.dispatch(22, 1239, 240));
		assertTrue(gate.dispatch(22, 1240, 240));
		assertFalse(gate.dispatch(22, 1300, 240));
	}

	@Test public void releaseIsConsumedOnceAndNextHoldStartsImmediately() {
		CandidateKeyRepeat gate = new CandidateKeyRepeat();
		assertFalse(gate.release(0));
		assertFalse(gate.release(22));
		assertTrue(gate.dispatch(22, 1000, 240));
		assertFalse(gate.release(21));
		assertTrue(gate.release(22));
		assertFalse(gate.release(22));
		assertTrue(gate.dispatch(22, 1050, 240));
	}

	@Test public void DirectionChangeAndNewSessionDoNotInheritDelay() {
		CandidateKeyRepeat gate = new CandidateKeyRepeat();
		assertTrue(gate.dispatch(22, 1000, 400));
		assertTrue(gate.dispatch(21, 1030, 400));
		assertFalse(gate.dispatch(21, 1060, 400));
		gate.reset();
		assertFalse(gate.release(21));
		assertTrue(gate.dispatch(21, 1061, 400));
	}

	@Test public void ExpandedAndSingleRowCanUseDifferentSpeeds() {
		CandidateKeyRepeat slow = new CandidateKeyRepeat(), fast = new CandidateKeyRepeat();
		int slowSteps = 0, fastSteps = 0;
		for (int time = 0; time <= 1200; time += 30) {
			if (slow.dispatch(22, time, 240)) slowSteps++;
			if (fast.dispatch(22, time, 60)) fastSteps++;
		}
		assertEquals(6, slowSteps);
		assertEquals(21, fastSteps);
	}
}
