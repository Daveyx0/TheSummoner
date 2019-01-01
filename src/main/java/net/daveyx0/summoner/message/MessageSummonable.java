package net.daveyx0.summoner.message;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.daveyx0.multimob.core.MultiMob;
import net.daveyx0.multimob.util.EntityUtil;
import net.daveyx0.summoner.common.capabilities.CapabilitySummonableEntity;
import net.daveyx0.summoner.common.capabilities.ISummonableEntity;
import net.daveyx0.summoner.entity.EntitySummoningIllager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSummonable implements IMessage 
{
    private String entityId;
    private String summonerId;
    private boolean following;
    private int timeLimit;

    public MessageSummonable() { }

    public MessageSummonable(String entityInID, String summonerInID, boolean following, int timeLimit) {
        this.entityId = entityInID;
        this.summonerId = summonerInID;
        this.following = following;
        this.timeLimit = timeLimit;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	entityId = ByteBufUtils.readUTF8String(buf);
    	summonerId = ByteBufUtils.readUTF8String(buf);
    	following = buf.readBoolean();
    	timeLimit = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, entityId);
        ByteBufUtils.writeUTF8String(buf, summonerId);
        buf.writeBoolean(following);
        buf.writeInt(timeLimit);
    }
    public static class Handler implements IMessageHandler<MessageSummonable, IMessage> {
        
        @Override
        public IMessage onMessage(MessageSummonable message, MessageContext ctx) 
        {
        	MultiMob.proxy.getThreadListener(ctx).addScheduledTask(() -> {
        		
        		if(!message.entityId.isEmpty() && !message.summonerId.isEmpty())
        		{
        			EntityLivingBase entity = EntityUtil.getLoadedEntityByUUID((UUID.fromString(message.entityId)), MultiMob.proxy.getClientWorld());
        			if(entity != null && entity.hasCapability(CapabilitySummonableEntity.SUMMONABLE_ENTITY_CAPABILITY, null))
        			{
        				ISummonableEntity summonable = EntityUtil.getCapability(entity, CapabilitySummonableEntity.SUMMONABLE_ENTITY_CAPABILITY, null);
        				summonable.setSummonedEntity(true);
        				summonable.setSummoner((UUID.fromString(message.summonerId)));
        				summonable.setFollowing(message.following);
        				summonable.setTimeLimit(message.timeLimit);
        				NBTTagCompound nbttagcompound = entity.writeToNBT(new NBTTagCompound());
        				nbttagcompound.setString("Owner", message.summonerId);
        				nbttagcompound.setString("OwnerUUID", message.summonerId);
        				nbttagcompound.setBoolean("Tame", true);
        				nbttagcompound.setBoolean("Tamed", true);
        				entity.readFromNBT(nbttagcompound);

        			}
        		}
        	});
            return null;
        }
    }

}