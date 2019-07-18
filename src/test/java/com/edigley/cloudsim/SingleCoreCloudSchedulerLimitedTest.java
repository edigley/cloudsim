package com.edigley.cloudsim;

import org.junit.Test;

public class SingleCoreCloudSchedulerLimitedTest extends CloudSchedulerLimitedTest {

	@Test public void test01() { assertSchedulingBillableHours(1,  JOB_RUNTIME, 1); }
	@Test public void test02() { assertSchedulingBillableHours(2,  JOB_RUNTIME, 2); }
	@Test public void test03() { assertSchedulingBillableHours(3,  JOB_RUNTIME, 2); }
	@Test public void test04() { assertSchedulingBillableHours(4,  JOB_RUNTIME, 2); }
	@Test public void test05() { assertSchedulingBillableHours(5,  JOB_RUNTIME, 2); }
	@Test public void test06() { assertSchedulingBillableHours(6,  JOB_RUNTIME, 2); }
	@Test public void test07() { assertSchedulingBillableHours(7,  JOB_RUNTIME, 3); }
	@Test public void test08() { assertSchedulingBillableHours(8,  JOB_RUNTIME, 4); }
	@Test public void test09() { assertSchedulingBillableHours(9,  JOB_RUNTIME, 4); }
	@Test public void test10() { assertSchedulingBillableHours(10, JOB_RUNTIME, 4); }
	@Test public void test11() { assertSchedulingBillableHours(11, JOB_RUNTIME, 4); }
	@Test public void test12() { assertSchedulingBillableHours(12, JOB_RUNTIME, 4); }
	@Test public void test13() { assertSchedulingBillableHours(13, JOB_RUNTIME, 4); }
	@Test public void test14() { assertSchedulingBillableHours(14, JOB_RUNTIME, 4); }
	@Test public void test15() { assertSchedulingBillableHours(15, JOB_RUNTIME, 5); }
	@Test public void test16() { assertSchedulingBillableHours(16, JOB_RUNTIME, 6); }
	@Test public void test17() { assertSchedulingBillableHours(17, JOB_RUNTIME, 6); }
	@Test public void test18() { assertSchedulingBillableHours(18, JOB_RUNTIME, 6); }
	@Test public void test19() { assertSchedulingBillableHours(19, JOB_RUNTIME, 6); }
	@Test public void test20() { assertSchedulingBillableHours(20, JOB_RUNTIME, 6); }
	@Test public void test21() { assertSchedulingBillableHours(21, JOB_RUNTIME, 6); }

}
