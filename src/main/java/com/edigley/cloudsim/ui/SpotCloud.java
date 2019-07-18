package com.edigley.cloudsim.ui;

import static com.edigley.cloudsim.ui.SpotCLI.GROUP_BY_PEER;
import static com.edigley.cloudsim.ui.SpotCLI.LIMIT;
import static com.edigley.cloudsim.ui.SpotCLI.MACHINES_DESCRIPTION;
import static com.edigley.cloudsim.ui.SpotCLI.NUM_USERS_BY_PEER;
import static com.edigley.cloudsim.ui.SpotCLI.PEERS_DESCRIPTION;
import static com.edigley.cloudsim.ui.SpotCLI.UTILIZATION;
import static com.edigley.cloudsim.util.BufferedWriterUtils.closeBufferedWriter;
import static com.edigley.cloudsim.util.BufferedWriterUtils.createBufferedWriter;
import static com.edigley.cloudsim.util.EC2InstancesSchedulerUtils.createSpotInstancesScheduler;
import static com.edigley.cloudsim.util.EC2InstancesSchedulerUtils.defineWorkloadToSpotInstances;
import static com.edigley.cloudsim.util.EventListenerUtils.deregisterJobEventListeners;
import static com.edigley.cloudsim.util.EventListenerUtils.deregisterTaskEventListeners;
import static com.edigley.cloudsim.util.EventListenerUtils.registerJobEventListeners;
import static com.edigley.oursim.ui.CLI.AVAILABILITY;
import static com.edigley.oursim.ui.CLI.OUTPUT;
import static com.edigley.oursim.ui.CLI.VERBOSE;
import static com.edigley.oursim.ui.CLIUTil.formatSummaryStatistics;
import static com.edigley.oursim.ui.CLIUTil.getSummaryStatistics;
import static com.edigley.oursim.ui.CLIUTil.prepareOutputAccounting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.time.StopWatch;

import com.edigley.cloudsim.entities.EC2Instance;
import com.edigley.cloudsim.io.input.SpotPrice;
import com.edigley.cloudsim.io.input.SpotPriceFluctuation;
import com.edigley.cloudsim.io.input.workload.TwoStagePredictionWorkload;
import com.edigley.cloudsim.policy.SpotInstancesMultiCoreSchedulerLimited;
import com.edigley.cloudsim.simulationevents.SpotInstancesActiveEntity;
import com.edigley.cloudsim.util.SpotInstanceTraceFormat;
import com.edigley.oursim.OurSim;
import com.edigley.oursim.dispatchableevents.taskevents.TaskEventListener;
import com.edigley.oursim.entities.Grid;
import com.edigley.oursim.io.input.Input;
import com.edigley.oursim.io.input.availability.AvailabilityRecord;
import com.edigley.oursim.io.input.workload.Workload;
import com.edigley.oursim.io.output.ComputingElementEventCounter;
import com.edigley.oursim.io.output.PrintOutput;
import com.edigley.oursim.policy.FifoSharingPolicy;
import com.edigley.oursim.policy.JobSchedulerPolicy;
import com.edigley.oursim.simulationevents.EventQueue;
import com.edigley.oursim.ui.SystemConfigurationCommandParser;
import com.edigley.oursim.util.TimeUtil;

public class SpotCloud {

	private CommandLine cmd;
	
	// the mandatory entities for the simulation
	private OurSim oursim;
	private Workload workload;
	private JobSchedulerPolicy jobScheduler;
	private Input<? extends AvailabilityRecord> availability;
	private Grid grid;

	// listeners to gather simulation events for statistics
	private PrintOutput printOutput;
	private ComputingElementEventCounter compElemEventCounter;
	
	// buffers
	private BufferedWriter utilizationBuffer;
	
	// auxiliary flags
	private boolean prepared = false;
	private boolean finished = false;
	
	public SpotCloud(CommandLine cmd) {
		this.cmd = cmd;
	}

	public void prepare() throws java.text.ParseException, IOException, ParseException {
		// Simulation output file for job related events
		printOutput = new PrintOutput((File) cmd.getParsedOptionValue(OUTPUT), false);
		compElemEventCounter = prepareOutputAccounting(cmd, cmd.hasOption(VERBOSE));

		String spotTraceFilePath = cmd.getOptionValue(AVAILABILITY);
		
		//a summary of the spot prices
		List<SpotPrice> refSpotPrices = SpotInstanceTraceFormat.extractReferenceSpotPrices(spotTraceFilePath);

		grid = prepareGrid(cmd);

		availability = prepareAvailabilityInput(spotTraceFilePath, refSpotPrices);
		
		workload = defineWorkloadToSpotInstances(cmd, grid.getMapOfPeers(), refSpotPrices);

		jobScheduler = createSpotInstancesScheduler(cmd, refSpotPrices);

		utilizationBuffer = createUtilizationBuffer(cmd, grid);

		registerJobEventListeners(printOutput, (TwoStagePredictionWorkload)workload);
		
		prepared = true;
	}
	
	public void run() throws java.text.ParseException, IOException, ParseException {
		if (!prepared) {
			prepare();			
		}
		
		//prepare and start the simulation
		oursim = new OurSim(EventQueue.getInstance(), grid, jobScheduler, workload, availability);
		oursim.setActiveEntity(new SpotInstancesActiveEntity());
		oursim.start();
		
		finished = true;
	}
	
	private static Grid prepareGrid(CommandLine cmd) throws FileNotFoundException, org.apache.commons.cli.ParseException {
		Grid grid = null;
		File peerDescriptionFile = (File) cmd.getParsedOptionValue(PEERS_DESCRIPTION);
		File machinesDescriptionFile = (File) cmd.getParsedOptionValue(MACHINES_DESCRIPTION);
		grid = SystemConfigurationCommandParser.readPeersDescription(peerDescriptionFile, machinesDescriptionFile, FifoSharingPolicy.getInstance());
		return grid;
	}
	
	private static BufferedWriter createUtilizationBuffer(CommandLine cmd, Grid grid) throws org.apache.commons.cli.ParseException {
		BufferedWriter bw = null;
		if (cmd.hasOption(UTILIZATION)) {
			bw = createBufferedWriter((File) cmd.getParsedOptionValue(UTILIZATION));
			grid.setUtilizationBuffer(bw);
		}
		return bw;
	}

	private static Input<? extends AvailabilityRecord> prepareAvailabilityInput(String spotTraceFilePath,
			List<SpotPrice> refSpotPrices) throws FileNotFoundException, java.text.ParseException {
		Input<? extends AvailabilityRecord> availability;
		long timeOfFirstSpotPrice = refSpotPrices.get(SpotInstanceTraceFormat.FIRST).getTime();
		long timeOfLastSpotPrice = refSpotPrices.get(SpotInstanceTraceFormat.LAST).getTime();

		long folga = TimeUtil.ONE_MONTH;
		long spotTraceTimeSpan = (timeOfLastSpotPrice - timeOfFirstSpotPrice);
		long randomValue;
		if (spotTraceTimeSpan > folga) {
			randomValue = (new Random()).nextInt((int) (spotTraceTimeSpan - folga));		
		} else {
			randomValue = 0;
		}

		long randomPoint = randomValue;

		availability = new SpotPriceFluctuation(spotTraceFilePath, timeOfFirstSpotPrice, randomPoint);
		return availability;
	}

	public void printSummaryStatistics(StopWatch stopWatch) throws IOException {
		if (finished) {
			printSummaryStatistics(stopWatch, cmd, compElemEventCounter, jobScheduler, grid);
		}
	}

	private static void printSummaryStatistics(StopWatch stopWatch, CommandLine cmd,
			ComputingElementEventCounter compElemEventCounter, JobSchedulerPolicy jobScheduler, Grid grid)
			throws IOException {
		FileWriter fw = new FileWriter(cmd.getOptionValue(OUTPUT), true);
		fw.write("# Simulation                  duration:" + stopWatch + ".\n");

		EC2Instance ec2Instance = ((SpotInstancesMultiCoreSchedulerLimited) jobScheduler).getEc2Instance();

		int upp = Integer.parseInt(cmd.getOptionValue(NUM_USERS_BY_PEER, "0"));
		fw.write(formatSummaryStatistics(compElemEventCounter, ec2Instance.name, cmd.getOptionValue(LIMIT), ec2Instance.group, cmd.hasOption(GROUP_BY_PEER),
				grid.getPeers().size(), grid.getListOfPeers().get(0).getNumberOfMachines(), upp, -1.0, -1.0, -1l,-1l,-1l,-1.0, stopWatch.getTime(), stopWatch.toString())
				+ "\n");

		// fw.write(" " + Integer.parseInt(cmd.getOptionValue(LIMIT))+ "\n");

		System.out.println(getSummaryStatistics(compElemEventCounter, ec2Instance.name, cmd.getOptionValue(LIMIT), ec2Instance.group, cmd
				.hasOption(GROUP_BY_PEER), grid.getListOfPeers().size(), grid.getListOfPeers().get(0).getNumberOfMachines(), upp, -1.0, -1.0, -1l,-1l,-1l,-1.0,  stopWatch.getTime(),
				stopWatch.toString()));

		fw.close();
	}
	
	public void releaseResources() {
		if (finished) {
			releaseResources(cmd, printOutput, compElemEventCounter, workload, availability, utilizationBuffer);
		}
	}

	private static void releaseResources(CommandLine cmd, PrintOutput printOutput,
			ComputingElementEventCounter compElemEventCounter, Workload workload,
			Input<? extends AvailabilityRecord> availability, BufferedWriter utilizationBuffer) {
		closeBuffers(cmd, utilizationBuffer, printOutput);
		
		deregisterJobEventListeners(printOutput, compElemEventCounter, (TwoStagePredictionWorkload) workload);
		deregisterTaskEventListeners((TaskEventListener) compElemEventCounter);
		
		EventQueue.getInstance().clear();
		
		availability.close();
		workload.close();
	}

	private static void closeBuffers(CommandLine cmd, BufferedWriter ub, PrintOutput po) {
		po.close();
		if (cmd.hasOption(UTILIZATION)) {
			closeBufferedWriter(ub);
		}	
	}
	
}
