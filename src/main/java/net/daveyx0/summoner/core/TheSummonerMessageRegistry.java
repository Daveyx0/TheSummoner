package net.daveyx0.summoner.core;

import net.daveyx0.multimob.message.MMMessageRegistry;
import net.daveyx0.summoner.message.MessageSummonable;
import net.minecraftforge.fml.relauncher.Side;

public class TheSummonerMessageRegistry extends MMMessageRegistry{

	public static void registerMessages()
	{
		registerMessage(MessageSummonable.Handler.class, MessageSummonable.class, Side.CLIENT);
	}
}
