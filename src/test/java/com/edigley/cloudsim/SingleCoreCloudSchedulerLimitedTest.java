package com.edigley.cloudsim;

import org.junit.Test;

public class SingleCoreCloudSchedulerLimitedTest extends CloudSchedulerLimitedTest {

	@Test public void test01() { assertsSingleCore(1,  JOB_RUNTIME, 1); }
	@Test public void test02() { assertsSingleCore(2,  JOB_RUNTIME, 2); }
	@Test public void test03() { assertsSingleCore(3,  JOB_RUNTIME, 2); }
	@Test public void test04() { assertsSingleCore(4,  JOB_RUNTIME, 2); }
	@Test public void test05() { assertsSingleCore(5,  JOB_RUNTIME, 2); }
	@Test public void test06() { assertsSingleCore(6,  JOB_RUNTIME, 2); }
	@Test public void test07() { assertsSingleCore(7,  JOB_RUNTIME, 3); }
	@Test public void test08() { assertsSingleCore(8,  JOB_RUNTIME, 4); }
	@Test public void test09() { assertsSingleCore(9,  JOB_RUNTIME, 4); }
	@Test public void test10() { assertsSingleCore(10, JOB_RUNTIME, 4); }
	@Test public void test11() { assertsSingleCore(11, JOB_RUNTIME, 4); }
	@Test public void test12() { assertsSingleCore(12, JOB_RUNTIME, 4); }
	@Test public void test13() { assertsSingleCore(13, JOB_RUNTIME, 4); }
	@Test public void test14() { assertsSingleCore(14, JOB_RUNTIME, 4); }
	@Test public void test15() { assertsSingleCore(15, JOB_RUNTIME, 5); }
	@Test public void test16() { assertsSingleCore(16, JOB_RUNTIME, 6); }
	@Test public void test17() { assertsSingleCore(17, JOB_RUNTIME, 6); }
	@Test public void test18() { assertsSingleCore(18, JOB_RUNTIME, 6); }
	@Test public void test19() { assertsSingleCore(19, JOB_RUNTIME, 6); }
	@Test public void test20() { assertsSingleCore(20, JOB_RUNTIME, 6); }
	@Test public void test21() { assertsSingleCore(21, JOB_RUNTIME, 6); }

}
