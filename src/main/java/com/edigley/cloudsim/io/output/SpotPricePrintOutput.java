package com.edigley.cloudsim.io.output;

import java.io.File;
import java.io.IOException;

import com.edigley.oursim.dispatchableevents.Event;
import com.edigley.oursim.entities.Task;
import com.edigley.oursim.io.output.OutputAdapter;
import com.edigley.cloudsim.dispatchableevents.spotinstances.SpotPriceEventListener;
import com.edigley.cloudsim.entities.BidValue;
import com.edigley.cloudsim.entities.SpotValue;
import com.edigley.cloudsim.io.input.SpotPrice;

public class SpotPricePrintOutput extends OutputAdapter implements SpotPriceEventListener {

	public SpotPricePrintOutput() {
		super();
	}

	public SpotPricePrintOutput(File file) throws IOException {
		super(file);
		super.appendln("type:time:value:task:machine:processor");
	}

	public void fullHourCompleted(Event<SpotValue> spotPriceEvent) {
		BidValue bidValue = (BidValue) spotPriceEvent.getSource();
		Task Task = bidValue.getTask();
		String machineName = Task.getTaskExecution().getMachine().getName();
		//int processorId = Task.getTaskExecution().getProcessors().get(0).getId();
//		super.appendln("H:" + bidValue.getTime() + ":" + bidValue.getPrice() + ":" + Task.getId() + ":" + machineName + ":" + processorId);
	}

	public void newSpotPrice(Event<SpotValue> spotPriceEvent) {
		SpotPrice newSpotPrice = (SpotPrice) spotPriceEvent.getSource();
		super.appendln("N:" + newSpotPrice.getTime() + ":" + newSpotPrice.getPrice() + ":NA:NA:NA");
	}

}
