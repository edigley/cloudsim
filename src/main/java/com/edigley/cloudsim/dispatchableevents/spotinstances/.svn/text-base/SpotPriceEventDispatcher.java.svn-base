package br.edu.ufcg.lsd.spotinstancessimulator.dispatchableevents.spotinstances;

import br.edu.ufcg.lsd.oursim.dispatchableevents.Event;
import br.edu.ufcg.lsd.oursim.dispatchableevents.EventDispatcher;
import br.edu.ufcg.lsd.spotinstancessimulator.entities.SpotValue;

/**
 * 
 * A dispatcher to the spot price's related events.
 * 
 * @author Edigley P. Fraga, edigley@lsd.ufcg.edu.br
 * @since 28/07/2010
 * 
 * @see {@link SpotPriceEventListener}
 * @see {@link SpotPriceEventFilter}
 * 
 */
public class SpotPriceEventDispatcher extends EventDispatcher<SpotValue, SpotPriceEventListener, SpotPriceEventFilter> {

	private static SpotPriceEventDispatcher instance = null;

	private SpotPriceEventDispatcher() {
		super();
	}

	public static SpotPriceEventDispatcher getInstance() {
		return instance = (instance != null) ? instance : new SpotPriceEventDispatcher();
	}

	@Override
	public void addListener(SpotPriceEventListener listener) {
		if (!this.getListeners().contains(listener)) {
			this.getListeners().add(listener);
			this.getListenerToFilter().put(listener, SpotPriceEventFilter.ACCEPT_ALL);
		} else {
			assert false;
		}
		assert this.getListenerToFilter().get(listener) != null;
	}

	@Override
	public boolean removeListener(SpotPriceEventListener listener) {
		return this.getListeners().remove(listener);
	}

	/**
	 * @see {@link SpotPriceEventListener#newSpotPrice(Event)
	 * @param machineName
	 * @param time
	 */
	public void dispatchNewSpotPrice(SpotValue spotValue) {
		dispatch(null, new Event<SpotValue>(spotValue.getTime(), spotValue));
	}

	public void dispatchFullHourCompleted(SpotValue spotValue) {
		Event<SpotValue> spotValueEvent = new Event<SpotValue>(spotValue.getTime(), spotValue);
		for (SpotPriceEventListener listener : this.getListeners()) {
			if (this.getListenerToFilter().get(listener).accept(spotValueEvent)) {
				listener.fullHourCompleted(spotValueEvent);
			}
		}
	}

	@Override
	protected void dispatch(Enum type, Event<SpotValue> spotPriceEvent) {
		for (SpotPriceEventListener listener : this.getListeners()) {
			if (this.getListenerToFilter().get(listener).accept(spotPriceEvent)) {
				listener.newSpotPrice(spotPriceEvent);
			}
		}
	}

}
