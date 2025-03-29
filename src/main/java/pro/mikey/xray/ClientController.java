package pro.mikey.xray;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import pro.mikey.xray.keybinding.KeyBindings;
import pro.mikey.xray.store.BlockStore;
import pro.mikey.xray.store.DiscoveryStorage;
import pro.mikey.xray.store.GameBlockStore;
import pro.mikey.xray.utils.BlockData;
import pro.mikey.xray.xray.Controller;
import pro.mikey.xray.xray.Events;

import java.util.ArrayList;
import java.util.List;

public class ClientController {
    // This contains all the games blocks to allow us to reference them
    // when needed. This allows us to avoid continually rebuilding
    public static GameBlockStore gameBlockStore = new GameBlockStore();
    public static DiscoveryStorage blockStore;

    static void onSetup(final FMLClientSetupEvent event) {
        XRay.logger.debug(I18n.get("xray.debug.init"));

        blockStore = new DiscoveryStorage();
        ClientController.gameBlockStore.populate();
        Controller.init();

        KeyBindings.setup();
        List<BlockData.SerializableBlockData> data = ClientController.blockStore.read();
        if( data.isEmpty() )
            return;

        ArrayList<BlockData> map = BlockStore.getFromSimpleBlockList(data);
        Controller.getBlockStore().setStore(map);
    }

    static void onGameJoin(final EntityJoinLevelEvent event) {
        if (!Configuration.firstRun.get()) {
            return;
        }

        if (!event.getLevel().isClientSide() || !( event.getEntity() instanceof Player player)) {
            return;
        }

        if (player != Minecraft.getInstance().player) {
            return;
        }

        player.displayClientMessage(Component.translatable("xray.chat.first-time", KeyBindings.toggleGui.getKey().getDisplayName().copy().withStyle(ChatFormatting.GREEN), KeyBindings.toggleXRay.getKey().getDisplayName().copy().withStyle(ChatFormatting.GREEN)), false);
        player.displayClientMessage(Component.translatable("xray.chat.first-time-line-2"), false);
        Configuration.firstRun.set(false);
        Configuration.firstRun.save();
    }
}
