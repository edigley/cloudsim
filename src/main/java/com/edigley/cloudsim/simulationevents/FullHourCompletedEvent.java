package com.edigley.cloudsim.simulationevents;

import com.edigley.oursim.simulationevents.TimedEventAbstract;
import com.edigley.cloudsim.dispatchableevents.spotinstances.SpotPriceEventDispatcher;
import com.edigley.cloudsim.entities.BidValue;

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
