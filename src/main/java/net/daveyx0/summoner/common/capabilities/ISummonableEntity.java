package net.daveyx0.summoner.common.capabilities;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;

/**
 * @author Daveyx0
 **/
public interface ISummonableEntity
{
	
    /**
     * Returns if this is a summonedEntity
     *
     *
     * @return boolean is summoned
     **/
	boolean isSummonedEntity();
	
    /**
     * Set this to a summoned entity
     *
     *
     * @param set true/false if this is a summoned entity
     **/
	void setSummonedEntity(boolean set);
	
    /**
     * Returns the summoner of this summoned entity
     *
     * @param entity the entity that has been summoned (used to get entity world)
     * @return EntityLivingBase summoner
     **/
	@Nullable
    EntityLivingBase getSummoner(EntityLivingBase entity);
    
    /**
     * Returns the unique id of the Summoner
     *
     *
     * @return UUID summonerUUID
     **/
    UUID getSummonerId();
    
    /**
     * Sets the summoner of this summoned entity
     *
     *
     * @param UUID the unique id of the summoner
     **/
    void setSummoner(UUID id);
    
    /**
     * Check if the summon is following its summoner
     *
     *
     * @return boolean is following
     **/
    boolean isFollowing();
    
    /**
     * Sets if this summon should follow summoner
     *
     *
     * @param set true/false if this summon should follow
     **/
    void setFollowing(boolean set);
    
	
    /**
     * Check the time Limit for the summon
     *
     *
     * @return amount of ticks this summon will stay before returning/disappearing
     **/
	int getTimeLimit();
	
    /**
     * Set the time Limit for the summon
     *
     *
     * @param ticks amount of ticks this summon will stay before returning/disappearing. Set to -1 for infinite.
     **/
	void setTimeLimit(int ticks);

}