package net.daveyx0.summoner.modint;

import jeresources.api.conditionals.LightLevel;
import jeresources.api.drop.LootDrop;
import net.daveyx0.multimob.modint.JustEnoughResourcesIntegration;
import net.daveyx0.summoner.core.TheSummonerLootTables;
import net.daveyx0.summoner.entity.EntitySummoner;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootTableList;

public class TheSummonerJERIntegration extends JustEnoughResourcesIntegration {

	@Override
	public void init() {		
				super.init();
				//Loottable mob loot
				jerAPI.getMobRegistry().register(new EntitySummoner(world), LightLevel.hostile, TheSummonerLootTables.ENTITIES_SUMMONER);
				jerAPI.getMobRegistry().register(new EntitySummoner(world).setBoss(true), LightLevel.hostile, TheSummonerLootTables.ENTITIES_SUMMONER_BOSS);

	}
}