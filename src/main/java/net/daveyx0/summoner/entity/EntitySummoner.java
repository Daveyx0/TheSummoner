package net.daveyx0.summoner.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import net.daveyx0.multimob.core.MultiMob;
import net.daveyx0.multimob.entity.IMultiMob;
import net.daveyx0.multimob.entity.ai.EntityAIBackOffFromEntity;
import net.daveyx0.multimob.message.MMMessageRegistry;
import net.daveyx0.multimob.message.MessageMMParticle;
import net.daveyx0.multimob.util.EntityUtil;
import net.daveyx0.summoner.common.capabilities.CapabilitySummonableEntity;
import net.daveyx0.summoner.common.capabilities.ISummonableEntity;
import net.daveyx0.summoner.core.SummonGroup;
import net.daveyx0.summoner.core.SummonGroupRegistry;
import net.daveyx0.summoner.core.TheSummonerLootTables;
import net.daveyx0.summoner.entity.ai.EntityAINearestAttackableTargetExceptSummons;
import net.daveyx0.summoner.entity.ai.EntityAISummonFollowOwner;
import net.daveyx0.summoner.message.MessageSummonable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpellcasterIllager;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class EntitySummoner extends EntitySummoningIllager implements IMultiMob {
	
	private final BossInfoServer bossInfo = (BossInfoServer)(new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS)).setDarkenSky(true);
	private static final DataParameter<Boolean> IS_BOSS = EntityDataManager.<Boolean>createKey(EntitySummoner.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> INVINSIBLE_TIMER = EntityDataManager.<Integer>createKey(EntitySummoner.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> WAVE = EntityDataManager.<Integer>createKey(EntitySummoner.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> SHOULD_SPAWN_WAVE = EntityDataManager.<Boolean>createKey(EntitySummoner.class, DataSerializers.BOOLEAN);
	
	public EntitySummoner(World worldIn) {

		super(worldIn);
        this.setSize(0.6F, 1.95F);
        this.experienceValue = 10;
        bossInfo.setVisible(false);
	}

    protected void initEntityAI()
    {
        super.initEntityAI();
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(3, new EntityAIBackOffFromEntity(this, 9D, true));
        this.tasks.addTask(4, new EntitySummoner.AISummonSpell());
        this.tasks.addTask(5, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(8, new EntityAIWander(this, 0.6D));
        this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[] {EntitySummoner.class}));
        this.targetTasks.addTask(2, (new EntityAINearestAttackableTargetExceptSummons(this, EntityPlayer.class, true)).setUnseenMemoryTicks(300));
        this.targetTasks.addTask(3, (new EntityAINearestAttackableTargetExceptSummons(this, EntityVillager.class, false)).setUnseenMemoryTicks(300));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTargetExceptSummons(this, EntityIronGolem.class, false));
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(12.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(24.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0.0D);
    }
    
    public void onUpdate()
    {
    	super.onUpdate();
    	
    	if(this.isInvinsible() && !this.isBoss())
    	{
    		this.setInvinsibleTimer(this.getInvinsibleTimer() - 1);
    	}
    	
    	if(this.isBoss() && !this.shouldSpawnWave())
    	{
    		if(this.ticksExisted % 20 == 0 && !hasSummonedEntitiesLeft())
    		{
    			this.setWave(this.getWave() + 1);

    			if(this.getWave() >= SummonGroupRegistry.BOSSSUMMONGROUPS.size())
    			{
    				this.setInvinsibleTimer(0);
    				this.setSpawnWave(false);
    				
    		    	EnumParticleTypes particle = EnumParticleTypes.WATER_SPLASH;
    				
    		    	for(int i = 0; i < 8; i++)
    		    	{
        				float f = 0.01745278F;
        		        double d = posX - Math.sin(rotationYaw * f) / 3D;
        		        double d1 = posY + rand.nextDouble() / 3D;
        		        double d2 = posZ + Math.cos(rotationYaw * f) / 3D;
        		        world.spawnParticle(particle, d, d1 + 1.8D, d2, 0.0D, 0.0D, 0.0D);
    		    	}
    			}
    			else
    			{
    				this.bossInfo.setVisible(true);
        			this.setHealth((10 * SummonGroupRegistry.BOSSSUMMONGROUPS.size()) - ((this.getWave() -1) * 10));
        			this.setSpawnWave(true);
    			}
    		}
    	}
    }
    
    public boolean processInteract(EntityPlayer player, EnumHand hand)
    {
        ItemStack itemstack = player.getHeldItem(hand);
        
        if(hand == EnumHand.MAIN_HAND && itemstack.getItem() == Items.ENDER_EYE)
        {
        	if(!player.isCreative())
        	{
        		itemstack.shrink(1);
        	}
        	
        	this.bossInfo.setVisible(true);
        	this.setBoss(true);
        	this.setInvinsibleTimer(1000);
        	this.experienceValue = 100;
        	this.setWave(1);
        	this.setSpawnWave(true);
        	this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10 * SummonGroupRegistry.BOSSSUMMONGROUPS.size());
        	this.setHealth(10 * SummonGroupRegistry.BOSSSUMMONGROUPS.size());
        	this.playSound(SoundEvents.ENTITY_WITHER_SPAWN, 1, 1);
        	MMMessageRegistry.getNetwork().sendToAll(new MessageMMParticle(EnumParticleTypes.ENCHANTMENT_TABLE.getParticleID(), 50, (float)this.posX + 0.5f, (float)this.posY + 0.5F, (float)this.posZ + 0.5f, 0D, 0.0D,0.0D, 0));
        	
        }
        
        return super.processInteract(player, hand);
    }
    
    public void setBossBarVisible(boolean b)
    {
    	this.bossInfo.setVisible(b);
    }
    
    /**
     * Called when a player attacks an entity. If this returns true the attack will not happen.
     */
    public boolean hitByEntity(Entity p_85031_1_)
    {
        if(this.isInvinsible())
        {
     	   return false;
        }
        return super.hitByEntity(p_85031_1_);
    }
    
    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
    {
       if(this.isInvinsible())
       {
    	   return false;
       }
       return super.attackEntityFrom(p_70097_1_, p_70097_2_);
    }
    
    /**
     * Returns false if this Entity is a boss, true otherwise.
     */
    public boolean isNonBoss()
    {
        return !isBoss();
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(IS_BOSS, false);
        this.dataManager.register(INVINSIBLE_TIMER, 0);
        this.dataManager.register(WAVE, 0);
        this.dataManager.register(SHOULD_SPAWN_WAVE, false);
    }
    
	public void setWave(int state) {

		 this.dataManager.set(WAVE, state);
	}
	
    public int getWave()
    {
        return ((Integer)this.dataManager.get(WAVE)).intValue();
    }
    
   protected ResourceLocation getLootTable()
   {
	   if(this.isBoss()) {return TheSummonerLootTables.ENTITIES_SUMMONER_BOSS;}
       return TheSummonerLootTables.ENTITIES_SUMMONER;
   }

   protected void updateAITasks()
   {
       super.updateAITasks();
       this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
   }

   /**
    * Add the given player to the list of players tracking this entity. For instance, a player may track a boss in
    * order to view its associated boss bar.
    */
   public void addTrackingPlayer(EntityPlayerMP player)
   {
       super.addTrackingPlayer(player);
       this.bossInfo.addPlayer(player);
   }

   /**
    * Removes the given player from the list of players tracking this entity. See {@link Entity#addTrackingPlayer} for
    * more information on tracking.
    */
   public void removeTrackingPlayer(EntityPlayerMP player)
   {
       super.removeTrackingPlayer(player);
       this.bossInfo.removePlayer(player);
   }

	
	@Override
	protected SoundEvent getSpellSound() {

		return SoundEvents.EVOCATION_ILLAGER_CAST_SPELL;
	}
	
	protected SoundEvent getAmbientSound()
    {
        return SoundEvents.ENTITY_EVOCATION_ILLAGER_AMBIENT;
    }

    protected SoundEvent getDeathSound()
    {
        return SoundEvents.EVOCATION_ILLAGER_DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return SoundEvents.ENTITY_EVOCATION_ILLAGER_HURT;
    }
    
    public boolean hasSummonedEntitiesLeft()
    {
    	for(Entity entity : this.world.loadedEntityList)
    	{
			if(CapabilitySummonableEntity.EventHandler.isEntitySuitableForSummon(entity))
			{
				ISummonableEntity summonable = EntityUtil.getCapability(entity, CapabilitySummonableEntity.SUMMONABLE_ENTITY_CAPABILITY, null);
				if(summonable != null && summonable.isSummonedEntity() && summonable.getSummonerId().equals(this.getUniqueID()))
				{
					return true;
				}
			}
    	}
    	return false;
    }
    
    class AISummonSpell extends EntitySummoningIllager.AIUseSummon
    {
        private AISummonSpell()
        {
            super();
        }
        
        public void startExecuting()
        {
        	super.startExecuting();
        	EntitySummoner.this.setInvinsibleTimer(200);
        }

        /**
         * Returns whether the EntityAIBase should begin execution.
         */
        public boolean shouldExecute()
        {
        	if(EntitySummoner.this.isBoss() && !EntitySummoner.this.shouldSpawnWave()){return false;}
        	
            if (!super.shouldExecute())
            {
                return false;
            }
            else
            {
                int i = EntitySummoner.this.world.getEntitiesWithinAABB(EntityVex.class, EntitySummoner.this.getEntityBoundingBox().grow(16.0D)).size();
                return EntitySummoner.this.rand.nextInt(8) + 1 > i;
            }
        }

        protected int getCastingTime()
        {
            return 100;
        }

        protected int getCastingInterval()
        {
            return 340;
        }

        protected void castSpell()
        {
        	SummonGroup group = this.getSummonGroup();
            for (int i = 0; i < group.getAmountToSummon(); i++)
            {
                BlockPos blockpos = (new BlockPos(EntitySummoner.this)).add(-2 + EntitySummoner.this.rand.nextInt(5), 1, -2 + EntitySummoner.this.rand.nextInt(5));
                EntityLiving entity = group.getEntityFromIndex(i, EntitySummoner.this.world, EntitySummoner.this);
                entity.setUniqueId(UUID.randomUUID());
                if(entity != null)
                {
                entity.moveToBlockPosAndAngles(blockpos, 0.0F, 0.0F);
                //entity.onInitialSpawn(EntitySummoner.this.world.getDifficultyForLocation(blockpos), (IEntityLivingData)null);
                
    			MMMessageRegistry.getNetwork().sendToAll(new MessageMMParticle(EnumParticleTypes.ENCHANTMENT_TABLE.getParticleID(), 50, blockpos.getX() + 0.5f, blockpos.getY() + 0.5F, blockpos.getZ() + 0.5f, 0D, 0.0D,0.0D, 0));
    			EntitySummoner.this.world.spawnEntity(entity);
    			
        		entity.setCustomNameTag(EntitySummoner.this.getName() + "'s " + "entity"); 
    			if(EntityUtil.getCapability(entity, CapabilitySummonableEntity.SUMMONABLE_ENTITY_CAPABILITY, null) != null)
    			{
    				ISummonableEntity summonable = EntityUtil.getCapability(entity, CapabilitySummonableEntity.SUMMONABLE_ENTITY_CAPABILITY, null);
    				summonable.setSummoner(EntitySummoner.this.getUniqueID());
    				summonable.setSummonedEntity(true);
    				summonable.setFollowing(true);
    				if(EntitySummoner.this.isBoss()){summonable.setTimeLimit(-1);}
    				else {summonable.setTimeLimit(500);}
    				MMMessageRegistry.getNetwork().sendToAllAround(new MessageSummonable(entity.getUniqueID().toString(), summonable.getSummonerId().toString(), summonable.isFollowing(), summonable.getTimeLimit()), 
    						new TargetPoint(EntitySummoner.this.dimension, entity.posX, entity.posY, entity.posZ, 255D));
    				CapabilitySummonableEntity.EventHandler.updateEntityTargetAI(entity);
    				entity.tasks.addTask(3, new EntityAISummonFollowOwner(entity, 1.2D, 8.0f, 2.0f));
    			}
                }
            }

            EntitySummoner.this.setSpawnWave(false);
        }

        protected SoundEvent getSpellPrepareSound()
        {
            return SoundEvents.EVOCATION_ILLAGER_PREPARE_SUMMON;
        }

        protected SummonGroup getSummonGroup()
        {
        	if(EntitySummoner.this.isBoss()) {return SummonGroupRegistry.getBossGroupFromId(EntitySummoner.this.getWave());}
        	
            return SummonGroupRegistry.getRandomGroup();
        }
    }
    
    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        compound.setInteger("Wave", this.getWave());
        compound.setInteger("Invinsible", this.getInvinsibleTimer());
        compound.setBoolean("Boss", this.isBoss());
        compound.setBoolean("Spawn", this.shouldSpawnWave());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        this.setWave(compound.getInteger("Wave"));
        this.setInvinsibleTimer(compound.getInteger("Invinsible"));
        this.setBoss(compound.getBoolean("Boss"));
        this.setSpawnWave(compound.getBoolean("Spawn"));
        
        if (this.hasCustomName())
        {
            this.bossInfo.setName(this.getDisplayName());
        }
    }
    
	public EntityLivingBase setBoss(boolean b) {

		this.dataManager.set(IS_BOSS, b);
		return this;
	}
	
	public boolean isBoss()
	{
		return ((Boolean)this.dataManager.get(IS_BOSS)).booleanValue();
	}
	
	public void setInvinsibleTimer(int b) {

		 this.dataManager.set(INVINSIBLE_TIMER, b);
	}
	
	public void setSpawnWave(boolean b) {

		 this.dataManager.set(SHOULD_SPAWN_WAVE, b);
	}
	
	public boolean shouldSpawnWave()
	{
		return ((Boolean)this.dataManager.get(SHOULD_SPAWN_WAVE)).booleanValue();
	}
	
	
	public int getInvinsibleTimer()
	{
		return ((Integer)this.dataManager.get(INVINSIBLE_TIMER)).intValue();
	}
	
	public boolean isInvinsible()
	{
		return ((Integer)this.dataManager.get(INVINSIBLE_TIMER)).intValue() > 0;
	}
	
    /**
     * Gets the pitch of living sounds in living entities.
     */
    protected float getSoundPitch()
    {
    	if(this.isBoss()){return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 0.5F;}
        return super.getSoundPitch();
    }

    /**
     * Sets the custom name tag for this entity
     */
    public void setCustomNameTag(String name)
    {
        super.setCustomNameTag(name);
        this.bossInfo.setName(this.getDisplayName());
    }
	
}
