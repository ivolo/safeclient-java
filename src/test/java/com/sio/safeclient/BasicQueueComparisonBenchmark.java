package com.sio.safeclient;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.sio.safeclient.benchmark.BenchmarkResult;
import com.sio.safeclient.benchmark.ThreadedBenchmark;
import com.sio.safeclient.benchmark.ThreadedBenchmark.Operation;

public class BasicQueueComparisonBenchmark {
	
	
	@Test
	public void linkedBlockQueue() {
		
		runAddTest(new QueueFactory() {
			
			public Queue<String> create() {
				return new LinkedBlockingQueue<String>();
			}
		});
	}
	
	
	@Test
	public void concurrentLinkedQueue() {
		
		runAddTest(new QueueFactory() {
			
			public Queue<String> create() {
				return new ConcurrentLinkedQueue<String>();
			}
		});
	}
	
	interface QueueFactory {
		Queue<String> create();
	}
	
	public void runAddTest(QueueFactory queueFactory) {
		
		int valuesToAdd = 1000000;
		int maxThreadCount = 25;
			
		for (int i = 0; i < maxThreadCount; i += 1) {
			
			final Queue<String> queue = queueFactory.create();
			
			ThreadedBenchmark benchmark = 
					new ThreadedBenchmark(
							i + 1,
							i + 1, 
							valuesToAdd,
							new Operation() {

							public void perform() {
								queue.add(RandomStringUtils.randomAlphanumeric(20));

								// this won't ever finish on ConcurrentLinkedQueue
								// since it has to traverse the entire thing
								//queue.size();
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
