package com.edigley.cloudsim.simulationevents;

import com.edigley.oursim.simulationevents.TimedEventAbstract;
import com.edigley.cloudsim.io.input.SpotPrice;

/**
 * 
 * The root class of all spot price's related events.
 * 
 * @author Edigley P. Fraga, edigley@lsd.ufcg.edu.br
 * @since 28/07/2010
 * 
 */
public abstract class SpotPriceTimedEvent extends TimedEventAbstract<SpotPrice> {

	public SpotPriceTimedEvent(long time, int priority, SpotPrice spotPrice) {
		super(time, priority, spotPrice);
	}

}
