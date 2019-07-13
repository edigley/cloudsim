package com.edigley.cloudsim.dispatchableevents.spotinstances;

import com.edigley.oursim.dispatchableevents.Event;
import com.edigley.oursim.dispatchableevents.EventListenerAdapter;
import com.edigley.cloudsim.entities.SpotValue;

/**
 * 
 * A default (empty) implementation of the listener.
 * 
 * @author Edigley P. Fraga, edigley@lsd.ufcg.edu.br
 * @since 28/07/2010
 * 
 */
public abstract class SpotPriceEventListenerAdapter extends EventListenerAdapter implements SpotPriceEventListener {

	public void fullHourCompleted(Event<SpotValue> spotPriceEvent) {
	}

	public void newSpotPrice(Event<SpotValue> spotPriceEvent) {
	}

}
