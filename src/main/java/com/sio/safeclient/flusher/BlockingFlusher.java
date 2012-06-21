package com.sio.safeclient.flusher;

import java.util.List;

import com.sio.safeclient.BatchedOperation;

public class BlockingFlusher implements IFlusher {


	public boolean canFlush() {
		return true;
	}
	
	public <M> void flush(BatchedOperation<M> operation, List<M> batch) {
		operation.flush(batch);
	}

	public void close() {
		// do nothing
	}

}
