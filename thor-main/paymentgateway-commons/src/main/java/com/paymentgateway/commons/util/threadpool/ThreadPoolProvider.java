package com.paymentgateway.commons.util.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.paymentgateway.commons.util.ConfigurationConstants;

public class ThreadPoolProvider {

	// int1= core pool size, int2 = max pool size, 0L(long) keeplAliveTime,time to keepAlive units, BlockingQueue, RejectedExecutionHandler
	//TODO  determine whether to use LinkedBlockingQueue or ArrayBlockingQueue
	private static int corePoolSize ;
	private static int maxPoolSize ;
	private static long maxThreadAliveTime;
	
	public static void loadConfig(){
		corePoolSize = Integer.parseInt(ConfigurationConstants.THREAD_POOL_CORE_POOL_SIZE.getValue());
		maxPoolSize = Integer.parseInt(ConfigurationConstants.THREAD_POOL_MAX_POOL_SIZE.getValue());
		maxThreadAliveTime = Long.parseLong(ConfigurationConstants.THREAD_POOL_MAX_THREAD_ALIVE_TIME.getValue());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ExecutorService getExecutorService(){
		loadConfig();
		ExecutorService executorService =new ThreadPoolExecutor(corePoolSize, maxPoolSize, maxThreadAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), new ThreadPoolExecutor.CallerRunsPolicy());
		return executorService;
	}
}
