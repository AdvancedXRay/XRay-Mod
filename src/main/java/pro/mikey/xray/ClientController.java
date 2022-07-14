package pro.mikey.xray;

import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pro.mikey.xray.keybinding.KeyBindings;
import pro.mikey.xray.store.BlockStore;
import pro.mikey.xray.store.DiscoveryStorage;
import pro.mikey.xray.store.GameBlockStore;
import pro.mikey.xray.utils.BlockData;
import pro.mikey.xray.xray.Controller;

import java.util.ArrayList;
import java.util.List;

public class ClientController {
    // This contains all of the games blocks to allow us to reference them
    // when needed. This allows us to avoid continually rebuilding
    public static GameBlockStore gameBlockStore = new GameBlockStore();
    public static DiscoveryStorage blockStore = new DiscoveryStorage();

    public static void setup() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        eventBus.addListener(ClientController::onSetup);
        eventBus.addListener(ClientController::onLoadComplete);
        eventBus.addListener(KeyBindings::registerKeyBinding);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Configuration.SPEC);

        // Keybindings
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
    }

    private static void onSetup(final FMLCommonSetupEvent event) {
        XRay.logger.debug(I18n.get("xray.debug.init"));

        KeyBindings.setup();
        List<BlockData.SerializableBlockData> data = ClientController.blockStore.read();
        if( data.isEmpty() )
            return;

        ArrayList<BlockData> map = BlockStore.getFromSimpleBlockList(data);
        Controller.getBlockStore().setStore(map);
    }

    private static void onLoadComplete(FMLLoadCompleteEvent event) {
        ClientController.gameBlockStore.populate();
    }
}
