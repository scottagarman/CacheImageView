package com.scottagarman.android.imageloadersample.imageloader.operations;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Operation.java
 *
 * Extend this class for ops. Uses shared thread pool.
 * TODO : get default thread pool size for device
 */
public class Operation implements Serializable {
	private static final long serialVersionUID = -3389953904685901495L;
    private static final int THREAD_POOL_MAX = 5;

	// Static thread pool for execution
	private static ExecutorService operationPool;

	// handle on thread process
	private Future<Runnable> future;
	
	public Operation(){}
	
	/**
	 * Gets static operation pool.
	 */
	protected static ExecutorService getOperationPool() {
		if(operationPool == null)
			operationPool = Executors.newFixedThreadPool(THREAD_POOL_MAX);
		return operationPool;
	}
	
	/**
	 * Adds operation runnable to thread pool and begins thread tasks.
	 */
	protected void beginOperation(Runnable operation) {
		future = (Future<Runnable>)Operation.getOperationPool().submit(operation);
	}
	
	/**
	 * Cancels operation.
	 */
	public void cancel() {
		if(future != null){
			future.cancel(true);
		}
    }
}
