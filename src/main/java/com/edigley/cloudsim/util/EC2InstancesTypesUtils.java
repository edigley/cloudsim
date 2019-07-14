package com.edigley.cloudsim.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.edigley.cloudsim.entities.EC2Instance;
import com.edigley.cloudsim.parser.Ec2InstanceParser;

public class EC2InstancesTypesUtils {

	public 	static Map<String, EC2Instance> loadEC2InstancesTypes(File file) throws FileNotFoundException {
		Ec2InstanceParser parser = new Ec2InstanceParser(new FileInputStream(file));
		Map<String, EC2Instance> ec2Instances = new HashMap<String, EC2Instance>();
		try {
			List<EC2Instance> result = parser.parse();
			for (EC2Instance ec2Instance : result) {
				ec2Instances.put(ec2Instance.type, ec2Instance);
			}
		} catch (Exception e) {
			System.exit(3);
		}
		return ec2Instances;
	}
	
}
