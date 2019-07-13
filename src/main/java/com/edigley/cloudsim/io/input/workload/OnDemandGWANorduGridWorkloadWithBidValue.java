package com.edigley.cloudsim.io.input.workload;

import java.io.FileNotFoundException;
import java.util.Map;

import com.edigley.oursim.entities.Job;
import com.edigley.oursim.entities.Peer;
import com.edigley.oursim.entities.Task;
import com.edigley.oursim.io.input.workload.OnDemandBoTGWANorduGridWorkload;

/**
 * 
 * 
 * @author Edigley P. Fraga, edigley@lsd.ufcg.edu.br
 * @since 29/07/2010
 * 
 */
public class OnDemandGWANorduGridWorkloadWithBidValue extends OnDemandBoTGWANorduGridWorkload {

	private double bidValue;

	public OnDemandGWANorduGridWorkloadWithBidValue(String workloadFilePath, Map<String, Peer> peers, long startingTime, double bidValue)
			throws FileNotFoundException {
		super(workloadFilePath, peers, startingTime);
		this.bidValue = bidValue;
	}

	@Override
	public Job peek() {
		Job nextJob = super.peek();
		if (nextJob != null) {
			for (Task Task : nextJob.getTasks()) {
				Task.setBidValue(bidValue);
			}
		}
		return nextJob;
	}

}
