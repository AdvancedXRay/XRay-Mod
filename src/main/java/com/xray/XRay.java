package com.xray;

import com.xray.keybinding.KeyBindings;
import com.xray.reference.Reference;
import com.xray.reference.block.BlockData;
import com.xray.reference.block.SimpleBlockData;
import com.xray.store.BlockStore;
import com.xray.store.GameBlockStore;
import com.xray.store.JsonStore;
import com.xray.xray.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(value= Reference.MOD_ID)
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
@OnlyIn(Dist.CLIENT)
public class XRay
{
	// This contains all of the games blocks to allow us to reference them
	// when needed. This allows us to avoid continually rebuilding
	public static GameBlockStore gameBlockStore = new GameBlockStore();

	public static Minecraft mc = Minecraft.getInstance();
	public static JsonStore blockStore = new JsonStore();

	public static Logger logger = LogManager.getLogger();

	public XRay() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

		eventBus.addListener(this::onSetup);
		eventBus.addListener(this::onLoadComplete);
//		eventBus.addListener(this::onConfigChanged);
		eventBus.addListener(this::onExit);
	}

	private void onSetup(final FMLCommonSetupEvent event) {
		logger.debug(I18n.format("xray.debug.init"));

		KeyBindings.setup();

		// Load the config
		Configuration.load();

		List<SimpleBlockData> data = blockStore.read();
		if( data.isEmpty() )
			return;

		ArrayList<BlockData> map = BlockStore.getFromSimpleBlockList(data);
		Controller.getBlockStore().setStore(map);
	}

	private void onLoadComplete(FMLLoadCompleteEvent event)
	{
		gameBlockStore.populate();
	}

	private void onExit(FMLServerStoppingEvent event)
	{
		Controller.shutdownExecutor();
	}

//	private void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
//	{
//		if (event.getModID().equals(Reference.MOD_ID))
//		{
////			Config.sync(Reference.MOD_ID, Config.Type.INSTANCE);
//		}
//	}
}
