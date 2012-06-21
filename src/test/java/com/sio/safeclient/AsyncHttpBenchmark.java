package com.sio.safeclient;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.sio.safeclient.benchmark.BenchmarkResult;
import com.sio.safeclient.benchmark.ThreadedBenchmark;
import com.sio.safeclient.benchmark.ThreadedBenchmark.Operation;
import com.sio.safeclient.flusher.IFlusher;
import com.sio.safeclient.flusher.ThreadPoolFlusher;

public class AsyncHttpBenchmark {

	@Test
	public void testRequests() {

		int valuesToAdd = 1000000;
		int maxThreadCount = 25;
			
		for (int i = 0; i < maxThreadCount; i += 1) {
			
			ThreadedBenchmark benchmark = 
					new ThreadedBenchmark(
							i + 1,
							i + 1, 
							valuesToAdd,
							new Operation() {

							public void perform() {
								
								operation.perform(RandomStringUtils.randomAlphanumeric(20));
								
							}
				
			});
			
			BenchmarkResult result = benchmark.run();
			
			String line = StringUtils.join(Arrays.asList(
					"" + (i+1),
					"" + result.getMin(),
					"" + result.getMax(),
					"" + result.getAverage()), ",");
			
			System.out.println(line);
		}
	}
	
	private AsyncHttpBatchedOperation<String> operation = 
			new AsyncHttpBatchedOperation<String>() {

		protected int getMaxFlushAmount() {
			return 500;
		}
		
		protected IFlusher createFlusher() {
			return new ThreadPoolFlusher(1, 1, 10);
		}
		
		@Override
		public Request buildRequest(List<String> batch) {
			
			String payload = StringUtils.join(batch, "-");
			
			return new RequestBuilder()
				.setMethod("POST")
				.setBody(payload)
				.setUrl("http://127.0.0.1/woo")
				.build();
		}

	};
	
}
