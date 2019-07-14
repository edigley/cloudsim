package com.edigley.cloudsim.util;

import com.edigley.oursim.dispatchableevents.jobevents.JobEventDispatcher;
import com.edigley.oursim.dispatchableevents.jobevents.JobEventListener;
import com.edigley.oursim.dispatchableevents.taskevents.TaskEventDispatcher;
import com.edigley.oursim.dispatchableevents.taskevents.TaskEventListener;

public class EventListenerUtils {

	public static void registerJobEventListeners(JobEventListener... listeners) {
		for (JobEventListener jobEventListener : listeners) {	
			JobEventDispatcher.getInstance().addListener(jobEventListener);
		}
	}

	public static void deregisterJobEventListeners(JobEventListener... listeners) {
		for (JobEventListener jobEventListener : listeners) {	
			JobEventDispatcher.getInstance().removeListener(jobEventListener);
		}
	}
	
	public static void registerTaskEventListeners(TaskEventListener... listeners) {
		for (TaskEventListener taskEventListener : listeners) {	
			TaskEventDispatcher.getInstance().addListener(taskEventListener);
		}
	}
	
	public static void deregisterTaskEventListeners(TaskEventListener... listeners) {
		for (TaskEventListener taskEventListener : listeners) {	
			TaskEventDispatcher.getInstance().removeListener(taskEventListener);
		}
	}
	
}
