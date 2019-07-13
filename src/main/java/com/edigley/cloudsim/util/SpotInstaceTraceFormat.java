package com.edigley.cloudsim.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.edigley.cloudsim.io.input.SpotPrice;
import com.edigley.cloudsim.io.input.SpotPriceFluctuation;

public class SpotInstaceTraceFormat {

	// 2009-11-30 19:33:57,0.039
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static SpotPrice createSpotPriceFromSpotTraceRecord(String nextLine, long startingTime) throws ParseException {
		Scanner scLine = new Scanner(nextLine);
		scLine.useDelimiter(",");
		String time = scLine.next();
		Double price = Double.parseDouble(scLine.next());
		Date instant = formatter.parse(time);
		return new SpotPrice("nome da máquina", instant, price, startingTime);
	}

	@Deprecated
	public static long extractTimeFromLastSpotPrice(String spotTraceFilePath) throws ParseException, FileNotFoundException {
		return extractLastSpotPrice(spotTraceFilePath).getTime();
	}

	@Deprecated
	public static long extractTimeFromFirstSpotPrice(String spotTraceFilePath) throws ParseException, FileNotFoundException {
		return extractFirstSpotPrice(spotTraceFilePath).getTime();
	}

	public static List<SpotPrice> extractTimeFromFirstAndLastSpotPrice(String spotTraceFilePath) throws ParseException, FileNotFoundException {
		Scanner sc = new Scanner(new File(spotTraceFilePath));
		SpotPrice firstSpotPriceRecord = SpotInstaceTraceFormat.createSpotPriceFromSpotTraceRecord(sc.nextLine(), 0);
		String lastLine = null;
		while (sc.hasNextLine()) {
			lastLine = sc.nextLine();
		}
		SpotPrice lastSpotPriceRecord = SpotInstaceTraceFormat.createSpotPriceFromSpotTraceRecord(lastLine, 0);
		List<SpotPrice> spotprices = new ArrayList<SpotPrice>();
		spotprices.add(firstSpotPriceRecord);
		spotprices.add(lastSpotPriceRecord);
		return spotprices;
	}

	@Deprecated
	public static SpotPrice extractLastSpotPrice(String spotTraceFilePath) throws ParseException, FileNotFoundException {
		Scanner sc = new Scanner(new File(spotTraceFilePath));
		String lastLine = null;
		while (sc.hasNextLine()) {
			lastLine = sc.nextLine();
		}
		SpotPrice lastSpotPriceRecord = SpotInstaceTraceFormat.createSpotPriceFromSpotTraceRecord(lastLine, 0);
		return lastSpotPriceRecord;
	}

	@Deprecated
	public static SpotPrice extractFirstSpotPrice(String spotTraceFilePath) throws ParseException, FileNotFoundException {
		Scanner sc = new Scanner(new File(spotTraceFilePath));
		SpotPrice firstSpotPriceRecord = SpotInstaceTraceFormat.createSpotPriceFromSpotTraceRecord(sc.nextLine(), 0);
		return firstSpotPriceRecord;
	}

	@Deprecated
	public static SpotPrice extractHighestSpotPrice(String spotTraceFilePath) throws ParseException, FileNotFoundException {
		SpotPriceFluctuation spotTrace = new SpotPriceFluctuation(spotTraceFilePath);
		SpotPrice highestSpotPrice = new SpotPrice("", 0, Double.MIN_VALUE, 0);
		while (spotTrace.peek() != null) {
			SpotPrice polledSpotPrice = spotTrace.poll();
			if (polledSpotPrice.getPrice() > highestSpotPrice.getPrice()) {
				highestSpotPrice = polledSpotPrice;
			}
		}
		return highestSpotPrice;
	}

	@Deprecated
	public static SpotPrice extractlowestSpotPrice(String spotTraceFilePath) throws ParseException, FileNotFoundException {
		SpotPriceFluctuation spotTrace = new SpotPriceFluctuation(spotTraceFilePath);
		SpotPrice lowestSpotPrice = new SpotPrice("", 0, Double.MAX_VALUE, 0);
		while (spotTrace.peek() != null) {
			SpotPrice polledSpotPrice = spotTrace.poll();
			if (polledSpotPrice.getPrice() < lowestSpotPrice.getPrice()) {
				lowestSpotPrice = polledSpotPrice;
			}
		}
		return lowestSpotPrice;
	}

	public static final int FIRST = 0;
	public static final int LOWEST = 1;
	public static final int MEAN = 2;
	public static final int HIGHEST = 3;
	public static final int LAST = 4;

	public static List<SpotPrice> extractReferenceSpotPrices(String spotTraceFilePath) throws ParseException, FileNotFoundException {

		SpotPrice firstSpotPriceRecord = null;
		SpotPrice lowestSpotPrice = new SpotPrice("", 0, Double.MAX_VALUE, 0);
		SpotPrice meanSpotPrice = null;
		SpotPrice highestSpotPrice = new SpotPrice("", 0, Double.MIN_VALUE, 0);
		SpotPrice lastSpotPriceRecord = null;

		double sumOfSpotPrices = 0;
		long numOfSpotPrices = 0;
		boolean first = true;
		SpotPriceFluctuation spotTrace = new SpotPriceFluctuation(spotTraceFilePath);
		while (spotTrace.peek() != null) {
			SpotPrice polledSpotPrice = spotTrace.poll();
			if (first) {
				firstSpotPriceRecord = polledSpotPrice;
				first = false;
			}
			if (polledSpotPrice.getPrice() < lowestSpotPrice.getPrice()) {
				lowestSpotPrice = polledSpotPrice;
			}
			if (polledSpotPrice.getPrice() > highestSpotPrice.getPrice()) {
				highestSpotPrice = polledSpotPrice;
			}
			lastSpotPriceRecord = polledSpotPrice;
			sumOfSpotPrices += polledSpotPrice.getPrice();
			numOfSpotPrices++;
		}
		meanSpotPrice = new SpotPrice("", 0, sumOfSpotPrices / (numOfSpotPrices * 1.0), 0);
		List<SpotPrice> spotprices = new ArrayList<SpotPrice>();
		spotprices.add(FIRST, firstSpotPriceRecord);
		spotprices.add(LOWEST, lowestSpotPrice);
		spotprices.add(MEAN, meanSpotPrice);
		spotprices.add(HIGHEST, highestSpotPrice);
		spotprices.add(LAST, lastSpotPriceRecord);
		return spotprices;
	}

}
