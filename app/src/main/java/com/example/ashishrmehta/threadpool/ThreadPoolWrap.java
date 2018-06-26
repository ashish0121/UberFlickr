package com.example.ashishrmehta.threadpool;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolWrap {
	
	private static ThreadPoolWrap instance = null;

	private ExecutorService es = null;
	
	private ThreadPoolWrap() {
		es = Executors.newSingleThreadExecutor();
	}

	public static ThreadPoolWrap getThreadPool() {
		if (instance == null)
			instance = new ThreadPoolWrap();
		return instance;
	}

	public void execute(ArrayList<Callable<String>> task) {
		try {
			es.invokeAll(task);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			es.shutdown();
		}
	}
	
	public void execute(Runnable task) {
		es.execute(task);
	}

	
	public void submit(Callable<String> task) {
		es.submit(task);		
	}
}
