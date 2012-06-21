
package com.sio.safeclient.queue;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class NonLockingQueue<T> implements IBatchQueue<T> {

	private ConcurrentLinkedQueue<T> queue;
	private AtomicBoolean lock;
	private AtomicInteger count;
	
	public NonLockingQueue() {
		queue = new ConcurrentLinkedQueue<T>();
		lock = new AtomicBoolean(false);
		count = new AtomicInteger(0);
	}
	
	public int add(T item) {
		queue.add(item);
		
		int size = count.addAndGet(1);
		
		return size;
	}
	
	public int size() {
		return count.get();
	}
	
	public List<T> flush(int maxAmount) {
		
		List<T> list = null;
		
		if (lock.compareAndSet(false, true)) {
			
			int flushed = 0;
			
			list = new LinkedList<T>(); 
			T item = queue.poll();
			while (item != null && flushed <= maxAmount) {
				list.add(item);
				flushed += 1;
				item = queue.poll();
			}
			
			lock.set(false);
			
			// subtract the amount we just removed
			count.addAndGet(-list.size());
		}
		
		return list;
	}

	public void clear() {
		queue.clear();
	}
	
}
