package com.edigley.cloudsim.ui;

public class Main {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		int spotLimit = 100;
		String spw = "input-files/jonquera_workload.txt";
		String spt = "input-files/us-east-1.linux.m1.small.csv";
		String isd = "input-files/jonquera_site_description.txt";
		String md = "input-files/jonquera_machines_speeds_10_sites.txt";
		String spi = "input-files/jonquera_ec2_instances.txt";
		String output = "spot-trace-persistent_output.txt";

		String cmdPattern = "spotsim.jar -spot -l %s -bid max -w %s -av %s -pd %s -md %s -ait %s -o %s";
		String spotsimCMD = String.format(cmdPattern, spotLimit, spw, spt, isd, md, spi, output);

		args = spotsimCMD.split("\\s+");
		SpotCLI.main(args);

	}
}
