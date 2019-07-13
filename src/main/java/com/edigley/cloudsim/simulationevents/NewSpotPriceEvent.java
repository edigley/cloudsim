package com.edigley.cloudsim.simulationevents;

import com.edigley.cloudsim.dispatchableevents.spotinstances.SpotPriceEventDispatcher;
import com.edigley.cloudsim.io.input.SpotPrice;

public class NewSpotPriceEvent extends SpotPriceTimedEvent {

	public static final int PRIORITY = 1;

	public NewSpotPriceEvent(SpotPrice spotPrice) {
		super(spotPrice.getTime(), PRIORITY, spotPrice);
	}

	@Override
	protected void doAction() {
		SpotPrice spotPrice = (SpotPrice) source;
		SpotPriceEventDispatcher.getInstance().dispatchNewSpotPrice(spotPrice);
	}

}
