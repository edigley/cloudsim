package br.edu.ufcg.lsd.spotinstancessimulator.entities;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class EC2Instance {

	public String type;

	public double memory;

	public long speed;

	public int numCores;

	public long speedByCore;

	public double storage;

	public String arch;

	public String group;

	public String name;

	public String fileName;

	public List<EC2InstanceBadge> badges = new ArrayList<EC2InstanceBadge>();

	public EC2InstanceBadge getBadge(String region, String so) {
		for (EC2InstanceBadge badge : badges) {
			if (badge.region.equals(region) && badge.so.equals(so)) {
				return badge;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("type", type).append("memory", memory).append("speed", speed).append(
				"storage", storage).append("arch", arch).append("badges", badges).toString();
	}

}
