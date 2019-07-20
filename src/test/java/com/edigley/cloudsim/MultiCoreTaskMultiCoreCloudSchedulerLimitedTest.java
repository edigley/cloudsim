package com.edigley.cloudsim;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.edigley.cloudsim.entities.EC2Instance;
import com.edigley.oursim.entities.Job;
import com.edigley.oursim.entities.Peer;
import com.edigley.oursim.entities.Task;
import com.edigley.oursim.io.input.workload.Workload;
import com.edigley.oursim.io.input.workload.WorkloadAbstract;

public class MultiCoreTaskMultiCoreCloudSchedulerLimitedTest extends CloudSchedulerLimitedTest {

	private int LIMIT = 20;
	private int CORES_PER_INSTANCE = 2;
	
	private EC2Instance ec2Instance = new EC2Instance(INSTANCE_NAME, CORES_PER_INSTANCE, SPEED_PER_CORE_IN_ECU);
	
	@Before
	public void setUp() throws Exception {
		INSTANCE_NAME = "m1.small";
		NUMBER_OF_CORES = 1;
		SPEED_PER_CORE_IN_ECU = 1 ;
		SPOT_PRICE = 0.1;
		super.setUp();
	}
	
	protected Workload createWorkload(final int numOfJobs, final long jobRuntime, final Peer peer, List<Job> jobs) {
		Workload workload = new WorkloadAbstract() {
			@Override
			protected void setUp() {
				for (int i = 0; i < numOfJobs; i++) {
					Job job = addJob(nextJobId++, 0, 2, jobRuntime, peer, this.inputs, jobs);
					job.setUserId("the-user");
					for (Task task : job.getTasks()) {
						task.setBidValue(SPOT_PRICE);
					}
				}
			}
		};
		return workload;
	}

	static long taskId;
	
	public static final Job addJob(long jobId, long submissionTime, int numberOfCores, long duration, final Peer peer, Collection<Job>... collectionsOfJob) {
		Job job = new Job(jobId, submissionTime, peer);
		job.addTask(new Task(taskId++, "executable.exe", numberOfCores, duration, submissionTime));
		for (Collection<Job> collection : collectionsOfJob) {
			collection.add(job);
		}
		return job;
	}

	//none of the submitted jobs should finish because the cloud machine is not capable of run any of the submitted tasks
	@Test public void test01_1() { assertSchedulingBillableHours(1,  0, JOB_RUNTIME, 0); }
	@Test public void test02_1() { assertSchedulingBillableHours(2,  0, JOB_RUNTIME, 0); }
	@Test public void test03_1() { assertSchedulingBillableHours(3,  0, JOB_RUNTIME, 0); }
	@Test public void test04_1() { assertSchedulingBillableHours(4,  0, JOB_RUNTIME, 0); }
	@Test public void test05_1() { assertSchedulingBillableHours(5,  0, JOB_RUNTIME, 0); }
	@Test public void test06_1() { assertSchedulingBillableHours(6,  0, JOB_RUNTIME, 0); }
	@Test public void test07_1() { assertSchedulingBillableHours(7,  0, JOB_RUNTIME, 0); }
	@Test public void test08_1() { assertSchedulingBillableHours(8,  0, JOB_RUNTIME, 0); }
	@Test public void test09_1() { assertSchedulingBillableHours(9,  0, JOB_RUNTIME, 0); }
	@Test public void test10_1() { assertSchedulingBillableHours(10, 0, JOB_RUNTIME, 0); }
	@Test public void test11_1() { assertSchedulingBillableHours(11, 0, JOB_RUNTIME, 0); }
	@Test public void test12_1() { assertSchedulingBillableHours(12, 0, JOB_RUNTIME, 0); }
	@Test public void test13_1() { assertSchedulingBillableHours(13, 0, JOB_RUNTIME, 0); }
	@Test public void test14_1() { assertSchedulingBillableHours(14, 0, JOB_RUNTIME, 0); }
	@Test public void test15_1() { assertSchedulingBillableHours(15, 0, JOB_RUNTIME, 0); }
	@Test public void test16_1() { assertSchedulingBillableHours(16, 0, JOB_RUNTIME, 0); }
	@Test public void test17_1() { assertSchedulingBillableHours(17, 0, JOB_RUNTIME, 0); }
	@Test public void test18_1() { assertSchedulingBillableHours(18, 0, JOB_RUNTIME, 0); }
	@Test public void test19_1() { assertSchedulingBillableHours(19, 0, JOB_RUNTIME, 0); }
	@Test public void test20_1() { assertSchedulingBillableHours(20, 0, JOB_RUNTIME, 0); }
	
	//machines available with exactly the number of cores each task needs
	@Test public void test01_2() { assertSchedulingBillableHours(1,  1, JOB_RUNTIME, 1, ec2Instance, LIMIT); }
	@Test public void test02_2() { assertSchedulingBillableHours(2,  2, JOB_RUNTIME, 2, ec2Instance, LIMIT); }
	@Test public void test03_2() { assertSchedulingBillableHours(3,  3, JOB_RUNTIME, 3, ec2Instance, LIMIT); }
	@Test public void test04_2() { assertSchedulingBillableHours(4,  4, JOB_RUNTIME, 4, ec2Instance, LIMIT); }
	@Test public void test05_2() { assertSchedulingBillableHours(5,  5, JOB_RUNTIME, 5, ec2Instance, LIMIT); }
	@Test public void test06_2() { assertSchedulingBillableHours(6,  6, JOB_RUNTIME, 6, ec2Instance, LIMIT); }
	@Test public void test07_2() { assertSchedulingBillableHours(7,  7, JOB_RUNTIME, 7, ec2Instance, LIMIT); }
	@Test public void test08_2() { assertSchedulingBillableHours(8,  8, JOB_RUNTIME, 8, ec2Instance, LIMIT); }
	@Test public void test09_2() { assertSchedulingBillableHours(9,  9, JOB_RUNTIME, 9, ec2Instance, LIMIT); }
	@Test public void test10_2() { assertSchedulingBillableHours(10, 10, JOB_RUNTIME, 10, ec2Instance, LIMIT); }
	@Test public void test11_2() { assertSchedulingBillableHours(11, 11, JOB_RUNTIME, 11, ec2Instance, LIMIT); }
	@Test public void test12_2() { assertSchedulingBillableHours(12, 12, JOB_RUNTIME, 12, ec2Instance, LIMIT); }
	@Test public void test13_2() { assertSchedulingBillableHours(13, 13, JOB_RUNTIME, 13, ec2Instance, LIMIT); }
	@Test public void test14_2() { assertSchedulingBillableHours(14, 14, JOB_RUNTIME, 14, ec2Instance, LIMIT); }
	@Test public void test15_2() { assertSchedulingBillableHours(15, 15, JOB_RUNTIME, 15, ec2Instance, LIMIT); }
	@Test public void test16_2() { assertSchedulingBillableHours(16, 16, JOB_RUNTIME, 16, ec2Instance, LIMIT); }
	@Test public void test17_2() { assertSchedulingBillableHours(17, 17, JOB_RUNTIME, 17, ec2Instance, LIMIT); }
	@Test public void test18_2() { assertSchedulingBillableHours(18, 18, JOB_RUNTIME, 18, ec2Instance, LIMIT); }
	@Test public void test19_2() { assertSchedulingBillableHours(19, 19, JOB_RUNTIME, 19, ec2Instance, LIMIT); }
	@Test public void test20_2() { assertSchedulingBillableHours(20, 20, JOB_RUNTIME, 20, ec2Instance, LIMIT); }
	
	//requests more instances than the limit
	@Test public void test21_1() { assertSchedulingBillableHours(21,  0, JOB_RUNTIME, 0); }
	@Test public void test22_1() { assertSchedulingBillableHours(22,  0, JOB_RUNTIME, 0); }
	@Test public void test23_1() { assertSchedulingBillableHours(23,  0, JOB_RUNTIME, 0); }
	@Test public void test40_1() { assertSchedulingBillableHours(40,  0, JOB_RUNTIME, 0); }
	@Test public void test21_2() { assertSchedulingBillableHours(21, 21, JOB_RUNTIME, 20, ec2Instance, LIMIT); }
	@Test public void test22_2() { assertSchedulingBillableHours(22, 22, JOB_RUNTIME, 20, ec2Instance, LIMIT); }
	@Test public void test23_2() { assertSchedulingBillableHours(23, 23, JOB_RUNTIME, 20, ec2Instance, LIMIT); }
	@Test public void test40_2() { assertSchedulingBillableHours(40, 40, JOB_RUNTIME, 20, ec2Instance, LIMIT); }

	
	
}
