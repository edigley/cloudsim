package com.edigley.cloudsim.io.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.PriorityQueue;
import java.util.Scanner;

import com.edigley.oursim.io.input.InputAbstract;
import com.edigley.cloudsim.util.SpotInstanceTraceFormat;

public class SpotPriceFluctuation extends InputAbstract<SpotPrice> {

	private final long startingTime;
	private final long randomPoint;
	private final boolean hasHeader;

	public SpotPriceFluctuation(String spotPriceFluctuationFilePath, long startingTime, long randomPoint) throws FileNotFoundException, ParseException {
		this(spotPriceFluctuationFilePath, startingTime, false, randomPoint);
	}

	public SpotPriceFluctuation(String spotPriceFluctuationFilePath) throws FileNotFoundException, ParseException {
		this(spotPriceFluctuationFilePath, 0, false, 0);
	}

	public SpotPriceFluctuation(String availabilityFilePath, long startingTime, boolean hasHeader, long randomPoint) throws FileNotFoundException,
			ParseException {
		assert startingTime >= 0;
		this.inputs = new PriorityQueue<SpotPrice>();
		this.startingTime = startingTime;
		this.hasHeader = hasHeader;
		this.randomPoint = randomPoint;
		Scanner sc = new Scanner(new File(availabilityFilePath));
		if (this.hasHeader) {
			sc.nextLine();// TODO desconsidera a primeira linha (cabeçalho)
		}
		long previousTime = -1;
		while (sc.hasNextLine()) {
			String lastReadLine = sc.nextLine();
			SpotPrice spotPriceRecord = SpotInstanceTraceFormat.createSpotPriceFromSpotTraceRecord(lastReadLine, this.startingTime);
			assert spotPriceRecord.getTime() >= previousTime;
			previousTime = spotPriceRecord.getTime();
			if (spotPriceRecord.getTime() >= this.randomPoint) {
				SpotPrice spotPrice = SpotInstanceTraceFormat.createSpotPriceFromSpotTraceRecord(lastReadLine, this.startingTime + this.randomPoint);
				this.inputs.add(spotPrice);
			}
		}
		sc.close();
	}

}
