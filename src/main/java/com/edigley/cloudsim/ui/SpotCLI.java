/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.edigley.cloudsim.ui;

import static com.edigley.cloudsim.util.BufferedWriterUtils.closeFileWriter;
import static com.edigley.cloudsim.util.BufferedWriterUtils.closeBufferedWriter;
import static com.edigley.cloudsim.util.BufferedWriterUtils.createBufferedWriter;
import static com.edigley.cloudsim.util.EC2InstancesSchedulerUtils.createSpotInstancesScheduler;
import static com.edigley.cloudsim.util.EC2InstancesSchedulerUtils.defineWorkloadToSpotInstances;
import static com.edigley.cloudsim.util.EventListenerUtils.deregisterJobEventListeners;
import static com.edigley.cloudsim.util.EventListenerUtils.registerJobEventListeners;
import static com.edigley.cloudsim.util.EventListenerUtils.deregisterTaskEventListeners;
import static com.edigley.oursim.ui.CLI.AVAILABILITY;
import static com.edigley.oursim.ui.CLI.EXECUTION_LINE;
import static com.edigley.oursim.ui.CLI.HELP;
import static com.edigley.oursim.ui.CLI.OUTPUT;
import static com.edigley.oursim.ui.CLI.USAGE;
import static com.edigley.oursim.ui.CLI.VERBOSE;
import static com.edigley.oursim.ui.CLI.WORKLOAD;
import static com.edigley.oursim.ui.CLIUTil.formatSummaryStatistics;
import static com.edigley.oursim.ui.CLIUTil.getSummaryStatistics;
import static com.edigley.oursim.ui.CLIUTil.parseCommandLine;
import static com.edigley.oursim.ui.CLIUTil.prepareOutputAccounting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.time.StopWatch;

import com.edigley.cloudsim.entities.EC2Instance;
import com.edigley.cloudsim.io.input.SpotPrice;
import com.edigley.cloudsim.io.input.SpotPriceFluctuation;
import com.edigley.cloudsim.io.input.workload.TwoStagePredictionWorkload;
import com.edigley.cloudsim.policy.SpotInstancesMultiCoreSchedulerLimited;
import com.edigley.cloudsim.simulationevents.SpotInstancesActiveEntity;
import com.edigley.cloudsim.util.SpotInstaceTraceFormat;
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

public class SpotCLI {

	public static final String SPOT_INSTANCES = "spot";

	public static final String INSTANCE_TYPE = "type";

	public static final String ALL_INSTANCE_TYPES = "ait";

	public static final String INSTANCE_REGION = "region";

	public static final String INSTANCE_SO = "so";

	public static final String BID_VALUE = "bid";

	public static final String LIMIT = "l";

	public static final String UTILIZATION = "u";

	public static final String SPOT_MACHINES_SPEED = "speed";

	public static final String MACHINES_DESCRIPTION = "md";

	public static final String PEERS_DESCRIPTION = "pd";

	public static final String GROUP_BY_PEER = "gbp";
	
	public static final String NUM_USERS_BY_PEER = "upp";

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		CommandLine cmd = parseCommandLine(args, prepareOptions(), HELP, USAGE, EXECUTION_LINE);
		
		// Simulation output file for job related events
		PrintOutput printOutput = new PrintOutput((File) cmd.getOptionObject(OUTPUT), false);
		ComputingElementEventCounter compElemEventCounter = prepareOutputAccounting(cmd, cmd.hasOption(VERBOSE));

		// the mandatory entities for the simulation
		OurSim oursim = null;
		Workload workload = null;
		JobSchedulerPolicy jobScheduler = null;
		Input<? extends AvailabilityRecord> availability = null;
		Grid grid = null;
		
		String spotTraceFilePath = cmd.getOptionValue(AVAILABILITY);
		
		//a summary of the spot prices
		List<SpotPrice> refSpotPrices = SpotInstaceTraceFormat.extractReferenceSpotPrices(spotTraceFilePath);

		grid = prepareGrid(cmd);

		availability = prepareAvailabilityInput(spotTraceFilePath, refSpotPrices);
		
		workload = defineWorkloadToSpotInstances(cmd, grid.getMapOfPeers(), refSpotPrices);

		jobScheduler = createSpotInstancesScheduler(cmd, refSpotPrices);

		BufferedWriter utilizationBuffer = setUpUtilizationBuffer(cmd, grid);

		registerJobEventListeners(printOutput, (TwoStagePredictionWorkload)workload);
		
		//prepare and start the simulation
		oursim = new OurSim(EventQueue.getInstance(), grid, jobScheduler, workload, availability);
		oursim.setActiveEntity(new SpotInstancesActiveEntity());
		oursim.start();

		//get simulation summary statistics
		stopWatch.stop();
		printSummaryStatistics(stopWatch, cmd, compElemEventCounter, jobScheduler, grid);
		
		//release allocated resources
		releaseResources(cmd, printOutput, compElemEventCounter, workload, availability, utilizationBuffer);

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

	private static void closeBuffers(CommandLine cmd, BufferedWriter ub, PrintOutput po) {
		po.close();
		if (cmd.hasOption(UTILIZATION)) {
			closeBufferedWriter(ub);
		}	
	}

	private static BufferedWriter setUpUtilizationBuffer(CommandLine cmd, Grid grid) {
		BufferedWriter bw = null;
		if (cmd.hasOption(UTILIZATION)) {
			bw = createBufferedWriter((File) cmd.getOptionObject(UTILIZATION));
			grid.setUtilizationBuffer(bw);
		}
		return bw;
	}

	private static Input<? extends AvailabilityRecord> prepareAvailabilityInput(String spotTraceFilePath,
			List<SpotPrice> refSpotPrices) throws FileNotFoundException, ParseException {
		Input<? extends AvailabilityRecord> availability;
		long timeOfFirstSpotPrice = refSpotPrices.get(SpotInstaceTraceFormat.FIRST).getTime();
		long timeOfLastSpotPrice = refSpotPrices.get(SpotInstaceTraceFormat.LAST).getTime();

		long folga = TimeUtil.ONE_MONTH;
		long randomValue = (new Random()).nextInt((int) (timeOfLastSpotPrice - timeOfFirstSpotPrice - folga));

		long randomPoint = randomValue;

		availability = new SpotPriceFluctuation(spotTraceFilePath, timeOfFirstSpotPrice, randomPoint);
		return availability;
	}

	private static Grid prepareGrid(CommandLine cmd) throws FileNotFoundException {
		Grid grid = null;
		File peerDescriptionFile = (File) cmd.getOptionObject(PEERS_DESCRIPTION);
		File machinesDescriptionFile = (File) cmd.getOptionObject(MACHINES_DESCRIPTION);
		grid = SystemConfigurationCommandParser.readPeersDescription(peerDescriptionFile, machinesDescriptionFile, FifoSharingPolicy.getInstance());
		return grid;
	}

	
	public static Options prepareOptions() {
		Options options = new Options();
		Option availability = new Option(AVAILABILITY, "availability", true, "Arquivo com a caracterização da disponibilidade para todos os recursos.");
		Option workload = new Option(WORKLOAD, "workload", true, "Arquivo com o workload no format GWA (Grid Workload Archive).");
		Option output = new Option(OUTPUT, "output", true, "O nome do arquivo em que o output da simulação será gravado.");
		Option utilization = new Option(UTILIZATION, "utilization", true, "Arquivo em que será registrada a utilização da grade.");
		Option machinesDescription = new Option(MACHINES_DESCRIPTION, "machinesdescription", true, "Descrição das máquinas presentes em cada peer.");
		Option peersDescription = new Option(PEERS_DESCRIPTION, "peers_description", true, "Arquivo descrevendo os peers.");
		Option allInstTypes = new Option(ALL_INSTANCE_TYPES, "all_instance_types", true, "Arquivo descrevendo todas as instâncias spot.");
		Option upp = new Option(NUM_USERS_BY_PEER, "upp", true, "O número de usuários por peer.");

		workload.setRequired(true);
		output.setRequired(true);
		peersDescription.setRequired(true);
		machinesDescription.setRequired(true);
		allInstTypes.setRequired(true);

		workload.setType(File.class);
		availability.setType(File.class);
		output.setType(File.class);
		utilization.setType(File.class);
		peersDescription.setType(File.class);
		machinesDescription.setType(File.class);
		allInstTypes.setType(File.class);
		upp.setType(Number.class);
		
		options.addOption(peersDescription);
		options.addOption(machinesDescription);
		options.addOption(utilization);
		options.addOption(availability);
		options.addOption(workload);
		options.addOption(output);
		options.addOption(allInstTypes);
		options.addOption(upp);
		options.addOption(SPOT_INSTANCES, "spot_instances", false, "Simular modelo amazon spot instances.");
		options.addOption(INSTANCE_TYPE, "instance_type", true, "Tipo de instância a ser simulada.");
		options.addOption(INSTANCE_REGION, "instance_region", true, "Região a qual a instância pertence.");
		options.addOption(INSTANCE_SO, "instance_so", true, "Sistema operacional da instância a ser simulada.");
		options.addOption(BID_VALUE, "bid_value", true, "Valor do bid para alocação de instâncias no modelo amazon spot instances..");
		options.addOption(GROUP_BY_PEER, "group_by_peer", false, "permitir apenas um usuário da cloud por peer.");
		// options.addOption(SPOT_MACHINES_SPEED, "machines_speed", true,
		// "Velocidade das máquinas spot instance.");
		options.addOption(LIMIT, "limit", true, "Número máximo de instâncias simultâneas que podem ser alocadas por usuário.");
		return options;
	}

}
