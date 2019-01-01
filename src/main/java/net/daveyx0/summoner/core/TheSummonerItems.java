package net.daveyx0.summoner.core;

import java.util.List;

import javax.annotation.Nullable;

import net.daveyx0.multimob.client.MMItemModelManager;
import net.daveyx0.multimob.core.MMItemRegistry;
import net.daveyx0.summoner.item.ItemSummonerOrb;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

public class TheSummonerItems extends MMItemRegistry{
	 
	 public static final ItemSummonerOrb SUMMONER_ORB = new ItemSummonerOrb("summoner_orb", 0);
	 public static final ItemSummonerOrb ENCHANCED_SUMMONER_ORB = new ItemSummonerOrb("enhanced_summoner_orb", 1);
	
	    @Mod.EventBusSubscriber(modid = TheSummonerReference.MODID)
		public static class RegistrationHandler {

			@SubscribeEvent
			public static void registerItems(final RegistryEvent.Register<Item> event) {
				Item[] items = {
						SUMMONER_ORB,
						ENCHANCED_SUMMONER_ORB
				};

				final IForgeRegistry<Item> registry = event.getRegistry();

				for (final Item item : items) {
					registry.register(item);
					ITEMS.add(item);
				}
			}
	    		

	}
	    
	    public static void registerItemColors()
	    {
	    	Item[] coloredItems = new Item[] {SUMMONER_ORB, ENCHANCED_SUMMONER_ORB};
	    	MMItemModelManager.INSTANCE.registerItemColors(coloredItems);
	    }
}