package net.daveyx0.summoner.core;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.daveyx0.summoner.item.ItemSummonerOrb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;

public class SummonGroup
{
    public int id;
    public double[] particleSpeed;
    private List<NBTTagCompound> entityTags;
    public int weight;
    
    public SummonGroup(int idIn, List<NBTTagCompound> tags, int weight)
    {
        this.id = idIn;
        this.particleSpeed = new double[] {0.7D, 0.7D, 0.8D};
        this.weight = weight;
        this.entityTags = tags;
    }
    
    public int getAmountToSummon()
    {
    	if(entityTags == null || entityTags.isEmpty()) {return 0;}
    	return entityTags.size();
    }
    
    public EntityLiving getEntityFromIndex(int index, World worldServerIn, EntityLivingBase owner)
    {
    	 if(index > entityTags.size()) {return null;}
    	 
    	 EntityLiving entity = null;
    	 
    	 if(entityTags.get(index) != null)
    	 {
    		 entity = ItemSummonerOrb.getEntityWithData(entityTags.get(index), worldServerIn, owner);
    	 }
         
         return entity;
    }

}