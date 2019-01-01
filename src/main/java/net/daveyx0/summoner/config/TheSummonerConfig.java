package net.daveyx0.summoner.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.daveyx0.summoner.core.TheSummonerReference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TheSummonerConfig {

	static Configuration config;
	
	public static boolean isInEditMode;
	public static String[] mobBlackList;
	public static boolean summonOverlay;
	
	public static void load(FMLPreInitializationEvent event) {
		File dir = getConfigurationLocation(event);
		
		if(!dir.exists())
		{
			dir.mkdirs();
		}

		config = new Configuration(new File(dir, "thesummoner.cfg"));
		reloadConfig();
		
		MinecraftForge.EVENT_BUS.register(new TheSummonerConfig());
	}

	private static void reloadConfig() {
		
		String category1 = "General Options";
		config.addCustomCategoryComment(category1, "General options for the Summoner mod");
		
		String tooltip1 = "If Edit Mode is on/off";
		String description1 = "Enable/Disable edit mode for summon groups. See the curse page for more info.";
		isInEditMode = config.get(category1, tooltip1, false ,description1 ).getBoolean();
		
		String tooltip2 = "Blacklist for entities that should not be affected by the Summon Orb.";
		String description2 = "Add resource name of the entities to blacklist. Like minecraft:creeper";
		mobBlackList = config.get(category1, tooltip2, new String[]{} ,description2).getStringList();
		
		String tooltip3 = "If the summon overlay should be applied";
		String description3 = "Enable/Disable the visual overlay of summons (CLIENT only)";
		summonOverlay = config.get(category1, tooltip3, true ,description3 ).getBoolean();
		
		if (config.hasChanged()) {
			config.save();
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(TheSummonerReference.MODID)) {
			reloadConfig();
		}
	}
	
	public static File getConfigurationLocation(FMLPreInitializationEvent event)
	{
		return new File(event.getModConfigurationDirectory(), "thesummoner");
	}
	
	public static boolean getSummonOverlay()
	{
		return summonOverlay;
	}
	
	public static boolean getEditMode()
	{
		return isInEditMode;
	}
	
	public static List<String> getMobBlackList()
	{
		if(mobBlackList == null || mobBlackList.length == 0){return new ArrayList<String>();}
		
		List<String> stringList = new ArrayList<String>();
		
		for(String str: mobBlackList)
		{
			stringList.add(str);
		}
		return stringList;
	}
}