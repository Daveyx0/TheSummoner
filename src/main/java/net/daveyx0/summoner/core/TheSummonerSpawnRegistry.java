package net.daveyx0.summoner.core;

import net.daveyx0.multimob.spawn.MMConfigSpawnEntry;
import net.daveyx0.multimob.spawn.MMSpawnRegistry;

public class TheSummonerSpawnRegistry extends MMSpawnRegistry{

		public static void registerSpawns() {
			
			registerSpawnEntry(new MMConfigSpawnEntry("_Summoner", "thesummoner:summoner", 4, true).setupBaseMobSpawnEntry(true).setAdditionalRarity(2));
		}
}