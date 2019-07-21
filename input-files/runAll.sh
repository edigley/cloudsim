#!/bin/bash
#for type in c5.18xlarge c5.9xlarge c5.4xlarge c5.2xlarge c5.xlarge c5.large m1.small m1.large m1.xlarge c1.medium c1.xlarge m2.xlarge m2.2xlarge m2.4xlarge; do for l in 1 2 4 8 16 32 64 128; do java -jar cloudsim-0.0.1-SNAPSHOT.jar -spot -l $l -bid max -w input-files/jonquera_single_core_8_workers_trace_mwa.txt -av input-files/us-east-1.linux.$type.csv -pd input-files/jonquera_site_description.txt -md input-files/jonquera_machine_speed.txt -ait input-files/jonquera_ec2_instances.txt -o spot-trace-persistent_output.txt | tail -n1 | sed "s/#//g" | awk '{print $17" "$18" "$2" "$6" "$13" "$9" "$12" "$8}'; done; done

instances="c5.18xlarge c5.9xlarge c5.4xlarge c5.2xlarge c5.xlarge c5.large m1.small m1.large m1.xlarge c1.medium c1.xlarge m2.xlarge m2.2xlarge m2.4xlarge"
limits="1 2 4 8 16 32 64 128"

#workload="input-files/jonquera_single_core_8_workers_trace_mwa.txt"
workload="input-files/jonquera_tac_16_workers_trace_mwa.txt"
siteDescription="input-files/jonquera_site_description.txt"
machineDescription="input-files/jonquera_machine_speed.txt"
instanceTypeDescription="input-files/jonquera_ec2_instances.txt"
output="spot-trace-persistent_output.txt"

echo "instance nNodes nJobs nTasks costPerTask sumOfTasksMakespan totalCost sumOfJobsMakespan"

for type in $instances; do 
	for l in $limits; do 
		java -jar cloudsim-0.0.1-SNAPSHOT.jar -spot -l $l -bid max -w $workload -av input-files/us-east-1.linux.$type.csv -pd $siteDescription -md $machineDescription -ait $instanceTypeDescription -sch tsp -o $output | tail -n1 | sed "s/#//g" | awk '{print $17" "$18" "$2" "$6" "$13" "$9" "$12" "$8}'; 
	done; 
done;

exit 0;