package com.edigley.cloudsim.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import com.google.common.collect.Lists;
import com.edigley.cloudsim.dispatchableevents.spotinstances.SpotPriceEventListener;
import com.edigley.cloudsim.entities.MachineAllocationManager;
import com.edigley.cloudsim.entities.BidValue;
import com.edigley.cloudsim.entities.CostAccountingManager;
import com.edigley.cloudsim.entities.EC2Instance;
import com.edigley.cloudsim.entities.SpotValue;
import com.edigley.cloudsim.entities.TaskRunManager;
import com.edigley.cloudsim.io.input.SpotPrice;

public class SpotInstancesMultiCoreSchedulerLimited extends SpotInstancesScheduler implements JobSchedulerPolicy, SpotPriceEventListener {

	private boolean onlyOneUserPerPeer = false;

	private MachineAllocationManager allocationManager;
	
	private TaskRunManager taskManager;
	
	private CostAccountingManager costManager;

	//one queued list per user
	private Map<String, List<Task>> queuedTasks;

	private SpotPrice currentSpotPrice;

	private final Peer theSpotPeer;

	private List<EC2Instance> ec2InstanceTypes;

	public SpotInstancesMultiCoreSchedulerLimited(Peer thePeer, SpotPrice initialSpotPrice, EC2Instance ec2Instance, int limit, boolean onlyOneUserByPeer) {
		this(thePeer, initialSpotPrice, Lists.newArrayList(ec2Instance), limit, onlyOneUserByPeer);
	}

	public SpotInstancesMultiCoreSchedulerLimited(Peer thePeer, SpotPrice initialSpotPrice, List<EC2Instance> ec2InstanceTypes, int limit, boolean onlyOneUserByPeer) {
		super(thePeer, initialSpotPrice, ec2InstanceTypes.get(0).speedPerCore);
		assert !ec2InstanceTypes.isEmpty();
		this.allocationManager = new MachineAllocationManager(limit);
		this.taskManager = new TaskRunManager();
		this.costManager = new CostAccountingManager();
		this.queuedTasks = new HashMap<String, List<Task>>();
		this.currentSpotPrice = initialSpotPrice;
		this.ec2InstanceTypes = ec2InstanceTypes;
		this.theSpotPeer = thePeer;
		this.onlyOneUserPerPeer = onlyOneUserByPeer;
		Collections.sort(this.ec2InstanceTypes);
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

	public void setOnlyOneUserPerPeer(boolean onlyOneUserPerPeer) {
		this.onlyOneUserPerPeer = onlyOneUserPerPeer;
	}

	public boolean isOnlyOneUserPerPeer() {
		return onlyOneUserPerPeer;
	}

	private List<Processor> getAnyEligibleFreeProcessors(String cloudUserId, Task task) {
		List<Machine> machines = this.allocationManager.getAllocatedMachines(cloudUserId);
		for (Machine machine : machines) {
			if (task.isAnEligibleMachine(machine)) {
				return machine.getFreeProcessors(task.getNumberOfCores());
			}
		}
		return null;
	}

	private String getCloudUserId(Task Task) {
		return onlyOneUserPerPeer ? /*Task.getSourceJob().getSourcePeer().getName()*/ "cloud" : Task.getSourceJob().getUserId();
	}

	private void startTask(Task task, List<Processor> processors) {
		long currentTime = getCurrentTime();
		task.setTaskExecution(new TaskExecution(task, processors, currentTime));
		task.setStartTime(currentTime);
		task.setTargetPeer(theSpotPeer);
		// XXX TODO Pensar nas consequências da linha abaixo!!!!
		this.taskManager.assignTask(processors.get(0), task);
		this.costManager.startAccounting(task);
		this.addStartedTaskEvent(task);
	}

	public List<EC2Instance> getEc2InstanceTypes() {
		return ec2InstanceTypes;
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
		// if task is still running or there is at least one processor busy in the machine...
		if (!task.isFinished() || task.getTaskExecution().getMachine().isAnyProcessorBusy()) {
			this.costManager.updateAccounting(task, currentSpotPrice);
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
			this.allocationManager.registerUser(cloudUserId);

			List<Processor> freeProcessors;
			EC2Instance ec2Instance;
			//gets from one of the already allocated machines
			if ((freeProcessors = getAnyEligibleFreeProcessors(cloudUserId, task)) != null) {

				startTask(task, freeProcessors);

			} else if ((ec2Instance = selectNewSuitableInstanceType(cloudUserId, task)) != null) { //allocates a new machine

				Machine newMachine = this.allocationManager.allocateNewMachine(cloudUserId, ec2Instance);
				BidValue bidValue = new BidValue(newMachine.getName(), getCurrentTime(), task.getBidValue(), task);
				this.addFullHourCompletedEvent(bidValue);

				List<Processor> allocatedProcessors = newMachine.getFreeProcessors(task.getNumberOfCores());
				this.costManager.account(bidValue, allocatedProcessors.get(0).getMachine());

				startTask(task, allocatedProcessors);

			} else { //enqueue the task to be executed later
				if (!queuedTasks.containsKey(cloudUserId)) {
					this.queuedTasks.put(cloudUserId, new ArrayList<Task>());
				}
				this.queuedTasks.get(cloudUserId).add(task);
			}
			
		} else {
			System.out.println("task.getBidValue() < currentSpotPrice.getPrice(): " + task.getBidValue() + " < " + currentSpotPrice.getPrice());
		}

	}

	private EC2Instance selectNewSuitableInstanceType(String cloudUserId, Task task) {
		for (EC2Instance ec2Instance : ec2InstanceTypes) {
			if (this.allocationManager.canAllocateNewInstance(cloudUserId, ec2Instance)) {
				if (task.getNumberOfCores() <= ec2Instance.numCores) {
					return ec2Instance;
				}
			}
		}
		return null;
	}

	@Override
	public void taskFinished(Event<Task> taskEvent) {
		Task sourceTask = taskEvent.getSource();
		String cloudUserId = getCloudUserId(sourceTask);

		List<Task> queuedTaskFromUser = queuedTasks.get(cloudUserId);
		// deallocate processor
		Processor processor = this.taskManager.deassignProcessor(sourceTask);
		Machine machine = processor.getMachine();
		
		// queue is empty
		if (queuedTaskFromUser == null || queuedTaskFromUser.isEmpty()) {

			if (machine.isAllProcessorsFree()) {
				// deallocate machine
				this.costManager.finishAccounting(machine, currentSpotPrice);

				boolean removed = this.allocationManager.deallocateMachine(cloudUserId, machine);
				assert removed : cloudUserId + " " + machine;
			}

		} else { // there are enqueued tasks
			
			BidValue currentBidValue = this.costManager.getBidValue(machine);
			assert currentBidValue != null;
			// this.allocatedMachines.remove(currentBidValue);

			if (queuedTaskFromUser.get(0).isAnEligibleMachine(machine)) {
				Task dequeuedTask = queuedTaskFromUser.remove(0);
	
				BidValue bidValue = new BidValue(machine.getName(), getCurrentTime(), dequeuedTask.getBidValue(), dequeuedTask);
	
				List<Processor> processors = machine.getFreeProcessors(dequeuedTask.getNumberOfCores());
				startTask(dequeuedTask, processors);
	
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
