package com.edigley.cloudsim.ui;

public class Main {

	/*
	 */
	public static void main(String[] args) throws Exception {

		int spotLimit = 1; // 1 | 2 | 10 | 20 | 64 | 128 | 256
		String instanceType = "m1.small"; // c1.xlarge | c1.medium | m2.4xlarge | m2.2xlarge | m2.xlarge | m1.xlarge | m1.large | m1.small  

		String wkl = "input-files/jonquera_single_core_8_workers_trace_mwa.txt";
		String spp = "input-files/us-east-1.linux." + instanceType + ".csv";
		String isd = "input-files/jonquera_site_description.txt";
		String md = "input-files/jonquera_machine_speed.txt";
		String spi = "input-files/jonquera_ec2_instances.txt";
		String output = "spot-trace-persistent_output.txt";

		//int[] spotLimits = new int[] {1, 2, 10, 20, 64, 128, 256}; 
		
		String cmdPattern = "spotsim.jar -spot -l %s -bid max -w %s -av %s -pd %s -md %s -ait %s -o %s";
		String spotsimCMD = String.format(cmdPattern, spotLimit, wkl, spp, isd, md, spi, output);
		args = spotsimCMD.split("\\s+");
		SpotCLI.main(args);

	}
}
