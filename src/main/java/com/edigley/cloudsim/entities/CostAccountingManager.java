package com.edigley.cloudsim.entities;

import java.util.HashMap;
import java.util.Map;

import com.edigley.cloudsim.io.input.SpotPrice;
import com.edigley.oursim.entities.Machine;
import com.edigley.oursim.entities.Task;
import com.edigley.oursim.util.BidirectionalMap;

public class CostAccountingManager {

	private BidirectionalMap<BidValue, Machine> allocatedMachines;
	
	private Map<Task, Double> accountedCost;
	
	public CostAccountingManager() {
		this.allocatedMachines = new BidirectionalMap<BidValue, Machine>();
		this.accountedCost = new HashMap<Task, Double>();
	}

	public void account(BidValue bidValue, Machine machine) {
		this.allocatedMachines.put(bidValue, machine);
	}

	public double finishAccounting(Machine machine, SpotPrice currentSpotPrice) {
		BidValue currentBidValue = this.allocatedMachines.getKey(machine);
		assert currentBidValue != null;
		this.allocatedMachines.remove(currentBidValue);
		Task firstAllocatedTask = currentBidValue.getTask();
		return this.updateAccounting(firstAllocatedTask, currentSpotPrice);
	}

	public BidValue getBidValue(Machine machine) {
		return this.allocatedMachines.getKey(machine);
	}

	public void startAccounting(Task task) {
		// XXX TODO Pensar nas consequências da linha abaixo!!!!
		this.accountedCost.put(task, 0.0);
	}

	public double updateAccounting(Task task, SpotPrice currentSpotPrice) {
		assert this.accountedCost.containsKey(task);
		double totalCost = this.accountedCost.get(task) + currentSpotPrice.getPrice();
		this.accountedCost.put(task, totalCost);
		task.setCost(totalCost);
		return task.getCost();
	}

}
