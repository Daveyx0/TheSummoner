package net.daveyx0.summoner.core;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;import java.util.Map;
import java.util.Random;

import net.daveyx0.multimob.core.MultiMob;
import net.daveyx0.summoner.common.capabilities.CapabilitySummonableEntity;
import net.daveyx0.summoner.config.TheSummonerConfig;
import net.daveyx0.summoner.core.SummonGroup;
import net.daveyx0.summoner.item.ItemSummonerOrb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SummonGroupRegistry
{
	public static final List<SummonGroup> SUMMONGROUPS = new ArrayList<SummonGroup>();
	public static final List<SummonGroup> BOSSSUMMONGROUPS = new ArrayList<SummonGroup>();
	
	static Random rand = new Random();
	static Map<Integer, List<NBTTagCompound>> nbtTagCompounds = new HashMap<Integer, List<NBTTagCompound>>();
	static Map<Integer, Integer> weights = new HashMap<Integer, Integer>();
	
	public static void registerSummonGroups()
	{
		int id = 0;
		SUMMONGROUPS.add(new SummonGroup(id++, null, 0));
		getNBTTagCompounds("group");
		
		if(nbtTagCompounds == null || nbtTagCompounds.isEmpty()) {setupDefaultSummonGroups();}
		
		if(nbtTagCompounds != null && !nbtTagCompounds.isEmpty())
		{
			for(int i = 1; i < nbtTagCompounds.size() + 1 ; i++)
			{
				registerSummonGroup(i, nbtTagCompounds.get(i), weights.get(i));
			}
		}
		
		id = 0;
		BOSSSUMMONGROUPS.add(new SummonGroup(id++, null, 0));
		getNBTTagCompounds("bossgroup");
		
		if(nbtTagCompounds == null || nbtTagCompounds.isEmpty()) {setupDefaultBossSummonGroups();}
		
		if(nbtTagCompounds != null && !nbtTagCompounds.isEmpty())
		{
			for(int i = 1; i < nbtTagCompounds.size() + 1 ; i++)
			{
				registerBossSummonGroup(i, nbtTagCompounds.get(i));
			}
		}
	}
	
    public static SummonGroup getFromId(int idIn)
    {
        return idIn < SUMMONGROUPS.size() ? SUMMONGROUPS.get(idIn) : null;
    }
    
    public static SummonGroup getBossGroupFromId(int idIn)
    {
        return idIn < BOSSSUMMONGROUPS.size() ? BOSSSUMMONGROUPS.get(idIn) : null;
    }
    
    public static SummonGroup getRandomGroup()
    {
    	List<SummonGroup> WEIGHTEDGROUPS = new ArrayList<SummonGroup>();
    	for(SummonGroup group : SUMMONGROUPS)
    	{
    		for(int i = 0; i < group.weight; i++)
    		{
        		WEIGHTEDGROUPS.add(group);
    		}
    	}
    	
    	if(WEIGHTEDGROUPS.isEmpty()){return getFromId(0);}
    	
    	return WEIGHTEDGROUPS.get(rand.nextInt(WEIGHTEDGROUPS.size()));
    }

    
    public static void registerSummonGroup(int id, List<NBTTagCompound> nbts, int weight)
    {
    	SUMMONGROUPS.add(new SummonGroup(id, nbts, weight));
    }
    
    public static void registerBossSummonGroup(int id, List<NBTTagCompound> nbts)
    {
    	BOSSSUMMONGROUPS.add(new SummonGroup(id, nbts, 1));
    }
    
    public static void getNBTTagCompounds(String subDirectoryName)
    {
    	nbtTagCompounds = new HashMap<Integer, List<NBTTagCompound>>();
    	File mainDirectory = CapabilitySummonableEntity.EventHandler.getSummonGroupDirectory();
    	
		if(mainDirectory.listFiles() == null || mainDirectory.listFiles().length == 0)
		{
			MultiMob.LOGGER.error("TheSummoner: No Summon Groups set up. Setting up default groups.");
			return;
		}
		
		for(File file: mainDirectory.listFiles())
		{
			if(file.listFiles() == null || file.listFiles().length == 0)
			{
				MultiMob.LOGGER.error("TheSummoner: Empty Summon group found with name "  + file.getName());
				continue;
			}
			
			if(file.getName().startsWith(subDirectoryName))
			{
				String numbers = file.getName().replaceAll(subDirectoryName, "");
				int id = 0;
				int weight = 1;
				
				if(numbers.contains("_"))
				{
					String[] numberSplit = numbers.split("_");
					try
					{
						id = Integer.parseInt(numberSplit[0]);
						weight = Integer.parseInt(numberSplit[1]); 
					}
					catch (NumberFormatException e)
					{
						MultiMob.LOGGER.error("TheSummoner: Incorrect group and weight numbers for summon group: " + file.getName());
						continue;
					}
				}
				else
				{
					try
					{
						id = Integer.parseInt(numbers);
					}
					catch (NumberFormatException e)
					{
						MultiMob.LOGGER.error("TheSummoner: Incorrect group number for summon group: " + file.getName());
						continue;
					}
				}

				List<NBTTagCompound> compounds = new ArrayList<NBTTagCompound>();
					
				for(File nbtFile: file.listFiles())
				{
					NBTTagCompound tagCompound = new NBTTagCompound();
					
					try {
						tagCompound = CompressedStreamTools.read(nbtFile);
					} catch (IOException e) {

						e.printStackTrace();
					}
					
					compounds.add(tagCompound);
				}
				
				//MultiMob.LOGGER.info("Adding Summons with id: " + id + ",weight: " + weight + " and compounds: " + compounds);
				weights.put(id, weight);
				nbtTagCompounds.put(id, compounds);
			}
		}
		
    }
    
    public static void setupDefaultSummonGroups()
    {
    	nbtTagCompounds = new HashMap<Integer, List<NBTTagCompound>>();
    	List<NBTTagCompound> compounds = new ArrayList<NBTTagCompound>();
    	compounds.add(getNBTFromAssets("group1_4", "ArmoredZombie1"));
    	compounds.add(getNBTFromAssets("group1_4", "ArmoredZombie2"));
    	compounds.add(getNBTFromAssets("group1_4", "ArmoredZombie3"));
    	weights.put(1, 4);
    	nbtTagCompounds.put(1, compounds);
    	compounds = new ArrayList<NBTTagCompound>();
    	compounds.add(getNBTFromAssets("group2_3", "ResistantSkeleton1"));
    	compounds.add(getNBTFromAssets("group2_3", "ResistantSkeleton2"));
    	weights.put(2, 3);
    	nbtTagCompounds.put(2, compounds);
    	compounds = new ArrayList<NBTTagCompound>();
    	compounds.add(getNBTFromAssets("group3_1", "Blaze1"));
    	compounds.add(getNBTFromAssets("group3_1", "Blaze2"));
    	weights.put(3, 2);
    	nbtTagCompounds.put(3, compounds);
    	compounds = new ArrayList<NBTTagCompound>();
    	compounds.add(getNBTFromAssets("group4_1", "Chicken1"));
    	compounds.add(getNBTFromAssets("group4_1", "Chicken2"));
    	compounds.add(getNBTFromAssets("group4_1", "Chicken3"));
    	compounds.add(getNBTFromAssets("group4_1", "Chicken4"));
    	compounds.add(getNBTFromAssets("group4_1", "Chicken5"));
    	weights.put(4, 1);
    	nbtTagCompounds.put(4, compounds);
    	compounds = new ArrayList<NBTTagCompound>();
    	compounds.add(getNBTFromAssets("group5_1", "GoldenPigman"));
    	weights.put(5, 1);
    	nbtTagCompounds.put(5, compounds);
    }
    
    public static void setupDefaultBossSummonGroups()
    {
    	nbtTagCompounds = new HashMap<Integer, List<NBTTagCompound>>();
    	List<NBTTagCompound> compounds = new ArrayList<NBTTagCompound>();
    	compounds.add(getNBTFromAssets("bossgroup1", "ArmoredZombie1"));
    	compounds.add(getNBTFromAssets("bossgroup1", "ArmoredZombie2"));
    	compounds.add(getNBTFromAssets("bossgroup1", "ArmoredZombie3"));
    	compounds.add(getNBTFromAssets("bossgroup1", "ArmoredZombie4"));
    	compounds.add(getNBTFromAssets("bossgroup1", "ArmoredZombie5"));
    	weights.put(1, 1);
    	nbtTagCompounds.put(1, compounds);
    	compounds = new ArrayList<NBTTagCompound>();
    	compounds.add(getNBTFromAssets("bossgroup2", "ResistantSkeleton1"));
    	compounds.add(getNBTFromAssets("bossgroup2", "ResistantSkeleton2"));
    	compounds.add(getNBTFromAssets("bossgroup2", "Spider1"));
    	compounds.add(getNBTFromAssets("bossgroup2", "Spider2"));
    	compounds.add(getNBTFromAssets("bossgroup2", "Spider3"));
    	weights.put(2, 1);
    	nbtTagCompounds.put(2, compounds);
    	compounds = new ArrayList<NBTTagCompound>();
    	compounds.add(getNBTFromAssets("bossgroup3", "Blaze"));
    	compounds.add(getNBTFromAssets("bossgroup3", "WitherSkeleton1"));
    	compounds.add(getNBTFromAssets("bossgroup3", "WitherSkeleton2"));
    	compounds.add(getNBTFromAssets("bossgroup3", "WitherSkeleton3"));
    	weights.put(3, 1);
    	nbtTagCompounds.put(3, compounds);
    	compounds = new ArrayList<NBTTagCompound>();
    	compounds.add(getNBTFromAssets("bossgroup4", "GoldenPigman1"));
    	compounds.add(getNBTFromAssets("bossgroup4", "GoldenPigman2"));
    	compounds.add(getNBTFromAssets("bossgroup4", "GoldenPigman3"));
    	weights.put(4, 1);
    	nbtTagCompounds.put(4, compounds);
    	compounds = new ArrayList<NBTTagCompound>();
    	compounds.add(getNBTFromAssets("bossgroup5", "Evoker"));
    	compounds.add(getNBTFromAssets("bossgroup5", "IronGolem"));
    	compounds.add(getNBTFromAssets("bossgroup5", "Vindicator"));
    	weights.put(5, 1);
    	nbtTagCompounds.put(5, compounds);
    }
    
    public static NBTTagCompound getNBTFromAssets(String group, String fileName)
    {
    	DataInputStream stream = new DataInputStream(MultiMob.instance.getClass().getClassLoader().getResourceAsStream("assets/thesummoner/summongroups/"+ group + "/" + fileName));
		NBTTagCompound tagCompound = new NBTTagCompound();
		try {
			tagCompound = CompressedStreamTools.read(stream);
		} catch (IOException e) {

			e.printStackTrace();
		}
		return tagCompound;
    }
}
