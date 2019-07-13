package com.edigley.cloudsim.dispatchableevents.spotinstances;

import com.edigley.oursim.dispatchableevents.Event;
import com.edigley.oursim.dispatchableevents.EventListener;
import com.edigley.cloudsim.entities.SpotValue;

/**
 * 
 * 
 * @author Edigley P. Fraga, edigley@lsd.ufcg.edu.br
 * @since 28/07/2010
 * 
 */
public interface SpotPriceEventListener extends EventListener {

	void newSpotPrice(Event<SpotValue> spotPriceEvent);

	void fullHourCompleted(Event<SpotValue> spotPriceEvent);

}
