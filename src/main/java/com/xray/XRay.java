package com.xray;

import com.xray.keybinding.KeyBindings;
import com.xray.utils.BlockData;
import com.xray.store.BlockStore;
import com.xray.store.GameBlockStore;
import com.xray.store.JsonStore;
import com.xray.xray.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(value= XRay.MOD_ID)
@Mod.EventBusSubscriber(modid = XRay.MOD_ID)
@OnlyIn(Dist.CLIENT)
public class XRay
{
	public static final String MOD_ID = "xray";
	public static final String PREFIX_GUI = MOD_ID +":"+"textures/gui/";

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

		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Configuration.SPEC);

		// Keybindings
		MinecraftForge.EVENT_BUS.register(KeyBindings.class);
	}

	private void onSetup(final FMLCommonSetupEvent event) {
		logger.debug(I18n.format("xray.debug.init"));

		KeyBindings.setup();
		List<BlockData.SerializableBlockData> data = blockStore.read();
		if( data.isEmpty() )
			return;

		ArrayList<BlockData> map = BlockStore.getFromSimpleBlockList(data);
		Controller.getBlockStore().setStore(map);
	}

	private void onLoadComplete(FMLLoadCompleteEvent event)
	{
		gameBlockStore.populate();
	}
}
