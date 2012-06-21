package com.sio.safeclient.utils;

import junit.framework.Assert;

import org.junit.Test;

import com.sio.safeclient.benchmark.BenchmarkResult;
import com.sio.safeclient.benchmark.ThreadedBenchmark;
import com.sio.safeclient.benchmark.ThreadedBenchmark.Operation;

public class StatisticTest {

	@Test
	public void statisticConcurrencyTest() {
	
		int operations = 1000000;
		int maxThreadCount = 25; 
			
		for (int i = 0; i < maxThreadCount; i += 1) {
			
			final Statistic statistic = new Statistic();
			
			ThreadedBenchmark benchmark = 
					new ThreadedBenchmark(
							i + 1,
							i + 1, 
							operations,
							new Operation() {

							public void perform() {
								
								statistic.update(Math.random());
								
							}
				
			});
			
			
			BenchmarkResult result = benchmark.run();

			Assert.assertEquals(operations, statistic.getCount());
			
			System.out.println("Average : " + statistic.getAverage());
			System.out.println("Std Dev : " + statistic.getStandardDeviation());
			
			System.out.println("Threads " + (i+1) + "," + result.toCSVLine());
		}
	}
	
}
