package br.edu.ufcg.lsd.spotinstancessimulator.io.input.workload;

import java.io.FileNotFoundException;
import java.util.Map;

import br.edu.ufcg.lsd.oursim.entities.Job;
import br.edu.ufcg.lsd.oursim.entities.Peer;
import br.edu.ufcg.lsd.oursim.entities.Task;
import br.edu.ufcg.lsd.oursim.io.input.workload.IosupWorkload;

/**
 * 
 * 
 * @author Edigley P. Fraga, edigley@lsd.ufcg.edu.br
 * @since 29/07/2010
 * 
 */
public class CopyOfIosupWorkloadWithBidValue extends IosupWorkload {

	private double bidValue;

	public CopyOfIosupWorkloadWithBidValue(String workloadFilePath, Map<String, Peer> peers, long startingTime, double bidValue)
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
