package com.edigley.cloudsim.entities;

import com.edigley.oursim.entities.Processor;
import com.edigley.oursim.entities.Task;
import com.edigley.oursim.util.BidirectionalMap;

public class TaskRunManager {

	private BidirectionalMap<Processor, Task> processor2Task;
	
	public TaskRunManager() {
		this.processor2Task = new BidirectionalMap<Processor, Task>();
	}

	public void assignTask(Processor processor, Task task) {
		this.processor2Task.put(processor, task);
	}

	public Processor deassignProcessor(Task sourceTask) {
		Processor processor = this.processor2Task.getKey(sourceTask);
		this.processor2Task.remove(processor);
		return processor;
	}
	
}
