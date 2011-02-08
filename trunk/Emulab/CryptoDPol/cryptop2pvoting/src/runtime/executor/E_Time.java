package runtime.executor;

import runtime.Time;

public class E_Time implements Time {

	public long getCurrentTime() {
		return System.currentTimeMillis();
	}

}
