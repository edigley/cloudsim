package com.edigley.cloudsim.policy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.edigley.oursim.dispatchableevents.Event;
import com.edigley.oursim.dispatchableevents.EventListener;
import com.edigley.oursim.entities.Job;
import com.edigley.oursim.entities.Machine;
import com.edigley.oursim.entities.Peer;
import com.edigley.oursim.entities.Processor;
import com.edigley.oursim.entities.Task;
import com.edigley.oursim.entities.TaskExecution;
import com.edigley.oursim.io.input.workload.Workload;
import com.edigley.oursim.policy.JobSchedulerPolicy;
import com.edigley.oursim.util.BidirectionalMap;
import com.edigley.cloudsim.dispatchableevents.spotinstances.SpotPriceEventListener;
import com.edigley.cloudsim.entities.BidValue;
import com.edigley.cloudsim.entities.SpotValue;
import com.edigley.cloudsim.io.input.SpotPrice;
import com.edigley.cloudsim.simulationevents.SpotInstancesActiveEntity;

public class SpotInstancesScheduler extends SpotInstancesActiveEntity implements JobSchedulerPolicy, SpotPriceEventListener {

	private BidirectionalMap<BidValue, Machine> allocatedMachines;

	private BidirectionalMap<Machine, Task> machine2Task;

	private Map<Task, Double> accountedCost;

	private SpotPrice currentSpotPrice;

	private long nextMachineId = 0;

	private final Peer thePeer;

	private long machineSpeed;

	public SpotInstancesScheduler(Peer thePeer, SpotPrice initialSpotPrice, long machineSpeed) {
		this.machine2Task = new BidirectionalMap<Machine, Task>();
		this.allocatedMachines = new BidirectionalMap<BidValue, Machine>();
		this.accountedCost = new HashMap<Task, Double>();
		this.currentSpotPrice = initialSpotPrice;
		this.machineSpeed = machineSpeed;
		this.thePeer = thePeer;
	}

	public void schedule() {
		Iterator<Entry<BidValue, Machine>> iterator = allocatedMachines.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<BidValue, Machine> entry = iterator.next();
			BidValue bid = entry.getKey();
			if (bid.getValue() < currentSpotPrice.getPrice()) {
				Machine machine = entry.getValue();
				Task Task = machine2Task.remove(machine);
				iterator.remove();
				addPreemptedTaskEvent(getCurrentTime(), Task);
			}
		}
	}

	public void addJob(Job job) {
		for (Task Task : job.getTasks()) {
			this.addSubmitTaskEvent(this.getCurrentTime(), Task);
		}
	}

	public void addWorkload(Workload workload) {
		throw new RuntimeException();
	}

	public boolean isFinished() {
		throw new RuntimeException();
	}

	// B-- beginning of implementation of SpotPriceEventListener

	public void newSpotPrice(Event<SpotValue> spotValueEvent) {
		SpotPrice newSpotPrice = (SpotPrice) spotValueEvent.getSource();
		this.currentSpotPrice = newSpotPrice;
	}

	public void fullHourCompleted(Event<SpotValue> spotValueEvent) {
		BidValue bidValue = (BidValue) spotValueEvent.getSource();
		Task Task = bidValue.getTask();
		if (!Task.isFinished()) {
			assert this.accountedCost.containsKey(Task);
			double totalCost = this.accountedCost.get(Task) + currentSpotPrice.getPrice();
			this.accountedCost.put(Task, totalCost);
			Task.setCost(totalCost);
			// estava sem essa linha
			this.addFullHourCompletedEvent(bidValue);
		} else {
			// já terminou antes de completar uma hora.
		}
	}

	// E-- End of implementation of SpotPriceEventListener

	// B-- beginning of implementation of JobEventListener

	public void jobSubmitted(Event<Job> jobEvent) {
		this.addJob(jobEvent.getSource());
	}

	public void jobPreempted(Event<Job> jobEvent) {
	}

	public void jobFinished(Event<Job> jobEvent) {
	}

	public void jobStarted(Event<Job> jobEvent) {
	}

	// E-- end of implementation of JobEventListener

	// B-- beginning of implementation of TaskEventListener

	public void taskSubmitted(Event<Task> taskEvent) {
		Task Task = taskEvent.getSource();
		assert this.machine2Task.size() == this.allocatedMachines.size();
		if (Task.getBidValue() >= currentSpotPrice.getPrice()) {
			String machineName = "m_" + nextMachineId++;
			Machine newMachine = new Machine(machineName, machineSpeed);
			BidValue bidValue = new BidValue(machineName, getCurrentTime(), Task.getBidValue(), Task);
			this.addFullHourCompletedEvent(bidValue);
			this.machine2Task.put(newMachine, Task);
			this.allocatedMachines.put(bidValue, newMachine);
			long currentTime = getCurrentTime();
			Processor defaultProcessor = newMachine.getDefaultProcessor();
			Task.setTaskExecution(new TaskExecution(Task, defaultProcessor, currentTime));
			Task.setStartTime(currentTime);
			Task.setTargetPeer(thePeer);
			this.accountedCost.put(Task, 0.0);
			this.addStartedTaskEvent(Task);
		}
		assert this.machine2Task.size() == this.allocatedMachines.size();
	}

	public void taskFinished(Event<Task> taskEvent) {
		Machine machine = this.machine2Task.getKey(taskEvent.getSource());
		this.machine2Task.remove(machine);
		BidValue bidValue = this.allocatedMachines.getKey(machine);
		this.allocatedMachines.remove(bidValue);
		Task Task = bidValue.getTask();
		assert this.accountedCost.containsKey(Task);
		double totalCost = this.accountedCost.get(Task) + currentSpotPrice.getPrice();
		this.accountedCost.put(Task, totalCost);
		Task.setCost(totalCost);
	}

	public void taskStarted(Event<Task> taskEvent) {
	}

	public void taskPreempted(Event<Task> taskEvent) {
	}

	public void taskCancelled(Event<Task> taskEvent) {
	}

	// E-- end of implementation of TaskEventListener

	// B-- beginning of implementation of SpotPriceEventListener

	public void workerAvailable(Event<String> workerEvent) {
	}

	public void workerUnavailable(Event<String> workerEvent) {
	}

	public void workerUp(Event<String> workerEvent) {
	}

	public void workerDown(Event<String> workerEvent) {
	}

	public void workerIdle(Event<String> workerEvent) {
	}

	public void workerRunning(Event<String> workerEvent) {
	}

	public int getQueueSize() {
		return -1;
	}

	public int getNumberOfRunningTasks() {
		return allocatedMachines.size();
	}

	// E-- end of implementation of SpotPriceEventListener

	public int compareTo(EventListener o) {
		return this.hashCode() - o.hashCode();
	}

	public void stop() {
	}

}
