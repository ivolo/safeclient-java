package com.sio.safeclient;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sio.safeclient.flusher.IFlusher;
import com.sio.safeclient.flusher.ThreadPoolFlusher;
import com.sio.safeclient.policy.flush.GreaterThanFlushPolicy;
import com.sio.safeclient.policy.flush.IFlushPolicy;
import com.sio.safeclient.policy.flush.LastFlushedTimePolicy;
import com.sio.safeclient.policy.queue.GreaterThanCapacityPolicy;
import com.sio.safeclient.policy.queue.IQueueDenyPolicy;
import com.sio.safeclient.queue.IBatchQueue;
import com.sio.safeclient.queue.NonLockingQueue;
import com.sio.safeclient.utils.RateLimit;
import com.sio.safeclient.utils.Statistics;

public abstract class BatchedOperation<M> {
	
	private static final Logger logger = 
			LoggerFactory.getLogger(BatchedOperation.class);
	
	protected Iterable<IFlushPolicy> flushPolicies = createFlushPolicies();
	protected Iterable<IQueueDenyPolicy> denyPolicies = createCapacityPolicies();
	
	private IFlusher flusher = createFlusher();
	
	private IBatchQueue<M> queue = createQueue();
	
	protected RateLimit errorLoggingRateLimit = new RateLimit(1, 1000);
	protected RateLimit statisticsLoggingRateLimit = new RateLimit(1, 5000);
	
	private DateTime lastFlush;
	
	protected Statistics statistics = new Statistics();
	
	
	public abstract boolean canFlush();
	
	/**
	 * Called when a flush needs to happen.
	 * @param batch The batch to flush.
	 * @return 
	 */
	public abstract void flush(List<M> batch);

	public boolean perform(M message) {
		
		boolean canEnqueue = true;
		
		int currentSize = queue.size();
		
		for (IQueueDenyPolicy denyPolicy : denyPolicies) {
			
			if (!denyPolicy.canQueue(currentSize)) {
				
				canEnqueue = false;
				
				statistics.update("Queue over Capacity => Denied Message", 1);
				
				break;
			}
		}
		
		if (canEnqueue) { 
			
			currentSize = queue.add(message);

			statistics.update("Enqueued Message", 1);
			
		} else {
			
			if (errorLoggingRateLimit.canPerform()) {
				
				logger.warn("Operation batch queue is full, and flushing operations are also " + 
						"pending. Choosing to drop this message from the queue.");
			}
		}
		
		if (canFlush()) {
		
			for (IFlushPolicy flushPolicy : flushPolicies) {
				
				if (flushPolicy.shouldFlush(currentSize, lastFlush)) {
					
					statistics.update("Asking to Flush", 1);
					triggerFlush();
					break;
				}
				
			}
			
		} else {
			
			if (errorLoggingRateLimit.canPerform()) {
				logger.warn("Batched operation can't flush.");
			}
			
			statistics.update("Batched Operation Can't Flush", 1);
		}
		
		statistics.update("Queue Size", queue.size());
		
		// should we log the statistics?
		if (shouldLogStatistics() && statisticsLoggingRateLimit.canPerform()) {
			logger.debug(statistics.toString());
		}
		
		return canEnqueue;	
	}
	
	protected void triggerFlush() {
		
		if (flusher.canFlush()) {
			
			int maxAmount = getMaxFlushAmount();
			
			List<M> batch = queue.flush(maxAmount);
			
			if (batch != null) {
				
				flusher.flush(this, batch);
				
				statistics.update("Flushes", 1);
				
				statistics.update("Flushed Batched Size", batch.size());
			}
			
			lastFlush = DateTime.now();
			
		} else {
			
			statistics.update("Flusher Can't Flush", 1);
		}
	}
	
	public boolean shouldLogStatistics() {
		return true;
	}
	
	protected int getMaxFlushAmount() {
		return 50;
	}
	
	protected int getMaxQueueSize() {
		return getMaxFlushAmount() * 20;
	}
	
	protected Iterable<IFlushPolicy> createFlushPolicies() {
		
		return Arrays.asList(		
			new LastFlushedTimePolicy(1000 * 10),
			new GreaterThanFlushPolicy(getMaxFlushAmount())
		);
		
	}
	
	protected Iterable<IQueueDenyPolicy> createCapacityPolicies() {
		
		List<IQueueDenyPolicy> policies = 
				new LinkedList<IQueueDenyPolicy>();
		
		policies.add(new GreaterThanCapacityPolicy(getMaxQueueSize()));
		
		return policies;
	}
	
	protected IFlusher createFlusher() {
		return new ThreadPoolFlusher(0, 1, 100);
	}
	
	protected IBatchQueue<M> createQueue() {
		return new NonLockingQueue<M>();
	}
	
	public void close() {
		if (flusher != null) flusher.close();
		if (queue != null) queue.clear();
	}
	
	
}
