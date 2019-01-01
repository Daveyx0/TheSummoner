package net.daveyx0.summoner.core;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;

public class TheSummonerLootTables {

	public static final ResourceLocation ENTITIES_SUMMONER = new ResourceLocation(TheSummonerReference.MODID + ":" + "entities/summoner");
	public static final ResourceLocation ENTITIES_SUMMONER_BOSS = new ResourceLocation(TheSummonerReference.MODID + ":" + "entities/summoner_boss");
	
	public static void registerLootTables() {
		LootTableList.register(ENTITIES_SUMMONER);
		LootTableList.register(ENTITIES_SUMMONER_BOSS);
	}
}
