package com.sio.safeclient;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.sio.safeclient.benchmark.BenchmarkResult;
import com.sio.safeclient.benchmark.ThreadedBenchmark;
import com.sio.safeclient.benchmark.ThreadedBenchmark.Operation;
import com.sio.safeclient.queue.IBatchQueue;
import com.sio.safeclient.queue.LockingQueue;
import com.sio.safeclient.queue.NonLockingQueue;

public class BatchQueueBenchmark {

	@Test
	public void lockingQueue() {
		
		runAddTest(new BatchQueueFactory() {
			
			public IBatchQueue<String> create() {
				return new LockingQueue<String>();
			}
		});
	}

	
	@Test
	public void nonLockingQueue() {
		
		runAddTest(new BatchQueueFactory() {
			
			public IBatchQueue<String> create() {
				return new NonLockingQueue<String>();
			}
		});
	}
	
	interface BatchQueueFactory {
		IBatchQueue<String> create();
	}
	
	public void runAddTest(BatchQueueFactory batchQueueFactory) {
		
		int valuesToAdd = 1000000;
		int maxThreadCount = 25;
			
		for (int i = 0; i < maxThreadCount; i += 1) {
			
			final IBatchQueue<String> queue = batchQueueFactory.create();
			
			ThreadedBenchmark benchmark = 
					new ThreadedBenchmark(
							i + 1,
							i + 1, 
							valuesToAdd,
							new Operation() {

							public void perform() {
								int size = queue.add(RandomStringUtils.randomAlphanumeric(20));
								
								if (size % 50 == 0) {
									List<String> flushed = queue.flush(50);
									if (flushed != null) System.out.println("Flushed " + flushed.size() + " and have " + queue.size() + " left in batch queue.");
									
								}
							}
				
			});
			
			BenchmarkResult result = benchmark.run();
			
			queue.clear();
			
			String line = StringUtils.join(Arrays.asList(
					"" + (i+1),
					"" + result.getMin(),
					"" + result.getMax(),
					"" + result.getAverage()), ",");
			
			System.out.println(line);
		}
		
	}
}
