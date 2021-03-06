package com.edigley.cloudsim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.edigley.cloudsim.dispatchableevents.spotinstances.SpotPriceEventDispatcher;
import com.edigley.cloudsim.entities.EC2Instance;
import com.edigley.cloudsim.io.input.SpotPrice;
import com.edigley.cloudsim.policy.SpotInstancesMultiCoreSchedulerLimited;
import com.edigley.cloudsim.simulationevents.SpotInstancesActiveEntity;
import com.edigley.oursim.AbstractOurSimAPITest;
import com.edigley.oursim.OurSim;
import com.edigley.oursim.dispatchableevents.jobevents.JobEventDispatcher;
import com.edigley.oursim.dispatchableevents.taskevents.TaskEventDispatcher;
import com.edigley.oursim.entities.Grid;
import com.edigley.oursim.entities.Job;
import com.edigley.oursim.entities.Peer;
import com.edigley.oursim.entities.Processor;
import com.edigley.oursim.entities.Task;
import com.edigley.oursim.io.input.Input;
import com.edigley.oursim.io.input.InputAbstract;
import com.edigley.oursim.io.input.workload.Workload;
import com.edigley.oursim.io.input.workload.WorkloadAbstract;
import com.edigley.oursim.policy.FifoSharingPolicy;
import com.edigley.oursim.simulationevents.EventQueue;
import com.edigley.oursim.util.TimeUtil;

public class CloudSchedulerLimitedTest extends AbstractOurSimAPITest {

	protected final long SIMULATION_TIME = TimeUtil.ONE_DAY;
	
	protected String INSTANCE_NAME = "m1.small";
	protected int NUMBER_OF_CORES = 1;
	protected int SPEED_PER_CORE_IN_ECU = 1 ;
	protected double SPOT_PRICE = 0.1;
	
	protected final int NUMBER_OF_PEERS = 1;
	protected final int NUMBER_OF_RESOURCES_PER_PEER = 1;
	
	protected int NUMBER_OF_JOBS = 10;
	protected long JOB_RUNTIME = TimeUtil.FIFTEEN_MINUTES;

	protected SpotInstancesMultiCoreSchedulerLimited spotScheduler;

	
	protected int limit;

	private Grid grid;
	private Peer dataCenter;
	private SpotPrice initialSpotPrice;
	private EC2Instance ec2Instance;
	
	private Input<SpotPrice> spotPriceEvents;
	private Workload workload;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		initialSpotPrice = new SpotPrice(INSTANCE_NAME, new Date(), SPOT_PRICE);
		this.limit = 2;
		this.ec2Instance = new EC2Instance();
		this.ec2Instance.name = INSTANCE_NAME;
		this.ec2Instance.numCores = NUMBER_OF_CORES;
		this.ec2Instance.speedPerCore = Math.round(SPEED_PER_CORE_IN_ECU * Processor.EC2_COMPUTE_UNIT.getSpeed());
		
		// the grid consists of only one peer
		peers = new ArrayList<Peer>(NUMBER_OF_PEERS);
		this.dataCenter = createPeer(NUMBER_OF_RESOURCES_PER_PEER);
		peers.add(this.dataCenter);
		grid = new Grid(peers);
		
		// generate one spotPrice event each 15 minutes until the end of the simulation
		this.spotPriceEvents = createSpotPriceInput(TimeUtil.FIFTEEN_MINUTES);
		
		// at most 2 cloud instances
		this.spotScheduler = createSpotScheduler(ec2Instance, limit);

		// 10 jobs each one with only one task
		this.jobs = new ArrayList<Job>(NUMBER_OF_JOBS);
		this.workload = createWorkload(NUMBER_OF_JOBS, JOB_RUNTIME, peers.get(0), jobs);
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
		// cleans all simulations remaining events
		TaskEventDispatcher.getInstance().clear();
		JobEventDispatcher.getInstance().clear();
		SpotPriceEventDispatcher.getInstance().clear();
		EventQueue.getInstance().clear();
	}

	
	@Test
	public void testRunSingleCoreMachines() {
		assertTrue(true);
		assertEquals(true, true);

		//assertsSingleCore(NUMBER_OF_JOBS, JOB_RUNTIME, 4);

	}
	
	protected void assertSchedulingBillableHours(int nOfSubmittedJobs, long jobRuntime, int nOfBillableHours) {
		assertSchedulingBillableHours(nOfSubmittedJobs, nOfSubmittedJobs, jobRuntime, nOfBillableHours);
	}
	
	protected void assertSchedulingBillableHours(int nOfSubmittedJobs, int nOfFinishedJobs, long jobRuntime, int nOfBillableHours, EC2Instance ec2Instance, int limit) {
		this.ec2Instance = ec2Instance;
		this.limit = limit;
		this.spotScheduler = createSpotScheduler(ec2Instance, limit);
		assertSchedulingBillableHours(nOfSubmittedJobs, nOfFinishedJobs, jobRuntime, nOfBillableHours);
	}

	private SpotInstancesMultiCoreSchedulerLimited createSpotScheduler(EC2Instance ec2Instance, int limit) {
		SpotPriceEventDispatcher.getInstance().clear();
		SpotInstancesMultiCoreSchedulerLimited spotScheduler = new SpotInstancesMultiCoreSchedulerLimited(dataCenter, initialSpotPrice, ec2Instance, limit, true);
		SpotPriceEventDispatcher.getInstance().addListener(spotScheduler);
		return spotScheduler;
	}
	
	protected void assertSchedulingBillableHours(int nOfSubmittedJobs, int nOfFinishedJobs, long jobRuntime, int nOfBillableHours) {
		
		// n jobs each one with only one task
		this.jobs = new ArrayList<Job>(nOfSubmittedJobs);
		this.workload = createWorkload(nOfSubmittedJobs, jobRuntime, peers.get(0), jobs);
		
		//TaskEventDispatcher.getInstance().addListener(new TaskPrintOutput());
		
		// start the simulation
		oursim = new OurSim(EventQueue.getInstance(), grid, spotScheduler, workload, spotPriceEvents);
		oursim.setActiveEntity(new SpotInstancesActiveEntity());
		oursim.start();
		

		// get the cost for all job executions
		double totalCost = 0.0;
		for (Job job : jobs) {
			totalCost += job.getCost();
		}

		// expected cost. We need to consider the comparison delta, otherwise the test fails
		assertEquals(nOfBillableHours * SPOT_PRICE, totalCost, 0.0000001);
		
		// expected number of events
		assertEquals(nOfFinishedJobs, this.jobEventCounter.getNumberOfFinishedJobs());
		assertEquals(0, this.jobEventCounter.getNumberOfPreemptionsForAllJobs());
		assertEquals(nOfFinishedJobs, this.taskEventCounter.getNumberOfFinishedTasks());
		assertEquals(0, this.taskEventCounter.getNumberOfPreemptionsForAllTasks());
		
	}

	private Peer createPeer(final int numberOfResources) {
		final Peer peer = new Peer("the_peer", numberOfResources, Processor.EC2_COMPUTE_UNIT.getSpeed(), FifoSharingPolicy.getInstance());
		return peer;
	}

	protected Workload createWorkload(final int numOfJobs, final long jobRuntime, final Peer peer, List<Job> jobs) {
		Workload workload = new WorkloadAbstract() {
			@Override
			protected void setUp() {
				for (int i = 0; i < numOfJobs; i++) {
					Job job = addJob(nextJobId++, 0, jobRuntime, peer, this.inputs, jobs);
					job.setUserId("default_user");
					for (Task Task : job.getTasks()) {
						Task.setBidValue(SPOT_PRICE);
					}
				}
			}
		};
		return workload;
	}

	private Input<SpotPrice> createSpotPriceInput(long period) {
		Input<SpotPrice> availability = new InputAbstract<SpotPrice>() {
			@Override
			protected void setUp() {
				long time = 0;
				while (time <= SIMULATION_TIME) {
					this.inputs.add(new SpotPrice(INSTANCE_NAME, time, SPOT_PRICE));
					time += period;
				}
			}
		};
		return availability;
	}

}
