package br.edu.ufcg.lsd.spotinstancessimulator.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.edu.ufcg.lsd.oursim.dispatchableevents.Event;
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

public class SpotInstancesSchedulerLimited extends SpotInstancesScheduler implements JobSchedulerPolicy, SpotPriceEventListener {

	private int limit;

	private Map<String, Integer> allocatedMachinesForUser;

	private BidirectionalMap<BidValue, Machine> allocatedMachines;

	private Map<String, List<Task>> queuedTasks;

	private BidirectionalMap<Machine, Task> machine2Task;

	private Map<Task, Double> accountedCost;

	private SpotPrice currentSpotPrice;

	private long nextMachineId = 0;

	private final Peer thePeer;

	private long machineSpeed;

	public SpotInstancesSchedulerLimited(Peer thePeer, SpotPrice initialSpotPrice, long machineSpeed, int limit) {
		super(thePeer, initialSpotPrice, machineSpeed);
		this.machine2Task = new BidirectionalMap<Machine, Task>();
		this.allocatedMachines = new BidirectionalMap<BidValue, Machine>();
		this.allocatedMachinesForUser = new HashMap<String, Integer>();
		this.queuedTasks = new HashMap<String, List<Task>>();
		this.accountedCost = new HashMap<Task, Double>();
		this.currentSpotPrice = initialSpotPrice;
		this.machineSpeed = machineSpeed;
		this.thePeer = thePeer;
//		this.thePeer.addMachine(new Machine("ReferenceMachine",this.machineSpeed));
		this.limit = limit;
	}

	@Override
	public void schedule() {
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
			String userId = Task.getSourceJob().getUserId();
			if (!allocatedMachinesForUser.containsKey(userId)) {
				this.allocatedMachinesForUser.put(userId, 0);
			}
			if (this.allocatedMachinesForUser.get(userId) <= limit) {
				String machineName = "m_" + nextMachineId++;
				Machine newMachine = new Machine(machineName, machineSpeed);
				BidValue bidValue = new BidValue(machineName, getCurrentTime(), Task.getBidValue(), Task);
				this.addFullHourCompletedEvent(bidValue);
				this.machine2Task.put(newMachine, Task);
				this.allocatedMachines.put(bidValue, newMachine);

				this.allocatedMachinesForUser.put(userId, this.allocatedMachinesForUser.get(userId) + 1);
				long currentTime = getCurrentTime();
				Processor defaultProcessor = newMachine.getDefaultProcessor();
				Task.setTaskExecution(new TaskExecution(Task, defaultProcessor, currentTime));
				Task.setStartTime(currentTime);
				Task.setTargetPeer(thePeer);
				this.accountedCost.put(Task, 0.0);
				this.addStartedTaskEvent(Task);
			} else {
				if (!queuedTasks.containsKey(userId)) {
					this.queuedTasks.put(userId, new ArrayList<Task>());
				}
				this.queuedTasks.get(userId).add(Task);
			}
		} else {
			System.out.println("---------------------");
		}
		assert this.machine2Task.size() == this.allocatedMachines.size();
	}

	@Override
	public void taskFinished(Event<Task> taskEvent) {
		String userId = taskEvent.getSource().getSourceJob().getUserId();
		List<Task> queuedTaskFromUser = queuedTasks.get(userId);
		if (queuedTaskFromUser == null || queuedTaskFromUser.isEmpty()) {
			Machine machine = this.machine2Task.getKey(taskEvent.getSource());
			this.machine2Task.remove(machine);
			BidValue bValue = this.allocatedMachines.getKey(machine);
			this.allocatedMachines.remove(bValue);
			Task Task = bValue.getTask();
			double totalCost = this.accountedCost.get(Task) + currentSpotPrice.getPrice();
			this.accountedCost.put(Task, totalCost);
			Task.setCost(totalCost);
			this.allocatedMachinesForUser.put(userId, this.allocatedMachinesForUser.get(userId) - 1);
		} else {
			Machine machine = this.machine2Task.getKey(taskEvent.getSource());
			this.machine2Task.remove(machine);
			BidValue bValue = this.allocatedMachines.getKey(machine);
			this.allocatedMachines.remove(bValue);
			Task queuedTask = queuedTaskFromUser.remove(0);
			this.machine2Task.put(machine, queuedTask);
			BidValue bidValue = new BidValue(machine.getName(), getCurrentTime(), queuedTask.getBidValue(), queuedTask);
			this.allocatedMachines.put(bidValue, machine);
			long currentTime = getCurrentTime();
			Processor defaultProcessor = machine.getDefaultProcessor();
			queuedTask.setTaskExecution(new TaskExecution(queuedTask, defaultProcessor, currentTime));
			queuedTask.setStartTime(currentTime);
			queuedTask.setTargetPeer(thePeer);
			this.accountedCost.put(queuedTask, 0.0);
			this.addStartedTaskEvent(queuedTask);
			this.addComplementaryHourCompletedEvent(bidValue, bValue);
		}

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

	// E-- end of implementation of SpotPriceEventListener

	@Override
	public int getQueueSize() {
		Collection<List<Task>> allEnqueuedTasks = queuedTasks.values();
		int queueSize = 0;
		for (List<Task> enqueuedTask : allEnqueuedTasks) {
			queueSize += enqueuedTask.size();
		}
		return queueSize;
	}

}
