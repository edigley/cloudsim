package br.edu.ufcg.lsd.spotinstancessimulator.simulationevents;

import java.util.HashMap;
import java.util.Map;

import br.edu.ufcg.lsd.oursim.entities.Job;
import br.edu.ufcg.lsd.oursim.entities.Task;
import br.edu.ufcg.lsd.oursim.io.input.availability.AvailabilityRecord;
import br.edu.ufcg.lsd.oursim.simulationevents.ActiveEntity;
import br.edu.ufcg.lsd.oursim.simulationevents.ActiveEntityImp;
import br.edu.ufcg.lsd.oursim.simulationevents.jobevents.SubmitJobEvent;
import br.edu.ufcg.lsd.oursim.util.TimeUtil;
import br.edu.ufcg.lsd.spotinstancessimulator.entities.BidValue;
import br.edu.ufcg.lsd.spotinstancessimulator.io.input.SpotPrice;

/**
 * 
 * A default, convenient implementation of an {@link ActiveEntity}.
 * 
 * @author Edigley P. Fraga, edigley@lsd.ufcg.edu.br
 * @since 01/06/2010
 * 
 */
public class SpotInstancesActiveEntity extends ActiveEntityImp {

	private Map<Task, FullHourCompletedEvent> task2FullHour = new HashMap<Task, FullHourCompletedEvent>();

	public void addNewSpotPriceEvent(SpotPrice spotPrice) {
		this.getEventQueue().addEvent(new NewSpotPriceEvent(spotPrice));
	}

	public void addFullHourCompletedEvent(BidValue bidValue) {
		long oneHourFromNow = getCurrentTime() + TimeUtil.ONE_HOUR;
		FullHourCompletedEvent fullHourCompletedEvent = new FullHourCompletedEvent(oneHourFromNow, bidValue);
		this.getEventQueue().addEvent(fullHourCompletedEvent);
		bidValue.setTime(oneHourFromNow);
		this.task2FullHour.put(bidValue.getTask(), fullHourCompletedEvent);
	}

	public void addComplementaryHourCompletedEvent(BidValue bidValue, BidValue oldBidValue) {
		// long oneHourFromComplementary =
		// task2FullHour.get(oldBidValue.getTask()).getTime();
		// FullHourCompletedEvent fullHourCompletedEvent = new
		// FullHourCompletedEvent(oneHourFromComplementary, bidValue);
		// this.getEventQueue().addEvent(fullHourCompletedEvent);
		// this.task2FullHour.put(bidValue.getTask(), fullHourCompletedEvent);

//		this.addFullHourCompletedEvent(oldBidValue);
	}

	@Override
	public void addAvailabilityRecordEvent(long time, AvailabilityRecord avRecord) {
		if (avRecord instanceof SpotPrice) {
			this.getEventQueue().addEvent(new NewSpotPriceEvent((SpotPrice) avRecord));
		} else {
			this.addWorkerAvailableEvent(time, avRecord.getMachineName(), avRecord.getDuration());
		}
	}

	@Override
	public void addSubmitJobEvent(long submitTime, Job job) {
		assert submitTime >= getCurrentTime() : submitTime + " >= " + getCurrentTime();
		this.getEventQueue().addEvent(new SubmitJobEvent(submitTime, job));
	}

	// public static void main(String[] args) {
	// AvailabilityRecord av = new AvailabilityRecord("", 1324l, 25);
	// SpotPrice sp = new SpotPrice("", new Date(), 123d);
	//		
	// System.out.println(sp instanceof AvailabilityRecord);
	// System.out.println(AvailabilityRecord.class.isInstance(sp));
	//		
	// System.out.println(av.getClass().getCanonicalName().equals(AvailabilityRecord.class.getCanonicalName()));
	// System.out.println(av.getClass() == AvailabilityRecord.class);
	//		
	// System.out.println(sp.getClass().getCanonicalName().equals(AvailabilityRecord.class.getCanonicalName()));
	// System.out.println(sp.getClass() == AvailabilityRecord.class);
	//		
	// System.out.println(sp.getClass().getCanonicalName().equals(SpotPrice.class.getCanonicalName()));
	// System.out.println(sp.getClass() == SpotPrice.class);
	// }

}
