package br.edu.ufcg.lsd.spotinstancessimulator.dispatchableevents.spotinstances;

import br.edu.ufcg.lsd.oursim.dispatchableevents.Event;
import br.edu.ufcg.lsd.oursim.dispatchableevents.EventFilter;
import br.edu.ufcg.lsd.spotinstancessimulator.entities.SpotValue;

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

		@Override
		public boolean accept(Event<SpotValue> spotPriceEvent) {
			return true;
		}

	};

	@Override
	boolean accept(Event<SpotValue> spotPriceEvent);

}