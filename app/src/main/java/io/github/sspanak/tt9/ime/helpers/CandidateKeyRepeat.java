package io.github.sspanak.tt9.ime.helpers;

/** Rate limits native repeat events without timers or work continuing after key release. */
public final class CandidateKeyRepeat {
	private int key;
	private long lastDispatch;

	public boolean dispatch(int keyCode, long now, int interval) {
		if (keyCode != key) {
			key = keyCode;
			lastDispatch = now;
			return true;
		}
		if (now - lastDispatch < Math.max(1, interval)) return false;
		lastDispatch = now;
		return true;
	}

	public boolean release(int keyCode) {
		if (key == 0 || keyCode != key) return false;
		reset();
		return true;
	}

	public void reset() { key = 0; lastDispatch = 0; }
}
