package com.edigley.cloudsim.ui;

public class Main {

	public static void main(String[] args) throws Exception {

		int spotLimit = 64; // 2| 4 | 8 | 16 | 32 | 64 | 128
		String instanceType = "m1.small"; // c5.18xlarge | c5.9xlarge | c5.4xlarge | c5.2xlarge | c5.xlarge | c5.large | c1.xlarge | c1.medium | m2.4xlarge | m2.2xlarge | m2.xlarge | m1.xlarge | m1.large | m1.small  
/*
		String wkl = "input-files/canonical_workload.txt";
		String spp = "input-files/us-east-1.linux." + instanceType + ".csv";
		String isd = "input-files/canonical_site_description.txt";
		String md = "input-files/canonical_machine_speed.txt";
		String spi = "input-files/canonical_ec2_instances.txt";
		String scheduler = "tsp"; //two-stage-prediction
		String output = "spot-trace-persistent_output.txt";

		String wkl = "input-files/jonquera_single_core_8_workers_trace_mwa.txt";
 */
		
		instanceType = "c5.2xlarge";
		String wkl = "input-files/jonquera_tac_16_workers_trace_mwa.txt";
		String spp = "input-files/us-east-1.linux." + instanceType + ".csv";
		String isd = "input-files/jonquera_site_description.txt";
		String md = "input-files/jonquera_machine_speed.txt";
		String spi = "input-files/jonquera_ec2_instances.txt";
		String scheduler = "tsp"; //two-stage-prediction
		String output = "spot-trace-persistent_output.txt";
		
		String cmdPattern = "spotsim.jar -spot -l %s -bid max -w %s -av %s -pd %s -md %s -ait %s -sch %s -o %s";
		String spotsimCMD = String.format(cmdPattern, spotLimit, wkl, spp, isd, md, spi, scheduler, output);
		args = spotsimCMD.split("\\s+");
		SpotCLI.main(args);

	}
	
}
