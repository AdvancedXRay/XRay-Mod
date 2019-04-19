package com.xray.common;

import com.xray.common.proxy.ClientProxy;
import com.xray.common.reference.block.BlockItem;
import com.xray.common.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

@Mod(modid= Reference.MOD_ID, name= Reference.MOD_NAME, version=Reference.MOD_VERSION, updateJSON= Reference.UPDATE_JSON)
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class XRay
{
	public static ArrayList<BlockItem> blockList = new ArrayList<>();

	public static Minecraft mc = Minecraft.getMinecraft();

    // Radius +/- around the player to search. So 8 is 8 on left and right of player plus under the player. So 17x17 area.
    public static final int[] distanceList = new int[] {8, 16, 32, 48, 64, 80, 128, 256};

	public static Logger logger;

	// The instance of your mod that Forge uses.
	@Instance(Reference.MOD_ID)
	public static XRay instance;

	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide="com.xray.common.proxy.ClientProxy", serverSide="com.xray.common.proxy.ServerProxy")
	private static ClientProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
		logger.debug(I18n.format("xray.debug.init"));

		proxy.preInit( event );
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.init( event );
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit( event );
	}

	@EventHandler
	public void onExit(FMLServerStoppingEvent event)
	{
		proxy.onExit(event);
	}

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if (event.getModID().equals(Reference.MOD_ID))
		{
			ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
		}
	}
}
