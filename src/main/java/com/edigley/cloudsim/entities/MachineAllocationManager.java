package com.edigley.cloudsim.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.edigley.oursim.entities.Machine;

public class MachineAllocationManager {

	private long nextMachineId = 1;
	
	private int limitPerUser;
	
	private Map<String, Integer> numberOfAllocatedMachinesForUser;

	private Map<String, List<Machine>> allocatedMachinesForUser;

	public MachineAllocationManager(int limitPerUser) {
		this.limitPerUser = limitPerUser;
		this.allocatedMachinesForUser = new HashMap<String, List<Machine>>();
		this.numberOfAllocatedMachinesForUser = new HashMap<String, Integer>();
	}

	public void registerUser(String cloudUserId) {
		if (!numberOfAllocatedMachinesForUser.containsKey(cloudUserId)) {
			this.numberOfAllocatedMachinesForUser.put(cloudUserId, 0);
			this.allocatedMachinesForUser.put(cloudUserId, new ArrayList<Machine>());
		}
	}

	public List<Machine> getAllocatedMachines(String cloudUserId) {
		return this.allocatedMachinesForUser.get(cloudUserId);
	}

	public Machine allocateNewMachine(String cloudUserId, EC2Instance ec2Instance) {
		assert cloudUserId != null && !cloudUserId.trim().isEmpty() && ec2Instance != null;
		
		String machineName = cloudUserId + "-m_" + nextMachineId++;
		Machine newMachine = new Machine(machineName, ec2Instance.speedPerCore, ec2Instance.numCores);

		this.numberOfAllocatedMachinesForUser.put(cloudUserId, this.numberOfAllocatedMachinesForUser.get(cloudUserId) + 1);
		this.allocatedMachinesForUser.get(cloudUserId).add(newMachine);
		
		assert this.numberOfAllocatedMachinesForUser.get(cloudUserId) <= limitPerUser;
		return newMachine;
	}

	public boolean canAllocateNewInstance(String cloudUserId, EC2Instance ec2Instance) {
		//TODO consider also the ec2Instance type in this decision
		return this.numberOfAllocatedMachinesForUser.get(cloudUserId) < limitPerUser;
	}

	public boolean deallocateMachine(String cloudUserId, Machine machine) {
		this.numberOfAllocatedMachinesForUser.put(cloudUserId, this.numberOfAllocatedMachinesForUser.get(cloudUserId) - 1);
		return this.allocatedMachinesForUser.get(cloudUserId).remove(machine);
	}
	
}
