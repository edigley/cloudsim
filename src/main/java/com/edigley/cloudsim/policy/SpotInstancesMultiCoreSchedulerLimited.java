package com.edigley.cloudsim.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.edigley.oursim.dispatchableevents.Event;
import com.edigley.oursim.entities.Job;
import com.edigley.oursim.entities.Machine;
import com.edigley.oursim.entities.Peer;
import com.edigley.oursim.entities.Processor;
import com.edigley.oursim.entities.Task;
import com.edigley.oursim.entities.TaskExecution;
import com.edigley.oursim.io.input.workload.Workload;
import com.edigley.oursim.policy.JobSchedulerPolicy;
import com.edigley.oursim.util.BidirectionalMap;
import com.google.common.collect.Lists;
import com.edigley.cloudsim.dispatchableevents.spotinstances.SpotPriceEventListener;
import com.edigley.cloudsim.entities.BidValue;
import com.edigley.cloudsim.entities.EC2Instance;
import com.edigley.cloudsim.entities.SpotValue;
import com.edigley.cloudsim.io.input.SpotPrice;

public class SpotInstancesMultiCoreSchedulerLimited extends SpotInstancesScheduler implements JobSchedulerPolicy, SpotPriceEventListener {

	private boolean onlyOneUserByPeer = false;

	private int limit;

	private Map<String, Integer> numberOfAllocatedMachinesForUser;

	private Map<String, List<Machine>> allocatedMachinesForUser;

	private BidirectionalMap<BidValue, Machine> allocatedMachines;

	private Map<String, List<Task>> queuedTasks;

	private BidirectionalMap<Processor, Task> processor2Task;

	private Map<Task, Double> accountedCost;

	private SpotPrice currentSpotPrice;

	private long nextMachineId = 1;

	private final Peer theSpotPeer;

	private EC2Instance ec2Instance;

	public SpotInstancesMultiCoreSchedulerLimited(Peer thePeer, SpotPrice initialSpotPrice, EC2Instance ec2Instance, int limit, boolean onlyOneUserByPeer) {
		super(thePeer, initialSpotPrice, ec2Instance.speedPerCore);
		this.processor2Task = new BidirectionalMap<Processor, Task>();
		this.allocatedMachines = new BidirectionalMap<BidValue, Machine>();
		this.allocatedMachinesForUser = new HashMap<String, List<Machine>>();
		this.numberOfAllocatedMachinesForUser = new HashMap<String, Integer>();
		this.queuedTasks = new HashMap<String, List<Task>>();
		this.accountedCost = new HashMap<Task, Double>();
		this.currentSpotPrice = initialSpotPrice;
		this.ec2Instance = ec2Instance;
		this.theSpotPeer = thePeer;
		this.limit = limit;
		this.onlyOneUserByPeer = onlyOneUserByPeer;
	}

	@Override
	public void schedule() {
	}

	@Override
	public int getQueueSize() {
		Collection<List<Task>> allEnqueuedTasks = queuedTasks.values();
		int queueSize = 0;
		for (List<Task> enqueuedTask : allEnqueuedTasks) {
			queueSize += enqueuedTask.size();
		}
		return queueSize;
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
		return false;
	}

	public void setOnlyOneUserByPeer(boolean onlyOneUserByPeer) {
		this.onlyOneUserByPeer = onlyOneUserByPeer;
	}

	public boolean isOnlyOneUserByPeer() {
		return onlyOneUserByPeer;
	}

	private static List<Processor> getAnyAvailableProcessors(List<Machine> machines, Task task) {
		for (Machine machine : machines) {
			if (task.isAnEligibleMachine(machine)) {
				return machine.getFreeProcessors(task.getNumberOfCores());
			}
		}
		return null;
	}

	private String getCloudUserId(Task Task) {
		return onlyOneUserByPeer ? /*Task.getSourceJob().getSourcePeer().getName()*/ "cloud" : Task.getSourceJob().getUserId();
	}

	private void startTask(Task task, List<Processor> processors) {
		long currentTime = getCurrentTime();
		task.setTaskExecution(new TaskExecution(task, processors, currentTime));
		task.setStartTime(currentTime);
		task.setTargetPeer(theSpotPeer);
		// XXX TODO Pensar nas consequências da linha abaixo!!!!
		this.accountedCost.put(task, 0.0);
		this.addStartedTaskEvent(task);
	}

	public EC2Instance getEc2Instance() {
		return ec2Instance;
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
		Task task = bidValue.getTask();
		if (!task.isFinished()) {
			assert this.accountedCost.containsKey(task);
			double totalCost = this.accountedCost.get(task) + currentSpotPrice.getPrice();
			this.accountedCost.put(task, totalCost);
			task.setCost(totalCost);
			this.addFullHourCompletedEvent(bidValue);
		} else if (task.getTaskExecution().getMachine().isAnyProcessorBusy()) {
			assert this.accountedCost.containsKey(task);
			double totalCost = this.accountedCost.get(task) + currentSpotPrice.getPrice();
			this.accountedCost.put(task, totalCost);
			task.setCost(totalCost);
			this.addFullHourCompletedEvent(bidValue);
		} else {
			// System.out.println("Task: " + task.getId() + ": já terminou antes
			// de completar uma hora e a máquina estava livre.");
		}
	} // E-- End of implementation of SpotPriceEventListener

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
	} // E-- end of implementation of JobEventListener

	// B-- beginning of implementation of TaskEventListener
	@Override
	public void taskSubmitted(Event<Task> taskEvent) {
		Task task = taskEvent.getSource();

		if (task.getBidValue() >= currentSpotPrice.getPrice()) {

			String cloudUserId = getCloudUserId(task);
			if (!numberOfAllocatedMachinesForUser.containsKey(cloudUserId)) {
				this.numberOfAllocatedMachinesForUser.put(cloudUserId, 0);
				this.allocatedMachinesForUser.put(cloudUserId, new ArrayList<Machine>());
			}

			List<Processor> availableProcessors;
			//gets from one of the already allocated machines
			if ((availableProcessors = getAnyAvailableProcessors(this.allocatedMachinesForUser.get(cloudUserId), task)) != null) {

				this.processor2Task.put(availableProcessors.get(0), task);
				startTask(task, availableProcessors);

			} else if (this.numberOfAllocatedMachinesForUser.get(cloudUserId) < limit && task.getNumberOfCores() <= ec2Instance.numCores) { //allocates a new machine

					String machineName = getCloudUserId(task) + "-m_" + nextMachineId++;
					Machine newMachine = new Machine(machineName, ec2Instance.speedPerCore, ec2Instance.numCores);
					BidValue bidValue = new BidValue(machineName, getCurrentTime(), task.getBidValue(), task);
					this.addFullHourCompletedEvent(bidValue);
	
					List<Processor> allocatedProcessors = newMachine.getFreeProcessors(task.getNumberOfCores());
					this.processor2Task.put(allocatedProcessors.get(0), task);
					this.allocatedMachines.put(bidValue, allocatedProcessors.get(0).getMachine());
	
					this.numberOfAllocatedMachinesForUser.put(cloudUserId, this.numberOfAllocatedMachinesForUser.get(cloudUserId) + 1);
					this.allocatedMachinesForUser.get(cloudUserId).add(newMachine);
	
					startTask(task, allocatedProcessors);

			} else { //enqueue the task to be executed later
				if (!queuedTasks.containsKey(cloudUserId)) {
					this.queuedTasks.put(cloudUserId, new ArrayList<Task>());
				}
				this.queuedTasks.get(cloudUserId).add(task);
			}
			
			assert this.numberOfAllocatedMachinesForUser.get(cloudUserId) <= limit;

		} else {
			System.out.println("task.getBidValue() < currentSpotPrice.getPrice(): " + task.getBidValue() + " < " + currentSpotPrice.getPrice());
		}

	}

	@Override
	public void taskFinished(Event<Task> taskEvent) {
		Task sourceTask = taskEvent.getSource();
		String userId = getCloudUserId(sourceTask);

		// System.out.println(getCurrentTime() + ":\n" +
		// this.allocatedMachines);

		List<Task> queuedTaskFromUser = queuedTasks.get(userId);
		// se não tem fila
		if (queuedTaskFromUser == null || queuedTaskFromUser.isEmpty()) {

			// deallocate processor
			Processor processor = this.processor2Task.getKey(sourceTask);
			this.processor2Task.remove(processor);

			if (processor.getMachine().isAllProcessorsFree()) {
				// deallocate machine
				BidValue currentBidValue = this.allocatedMachines.getKey(processor.getMachine());
				assert currentBidValue != null;
				this.allocatedMachines.remove(currentBidValue);

				Task firstAllocatedTask = currentBidValue.getTask();
				double totalCost = this.accountedCost.get(firstAllocatedTask) + currentSpotPrice.getPrice();
				this.accountedCost.put(firstAllocatedTask, totalCost);
				firstAllocatedTask.setCost(totalCost);

				this.numberOfAllocatedMachinesForUser.put(userId, this.numberOfAllocatedMachinesForUser.get(userId) - 1);
				boolean removed = this.allocatedMachinesForUser.get(userId).remove(processor.getMachine());
				assert removed : userId + " " + processor.getMachine();
			}

		} else { // tem fila

			Processor processor = this.processor2Task.getKey(sourceTask);
			this.processor2Task.remove(processor);

			BidValue currentBidValue = this.allocatedMachines.getKey(processor.getMachine());
			assert currentBidValue != null;
			// this.allocatedMachines.remove(currentBidValue);

			if (queuedTaskFromUser.get(0).isAnEligibleMachine(processor.getMachine())) {
				Task queuedTask = queuedTaskFromUser.remove(0);
				this.processor2Task.put(processor, queuedTask);
	
				BidValue bidValue = new BidValue(processor.getMachine().getName(), getCurrentTime(), queuedTask.getBidValue(), queuedTask);
				// this.allocatedMachines.put(bidValue, processor);
	
				List<Processor> processors = getAnyAvailableProcessors(Lists.newArrayList(processor.getMachine()), queuedTask);;
				startTask(queuedTask, processors);
	
				this.addComplementaryHourCompletedEvent(bidValue, currentBidValue);			
			}

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
	} // E-- end of implementation of TaskEventListener

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
	} // E-- end of implementation of SpotPriceEventListener

}
