package com.sio.safeclient.policy.flush;

import org.joda.time.DateTime;

public class LastFlushedTimePolicy implements IFlushPolicy {

	private long thresholdMs;
	
	public LastFlushedTimePolicy(long thresholdMs) {
		this.thresholdMs = thresholdMs; 
	}
	
	public boolean shouldFlush(int queueSize, DateTime lastFlush) {
		if (lastFlush == null) return true;
		else {
			long since = DateTime.now().getMillis() - lastFlush.getMillis();
			return since >= thresholdMs;
		}
	}
	
}
