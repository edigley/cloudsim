package br.edu.ufcg.lsd.spotinstancessimulator.entities;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import br.edu.ufcg.lsd.oursim.entities.Task;

public class BidValue implements SpotValue, Comparable<BidValue> {

	private String instance;

	private Task Task;

	private long initialTime;

	private long time;

	private double value;

	public BidValue(String instance, long time, double value, Task Task) {
		this.instance = instance;
		this.initialTime = time;
		this.time = time;
		this.value = value;
		this.Task = Task;
	}

	public long getTime() {
		return time;
	}

	public double getValue() {
		return value;
	}

	@Override
	public double getPrice() {
		return getValue();
	}

	public String getInstance() {
		return instance;
	}

	@Override
	public int compareTo(BidValue o) {
		int timeDiff = (int) (this.time - o.time);
		if (timeDiff == 0) {
			return this.hashCode() - o.hashCode();
		} else {
			return timeDiff;
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("instance", instance).append("time", time).append("value", value).toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof BidValue))
			return false;
		BidValue castOther = (BidValue) other;
		return new EqualsBuilder().append(instance, castOther.instance).append(time, castOther.time).append(value, castOther.value).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(instance).append(time).append(value).toHashCode();
	}

	public Task getTask() {
		return Task;
	}

	public long getInitialTime() {
		return initialTime;
	}

	public void setTime(long time) {
		this.time = time;
	}

}
