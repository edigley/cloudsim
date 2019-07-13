package br.edu.ufcg.lsd.spotinstancessimulator.simulationevents;

import br.edu.ufcg.lsd.oursim.simulationevents.TimedEventAbstract;
import br.edu.ufcg.lsd.spotinstancessimulator.dispatchableevents.spotinstances.SpotPriceEventDispatcher;
import br.edu.ufcg.lsd.spotinstancessimulator.entities.BidValue;
import br.edu.ufcg.lsd.spotinstancessimulator.io.input.SpotPrice;

public class FullHourCompletedEvent extends TimedEventAbstract<BidValue> {

	public static final int PRIORITY = 0;

	public FullHourCompletedEvent(long time, BidValue bidValue) {
		super(time, PRIORITY, bidValue);
	}

	@Override
	protected void doAction() {
		SpotPriceEventDispatcher.getInstance().dispatchFullHourCompleted(source);
	}

}
