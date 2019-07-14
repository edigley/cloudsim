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

import static com.edigley.oursim.ui.CLI.AVAILABILITY;
import static com.edigley.oursim.ui.CLI.EXECUTION_LINE;
import static com.edigley.oursim.ui.CLI.HELP;
import static com.edigley.oursim.ui.CLI.OUTPUT;
import static com.edigley.oursim.ui.CLI.USAGE;
import static com.edigley.oursim.ui.CLI.WORKLOAD;
import static com.edigley.oursim.ui.CLIUTil.parseCommandLine;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.time.StopWatch;

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

	public static void main(String[] args) throws Exception {

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		CommandLine cmd = parseCommandLine(args, prepareOptions(), HELP, USAGE, EXECUTION_LINE);
		
		SpotCloud spotCloud = new SpotCloud(cmd);
		spotCloud.prepare();
		spotCloud.run();

		//get simulation summary statistics
		stopWatch.stop();
		spotCloud.printSummaryStatistics(stopWatch);
		
		//release allocated resources
		spotCloud.releaseResources();

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
