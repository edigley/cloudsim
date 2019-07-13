package br.edu.ufcg.lsd.spotinstancessimulator.io.output;

import java.io.File;
import java.io.IOException;

import br.edu.ufcg.lsd.oursim.dispatchableevents.Event;
import br.edu.ufcg.lsd.oursim.entities.Task;
import br.edu.ufcg.lsd.oursim.io.output.OutputAdapter;
import br.edu.ufcg.lsd.spotinstancessimulator.dispatchableevents.spotinstances.SpotPriceEventListener;
import br.edu.ufcg.lsd.spotinstancessimulator.entities.BidValue;
import br.edu.ufcg.lsd.spotinstancessimulator.entities.SpotValue;
import br.edu.ufcg.lsd.spotinstancessimulator.io.input.SpotPrice;

public class SpotPricePrintOutput extends OutputAdapter implements SpotPriceEventListener {

	public SpotPricePrintOutput() {
		super();
	}

	public SpotPricePrintOutput(File file) throws IOException {
		super(file);
		super.appendln("type:time:value:task:machine:processor");
	}

	@Override
	public void fullHourCompleted(Event<SpotValue> spotPriceEvent) {
		BidValue bidValue = (BidValue) spotPriceEvent.getSource();
		Task Task = bidValue.getTask();
		String machineName = Task.getTaskExecution().getMachine().getName();
		int processorId = Task.getTaskExecution().getProcessor().getId();
//		super.appendln("H:" + bidValue.getTime() + ":" + bidValue.getPrice() + ":" + Task.getId() + ":" + machineName + ":" + processorId);
	}

	@Override
	public void newSpotPrice(Event<SpotValue> spotPriceEvent) {
		SpotPrice newSpotPrice = (SpotPrice) spotPriceEvent.getSource();
		super.appendln("N:" + newSpotPrice.getTime() + ":" + newSpotPrice.getPrice() + ":NA:NA:NA");
	}

}
