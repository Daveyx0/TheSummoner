package net.daveyx0.summoner.core;

import java.io.File;
import java.io.InputStream;

import net.daveyx0.summoner.common.TheSummonerCommonProxy;
import net.daveyx0.summoner.config.TheSummonerConfig;
import net.daveyx0.summoner.entity.EntitySummoningIllager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid= TheSummonerReference.MODID, name = TheSummonerReference.NAME, version = TheSummonerReference.VERSION, acceptedMinecraftVersions = "[1.12]",	
guiFactory = "net.daveyx0.summoner.config.TheSummonerFactoryGui", dependencies= "required-after:multimob")

public class TheSummoner {
	
	@Instance(TheSummonerReference.MODID)
	public static TheSummoner instance = new TheSummoner();
	
	@SidedProxy(clientSide = "net.daveyx0.summoner.client.TheSummonerClientProxy", serverSide = "net.daveyx0.summoner.common.TheSummonerCommonProxy")
	public static TheSummonerCommonProxy proxy;
	private File directory;

	@EventHandler
	public void PreInit(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new TheSummoner());
		
		directory = new File(event.getModConfigurationDirectory(), TheSummonerReference.MODID);
		
		if(!directory.exists())
		{
			directory.mkdirs();
		}

		TheSummonerConfig.load(event);
		
		TheSummonerMessageRegistry.registerMessages();
		TheSummonerEntityRegistry.registerEntities();
		
		TheSummonerCapabilities.preInit();
		TheSummonerLootTables.registerLootTables();
		proxy.preInit(event);
	}
	
	@EventHandler
	public void Init(FMLInitializationEvent event)
	{
		TheSummonerSpawnRegistry.registerSpawns();
		proxy.init(event);
	}
	

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		SummonGroupRegistry.registerSummonGroups();
		proxy.postInit(event);
	}
	
	public File getDirectory()
	{
		return directory;
	}
		
}
