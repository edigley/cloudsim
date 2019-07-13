package com.edigley.cloudsim.dispatchableevents.spotinstances;

import com.edigley.oursim.dispatchableevents.Event;
import com.edigley.oursim.dispatchableevents.EventFilter;
import com.edigley.cloudsim.entities.SpotValue;

/**
 * 
 * The filter that determines which events related to workers the listener wants
 * to be notified.
 * 
 * 
 * @author Edigley P. Fraga, edigley@lsd.ufcg.edu.br
 * @since 19/05/2010
 * 
 */
public interface SpotPriceEventFilter extends EventFilter<Event<SpotValue>> {

	/**
	 * A lenient SpotPriceEventFilter that accepts all events.
	 */
	SpotPriceEventFilter ACCEPT_ALL = new SpotPriceEventFilter() {

		public boolean accept(Event<SpotValue> spotPriceEvent) {
			return true;
		}

	};

	boolean accept(Event<SpotValue> spotPriceEvent);

}