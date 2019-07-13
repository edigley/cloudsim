package br.edu.ufcg.lsd.spotinstancessimulator.policy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import br.edu.ufcg.lsd.oursim.dispatchableevents.Event;
import br.edu.ufcg.lsd.oursim.dispatchableevents.EventListener;
import br.edu.ufcg.lsd.oursim.entities.Job;
import br.edu.ufcg.lsd.oursim.entities.Machine;
import br.edu.ufcg.lsd.oursim.entities.Peer;
import br.edu.ufcg.lsd.oursim.entities.Processor;
import br.edu.ufcg.lsd.oursim.entities.Task;
import br.edu.ufcg.lsd.oursim.entities.TaskExecution;
import br.edu.ufcg.lsd.oursim.io.input.workload.Workload;
import br.edu.ufcg.lsd.oursim.policy.JobSchedulerPolicy;
import br.edu.ufcg.lsd.oursim.util.BidirectionalMap;
import br.edu.ufcg.lsd.spotinstancessimulator.dispatchableevents.spotinstances.SpotPriceEventListener;
import br.edu.ufcg.lsd.spotinstancessimulator.entities.BidValue;
import br.edu.ufcg.lsd.spotinstancessimulator.entities.SpotValue;
import br.edu.ufcg.lsd.spotinstancessimulator.io.input.SpotPrice;
import br.edu.ufcg.lsd.spotinstancessimulator.simulationevents.SpotInstancesActiveEntity;

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

	@Override
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

	@Override
	public void addJob(Job job) {
		for (Task Task : job.getTasks()) {
			this.addSubmitTaskEvent(this.getCurrentTime(), Task);
		}
	}

	@Override
	public void addWorkload(Workload workload) {
		throw new RuntimeException();
	}

	@Override
	public boolean isFinished() {
		throw new RuntimeException();
	}

	// B-- beginning of implementation of SpotPriceEventListener

	@Override
	public void newSpotPrice(Event<SpotValue> spotValueEvent) {
		SpotPrice newSpotPrice = (SpotPrice) spotValueEvent.getSource();
		this.currentSpotPrice = newSpotPrice;
	}

	@Override
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

	@Override
	public void jobSubmitted(Event<Job> jobEvent) {
		this.addJob(jobEvent.getSource());
	}

	@Override
	public void jobPreempted(Event<Job> jobEvent) {
	}

	@Override
	public void jobFinished(Event<Job> jobEvent) {
	}

	@Override
	public void jobStarted(Event<Job> jobEvent) {
	}

	// E-- end of implementation of JobEventListener

	// B-- beginning of implementation of TaskEventListener

	@Override
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

	@Override
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

	@Override
	public void taskStarted(Event<Task> taskEvent) {
	}

	@Override
	public void taskPreempted(Event<Task> taskEvent) {
	}

	@Override
	public void taskCancelled(Event<Task> taskEvent) {
	}

	// E-- end of implementation of TaskEventListener

	// B-- beginning of implementation of SpotPriceEventListener

	@Override
	public void workerAvailable(Event<String> workerEvent) {
	}

	@Override
	public void workerUnavailable(Event<String> workerEvent) {
	}

	@Override
	public void workerUp(Event<String> workerEvent) {
	}

	@Override
	public void workerDown(Event<String> workerEvent) {
	}

	@Override
	public void workerIdle(Event<String> workerEvent) {
	}

	@Override
	public void workerRunning(Event<String> workerEvent) {
	}

	@Override
	public int getQueueSize() {
		return -1;
	}

	@Override
	public int getNumberOfRunningTasks() {
		return allocatedMachines.size();
	}

	// E-- end of implementation of SpotPriceEventListener

	@Override
	public int compareTo(EventListener o) {
		return this.hashCode() - o.hashCode();
	}

	@Override
	public void stop() {
	}
}
