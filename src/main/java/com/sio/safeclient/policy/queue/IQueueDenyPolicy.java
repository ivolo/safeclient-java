package com.sio.safeclient.policy.queue;

public interface IQueueDenyPolicy {

	public boolean canQueue(int currentSize);
	
}
