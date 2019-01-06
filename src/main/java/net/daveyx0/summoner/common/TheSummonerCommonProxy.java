package net.daveyx0.summoner.common;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.daveyx0.multimob.modint.MMModIntegrationRegistry;
import net.daveyx0.summoner.core.TheSummonerReference;
import net.daveyx0.summoner.modint.TheSummonerJERIntegration;

@Mod.EventBusSubscriber(modid = TheSummonerReference.MODID)

public class TheSummonerCommonProxy
{


public void preInit(FMLPreInitializationEvent event) {

}

public void init(FMLInitializationEvent event) {

}

public void postInit(FMLPostInitializationEvent event) {

	if(Loader.isModLoaded("jeresources"))
	{
		MMModIntegrationRegistry.registerModIntegration(new TheSummonerJERIntegration());
	}
}

}