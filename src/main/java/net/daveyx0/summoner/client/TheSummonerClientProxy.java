package net.daveyx0.summoner.client;

import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.daveyx0.summoner.common.TheSummonerCommonProxy;
import net.daveyx0.summoner.config.TheSummonerConfig;
import net.daveyx0.summoner.core.TheSummonerEntityRegistry;
import net.daveyx0.summoner.core.TheSummonerItems;
import net.daveyx0.summoner.core.TheSummonerReference;

@Mod.EventBusSubscriber(value= Side.CLIENT, modid = TheSummonerReference.MODID)

public class TheSummonerClientProxy extends TheSummonerCommonProxy {
	
	 @Override
	    public void preInit(FMLPreInitializationEvent event) {
	        OBJLoader.INSTANCE.addDomain(TheSummonerReference.MODID);
	        TheSummonerEntityRegistry.registerRenderers();
	        super.preInit(event);
	    }

	    @Override
	    public void init(FMLInitializationEvent event) {
	    	super.init(event);
	    }

	    @Override
	    public void postInit(FMLPostInitializationEvent event) {
	    	TheSummonerItems.registerItemColors();
	    	if(TheSummonerConfig.getSummonOverlay())
	    	{
		    	RenderManagerSummon.addRenderLayers();
	    	}
	    	super.postInit(event);

	    }
	    
}