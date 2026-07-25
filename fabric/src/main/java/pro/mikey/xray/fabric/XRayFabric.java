package pro.mikey.xray.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.mikey.xray.ClientController;
import pro.mikey.xray.XRay;
import pro.mikey.xray.core.OutlineRender;
import pro.mikey.xray.screens.HudOverlay;
import pro.mikey.xray.core.ScanController;

public class XRayFabric implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(XRayFabric.class);
    private static final Identifier HUD_ELEMENT_ID = XRay.id("xray_overlay");

    @Override
    public void onInitializeClient() {
        XRay.INSTANCE.init();

        KeyMappingHelper.registerKeyMapping(XRay.OPEN_GUI_KEY);
        KeyMappingHelper.registerKeyMapping(XRay.TOGGLE_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(this::clientTickEvent);
        ClientLifecycleEvents.CLIENT_STARTED.register((mc) -> ClientController.onSetup());
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context -> OutlineRender.renderBlocks());
        LOGGER.info("Registered OutlineRender on LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES");

        if (FabricLoader.getInstance().isModLoaded("iris")) {
            try {
                IrisCompat.register();
                LOGGER.info("Registered X-Ray outline pipeline with Iris's shader compatibility layer");
            } catch (Throwable t) {
                LOGGER.warn("Failed to register Iris shader compatibility - outlines may not render correctly with a shader pack active", t);
            }
        }

        HudElementRegistry.addLast(HUD_ELEMENT_ID, (guiGraphics, tickCounter) -> HudOverlay.renderGameOverlayEvent(guiGraphics));
    }

    private void clientTickEvent(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gui.screen() != null) {
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
