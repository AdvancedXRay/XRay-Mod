package pro.mikey.xray;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import pro.mikey.xray.keybinding.KeyBindings;
import pro.mikey.xray.xray.Controller;
import pro.mikey.xray.xray.Events;

public class ClientController {
    public static void setup(IEventBus eventBus) {

        eventBus.addListener(ClientController::onSetup);
        eventBus.addListener(KeyBindings::registerKeyBinding);

        ModLoadingContext.get().getActiveContainer()
                .registerConfig(ModConfig.Type.CLIENT, Configuration.SPEC);

        // Keybindings
        NeoForge.EVENT_BUS.addListener(KeyBindings::eventInput);
        NeoForge.EVENT_BUS.addListener(ClientController::onGameJoin);

        NeoForge.EVENT_BUS.addListener(Events::tickEnd);
        NeoForge.EVENT_BUS.addListener(Events::onWorldRenderLast);
    }

    private static void onSetup(final FMLCommonSetupEvent event) {
        XRay.logger.debug(I18n.get("xray.debug.init"));

        KeyBindings.setup();
        Controller.getBlockStore().recoverBlockStore();
    }

    private static void onGameJoin(final EntityJoinLevelEvent event) {
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
