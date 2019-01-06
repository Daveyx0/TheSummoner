package net.daveyx0.summoner.common.capabilities;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import net.daveyx0.multimob.capabilities.CapabilityProviderSerializable;
import net.daveyx0.multimob.core.MMReference;
import net.daveyx0.multimob.core.MultiMob;
import net.daveyx0.multimob.entity.ai.EntityAITameableOwnerHurtByTarget;
import net.daveyx0.multimob.entity.ai.EntityAITameableOwnerHurtTarget;
import net.daveyx0.multimob.message.MMMessageRegistry;
import net.daveyx0.multimob.message.MessageMMParticle;
import net.daveyx0.multimob.util.EntityUtil;
import net.daveyx0.summoner.config.TheSummonerConfig;
import net.daveyx0.summoner.core.TheSummoner;
import net.daveyx0.summoner.core.TheSummonerReference;
import net.daveyx0.summoner.entity.EntitySummoner;
import net.daveyx0.summoner.entity.EntitySummoningIllager;
import net.daveyx0.summoner.entity.ai.EntityAISummonFollowOwner;
import net.daveyx0.summoner.entity.ai.EntityAISummonOwnerHurtByTarget;
import net.daveyx0.summoner.entity.ai.EntityAISummonOwnerHurtTarget;
import net.daveyx0.summoner.item.ItemSummonerOrb;
import net.daveyx0.summoner.message.MessageSummonable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * @author Daveyx0
 **/
public class CapabilitySummonableEntity {
	
	    @CapabilityInject(ISummonableEntity.class)
	    public static Capability<ISummonableEntity> SUMMONABLE_ENTITY_CAPABILITY = null;

	    public static final ResourceLocation capabilityID = new ResourceLocation(MMReference.MODID, "Summonable");
	    
	    public static void register()
	    {
	        CapabilityManager.INSTANCE.register(ISummonableEntity.class, new Capability.IStorage<ISummonableEntity>()
	        {
	            @Override
	            public NBTBase writeNBT(Capability<ISummonableEntity> capability, ISummonableEntity instance, EnumFacing side)
	            {
	            	NBTTagCompound compound = new NBTTagCompound();
	            	UUID summoner = instance.getSummonerId();
		            	
	            	if (summoner == null)
	                {
	                    compound.setString("SummonerUUID", "");
	                }
	                else
	                {
	                	compound.setString("SummonerUUID", summoner.toString());
	                }
	            	
	            	compound.setBoolean("Following", instance.isFollowing());
	            	compound.setInteger("TimeLimit", instance.getTimeLimit());
	            	
	                return compound;
	            }

	            @Override
	            public void readNBT(Capability<ISummonableEntity> capability, ISummonableEntity instance, EnumFacing side, NBTBase base)
	            {
	            	NBTTagCompound compound = (NBTTagCompound)base;
	                String s = "";

	                if (compound.hasKey("SummonerUUID", 8))
	                {
	                    s = compound.getString("SummonerUUID");
	                }

	                if (!s.isEmpty())
	                {
	                    try
	                    {
	                        instance.setSummoner((UUID.fromString(s)));
	                        instance.setSummonedEntity(true);
	                    }
	                    catch (Throwable var4)
	                    {
	                    	instance.setSummonedEntity(false);
	                    }
	                }
	                
	                instance.setFollowing(compound.getBoolean("Following"));
	                instance.setTimeLimit(compound.getInteger("TimeLimit"));

	            }
	        }, SummonableEntityHandler::new);
	    }
	    
	    
	//Most stuff for the Summonable Entities is done through this event handler
	@Mod.EventBusSubscriber(modid = TheSummonerReference.MODID)
	public static class EventHandler
	{
		//Attach Summonable Entity capability
		@SubscribeEvent
		public static void AttachEntityCapabilitiesEvent(AttachCapabilitiesEvent<Entity> event)
		{
			if(event.getObject() != null && event.getObject() instanceof EntityLiving)// && PrimitiveMobsConfigSpecial.getSummonEnable())
			{	
				event.addCapability(capabilityID, new CapabilityProviderSerializable(SUMMONABLE_ENTITY_CAPABILITY));
			}
		}
		
		//Assign an ItemSummonerOrb in a player inventory with entity data randomly on death
		@SubscribeEvent
		public static void EntityLivingDeathEvent(LivingDeathEvent event)
		{
			if(isEntitySuitableForSummon(event.getEntityLiving()))
			{
				ISummonableEntity summonable = EntityUtil.getCapability(event.getEntity(), SUMMONABLE_ENTITY_CAPABILITY, null);
				EntityLiving entity = (EntityLiving)event.getEntityLiving();
				if(spiritCanBeCaptured(entity, summonable, event.getSource()) || summonable != null && summonable.isSummonedEntity())
				{
					if(summonable.isSummonedEntity())
					{
						entity.playSound(SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, 1, 1);
					}
					EntityPlayer player = null;
					if(!summonable.isSummonedEntity())
					{
						player = (EntityPlayer)event.getSource().getTrueSource();
					}				
					else if(summonable.isSummonedEntity() && summonable.getSummoner(entity) instanceof EntityPlayer)
					{
						player = (EntityPlayer)summonable.getSummoner(entity);
					}
					
					if(TheSummonerConfig.getMobBlackList() != null)
					{
						for(String str: TheSummonerConfig.getMobBlackList())
						{
							if(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(str)) != null)
							{
								if(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(str)).getEntityClass() == entity.getClass())
								{
									return;
								}
							}
						}

					}
					
					if(player != null)
					{
					for(int i = 0 ; i < player.inventory.getSizeInventory() ; i++)
					{
						ItemStack item = player.inventory.getStackInSlot(i);
					
						if(!item.isEmpty())
						{
							if(item.getItem() instanceof ItemSummonerOrb && (item.getTagCompound() == null ||  !item.getTagCompound().hasKey("RegistryNameDomain")))
							{
								ItemSummonerOrb.setEntityData(item, entity, player);
								event.setCanceled(true);
								entity.setDead();
								if(!entity.getEntityWorld().isRemote)
								{
									MMMessageRegistry.getNetwork().sendToAll(new MessageMMParticle(EnumParticleTypes.ENCHANTMENT_TABLE.getParticleID(), 50, (float)entity.posX + 0.5f, (float)entity.posY + 0.5F, (float)entity.posZ + 0.5f, 0D, 0.0D,0.0D, 0));
								}
								break;
							}
						}
					
					}
					}
				}
			}
		}
		
		//Update the summoned entity AI once the entity joins the world
		@SubscribeEvent
		public static void JoinWorldEvent(EntityJoinWorldEvent event)
		{
			if(isEntitySuitableForSummon(event.getEntity()))
			{
				EntityLiving entity = (EntityLiving)event.getEntity();
				ISummonableEntity summonable = EntityUtil.getCapability(event.getEntity(), SUMMONABLE_ENTITY_CAPABILITY, null);
				if(summonable != null && summonable.isSummonedEntity())
				{
					updateEntityTargetAI(entity);
					
					entity.tasks.taskEntries.stream().filter(taskEntry -> taskEntry.action instanceof EntityAISummonFollowOwner)
					.findFirst().ifPresent(taskEntry -> entity.tasks.removeTask(taskEntry.action));
					
					if(summonable.isFollowing())
					{
						entity.tasks.addTask(3, new EntityAISummonFollowOwner(entity, 1.2D, 8.0f, 2.0f));
					}
				}
			}
		}
		
		//When a summoned entity does not follow, constantly clear the path entity, which is essentially what the sit AI does
		@SubscribeEvent
		public static void EntityUpdateEvent(LivingUpdateEvent event)
		{
			if(isEntitySuitableForSummon(event.getEntityLiving()))
			{
				EntityLiving entity = (EntityLiving)event.getEntity();
				ISummonableEntity summonable = EntityUtil.getCapability(event.getEntity(), SUMMONABLE_ENTITY_CAPABILITY, null);
				if(summonable != null && summonable.isSummonedEntity())
				{
					
		            if (entity.world.rand.nextInt(5) == 0)
		            {
		            	entity.getEntityWorld().spawnParticle(EnumParticleTypes.CRIT, entity.posX + (entity.world.rand.nextFloat() - entity.world.rand.nextFloat()), entity.posY + entity.world.rand.nextFloat() + 1D, entity.posZ + (entity.world.rand.nextFloat() - entity.world.rand.nextFloat()), 0, 0, 0);
		            }
		            if(!summonable.isFollowing())
		            {
						entity.getNavigator().clearPath();
						entity.setAIMoveSpeed(0);
						entity.setAttackTarget(null);
		            }

		           // MultiMob.LOGGER.info(summonable.getTimeLimit());
		            if(summonable.getTimeLimit() != -1)
		            {
			            if(summonable.getTimeLimit() > 0)
			            {
			            	summonable.setTimeLimit(summonable.getTimeLimit() - 1);
			            }
			            else
			            {
			            	net.minecraftforge.common.ForgeHooks.onLivingDeath(entity, DamageSource.GENERIC);
			            	entity.setHealth(0); 
			            }
		            }
				}
			}
		}
		
		//Make sure the summoned entity gets updated on client when a new player starts tracking it
		@SubscribeEvent
		public static void PlayerStartsTrackingEvent(StartTracking event)
		{
			if(!event.getEntityPlayer().getEntityWorld().isRemote && 
					isEntitySuitableForSummon(event.getTarget()))
			{
				ISummonableEntity summonable = EntityUtil.getCapability(event.getTarget(), SUMMONABLE_ENTITY_CAPABILITY, null);
				if(summonable.getSummonerId() != null)
				{
					MMMessageRegistry.getNetwork().sendToAllAround(new MessageSummonable(event.getTarget().getUniqueID().toString(), summonable.getSummonerId().toString(), summonable.isFollowing(), summonable.getTimeLimit()), 
						new TargetPoint(event.getEntityPlayer().dimension, event.getTarget().posX, event.getTarget().posY, event.getTarget().posZ, 255D));
				}
			}
		}
		
		
		@SubscribeEvent
		public static void EntityDespawnEvent(AllowDespawn event)
		{
			if(isEntitySuitableForSummon(event.getEntity()))
			{
				ISummonableEntity summonable = EntityUtil.getCapability(event.getEntity(), SUMMONABLE_ENTITY_CAPABILITY, null);
				if(summonable != null && summonable.isSummonedEntity())
				{
					if(event.getEntityLiving() != null && !(summonable.getSummoner(event.getEntityLiving()) instanceof EntitySummoningIllager))
					{
						event.setResult(Result.DENY);
					}
				}
			}
		}
		
		@SubscribeEvent
		public static void EntityDropsEvent(LivingDropsEvent event)
		{
			if(isEntitySuitableForSummon(event.getEntity()))
			{
				ISummonableEntity summonable = EntityUtil.getCapability(event.getEntity(), SUMMONABLE_ENTITY_CAPABILITY, null);
				if(summonable != null && summonable.isSummonedEntity())
				{
					event.setCanceled(true);
				}
			}
		}
		
		//Sneak + Right-click a summoned entity to make the follow or unfollow
		@SubscribeEvent
		public static void PlayerInteractEvent(EntityInteract event) throws IOException
		{
			if(isEntitySuitableForSummon(event.getTarget()) && event.getHand() == EnumHand.MAIN_HAND)
			{
				EntityLiving entity = (EntityLiving)event.getTarget();
				ISummonableEntity summonable = EntityUtil.getCapability(event.getTarget(), SUMMONABLE_ENTITY_CAPABILITY, null);
				if(summonable != null && summonable.isSummonedEntity() && summonable.getSummoner(entity) == event.getEntityPlayer())
				{
					if(event.getEntityPlayer().isSneaking())
					{
						if(!event.getEntity().getEntityWorld().isRemote)
						{
							MMMessageRegistry.getNetwork().sendToAll(new MessageMMParticle(EnumParticleTypes.NOTE.getParticleID(), 15, (float)entity.posX + 0.5f, (float)entity.posY + 0.5F, (float)entity.posZ + 0.5f, 0D, 0.0D,0.0D, 0));		
						}
						summonable.setFollowing(!summonable.isFollowing());
							MMMessageRegistry.getNetwork().sendToAllAround(new MessageSummonable(event.getTarget().getUniqueID().toString(), summonable.getSummonerId().toString(), summonable.isFollowing(), summonable.getTimeLimit()), 
							new TargetPoint(event.getEntityPlayer().dimension, event.getTarget().posX, event.getTarget().posY, event.getTarget().posZ, 255D));
					
						if(summonable.isFollowing())
						{
							updateEntityTargetAI(entity);
							event.getEntityPlayer().sendMessage(new TextComponentTranslation("%1$s is now following.",new Object[] {event.getTarget().getDisplayName()}));
							entity.tasks.addTask(3, new EntityAISummonFollowOwner(entity, 1.2D, 8.0f, 2.0f));
						}
						else
						{
							resetEntityTargetAI(entity);
							entity.tasks.taskEntries.stream().filter(taskEntry -> taskEntry.action instanceof EntityAISummonFollowOwner)
							.findFirst().ifPresent(taskEntry -> entity.tasks.removeTask(taskEntry.action));
							event.getEntityPlayer().sendMessage(new TextComponentTranslation("%1$s is now sitting.",new Object[] {event.getTarget().getDisplayName()}));
						}
					}
					else if(entity.canBeSteered())
					{
						if(!event.getEntity().getEntityWorld().isRemote)
						{
							event.getEntityPlayer().startRiding(entity);
						}
					}
				}

				if(summonable != null && (summonable.isSummonedEntity() && summonable.getSummoner(entity) == event.getEntityPlayer() || !summonable.isSummonedEntity() && event.getEntityPlayer().isCreative()) && event.getEntityPlayer().getHeldItem(event.getHand()).getItem() instanceof ItemSummonerOrb  && event.getHand() == EnumHand.MAIN_HAND)
				{
					if(TheSummonerConfig.getMobBlackList() != null)
					{
						for(String str: TheSummonerConfig.getMobBlackList())
						{
							if(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(str)) != null)
							{
								if(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(str)).getEntityClass() == entity.getClass())
								{
									return;
								}
							}
						}

					}
					if(event.getEntityPlayer().getHeldItem(event.getHand()).getTagCompound() == null || !event.getEntityPlayer().getHeldItem(event.getHand()).getTagCompound().hasKey("RegistryNameDomain"))
					{
						ItemSummonerOrb orb = (ItemSummonerOrb)event.getEntityPlayer().getHeldItem(event.getHand()).getItem();
						if(TheSummonerConfig.getEditMode()){ createSummonerCreature(getSummonGroupDirectory(), event.getEntityPlayer().getHeldItem(event.getHand()).getDisplayName(), entity.getName(), ItemSummonerOrb.getEntityNBT(entity));}
						//MultiMob.LOGGER.info(ItemSummonerOrb.getEntityNBT(entity));
						orb.setEntityData(event.getEntityPlayer().getHeldItem(event.getHand()), entity, event.getEntityPlayer());
					}
					if(!event.getEntityPlayer().getEntityWorld().isRemote)
					{
						entity.setDead();
					}
				}
			}
		}
		
		public static File getSummonGroupDirectory()
		{
			File subDirectory = new File(TheSummoner.instance.getDirectory(), "summongroups");
			
			if(!subDirectory.exists())
			{
				subDirectory.mkdirs();
			}
			
			return subDirectory;
		}
		
		public static void createSummonerCreature(File directory, String folderName, String entityName, NBTTagCompound compound)
		{
			File subDirectory = new File(directory, folderName);
			
			if(!subDirectory.exists())
			{
				subDirectory.mkdirs();
			}
			NBTTagCompound compoundo = null;
			File file = new File(subDirectory, entityName);
			try {
				CompressedStreamTools.safeWrite(compound, file);
			} catch (IOException e) {

				e.printStackTrace();
			}

		}
		
		public static void resetEntityTargetAI(EntityLiving base)
		{
			while(base.targetTasks.taskEntries.stream()
			.filter(taskEntry -> taskEntry.action instanceof EntityAIBase).findFirst().isPresent())
			{
				base.targetTasks.taskEntries.stream().filter(taskEntry -> taskEntry.action instanceof EntityAIBase)
				.findFirst().ifPresent(taskEntry -> base.targetTasks.removeTask(taskEntry.action));
			}
		}
		
		public static void updateEntityTargetAI(EntityLiving base)
		{
			while(base.targetTasks.taskEntries.stream()
			.filter(taskEntry -> taskEntry.action instanceof EntityAIBase).findFirst().isPresent())
			{
				base.targetTasks.taskEntries.stream().filter(taskEntry -> taskEntry.action instanceof EntityAIBase)
				.findFirst().ifPresent(taskEntry -> base.targetTasks.removeTask(taskEntry.action));
			}
			
			base.targetTasks.addTask(0, new EntityAISummonOwnerHurtByTarget(base));
			base.targetTasks.addTask(1, new EntityAISummonOwnerHurtTarget(base));
		}
		
		public static boolean isEntitySuitableForSummon(Entity entity)
		{
			return entity != null && entity instanceof EntityLiving && entity.hasCapability(SUMMONABLE_ENTITY_CAPABILITY, null);
		}
		
		public static boolean spiritCanBeCaptured(EntityLiving entity, ISummonableEntity summonable, DamageSource source)
		{
			return entity != null && summonable != null && !summonable.isSummonedEntity() && source != null && source.getTrueSource() != null && source.getTrueSource() instanceof EntityPlayer
					&& (int)entity.getMaxHealth() >= 0 && entity.getEntityWorld().rand.nextInt((int)entity.getMaxHealth() + 1) == 0;
		}
		
	}

}
