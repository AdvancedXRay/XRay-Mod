package pro.mikey.xray.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import pro.mikey.xray.ClientController;
import pro.mikey.xray.XRay;
import pro.mikey.xray.screens.HudOverlay;
import pro.mikey.xray.core.ScanController;
import pro.mikey.xray.core.OutlineRender;

public class XRayFabric implements ClientModInitializer {
    private static final ResourceLocation HUD_ELEMENT_ID = XRay.id("xray_overlay");

    @Override
    public void onInitializeClient() {
        XRay.INSTANCE.init();

        KeyBindingHelper.registerKeyBinding(XRay.OPEN_GUI_KEY);
        KeyBindingHelper.registerKeyBinding(XRay.TOGGLE_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(this::clientTickEvent);
        ClientLifecycleEvents.CLIENT_STARTED.register((mc) -> ClientController.onSetup());
        WorldRenderEvents.LAST.register((context) -> OutlineRender.renderBlocks(context.matrixStack()));

        HudElementRegistry.addLast(HUD_ELEMENT_ID, (guiGraphics, tickCounter) -> HudOverlay.renderGameOverlayEvent(guiGraphics));
    }

    private void clientTickEvent(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.screen != null) {
            return;
        }

        ScanController.INSTANCE.requestBlockFinder(false);

        while (XRay.OPEN_GUI_KEY.consumeClick()) {
            XRay.INSTANCE.onOpenGuiKeyPressed();
        }

        while (XRay.TOGGLE_KEY.consumeClick()) {
            XRay.INSTANCE.onToggleKeyPressed();
        }
    }
}
