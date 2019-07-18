package com.edigley.cloudsim.util;

import static com.edigley.cloudsim.ui.SpotCLI.ALL_INSTANCE_TYPES;
import static com.edigley.cloudsim.ui.SpotCLI.BID_VALUE;
import static com.edigley.cloudsim.ui.SpotCLI.GROUP_BY_PEER;
import static com.edigley.cloudsim.ui.SpotCLI.INSTANCE_REGION;
import static com.edigley.cloudsim.ui.SpotCLI.INSTANCE_SO;
import static com.edigley.cloudsim.ui.SpotCLI.INSTANCE_TYPE;
import static com.edigley.cloudsim.ui.SpotCLI.LIMIT;
import static com.edigley.cloudsim.ui.SpotCLI.SCHEDULER;
import static com.edigley.cloudsim.util.EC2InstancesTypesUtils.loadEC2InstancesTypes;
import static com.edigley.oursim.ui.CLI.AVAILABILITY;
import static com.edigley.oursim.ui.CLI.WORKLOAD;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import com.edigley.cloudsim.dispatchableevents.spotinstances.SpotPriceEventDispatcher;
import com.edigley.cloudsim.entities.EC2Instance;
import com.edigley.cloudsim.entities.EC2InstanceBadge;
import com.edigley.cloudsim.io.input.SpotPrice;
import com.edigley.cloudsim.io.input.workload.IosupWorkloadWithBidValue;
import com.edigley.cloudsim.io.input.workload.TwoStagePredictionWorkload;
import com.edigley.cloudsim.policy.SpotInstancesMultiCoreSchedulerLimited;
import com.edigley.cloudsim.policy.SpotInstancesScheduler;
import com.edigley.oursim.entities.Peer;
import com.edigley.oursim.io.input.workload.Workload;
import com.edigley.oursim.policy.FifoSharingPolicy;
import com.edigley.oursim.policy.JobSchedulerPolicy;

public class EC2InstancesSchedulerUtils {

	public static JobSchedulerPolicy createSpotInstancesScheduler(CommandLine cmd, List<SpotPrice> refSpotPrices)
			throws FileNotFoundException, java.text.ParseException, ParseException {
		JobSchedulerPolicy jobScheduler;
		// String ec2InstancesFilePath = "resources/ec2_instances.txt";
		File ec2InstancesFile = (File) cmd.getParsedOptionValue(ALL_INSTANCE_TYPES);
		EC2Instance ec2Instance;
		if (cmd.hasOption(INSTANCE_TYPE)) {
			ec2Instance = loadEC2InstancesTypes(ec2InstancesFile).get(cmd.getOptionValue(INSTANCE_TYPE));
			EC2InstanceBadge badge = ec2Instance.getBadge(cmd.getOptionValue(INSTANCE_REGION),
					cmd.getOptionValue(INSTANCE_SO));
		} else {
			// us-west-1.windows.m2.4xlarge.csv
			File f = new File(cmd.getOptionValue(AVAILABILITY));
			String spotTraceFileName = f.getName();
			String resto = spotTraceFileName;
			String region = resto.substring(0, resto.indexOf("."));
			resto = resto.substring(resto.indexOf(".") + 1);
			String so = resto.substring(0, resto.indexOf("."));
			resto = resto.substring(resto.indexOf(".") + 1);
			String type = resto.substring(0, resto.lastIndexOf("."));
			ec2Instance = loadEC2InstancesTypes(ec2InstancesFile).get(type);
			ec2Instance.name = type;
			ec2Instance.group = type.substring(0, type.indexOf("."));
			ec2Instance.fileName = f.getName();
		}
		Peer spotInstancesPeer = new Peer("SpotInstancesPeer", FifoSharingPolicy.getInstance());

		SpotPrice initialSpotPrice = refSpotPrices.get(SpotInstanceTraceFormat.FIRST);
		int limit = Integer.parseInt(cmd.getOptionValue(LIMIT));
		long speed = ec2Instance.speedPerCore;
		if (cmd.getOptionValue(SCHEDULER).equals("tsp")) {
			jobScheduler = new SpotInstancesMultiCoreSchedulerLimited(spotInstancesPeer, initialSpotPrice, ec2Instance,
					limit, cmd.hasOption(GROUP_BY_PEER));
		} else {
			jobScheduler = new SpotInstancesMultiCoreSchedulerLimited(spotInstancesPeer, initialSpotPrice, ec2Instance,
					limit, cmd.hasOption(GROUP_BY_PEER));
		}
		SpotPriceEventDispatcher.getInstance().addListener((SpotInstancesScheduler) jobScheduler);
		return jobScheduler;
	}

	public static Workload defineWorkloadToSpotInstances(CommandLine cmd, Map<String, Peer> peersMap,
			List<SpotPrice> refSpotPrices) throws IOException, java.text.ParseException, FileNotFoundException {
		Workload workload;
		double bidValue = -1;
		try {
			bidValue = Double.parseDouble(cmd.getOptionValue(BID_VALUE));
		} catch (NumberFormatException e) {
			if (cmd.getOptionValue(BID_VALUE).equals("min")) {
				bidValue = refSpotPrices.get(SpotInstanceTraceFormat.LOWEST).getPrice();
			} else if (cmd.getOptionValue(BID_VALUE).equals("max")) {
				bidValue = refSpotPrices.get(SpotInstanceTraceFormat.HIGHEST).getPrice();
			} else if (cmd.getOptionValue(BID_VALUE).equals("med")) {
				double med = refSpotPrices.get(SpotInstanceTraceFormat.MEAN).getPrice();
				bidValue = med;
			} else {
				System.err.println("bid inválido.");
				System.exit(10);
			}
		}
		if (cmd.getOptionValue(SCHEDULER).equals("tsp")) {
			workload = new TwoStagePredictionWorkload(cmd.getOptionValue(WORKLOAD), peersMap, 0, bidValue);
		} else {
			workload = new IosupWorkloadWithBidValue(cmd.getOptionValue(WORKLOAD), peersMap, 0, bidValue);
		}
		return workload;
	}

}
