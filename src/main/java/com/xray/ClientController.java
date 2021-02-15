package com.xray;

import com.xray.keybinding.KeyBindings;
import com.xray.store.BlockStore;
import com.xray.store.GameBlockStore;
import com.xray.store.JsonStore;
import com.xray.utils.BlockData;
import com.xray.xray.Controller;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;
import java.util.List;

public class ClientController {
    // This contains all of the games blocks to allow us to reference them
    // when needed. This allows us to avoid continually rebuilding
    public static GameBlockStore gameBlockStore = new GameBlockStore();
    public static JsonStore blockStore = new JsonStore();

    public static void setup() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        eventBus.addListener(ClientController::onSetup);
        eventBus.addListener(ClientController::onLoadComplete);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Configuration.SPEC);

        // Keybindings
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
    }

    private static void onSetup(final FMLCommonSetupEvent event) {
        XRay.logger.debug(I18n.format("xray.debug.init"));

        KeyBindings.setup();
        List<BlockData.SerializableBlockData> data = ClientController.blockStore.read();
        if( data.isEmpty() )
            return;

        ArrayList<BlockData> map = BlockStore.getFromSimpleBlockList(data);
        Controller.getBlockStore().setStore(map);
    }

    private static void onLoadComplete(FMLLoadCompleteEvent event)
    {
        ClientController.gameBlockStore.populate();
    }
}
