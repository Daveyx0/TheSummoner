package net.daveyx0.summoner.core;

import net.daveyx0.multimob.core.MMEntityRegistry;
import net.daveyx0.summoner.client.renderer.entity.RenderSummoner;
import net.daveyx0.summoner.entity.EntitySummoner;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TheSummonerEntityRegistry extends MMEntityRegistry{

	public static int id;
	
    public static void registerEntities()
    {
    	id = 0;
    	addEntities(EntitySummoner.class, "summoner", ++id ,0x455070, 0x959B9B, true);
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerRenderers()
    {
    	RenderingRegistry.registerEntityRenderingHandler(EntitySummoner.class, RenderSummoner::new);
    }
    
    public static void addEntities(Class var1, String name1,  int entityid, int bkEggColor, int fgEggColor, boolean flag)
    {
    	addEntities(TheSummonerReference.MODID, TheSummoner.instance, var1, name1, entityid, bkEggColor, fgEggColor, flag);
    }
    
    public static void addEntitiesWithoutEgg(Class var1, String name1,  int entityid, boolean flag)
    {
    	addEntitiesWithoutEgg(TheSummonerReference.MODID, TheSummoner.instance, var1, name1, entityid, flag);
    }
    
    
    public static void addCustomEntities(Class var1, String name1,  int entityid, int track, int freq, boolean vel)
    {
    	addCustomEntities(TheSummonerReference.MODID, TheSummoner.instance, var1, name1, entityid, track, freq, vel);
    }
}