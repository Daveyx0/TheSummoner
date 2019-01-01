package net.daveyx0.summoner.core;

import net.daveyx0.summoner.common.capabilities.CapabilitySummonableEntity;

public class TheSummonerCapabilities {

	public static void preInit()
	{
		CapabilitySummonableEntity.register();
	}
}
