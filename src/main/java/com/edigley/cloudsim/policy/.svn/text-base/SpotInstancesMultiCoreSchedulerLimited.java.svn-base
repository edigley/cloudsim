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
import br.edu.ufcg.lsd.spotinstancessimulator.entities.EC2Instance;
import br.edu.ufcg.lsd.spotinstancessimulator.entities.SpotValue;
import br.edu.ufcg.lsd.spotinstancessimulator.io.input.SpotPrice;

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

	private final Peer thePeer;

	private EC2Instance ec2Instance;

	public SpotInstancesMultiCoreSchedulerLimited(Peer thePeer, SpotPrice initialSpotPrice, EC2Instance ec2Instance, int limit, boolean onlyOneUserByPeer) {
		super(thePeer, initialSpotPrice, ec2Instance.speedByCore);
		this.processor2Task = new BidirectionalMap<Processor, Task>();
		this.allocatedMachines = new BidirectionalMap<BidValue, Machine>();
		this.allocatedMachinesForUser = new HashMap<String, List<Machine>>();
		this.numberOfAllocatedMachinesForUser = new HashMap<String, Integer>();
		this.queuedTasks = new HashMap<String, List<Task>>();
		this.accountedCost = new HashMap<Task, Double>();
		this.currentSpotPrice = initialSpotPrice;
		this.ec2Instance = ec2Instance;
		this.thePeer = thePeer;
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

	private static Processor getAnyAvailableProcessor(List<Machine> machines) {
		for (Machine machine : machines) {
			Processor processor;
			if ((processor = machine.getFreeProcessor()) != null) {
				return processor;
			}
		}
		return null;
	}

	private String getCloudUserId(Task Task) {
		return onlyOneUserByPeer ? Task.getSourceJob().getSourcePeer().getName() : Task.getSourceJob().getUserId();
	}

	private void startTask(Task queuedTask, Processor processor) {
		long currentTime = getCurrentTime();
		queuedTask.setTaskExecution(new TaskExecution(queuedTask, processor, currentTime));
		queuedTask.setStartTime(currentTime);
		queuedTask.setTargetPeer(thePeer);
		// XXX TODO Pensar nas consequências da linha abaixo!!!!
		this.accountedCost.put(queuedTask, 0.0);
		this.addStartedTaskEvent(queuedTask);
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
		Task Task = bidValue.getTask();
		if (!Task.isFinished()) {
			assert this.accountedCost.containsKey(Task);
			double totalCost = this.accountedCost.get(Task) + currentSpotPrice.getPrice();
			this.accountedCost.put(Task, totalCost);
			Task.setCost(totalCost);
			this.addFullHourCompletedEvent(bidValue);
		} else if (Task.getTaskExecution().getMachine().isAnyProcessorBusy()) {
			assert this.accountedCost.containsKey(Task);
			double totalCost = this.accountedCost.get(Task) + currentSpotPrice.getPrice();
			this.accountedCost.put(Task, totalCost);
			Task.setCost(totalCost);
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
		Task Task = taskEvent.getSource();

		if (Task.getBidValue() >= currentSpotPrice.getPrice()) {

			String cloudUserId = getCloudUserId(Task);
			if (!numberOfAllocatedMachinesForUser.containsKey(cloudUserId)) {
				this.numberOfAllocatedMachinesForUser.put(cloudUserId, 0);
				this.allocatedMachinesForUser.put(cloudUserId, new ArrayList<Machine>());
			}

			Processor availableProcessor;
			if ((availableProcessor = getAnyAvailableProcessor(this.allocatedMachinesForUser.get(cloudUserId))) != null) {

				this.processor2Task.put(availableProcessor, Task);
				startTask(Task, availableProcessor);

			} else if (this.numberOfAllocatedMachinesForUser.get(cloudUserId) < limit) {

				String machineName = getCloudUserId(Task) + "-m_" + nextMachineId++;
				Machine newMachine = new Machine(machineName, ec2Instance.speedByCore, ec2Instance.numCores);
				BidValue bidValue = new BidValue(machineName, getCurrentTime(), Task.getBidValue(), Task);
				this.addFullHourCompletedEvent(bidValue);

				Processor allocatedProcessor = newMachine.getDefaultProcessor();
				this.processor2Task.put(allocatedProcessor, Task);
				this.allocatedMachines.put(bidValue, allocatedProcessor.getMachine());

				this.numberOfAllocatedMachinesForUser.put(cloudUserId, this.numberOfAllocatedMachinesForUser.get(cloudUserId) + 1);
				this.allocatedMachinesForUser.get(cloudUserId).add(newMachine);

				startTask(Task, allocatedProcessor);

			} else {
				if (!queuedTasks.containsKey(cloudUserId)) {
					this.queuedTasks.put(cloudUserId, new ArrayList<Task>());
				}
				this.queuedTasks.get(cloudUserId).add(Task);
			}

		} else {
			System.out.println("task.getBidValue() < currentSpotPrice.getPrice(): " + Task.getBidValue() + " < " + currentSpotPrice.getPrice());
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

			Task queuedTask = queuedTaskFromUser.remove(0);
			this.processor2Task.put(processor, queuedTask);

			BidValue bidValue = new BidValue(processor.getMachine().getName(), getCurrentTime(), queuedTask.getBidValue(), queuedTask);
			// this.allocatedMachines.put(bidValue, processor);

			startTask(queuedTask, processor);

			this.addComplementaryHourCompletedEvent(bidValue, currentBidValue);

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
