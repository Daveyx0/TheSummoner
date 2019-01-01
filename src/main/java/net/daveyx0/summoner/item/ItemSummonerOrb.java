package net.daveyx0.summoner.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import net.daveyx0.multimob.core.MultiMob;
import net.daveyx0.multimob.message.MMMessageRegistry;
import net.daveyx0.multimob.message.MessageMMParticle;
import net.daveyx0.multimob.util.EntityUtil;
import net.daveyx0.summoner.common.capabilities.CapabilitySummonableEntity;
import net.daveyx0.summoner.common.capabilities.ISummonableEntity;
import net.daveyx0.summoner.entity.ai.EntityAISummonFollowOwner;
import net.daveyx0.summoner.message.MessageSummonable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSummonerOrb extends Item {

	public int type = 0;
	public ItemSummonerOrb(String itemName, int type) {
		super();
		this.setMaxStackSize(1);
		this.setMaxDamage(64);
		setItemName(this, itemName);
		setCreativeTab(CreativeTabs.MISC);
		this.type = type;
	}
	
	public static void setItemName(Item item, String itemName) {
		item.setRegistryName(itemName);
		item.setUnlocalizedName(item.getRegistryName().toString());
	}
	
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair)
    {
        return repair.getItem() == Items.ENDER_PEARL;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
			if(this.type == 1)
			{
				tooltip.add("\u00a71Increased Durability");
				tooltip.add("\u00a71No Summon Time Limit");
			}
			
    	NBTTagCompound nbttagcompound = stack.getTagCompound();
    	if(nbttagcompound != null && nbttagcompound.hasKey("EntityName"))
    	{
    		if(nbttagcompound.getString("EntityName") != null && !nbttagcompound.getString("EntityName").isEmpty())
    		{
        		tooltip.add("Name: " + nbttagcompound.getString("EntityName"));
            	ResourceLocation registryName = new ResourceLocation(nbttagcompound.getString("RegistryNameDomain"), nbttagcompound.getString("RegistryNamePath"));
            	tooltip.add("Type: " + registryName.toString());
            }
        	else
        	{
        		tooltip.add("empty");
        	}
    	}
    	else
    	{
    		tooltip.add("empty");
    	}
    }
    
    /**
     * Called when a Block is right-clicked with this Item
     */
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        NBTTagCompound nbttagcompound = player.getHeldItemMainhand().getTagCompound();
    	EntityLiving summonedEntity = getEntityWithData(nbttagcompound, worldIn, player);
    	if(summonedEntity != null)
    	{
        	summonedEntity.setUniqueId(UUID.randomUUID());
    		if(!worldIn.isRemote)
    		{
    			summonedEntity.setLocationAndAngles(pos.getX(), pos.getY() + 1D, pos.getZ(), player.rotationYaw, player.rotationPitch);
    			summonedEntity.setRotationYawHead(player.rotationYawHead);
    			MMMessageRegistry.getNetwork().sendToAll(new MessageMMParticle(EnumParticleTypes.ENCHANTMENT_TABLE.getParticleID(), 50, pos.getX() + 0.5f, pos.getY() + 0.5F, pos.getZ() + 0.5f, 0D, 0.0D,0.0D, 0));
    			worldIn.spawnEntity(summonedEntity);
    			if(this.type == 1)
    			{
        			player.getHeldItemMainhand().damageItem(2, player);
    			}
    			else
    			{
        			player.getHeldItemMainhand().damageItem(8, player);
    			}

        		summonedEntity.setCustomNameTag(player.getName() + "'s " + player.getHeldItemMainhand().getTagCompound().getString("EntityName"));  				
    			if(EntityUtil.getCapability(summonedEntity, CapabilitySummonableEntity.SUMMONABLE_ENTITY_CAPABILITY, null) != null)
    			{
    				ISummonableEntity summonable = EntityUtil.getCapability(summonedEntity, CapabilitySummonableEntity.SUMMONABLE_ENTITY_CAPABILITY, null);
    				summonable.setSummoner(player.getUniqueID());
    				summonable.setSummonedEntity(true);
    				summonable.setFollowing(true);
    				if(this.type == 1)
    				{
    					summonable.setTimeLimit(-1);
    				}
    				else
    				{
        				summonable.setTimeLimit(2000);
    				}

    				MMMessageRegistry.getNetwork().sendToAllAround(new MessageSummonable(summonedEntity.getUniqueID().toString(), summonable.getSummonerId().toString(), summonable.isFollowing() , summonable.getTimeLimit()), 
    						new TargetPoint(player.dimension, summonedEntity.posX, summonedEntity.posY, summonedEntity.posZ, 255D));
    				CapabilitySummonableEntity.EventHandler.updateEntityTargetAI(summonedEntity);
    				summonedEntity.tasks.addTask(3, new EntityAISummonFollowOwner(summonedEntity, 1.2D, 8.0f, 2.0f));
    			}
    		}
    		summonedEntity.playSound(SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, 1, 1);
    		setEntityData(player.getHeldItemMainhand(), null, player);
    		
    		return EnumActionResult.PASS;
    	}
        return EnumActionResult.PASS;
    }
    
    public static EntityLiving getEntityWithData(NBTTagCompound nbttagcompound, World world, EntityLivingBase owner)
    {
        if (nbttagcompound == null)
        {
            return null;
        }
        else
        {
        EntityLiving entityLiving = null;
        if(nbttagcompound.hasKey("RegistryNameDomain"))
        {

        	ResourceLocation registryName = new ResourceLocation(nbttagcompound.getString("RegistryNameDomain"), nbttagcompound.getString("RegistryNamePath"));
        	if (ForgeRegistries.ENTITIES.containsKey(registryName))
            {
        		EntityEntry entry = ForgeRegistries.ENTITIES.getValue(registryName);

                 try
                 {
                	 entityLiving = (EntityLiving)entry.getEntityClass().getConstructor(new Class[] {World.class}).newInstance(new Object[] {world});
                 }
                 catch (Exception exception)
                 {
                     exception.printStackTrace();
                 }
            }
        }
        	
        	if(entityLiving != null)
        	{
				nbttagcompound.setString("Owner", owner.getUniqueID().toString());
				nbttagcompound.setString("OwnerUUID", owner.getUniqueID().toString());
				nbttagcompound.setBoolean("Tame", true);
				nbttagcompound.setBoolean("Tamed", true);
        		entityLiving.readFromNBT(nbttagcompound);
        	}

        	return entityLiving;
        }
        
    }
    
    public static void setEntityData(ItemStack stack, EntityLiving entity, EntityPlayer player)
    {
    	if(entity == null)
    	{
    		NBTTagCompound nbttagcompound = new NBTTagCompound();
    		stack.setTagCompound(nbttagcompound);
    	}
    	else
    	{
        	
        	if(EntityRegistry.getEntry(entity.getClass()) != null)
        	{
            	NBTTagCompound nbttagcompound = entity.writeToNBT(new NBTTagCompound());
            	nbttagcompound.setBoolean("PersistenceRequired", true);
            	nbttagcompound.removeTag("Dimension");
            	//nbttagcompound.removeTag("ActiveEffects");
            	nbttagcompound.removeTag("Pos");
            	nbttagcompound.removeTag("Motion");
            	nbttagcompound.removeTag("Rotation");
            	nbttagcompound.removeTag("FallDistance");
            	nbttagcompound.removeTag("Fire");
            	nbttagcompound.removeTag("NoAI");
               	if(entity.getHealth() < entity.getMaxHealth())
            	{
            		nbttagcompound.removeTag("Health");
            	}
            	nbttagcompound.removeTag("HurtTime");
            	nbttagcompound.removeTag("HurtByTimestamp");
            	nbttagcompound.removeTag("DeathTime");
        		nbttagcompound.setString("RegistryNameDomain", EntityRegistry.getEntry(entity.getClass()).getRegistryName().getResourceDomain());
        		nbttagcompound.setString("RegistryNamePath", EntityRegistry.getEntry(entity.getClass()).getRegistryName().getResourcePath());
            	
        		if(EntityRegistry.getEntry(entity.getClass()).getEgg() != null && EntityRegistry.getEntry(entity.getClass()).getEgg().primaryColor != 0)
        		{
            	NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");

                if (!nbttagcompound.hasKey("display", 10))
                {
                    nbttagcompound.setTag("display", nbttagcompound1);
                }
                
                nbttagcompound1.setInteger("color", EntityRegistry.getEntry(entity.getClass()).getEgg().primaryColor);
        		}
            	
        		String name = entity.getName();
        		String message = "";
        		if(name.contains("'s "))
        		{
        			//MultiMob.gotHere();
        			String[] names = name.split("'s ");
        			name = names[1];
        			message = "Spirit Returned: " + name;
        		}
        		else
        		{
        			message = "Spirit Captured: " + name;        			
        		}
        		
            	nbttagcompound.setString("EntityName", name);
            	
        		
        		if(!player.getEntityWorld().isRemote)
        		{
        			player.sendStatusMessage(new TextComponentString(message), false);
        		}
        		
            	nbttagcompound.setUniqueId("UUID", UUID.randomUUID());

            	//Purely here for Potion Core compatibility
            	if (nbttagcompound.hasKey("ForgeData"))
            	{ 
            		List<String> keysToRemove = new ArrayList<String>();
            		NBTTagCompound customEntityData = nbttagcompound.getCompoundTag("ForgeData");
            		for(String string : customEntityData.getKeySet())
            		{
            		if(string.contains("Potion Core"))
            		{
            			keysToRemove.add(string);
            		}
            		}
            		
                	if(keysToRemove != null && !keysToRemove.isEmpty())
                	{
                		for(String key: keysToRemove)
                		{
                			customEntityData.removeTag(key);
                		}
                	}
            	}
            	/*
            	if (nbttagcompound.hasKey("Attributes", 9))
            	{ 
            		List<Integer> idsToRemove = new ArrayList<Integer>();
            		NBTTagList tagList = nbttagcompound.getTagList("Attributes", 10);
            		
            		while(getIdForAttributeFromMod(tagList,"potioncore") != -1)
            		{
            	    	tagList.removeTag(getIdForAttributeFromMod(tagList,"potioncore"));
            		}
            	}*/
            	//MultiMob.LOGGER.info(nbttagcompound.toString());

                stack.setTagCompound(nbttagcompound);
        	}
        	else
        	{
        		return;
        	}
    	}
    }
    
    public static int getIdForAttributeFromMod(NBTTagList tagList, String string){

        for (int i = 0; i < tagList.tagCount(); ++i)
        {
            NBTTagCompound nbt = tagList.getCompoundTagAt(i);
            if(nbt.getString("Name").contains(string))
            {
            	return i;
            }
        }
        return -1;
    }
    
    public static NBTTagCompound getEntityNBT(EntityLiving entity)
    {
    	if(entity != null)
    	{ 	
        	if(EntityRegistry.getEntry(entity.getClass()) != null)
        	{
            	NBTTagCompound nbttagcompound = entity.writeToNBT(new NBTTagCompound());
            	nbttagcompound.setBoolean("PersistenceRequired", true);
            	nbttagcompound.removeTag("Dimension");
            	nbttagcompound.removeTag("Pos");
            	nbttagcompound.removeTag("Motion");
            	nbttagcompound.removeTag("Rotation");
            	nbttagcompound.removeTag("FallDistance");
            	nbttagcompound.removeTag("Fire");
            	nbttagcompound.removeTag("UUID");
            	nbttagcompound.removeTag("NoAI");
            	nbttagcompound.removeTag("HurtTime");
            	nbttagcompound.removeTag("HurtByTimestamp");
            	nbttagcompound.removeTag("DeathTime");
        		nbttagcompound.setString("RegistryNameDomain", EntityRegistry.getEntry(entity.getClass()).getRegistryName().getResourceDomain());
        		nbttagcompound.setString("RegistryNamePath", EntityRegistry.getEntry(entity.getClass()).getRegistryName().getResourcePath());
        		return nbttagcompound;
        	}
    	}
    	
    	return new NBTTagCompound();
    }
    
    public static NBTTagCompound getEntityNBT(String resourceDomain, String resourcePath)
    {
    	ResourceLocation registryName = new ResourceLocation(resourceDomain, resourcePath);
    	EntityEntry entry = ForgeRegistries.ENTITIES.getValue(registryName);
    	if(entry != null)
    	{ 	
            	NBTTagCompound nbttagcompound = new NBTTagCompound();
            	nbttagcompound.setBoolean("PersistenceRequired", true);
            	nbttagcompound.removeTag("Dimension");
            	nbttagcompound.removeTag("Pos");
            	nbttagcompound.removeTag("Motion");
            	nbttagcompound.removeTag("Rotation");
            	nbttagcompound.removeTag("FallDistance");
            	nbttagcompound.removeTag("Fire");
            	nbttagcompound.removeTag("UUID");
            	nbttagcompound.removeTag("NoAI");
            	nbttagcompound.removeTag("HurtTime");
            	nbttagcompound.removeTag("HurtByTimestamp");
            	nbttagcompound.removeTag("DeathTime");
        		nbttagcompound.setString("RegistryNameDomain", resourceDomain);
        		nbttagcompound.setString("RegistryNamePath", resourcePath);
        		return nbttagcompound;
    	}
    	
    	return new NBTTagCompound();
    }
    
    /**
     * Return the color for the specified armor ItemStack.
     */ 
    public static int getColor(ItemStack stack)
    {
            NBTTagCompound nbttagcompound = stack.getTagCompound();

            if (nbttagcompound != null)
            {
                NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");

                if (nbttagcompound1 != null && nbttagcompound1.hasKey("color", 3))
                {
                    return nbttagcompound1.getInteger("color");
                }
            }

            return 16777215;
    }
    
	 
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack)
    {
        return type == 1 ? true : false;
    }

}
